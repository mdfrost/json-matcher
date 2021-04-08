package org.fierypit.util.test.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonValue;

class ArrayBuilderImpl
implements JsonMatcher.ArrayBuilder
{
	private final List<JsonMatcher<?>> expected;

	ArrayBuilderImpl()
	{
		this.expected = new ArrayList<>();
	}

	/** Private copy constructor for use by {@link #duplicate()}  */
	private ArrayBuilderImpl(ArrayBuilderImpl orig)
	{
		this.expected = new ArrayList<>(orig.expected);
	}

	@Override
	public JsonMatcher.ArrayBuilder add(String value)
	{
		return add(JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ArrayBuilder add(int value)
	{
		return add(JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ArrayBuilder add(long value)
	{
		return add(JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ArrayBuilder add(double value)
	{
		return add(JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ArrayBuilder add(BigInteger value)
	{
		return add(JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ArrayBuilder add(BigDecimal value)
	{
		return add(JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ArrayBuilder add(boolean value)
	{
		return add(JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ArrayBuilder add(JsonValue value)
	{
		return add(JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ArrayBuilder addNull()
	{
		return add(JsonMatcher.NULL);
	}

	@Override
	public JsonMatcher.ArrayBuilder add(JsonMatcher<?> valueMatcher)
	{
		expected.add(valueMatcher);
		return this;
	}

	@Override
	public JsonMatcher.ArrayBuilder addAll(Collection<?> values)
	{
		for (Object value : values) {
			add(JsonMatcher.collectionValue(value));
		}
		return this;
	}

	@Override
	public JsonMatcher.ArrayBuilder set(int index, String value)
	{
		return set(index, JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ArrayBuilder set(int index, int value)
	{
		return set(index, JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ArrayBuilder set(int index, long value)
	{
		return set(index, JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ArrayBuilder set(int index, double value)
	{
		return set(index, JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ArrayBuilder set(int index, BigInteger value)
	{
		return set(index, JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ArrayBuilder set(int index, BigDecimal value)
	{
		return set(index, JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ArrayBuilder set(int index, boolean value)
	{
		return set(index, JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ArrayBuilder set(int index, JsonValue value)
	{
		return set(index, JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ArrayBuilder set(int index, JsonMatcher<?> valueMatcher)
	{
		// We rely on ArrayList.set() to throw the expected IndexOutOfBoundsException if the index is invalid.
		expected.set(index, valueMatcher);
		return this;
	}

	@Override
	public JsonMatcher.ArrayBuilder setNull(int index)
	{
		return set(index, JsonMatcher.NULL);
	}

	@Override
	public JsonMatcher.ArrayBuilder remove(int index)
	{
		expected.remove(index);
		return this;
	}

	@Override
	public JsonMatcher.ArrayBuilder reset()
	{
		expected.clear();
		return this;
	}

	@Override
	public JsonMatcher.ArrayBuilder duplicate()
	{
		return new ArrayBuilderImpl(this);
	}

	@Override
	public JsonMatcher<JsonArray> exact()
	{
		return new OrderedArrayMatcher(expected);
	}

	@Override
	public JsonMatcher<JsonArray> exactIgnoreOrder()
	{
		return new UnorderedArrayMatcher(expected, true);
	}

	@Override
	public JsonMatcher<JsonArray> containsIgnoreOrder()
	{
		return new UnorderedArrayMatcher(expected, false);
	}
}
