package com.illucit.instatrie.index;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.illucit.instatrie.highlight.HighlightedString;

public interface PrefixIndex<T extends Serializable> extends Serializable {

	/**
	 * Create or recreate prefix index. Previously allocated indexes are
	 * cleared. The operation must be threadsafe, so the new index is switched
	 * as atomic operation (and other threads will have access to the old index
	 * until it is completely updated).
	 * 
	 * @param models
	 *            collection of models to index
	 */
	public void createIndex(Collection<T> models);

	/**
	 * Search the index for a query string and return a stream of indexed
	 * models, which match the query prefix criteria. The query string is
	 * splitted with the given search word splitter (the default search word
	 * splitter is extracting the query words by finding connected alphanumeric
	 * sequences). All query words must be found (completely or as prefixes) in
	 * the index for an entry to be considered found. The stream still requires
	 * a terminal operation in order to be processed.
	 * 
	 * @param query
	 *            query string
	 * @return list of models, in the same order or the iterator of the indexed
	 *         collection
	 */
	public Stream<T> searchStream(String query);

	/**
	 * Search the index for a query string and return a list of indexed models,
	 * which match the query prefix criteria. The query string is splitted with
	 * the given search word splitter (the default search word splitter is
	 * extracting the query words by finding connected alphanumeric sequences).
	 * All query words must be found (completely or as prefixes) in the index
	 * for an entry to be considered found.
	 * 
	 * @param query
	 *            query string
	 * @return list of models, in the same order or the iterator of the indexed
	 *         collection
	 */
	public List<T> search(String query);

	/**
	 * Search the index for a query string and return a list of indexed models,
	 * which match the query prefix criteria. The query string is splitted with
	 * the given search word splitter (the default search word splitter is
	 * extracting the query words by finding connected alphanumeric sequences).
	 * All query words must be found (completely or as prefixes) in the index
	 * for an entry to be considered found. If there are more entries found than
	 * the parameter limit, all remaining are omitted from the result list.
	 * 
	 * @param query
	 *            query string
	 * @param maxSize
	 *            as soon as there are so many entries found, all others are
	 *            omitted
	 * @return list of models, in the same order or the iterator of the indexed
	 *         collection
	 */
	public List<T> search(String query, long maxSize);

	/**
	 * Search the index for a query string and return a list of indexed models,
	 * which match the query criteria. The query string is splitted with the
	 * given search word splitter (the default search word splitter is
	 * extracting the query words by finding connected alphanumeric sequences).
	 * All query words must be found completely in the index for an entry to be
	 * considered found.
	 * 
	 * @param query
	 *            query string
	 * @return list of models, in the same order or the iterator of the indexed
	 *         collection
	 */
	public List<T> searchExact(String query);

	/**
	 * Get all model entries in the index as list (in the same order they were
	 * in the collection iterator on index building).
	 * 
	 * @return list of models
	 */
	public List<T> getAll();

	/**
	 * Highlight the hits from a prefix query in a model value String.
	 * 
	 * @param modelValue
	 *            value to resolve highlights from
	 * @param query
	 *            query string with prefixes to be highlighted
	 * @return highlighted String
	 */
	public HighlightedString getHighlighted(String modelValue, String query);

	/**
	 * Highlight the hits from a prefix query in a model value String with HTML.
	 * 
	 * @param modelValue
	 *            value to resolve highlights from (with HTML tags)
	 * @param query
	 *            query string with prefixes to be highlighted
	 * @return highlighted String
	 */
	public HighlightedString getHighlightedHtml(String modelValue, String query);

	/**
	 * Get an instance of {@link PrefixIndex} which also implements the
	 * {@link List} interface. The returned instance is completely entangled
	 * with the current object, so that both objects share a state and each trie
	 * operation on one of the objects also affects the other object in the same
	 * way.
	 * 
	 * @return decorated prefix index
	 */
	public PrefixIndexListDecorator<T> decorateAsList();

	/**
	 * Get a view on the current prefix index which only returns models matching
	 * the given filter predicate. The returned instance is completely entangled
	 * with the current object, so that both objects share a state and each trie
	 * operation on one of the objects also affects the other object in the same
	 * way. The original prefix index won't be modified by this operation. If
	 * the current instance already contains a filter, the new predicate will be
	 * added as additional filter predicate.
	 * 
	 * @param filterFunction
	 *            filter function to filter search stream
	 * @return decorated prefix index
	 */
	public PrefixIndex<T> getFilteredView(Predicate<T> filterFunction);

}