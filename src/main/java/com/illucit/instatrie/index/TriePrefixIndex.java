package com.illucit.instatrie.index;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.illucit.instatrie.splitter.HighlightedString;
import com.illucit.instatrie.splitter.StringWordSplitter;
import com.illucit.instatrie.splitter.StringWordSplitter.IdentityStringWordSplitter;
import com.illucit.instatrie.splitter.SubwordHighlighter;
import com.illucit.instatrie.splitter.WordSplitter;
import com.illucit.instatrie.trie.Trie;

/**
 * Data structure to manage a collection of data objects and allow to find
 * entries from the collection efficiently with prefix search strings. The data
 * structure is designed to be filled once and then only be queried.
 * 
 * @author Christian Simon
 *
 * @param <T>
 *            model type
 */
public class TriePrefixIndex<T extends Serializable> implements PrefixIndex<T>, SubwordHighlighter {

	private static final long serialVersionUID = 2489840590069035280L;

	/*
	 * Configuration
	 */

	/**
	 * Splitter function to get search words from a model instance.
	 */
	private final WordSplitter<T> dataWordSplitter;

	/**
	 * Splitter function to get query words from a query string.
	 */
	private final WordSplitter<String> searchWordSplitter;

	/**
	 * Highlighter for highlighting matched prefixes in model value strings.
	 */
	private final SubwordHighlighter highlighter;

	/*
	 * Index data
	 */

	/**
	 * Atomic reference holding the mutable state of the trie.
	 */
	private final AtomicReference<TriePrefixIndexData<T>> indexData;

	/*
	 * Constructors
	 */

	/**
	 * Create prefix index with default query splitter.
	 * 
	 * @param stringSplitterFunction
	 *            splitter function to get a search String from a model instance
	 */
	public TriePrefixIndex(Function<T, String> stringSplitterFunction) {
		this(new StringWordSplitter<>(stringSplitterFunction), IdentityStringWordSplitter.instance());
	}

	/**
	 * Create prefix index with default query splitter.
	 * 
	 * @param dataWordSplitter
	 *            splitter function to get a set of search words from a model
	 *            instance
	 */
	public TriePrefixIndex(WordSplitter<T> dataWordSplitter) {
		this(dataWordSplitter, IdentityStringWordSplitter.instance());
	}

	/**
	 * Create prefix index.
	 * 
	 * @param dataWordSplitter
	 *            splitter function to get a set of search words from a model
	 *            instance
	 * @param searchWordSplitter
	 *            splitter function to get query words from a query string adn
	 *            also for highlighting
	 */
	public TriePrefixIndex(WordSplitter<T> dataWordSplitter, StringWordSplitter<String> searchWordSplitter) {
		this(dataWordSplitter, searchWordSplitter, searchWordSplitter);
	}

	/**
	 * Create prefix index.
	 * 
	 * @param dataWordSplitter
	 *            splitter function to get a set of search words from a model
	 *            instance
	 * @param searchWordSplitter
	 *            splitter function to get query words from a query string
	 * @param highlighter
	 *            highlighter for highlighting of matches prefixes
	 */
	public TriePrefixIndex(WordSplitter<T> dataWordSplitter, WordSplitter<String> searchWordSplitter,
			SubwordHighlighter highlighter) {
		this.dataWordSplitter = dataWordSplitter;
		this.searchWordSplitter = searchWordSplitter;
		this.highlighter = highlighter;
		this.indexData = new AtomicReference<>(new TriePrefixIndexData<>());
	}

	/**
	 * Create view on other trie prefix index. The data structures are
	 * referenced, so the new instance is a view on the original prefix index
	 * (so changes are propagated in both directions). This can be used for
	 * decorating.
	 * 
	 * @param other
	 *            other prefix index
	 */
	private TriePrefixIndex(TriePrefixIndex<T> other) {
		this.dataWordSplitter = other.dataWordSplitter;
		this.searchWordSplitter = other.searchWordSplitter;
		this.highlighter = other.highlighter;

		// Bind reference to index data to index data of other prefix index
		this.indexData = other.indexData;
	}

	/*
	 * PrefixIndex API methods
	 */

	@Override
	public void createIndex(Collection<T> models) {
		TriePrefixIndexData<T> data = new TriePrefixIndexData<>();
		Trie<HashSet<String>> wordsTrie = data.getWordsTrie();
		ArrayList<T> modelData = data.getModelData();
		Map<String, Set<Integer>> wordsToModelIndex = data.getWordsToModelIndex();

		modelData.ensureCapacity(models.size());

		TreeSet<String> allSearchStrings = new TreeSet<>();

		// Iterate over model data
		for (T model : models) {

			// Store model in array list
			Integer modelIndex = modelData.size(); // next index to be inserted
			modelData.add(model);

			// Connect model with complete search strings
			Set<String> searchStrings = dataWordSplitter.split(model);
			for (String searchWord : searchStrings) {
				if (!wordsToModelIndex.containsKey(searchWord)) {
					wordsToModelIndex.put(searchWord, new HashSet<>());
				}
				wordsToModelIndex.get(searchWord).add(modelIndex);
				allSearchStrings.add(searchWord);
			}

		}

		// Store search strings and their substrings in trie
		Map<String, Set<String>> wordsByPrefix = new HashMap<>();
		// HashMultimap<String, String> wordsByPrefix = HashMultimap.create();
		for (String searchWord : allSearchStrings) {
			for (int i = 1; i <= searchWord.length(); i++) {
				String key = searchWord.substring(0, i);
				if (!wordsByPrefix.containsKey(key)) {
					wordsByPrefix.put(key, new HashSet<>());
				}
				wordsByPrefix.get(key).add(searchWord);
			}
		}

		TreeSet<String> prefixes = new TreeSet<>(COMPARE_BY_LENGTH);
		prefixes.addAll(wordsByPrefix.keySet());
		for (String prefix : prefixes) {
			wordsTrie.insert(prefix, new HashSet<>(wordsByPrefix.get(prefix)));
		}

		indexData.set(data);
	}

	@Override
	public Stream<T> searchStream(String query) {
		TriePrefixIndexData<T> data = indexData.get();

		Set<String> queryPrefixes = searchWordSplitter.split(query);
		if (queryPrefixes == null || queryPrefixes.isEmpty()) {
			// No filtering enabled
			return data.getModelData().stream();
		}
		Set<Integer> filteredIndices = new HashSet<>();
		boolean empty = true;
		for (String queryPrefix : queryPrefixes) {
			HashSet<String> wordsForPrefix = data.getWordsTrie().getData(queryPrefix);
			if (wordsForPrefix == null) {
				// No words found for query - so no models can match it
				return Stream.empty();
			}
			HashSet<Integer> filteredIndicesForWord = new HashSet<>();
			for (String word : wordsForPrefix) {
				filteredIndicesForWord.addAll(data.getWordsToModelIndex().getOrDefault(word, new HashSet<>()));
			}

			if (empty) {
				// First result set is basis of intersection
				filteredIndices.addAll(filteredIndicesForWord);
				empty = false;
				continue;
			}
			// Intersect with previous sets
			filteredIndices.retainAll(filteredIndicesForWord);
		}

		// @formatter:off
		return getArrayListStream(data.getModelData())
				.filter(e -> filteredIndices.contains(e.getIndex()))
				.map(e -> e.getData());
		// @formatter:on
	}

	@Override
	public List<T> search(String query) {
		return unmodifiableList(searchStream(query).collect(toList()));
	}

	@Override
	public List<T> search(String query, long maxSize) {
		return unmodifiableList(searchStream(query).limit(maxSize).collect(toList()));
	}

	@Override
	public List<T> searchExact(String query) {
		TriePrefixIndexData<T> data = indexData.get();

		Set<String> queryWords = searchWordSplitter.split(query);
		if (queryWords == null || queryWords.isEmpty()) {
			// No filtering enabled
			return unmodifiableList(data.getModelData());
		}

		Set<Integer> filteredIndices = new HashSet<>();
		boolean empty = true;
		for (String queryWord : queryWords) {
			Set<Integer> filteredIndicesForWord = data.getWordsToModelIndex().getOrDefault(queryWord, new HashSet<>());
			if (empty) {
				// First result set is basis of intersection
				filteredIndices.addAll(filteredIndicesForWord);
				empty = false;
				continue;
			}
			// Intersect with previous sets
			filteredIndices.retainAll(filteredIndicesForWord);
		}

		// @formatter:off
		return getArrayListStream(data.getModelData())
				.filter(e -> filteredIndices.contains(e.getIndex()))
				.map(e -> e.getData())
				.collect(toList());
		// @formatter:on
	}

	@Override
	public List<T> getAll() {
		TriePrefixIndexData<T> data = indexData.get();
		return unmodifiableList(data.getModelData());
	}

	@Override
	public HighlightedString getHighlighted(String modelValue, String query) {
		Set<String> queryWords = searchWordSplitter.split(query);
		return highlightSubwordPrefixes(modelValue, queryWords);
	}

	@Override
	public HighlightedString getHighlightedHtml(String modelValue, String query) {
		Set<String> queryWords = searchWordSplitter.split(query);
		return highlightSubwordPrefixesWithHtml(modelValue, queryWords);
	}

	@Override
	public PrefixIndexListDecorator<T> decorateAsList() {
		return new TriePrefixIndexListDecorator<>(this);
	}

	@Override
	public TriePrefixIndexFiltered<T> getFilteredView(Predicate<T> filterFunction) {
		return new TriePrefixIndexFiltered<>(this, filterFunction);
	}

	/*
	 * SubwordHighlighter API methods
	 */

	@Override
	public HighlightedString highlightSubwordPrefixes(String value, Set<String> queryWords) {
		// delegate to subword highlighter instance
		return highlighter.highlightSubwordPrefixes(value, queryWords);
	}

	@Override
	public HighlightedString highlightSubwordPrefixesWithHtml(String value, Set<String> queryWords) {
		// delegate to subword highlighter instance
		return highlighter.highlightSubwordPrefixesWithHtml(value, queryWords);
	}

	/*
	 * Utility functions.
	 */

	/**
	 * Get a stream of array list entries from an array list.
	 * 
	 * @param list
	 *            array list for data
	 * @return stream
	 */
	private static <T extends Serializable> Stream<ArrayListEntry<T>> getArrayListStream(ArrayList<T> list) {
		ArrayListEntryIterator<T> iterator = new ArrayListEntryIterator<>(list);
		Spliterator<ArrayListEntry<T>> spliterator = Spliterators.spliterator(iterator, list.size(), 0);
		return StreamSupport.stream(spliterator, false);
	}

	@Override
	public String toString() {
		TriePrefixIndexData<T> data = indexData.get();

		StringBuffer result = new StringBuffer();
		for (String word : new TreeSet<>(data.getWordsToModelIndex().keySet())) {
			result.append(word).append(": ");
			Set<Integer> filteredIndices = data.getWordsToModelIndex().get(word);
			result.append(getArrayListStream(data.getModelData()).filter(e -> filteredIndices.contains(e.getIndex()))
					.map(e -> e.getData()).collect(toList()));
			result.append("\n");
		}
		result.append(data.getWordsTrie());
		return result.toString();
	}

	/*
	 * Utility classes
	 */

	/**
	 * Comparator for String with ordering by character length, then by natural
	 * String ordering.
	 */
	private static Comparator<String> COMPARE_BY_LENGTH = (o1, o2) -> {
		if (o1.length() != o2.length()) {
			return Integer.compare(o1.length(), o2.length());
		}
		return String.CASE_INSENSITIVE_ORDER.compare(o1, o2);
	};

	/**
	 * Bean to bundle the mutable data of the {@link TriePrefixIndex} in order
	 * to allow atomic updates.
	 * 
	 * @author Christian Simon
	 * @param <T>
	 *            model type
	 *
	 */
	private static class TriePrefixIndexData<T> implements Serializable {

		private static final long serialVersionUID = 3013195355271753160L;

		/**
		 * This trie stores to each prefix the list of all words in the word
		 * corpus that have that prefix.
		 */
		private final Trie<HashSet<String>> wordsTrie;

		/**
		 * This array (list) stores all indexed models and can be accessed by
		 * the index number.
		 */
		private final ArrayList<T> modelData;

		/**
		 * This multimap stores for each word (map key) the index numbers of all
		 * models
		 */
		private final Map<String, Set<Integer>> wordsToModelIndex;

		/**
		 * Create empty index data bean.
		 */
		public TriePrefixIndexData() {
			this.wordsTrie = new Trie<>();
			this.modelData = new ArrayList<>();
			this.wordsToModelIndex = new HashMap<>();
		}

		/**
		 * Get list where models are stored.
		 * 
		 * @return list as {@link ArrayList}
		 */
		public ArrayList<T> getModelData() {
			return modelData;
		}

		/**
		 * Get multimap where is stored, which words are contained in which
		 * models.
		 * 
		 * @return index as {@link HashMultimap}
		 */
		public Map<String, Set<Integer>> getWordsToModelIndex() {
			return wordsToModelIndex;
		}

		/**
		 * Get the trie, where the words for prefixes are stored.
		 * 
		 * @return words index as {@link Trie}
		 */
		public Trie<HashSet<String>> getWordsTrie() {
			return wordsTrie;
		}

	}

	/**
	 * Customized Iterator for array list entries.
	 * 
	 * @author Christian Simon
	 *
	 * @param <T>
	 *            payload data
	 */
	private static class ArrayListEntryIterator<T extends Serializable> implements Iterator<ArrayListEntry<T>> {

		private final ArrayList<T> list;

		private int index;

		/**
		 * Create iterator from list.
		 * 
		 * @param list
		 *            target array list
		 */
		public ArrayListEntryIterator(ArrayList<T> list) {
			this.list = list;
			this.index = 0;
		}

		@Override
		public boolean hasNext() {
			return list.size() > index;
		}

		@Override
		public ArrayListEntry<T> next() {
			int resultIndex = index++;
			return new ArrayListEntry<>(resultIndex, list.get(resultIndex));
		}

	}

	/**
	 * Entry for array lists (index plus payload data).
	 * 
	 * @author Christian Simon
	 *
	 * @param <T>
	 *            payload type
	 */
	private static class ArrayListEntry<T extends Serializable> implements Serializable {

		private static final long serialVersionUID = 1705097382050255522L;

		private final int index;

		private final T data;

		/**
		 * Create array list entry.
		 * 
		 * @param index
		 *            index of entry
		 * @param data
		 *            payload data of entry
		 */
		public ArrayListEntry(int index, T data) {
			this.index = index;
			this.data = data;
		}

		/**
		 * Get index.
		 * 
		 * @return list index
		 */
		public int getIndex() {
			return index;
		}

		/**
		 * Get payload data.
		 * 
		 * @return data
		 */
		public T getData() {
			return data;
		}

	}

	/*
	 * Index Prefix Modifications (Subclasses with additional functionality)
	 */

	/**
	 * Decorator implementation of {@link PrefixIndexListDecorator} for
	 * {@link TriePrefixIndex}.
	 * 
	 * @author Christian Simon
	 *
	 * @param <T>
	 *            model type
	 */
	public static class TriePrefixIndexListDecorator<T extends Serializable> extends TriePrefixIndex<T> implements
			PrefixIndexListDecorator<T> {

		private static final long serialVersionUID = 8735909387455764162L;

		/**
		 * Decorate undecorated instance.
		 * 
		 * @param undecorated
		 *            trie prefix index instance
		 */
		private TriePrefixIndexListDecorator(TriePrefixIndex<T> undecorated) {
			super(undecorated);
		}

		@Override
		public TriePrefixIndexListDecorator<T> decorateAsList() {
			return this;
		}

		@Override
		public TriePrefixIndexListDecoratorFiltered<T> getFilteredView(Predicate<T> filterFunction) {
			return new TriePrefixIndexListDecoratorFiltered<>(this, filterFunction);
		}
	}

	/**
	 * View on a {@link TriePrefixIndex} where all result lists are filtered by
	 * a target predicate function. The internal data structures are completely
	 * entangled with the given dalagate trie prefix index, so the state is
	 * share across both instances.
	 * 
	 * @author Christian Simon
	 *
	 * @param <T>
	 *            model type
	 */
	public static class TriePrefixIndexFiltered<T extends Serializable> extends TriePrefixIndex<T> {

		private static final long serialVersionUID = -4540409786854800858L;

		private final Predicate<T> filterFunction;

		/**
		 * Upgrade from common prefix index.
		 * 
		 * @param delegate
		 *            prefix index delegate
		 * @param filterFunction
		 *            filter predicate
		 */
		public TriePrefixIndexFiltered(TriePrefixIndex<T> delegate, Predicate<T> filterFunction) {
			super(delegate);
			this.filterFunction = filterFunction;
		}

		/**
		 * Upgrade from {@link TriePrefixIndexFiltered} with an additional
		 * filter predicate.
		 * 
		 * @param delegate
		 *            prefix index delegate
		 * @param additionalFilterFunction
		 *            additional filter predicate
		 */
		public TriePrefixIndexFiltered(TriePrefixIndexFiltered<T> delegate, Predicate<T> additionalFilterFunction) {
			super(delegate);
			this.filterFunction = delegate.getFilterFunction().and(additionalFilterFunction);
		}

		/**
		 * Get the filter predicate used by this view.
		 * 
		 * @return filter method
		 */
		public Predicate<T> getFilterFunction() {
			return filterFunction;
		}

		@Override
		public List<T> getAll() {
			return super.getAll().stream().filter(filterFunction).collect(toList());
		}

		@Override
		public Stream<T> searchStream(String query) {
			return super.searchStream(query).filter(filterFunction);
		}

		@Override
		public List<T> searchExact(String query) {
			return super.searchExact(query).stream().filter(filterFunction).collect(toList());
		}

		@Override
		public TriePrefixIndexListDecoratorFiltered<T> decorateAsList() {
			return new TriePrefixIndexListDecoratorFiltered<>(this);
		}

		@Override
		public TriePrefixIndexFiltered<T> getFilteredView(Predicate<T> filterFunction) {
			return new TriePrefixIndexFiltered<T>(this, filterFunction);
		}

	}

	/**
	 * Decorator implementation of {@link PrefixIndexListDecorator} for
	 * {@link TriePrefixIndexFiltered}.
	 * 
	 * @author Christian Simon
	 *
	 * @param <T>
	 *            model type
	 */
	public static class TriePrefixIndexListDecoratorFiltered<T extends Serializable> extends TriePrefixIndexFiltered<T>
			implements PrefixIndexListDecorator<T> {

		private static final long serialVersionUID = -6157245745365047995L;

		/**
		 * Upgrade from {@link TriePrefixIndexListDecorator} with a filter
		 * predicate.
		 * 
		 * @param delegate
		 *            prefix index delegate
		 * @param filterFunction
		 *            filter predicate
		 */
		public TriePrefixIndexListDecoratorFiltered(TriePrefixIndexListDecorator<T> delegate,
				Predicate<T> filterFunction) {
			super(delegate, filterFunction);
		}

		/**
		 * Upgrade from {@link TriePrefixIndexFiltered} to list decorator.
		 * 
		 * @param delegate
		 *            prefix index delegate
		 */
		public TriePrefixIndexListDecoratorFiltered(TriePrefixIndexFiltered<T> delegate) {
			super(delegate, delegate.getFilterFunction());
		}

		/**
		 * Upgrade from {@link TriePrefixIndexListDecoratorFiltered} with an
		 * additional filter predicate.
		 * 
		 * @param delegate
		 *            prefix index delegate
		 * @param additionalFilterFunction
		 *            additional filter predicate
		 */
		public TriePrefixIndexListDecoratorFiltered(TriePrefixIndexListDecoratorFiltered<T> delegate,
				Predicate<T> additionalFilterFunction) {
			super(delegate, delegate.getFilterFunction().and(additionalFilterFunction));
		}

		@Override
		public TriePrefixIndexListDecoratorFiltered<T> decorateAsList() {
			return this;
		}

		@Override
		public TriePrefixIndexListDecoratorFiltered<T> getFilteredView(Predicate<T> filterFunction) {
			return new TriePrefixIndexListDecoratorFiltered<T>(this, filterFunction);
		}

	}

}
