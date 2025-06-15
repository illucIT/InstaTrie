package com.illucit.instatrie;

import com.illucit.instatrie.trie.Trie;
import com.illucit.instatrie.trie.TrieNode;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the iteratable utility methods of {@link TrieNode}.
 *
 * @author Christian Simon
 */
public class TestIterators {

    @Test
    public void testEmptyIterables() {
        Trie<String> trie = new Trie<>();

        TrieNode<String> root = trie.getRoot();

        Iterator<TrieNode<String>> childrenIterator = root.children().iterator();
        assertFalse(childrenIterator.hasNext());

        Iterator<TrieNode<String>> brothersIterator = root.brothers().iterator();
        assertTrue(brothersIterator.hasNext());
        assertEquals(root, brothersIterator.next());
        assertFalse(brothersIterator.hasNext());

        Iterator<TrieNode<String>> descendantsIterator = root.descendants().iterator();
        assertTrue(descendantsIterator.hasNext());
        assertEquals(root, descendantsIterator.next());
        assertFalse(descendantsIterator.hasNext());
    }

    @Test
    public void testIterables() {
        Trie<String> trie = new Trie<>();
        trie.insert("abc", "abc");
        trie.insert("abcde", "abcde");
        trie.insert("axy", "axy");
        trie.insert("a", "a");
        trie.insert("zzz", "zzz");

        TrieNode<String> root = trie.getRoot();

        String[] childrenExpectedArray = new String[]{"a", "zzz"};
        LinkedList<String> childrenExpected = new LinkedList<>(Arrays.asList(childrenExpectedArray));
        LinkedList<String> childrenResult = new LinkedList<>();
        root.children().stream().filter(TrieNode::hasData).forEach(node -> childrenResult.add(node.getData()));
        assertEquals(childrenExpected, childrenResult);

        TrieNode<String> firstChild = root.getFirstSon();
        String[] brothersExpectedArray = new String[]{"a", "zzz"};
        LinkedList<String> brothersExpected = new LinkedList<>(Arrays.asList(brothersExpectedArray));
        LinkedList<String> brothersResult = new LinkedList<>();
        firstChild.brothers().stream().filter(TrieNode::hasData)
                .forEach(node -> brothersResult.add(node.getData()));
        assertEquals(brothersExpected, brothersResult);

        String[] descendentsExpectedArray = new String[]{"abc", "abcde", "axy", "a", "zzz"};
        TreeSet<String> descendentsExpected = new TreeSet<>(Arrays.asList(descendentsExpectedArray));
        TreeSet<String> descendentsResult = new TreeSet<>();
        root.descendants().stream().filter(TrieNode::hasData)
                .forEach(node -> descendentsResult.add(node.getData()));
        assertEquals(descendentsExpected, descendentsResult);
    }

}
