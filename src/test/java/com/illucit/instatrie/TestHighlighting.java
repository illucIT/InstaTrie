package com.illucit.instatrie;

import com.illucit.instatrie.highlight.HighlightedString.HighlightSegment;
import com.illucit.instatrie.index.PrefixIndex;
import com.illucit.instatrie.index.TriePrefixIndex;
import com.illucit.instatrie.splitter.StringWordSplitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.function.Function.identity;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for highlighting mechanism of {@link StringWordSplitter} and
 * {@link PrefixIndex}.
 *
 * @author Christian Simon
 */
public class TestHighlighting {

    private PrefixIndex<String> index;

    private List<HighlightSegment> result;
    private List<HighlightSegment> expected;

    @BeforeEach
    public void prepare() {
        index = new TriePrefixIndex<>(identity());
        result = null;
        expected = null;
    }

    @Test
    public void testEmpty() {

        // @formatter:off
		result = index.getHighlighted(null, "test").getSegments();
		expected = new ArrayList<>();
		// @formatter:on
        assertEquals(expected, result);

        // @formatter:off
		result = index.getHighlighted("", "test").getSegments();
		expected = new ArrayList<>();
		// @formatter:on
        assertEquals(expected, result);

        // @formatter:off
		result = index.getHighlighted(null, "").getSegments();
		expected = new ArrayList<>();
		// @formatter:on
        assertEquals(expected, result);

        // @formatter:off
		result = index.getHighlighted("", "").getSegments();
		expected = new ArrayList<>();
		// @formatter:on
        assertEquals(expected, result);

        // @formatter:off
		result = index.getHighlighted(null, null).getSegments();
		expected = new ArrayList<>();
		// @formatter:on
        assertEquals(expected, result);

        // @formatter:off
		result = index.getHighlighted("", null).getSegments();
		expected = new ArrayList<>();
		// @formatter:on
        assertEquals(expected, result);

    }

    @Test
    public void testSingleHighlights() {

        // @formatter:off
		result = index.getHighlighted("qwertzuiop", "qwer").getSegments();
		expected = Arrays.asList(
				new HighlightSegment("qwer", true),
				new HighlightSegment("tzuiop", false)
		);
		// @formatter:on
        assertEquals(expected, result);

        // @formatter:off
		result = index.getHighlighted("qwertzuiop", "q").getSegments();
		expected = Arrays.asList(
				new HighlightSegment("q", true),
				new HighlightSegment("wertzuiop", false)
		);
		// @formatter:on
        assertEquals(expected, result);

        // @formatter:off
		result = index.getHighlighted("qwertzuiop", "qwertzuiop").getSegments();
		expected = Collections.singletonList(
				new HighlightSegment("qwertzuiop", true)
		);
		// @formatter:on
        assertEquals(expected, result);

        // @formatter:off
		result = index.getHighlighted("qwertzuiop asdfg", "qwer").getSegments();
		expected = Arrays.asList(
				new HighlightSegment("qwer", true),
				new HighlightSegment("tzuiop asdfg", false)
		);
		// @formatter:on
        assertEquals(expected, result);

        // @formatter:off
		result = index.getHighlighted("qwertzuiop asdfg", "q").getSegments();
		expected = Arrays.asList(new HighlightSegment("q", true),
				new HighlightSegment("wertzuiop asdfg", false)
		);
		// @formatter:on
        assertEquals(expected, result);

        // @formatter:off
		result = index.getHighlighted("qwertzuiop asdfg", "qwertzuiop").getSegments();
		expected = Arrays.asList(
				new HighlightSegment("qwertzuiop", true),
				new HighlightSegment(" asdfg", false)
		);
		// @formatter:on
        assertEquals(expected, result);

        // @formatter:off
		result = index.getHighlighted("vbnm hjkl uiop", "hjk").getSegments();
		expected = Arrays.asList(
				new HighlightSegment("vbnm ", false),
				new HighlightSegment("hjk", true),
				new HighlightSegment("l uiop", false)
		);
		// @formatter:on
        assertEquals(expected, result);

        // @formatter:off
		result = index.getHighlighted("vbnm hjkl uiop", "jkl").getSegments();
		expected = Collections.singletonList(
				new HighlightSegment("vbnm hjkl uiop", false)
		);
		// @formatter:on
        assertEquals(expected, result);

    }

    @Test
    public void testMultipleHighlights() {

        // @formatter:off
		result = index.getHighlighted("Hans-Dieter Meier", "Hans-Dieter Meier").getSegments();
		expected = Arrays.asList(
				new HighlightSegment("Hans", true),
				new HighlightSegment("-", false),
				new HighlightSegment("Dieter", true),
				new HighlightSegment(" ", false),
				new HighlightSegment("Meier", true)
		);
		// @formatter:on
        assertEquals(expected, result);

        // @formatter:off
		result = index.getHighlighted("Ottos Mops kotzt.", "mo ko").getSegments();
		expected = Arrays.asList(
				new HighlightSegment("Ottos ", false),
				new HighlightSegment("Mo", true),
				new HighlightSegment("ps ", false),
				new HighlightSegment("ko", true),
				new HighlightSegment("tzt.", false)
		);
		// @formatter:on
        assertEquals(expected, result);

        // @formatter:off
		result = index.getHighlighted("Annas Ananas ist nass.", "nas ann").getSegments();
		expected = Arrays.asList(
				new HighlightSegment("Ann", true),
				new HighlightSegment("as Ananas ist ", false),
				new HighlightSegment("nas", true),
				new HighlightSegment("s.", false)
		);
		// @formatter:on
        assertEquals(expected, result);

    }

    @Test
    public void testAsciiFolding() {

        // @formatter:off
		result = index.getHighlighted("García Coruña", "garcia cöruná").getSegments();
		expected = Arrays.asList(
				new HighlightSegment("García", true),
				new HighlightSegment(" ", false),
				new HighlightSegment("Coruña", true)
		);
		// @formatter:on
        assertEquals(expected, result);

        // @formatter:off
		result = index.getHighlighted("Hans Müller-Lüdenscheidt", "mull lude").getSegments();
		expected = Arrays.asList(
				new HighlightSegment("Hans ", false),
				new HighlightSegment("Müll", true),
				new HighlightSegment("er-", false),
				new HighlightSegment("Lüde", true),
				new HighlightSegment("nscheidt", false)
		);
		// @formatter:on
        assertEquals(expected, result);

    }

    @Test
    public void testAsciiFoldingExpansion() {

        // @formatter:off
		result = index.getHighlighted("Der Haß ist krass ohne Maß.", "kraß mass").getSegments();
		expected = Arrays.asList(
				new HighlightSegment("Der Haß ist ", false),
				new HighlightSegment("krass", true),
				new HighlightSegment(" ohne ", false),
				new HighlightSegment("Maß", true),
				new HighlightSegment(".", false)
		);
		// @formatter:on
        assertEquals(expected, result);

    }

}
