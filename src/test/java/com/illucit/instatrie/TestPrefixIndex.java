package com.illucit.instatrie;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.illucit.instatrie.index.PrefixIndex;
import com.illucit.instatrie.index.TriePrefixIndex;

/**
 * Test suite for {@link TriePrefixIndex}.
 * 
 * @author Christian Simon
 *
 */
public class TestPrefixIndex {

	private LinkedList<TestBean> entries = new LinkedList<>();

	private PrefixIndex<TestBean> index;

	@Before
	public void initialize() {
		index = new TriePrefixIndex<>((TestBean bean) -> bean.getQueryString());

		entries.add(new TestBean(1, "Der Herr der Ringe - Die Gefährten", "J. R. R. Tolkien"));
		entries.add(new TestBean(2, "Der Herr der Ringe - Die Zwei Türme", "J. R. R. Tolkien"));
		entries.add(new TestBean(3, "Der Herr der Ringe - Die Rückkehr des Königs", "J. R. R. Tolkien"));
		entries.add(new TestBean(4, "Der kleine Hobbit", "J. R. R. Tolkien"));
		entries.add(new TestBean(5, "Zwei außer Rand und Band", "Bud Spencer", "Terence Hill"));
		entries.add(new TestBean(6, "Vier Fäuste für ein Halleluja", "Bud Spencer", "Terence Hill"));
		entries.add(new TestBean(7, "Buddy", "Bully Herbig"));

		index.createIndex(entries);
	}

	@Test
	public void testAllEntries() {
		Assert.assertEquals(entries, index.getAll());
	}

	@Test
	public void testEmptySearchIndex() {
		List<TestBean> resultEmpty;

		// Test empty search string

		resultEmpty = index.search("");
		Assert.assertEquals(entries, resultEmpty);

		resultEmpty = index.searchExact("");
		Assert.assertEquals(entries, resultEmpty);

		// Test search string without usable characters

		resultEmpty = index.search("!\"§ $%&/() =?+#*");
		Assert.assertEquals(entries, resultEmpty);

		resultEmpty = index.searchExact("!\"§ $%&/() =?+#*");
		Assert.assertEquals(entries, resultEmpty);
	}

	@Test
	public void testExactSearchSingleWord() {

		String query;
		List<TestBean> expected;
		List<TestBean> result;

		query = "ringe";
		expected = getExpectedResult(1, 2, 3);
		result = index.searchExact(query);
		Assert.assertEquals(expected, result);

		query = "zwei";
		expected = getExpectedResult(2, 5);
		result = index.searchExact(query);
		Assert.assertEquals(expected, result);

		// Normaize case

		query = "TOLKIEN";
		expected = getExpectedResult(1, 2, 3, 4);
		result = index.searchExact(query);
		Assert.assertEquals(expected, result);

		// Normalize diacritical characters

		query = "Turme";
		expected = getExpectedResult(2);
		result = index.searchExact(query);
		Assert.assertEquals(expected, result);

		query = "Undefined";
		expected = getExpectedResult();
		result = index.searchExact(query);
		Assert.assertEquals(expected, result);

	}

	@Test
	public void testExactSearchMultipleWords() {

		String query;
		List<TestBean> expected;
		List<TestBean> result;

		query = "herr ringe";
		expected = getExpectedResult(1, 2, 3);
		result = index.searchExact(query);
		Assert.assertEquals(expected, result);

		query = "Tolkien der";
		expected = getExpectedResult(1, 2, 3, 4);
		result = index.searchExact(query);
		Assert.assertEquals(expected, result);

		query = "Zwei SPENCER";
		expected = getExpectedResult(5);
		result = index.searchExact(query);
		Assert.assertEquals(expected, result);

		query = "Hobbit asdf";
		expected = getExpectedResult();
		result = index.searchExact(query);
		Assert.assertEquals(expected, result);

	}

	@Test
	public void testSearchSingleWord() {

		String query;
		List<TestBean> expected;
		List<TestBean> result;

		query = "bud";
		expected = getExpectedResult(5, 6, 7);
		result = index.search(query);
		Assert.assertEquals(expected, result);

		query = "d";
		expected = getExpectedResult(1, 2, 3, 4);
		result = index.search(query);
		Assert.assertEquals(expected, result);

		query = "h";
		expected = getExpectedResult(1, 2, 3, 4, 5, 6, 7);
		result = index.search(query);
		Assert.assertEquals(expected, result);

		query = "GEFAHR";
		expected = getExpectedResult(1);
		result = index.search(query);
		Assert.assertEquals(expected, result);

		query = "he";
		expected = getExpectedResult(1, 2, 3, 7);
		result = index.search(query);
		Assert.assertEquals(expected, result);

	}

	@Test
	public void testSearchMultipleWords() {

		String query;
		List<TestBean> expected;
		List<TestBean> result;

		query = "bud ter";
		expected = getExpectedResult(5, 6);
		result = index.search(query);
		Assert.assertEquals(expected, result);

		query = "ring j";
		expected = getExpectedResult(1, 2, 3);
		result = index.search(query);
		Assert.assertEquals(expected, result);
		
		query = "hobbit asdf";
		expected = getExpectedResult();
		result = index.search(query);
		Assert.assertEquals(expected, result);

	}

	/**
	 * Get a result list that is expected.
	 * 
	 * @param ids
	 *            ids to include (in this order)
	 * @return list of bean entries
	 */
	private List<TestBean> getExpectedResult(Integer... ids) {
		HashSet<Integer> expectedIdSet = new HashSet<>(Arrays.asList(ids));
		return entries.stream().filter(bean -> expectedIdSet.contains(bean.id)).collect(Collectors.toList());
	}

	/**
	 * Test bean.
	 * 
	 * @author Christian Simon
	 *
	 */
	private static class TestBean implements Serializable {

		private static final long serialVersionUID = 5808216132288606476L;

		private String title;

		private List<String> authors;

		private int id;

		public TestBean(int id, String title, String... authors) {
			this.id = id;
			this.title = title;
			this.authors = Arrays.asList(authors);
		}

		public String getTitle() {
			return title;
		}

		public List<String> getAuthors() {
			return authors;
		}

		public int getId() {
			return id;
		}

		public String getQueryString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append(getTitle()).append(" ");
			for (String author : getAuthors()) {
				buffer.append(author).append(" ");
			}
			return buffer.toString();
		}

		@Override
		public String toString() {
			StringBuffer result = new StringBuffer();
			result.append("[").append(getId()).append(": ").append(getTitle());
			for (String author : getAuthors()) {
				result.append(" ~ ").append(author);
			}
			result.append("]");
			return result.toString();
		}

	}

}
