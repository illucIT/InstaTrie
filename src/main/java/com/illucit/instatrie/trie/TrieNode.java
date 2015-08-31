package com.illucit.instatrie.trie;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Stack;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Node inside a {@link Trie}. The nodes are organized horizontal (each node can
 * get the next brother node) and vertical (each node can get the first son). <br>
 * <br>
 * This node structure is not threadsafe, so simultanious access by multiple
 * concurrent threads is not permitted, if one of them makes modifications to
 * the node structure.
 * 
 * @author Christian Simon
 *
 * @param <T>
 *            payload data type
 */
public class TrieNode<T extends Serializable> implements Serializable {

	private static final long serialVersionUID = -6073377518906283282L;

	/*
	 * Static cache for char arrays with size one in ASCII range (performance!)
	 */

	/** Cache */
	private static final char[][] CHAR_ARRAYS = new char[256][];

	static {
		for (char c = 0; c < 256; c++) {
			CHAR_ARRAYS[c] = new char[] { c };
		}
	}

	/*
	 * Trie node members
	 */

	/**
	 * Character path leading from the parent from this node. If the trie
	 * contains substrings (e.g. for search), this array will most likely have
	 * one element.
	 */
	private char[] chars;

	/**
	 * First son inside the tree.
	 */
	private TrieNode<T> firstSon;

	/**
	 * Next brother inside the tree.
	 */
	private TrieNode<T> nextBrother;

	/**
	 * Payload data.
	 */
	private T data;

	/**
	 * Flag if the node has been explicitely inserted or if it was only
	 * introduced as inner node of a path.
	 */
	private boolean inserted;

	/*
	 * Constructors
	 */

	/**
	 * Create new trie node.
	 * 
	 * @param chars
	 *            chars containing the path leading from the parent node to this
	 *            node
	 * @param brother
	 *            next brother (optional)
	 * @param son
	 *            first son (optional)
	 * @param data
	 *            playload data (optional)
	 * @param inserted
	 *            true if the node was introduced as leaf of an insert operation
	 */
	public TrieNode(char[] chars, TrieNode<T> brother, TrieNode<T> son, T data, boolean inserted) {
		this.nextBrother = brother;
		this.firstSon = son;
		this.data = data;
		this.inserted = inserted;
		setChars(chars);
	}

	/**
	 * Create new trie node.
	 * 
	 * @param chars
	 *            String containing the path leading from the parent node to
	 *            this node
	 * @param brother
	 *            next brother (optional)
	 * @param son
	 *            first son (optional)
	 * @param data
	 *            playload data (optional)
	 * @param inserted
	 *            true if the node was introduced as leaf of an insert operation
	 */
	public TrieNode(final String chars, final TrieNode<T> brother, final TrieNode<T> son, final T data, boolean inserted) {
		this.nextBrother = brother;
		this.firstSon = son;
		this.data = data;
		setChars(chars.toCharArray());
	}

	/*
	 * Properties
	 */

	/**
	 * Get the characters leading from the parent ot this node.
	 * 
	 * @return character array
	 */
	public char[] getChars() {
		return chars;
	}

	/**
	 * Set the characters leading from the parent ot this node.
	 * 
	 * @param chars
	 *            character array
	 */
	public void setChars(char[] chars) {
		if (chars.length == 1 && chars[0] < 256) {
			this.chars = CHAR_ARRAYS[chars[0]];
		} else {
			this.chars = chars;
		}
	}

	/**
	 * Get the first son of this node.
	 * 
	 * @return trie node or null
	 */
	public TrieNode<T> getFirstSon() {
		return firstSon;
	}

	/**
	 * Set the first son of this node.
	 * 
	 * @param firstSon
	 *            trie node or null
	 */
	public void setFirstSon(TrieNode<T> firstSon) {
		this.firstSon = firstSon;
	}

	/**
	 * Get the next brother of this node.
	 * 
	 * @return trie node or null
	 */
	public TrieNode<T> getNextBrother() {
		return nextBrother;
	}

	/**
	 * Set the next brother of this node.
	 * 
	 * @param nextBrother
	 *            trie node or null
	 */
	public void setNextBrother(TrieNode<T> nextBrother) {
		this.nextBrother = nextBrother;
	}

	/**
	 * Get payload data of the node.
	 * 
	 * @return playload data or null
	 */
	public T getData() {
		return data;
	}

	/**
	 * Set payload data of the node.
	 * 
	 * @param data
	 *            playload data or null
	 */
	public void setData(T data) {
		this.data = data;
	}

	/**
	 * Get flag if node represents an inserted string in the trie.
	 * 
	 * @return true if node represents an inserted string
	 */
	public boolean isInserted() {
		return inserted;
	}

	/**
	 * Set flag if node represents an inserted string in the trie.
	 * 
	 * @param inserted
	 *            true if node represents an inserted string
	 */
	public void setInserted(boolean inserted) {
		this.inserted = inserted;
	}

	/*
	 * Methods
	 */

	/**
	 * Check if a node has children.
	 * 
	 * @return true if at least one son exists
	 */
	public boolean hasChildren() {
		return this.firstSon != null;
	}

	/**
	 * Check if a node has next brothers.
	 * 
	 * @return true if at least one next brother exists
	 */
	public boolean hasBrothers() {
		return this.nextBrother != null;
	}

	/**
	 * Check if a node has data attached to it.
	 * 
	 * @return true if data is attached to the node
	 */
	public boolean hasData() {
		return this.data != null;
	}

	/**
	 * Get the first character of the characters loading from the parent to this
	 * node.
	 * 
	 * @return first character
	 */
	public char getFirstChar() {
		return this.chars[0];
	}

	/**
	 * Calculate the depth of the tree starting from this node.
	 * 
	 * @return depth
	 */
	public int getDepth() {
		if (this.firstSon != null) {
			return this.firstSon.getDepthRecursive();
		}
		return 0;
	}

	/**
	 * Recursive method to calculate the depth.
	 * 
	 * @return depth
	 */
	private int getDepthRecursive() {
		int depth = 0;
		for (TrieNode<T> node : brothers()) {
			if (node.firstSon != null) {
				depth = Math.max(depth, node.firstSon.getDepthRecursive());
			}
		}
		return depth + 1;
	}

	/**
	 * Get iterable (for use in foreach loop) to iterate over this node and all
	 * of its next brothers. <br>
	 * <br>
	 * Note, that the returned iterable is still attached to the node structure,
	 * so concurrent modifications while the iterable is processed are not
	 * permitted.
	 * 
	 * @return iterable of trie nodes
	 */
	public TrieNodeIterable<T> brothers() {
		return new TrieNodeBrothersIterable<>(this);
	}

	/**
	 * Get iterable (for use in foreach loop) to iterate over all children of
	 * this node. <br>
	 * <br>
	 * Note, that the returned iterable is still attached to the node structure,
	 * so concurrent modifications while the iterable is processed are not
	 * permitted.
	 * 
	 * @return iterable of trie nodes
	 */
	public TrieNodeIterable<T> children() {
		return new TrieNodeBrothersIterable<>(getFirstSon());
	}

	/**
	 * Get iterable (for use in foreach loop) to iterate over the node and all
	 * of its descendants (its children and their children and so on). <br>
	 * <br>
	 * Note, that the returned iterable is still attached to the node structure,
	 * so concurrent modifications while the iterable is processed are not
	 * permitted.
	 * 
	 * @return iterable of trie nodes
	 */
	public TrieNodeIterable<T> descendants() {
		return new TrieNodeDescendantsIterable<>(this);
	}

	/**
	 * Render the tree starting from this node as String.
	 * 
	 * @return String representation of the tree at this node
	 */
	@Override
	public String toString() {
		return toString("");
	}
	
	public String toString(String indentation) {
		final StringBuilder buffer = new StringBuilder();
		buffer.append(indentation);
		buffer.append("\"").append(this.chars).append("\"");
		if (this.data != null) {
			buffer.append(" {");
			if (this.data instanceof Object[]) {
				buffer.append(Arrays.toString((Object[]) this.data));
			} else {
				buffer.append(this.data.toString());
			}
			buffer.append('}');
		}
		if (hasChildren()) {
			buffer.append("\n");
			buffer.append(this.firstSon.toString(indentation + "  "));
		}
		if (hasBrothers()) {
			buffer.append("\n");
			buffer.append(this.nextBrother.toString(indentation));
		}
		return buffer.toString();
	}

	/*
	 * Utility classes
	 */

	/**
	 * Interface for itable classes with stream support for {@link TrieNode}s.
	 * 
	 * @author Christian Simon
	 *
	 * @param <T>
	 *            payload type
	 */
	public static interface TrieNodeIterable<T extends Serializable> extends Iterable<TrieNode<T>> {

		/**
		 * Returns a sequential {@code Stream} with this iterable as its source.
		 * 
		 * @return stream
		 */
		public default Stream<TrieNode<T>> stream() {
			return StreamSupport.stream(spliterator(), false);
		}

	}

	/**
	 * Iterable to iterate over a node and all its next brothers.
	 * 
	 * @author Christian Simon
	 *
	 * @param <T>
	 *            payload type
	 */
	public static class TrieNodeBrothersIterable<T extends Serializable> implements TrieNodeIterable<T> {

		/**
		 * Contains the node this iterable was created for.
		 */
		private final TrieNode<T> node;

		/**
		 * Create iterable on a target node.
		 * 
		 * @param node
		 *            node to iterate over it's brothers
		 */
		public TrieNodeBrothersIterable(TrieNode<T> node) {
			this.node = node;
		}

		@Override
		public Iterator<TrieNode<T>> iterator() {
			return new Iterator<TrieNode<T>>() {

				private TrieNode<T> nextNode = node;

				@Override
				public boolean hasNext() {
					return nextNode != null;
				}

				@Override
				public TrieNode<T> next() {
					TrieNode<T> result = nextNode;
					nextNode = nextNode.getNextBrother();
					return result;
				}

			};
		}
	}

	/**
	 * Iterable to iterate over a node and all its descendants (all children and
	 * children's children). This is achieved via depth-first-search.
	 * 
	 * @author Christian Simon
	 *
	 * @param <T>
	 *            payload type
	 */
	public static class TrieNodeDescendantsIterable<T extends Serializable> implements TrieNodeIterable<T> {

		/**
		 * Contains the node this iterable was created for.
		 */
		private final TrieNode<T> node;

		/**
		 * Create iterable on a target node.
		 * 
		 * @param node
		 *            node to iterate over it's brothers
		 */
		public TrieNodeDescendantsIterable(TrieNode<T> node) {
			this.node = node;
		}

		@Override
		public Iterator<TrieNode<T>> iterator() {
			return new Iterator<TrieNode<T>>() {

				/** Current node. */
				private TrieNode<T> nextNode = node;

				/** Stack to manage the different depth levels of the tree. */
				private Stack<TrieNode<T>> parentNodes = new Stack<>();

				/**
				 * Find next node to continue.
				 */
				private void selectNextNode() {
					TrieNode<T> currentNode = nextNode;
					if (currentNode.hasChildren()) {
						// Put node on stack and descend to first child
						parentNodes.push(currentNode);
						nextNode = currentNode.getFirstSon();
					} else if (currentNode.hasBrothers()) {
						// Continue with next brother and it's children ...
						nextNode = currentNode.getNextBrother();
					} else {
						// No children or brothers for current node
						// Pull parent node from stack and ascend
						while (!parentNodes.isEmpty()) {
							TrieNode<T> nextParent = parentNodes.pop();
							if (nextParent.hasBrothers()) {
								nextNode = nextParent.getNextBrother();
								return;
							}
						}
						// Stack is empty - all nodes finished
						nextNode = null;
					}
				}

				@Override
				public boolean hasNext() {
					return nextNode != null;
				}

				@Override
				public TrieNode<T> next() {
					TrieNode<T> result = nextNode;
					selectNextNode();
					return result;
				}

			};
		}
	}

}
