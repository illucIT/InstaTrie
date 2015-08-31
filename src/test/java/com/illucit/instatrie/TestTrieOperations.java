package com.illucit.instatrie;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.illucit.instatrie.trie.Trie;

/**
 * Test suite for all major trie operations.
 * 
 * @author Christian Simon
 *
 */
public class TestTrieOperations {

	private static final Random random = new Random();

	/**
	 * Generate random word. The length of the word and the characters are
	 * equally distributed.
	 * 
	 * @param prefix
	 *            fixed prefix (optional)
	 * @param minVarLength
	 *            minimum length of veriable string part
	 * @param maxVarLength
	 *            maximum length of veriable string part
	 * @param minChar
	 *            minimum char in Unicode range
	 * @param maxChar
	 *            maximum char in Unicode range
	 * @return random word under the given preconditions
	 * @throws IllegalArgumentException
	 *             if the preconditions are invalid (min > max)
	 */
	private static String genWord(String prefix, int minVarLength, int maxVarLength, char minChar, char maxChar) {
		if (minVarLength > maxVarLength) {
			throw new IllegalArgumentException();
		}
		if (minChar > maxChar) {
			throw new IllegalArgumentException();
		}
		int length = minVarLength;
		if (maxVarLength > minVarLength) {
			length += random.nextInt(maxVarLength - minVarLength);
		}
		char[] result = new char[length];
		for (int i = 0; i < length; i++) {
			char c = (char) (minChar + (char) random.nextInt(maxChar - minChar));
			result[i] = c;
		}
		if (prefix == null) {
			return String.valueOf(result);
		}
		return prefix + String.valueOf(result);
	}

	/**
	 * Generate a list of random words. The length of the words and the
	 * characters are equally distributed.
	 * 
	 * @param prefix
	 *            fixed prefix (optional)
	 * @param minVarLength
	 *            minimum length of veriable string part
	 * @param maxVarLength
	 *            maximum length of veriable string part
	 * @param minChar
	 *            minimum char in Unicode range
	 * @param maxChar
	 *            maximum char in Unicode range
	 * @return list of random words under the given preconditions
	 * @throws IllegalArgumentException
	 *             if the preconditions are invalid (min > max)
	 */
	private static List<String> genWords(String prefix, int minVarLength, int maxVarLength, char minChar, char maxChar,
			int numStrings) {
		LinkedList<String> result = new LinkedList<>();
		for (int i = 0; i < numStrings; i++) {
			result.add(genWord(prefix, minVarLength, maxVarLength, minChar, maxChar));
		}
		return result;
	}

	/**
	 * Generate a list of random words. The length of the words and the
	 * characters are equally distributed. The alpha character range (a-z) is
	 * taken.
	 * 
	 * @param prefix
	 *            fixed prefix (optional)
	 * @param minVarLength
	 *            minimum length of veriable string part
	 * @param maxVarLength
	 *            maximum length of veriable string part
	 * @return list of random words under the given preconditions
	 * @throws IllegalArgumentException
	 *             if the preconditions are invalid (min > max)
	 */
	private static List<String> genWords(String prefix, int minVarLength, int maxVarLength, int numStrings) {
		return genWords(prefix, minVarLength, maxVarLength, 'a', 'z', numStrings);
	}

	@Test
	public void runTestSuite() {
		runTestSuite(false);
	}

	/**
	 * Main method (verbose variant of the test suite, which prints also
	 * performance information).
	 * 
	 * @param args
	 *            not used
	 */
	public static void main(String[] args) {
		new TestTrieOperations().runTestSuite(true);
	}

	/**
	 * Run test suite.
	 * 
	 * @param verbose
	 *            print debug messages to STDOUT/STDERR, if true
	 */
	private void runTestSuite(boolean verbose) {

		final int NUM_PREFIXES = 5;
		final int NUM_POS_PRE_PREFIX = 5000;
		final int NUM_NEG = 5000;

		Trie<String> trie = new Trie<>();

		List<String> prefixes = genWords(null, 0, 3, NUM_PREFIXES);

		long start_date = new Date().getTime();
		long end_date;

		String unfoundPrefix = "qwertz";
		String unfoundPrefix2 = "zzzzzz";
		String unfoundPrefix3 = "aaa";

		// Generate positive Strings
		LinkedList<String> positives = new LinkedList<>();
		for (String s : genWords(null, 3, 8, NUM_POS_PRE_PREFIX)) {
			if (!s.startsWith(unfoundPrefix) && !s.startsWith(unfoundPrefix2) && !s.startsWith(unfoundPrefix3)) {
				positives.add(s);
			}
		}

		for (String prefix : prefixes) {
			for (String s : genWords(prefix, 2, 6, NUM_POS_PRE_PREFIX)) {
				if (!s.startsWith(unfoundPrefix) && !s.startsWith(unfoundPrefix2) && !s.startsWith(unfoundPrefix3)) {
					positives.add(s);
				}
			}
		}
		if (verbose) {
			end_date = new Date().getTime();
			System.out
					.println("Generated " + positives.size() + " positive Strings: " + (end_date - start_date) + "ms");
			start_date = end_date;
		}

		// Test add empty string
		trie.insert("");
		if (verbose) {
			end_date = new Date().getTime();
			System.out.println("Insert empty String: " + (end_date - start_date) + "ms");
			start_date = end_date;
		}

		// Test add positive Strings
		for (String s : positives) {
			trie.insert(s, s);
		}
		if (verbose) {
			end_date = new Date().getTime();
			System.out.println("Insert positive Strings: " + (end_date - start_date) + "ms");
			start_date = end_date;
		}

		// Generate negative Strings
		LinkedList<String> negatives = new LinkedList<>();
		HashSet<String> allPositivesForCheck = new HashSet<>(positives);
		HashSet<String> negativesWithPrefixInPositives = new HashSet<>();
		for (String s : genWords(null, 1, 6, NUM_NEG)) {
			if (!allPositivesForCheck.contains(s)) {
				negatives.add(s);
				for (String pos : positives) {
					if (pos.startsWith(s)) {
						negativesWithPrefixInPositives.add(s);
					}
				}
			}
		}
		if (verbose) {
			end_date = new Date().getTime();
			System.out.println("Generate " + negatives.size() + " negative Strings (with "
					+ negativesWithPrefixInPositives.size() + " prefixes) : " + (end_date - start_date) + "ms");
			start_date = end_date;
		}

		LinkedList<String> posAndNeg = new LinkedList<>();
		posAndNeg.addAll(positives);
		posAndNeg.addAll(negatives);
		Collections.shuffle(posAndNeg);
		if (verbose) {
			end_date = new Date().getTime();
			System.out.println("Compiled " + posAndNeg.size() + " positive and negative Strings: "
					+ (end_date - start_date) + "ms");
			start_date = end_date;
		}

		int foundPosCandidates = 0;
		for (String candidate : posAndNeg) {
			boolean shouldBeContainedPrefix = allPositivesForCheck.contains(candidate)
					|| negativesWithPrefixInPositives.contains(candidate);
			if (shouldBeContainedPrefix) {
				boolean contained = trie.containsPrefix(candidate);
				if (!contained) {
					if (verbose) {
						System.out.println("!!! Not contained: " + candidate);
						if (allPositivesForCheck.contains(candidate)) {
							System.out.println("!!! In positive list");
						}
						if (negativesWithPrefixInPositives.contains(candidate)) {
							System.out.println("!!! In negative prefix list");
							for (String pos : positives) {
								if (pos.startsWith(candidate)) {
									System.out.println("!!! Prefix in positive list: " + pos);
								}
							}
						}
					}
				}
				Assert.assertTrue(contained);
				foundPosCandidates++;
			}
			boolean shouldBeContained = allPositivesForCheck.contains(candidate);
			boolean contained = trie.contains(candidate);
			if (shouldBeContained && !contained) {
				if (verbose) {
					System.out.println("!!! Not contained: " + candidate);
					if (allPositivesForCheck.contains(candidate)) {
						System.out.println("!!! In positive list");
					}
					if (negativesWithPrefixInPositives.contains(candidate)) {
						System.out.println("!!! In negative prefix list");
						for (String pos : positives) {
							if (pos.startsWith(candidate)) {
								System.out.println("!!! Prefix in positive list: " + pos);
							}
						}
					}
				}
				trie.contains(candidate);
			}
			if (!shouldBeContained && contained) {
				if (verbose) {
					System.out.println("!!! contained: " + candidate);
					if (allPositivesForCheck.contains(candidate)) {
						System.out.println("!!! In positive list");
					}
					if (negativesWithPrefixInPositives.contains(candidate)) {
						System.out.println("!!! In negative prefix list");
						for (String pos : positives) {
							if (pos.startsWith(candidate)) {
								System.out.println("!!! Prefix in positive list: " + pos);
							}
						}
					}
				}
				trie.contains(candidate);
			}
			Assert.assertTrue(contained == shouldBeContained);
		}
		if (verbose) {
			end_date = new Date().getTime();
			System.out.println("Checked " + foundPosCandidates
					+ " positive (and prefixed negative) Strings (contains): " + (end_date - start_date) + "ms");
			start_date = end_date;
		}

		int foundNegCandidates = 0;
		String[] unfoundPrefixes = { unfoundPrefix, unfoundPrefix2, unfoundPrefix3 };
		for (String candidate : unfoundPrefixes) {
			Assert.assertFalse(trie.containsPrefix(candidate.toCharArray()));
			foundNegCandidates++;
		}
		if (verbose) {
			end_date = new Date().getTime();
			System.out.println("Checked " + foundNegCandidates + " negative Strings (contains): "
					+ (end_date - start_date) + "ms");
			start_date = end_date;
		}

		for (String candidate : posAndNeg) {
			boolean shouldBeContained = allPositivesForCheck.contains(candidate);
			String data = trie.getData(candidate.toCharArray());
			if (shouldBeContained) {
				Assert.assertEquals(candidate, data);
			} else {
				if (data != null) {
					if (verbose) {
						System.err.println("!!! Found " + data + " for candidate " + candidate);
						if (allPositivesForCheck.contains(data)) {
							System.err.println("!!! is in positive list");
						}
					}
				}
				Assert.assertNull(data);
			}
		}
		if (verbose) {
			end_date = new Date().getTime();
			System.out.println("Checked " + posAndNeg.size() + " positive and negative Strings (getData): "
					+ (end_date - start_date) + "ms");
			start_date = end_date;
		}
	}

}
