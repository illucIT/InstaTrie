package com.illucit.instatrie.splitter;

import java.util.Set;

/**
 * A word splitter constitutes a function to reduce any complex model to a set
 * of words, which can be used to allocate the model in an indexed data
 * structure.
 * 
 * @author Christian Simon
 *
 * @param <T>
 *            model type
 */
@FunctionalInterface
public interface WordSplitter<T> {

	/**
	 * Split the data of a model to a set of search words that identify the
	 * model for searching in a search index. if null is returned, the model
	 * won't be indexed.
	 * 
	 * @param data
	 *            model data
	 * @return set of search strings or null
	 */
	public Set<String> split(T data);

}
