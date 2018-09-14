package com.illucit.instatrie.trie;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Interface of a dictionary to store Strings together with payload data, with
 * efficient methods to find prefixes.
 * 
 * @author Christian Simon
 *
 * @param <T>
 *            payload type
 */
public interface PrefixDictionary<T extends Serializable> extends Serializable {

	/**
	 * Insert a word (with no payload data) inside the trie.
	 * 
	 * @param word
	 *            word to be inserted
	 */
	default void insert(String word) {
		insert(word.toCharArray(), 0, word.length(), null);
	}

	/**
	 * Insert a word (with no payload data) inside the trie.
	 * 
	 * @param word
	 *            word to be inserted
	 */
	default void insert(char[] word) {
		insert(word, 0, word.length, null);
	}

	/**
	 * Insert a word with payload data inside the trie.
	 * 
	 * @param word
	 *            word to be inserted
	 * @param data
	 *            payload data
	 */
	default void insert(String word, T data) {
		insert(word.toCharArray(), 0, word.length(), data);
	}

	/**
	 * Insert a word with payload data inside the trie.
	 * 
	 * @param word
	 *            word to be inserted
	 * @param data
	 *            payload data
	 */
	default void insert(char[] word, T data) {
		insert(word, 0, word.length, data);
	}

	/**
	 * Insert a substring of word (with no payload data) inside the trie.
	 * 
	 * @param word
	 *            word to be inserted
	 * @param startIndex
	 *            index to begin the substring with (inclusive)
	 * @param endIndex
	 *            index to end the substring at (exclusive)
	 */
	default void insert(String word, int startIndex, int endIndex) {
		insert(word.toCharArray(), startIndex, endIndex, null);
	}

	/**
	 * Insert a substring of word (with no payload data) inside the trie.
	 * 
	 * @param word
	 *            word to be inserted
	 * @param startIndex
	 *            index to begin the substring with (inclusive)
	 * @param endIndex
	 *            index to end the substring at (exclusive)
	 */
	default void insert(char[] word, int startIndex, int endIndex) {
		insert(word, startIndex, endIndex, null);
	}

	/**
	 * Insert a substring of word with payload data inside the trie.
	 * 
	 * @param word
	 *            word to be inserted
	 * @param startIndex
	 *            index to begin the substring with (inclusive)
	 * @param endIndex
	 *            index to end the substring at (exclusive)
	 * @param data
	 *            payload data
	 */
	default void insert(String word, int startIndex, int endIndex, T data) {
		insert(word.toCharArray(), startIndex, endIndex, data);
	}

	/**
	 * Insert a substring of word with payload data inside the trie.
	 * 
	 * @param word
	 *            word to be inserted
	 * @param startIndex
	 *            index to begin the substring with (inclusive)
	 * @param endIndex
	 *            index to end the substring at (exclusive)
	 * @param data
	 *            payload data
	 */
	void insert(char[] word, int startIndex, int endIndex, T data);

	/**
	 * Clear all contained words and playload data.
	 */
	void clear();

	/**
	 * Check if a word is either included completely or as prefix in the trie.
	 * 
	 * @param word
	 *            word to search for
	 * @return true if the word is included either completely or as prefix of
	 *         another word
	 */
	default boolean containsPrefix(String word) {
		return containsPrefix(word.toCharArray());
	}

	/**
	 * Check if a word is either included completely or as prefix in the trie.
	 * 
	 * @param word
	 *            word to search for
	 * @return true if the word is included either completely or as prefix of
	 *         another word
	 */
	boolean containsPrefix(char[] word);

	/**
	 * Check if a word is included exactly in the trie.
	 * 
	 * @param word
	 *            word to search for
	 * @return true if the word is included in the trie
	 */
	default boolean contains(String word) {
		return contains(word.toCharArray());
	}

	/**
	 * Check if a word is included exactly in the trie.
	 * 
	 * @param word
	 *            word to search for
	 * @return true if the word is included in the trie
	 */
	boolean contains(char[] word);

	/**
	 * Delete the data associated with the given word. If the word is not
	 * contained in the dictionary, nothing happens.
	 * 
	 * @param word
	 *            word to search for
	 */
	default void delete(String word) {
		delete(word.toCharArray());
	}

	/**
	 * Delete the data associated with the given word. If the word is not
	 * contained in the dictionary, nothing happens.
	 * 
	 * @param word
	 *            word to search for
	 */
	void delete(char[] word);

	/**
	 * Get data associated with the word in the trie, or null if the word was
	 * not included.
	 * 
	 * @param word
	 *            word to search
	 * @return Daten am gefundenen Knoten order null
	 */
	default T getData(String word) {
		return getData(word.toCharArray());
	}

	/**
	 * Get data associated with the word in the trie, or null if the word was
	 * not included.
	 * 
	 * @param word
	 *            word to search
	 * @return Daten am gefundenen Knoten order null
	 */
	T getData(char[] word);

	/**
	 * Insert or update data in the trie. If the word is already included in the
	 * trie, the update function is called with the current value and the value
	 * associated with the word is updated with the return value of the
	 * function. If the word is not included in the trie, the updateFunction is
	 * called with null and the returned value is inserted for the word.
	 * 
	 * @param word
	 *            word to search
	 * @param updateFunction
	 *            function that returns the inserted value (if input data is
	 *            null) or returns the updates value (if input data is present)
	 */
	default void updateOrInsertData(String word, Function<T, T> updateFunction) {
		updateOrInsertData(word.toCharArray(), updateFunction);
	}

	/**
	 * Insert or update data in the trie. If the word is already included in the
	 * trie, the update function is called with the current value and the value
	 * associated with the word is updated with the return value of the
	 * function. If the word is not included in the trie, the updateFunction is
	 * called with null and the returned value is inserted for the word.
	 * 
	 * @param word
	 *            word to search
	 * @param updateFunction
	 *            function that returns the inserted value (if input data is
	 *            null) or returns the updates value (if input data is present)
	 */
	void updateOrInsertData(char[] word, Function<T, T> updateFunction);

	/**
	 * Get depth of the trie.
	 * 
	 * @return depth of trie
	 */
	int getDepth();
}
