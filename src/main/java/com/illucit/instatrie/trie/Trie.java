package com.illucit.instatrie.trie;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Trie data structure to store string chains together with payload data. This
 * trie structure is very efficient to determine whether a string (prefix) is
 * contained in the trie or to get the payload data associated with a string. <br>
 * <br>
 * This data structure is not threadsafe, so simultanious access by multiple
 * concurrent threads is not permitted, if one of them makes modifications to
 * the node structure.
 * 
 * @author Christian Simon
 *
 * @param <T>
 *            payload data type
 */
public class Trie<T extends Serializable> implements PrefixDictionary<T> {

	private static final long serialVersionUID = 2207742126800619167L;

	private static final char[] EMPTY_CHAR_ARRAY = new char[0];

	/**
	 * Root node of the tree.
	 */
	private final TrieNode<T> root;

	/**
	 * Create empty trie.
	 */
	public Trie() {
		this.root = new TrieNode<T>(EMPTY_CHAR_ARRAY, null, null, null, false);
	}

	/**
	 * Create a trie with an existing root node.
	 * 
	 * @param root
	 *            root node
	 */
	public Trie(TrieNode<T> root) {
		this.root = root;
	}

	/**
	 * Get the root node of the trie.
	 * 
	 * @return root node
	 */
	public TrieNode<T> getRoot() {
		return this.root;
	}

	@Override
	public void clear() {
		this.root.setData(null);
		this.root.setFirstSon(null);
		this.root.setNextBrother(null);
	}

	@Override
	public void insert(char[] word, int startIndex, int endIndex, T data) {
		if (endIndex < startIndex) {
			throw new IllegalArgumentException(endIndex + " < " + startIndex);
		}

		if (endIndex == 0) {
			// Word of length 0 ends in root node
			this.root.setData(data);
			this.root.setInserted(true);
			return;
		}

		int wordPos = startIndex; // current position in word
		TrieNode<T> node = this.root; // current node

		// Descend in tree
		while (true) {

			// First character to search for in children
			final char firstChar = word[wordPos];

			final TrieNode<T> firstSon = node.getFirstSon();
			if (firstSon == null || firstSon.getFirstChar() > firstChar) {
				// Insert new node as first son of current node (shift existing
				// child nodes)
				node.setFirstSon(new TrieNode<T>(subarray(word, wordPos, endIndex), firstSon, null, data, true));
				return;
			}

			// find son to insert after or below
			TrieNode<T> currSon = firstSon;
			for (TrieNode<T> brother : firstSon.brothers()) {
				if (brother.getFirstChar() > firstChar) {
					break;
				}
				currSon = brother;
			}

			if (currSon.getFirstChar() == firstChar) {

				// Find matching chars in path of current node
				char[] sonChars = currSon.getChars();
				int sonPos = 1;
				wordPos++;
				int sonLength = sonChars.length;
				while (sonPos < sonLength && wordPos < endIndex && sonChars[sonPos] == word[wordPos]) {
					sonPos++;
					wordPos++;
				}

				if (wordPos == endIndex) {
					// Word completely found in node

					if (sonPos == sonLength) {
						// Update node data
						currSon.setData(data);
						currSon.setInserted(true);
						return;
					}

					// Word is prefix of node path: split node to add the data

					// create sub-node as child of this node to split up path
					TrieNode<T> subNode = new TrieNode<T>(subarray(sonChars, sonPos, sonChars.length), null,
							currSon.getFirstSon(), currSon.getData(), currSon.isInserted());
					currSon.setFirstSon(subNode);
					currSon.setChars(subarray(sonChars, 0, sonPos));
					currSon.setData(data);
					currSon.setInserted(true);
					return;

				} else if (sonPos == sonLength) {
					// Node matches completely, but word is not yet finished
					// continue to descend

					node = currSon;

				} else {
					// Node only has common prefix with word, but down not match
					// it completely
					// So node needs to be splitted

					// different suffixes
					final char[] wordSuffix = subarray(word, wordPos, endIndex);
					final char[] sonSuffix = subarray(sonChars, sonPos, sonChars.length);

					if (sonSuffix[0] < wordSuffix[0]) {
						// new node needs to be inserted after the splitted
						// subnode (as next brother)
						TrieNode<T> newWordNode = new TrieNode<T>(wordSuffix, null, null, data, true);
						TrieNode<T> splittedSubNode = new TrieNode<T>(sonSuffix, newWordNode, currSon.getFirstSon(),
								currSon.getData(), currSon.isInserted());
						currSon.setFirstSon(splittedSubNode);
					} else {
						// new node needs to be inserted before the splitted
						// subnode (so splitted subnode is next browser of
						// inserted node)
						TrieNode<T> splittedSubNode = new TrieNode<T>(sonSuffix, null, currSon.getFirstSon(),
								currSon.getData(), currSon.isInserted());
						TrieNode<T> newWordNode = new TrieNode<T>(wordSuffix, splittedSubNode, null, data, true);
						currSon.setFirstSon(newWordNode);
					}

					// Update path to common prefix
					currSon.setChars(subarray(sonChars, 0, sonPos));
					// Clear data and inserted flag from now splitted node
					currSon.setData(null);
					currSon.setInserted(false);
					return;
				}
			} else {
				// insert new node after current node (shift next brothers)
				TrieNode<T> insertedNode = new TrieNode<>(subarray(word, wordPos, endIndex), currSon.getNextBrother(),
						null, data, true);
				currSon.setNextBrother(insertedNode);
				return;
			}
		}
	}

	@Override
	public void updateOrInsertData(char[] word, Function<T, T> updateFunction) {
		int endIndex = word.length;

		if (endIndex == 0) {
			// Word of length 0 ends in root node
			this.root.setData(updateFunction.apply(this.root.getData()));
			this.root.setInserted(true);
			return;
		}

		int wordPos = 0; // current position in word
		TrieNode<T> node = this.root; // current node

		// Descend in tree
		while (true) {

			// First character to search for in children
			final char firstChar = word[wordPos];

			final TrieNode<T> firstSon = node.getFirstSon();
			if (firstSon == null || firstSon.getFirstChar() > firstChar) {
				// Insert new node as first son of current node (shift existing
				// child nodes)
				node.setFirstSon(new TrieNode<T>(subarray(word, wordPos, endIndex), firstSon, null, updateFunction
						.apply(null), true));
				return;
			}

			// find son to insert after or below
			TrieNode<T> currSon = firstSon;
			for (TrieNode<T> brother : firstSon.brothers()) {
				if (brother.getFirstChar() > firstChar) {
					break;
				}
				currSon = brother;
			}

			if (currSon.getFirstChar() == firstChar) {

				// Find matching chars in path of current node
				char[] sonChars = currSon.getChars();
				int sonPos = 1;
				wordPos++;
				int sonLength = sonChars.length;
				while (sonPos < sonLength && wordPos < endIndex && sonChars[sonPos] == word[wordPos]) {
					sonPos++;
					wordPos++;
				}

				if (wordPos == endIndex) {
					// Word completely found in node

					if (sonPos == sonLength) {
						// Update node data
						currSon.setData(updateFunction.apply(currSon.getData()));
						currSon.setInserted(true);
						return;
					}

					// Word is prefix of node path: split node to add the data

					// create sub-node as child of this node to split up path
					TrieNode<T> subNode = new TrieNode<T>(subarray(sonChars, sonPos, sonChars.length), null,
							currSon.getFirstSon(), currSon.getData(), currSon.isInserted());
					currSon.setFirstSon(subNode);
					currSon.setChars(subarray(sonChars, 0, sonPos));
					currSon.setData(updateFunction.apply(null));
					currSon.setInserted(true);
					return;

				} else if (sonPos == sonLength) {
					// Node matches completely, but word is not yet finished
					// continue to descend

					node = currSon;

				} else {
					// Node only has common prefix with word, but down not match
					// it completely
					// So node needs to be splitted

					// different suffixes
					final char[] wordSuffix = subarray(word, wordPos, endIndex);
					final char[] sonSuffix = subarray(sonChars, sonPos, sonChars.length);

					if (sonSuffix[0] < wordSuffix[0]) {
						// new node needs to be inserted after the splitted
						// subnode (as next brother)
						TrieNode<T> newWordNode = new TrieNode<T>(wordSuffix, null, null, updateFunction.apply(null),
								true);
						TrieNode<T> splittedSubNode = new TrieNode<T>(sonSuffix, newWordNode, currSon.getFirstSon(),
								currSon.getData(), currSon.isInserted());
						currSon.setFirstSon(splittedSubNode);
					} else {
						// new node needs to be inserted before the splitted
						// subnode (so splitted subnode is next browser of
						// inserted node)
						TrieNode<T> splittedSubNode = new TrieNode<T>(sonSuffix, null, currSon.getFirstSon(),
								currSon.getData(), currSon.isInserted());
						TrieNode<T> newWordNode = new TrieNode<T>(wordSuffix, splittedSubNode, null,
								updateFunction.apply(null), true);
						currSon.setFirstSon(newWordNode);
					}

					// Update path to common prefix
					currSon.setChars(subarray(sonChars, 0, sonPos));
					// Clear data and inserted flag from now splitted node
					currSon.setData(null);
					currSon.setInserted(false);
					return;
				}
			} else {
				// insert new node after current node (shift next brothers)
				TrieNode<T> insertedNode = new TrieNode<>(subarray(word, wordPos, endIndex), currSon.getNextBrother(),
						null, updateFunction.apply(null), true);
				currSon.setNextBrother(insertedNode);
				return;
			}
		}
	}

	@Override
	public final boolean containsPrefix(final char[] word) {
		TrieNode<T> node = getNode(word, false);
		return node != null;
	}

	@Override
	public boolean contains(char[] word) {
		TrieNode<T> node = getNode(word, true);
		return node != null && node.isInserted();
	}

	@Override
	public void delete(char[] word) {
		TrieNode<T> node = getNode(word, true);
		if (node != null) {
			node.setData(null);
			node.setInserted(false);
		}
	}

	@Override
	public T getData(char[] word) {
		TrieNode<T> node = getNode(word, true);
		if (node == null) {
			return null;
		}
		return node.getData();
	}

	/**
	 * Find a node that represents the given word. If the search is exact, null
	 * is returned if the node is only a substring node of the next found node.
	 * 
	 * @param word
	 *            word to search for
	 * @param exact
	 *            flag if exact node search should be performed
	 * @return node or null, if not found (exact)
	 */
	public TrieNode<T> getNode(char[] word, boolean exact) {
		int wordPos = 0; // current position in word
		final int wordLength = word.length;
		TrieNode<T> node = this.root; // current node

		// Descend in tree while word is not completely found
		while (wordPos < wordLength) {

			// First character to search for in children
			final char firstChar = word[wordPos];

			// Iterate over sons to find the one with matches firstChar
			TrieNode<T> currentSon = null;
			for (TrieNode<T> childNode : node.children()) {
				if (childNode.getFirstChar() == firstChar) {
					// Son which matches firstChar is found
					currentSon = childNode;
					break;
				}
				if (childNode.getFirstChar() > firstChar) {
					// character of current child node is too large for
					// firstChar
					// word cannot be found any more
					return null;
				}
			}
			if (currentSon == null) {
				// Either no sons or no son with matching firstChar was found
				// Cannot descend any further
				return null;
			}

			// Consume matching characters in word
			char[] sonChars = currentSon.getChars();
			int sonPos = 1;
			wordPos++;
			int sonLength = sonChars.length;
			while (sonPos < sonLength && wordPos < wordLength && sonChars[sonPos] == word[wordPos]) {
				sonPos++;
				wordPos++;
			}

			if (sonPos < sonLength) {
				// could not completely match characters of node

				if (exact) {
					// Can't find exact match any more
					// Either prefix match or mismatch with word
					return null;
				}
				if (wordPos == wordLength) {
					// Prefix match (no exact search)
					return currentSon;
				}

				// Mismatch with word
				return null;
			}

			// Son matched completely with word
			// Descend further until word is consumed
			node = currentSon;
		}
		// Word is completely found (exact)
		return node;
	}

	/**
	 * Walk the trie down along a path (up to the outermost node still matching
	 * the word) and call the consumer function every time (from the root to the
	 * outermost node).
	 * 
	 * @param word
	 *            word to walk upon
	 * @param consumer
	 *            consumer function to call on every node
	 * @param includePrefixMatch
	 *            if this flag is true, the last node is also included if the
	 *            word parameter is only a prefix of the path of this node (no
	 *            exact match)
	 * @return true if the word was found completely, false otherwise
	 */
	public boolean walkPath(char[] word, Consumer<TrieNode<T>> consumer, boolean includePrefixMatch) {
		int wordPos = 0; // current position in word
		final int wordLength = word.length;
		TrieNode<T> node = this.root; // current node

		// Call consumer for root
		consumer.accept(node);

		// Descend in tree while word is not completely found
		while (wordPos < wordLength) {
			// First character to search for in children
			final char firstChar = word[wordPos];

			// Iterate over sons to find the one with matches firstChar
			TrieNode<T> currentSon = null;
			for (TrieNode<T> childNode : node.children()) {
				if (childNode.getFirstChar() == firstChar) {
					// Son which matches firstChar is found
					currentSon = childNode;
					break;
				}
				if (childNode.getFirstChar() > firstChar) {
					// character of current child node is too large for
					// firstChar
					// word cannot be found any more
					return false;
				}
			}
			if (currentSon == null) {
				// Either no sons or no son with matching firstChar was found
				// Cannot descend any further
				return false;
			}

			// Consume matching characters in word
			final char[] sonChars = currentSon.getChars();
			int sonPos = 1;
			wordPos++;
			final int sonLength = sonChars.length;
			while (sonPos < sonLength && wordPos < wordLength && sonChars[sonPos] == word[wordPos]) {
				sonPos++;
				wordPos++;
			}

			if (sonPos < sonLength) {
				// could not completely match characters of node

				if (!includePrefixMatch) {
					// Can't find exact match any more
					// Either prefix match or mismatch with word
					return false;
				}
				if (wordPos == wordLength) {
					// Prefix match (no exact search)

					// Call consumer
					consumer.accept(currentSon);

					return true;
				}

				// Mismatch with word
				return false;
			}

			// Son matched completely with word
			// Descend further until word is consumed
			node = currentSon;

			// Call consumer
			consumer.accept(node);
		}
		// Word is completely found (exact)
		return true;
	}

	@Override
	public int getDepth() {
		return this.root.getDepth();
	}

	/**
	 * Get String representation of trie.
	 * 
	 * @return String representation of root node
	 */
	@Override
	public String toString() {
		return this.root.toString();
	}

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
	private static char[] subarray(char[] array, int startIndex, int endIndex) {
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

}
