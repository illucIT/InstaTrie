package com.illucit.instatrie.splitter;

import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;

import com.illucit.instatrie.highlight.HighlightedString;
import com.illucit.instatrie.highlight.SubwordHighlighter;
import com.illucit.instatrie.highlight.HighlightedString.Highlight;
import com.illucit.util.ASCIIUtils;

/**
 * Implementation of a {@link WordSplitter} which uses a regular expression to
 * extract subwords. All string values will be normalized to lower case. Also
 * normalization of unicode characters to their ASCII value (like &auml; to a)
 * is enabled by default.<br>
 * <br>
 * This splitter class also implements {@link SubwordHighlighter} and can
 * therefore be used for highlighting query words in a model string
 * 
 * 
 * @author Christian Simon
 *
 * @param <T>
 *            model type
 */
public class StringWordSplitter<T> implements WordSplitter<T>, SubwordHighlighter {

	public static final Pattern DEFAULT_SUBWORD_PATTERN = Pattern.compile("[a-z0-9]+");

	public static final Pattern SIMPLE_TAG_PATTERN = Pattern.compile("</?[a-z]+>");

	private final Pattern subwordPattern;

	private final Function<T, String> resolveIndexString;

	private final boolean normalizeUnicode;

	/**
	 * Create new string word splitter with default subword pattern. Unicode
	 * normalization is performed.
	 * 
	 * @param resolveIndexString
	 *            transformation function get get the indexable string from a
	 *            model instance
	 */
	public StringWordSplitter(Function<T, String> resolveIndexString) {
		this(resolveIndexString, null, true);
	}

	/**
	 * Create new string word splitter.
	 * 
	 * @param resolveIndexString
	 *            transformation function get the indexable string from a model
	 *            instance
	 * @param subwordPattern
	 *            pattern to identify groups or characters to index as
	 *            individual words (if null is given, the default alphanumeric
	 *            subword pattern is used)
	 * @param normalizeUnicode
	 *            flag, if unicode special characters whould be normalized to
	 *            ASCII, if possible
	 */
	public StringWordSplitter(Function<T, String> resolveIndexString, String subwordPattern, boolean normalizeUnicode) {
		this.resolveIndexString = resolveIndexString;
		this.subwordPattern = (subwordPattern != null) ? Pattern.compile(subwordPattern) : DEFAULT_SUBWORD_PATTERN;
		this.normalizeUnicode = normalizeUnicode;
	}

	@Override
	public Set<String> split(T data) {
		if (data == null) {
			return null;
		}
		String indexableData = resolveIndexString.apply(data);
		if (indexableData == null) {
			return null;
		}
		indexableData = indexableData.toLowerCase();
		if (normalizeUnicode) {
			indexableData = ASCIIUtils.foldToASCII(indexableData);
		}

		Matcher subwordMatcher = subwordPattern.matcher(indexableData);
		TreeSet<String> result = new TreeSet<>();

		while (subwordMatcher.find()) {
			result.add(subwordMatcher.group());
		}

		return result;
	}

	@Override
	public HighlightedString highlightSubwordPrefixes(String value, Set<String> queryWords) {
		return highlightSubwordPrefixes(value, queryWords, false);
	}

	@Override
	public HighlightedString highlightSubwordPrefixesWithHtml(String value, Set<String> queryWords) {
		return highlightSubwordPrefixes(value, queryWords, true);
	}

	private HighlightedString highlightSubwordPrefixes(String value, Set<String> queryWords, boolean html) {

		if (queryWords == null || queryWords.isEmpty()) {
			// No filtering enabled
			return new HighlightedString(value);
		}

		if (value == null || value.trim().isEmpty()) {
			// No highlights possible
			return new HighlightedString(value);
		}

		// Order by length (descending), so longer hits are found before shorted
		// prefixes
		TreeSet<String> queryWordsSorted = new TreeSet<>(COMPARE_BY_LENGTH);
		queryWordsSorted.addAll(queryWords);

		// position and size of highlights still synchronous
		String valueLowerCase = value.toLowerCase();

		LinkedList<Highlight> highlights = new LinkedList<>();

		boolean lengthChanged = false;
		String valueTransformed;
		int[] posMap = null;
		if (normalizeUnicode || html) {
			valueTransformed = ASCIIUtils.foldToASCII(valueLowerCase);
			if (valueTransformed.length() != value.length()) {
				lengthChanged = true;
			}
			if (html) {
				int lengthBeforeHtmlTransform = valueTransformed.length();
				valueTransformed = removeSimpleHtmlTags(valueTransformed);
				if (valueTransformed.length() != lengthBeforeHtmlTransform) {
					lengthChanged = true;
				}
			}
			if (lengthChanged) {
				posMap = getPositionMap(valueLowerCase, html);
			}
		} else {
			valueTransformed = valueLowerCase;
		}

		// Find possible prefix matches and check if query words match
		Matcher m = subwordPattern.matcher(valueTransformed);
		while (m.find()) {

			int start = m.start();

			for (String queryWord : queryWordsSorted) {
				if (valueTransformed.startsWith(queryWord, start)) {

					int length = queryWord.length();

					int hlStart;
					int hlLength;
					if (!lengthChanged) {
						hlStart = start;
						hlLength = length;
					} else {
						// Map positions
						hlStart = posMap[start];
						int hlEnd = posMap[start + length];
						hlLength = hlEnd - hlStart;
					}

					highlights.add(new Highlight(hlStart, hlLength));
					break;
				}
			}

		}
		
		// Exclude tags from highlighted segments and split highlighting for it (so tags do not overlap later)
		if (html) {
			LinkedList<Highlight> resultHighlights = new LinkedList<>();
			for (Highlight highlight : highlights) {
				int hlStart = highlight.getStart();
				String hlText = value.substring(hlStart, highlight.getLength() + hlStart);
				int hlEnd = hlText.length();
				Matcher mTags = SIMPLE_TAG_PATTERN.matcher(hlText);
				int latestPos = 0;
				while (mTags.find()) {
					int foundPos = mTags.start();
					if (latestPos < foundPos) {
						// Add previous segment
						resultHighlights.add(new Highlight(hlStart + latestPos, foundPos - latestPos));
					}
					latestPos = mTags.end();
				}
				if (latestPos < hlEnd) {
					// Add remainder to highlight
					resultHighlights.add(new Highlight(hlStart + latestPos, hlEnd - latestPos));
				}
			}
			highlights = resultHighlights;
		}

		return new HighlightedString(value, highlights);
	}

	/**
	 * Calculate length mapping of positions in transformed string.
	 * 
	 * @param inputStr
	 *            input string
	 * @param html
	 *            if true, ignore add HTML tags as gaps in position map
	 * @return array of positions. The index represents the positions in the
	 *         output string and the value is the corresponding position in the
	 *         input string
	 */
	private static int[] getPositionMap(String inputStr, boolean html) {
		char[] input = inputStr.toCharArray();
		int length = input.length;

		// Worst-case length required:
		final int maxSizeNeeded = 4 * length;
		char[] output = new char[maxSizeNeeded];
		int[] posTransitions = new int[maxSizeNeeded];

		int outputPos = 0;

		Map<Integer, Integer> htmlTagLength = new Hashtable<>();
		if (html) {
			Matcher m = SIMPLE_TAG_PATTERN.matcher(inputStr);
			while (m.find()) {
				int mPos = m.start();
				int mLen = m.end() - m.start();
				htmlTagLength.put(mPos, mLen);
			}
		}

		for (int inputPos = 0; inputPos < length; inputPos++) {
			int nextInputPos = inputPos + 1;
			if (htmlTagLength.containsKey(inputPos)) {
				// Cursor is on HTML tag - skip over it
				int htmlLen = htmlTagLength.get(inputPos);
				inputPos += htmlLen - 1;
				continue;
			}
			int nextOutputPos = ASCIIFoldingFilter.foldToASCII(input, inputPos, output, outputPos, 1);
			for (int curOutputPos = outputPos + 1; curOutputPos <= nextOutputPos; curOutputPos++) {
				posTransitions[curOutputPos] = nextInputPos;
			}
			outputPos = nextOutputPos;
		}

		return posTransitions;
	}

	private static String removeSimpleHtmlTags(String html) {
		return html == null ? null : html.replaceAll(SIMPLE_TAG_PATTERN.pattern(), "");
	}

	/**
	 * String Comparator to sort by length (descending) as primary, then
	 * alphanumerical as secondary feature.
	 */
	private static Comparator<String> COMPARE_BY_LENGTH = (o1, o2) -> {
		int result = -Integer.compare(o1.length(), o2.length());
		if (result != 0) {
			return result;
		}
		return o1.compareTo(o2);
	};

	/**
	 * {@code StringWordSplitter} implementation for String models which use the
	 * complete String data as source for the word splitter.
	 * 
	 * @author Christian Simon
	 *
	 */
	public static class IdentityStringWordSplitter extends StringWordSplitter<String> {

		private static final IdentityStringWordSplitter INSTANCE = new IdentityStringWordSplitter();

		/**
		 * Create Identity String Word Splitter.
		 */
		private IdentityStringWordSplitter() {
			super(Function.identity());
		}

		/**
		 * Get a static instance to this word splitter.
		 * 
		 * @return word splitter instance
		 */
		public static IdentityStringWordSplitter instance() {
			return INSTANCE;
		}

	}

}
