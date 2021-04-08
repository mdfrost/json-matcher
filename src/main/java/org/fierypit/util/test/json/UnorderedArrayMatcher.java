package org.fierypit.util.test.json;

import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.JsonArray;
import javax.json.JsonValue;

/**
 * Matcher implementation which matches a {@link JsonArray} ignoring order.
 */
class UnorderedArrayMatcher
implements JsonMatcher<JsonArray>
{
	private final List<JsonMatcher<?>> expected;
	private final boolean exact;

	UnorderedArrayMatcher(List<JsonMatcher<?>> expected, boolean exact)
	{
		this.expected = List.copyOf(expected);
		this.exact = exact;
	}

	@Override
	public boolean test(JsonValue value)
	{
		if (!(value instanceof JsonArray jsonArray)) {
			return false;	// Not an array.
		}

		int jsonArraySize = jsonArray.size();
		if (exact && jsonArraySize != expected.size()) {
			return false;
		}

		// This matching algorithm is O(N^2), because the inner loop has to perform a linear search over the whole of the array
		// being matched. However, this approach is used because it is deterministic and predictable in the presence of arbitrary
		// user-supplied matchers: matchers are processed in the order they were declared, and array elements are consumed by
		// matchers, always searching from the start of the array.

		BitSet used = new BitSet();	// bit N is set iff array element N has been consumed by a matcher

		matcher_loop: for (JsonMatcher<?> matcher : expected) {
			int index = -1;
			while ((index = used.nextClearBit(index+1)) < jsonArraySize) {
				if (matcher.test(jsonArray.get(index))) {
					used.set(index);
					continue matcher_loop;
				}
			}
			return false;	// This expectation is not matched by any element in the array.
		}

		// All of the expected values have been found. For an exact match we also need there to be no unexpected values, but
		// this was handled earlier by checking array size against expected size.

		return true;
	}

	@Override
	public String toString()
	{
		return (exact ? "(exactIgnoreOrder)[" : "(containsIgnoreOrder)[")
				+ expected.stream().map(Object::toString).collect(Collectors.joining(",")) + "]";
	}
}
