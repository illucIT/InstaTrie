package com.illucit.instatrie.splitter;

import java.util.Set;

/**
 * Interface for classes which are capable of splitting a value String into a
 * highlighted string by locating all prefix matches inside
 * 
 * @author Christian Simon
 *
 */
public interface SubwordHighlighter {

	/**
	 * Search value String for prefixes in query words and highlight in original
	 * String.
	 * 
	 * @param value
	 *            value String to split and transform
	 * @param queryWords
	 *            query words to highlight in prefixes
	 * @return highlighted String
	 */
	public HighlightedString highlightSubwordPrefixes(String value, Set<String> queryWords);
	
	/**
	 * Search value String (with HTML tags) for prefixes in query words and highlight in original
	 * String.
	 * 
	 * @param value
	 *            value HTML String to split and transform
	 * @param queryWords
	 *            query words to highlight in prefixes
	 * @return highlighted String
	 */
	public HighlightedString highlightSubwordPrefixesWithHtml(String value, Set<String> queryWords);

}
