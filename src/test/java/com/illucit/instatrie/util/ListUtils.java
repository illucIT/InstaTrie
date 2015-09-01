package com.illucit.instatrie.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for easy list creation (for Unit tests).
 * 
 * @author Christian Simon
 *
 */
public class ListUtils {

	/**
	 * Create {@link ArrayList} with some elements contained.
	 * 
	 * @param elements
	 *            elements to be contained in the list.
	 * @param <T>
	 *            type of list elements
	 * @return list
	 */
	@SafeVarargs
	public static <T> List<T> newList(T... elements) {
		ArrayList<T> result = new ArrayList<>();
		for (T element : elements) {
			result.add(element);
		}
		return result;
	}

}
