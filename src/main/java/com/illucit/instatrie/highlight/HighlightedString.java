package com.illucit.instatrie.highlight;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 * Result class to store the highlights of a search query in a model string.
 * 
 * @author Christian Simon
 *
 */
public class HighlightedString implements Serializable {

	private static final long serialVersionUID = -454013472639713514L;

	private final String value;

	private final TreeSet<Highlight> highlights;

	/**
	 * Construct hightlighted String instance without highlighting.
	 * 
	 * @param value
	 *            model value string
	 */
	public HighlightedString(String value) {
		this.value = value;
		this.highlights = null;
	}

	/**
	 * Construct hightlighted String. It is required that the highlights do not
	 * overlap or exceed the length of the string.
	 * 
	 * @param value
	 *            model value string
	 * @param highlights
	 *            array of highlights (found prefixes in model string)
	 */
	public HighlightedString(String value, Highlight[] highlights) {
		this.value = value;
		this.highlights = new TreeSet<>(Arrays.asList(highlights));
	}

	/**
	 * Construct hightlighted String. It is required that the highlights do not
	 * overlap or exceed the length of the string.
	 * 
	 * @param value
	 *            model value string
	 * @param highlights
	 *            collection of highlights (found prefixes in model string)
	 */
	public HighlightedString(String value, Collection<Highlight> highlights) {
		this.value = value;
		this.highlights = new TreeSet<>(highlights);
	}

	/**
	 * Get all highlighted segments. The segments are disjunct and form the
	 * whole model value string on concatenation, while each segment can be
	 * either highlighted or not. Adjacent segments should not have the same
	 * highlighted characteristic. If highlights are overlapping or exceeding
	 * the string size, those parts of the highlights are ignored.
	 * 
	 * @return list of segments in order
	 */
	public List<HighlightSegment> getSegments() {
		LinkedList<HighlightSegment> result = new LinkedList<>();
		if (value == null || value.isEmpty()) {
			// Return empty list for empty value
			return result;
		}

		if (highlights == null || highlights.isEmpty()) {
			// Return one big non-highlighted String for no highlights at all
			result.add(new HighlightSegment(value, false));
			return result;
		}
		int pos = 0;
		int max = value.length();
		// Highlights are ordered by start position
		for (Highlight highlight : this.highlights) {
			int hStart = highlight.getStart();
			int hLen = highlight.getLength();
			if (hStart < pos) {
				// Overlapping previous highlight. Skip overlapping part
				hLen -= (pos - hStart);
				hStart = pos;
				if (hLen <= 0) {
					// Skipped completely
					continue;
				}
			}
			if (hStart >= max) {
				// Highlight outside of range of value
				// All remaining highlights will be out of range too
				break;
			}
			if (hLen > max - hStart) {
				// Highlight too large (exceeding String length)
				// Resume by limiting highlight to maximum
				hLen = max - hStart;
			}
			if (hLen <= 0) {
				// highlight is empty or trimmed to empty
				continue;
			}

			// Now the highlight is valid:
			// pos <= hStart < hStart + hLen <= max
			assert pos <= hStart : "pos <= hStart";
			assert hStart < hStart + hLen : "hStart < hStart + hLen";
			assert hStart + hLen <= max : "hStart + hLen <= max";

			// Add non-highlighted segment (if not empty)
			if (hStart > pos) {
				result.add(new HighlightSegment(value.substring(pos, hStart), false));
				pos = hStart;
			}

			// Add highlighted
			result.add(new HighlightSegment(value.substring(pos, pos + hLen), true));
			pos = pos + hLen;
		}

		// Add final non-highlighted segment
		if (pos < max) {
			result.add(new HighlightSegment(value.substring(pos), false));
		}

		// Assert the segmentation was complete, non-overlapping and in order
		assert value.equals(result.stream().map(hl -> hl.getValue()).reduce("", (x, y) -> x + y)) : "correct segmentation";

		return result;
	}

	/**
	 * POJO to store highlight interval coordinates (position and length).
	 * Highlights have a natural ordering by start position (ascending).
	 * Highlights need to have a non-negative start position and a positive
	 * length. Different highlights in the same string must not overlap or
	 * exceed the length of the string.
	 * 
	 * @author Christian Simon
	 *
	 */
	public static class Highlight implements Serializable, Comparable<Highlight> {

		private static final long serialVersionUID = 7167866898333608642L;

		private final int start;

		private final int length;

		/**
		 * Create highlighted interval.
		 * 
		 * @param start
		 *            start position
		 * @param length
		 *            length of interval
		 */
		public Highlight(int start, int length) {
			if (start < 0 || length < 1) {
				throw new IllegalArgumentException();
			}
			this.start = start;
			this.length = length;
		}

		/**
		 * Get start position of a highlighted interval
		 * 
		 * @return start position
		 */
		public int getStart() {
			return start;
		}

		/**
		 * Get length of highlighted interval.
		 * 
		 * @return length
		 */
		public int getLength() {
			return length;
		}

		@Override
		public int compareTo(Highlight o) {
			// Compare by start position ascending
			int result = Integer.compare(start, o.start);
			if (result != 0) {
				return result;
			}
			// If highlights start at the same position, order the longest
			// first, so the shorter ones are being consumed by the segmenting
			// algorithm afterwards without producing adjacent highlighted
			// segments
			return -Integer.compare(length, o.length);
		}

		@Override
		public String toString() {
			return "[" + getStart() + ":" + getLength() + "]";
		}

	}

	/**
	 * POJO to store single segment in highlighted string. Each segment
	 * describes either a non-highlighted or a highlighted part of the complete
	 * value string. The values of the segments in order constitute the original
	 * value string.
	 * 
	 * @author Christian Simon
	 *
	 */
	public static class HighlightSegment implements Serializable {

		private static final long serialVersionUID = 7615942217422536563L;

		private final String value;

		private final boolean highlighted;

		/**
		 * Create segment.
		 * 
		 * @param value
		 *            segment value
		 * @param highlighted
		 *            true if segment should be highlighted
		 */
		public HighlightSegment(String value, boolean highlighted) {
			this.value = value;
			this.highlighted = highlighted;
		}

		/**
		 * Get string value of segment.
		 * 
		 * @return string value
		 */
		public String getValue() {
			return value;
		}

		/**
		 * Get if segment is highlighted.
		 * 
		 * @return true if segment should be displayed highlighted
		 */
		public boolean isHighlighted() {
			return highlighted;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof HighlightSegment)) {
				return false;
			}
			HighlightSegment other = (HighlightSegment) obj;
			return value.equals(other.value) && (highlighted == other.highlighted);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (highlighted ? 1231 : 1237);
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public String toString() {
			if (!highlighted) {
				return value;
			}
			return "<hl>" + value + "</hl>";
		}

	}
}
