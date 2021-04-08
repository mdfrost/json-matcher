package org.fierypit.util.test.json;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * Matcher implementation which matches a {@link JsonObject} in various ways.
 */
class ObjectMatcher
implements JsonMatcher<JsonObject>
{
	private final Map<String, JsonMatcher<?>> expected;
	private final boolean exact;

	ObjectMatcher(Map<String, JsonMatcher<?>> expected, boolean exact)
	{
		this.expected = Map.copyOf(expected);
		this.exact = exact;
	}

	@Override
	public boolean test(JsonValue value)
	{
		if (!(value instanceof JsonObject jsonObject)) {
			// Not an object.
			return false;
		}

		if (exact && jsonObject.size() != expected.size()) {
			return false;
		}

		for (Map.Entry<String, JsonMatcher<?>> entry : expected.entrySet()) {
			JsonValue memberValue = jsonObject.get(entry.getKey());
			if (memberValue == null || !entry.getValue().test(memberValue)) {
				return false;	// Expected member missing or not matching expectation.
			}
		}

		// All of the expected values have been found. For an exact match we also need there to be no unexpected values, but
		// this was handled earlier by checking array size against expected size.

		return true;
	}

	@Override
	public String toString()
	{
		return (exact ? "(exact){" : "(contains){")
				+ expected.entrySet().stream()
						.sorted(Map.Entry.comparingByKey())
						.map(entry -> "\"" + entry.getKey() + "\":" + entry.getValue())
						.collect(Collectors.joining(","))
				+ "}";
	}
}
