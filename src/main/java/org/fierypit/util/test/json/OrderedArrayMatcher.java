package org.fierypit.util.test.json;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.JsonArray;
import javax.json.JsonValue;

/**
 * Matcher implementation which matches a {@link JsonArray} exactly in order.
 */
class OrderedArrayMatcher
implements JsonMatcher<JsonArray>
{
	private final List<JsonMatcher<?>> expected;

	OrderedArrayMatcher(List<JsonMatcher<?>> expected)
	{
		this.expected = List.copyOf(expected);
	}

	@Override
	public boolean test(JsonValue value)
	{
		if (!(value instanceof JsonArray jsonArray)) {
			// Value is not an array.
			return false;
		}

		int expectedSize = expected.size();

		if (jsonArray.size() != expectedSize) {
			return false;
		}

		Iterator<JsonMatcher<?>> expectedIterator = expected.iterator();
		Iterator<JsonValue> actualIterator = jsonArray.iterator();
		while (expectedIterator.hasNext()) {
			if (!expectedIterator.next().test(actualIterator.next())) {
				return false;
			}
		}

		return true;
	}

	@Override
	public String toString()
	{
		return "(exact)[" + expected.stream().map(Object::toString).collect(Collectors.joining(",")) + "]";
	}
}
