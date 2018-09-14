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

import com.illucit.instatrie.highlight.HighlightedString;
import com.illucit.instatrie.highlight.SubwordHighlighter;
import com.illucit.instatrie.splitter.StringWordSplitter;
import com.illucit.instatrie.splitter.StringWordSplitter.IdentityStringWordSplitter;
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

		if (query == null) {
			// No filtering enabled
			return data.getModelData().stream();
		}
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
				.map(ArrayListEntry::getData);
		// @formatter:on
	}

	@Override
	public Stream<T> searchExactStream(String query) {
		TriePrefixIndexData<T> data = indexData.get();

		if (query == null) {
			// No filtering enabled
			return data.getModelData().stream();
		}
		Set<String> queryWords = searchWordSplitter.split(query);
		if (queryWords == null || queryWords.isEmpty()) {
			// No filtering enabled
			return data.getModelData().stream();
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
				.map(ArrayListEntry::getData);
		// @formatter:on
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

		StringBuilder result = new StringBuilder();
		for (String word : new TreeSet<>(data.getWordsToModelIndex().keySet())) {
			result.append(word).append(": ");
			Set<Integer> filteredIndices = data.getWordsToModelIndex().get(word);
			result.append(getArrayListStream(data.getModelData()).filter(e -> filteredIndices.contains(e.getIndex()))
					.map(ArrayListEntry::getData).collect(toList()));
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
		 * @return index as multi map
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

}
