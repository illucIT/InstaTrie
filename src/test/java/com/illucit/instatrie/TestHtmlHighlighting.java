package com.illucit.instatrie;

import static com.illucit.instatrie.util.ListUtils.newList;
import static java.util.function.Function.identity;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.illucit.instatrie.index.PrefixIndex;
import com.illucit.instatrie.index.TriePrefixIndex;
import com.illucit.instatrie.splitter.HighlightedString.HighlightSegment;
import com.illucit.instatrie.splitter.StringWordSplitter;

/**
 * Test class for HTML highlighting machanism of {@link StringWordSplitter} and
 * {@link PrefixIndex}.
 * 
 * @author Christian Simon
 *
 */
public class TestHtmlHighlighting {

	private PrefixIndex<String> index;

	private List<HighlightSegment> result;
	private List<HighlightSegment> expected;

	@Before
	public void prepare() {
		index = new TriePrefixIndex<String>(identity());
		result = null;
		expected = null;
	}

	@Test
	public void testEmpty() {

		// @formatter:off
		result = index.getHighlightedHtml(null, "test").getSegments();
		expected = newList();
		// @formatter:on
		Assert.assertEquals(expected, result);

		// @formatter:off
		result = index.getHighlightedHtml("", "test").getSegments();
		expected = newList();
		// @formatter:on
		Assert.assertEquals(expected, result);

		// @formatter:off
		result = index.getHighlightedHtml(null, "").getSegments();
		expected = newList();
		// @formatter:on
		Assert.assertEquals(expected, result);

		// @formatter:off
		result = index.getHighlightedHtml("", "").getSegments();
		expected = newList();
		// @formatter:on
		Assert.assertEquals(expected, result);

		// @formatter:off
		result = index.getHighlightedHtml(null, null).getSegments();
		expected = newList();
		// @formatter:on
		Assert.assertEquals(expected, result);

		// @formatter:off
		result = index.getHighlightedHtml("", null).getSegments();
		expected = newList();
		// @formatter:on
		Assert.assertEquals(expected, result);

	}

	@Test
	public void testSingleHighlights() {

		// @formatter:off
		result = index.getHighlightedHtml("qwertzuiop", "qwer").getSegments();
		expected = newList(
				new HighlightSegment("qwer", true), 
				new HighlightSegment("tzuiop", false)
		);
		// @formatter:on
		Assert.assertEquals(expected, result);

		// @formatter:off
		result = index.getHighlightedHtml("qwertzuiop", "q").getSegments();
		expected = newList(
				new HighlightSegment("q", true), 
				new HighlightSegment("wertzuiop", false)
		);
		// @formatter:on
		Assert.assertEquals(expected, result);

		// @formatter:off
		result = index.getHighlightedHtml("qwertzuiop", "qwertzuiop").getSegments();
		expected = newList(
				new HighlightSegment("qwertzuiop", true)
		);
		// @formatter:on
		Assert.assertEquals(expected, result);

		// @formatter:off
		result = index.getHighlightedHtml("qwertzuiop asdfg", "qwer").getSegments();
		expected = newList(
				new HighlightSegment("qwer", true), 
				new HighlightSegment("tzuiop asdfg", false)
		);
		// @formatter:on
		Assert.assertEquals(expected, result);

		// @formatter:off
		result = index.getHighlightedHtml("qwertzuiop asdfg", "q").getSegments();
		expected = newList(
				new HighlightSegment("q", true), 
				new HighlightSegment("wertzuiop asdfg", false)
		);
		// @formatter:on
		Assert.assertEquals(expected, result);

		// @formatter:off
		result = index.getHighlightedHtml("qwertzuiop asdfg", "qwertzuiop").getSegments();
		expected = newList(
				new HighlightSegment("qwertzuiop", true),
				new HighlightSegment(" asdfg", false)
		);
		// @formatter:on
		Assert.assertEquals(expected, result);

		// @formatter:off
		result = index.getHighlightedHtml("vbnm hjkl uiop", "hjk").getSegments();
		expected = newList(
				new HighlightSegment("vbnm ", false),
				new HighlightSegment("hjk", true),
				new HighlightSegment("l uiop", false)
		);
		// @formatter:on
		Assert.assertEquals(expected, result);

		// @formatter:off
		result = index.getHighlightedHtml("vbnm hjkl uiop", "jkl").getSegments();
		expected = newList(
				new HighlightSegment("vbnm hjkl uiop", false)
		);
		// @formatter:on
		Assert.assertEquals(expected, result);

	}

	@Test
	public void testMultipleHighlights() {

		// @formatter:off
		result = index.getHighlightedHtml("Hans-Dieter Meier", "Hans-Dieter Meier").getSegments();
		expected = newList(
				new HighlightSegment("Hans", true),
				new HighlightSegment("-", false),
				new HighlightSegment("Dieter", true),
				new HighlightSegment(" ", false),
				new HighlightSegment("Meier", true)
		);
		// @formatter:on
		Assert.assertEquals(expected, result);

		// @formatter:off
		result = index.getHighlightedHtml("Ottos Mops kotzt.", "mo ko").getSegments();
		expected = newList(
				new HighlightSegment("Ottos ", false),
				new HighlightSegment("Mo", true),
				new HighlightSegment("ps ", false),
				new HighlightSegment("ko", true),
				new HighlightSegment("tzt.", false)
		);
		// @formatter:on
		Assert.assertEquals(expected, result);

		// @formatter:off
		result = index.getHighlightedHtml("Annas Ananas ist nass.", "nas ann").getSegments();
		expected = newList(
				new HighlightSegment("Ann", true),
				new HighlightSegment("as Ananas ist ", false),
				new HighlightSegment("nas", true),
				new HighlightSegment("s.", false)
		);
		// @formatter:on
		Assert.assertEquals(expected, result);

	}

	@Test
	public void testAsciiFolding() {

		// @formatter:off
		result = index.getHighlightedHtml("García Coruña", "garcia cöruná").getSegments();
		expected = newList(
				new HighlightSegment("García", true),
				new HighlightSegment(" ", false),
				new HighlightSegment("Coruña", true)
		);
		// @formatter:on
		Assert.assertEquals(expected, result);

		// @formatter:off
		result = index.getHighlightedHtml("Hans Müller-Lüdenscheidt", "mull lude").getSegments();
		expected = newList(
				new HighlightSegment("Hans ", false),
				new HighlightSegment("Müll", true),
				new HighlightSegment("er-", false),
				new HighlightSegment("Lüde", true),
				new HighlightSegment("nscheidt", false)
		);
		// @formatter:on
		Assert.assertEquals(expected, result);

	}

	@Test
	public void testAsciiFoldingExpansion() {

		// @formatter:off
		result = index.getHighlightedHtml("Der Haß ist krass ohne Maß.", "kraß mass").getSegments();
		expected = newList(
				new HighlightSegment("Der Haß ist ", false),
				new HighlightSegment("krass", true),
				new HighlightSegment(" ohne ", false),
				new HighlightSegment("Maß", true),
				new HighlightSegment(".", false)
		);
		// @formatter:on
		Assert.assertEquals(expected, result);

	}

	@Test
	public void testHtml() {

		// @formatter:off
		result = index.getHighlightedHtml("<i>Tag1 <b>Tag2</b></i>", "TAG").getSegments();
		expected = newList(
				new HighlightSegment("<i>", false),
				new HighlightSegment("Tag", true),
				new HighlightSegment("1 <b>", false),
				new HighlightSegment("Tag", true),
				new HighlightSegment("2</b></i>", false)
		);
		// @formatter:on
		Assert.assertEquals(expected, result);

		// @formatter:off
		result = index.getHighlightedHtml("Foo <b>foobar</b> foo<i>baz</i>", "foo").getSegments();
		expected = newList(
				new HighlightSegment("Foo", true),
				new HighlightSegment(" <b>", false),
				new HighlightSegment("foo", true),
				new HighlightSegment("bar</b> ", false),
				new HighlightSegment("foo", true),
				new HighlightSegment("<i>baz</i>", false)
		);
		// @formatter:on
		Assert.assertEquals(expected, result);

		// @formatter:off
		result = index.getHighlightedHtml("<strong>Gernot Haßknecht</strong>", "hass").getSegments();
		expected = newList(
				new HighlightSegment("<strong>Gernot ", false),
				new HighlightSegment("Haß", true),
				new HighlightSegment("knecht</strong>", false)
		);
		// @formatter:on
		Assert.assertEquals(expected, result);

		// @formatter:off
		result = index.getHighlightedHtml("H<sub>2</sub>O H<sub>2</sub>SO<sub>4</sub>", "H2S").getSegments();
		expected = newList(
				new HighlightSegment("H<sub>2</sub>O ", false),
				new HighlightSegment("H", true),
				new HighlightSegment("<sub>", false),
				new HighlightSegment("2", true),
				new HighlightSegment("</sub>", false),
				new HighlightSegment("S", true),
				new HighlightSegment("O<sub>4</sub>", false)
		);
		// @formatter:on
		Assert.assertEquals(expected, result);

		// @formatter:off
		result = index.getHighlightedHtml("<b>A<i>B</i>C<span></span>D</b>E", "abcde").getSegments();
		expected = newList(
				new HighlightSegment("<b>", false),
				new HighlightSegment("A", true),
				new HighlightSegment("<i>", false),
				new HighlightSegment("B", true),
				new HighlightSegment("</i>", false),
				new HighlightSegment("C", true),
				new HighlightSegment("<span></span>", false),
				new HighlightSegment("D", true),
				new HighlightSegment("</b>", false),
				new HighlightSegment("E", true)
		);
		// @formatter:on
		Assert.assertEquals(expected, result);

	}

}
