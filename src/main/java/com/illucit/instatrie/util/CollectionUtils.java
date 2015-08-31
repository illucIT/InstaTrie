package com.illucit.instatrie.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for collections.
 * 
 * @author Christian Simon
 *
 */
public class CollectionUtils {

	public static final char[] EMPTY_CHAR_ARRAY = new char[0];

	/**
	 * Create subarray of char array.
	 * 
	 * @param array
	 *            input array
	 * @param startIndex
	 *            start index (inclusive)
	 * @param endIndex
	 *            end index (exclusive)
	 * @return sub array of size (end index - start index)
	 */
	public static char[] subarray(char[] array, int startIndex, int endIndex) {
		startIndex = Math.max(startIndex, 0);
		endIndex = Math.min(endIndex, array.length);
		int newSize = endIndex - startIndex;
		if (newSize <= 0) {
			return EMPTY_CHAR_ARRAY;
		}

		char[] subarray = new char[newSize];
		System.arraycopy(array, startIndex, subarray, 0, newSize);
		return subarray;
	}

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
