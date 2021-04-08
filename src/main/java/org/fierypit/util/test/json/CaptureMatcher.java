package org.fierypit.util.test.json;

import java.util.function.Consumer;

import javax.json.JsonValue;

class CaptureMatcher<T extends JsonValue>
implements JsonMatcher<T>
{
	private final JsonMatcher<T> matcher;
	private final Consumer<T> consumer;

	CaptureMatcher(JsonMatcher<T> matcher, Consumer<T> consumer)
	{
		this.matcher = matcher;
		this.consumer = consumer;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean test(JsonValue value)
	{
		boolean match = matcher.test(value);
		if (match) {
			consumer.accept((T) value);
		}
		return match;
	}

	@Override
	public String toString()
	{
		return "(capture)" + matcher.toString();
	}
}
