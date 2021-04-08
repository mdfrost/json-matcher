package org.fierypit.util.test.json;

import java.util.Objects;

import javax.json.JsonValue;

class AnyMatcher<T extends JsonValue>
implements JsonMatcher<T>
{
	private final Class<T> type;
	private final String string;

	AnyMatcher(Class<T> type, String string)
	{
		this.type = type;
		this.string = string;
	}

	@Override
	public boolean test(JsonValue value)
	{
		return type.isInstance(Objects.requireNonNull(value));
	}

	@Override
	public String toString()
	{
		return string;
	}
}
