package org.fierypit.util.test.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 * Object representing a pattern which can be matched against JSON structures.
 */
@FunctionalInterface
public interface JsonMatcher<T extends JsonValue> extends Predicate<JsonValue>
{
	/**
	 * Get a new builder for object matchers.
	 * @return a new {@link ObjectBuilder}
	 */
	static ObjectBuilder object()
	{
		return new ObjectBuilderImpl();
	}

	/**
	 * Get a new builder for object matchers, and add all of the mappings from the supplied {@link Map} to it.
	 * Values from the map are converted to matchers as if by {@link #collectionValue(Object)}.
	 * Although the map generic type is declared as {@code <?,?>}, the keys must all be strings else an exception will be thrown.
	 * @param map the map whose mappings should be added to the builder
	 * @return a new {@link ObjectBuilder} with the mappings added
	 * @exception ClassCastException if any map key is not a String
	 * @exception NullPointerException if any map key or value is {@code null}
	 */
	static ObjectBuilder object(Map<?,?> map)
	{
		ObjectBuilder builder = new ObjectBuilderImpl();

		// We need to add the map entries individually here, in order to ensure that all keys are strings.
		for (Map.Entry<?,?> entry : map.entrySet()) {
			if (entry.getKey() instanceof String key) {
				builder.add(key, collectionValue(entry.getValue()));
			} else {
				throw new ClassCastException("map key is not a string: " + entry.getKey().getClass());
			}
		}

		return builder;
	}

	/**
	 * Get a new builder for array matchers.
	 * @return a new {@link ArrayBuilder}
	 */
	static ArrayBuilder array()
	{
		return new ArrayBuilderImpl();
	}

	/**
	 * Get a new builder for array matchers, and add all of the values from the supplied {@link Collection} to it.
	 * Values from the collection are converted to matchers as if by {@link #collectionValue(Object)}.
	 * @param collection the collection whose values should be added to the builder
	 * @return a new {@link ArrayBuilder} with the mappings added
	 */
	static ArrayBuilder array(Collection<?> collection)
	{
		return new ArrayBuilderImpl().addAll(collection);
	}

	/**
	 * Get a matcher expecting a string {@link JsonValue}.
	 * @param value the expected string
	 * @return a matcher expecting a string {@link JsonValue}, which must match exactly
	 * @exception NullPointerException if {@code value} is {@code null}
	 */
	static JsonMatcher<JsonString> value(String value)
	{
		return value(Json.createValue(Objects.requireNonNull(value)));
	}

	/**
	 * Get a matcher expecting a numeric {@link JsonValue}.
	 * @param value the expected integer value
	 * @return a matcher expecting a numeric {@link JsonValue}, which must match the integer exactly
	 */
	static JsonMatcher<JsonNumber> value(int value)
	{
		return value(Json.createValue(value));
	}

	/**
	 * Get a matcher expecting a numeric {@link JsonValue}.
	 * @param value the expected long integer value
	 * @return a matcher expecting a numeric {@link JsonValue}, which must match the long integer exactly
	 */
	static JsonMatcher<JsonNumber> value(long value)
	{
		return value(Json.createValue(value));
	}

	/**
	 * Get a matcher expecting a numeric {@link JsonValue}.
	 * @param value the expected double value
	 * @return a matcher expecting a numeric {@link JsonValue}, which must match the double value exactly
	 */
	static JsonMatcher<JsonNumber> value(double value)
	{
		return value(Json.createValue(value));
	}

	/**
	 * Get a matcher expecting a numeric {@link JsonValue}.
	 * @param value the expected value
	 * @return a matcher expecting a numeric {@link JsonValue}, which must match the {@link BigInteger} value exactly
	 * @exception NullPointerException if {@code value} is {@code null}
	 */
	static JsonMatcher<JsonNumber> value(BigInteger value)
	{
		return value(Json.createValue(Objects.requireNonNull(value)));
	}

	/**
	 * Get a matcher expecting a numeric {@link JsonValue}.
	 * @param value the expected value
	 * @return a matcher expecting a numeric {@link JsonValue}, which must match the {@link BigDecimal} value exactly
	 * @exception NullPointerException if {@code value} is {@code null}
	 */
	static JsonMatcher<JsonNumber> value(BigDecimal value)
	{
		return value(Json.createValue(Objects.requireNonNull(value)));
	}

	/**
	 * Get a matcher expecting a numeric {@link JsonValue}.
	 * @param value the expected value
	 * @return a matcher expecting a numeric {@link JsonValue}, which must match the {@link BigInteger} value exactly
	 */
	static JsonMatcher<JsonValue> value(boolean value)
	{
		return value ? TRUE : FALSE;
	}

	/**
	 * Get a matcher expecting a single JSON value, which must match exactly.
	 * @param value the expected value
	 * @return a {@link JsonMatcher} which will match the exact value
	 * @exception NullPointerException if {@code value} is {@code null}
	 */
	@SuppressWarnings("unchecked")
	static <T extends JsonValue> JsonMatcher<T> value(T value)
	{
		if (value.equals(JsonValue.TRUE)) {	// Will also throw NullPointerException if value is null
			return (JsonMatcher<T>) TRUE;
		} else if (value.equals(JsonValue.FALSE)) {
			return (JsonMatcher<T>) FALSE;
		} else if (value.equals(JsonValue.NULL)) {
			return (JsonMatcher<T>) NULL;
		} else if (value.equals(JsonValue.EMPTY_JSON_ARRAY)) {
			return (JsonMatcher<T>) EMPTY_ARRAY;
		} else if (value.equals(JsonValue.EMPTY_JSON_OBJECT)) {
			return (JsonMatcher<T>) EMPTY_OBJECT;
		} else {
			return new ValueMatcher<>(value);
		}
	}

	/**
	 * Get a matcher for a single value of a variety of types. This factory method is used when adding elements from collections.
	 * The following types are accepted for {@code value}, and will map to these corresponding JSON values:
	 * <ul>
	 *     <li>{@link JsonMatcher} - returned directly</li>
	 *     <li>{@link JsonValue} (including {@link JsonObject} and {@link JsonArray}) - an explicit JSON value</li>
	 *     <li>{@link String} - a JSON string value</li>
	 *     <li>{@link Integer}, {@link Long}, {@link Float}, {@link Double}, {@link BigInteger}, {@link BigDecimal}
	 *         - a JSON numeric value</li>
	 *     <li>{@link Boolean} - a JSON {@code true} or {@code false} value</li>
	 *     <li>{@link Map} with {@link String} keys - a JSON object, matched in exact mode</li>
	 *     <li>{@link Collection} - a JSON array, matched in exact mode, in the collection's iteration order</li>
	 * </ul>
	 * Any {@link Map} or {@link Collection} encountered will have its values processed recursively by this factory method.
	 * The corresponding JSON object or JSON array matcher will require an exact match.
	 * Note that {@code null} is NOT a supported value, and will cause {@link NullPointerException} to be thrown.
	 * To include a {@code null} member in an array or map, use {@link JsonValue#NULL}.
	 * @param value a value of one of the supported types
	 * @return a {@link JsonMatcher} to match the corresponding JSON value exactly
	 * @exception NullPointerException if {@code value} is {@code null}, or if {@code value} is a {@link Map} or
	 *		{@link Collection}, and some key or value included within it is {@code null}
	 * @exception ClassCastException if {@code value} is not one of the supported types, or if {@code value} is a {@link Map} or
	 * 		{@link Collection}, and some key or value included within it is not of a supported type
	 */
	static JsonMatcher<?> collectionValue(Object value)
	{
		if (value instanceof JsonMatcher<?> jsonMatcher) {
			return jsonMatcher;
		} else if (value instanceof JsonValue jsonValue) {
			return value(jsonValue);
		} else if (value instanceof String stringValue) {
			return value(stringValue);
		} else if (value instanceof Integer intValue) {
			return value(intValue);
		} else if (value instanceof Long longValue) {
			return value(longValue);
		} else if (value instanceof Float floatValue) {
			return value(floatValue);
		} else if (value instanceof Double doubleValue) {
			return value(doubleValue);
		} else if (value instanceof BigInteger bigIntegerValue) {
			return value(bigIntegerValue);
		} else if (value instanceof BigDecimal bigDecimalValue) {
			return value(bigDecimalValue);
		} else if (value instanceof Boolean booleanValue) {
			return value(booleanValue);
		} else if (value instanceof Map<?,?> map) {
			return object(map).exact();
		} else if (value instanceof Collection<?> collection) {
			return array(collection).exact();
		} else {
			throw new ClassCastException("unsupported value type: " + value.getClass());
		}
	}

	/** Flyweight matcher for {@link JsonValue#TRUE}. */
	JsonMatcher<JsonValue> TRUE = new ValueMatcher<>(JsonValue.TRUE);

	/** Flyweight matcher for {@link JsonValue#FALSE}. */
	JsonMatcher<JsonValue> FALSE = new ValueMatcher<>(JsonValue.FALSE);

	/** Flyweight matcher for {@link JsonValue#NULL}. */
	JsonMatcher<JsonValue> NULL = new ValueMatcher<>(JsonValue.NULL);

	/** Flyweight matcher for empty array. */
	JsonMatcher<JsonArray> EMPTY_ARRAY = new ValueMatcher<>(JsonValue.EMPTY_JSON_ARRAY);

	/** Flyweight matcher for empty object. */
	JsonMatcher<JsonObject> EMPTY_OBJECT = new ValueMatcher<>(JsonValue.EMPTY_JSON_OBJECT);

	/** Flyweight matcher for any JSON value. */
	JsonMatcher<JsonValue> ANY_VALUE = new AnyMatcher<>(JsonValue.class, "(any value)");

	/** Flyweight matcher for any JSON number value. */
	JsonMatcher<JsonNumber> ANY_NUMBER = new AnyMatcher<>(JsonNumber.class, "(any number)");

	/** Flyweight matcher for any JSON string value. */
	JsonMatcher<JsonString> ANY_STRING = new AnyMatcher<>(JsonString.class, "(any string)");

	/** Flyweight matcher for any JSON boolean value. */
	JsonMatcher<JsonValue> ANY_BOOLEAN = new AnyMatcher<>(JsonValue.class, "(any boolean)") {
		@Override public boolean test(JsonValue value) {
			JsonValue.ValueType valueType = value.getValueType();
			return valueType == JsonValue.ValueType.FALSE || valueType == JsonValue.ValueType.TRUE;
		}
	};

	/** Flyweight matcher for any JSON object. */
	JsonMatcher<JsonObject> ANY_OBJECT = new AnyMatcher<>(JsonObject.class, "(any object)");

	/** Flyweight matcher for any JSON array. */
	JsonMatcher<JsonArray> ANY_ARRAY = new AnyMatcher<>(JsonArray.class, "(any array)");

	/**
	 * Test whether a {@link JsonValue} matches what this matcher expects. This method implements {@link Predicate#test(Object)}.
	 * Note that this method always takes a {@code JsonValue}, regardless of the type parameter on the instance. This is because
	 * it is always valid to test any JSON value to see whether it matches; the type parameter applies only if the match succeeds.
	 * @param value the value to test
	 * @return {@code true} if the matcher matches the value, or {@code false} otherwise
	 * @exception NullPointerException if {@code value} is {@code null}
	 */
	@Override boolean test(JsonValue value);

	/**
	 * Get a new matcher which wraps this one.
	 * Each time this matcher matches a JSON value, the matched value will be passed to the supplied {@link Consumer}>.
	 * Note that the use of generics to match the types between matcher and consumer relies on the matchers correctly declaring
	 * their generic type. If a custom matcher declares the wrong type, then a capture using this custom matcher will fail with
	 * a {@code ClassCastException}.
	 * @param consumer a consumer of JSON values
	 */
	default JsonMatcher<T> capture(Consumer<T> consumer)
	{
		return new CaptureMatcher<>(this, consumer);
	}

	/**
	 * Interface to a builder, constructing a matcher to match {@link JsonObject} values.
	 * Each of the various {@code add} calls in this builder mutate the state of this builder by adding an expected member to the
	 * object to be matched. All such expected members must be present in a {@link JsonObject} for it to be matched. To actually
	 * perform matches, a matcher must be built from this builder using either the {@link #exact()} or {@link #contains()} methods.
	 * These methods both construct a matcher reflecting the expectations in the builder at the time of the call. After such a
	 * call, the builder object remains valid and can have its expectations further modified, but these changes will not be
	 * reflected in any matchers already built.
	 * The {@link #exact()} and {@link #contains()} both create matchers, but the returned matchers differ in their behaviour.
	 * Matchers created with {@link #exact()} require all of the expected members to be present, and no others; whereas matchers
	 * created with {@link #contains()} require all of the expected members to be present, but will permit (and ignore) any
	 * additional members with different names.
	 */
	interface ObjectBuilder
	{
		/**
		 * Add an expected object member, expecting a string value.
		 * If the builder already expects a member with that name, the expected value will be overwritten by the new one.
		 * @param name the member name
		 * @param value the expected value
		 * @return this {@link ObjectBuilder}, for chaining
		 * @exception NullPointerException if {@code name} or {@code value} is {@code null}
		 */
		ObjectBuilder add(String name, String value);

		/**
		 * Add an expected object member, expecting a numeric value.
		 * If the builder already expects a member with that name, the expected value will be overwritten by the new one.
		 * @param name the member name
		 * @param value the expected value
		 * @return this {@link ObjectBuilder}, for chaining
		 * @exception NullPointerException if {@code name} is {@code null}
		 */
		ObjectBuilder add(String name, int value);

		/**
		 * Add an expected object member, expecting a numeric value.
		 * If the builder already expects a member with that name, the expected value will be overwritten by the new one.
		 * @param name the member name
		 * @param value the expected value
		 * @return this {@link ObjectBuilder}, for chaining
		 * @exception NullPointerException if {@code name} is {@code null}
		 */
		ObjectBuilder add(String name, long value);

		/**
		 * Add an expected object member, expecting a numeric value.
		 * If the builder already expects a member with that name, the expected value will be overwritten by the new one.
		 * @param name the member name
		 * @param value the expected value
		 * @return this {@link ObjectBuilder}, for chaining
		 * @exception NullPointerException if {@code name} is {@code null}
		 */
		ObjectBuilder add(String name, double value);

		/**
		 * Add an expected object member, expecting a numeric value.
		 * If the builder already expects a member with that name, the expected value will be overwritten by the new one.
		 * @param name the member name
		 * @param value the expected value
		 * @return this {@link ObjectBuilder}, for chaining
		 * @exception NullPointerException if {@code name} or {@code value} is {@code null}
		 */
		ObjectBuilder add(String name, BigInteger value);

		/**
		 * Add an expected object member, expecting a numeric value.
		 * If the builder already expects a member with that name, the expected value will be overwritten by the new one.
		 * @param name the member name
		 * @param value the expected value
		 * @return this {@link ObjectBuilder}, for chaining
		 * @exception NullPointerException if {@code name} or {@code value} is {@code null}
		 */
		ObjectBuilder add(String name, BigDecimal value);

		/**
		 * Add an expected object member, expecting a boolean value.
		 * If the builder already expects a member with that name, the expected value will be overwritten by the new one.
		 * @param name the member name
		 * @param value the expected value
		 * @return this {@link ObjectBuilder}, for chaining
		 * @exception NullPointerException if {@code name} is {@code null}
		 */
		ObjectBuilder add(String name, boolean value);

		/**
		 * Add an expected object member, expecting a null value.
		 * If the builder already expects a member with that name, the expected value will be overwritten by the new one.
		 * @param name the member name
		 * @return this {@link ObjectBuilder}, for chaining
		 * @exception NullPointerException if {@code name} is {@code null}
		 */
		ObjectBuilder addNull(String name);

		/**
		 * Add an expected object member, expecting a specific {@link JsonValue} as the member value.
		 * If the builder already expects a member with that name, the expected value will be overwritten by the new one.
		 * @param name the member name
		 * @param value the expected value
		 * @return this {@link ObjectBuilder}, for chaining
		 * @exception NullPointerException if {@code name} or {@code value} is {@code null}
		 */
		ObjectBuilder add(String name, JsonValue value);

		/**
		 * Add an expected object member, expecting a value matched by a {@link JsonMatcher}.
		 * If the builder already expects a member with that name, the expected value will be overwritten by the new one.
		 * @param name the member name
		 * @param valueMatcher a matcher for the expected value
		 * @return this {@link ObjectBuilder}, for chaining
		 * @exception NullPointerException if {@code name} or {@code valueMatcher} is {@code null}
		 */
		ObjectBuilder add(String name, JsonMatcher<?> valueMatcher);

		/**
		 * Add multiple expected object members, copied from an existing map. Values in the map will be converted to matchers by
		 * the {@link #collectionValue(Object)} factory method. If the builder already expects any members with names matching
		 * those in the provided map, the expected values will be overwritten by the new ones from the object.
		 * @param map the map whose mappings will be added
		 * @return this {@link ObjectBuilder}, for chaining
		 * @exception NullPointerException if {@code name} is {@code null}
		 */
		ObjectBuilder addAll(Map<String,?> map);

		/**
		 * Remove an expectation from this builder.
		 * @param name the member name to remove
		 * @return this {@link ObjectBuilder}, for chaining
		 * @exception NullPointerException if {@code name} is {@code null}
		 */
		ObjectBuilder remove(String name);

		/**
		 * Reset this builder to contain no expectations. This restores the builder to a freshly-created state.
		 * @return this {@link ObjectBuilder}, for chaining
		 */
		ObjectBuilder reset();

		/**
		 * Duplicate this builder, returning a new builder with the same set of expectations. The original and duplicate are
		 * entirely independent: changes made to one will not affect the other.
		 * @return a new builder initially containing the current set of expectations from this builder
		 */
		ObjectBuilder duplicate();

		/**
		 * Build a matcher which matches objects containing exactly the expected members from the current state of this builder.
		 * Objects containing any members not included in the expectations will not be matched.
		 * The matcher returned from this method will not reflect any changes to the builder made after this call.
		 * @return a matcher that matches objects exactly matching this builder's current state
		 */
		JsonMatcher<JsonObject> exact();

		/**
		 * Build a matcher which matches objects containing all of the expected members from this builder, but which may contain
		 * other members too. Any members with names not included in the expectations will be ignored by the matcher.
		 * The matcher returned from this method will not reflect any changes to the builder made after this call.
		 * @return a matcher that matches objects containing at least the members in this builder's current state
		 */
		JsonMatcher<JsonObject> contains();
	}

	// TODO Fix javadoc for ArrayBuilder; note that order-ignoring matchers are O(N^2).
	/**
	 * Interface to a builder, constructing a matcher to match {@link JsonArray} values.
	 * Each of the various {@code add} and {@code set} calls in this builder mutate the state of this builder by appending an
	 * expected member to the end of the array to be matched (or by replacing an existing expectation, in the case of
	 * {@code set}). All such expected members must be present in a {@link JsonArray} for it to be matched.
	 * To actually perform matches, a matcher must be built from this builder using one of the {@link #exact()},
	 * {@link #exactIgnoreOrder()} or {@link #containsIgnoreOrder()} methods. These methods each construct a matcher reflecting
	 * the expectations in the builder at the time of the call. After such a call, the builder object remains valid and can have
	 * its expectations further modified, but these changes will not be reflected in any matchers already built.
	 * The {@link #exact()}, {@link #exactIgnoreOrder()} and {@link #containsIgnoreOrder()} all create matchers, but the returned
	 * matchers differ in their behaviour. Matchers created with {@link #exact()} will only match a JSON array that contains
	 * exactly the expected values in the expected order, just like {@link JsonArray#equals(Object)} would. On the other hand,
	 * matchers created by {@link #exactIgnoreOrder()} and {@link #containsIgnoreOrder()} treat JSON arrays as unordered bags
	 * of values. A matcher created by {@link #exactIgnoreOrder()} requires exactly the expected values and repetitions to be
	 * present, and no others; a matcher created by {@link #containsIgnoreOrder()} requires all expected values (repeated as
	 * indicated) to be present, but permits others to be present as long as all expected values are. Note, however, that these
	 * order-ignoring matchers will take time proportional to the product of the number of expectations and the size of the array
	 * being matched - therefore O(N<sup>2</sup>) if both have the same length.
	 */
	interface ArrayBuilder
	{
		/**
		 * Append an expected array member, expecting a string value.
		 * @param value the expected value
		 * @return this {@link ArrayBuilder}, for chaining
		 * @exception NullPointerException if {@code value} is {@code null}
		 */
		ArrayBuilder add(String value);

		/**
		 * Append an expected array member, expecting a numeric value.
		 * @param value the expected value
		 * @return this {@link ArrayBuilder}, for chaining
		 * @exception NullPointerException if {@code value} is {@code null}
		 */
		ArrayBuilder add(int value);

		/**
		 * Append an expected array member, expecting a numeric value.
		 * @param value the expected value
		 * @return this {@link ArrayBuilder}, for chaining
		 * @exception NullPointerException if {@code value} is {@code null}
		 */
		ArrayBuilder add(long value);

		/**
		 * Append an expected array member, expecting a numeric value.
		 * @param value the expected value
		 * @return this {@link ArrayBuilder}, for chaining
		 * @exception NullPointerException if {@code value} is {@code null}
		 */
		ArrayBuilder add(double value);

		/**
		 * Append an expected array member, expecting a string value.
		 * @param value the expected value
		 * @return this {@link ArrayBuilder}, for chaining
		 * @exception NullPointerException if {@code value} is {@code null}
		 */
		ArrayBuilder add(BigInteger value);

		/**
		 * Append an expected array member, expecting a numeric value.
		 * @param value the expected value
		 * @return this {@link ArrayBuilder}, for chaining
		 * @exception NullPointerException if {@code value} is {@code null}
		 */
		ArrayBuilder add(BigDecimal value);

		/**
		 * Append an expected array member, expecting a boolean value.
		 * @param value the expected value
		 * @return this {@link ArrayBuilder}, for chaining
		 */
		ArrayBuilder add(boolean value);

		/**
		 * Append an expected array member, expecting a string value.
		 * @param value the expected value
		 * @return this {@link ArrayBuilder}, for chaining
		 * @exception NullPointerException if {@code value} is {@code null}
		 */
		ArrayBuilder add(JsonValue value);

		/**
		 * Append an expected array member, whose value is expected to be null.
		 * @return this {@link ArrayBuilder}, for chaining
		 */
		ArrayBuilder addNull();

		/**
		 * Append an expected array member, expecting a value matched by a {@link JsonMatcher}.
		 * @param valueMatcher a matcher for the expected value
		 * @return this {@link ArrayBuilder}, for chaining
		 * @exception NullPointerException if {@code valueMatcher} is {@code null}
		 */
		ArrayBuilder add(JsonMatcher<?> valueMatcher);

		/**
		 * Append all of the members from a collection. The collection values will be converted to matchers by the
		 * {@link #collectionValue(Object)} factory method.
		 * @param values the collection of values to append
		 * @return this {@link ArrayBuilder}, for chaining
		 * @exception NullPointerException if {@code values} or any element within it is {@code null}
		 * @exception ClassCastException if any value within {@code values} is not of a supported type
		 */
		ArrayBuilder addAll(Collection<?> values);

		/**
		 * Set the expected array member at a particular index, expecting a string value.
		 * @param value the expected value
		 * @return this {@link ArrayBuilder}, for chaining
		 * @exception NullPointerException if {@code value} is {@code null}
		 */
		ArrayBuilder set(int index, String value);

		/**
		 * Set the expected array member at a particular index, expecting a numeric value.
		 * @param value the expected value
		 * @return this {@link ArrayBuilder}, for chaining
		 * @exception NullPointerException if {@code value} is {@code null}
		 */
		ArrayBuilder set(int index, int value);

		/**
		 * Set the expected array member at a particular index, expecting a numeric value.
		 * @param value the expected value
		 * @return this {@link ArrayBuilder}, for chaining
		 * @exception NullPointerException if {@code value} is {@code null}
		 */
		ArrayBuilder set(int index, long value);

		/**
		 * Set the expected array member at a particular index, expecting a numeric value.
		 * @param value the expected value
		 * @return this {@link ArrayBuilder}, for chaining
		 * @exception NullPointerException if {@code value} is {@code null}
		 */
		ArrayBuilder set(int index, double value);

		/**
		 * Set the expected array member at a particular index, expecting a string value.
		 * @param value the expected value
		 * @return this {@link ArrayBuilder}, for chaining
		 * @exception NullPointerException if {@code value} is {@code null}
		 */
		ArrayBuilder set(int index, BigInteger value);

		/**
		 * Set the expected array member at a particular index, replacing the expected member already at that index.
		 * The new expected value is numeric.
		 * @param value the expected value
		 * @return this {@link ArrayBuilder}, for chaining
		 * @exception NullPointerException if {@code value} is {@code null}
		 */
		ArrayBuilder set(int index, BigDecimal value);

		/**
		 * Set the expected array member at a particular index, replacing the expected member already at that index.
		 * The new expected value is a boolean.
		 * @param value the expected value
		 * @return this {@link ArrayBuilder}, for chaining
		 */
		ArrayBuilder set(int index, boolean value);

		/**
		 * Set the expected array member at a particular index, replacing the expected member already at that index.
		 * The new expected value is a string.
		 * @param value the expected value
		 * @return this {@link ArrayBuilder}, for chaining
		 * @exception NullPointerException if {@code value} is {@code null}
		 */
		ArrayBuilder set(int index, JsonValue value);

		/**
		 * Set the expected array member at a particular index, replacing the expected member already at that index.
		 * The new expected value will be matched by a {@link JsonMatcher}.
		 * @param valueMatcher a matcher for the expected value
		 * @return this {@link ArrayBuilder}, for chaining
		 * @exception NullPointerException if {@code valueMatcher} is {@code null}
		 */
		ArrayBuilder set(int index, JsonMatcher<?> valueMatcher);

		/**
		 * Set the expected array member at a particular index, replacing the expected member already there.
		 * The new expected value is null.
		 * @return this {@link ArrayBuilder}, for chaining
		 */
		ArrayBuilder setNull(int index);

		/**
		 * Remove an expectation from this builder. Expected members with later indices are shifted up to fill the space.
		 * @param index the index of the member to remove
		 * @return this {@link ArrayBuilder}, for chaining
		 * @exception IndexOutOfBoundsException if the index is less than 0 or greater than the index of the last element
		 */
		ArrayBuilder remove(int index);

		/**
		 * Reset this builder to contain no expectations. This restores the builder to a freshly-created state.
		 * @return this {@link ArrayBuilder}, for chaining
		 */
		ArrayBuilder reset();

		/**
		 * Duplicate this builder, returning a new builder with the same set of expectations. The original and duplicate are
		 * entirely independent: changes made to one will not affect the other.
		 * @return a new builder initially containing the current set of expectations from this builder
		 */
		ArrayBuilder duplicate();

		/**
		 * Build a matcher which matches arrays containing exactly the expected members from the current state of this builder,
		 * with no additional members, all in the order that they were added.
		 * The matcher returned from this method will not reflect any changes to the builder made after this call.
		 * @return a matcher that matches objects exactly matching this builder's current state
		 */
		JsonMatcher<JsonArray> exact();

		/**
		 * Build a matcher which matches arrays containing exactly the expected members from the current state of this builder,
		 * with no additional members, but allowing any order for the elements in the array.
		 * The matcher returned from this method will not reflect any changes to the builder made after this call.
		 * The returned matcher will apply its expectations in index order - the order they were supplied to the builder by
		 * {@code add}, or the order of the index supplied to {@code set}. Each expectation will be satisfied by the first element
		 * remaining in the array that matches its condition, and this element will be ignored by subsequent matchers. This
		 * matching algorithm ensures that matching is deterministic regardless of the specific expectations. For example, if
		 * {@code builder.add("a").add(ANY_STRING)} were called, the fixed string "a" would be searched for before the ANY_STRING.
		 * If the order of these expectations were reversed, then the ANY_STRING would consume the first string in the array,
		 * even if that is the only instance of the string {@code "a"}. This matching algorithm runs in time proportional to
		 * the product of the number of expectations and the length of the array - that is, O(N<sup>2</sup>) if both are the same
		 * length.
		 * @return a matcher that matches objects exactly matching this builder's current state, ignoring order
		 */
		JsonMatcher<JsonArray> exactIgnoreOrder();	// ignore the ordering of elements in both template and value

		/**
		 * Build a matcher which matches arrays containing all of the expected members from the current state of this builder,
		 * allowing any order for the elements in the array, and allowing additional elements in the array.
		 * The matcher returned from this method will not reflect any changes to the builder made after this call.
		 * The returned matcher will apply its expectations in index order - the order they were supplied to the builder by
		 * {@code add}, or the order of the index supplied to {@code set}. Each expectation will be satisfied by the first element
		 * remaining in the array that matches its condition, and this element will be ignored by subsequent matchers. This
		 * matching algorithm ensures that matching is deterministic regardless of the specific expectations. For example, if
		 * {@code builder.add("a").add(ANY_STRING)} were called, the fixed string "a" would be searched for before the ANY_STRING.
		 * If the order of these expectations were reversed, then the ANY_STRING would consume the first string in the array,
		 * even if that is the only instance of the string {@code "a"}. This matching algorithm runs in time proportional to
		 * the product of the number of expectations and the length of the array - that is, O(N<sup>2</sup>) if both are the same
		 * length.
		 * @return a matcher that matches objects matching all of the expectations in this builder's current state, ignoring
		 *         order, and ignoring any extra elements
		 */
		JsonMatcher<JsonArray> containsIgnoreOrder();
	}
}
