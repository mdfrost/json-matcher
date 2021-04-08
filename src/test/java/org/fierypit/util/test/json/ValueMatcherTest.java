package org.fierypit.util.test.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.junit.jupiter.api.Test;

import static org.fierypit.util.test.json.TestUtil.parseJson;

import static org.junit.jupiter.api.Assertions.*;

class ValueMatcherTest
{
	@Test
	void value_string()
	{
		JsonMatcher<JsonString> matcher = JsonMatcher.value("test string");
		assertTrue(matcher.test(Json.createValue("test string")));
		assertFalse(matcher.test(Json.createValue("test string2")));
		assertFalse(matcher.test(JsonValue.NULL));
		assertEquals("\"test string\"", matcher.toString());
	}

	@Test
	void value_int()
	{
		JsonMatcher<JsonNumber> matcher = JsonMatcher.value(123);
		assertTrue(matcher.test(Json.createValue(123)));
		assertFalse(matcher.test(Json.createValue(124)));
		assertFalse(matcher.test(JsonValue.NULL));
		assertEquals("123", matcher.toString());
	}

	@Test
	void value_long()
	{
		JsonMatcher<JsonNumber> matcher = JsonMatcher.value(9876543210L);
		assertTrue(matcher.test(Json.createValue(9876543210L)));
		assertFalse(matcher.test(Json.createValue(9876543211L)));
		assertFalse(matcher.test(JsonValue.NULL));
		assertEquals("9876543210", matcher.toString());
	}

	@Test
	void value_double()
	{
		JsonMatcher<JsonNumber> matcher = JsonMatcher.value(123.456);
		assertTrue(matcher.test(Json.createValue(123.456)));
		assertFalse(matcher.test(Json.createValue(123.457)));
		assertFalse(matcher.test(JsonValue.NULL));
		assertEquals("123.456", matcher.toString());
	}

	@Test
	void value_BigInteger()
	{
		JsonMatcher<JsonNumber> matcher = JsonMatcher.value(new BigInteger("98765432109876543210"));
		assertTrue(matcher.test(Json.createValue(new BigInteger("98765432109876543210"))));
		assertFalse(matcher.test(Json.createValue(new BigInteger("98765432109876543211"))));
		assertFalse(matcher.test(JsonValue.NULL));
		assertEquals("98765432109876543210", matcher.toString());
	}

	@Test
	void value_BigDecimal()
	{
		JsonMatcher<JsonNumber> matcher = JsonMatcher.value(new BigDecimal("98765432109876543210.123456789"));
		assertTrue(matcher.test(Json.createValue(new BigDecimal("98765432109876543210.123456789"))));
		assertFalse(matcher.test(Json.createValue(new BigDecimal("98765432109876543210.1234567891"))));
		assertFalse(matcher.test(JsonValue.NULL));
		assertEquals("98765432109876543210.123456789", matcher.toString());
	}

	@Test
	void value_boolean()
	{
		// Just test that the value(boolean) method returns the correct flyweight matcher.
		// The actual matcher behaviour is tested by staticMatchers().

		assertSame(JsonMatcher.TRUE, JsonMatcher.value(true));
		assertSame(JsonMatcher.FALSE, JsonMatcher.value(false));
	}

	@Test
	void value_JsonValue_fixed()
	{
		// Just test that the value(JsonValue) method returns the correct flyweight matcher.
		// The actual matcher behaviour is tested by staticMatchers().

		assertSame(JsonMatcher.TRUE, JsonMatcher.value(JsonValue.TRUE));
		assertSame(JsonMatcher.FALSE, JsonMatcher.value(JsonValue.FALSE));
		assertSame(JsonMatcher.NULL, JsonMatcher.value(JsonValue.NULL));
	}

	@Test
	void value_JsonValue_object()
	{
		JsonObject jsonObject = Json.createObjectBuilder()
				.add("a", 123)
				.add("b", true)
				.build();

		JsonMatcher<JsonObject> matcher = JsonMatcher.value(jsonObject);

		assertTrue(matcher.test(parseJson("{\"a\":123,\"b\":true}")));
		assertFalse(matcher.test(parseJson("{\"a\":123,\"b\":true,\"c\":\"test\"}")));
		assertFalse(matcher.test(parseJson("[\"a\",123,true]")));
		assertFalse(matcher.test(JsonValue.NULL));
		// Although our matchers sort keys in objects when converting to strings, JsonObject does not do this, so we must compare
		// against the actual string representation of the JSON object, rather than a fixed string.
		assertEquals(jsonObject.toString(), matcher.toString());
	}

	@Test
	void value_JsonValue_array()
	{
		JsonMatcher<JsonArray> matcher = JsonMatcher.value(
			Json.createArrayBuilder()
				.add("a")
				.add(123)
				.add(true)
				.build()
		);

		assertTrue(matcher.test(parseJson("[\"a\",123,true]")));
		assertFalse(matcher.test(parseJson("[\"a\",true,123]")));
		assertFalse(matcher.test(parseJson("{\"a\":123,\"b\":true}")));
		assertFalse(matcher.test(JsonValue.NULL));
		assertEquals("[\"a\",123,true]", matcher.toString());
	}

	@Test
	void staticMatchers()
	{
		assertTrue(JsonMatcher.TRUE.test(JsonValue.TRUE));
		assertFalse(JsonMatcher.TRUE.test(JsonValue.FALSE));
		assertFalse(JsonMatcher.TRUE.test(parseJson("{\"a\":123,\"b\":true}")));
		assertFalse(JsonMatcher.TRUE.test(JsonValue.NULL));
		assertEquals("true", JsonMatcher.TRUE.toString());

		assertTrue(JsonMatcher.FALSE.test(JsonValue.FALSE));
		assertFalse(JsonMatcher.FALSE.test(JsonValue.TRUE));
		assertFalse(JsonMatcher.FALSE.test(parseJson("{\"a\":123,\"b\":true}")));
		assertFalse(JsonMatcher.FALSE.test(JsonValue.NULL));
		assertEquals("false", JsonMatcher.FALSE.toString());

		assertTrue(JsonMatcher.NULL.test(JsonValue.NULL));
		assertFalse(JsonMatcher.NULL.test(Json.createValue(123)));
		assertFalse(JsonMatcher.NULL.test(parseJson("{\"a\":123,\"b\":true}")));
		assertFalse(JsonMatcher.NULL.test(Json.createValue("test")));
		assertEquals("null", JsonMatcher.NULL.toString());
	}

	@Test
	void anyMatchers()
	{
		assertTrue(JsonMatcher.ANY_VALUE.test(Json.createValue("test")));
		assertTrue(JsonMatcher.ANY_VALUE.test(Json.createValue(123)));
		assertTrue(JsonMatcher.ANY_VALUE.test(Json.createValue(9876543210L)));
		assertTrue(JsonMatcher.ANY_VALUE.test(Json.createValue(123.456)));
		assertTrue(JsonMatcher.ANY_VALUE.test(Json.createValue(new BigInteger("98765432109876543210"))));
		assertTrue(JsonMatcher.ANY_VALUE.test(Json.createValue(new BigDecimal("98765432109876543210.123456789"))));
		assertTrue(JsonMatcher.ANY_VALUE.test(JsonValue.TRUE));
		assertTrue(JsonMatcher.ANY_VALUE.test(JsonValue.FALSE));
		assertTrue(JsonMatcher.ANY_VALUE.test(JsonValue.NULL));
		assertTrue(JsonMatcher.ANY_VALUE.test(JsonValue.EMPTY_JSON_OBJECT));
		assertTrue(JsonMatcher.ANY_VALUE.test(JsonValue.EMPTY_JSON_ARRAY));
		assertEquals("(any value)", JsonMatcher.ANY_VALUE.toString());

		assertFalse(JsonMatcher.ANY_NUMBER.test(Json.createValue("test")));
		assertTrue(JsonMatcher.ANY_NUMBER.test(Json.createValue(123)));
		assertTrue(JsonMatcher.ANY_NUMBER.test(Json.createValue(9876543210L)));
		assertTrue(JsonMatcher.ANY_NUMBER.test(Json.createValue(123.456)));
		assertTrue(JsonMatcher.ANY_NUMBER.test(Json.createValue(new BigInteger("98765432109876543210"))));
		assertTrue(JsonMatcher.ANY_NUMBER.test(Json.createValue(new BigDecimal("98765432109876543210.123456789"))));
		assertFalse(JsonMatcher.ANY_NUMBER.test(JsonValue.TRUE));
		assertFalse(JsonMatcher.ANY_NUMBER.test(JsonValue.FALSE));
		assertFalse(JsonMatcher.ANY_NUMBER.test(JsonValue.NULL));
		assertFalse(JsonMatcher.ANY_NUMBER.test(JsonValue.EMPTY_JSON_OBJECT));
		assertFalse(JsonMatcher.ANY_NUMBER.test(JsonValue.EMPTY_JSON_ARRAY));
		assertEquals("(any number)", JsonMatcher.ANY_NUMBER.toString());

		assertTrue(JsonMatcher.ANY_STRING.test(Json.createValue("test")));
		assertFalse(JsonMatcher.ANY_STRING.test(Json.createValue(123)));
		assertFalse(JsonMatcher.ANY_STRING.test(Json.createValue(9876543210L)));
		assertFalse(JsonMatcher.ANY_STRING.test(Json.createValue(123.456)));
		assertFalse(JsonMatcher.ANY_STRING.test(Json.createValue(new BigInteger("98765432109876543210"))));
		assertFalse(JsonMatcher.ANY_STRING.test(Json.createValue(new BigDecimal("98765432109876543210.123456789"))));
		assertFalse(JsonMatcher.ANY_STRING.test(JsonValue.TRUE));
		assertFalse(JsonMatcher.ANY_STRING.test(JsonValue.FALSE));
		assertFalse(JsonMatcher.ANY_STRING.test(JsonValue.NULL));
		assertFalse(JsonMatcher.ANY_STRING.test(JsonValue.EMPTY_JSON_OBJECT));
		assertFalse(JsonMatcher.ANY_STRING.test(JsonValue.EMPTY_JSON_ARRAY));
		assertEquals("(any string)", JsonMatcher.ANY_STRING.toString());

		assertFalse(JsonMatcher.ANY_BOOLEAN.test(Json.createValue("test")));
		assertFalse(JsonMatcher.ANY_BOOLEAN.test(Json.createValue(123)));
		assertFalse(JsonMatcher.ANY_BOOLEAN.test(Json.createValue(9876543210L)));
		assertFalse(JsonMatcher.ANY_BOOLEAN.test(Json.createValue(123.456)));
		assertFalse(JsonMatcher.ANY_BOOLEAN.test(Json.createValue(new BigInteger("98765432109876543210"))));
		assertFalse(JsonMatcher.ANY_BOOLEAN.test(Json.createValue(new BigDecimal("98765432109876543210.123456789"))));
		assertTrue(JsonMatcher.ANY_BOOLEAN.test(JsonValue.TRUE));
		assertTrue(JsonMatcher.ANY_BOOLEAN.test(JsonValue.FALSE));
		assertFalse(JsonMatcher.ANY_BOOLEAN.test(JsonValue.NULL));
		assertFalse(JsonMatcher.ANY_BOOLEAN.test(JsonValue.EMPTY_JSON_OBJECT));
		assertFalse(JsonMatcher.ANY_BOOLEAN.test(JsonValue.EMPTY_JSON_ARRAY));
		assertEquals("(any boolean)", JsonMatcher.ANY_BOOLEAN.toString());

		assertFalse(JsonMatcher.ANY_OBJECT.test(Json.createValue("test")));
		assertFalse(JsonMatcher.ANY_OBJECT.test(Json.createValue(123)));
		assertFalse(JsonMatcher.ANY_OBJECT.test(Json.createValue(9876543210L)));
		assertFalse(JsonMatcher.ANY_OBJECT.test(Json.createValue(123.456)));
		assertFalse(JsonMatcher.ANY_OBJECT.test(Json.createValue(new BigInteger("98765432109876543210"))));
		assertFalse(JsonMatcher.ANY_OBJECT.test(Json.createValue(new BigDecimal("98765432109876543210.123456789"))));
		assertFalse(JsonMatcher.ANY_OBJECT.test(JsonValue.TRUE));
		assertFalse(JsonMatcher.ANY_OBJECT.test(JsonValue.FALSE));
		assertFalse(JsonMatcher.ANY_OBJECT.test(JsonValue.NULL));
		assertTrue(JsonMatcher.ANY_OBJECT.test(JsonValue.EMPTY_JSON_OBJECT));
		assertFalse(JsonMatcher.ANY_OBJECT.test(JsonValue.EMPTY_JSON_ARRAY));
		assertEquals("(any object)", JsonMatcher.ANY_OBJECT.toString());

		assertFalse(JsonMatcher.ANY_ARRAY.test(Json.createValue("test")));
		assertFalse(JsonMatcher.ANY_ARRAY.test(Json.createValue(123)));
		assertFalse(JsonMatcher.ANY_ARRAY.test(Json.createValue(9876543210L)));
		assertFalse(JsonMatcher.ANY_ARRAY.test(Json.createValue(123.456)));
		assertFalse(JsonMatcher.ANY_ARRAY.test(Json.createValue(new BigInteger("98765432109876543210"))));
		assertFalse(JsonMatcher.ANY_ARRAY.test(Json.createValue(new BigDecimal("98765432109876543210.123456789"))));
		assertFalse(JsonMatcher.ANY_ARRAY.test(JsonValue.TRUE));
		assertFalse(JsonMatcher.ANY_ARRAY.test(JsonValue.FALSE));
		assertFalse(JsonMatcher.ANY_ARRAY.test(JsonValue.NULL));
		assertFalse(JsonMatcher.ANY_ARRAY.test(JsonValue.EMPTY_JSON_OBJECT));
		assertTrue(JsonMatcher.ANY_ARRAY.test(JsonValue.EMPTY_JSON_ARRAY));
		assertEquals("(any array)", JsonMatcher.ANY_ARRAY.toString());
	}

	@Test
	void collectionValue_fail_null()
	{
		assertThrows(NullPointerException.class, () -> JsonMatcher.collectionValue(null));
	}

	@Test
	void collectionValue_fail_mapKeyNull()
	{
		Map<String,Object> map = new HashMap<>();
		map.put(null, "value");
		assertThrows(NullPointerException.class, () -> JsonMatcher.collectionValue(map));
	}

	@Test
	void collectionValue_fail_mapValueNull()
	{
		Map<String,Object> map = new HashMap<>();
		map.put("key", null);
		assertThrows(NullPointerException.class, () -> JsonMatcher.collectionValue(map));
	}

	@Test
	void collectionValue_fail_invalidType()
	{
		assertThrows(ClassCastException.class, () -> JsonMatcher.collectionValue(new Object()));
	}

	@Test
	void collectionValue_fail_mapKeyInvalidType()
	{
		Map<?,?> map = Map.of(123, "value");
		assertThrows(ClassCastException.class, () -> JsonMatcher.collectionValue(map));
	}

	@Test
	void collectionValue_fail_mapValueInvalidType()
	{
		Map<?,?> map = Map.of("key", (byte) 0);
		assertThrows(ClassCastException.class, () -> JsonMatcher.collectionValue(map));
	}

}