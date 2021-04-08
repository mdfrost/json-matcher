package org.fierypit.util.test.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;
import javax.json.JsonValue;

class ObjectBuilderImpl
implements JsonMatcher.ObjectBuilder
{
	private final Map<String, JsonMatcher<?>> expected;

	ObjectBuilderImpl()
	{
		this.expected = new HashMap<>();
	}

	/** Private copy constructor for use by {@link #duplicate()}  */
	private ObjectBuilderImpl(ObjectBuilderImpl orig)
	{
		this.expected = new HashMap<>(orig.expected);
	}

	@Override
	public JsonMatcher.ObjectBuilder add(String name, String value)
	{
		return add(name, JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ObjectBuilder add(String name, int value)
	{
		return add(name, JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ObjectBuilder add(String name, long value)
	{
		return add(name, JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ObjectBuilder add(String name, double value)
	{
		return add(name, JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ObjectBuilder add(String name, BigInteger value)
	{
		return add(name, JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ObjectBuilder add(String name, BigDecimal value)
	{
		return add(name, JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ObjectBuilder add(String name, boolean value)
	{
		return add(name, JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ObjectBuilder add(String name, JsonValue value)
	{
		return add(name, JsonMatcher.value(value));
	}

	@Override
	public JsonMatcher.ObjectBuilder addNull(String name)
	{
		return add(name, JsonMatcher.NULL);
	}

	@Override
	public JsonMatcher.ObjectBuilder add(String name, JsonMatcher<?> valueMatcher)
	{
		expected.put(name, valueMatcher);
		return this;
	}

	@Override
	public JsonMatcher.ObjectBuilder addAll(Map<String, ?> map)
	{
		for (Map.Entry<String, ?> entry : map.entrySet()) {
			add(entry.getKey(), JsonMatcher.collectionValue(entry.getValue()));
		}
		return this;
	}

	@Override
	public JsonMatcher.ObjectBuilder remove(String name)
	{
		expected.remove(name);
		return this;
	}

	@Override
	public JsonMatcher.ObjectBuilder reset()
	{
		expected.clear();
		return this;
	}

	@Override
	public JsonMatcher.ObjectBuilder duplicate()
	{
		return new ObjectBuilderImpl(this);
	}

	@Override
	public JsonMatcher<JsonObject> exact()
	{
		return new ObjectMatcher(expected, true);
	}

	@Override
	public JsonMatcher<JsonObject> contains()
	{
		return new ObjectMatcher(expected, false);
	}
}
