package org.fierypit.util.test.json;

import java.util.Objects;

import javax.json.JsonValue;

/**
 * Matcher implementation matching a single {@link JsonValue} by object equality. It would actually be possible to just use
 * {@code value::equals} as a lambda, but using this class allows us to implement a nicer {@link #toString()} method.
 */
class ValueMatcher<T extends JsonValue>
implements JsonMatcher<T>
{
	private final T value;

	ValueMatcher(T value)
	{
		this.value = Objects.requireNonNull(value);
	}

	@Override
	public boolean test(JsonValue value)
	{
		return this.value.equals(value);
	}

	@Override
	public String toString()
	{
		return value.toString();
	}
}
