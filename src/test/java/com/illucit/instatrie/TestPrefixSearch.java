package com.illucit.instatrie;

import com.google.common.collect.ImmutableList;
import com.illucit.instatrie.index.PrefixIndex;
import com.illucit.instatrie.index.PrefixSearch;
import com.illucit.instatrie.index.TriePrefixIndex;
import com.illucit.instatrie.splitter.StringWordSplitter;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestPrefixSearch {

    @Test
    public void testFilter() {
        PrefixIndex<String> index = new TriePrefixIndex<>(StringWordSplitter.IdentityStringWordSplitter.instance());
        PrefixSearch<String> filteredSearch = index.filter(s -> s.length() > 8);
        PrefixSearch<String> filteredSearch2 = index.filter(s -> s.contains("um"));

        List<String> models = new LinkedList<>();
        models.add("red wine");
        models.add("white wine");
        models.add("rose wine");
        models.add("water");
        models.add("rum");
        index.createIndex(models);

        assertEquals(ImmutableList.of("red wine", "white wine", "rose wine"), index.search("wi"));
        assertEquals(ImmutableList.of("white wine", "rose wine"), filteredSearch.search("wi"));

        assertEquals(ImmutableList.of("red wine", "rose wine"), index.search("r w"));
        assertEquals(ImmutableList.of("rose wine"), filteredSearch.search("r w"));

        assertEquals(ImmutableList.of("red wine", "rose wine", "rum"), index.search("r"));
        assertEquals(ImmutableList.of("rum"), filteredSearch2.search("r"));
    }

    @Test
    public void testFilterIterative() {
        PrefixIndex<String> index = new TriePrefixIndex<>(StringWordSplitter.IdentityStringWordSplitter.instance());
        PrefixSearch<String> filteredSearch = index.filter(s -> s.length() < 9);
        PrefixSearch<String> filteredSearchAgain = filteredSearch.filter(s -> s.contains("wine"));

        List<String> models = new LinkedList<>();
        models.add("red wine");
        models.add("white wine");
        models.add("rose wine");
        models.add("water");
        models.add("rum");
        index.createIndex(models);

        assertEquals(ImmutableList.of("red wine", "rose wine", "rum"), index.search("r"));
        assertEquals(ImmutableList.of("red wine"), filteredSearchAgain.search("r"));
    }

    @Test
    public void testMapping() {
        PrefixIndex<String> index = new TriePrefixIndex<>(StringWordSplitter.IdentityStringWordSplitter.instance());
        PrefixSearch<Integer> mappedSearch = index.map(String::length);

        List<String> models = new LinkedList<>();
        models.add("red wine");
        models.add("white wine");
        models.add("rose wine");
        models.add("water");
        models.add("rum");
        index.createIndex(models);

        assertEquals(ImmutableList.of("red wine", "rose wine", "rum"), index.search("r"));
        assertEquals(ImmutableList.of(8, 9, 3), mappedSearch.search("r"));
    }

    @Test
    public void testMappingAndFilter() {
        PrefixIndex<String> index = new TriePrefixIndex<>(StringWordSplitter.IdentityStringWordSplitter.instance());
        PrefixSearch<String> filteredSearch = index.filter(s -> s.length() < 9);
        PrefixSearch<Integer> mappedSearch = filteredSearch.map(String::length);

        List<String> models = new LinkedList<>();
        models.add("red wine");
        models.add("white wine");
        models.add("rose wine");
        models.add("water");
        models.add("rum");
        index.createIndex(models);

        assertEquals(ImmutableList.of("red wine", "rose wine", "rum"), index.search("r"));
        assertEquals(ImmutableList.of(8, 3), mappedSearch.search("r"));
    }


}
