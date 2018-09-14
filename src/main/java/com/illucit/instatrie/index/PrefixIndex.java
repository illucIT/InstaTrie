package com.illucit.instatrie.index;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.illucit.instatrie.highlight.HighlightedString;

public interface PrefixIndex<T extends Serializable> extends PrefixSearch<T>, Serializable {

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

}