package org.fierypit.util.test.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
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

// These tests test both the builder and the matcher together.

class ArrayMatcherTest
{
	// Custom matcher matching any JSON string containing at least one character.
	private static final JsonMatcher<JsonString> TEST_MATCHER_NON_EMPTY_STRING =
			value -> value instanceof JsonString jsonString && jsonString.getString().length() > 0;

	// Custom matcher matching any JSON number greater than zero.
	private static final JsonMatcher<JsonNumber> TEST_MATCHER_POSITIVE_NUMBER =
			value -> value instanceof JsonNumber jsonNumber && jsonNumber.bigDecimalValue().signum() > 0;

	// This test just ensures that the custom matchers above actually work as expected!
	@Test
	void testCustomMatchers()
	{
		assertTrue(TEST_MATCHER_NON_EMPTY_STRING.test(Json.createValue("a")));
		assertFalse(TEST_MATCHER_NON_EMPTY_STRING.test(Json.createValue("")));
		assertFalse(TEST_MATCHER_NON_EMPTY_STRING.test(Json.createValue(123)));

		assertTrue(TEST_MATCHER_POSITIVE_NUMBER.test(Json.createValue(1)));
		assertTrue(TEST_MATCHER_POSITIVE_NUMBER.test(Json.createValue(new BigDecimal("9999999999999999999999999.999999999"))));
		assertFalse(TEST_MATCHER_POSITIVE_NUMBER.test(Json.createValue(0.0)));
		assertFalse(TEST_MATCHER_POSITIVE_NUMBER.test(Json.createValue(BigInteger.ONE.negate())));
		assertFalse(TEST_MATCHER_POSITIVE_NUMBER.test(Json.createValue(-9999999999L)));
		assertFalse(TEST_MATCHER_POSITIVE_NUMBER.test(Json.createValue("abc")));
	}

	@Test
	void arrayMatcher_empty_exact()
	{
		JsonMatcher<JsonArray> matcher = JsonMatcher.array().exact();

		assertTrue(matcher.test(parseJson("[]")), "empty array should match");
		assertFalse(matcher.test(parseJson("[123]")), "non-empty array should not match for exact");
		assertFalse(matcher.test(parseJson("{}")), "object should not match");
		assertFalse(matcher.test(JsonValue.NULL), "null should not match");

		assertEquals("(exact)[]", matcher.toString());
	}

	@Test
	void arrayMatcher_empty_exactIgnoreOrder()
	{
		JsonMatcher<JsonArray> matcher = JsonMatcher.array().exactIgnoreOrder();

		assertTrue(matcher.test(parseJson("[]")), "empty array should match");
		assertFalse(matcher.test(parseJson("[123]")), "non-empty array should not match for exactIgnoreOrder");
		assertFalse(matcher.test(parseJson("{}")), "object should not match");
		assertFalse(matcher.test(JsonValue.NULL), "null should not match");

		assertEquals("(exactIgnoreOrder)[]", matcher.toString());
	}

	@Test
	void arrayMatcher_empty_containsIgnoreOrder()
	{
		JsonMatcher<JsonArray> matcher = JsonMatcher.array().containsIgnoreOrder();

		assertTrue(matcher.test(parseJson("[]")), "empty array should match");
		assertTrue(matcher.test(parseJson("[123]")), "non-empty array should match for containsIgnoreOrder");
		assertFalse(matcher.test(parseJson("{}")), "object should not match");
		assertFalse(matcher.test(JsonValue.NULL), "null should not match");

		assertEquals("(containsIgnoreOrder)[]", matcher.toString());
	}

	@Test
	void arrayMatcher_simple_exact()
	{
		JsonMatcher<JsonArray> matcher = JsonMatcher.array()
				.add("test")
				.add(123)
				.add(9876543210L)
				.add(123.456)
				.add(new BigInteger("98765432109876543210"))
				.add(new BigDecimal("98765432109876543210.123456789"))
				.add(true)
				.addNull()
				.exact();

		assertEquals("""
				(exact)["test",123,9876543210,123.456,98765432109876543210,98765432109876543210.123456789,true,null]\
				""", matcher.toString());

		assertTrue(matcher.test(parseJson("""
				[
					"test",
					123,
					9876543210,
					123.456,
					98765432109876543210,
					98765432109876543210.123456789,
					true,
					null
				]
				""")), "exact same array should match");

		assertFalse(matcher.test(parseJson("""
				[
					"test",
					123,
					9876543210,
					123.456,
					98765432109876543210.123456789,
					98765432109876543210,
					true,
					null
				]
				""")), "right elements but one out of order: should not match for exact");

		assertFalse(matcher.test(parseJson("""
				[
					"test",
					123,
					9876543210,
					123.456,
					98765432109876543210,
					98765432109876543210.123456789,
					true
				]
				""")), "one member missing");

		assertFalse(matcher.test(parseJson("""
				[
					"test",
					123,
					9876543210,
					123.456,
					98765432109876543210,
					98765432109876543211.123456789,
					true,
					null
				]
				""")), "one member wrong value");

		// This match should fail for exact and exactIgnoreOrder matches, whereas it should succeed for containsIgnoreOrder match.
		// This match has the extra member at the end, so the ordering of other members is not affected.
		assertFalse(matcher.test(parseJson("""
				[
					"test",
					123,
					9876543210,
					123.456,
					98765432109876543210,
					98765432109876543210.123456789,
					true,
					null,
					"extra string"
				]
				""")), "extra member (not valid for exact match)");

		// This match should fail for exact and exactIgnoreOrder matches, whereas it should succeed for containsIgnoreOrder match.
		// This match has the extra member in the middle, so also affects member order.
		assertFalse(matcher.test(parseJson("""
				[
					"test",
					123,
					9876543210,
					"extra string",
					123.456,
					98765432109876543210,
					98765432109876543210.123456789,
					true,
					null
				]
				""")), "extra member (not valid for exact match)");
	}

	@Test
	void arrayMatcher_simple_exactIgnoreOrder()
	{
		JsonMatcher<JsonArray> matcher = JsonMatcher.array()
				.add("test")
				.add(123)
				.add(9876543210L)
				.add(123.456)
				.add(new BigInteger("98765432109876543210"))
				.add(new BigDecimal("98765432109876543210.123456789"))
				.add(true)
				.addNull()
				.exactIgnoreOrder();

		assertEquals("""
				(exactIgnoreOrder)["test",123,9876543210,123.456,98765432109876543210,98765432109876543210.123456789,true,null]\
				""", matcher.toString());

		assertTrue(matcher.test(parseJson("""
				[
					"test",
					123,
					9876543210,
					123.456,
					98765432109876543210,
					98765432109876543210.123456789,
					true,
					null
				]
				""")), "exact same array should match");

		assertTrue(matcher.test(parseJson("""
				[
					"test",
					123,
					9876543210,
					123.456,
					98765432109876543210.123456789,
					98765432109876543210,
					true,
					null
				]
				""")), "right elements but one out of order: should match for exactIgnoreOrder");

		assertTrue(matcher.test(parseJson("""
				[
					null,
					true,
					98765432109876543210,
					98765432109876543210.123456789,
					123.456,
					9876543210,
					123,
					"test"
				]
				""")), "right elements in reverse order: should match for exactIgnoreOrder");

		assertFalse(matcher.test(parseJson("""
				[
					"test",
					123,
					9876543210,
					123.456,
					98765432109876543210,
					98765432109876543210.123456789,
					true
				]
				""")), "one member missing");

		assertFalse(matcher.test(parseJson("""
				[
					"test",
					123,
					9876543210,
					123.456,
					98765432109876543210,
					98765432109876543211.123456789,
					true,
					null
				]
				""")), "one member wrong value");

		// This match should fail for exact and exactIgnoreOrder matches, whereas it should succeed for containsIgnoreOrder match.
		// This match has the extra member at the end, so the ordering of other members is not affected.
		assertFalse(matcher.test(parseJson("""
				[
					"test",
					123,
					9876543210,
					123.456,
					98765432109876543210,
					98765432109876543210.123456789,
					true,
					null,
					"extra string"
				]
				""")), "extra member (not valid for exactIgnoreOrder match)");

		// This match should fail for exact and exactIgnoreOrder matches, whereas it should succeed for containsIgnoreOrder match.
		// This match has the extra member in the middle, so also affects member order.
		assertFalse(matcher.test(parseJson("""
				[
					"test",
					123,
					9876543210,
					"extra string",
					123.456,
					98765432109876543210,
					98765432109876543210.123456789,
					true,
					null
				]
				""")), "extra member (not valid for exactIgnoreOrder match)");
	}

	@Test
	void arrayMatcher_simple_containsIgnoreOrder()
	{
		JsonMatcher<JsonArray> matcher = JsonMatcher.array()
				.add("test")
				.add(123)
				.add(9876543210L)
				.add(123.456)
				.add(new BigInteger("98765432109876543210"))
				.add(new BigDecimal("98765432109876543210.123456789"))
				.add(true)
				.addNull()
				.containsIgnoreOrder();

		assertEquals("""
				(containsIgnoreOrder)["test",123,9876543210,123.456,98765432109876543210,98765432109876543210.123456789,true,null]\
				""", matcher.toString());

		assertTrue(matcher.test(parseJson("""
				[
					"test",
					123,
					9876543210,
					123.456,
					98765432109876543210,
					98765432109876543210.123456789,
					true,
					null
				]
				""")), "exact same array should match");

		assertTrue(matcher.test(parseJson("""
				[
					"test",
					123,
					9876543210,
					123.456,
					98765432109876543210.123456789,
					98765432109876543210,
					true,
					null
				]
				""")), "right elements but one out of order: should match for containsIgnoreOrder");

		assertTrue(matcher.test(parseJson("""
				[
					null,
					true,
					98765432109876543210,
					98765432109876543210.123456789,
					123.456,
					9876543210,
					123,
					"test"
				]
				""")), "right elements in reverse order: should match for containsIgnoreOrder");

		assertFalse(matcher.test(parseJson("""
				[
					"test",
					123,
					9876543210,
					123.456,
					98765432109876543210,
					98765432109876543210.123456789,
					true
				]
				""")), "one member missing");

		assertFalse(matcher.test(parseJson("""
				[
					"test",
					123,
					9876543210,
					123.456,
					98765432109876543210,
					98765432109876543211.123456789,
					true,
					null
				]
				""")), "one member wrong value");

		// This match should fail for exact and exactIgnoreOrder matches, whereas it should succeed for containsIgnoreOrder match.
		// This match has the extra member at the end, so the ordering of other members is not affected.
		assertTrue(matcher.test(parseJson("""
				[
					"test",
					123,
					9876543210,
					123.456,
					98765432109876543210,
					98765432109876543210.123456789,
					true,
					null,
					"extra string"
				]
				""")), "extra member (permitted for containsIgnoreOrder match)");

		// This match should fail for exact and exactIgnoreOrder matches, whereas it should succeed for containsIgnoreOrder match.
		// This match has the extra member in the middle, so also affects member order.
		assertTrue(matcher.test(parseJson("""
				[
					"test",
					123,
					9876543210,
					"extra string",
					123.456,
					98765432109876543210,
					98765432109876543210.123456789,
					true,
					null
				]
				""")), "extra member (permitted for containsIgnoreOrder match)");
	}

	@Test
	void arrayMatcher_addAll()
	{
		// Custom matcher used to check that such things are accepted.
		JsonMatcher<?> customMatcher = new JsonMatcher<>() {
			@Override public boolean test(JsonValue value) { return true; }
			@Override public String toString() { return "(CUSTOM)"; }
		};

		// We use a limited value for the Float element, that we know can be represented exactly in single precision.

		JsonMatcher<JsonArray> matcher = JsonMatcher.array().addAll(List.of(
				"test",
				123,
				9876543210L,
				1.25f,
				123.456,
				new BigInteger("98765432109876543210"),
				new BigDecimal("98765432109876543210.123456789"),
				Boolean.TRUE,
				Boolean.FALSE,
				JsonValue.NULL,
				Map.of(
						"String", "test",
						"Integer", 123,
						"Long", 9876543210L,
						"Float", 1.25f,
						"Double", 123.456d,
						"BigInteger", new BigInteger("98765432109876543210"),
						"BigDecimal", new BigDecimal("98765432109876543210.123456789"),
						"true", Boolean.TRUE,
						"false", Boolean.FALSE,
						"null", JsonValue.NULL
				),
				List.of(
						"test",
						123,
						9876543210L,
						1.25f,
						123.456d,
						new BigInteger("98765432109876543210"),
						new BigDecimal("98765432109876543210.123456789"),
						Boolean.TRUE,
						Boolean.FALSE,
						JsonValue.NULL
				),
				customMatcher,
				JsonValue.EMPTY_JSON_OBJECT,
				JsonValue.EMPTY_JSON_ARRAY,
				JsonValue.TRUE,
				JsonValue.FALSE
		)).exact();

		// Rather than trying to actually test this matcher by matching values, we just check its string representation.
		// Note that object keys are sorted within the matcher constructed from the nested Map, but array order is unchanged.

		assertEquals("""
				(exact)[\
				"test",\
				123,\
				9876543210,\
				1.25,\
				123.456,\
				98765432109876543210,\
				98765432109876543210.123456789,\
				true,\
				false,\
				null,\
				(exact){"BigDecimal":98765432109876543210.123456789,"BigInteger":98765432109876543210,"Double":123.456,"Float":1.25,"Integer":123,"Long":9876543210,"String":"test","false":false,"null":null,"true":true},\
				(exact)["test",123,9876543210,1.25,123.456,98765432109876543210,98765432109876543210.123456789,true,false,null],\
				(CUSTOM),\
				{},\
				[],\
				true,\
				false]\
				""", matcher.toString());
	}

	@Test
	void arrayMatcher_nested_exact()
	{
		JsonMatcher<JsonArray> matcher = JsonMatcher.array()
				.add(1)
				.add(JsonMatcher.array()
						.add("nestedArrayExact")
						.add(false)
						.exact())
				.add(JsonMatcher.array()
						.add("nestedArrayExactIgnoreOrder")
						.add(2)
						.exactIgnoreOrder())
				.add(JsonMatcher.array()
						.add("nestedArrayContainsIgnoreOrder")
						.add(3)
						.containsIgnoreOrder())
				.add(Json.createArrayBuilder()
						.add("nestedArrayAsValue")
						.add(4)
						.build())
				.exact();

		assertEquals("""
				(exact)[1,\
				(exact)["nestedArrayExact",false],\
				(exactIgnoreOrder)["nestedArrayExactIgnoreOrder",2],\
				(containsIgnoreOrder)["nestedArrayContainsIgnoreOrder",3],\
				["nestedArrayAsValue",4]]\
				""", matcher.toString());

		assertTrue(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					[ "nestedArrayExactIgnoreOrder", 2 ],
					[ "nestedArrayContainsIgnoreOrder", 3 ],
					[ "nestedArrayAsValue", 4 ]
				]
				""")), "array should match exactly");

		assertFalse(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					[ "nestedArrayExactIgnoreOrder", 2 ],
					[ "nestedArrayAsValue", 4 ],
					[ "nestedArrayContainsIgnoreOrder", 3 ]
				]
				""")), "two elements swapped: should not match for exact");

		assertFalse(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					"EXTRA 1",
					[ "nestedArrayExactIgnoreOrder", 2 ],
					"EXTRA 2",
					[ "nestedArrayContainsIgnoreOrder", 3 ],
					"EXTRA 3",
					[ "nestedArrayAsValue", 4 ],
					"EXTRA 4"
				]
				""")), "extra elements: should not match for exact");

		assertFalse(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", true ],
					[ "nestedArrayExactIgnoreOrder", 2 ],
					[ "nestedArrayContainsIgnoreOrder", 3 ],
					[ "nestedArrayAsValue", 4 ]
				]
				""")), "nestedArrayExact has incorrect member value (index 1)");

		assertFalse(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					[ "nestedArrayExactIgnoreOrder", -2 ],
					[ "nestedArrayContainsIgnoreOrder", 3 ],
					[ "nestedArrayAsValue", 4 ]
				]
				""")), "nestedArrayExactIgnoreOrder has incorrect member value (index 1)");

		assertTrue(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					[ 2, "nestedArrayExactIgnoreOrder" ],
					[ "nestedArrayContainsIgnoreOrder", 3 ],
					[ "nestedArrayAsValue", 4 ]
				]
				""")), "nestedArrayExactIgnoreOrder has the expected members in a different order (ok)");

		assertFalse(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					[ "nestedArrayExactIgnoreOrder", 2 ],
					[ "nestedArrayContainsIgnoreOrder", -3 ],
					[ "nestedArrayAsValue", 4 ]
				]
				""")), "nestedArrayContainsIgnoreOrder has incorrect member value (index 1)");

		assertTrue(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					[ "nestedArrayExactIgnoreOrder", 2 ],
					[ 3, "nestedArrayContainsIgnoreOrder" ],
					[ "nestedArrayAsValue", 4 ]
				]
				""")), "nestedArrayContainsIgnoreOrder has the expected members in a different order (ok)");

		assertTrue(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					[ "nestedArrayExactIgnoreOrder", 2 ],
					[ "***EXTRA***", "nestedArrayContainsIgnoreOrder", 3 ],
					[ "nestedArrayAsValue", 4 ]
				]
				""")), "nestedArrayContainsIgnoreOrder has the expected members plus an extra one (ok)");
	}

	@Test
	void arrayMatcher_nested_exactIgnoreOrder()
	{
		JsonMatcher<JsonArray> matcher = JsonMatcher.array()
				.add(1)
				.add(JsonMatcher.array()
						.add("nestedArrayExact")
						.add(false)
						.exact())
				.add(JsonMatcher.array()
						.add("nestedArrayExactIgnoreOrder")
						.add(2)
						.exactIgnoreOrder())
				.add(JsonMatcher.array()
						.add("nestedArrayContainsIgnoreOrder")
						.add(3)
						.containsIgnoreOrder())
				.add(Json.createArrayBuilder()
						.add("nestedArrayAsValue")
						.add(4)
						.build())
				.exactIgnoreOrder();

		assertEquals("""
				(exactIgnoreOrder)[1,\
				(exact)["nestedArrayExact",false],\
				(exactIgnoreOrder)["nestedArrayExactIgnoreOrder",2],\
				(containsIgnoreOrder)["nestedArrayContainsIgnoreOrder",3],\
				["nestedArrayAsValue",4]]\
				""", matcher.toString());

		assertTrue(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					[ "nestedArrayExactIgnoreOrder", 2 ],
					[ "nestedArrayContainsIgnoreOrder", 3 ],
					[ "nestedArrayAsValue", 4 ]
				]
				""")), "exact array should match");

		assertTrue(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					[ "nestedArrayExactIgnoreOrder", 2 ],
					[ "nestedArrayAsValue", 4 ],
					[ "nestedArrayContainsIgnoreOrder", 3 ]
				]
				""")), "two elements swapped: should not match for exactIgnoreOrder");

		assertFalse(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					"EXTRA 1",
					[ "nestedArrayExactIgnoreOrder", 2 ],
					"EXTRA 2",
					[ "nestedArrayContainsIgnoreOrder", 3 ],
					"EXTRA 3",
					[ "nestedArrayAsValue", 4 ],
					"EXTRA 4"
				]
				""")), "extra elements: not valid for exactIgnoreOrder");

		assertFalse(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", true ],
					[ "nestedArrayExactIgnoreOrder", 2 ],
					[ "nestedArrayContainsIgnoreOrder", 3 ],
					[ "nestedArrayAsValue", 4 ]
				]
				""")), "nestedArrayExact has incorrect member value (index 1)");

		assertFalse(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					[ "nestedArrayExactIgnoreOrder", -2 ],
					[ "nestedArrayContainsIgnoreOrder", 3 ],
					[ "nestedArrayAsValue", 4 ]
				]
				""")), "nestedArrayExactIgnoreOrder has incorrect member value (index 1)");

		assertTrue(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					[ 2, "nestedArrayExactIgnoreOrder" ],
					[ "nestedArrayContainsIgnoreOrder", 3 ],
					[ "nestedArrayAsValue", 4 ]
				]
				""")), "nestedArrayExactIgnoreOrder has the expected members in a different order (ok)");

		assertFalse(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					[ "nestedArrayExactIgnoreOrder", 2 ],
					[ "nestedArrayContainsIgnoreOrder", -3 ],
					[ "nestedArrayAsValue", 4 ]
				]
				""")), "nestedArrayContainsIgnoreOrder has incorrect member value (index 1)");

		assertTrue(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					[ "nestedArrayExactIgnoreOrder", 2 ],
					[ 3, "nestedArrayContainsIgnoreOrder" ],
					[ "nestedArrayAsValue", 4 ]
				]
				""")), "nestedArrayContainsIgnoreOrder has the expected members in a different order (ok)");

		assertTrue(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					[ "nestedArrayExactIgnoreOrder", 2 ],
					[ "***EXTRA***", "nestedArrayContainsIgnoreOrder", 3 ],
					[ "nestedArrayAsValue", 4 ]
				]
				""")), "nestedArrayContainsIgnoreOrder has the expected members plus an extra one (ok)");
	}

	@Test
	void arrayMatcher_nested_containsIgnoreOrder()
	{
		JsonMatcher<JsonArray> matcher = JsonMatcher.array()
				.add(1)
				.add(JsonMatcher.array()
						.add("nestedArrayExact")
						.add(false)
						.exact())
				.add(JsonMatcher.array()
						.add("nestedArrayExactIgnoreOrder")
						.add(2)
						.exactIgnoreOrder())
				.add(JsonMatcher.array()
						.add("nestedArrayContainsIgnoreOrder")
						.add(3)
						.containsIgnoreOrder())
				.add(Json.createArrayBuilder()
						.add("nestedArrayAsValue")
						.add(4)
						.build())
				.containsIgnoreOrder();

		assertEquals("""
				(containsIgnoreOrder)[1,\
				(exact)["nestedArrayExact",false],\
				(exactIgnoreOrder)["nestedArrayExactIgnoreOrder",2],\
				(containsIgnoreOrder)["nestedArrayContainsIgnoreOrder",3],\
				["nestedArrayAsValue",4]]\
				""", matcher.toString());

		assertTrue(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					[ "nestedArrayExactIgnoreOrder", 2 ],
					[ "nestedArrayContainsIgnoreOrder", 3 ],
					[ "nestedArrayAsValue", 4 ]
				]
				""")), "exact array should match");

		assertTrue(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					[ "nestedArrayExactIgnoreOrder", 2 ],
					[ "nestedArrayAsValue", 4 ],
					[ "nestedArrayContainsIgnoreOrder", 3 ]
				]
				""")), "two elements swapped: should be valid for containsIgnoreOrder");

		assertTrue(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					"EXTRA 1",
					[ "nestedArrayExactIgnoreOrder", 2 ],
					"EXTRA 2",
					[ "nestedArrayContainsIgnoreOrder", 3 ],
					"EXTRA 3",
					[ "nestedArrayAsValue", 4 ],
					"EXTRA 4"
				]
				""")), "extra elements: should be valid for containsIgnoreOrder");

		assertFalse(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", true ],
					[ "nestedArrayExactIgnoreOrder", 2 ],
					[ "nestedArrayContainsIgnoreOrder", 3 ],
					[ "nestedArrayAsValue", 4 ]
				]
				""")), "nestedArrayExact has incorrect member value (index 1)");

		assertFalse(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					[ "nestedArrayExactIgnoreOrder", -2 ],
					[ "nestedArrayContainsIgnoreOrder", 3 ],
					[ "nestedArrayAsValue", 4 ]
				]
				""")), "nestedArrayExactIgnoreOrder has incorrect member value (index 1)");

		assertTrue(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					[ 2, "nestedArrayExactIgnoreOrder" ],
					[ "nestedArrayContainsIgnoreOrder", 3 ],
					[ "nestedArrayAsValue", 4 ]
				]
				""")), "nestedArrayExactIgnoreOrder has the expected members in a different order (ok)");

		assertFalse(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					[ "nestedArrayExactIgnoreOrder", 2 ],
					[ "nestedArrayContainsIgnoreOrder", -3 ],
					[ "nestedArrayAsValue", 4 ]
				]
				""")), "nestedArrayContainsIgnoreOrder has incorrect member value (index 1)");

		assertTrue(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					[ "nestedArrayExactIgnoreOrder", 2 ],
					[ 3, "nestedArrayContainsIgnoreOrder" ],
					[ "nestedArrayAsValue", 4 ]
				]
				""")), "nestedArrayContainsIgnoreOrder has the expected members in a different order (ok)");

		assertTrue(matcher.test(parseJson("""
				[
					1,
					[ "nestedArrayExact", false ],
					[ "nestedArrayExactIgnoreOrder", 2 ],
					[ "***EXTRA***", "nestedArrayContainsIgnoreOrder", 3 ],
					[ "nestedArrayAsValue", 4 ]
				]
				""")), "nestedArrayContainsIgnoreOrder has the expected members plus an extra one (ok)");
	}

	@Test
	void arrayMatcher_nestedObject_exact()
	{
		JsonObject nestedObjectValue = Json.createObjectBuilder()
				.add("nestedObjectValue", 4)
				.add("c", "test")
				.build();

		JsonMatcher<JsonArray> matcher = JsonMatcher.array()
				.add(1)
				.add(JsonMatcher.object()
						.add("nestedObjectExact", 2)
						.add("a", false)
						.exact())
				.add(JsonMatcher.object()
						.add("nestedObjectContains", 3)
						.add("b", true)
						.contains())
				.add(nestedObjectValue)
				.add(5)
				.exact();

		assertEquals("(exact)[1,"
						+ "(exact){\"a\":false,\"nestedObjectExact\":2},"
						+ "(contains){\"b\":true,\"nestedObjectContains\":3},"
						+ nestedObjectValue + ",5]",
				matcher.toString());

		assertTrue(matcher.test(parseJson("""
				[
					1,
					{
						"nestedObjectExact": 2,
						"a": false
					},
					{
						"nestedObjectContains": 3,
				  		"b": true
				  	},
				  	{
				  		"nestedObjectValue": 4,
				    	"c": "test"
				  	},
				  	5
				]
				""")), "objects nested in arrays: exact value should match");

		assertTrue(matcher.test(parseJson("""
				[
					1,
					{
						"nestedObjectExact": 2,
						"a": false
					},
					{
						"nestedObjectContains": 3,
				  		"b": true,
				  		"extra": 1234
				  	},
				  	{
				  		"nestedObjectValue": 4,
				    	"c": "test"
				  	},
				  	5
				]
				""")), "objects nested in arrays: nested contains object has permitted extra member");

		assertFalse(matcher.test(parseJson("""
				[
					1,
				  	{
				  		"nestedObjectValue": 4,
				    	"c": "test"
				  	},
					{
						"nestedObjectContains": 3,
				  		"b": true,
				  		"extra": 1234
				  	},
					{
						"nestedObjectExact": 2,
						"a": false
					},
				  	5
				]
				""")), "objects nested in arrays: wrong member order not permitted for exact");

		assertFalse(matcher.test(parseJson("""
				[
					[ 9, 8, 7, 6 ],
					1,
					{
						"nestedObjectExact": 2,
						"a": false
					},
					{
						"nestedObjectContains": 3,
				  		"b": true
				  	},
				  	{
				  		"nestedObjectValue": 4,
				    	"c": "test"
				  	},
				  	5
				]
				""")), "objects nested in arrays: extra member not permitted for exact");
	}

	@Test
	void arrayMatcher_nestedObject_exactIgnoreOrder()
	{
		JsonObject nestedObjectValue = Json.createObjectBuilder()
				.add("nestedObjectValue", 4)
				.add("c", "test")
				.build();

		JsonMatcher<JsonArray> matcher = JsonMatcher.array()
				.add(1)
				.add(JsonMatcher.object()
						.add("nestedObjectExact", 2)
						.add("a", false)
						.exact())
				.add(JsonMatcher.object()
						.add("nestedObjectContains", 3)
						.add("b", true)
						.contains())
				.add(nestedObjectValue)
				.add(5)
				.exactIgnoreOrder();

		assertEquals("(exactIgnoreOrder)[1,"
						+ "(exact){\"a\":false,\"nestedObjectExact\":2},"
						+ "(contains){\"b\":true,\"nestedObjectContains\":3},"
						+ nestedObjectValue + ",5]",
				matcher.toString());

		assertTrue(matcher.test(parseJson("""
				[
					1,
					{
						"nestedObjectExact": 2,
						"a": false
					},
					{
						"nestedObjectContains": 3,
				  		"b": true
				  	},
				  	{
				  		"nestedObjectValue": 4,
				    	"c": "test"
				  	},
				  	5
				]
				""")), "objects nested in arrays: exact value should match");

		assertTrue(matcher.test(parseJson("""
				[
					1,
					{
						"nestedObjectExact": 2,
						"a": false
					},
					{
						"nestedObjectContains": 3,
				  		"b": true,
				  		"extra": 1234
				  	},
				  	{
				  		"nestedObjectValue": 4,
				    	"c": "test"
				  	},
				  	5
				]
				""")), "objects nested in arrays: nested contains object has permitted extra member");

		assertTrue(matcher.test(parseJson("""
				[
					1,
				  	{
				  		"nestedObjectValue": 4,
				    	"c": "test"
				  	},
					{
						"nestedObjectContains": 3,
				  		"b": true,
				  		"extra": 1234
				  	},
					{
						"nestedObjectExact": 2,
						"a": false
					},
				  	5
				]
				""")), "objects nested in arrays: wrong member order is permitted for exactIgnoreOrder");

		assertFalse(matcher.test(parseJson("""
				[
					[ 9, 8, 7, 6 ],
					1,
					{
						"nestedObjectExact": 2,
						"a": false
					},
					{
						"nestedObjectContains": 3,
				  		"b": true
				  	},
				  	{
				  		"nestedObjectValue": 4,
				    	"c": "test"
				  	},
				  	5
				]
				""")), "objects nested in arrays: extra member not permitted for exactIgnoreOrder");
	}

	@Test
	void arrayMatcher_nestedObject_containsIgnoreOrder()
	{
		JsonObject nestedObjectValue = Json.createObjectBuilder()
				.add("nestedObjectValue", 4)
				.add("c", "test")
				.build();

		JsonMatcher<JsonArray> matcher = JsonMatcher.array()
				.add(1)
				.add(JsonMatcher.object()
						.add("nestedObjectExact", 2)
						.add("a", false)
						.exact())
				.add(JsonMatcher.object()
						.add("nestedObjectContains", 3)
						.add("b", true)
						.contains())
				.add(nestedObjectValue)
				.add(5)
				.containsIgnoreOrder();

		assertEquals("(containsIgnoreOrder)[1,"
						+ "(exact){\"a\":false,\"nestedObjectExact\":2},"
						+ "(contains){\"b\":true,\"nestedObjectContains\":3},"
						+ nestedObjectValue + ",5]",
				matcher.toString());

		assertTrue(matcher.test(parseJson("""
				[
					1,
					{
						"nestedObjectExact": 2,
						"a": false
					},
					{
						"nestedObjectContains": 3,
				  		"b": true
				  	},
				  	{
				  		"nestedObjectValue": 4,
				    	"c": "test"
				  	},
				  	5
				]
				""")), "objects nested in arrays: exact value should match");

		assertTrue(matcher.test(parseJson("""
				[
					1,
					{
						"nestedObjectExact": 2,
						"a": false
					},
					{
						"nestedObjectContains": 3,
				  		"b": true,
				  		"extra": 1234
				  	},
				  	{
				  		"nestedObjectValue": 4,
				    	"c": "test"
				  	},
				  	5
				]
				""")), "objects nested in arrays: nested contains object has permitted extra member");

		assertTrue(matcher.test(parseJson("""
				[
					1,
				  	{
				  		"nestedObjectValue": 4,
				    	"c": "test"
				  	},
					{
						"nestedObjectContains": 3,
				  		"b": true,
				  		"extra": 1234
				  	},
					{
						"nestedObjectExact": 2,
						"a": false
					},
				  	5
				]
				""")), "objects nested in arrays: wrong member order is permitted for containsIgnoreOrder");

		assertTrue(matcher.test(parseJson("""
				[
					[ 9, 8, 7, 6 ],
					1,
					{
						"nestedObjectExact": 2,
						"a": false
					},
					{
						"nestedObjectContains": 3,
				  		"b": true
				  	},
				  	{
				  		"nestedObjectValue": 4,
				    	"c": "test"
				  	},
				  	5
				]
				""")), "objects nested in arrays: extra member is permitted for containsIgnoreOrder");
	}

	@Test
	void arrayMatcher_allTypes()
	{
		JsonMatcher.ArrayBuilder builder = JsonMatcher.array()
				.add("string")
				.add(1)
				.add(9876543210L)
				.add(123.456)
				.add(new BigInteger("98765432109876543210"))
				.add(new BigDecimal("98765432109876543210.123456789"))
				.add(true)
				.add(false)
				.add(JsonValue.EMPTY_JSON_ARRAY)
				.add(JsonValue.EMPTY_JSON_OBJECT)
				.addNull()
				.add(TEST_MATCHER_NON_EMPTY_STRING);

		JsonMatcher<JsonArray> matcher1 = builder.exact();

		assertEquals("(exact)[\"string\",1,9876543210,123.456,98765432109876543210,98765432109876543210.123456789,"
						+ "true,false,[],{},null," + TEST_MATCHER_NON_EMPTY_STRING + "]",
				matcher1.toString());

		assertTrue(matcher1.test(parseJson("""
				[
					"string",
					1,
					9876543210,
					123.456,
					98765432109876543210,
					98765432109876543210.123456789,
					true,
					false,
					[],
					{},
					null,
					"a"
				]
				""")), "array with all builder add() methods used, correct value, should match");

		assertFalse(matcher1.test(parseJson("""
				[
					"string",
					1,
					9876543210,
					123.456,
					98765432109876543210,
					98765432109876543210.123456789,
					true,
					false,
					[],
					{},
					null,
					""
				]
				""")), "array with all builder add() methods used, custom matcher value should be rejected");

		// Now replace all of the values using the set() methods. Values are different where possible, and the order is different.

		builder
			.set(0, TEST_MATCHER_POSITIVE_NUMBER)
			.set(1, true)
			.set(2, false)
			.set(3, JsonValue.EMPTY_JSON_ARRAY)
			.set(4, JsonValue.EMPTY_JSON_OBJECT)
			.setNull(5)
			.set(6, "different string")
			.set(7, -1)
			.set(8, -9876543210L)
			.set(9, -123.456)
			.set(10, new BigInteger("-98765432109876543210"))
			.set(11, new BigDecimal("-98765432109876543210.123456789"));

		JsonMatcher<JsonArray> matcher2 = builder.exact();

		assertEquals("(exact)[" + TEST_MATCHER_POSITIVE_NUMBER + ",true,false,[],{},null,\"different string\","
						+ "-1,-9876543210,-123.456,-98765432109876543210,-98765432109876543210.123456789]",
				matcher2.toString());

		assertTrue(matcher2.test(parseJson("""
				[
					1,
					true,
					false,
					[],
					{},
					null,
					"different string",
					-1,
					-9876543210,
					-123.456,
					-98765432109876543210,
					-98765432109876543210.123456789
				]
				""")), "replaced all array elements using all set() methods, correct value, should match");

		assertFalse(matcher2.test(parseJson("""
				[
					-1,
					true,
					false,
					[],
					{},
					null,
					"different string",
					-1,
					-9876543210,
					-123.456,
					-9876543210987654321,
					-98765432109876543210.123456789
				]
				""")), "replaced all array elements using all set() methods, custom matcher should reject");
	}

	@Test
	void arrayMatcher_reuseBuilder()
	{
		JsonMatcher.ArrayBuilder builder = JsonMatcher.array();

		JsonMatcher<JsonArray> matcher1 = builder.add("a").exact();
		assertTrue(matcher1.test(parseJson("""
				[ "a" ]
				""")), "one member in builder (a)");
		assertEquals("(exact)[\"a\"]", matcher1.toString());

		JsonMatcher<JsonArray> matcher2 = builder.add("b").exact();
		assertTrue(matcher2.test(parseJson("""
				[ "a", "b" ]
				""")), "add second member (b)");
		assertTrue(matcher1.test(parseJson("""
				[ "a" ]
				""")), "retry matcher1");
		assertFalse(matcher1.test(parseJson("""
				[ "a", "b" ]
				""")), "try matcher1 with object for matcher2");
		assertEquals("(exact)[\"a\",\"b\"]", matcher2.toString());
		assertEquals("(exact)[\"a\"]", matcher1.toString());

		JsonMatcher<JsonArray> matcher3 = builder.set(1, "overwritten").exact();
		assertTrue(matcher3.test(parseJson("""
				[ "a", "overwritten" ]
				""")), "overwrite member (b->overwritten)");
		assertEquals("(exact)[\"a\",\"overwritten\"]", matcher3.toString());

		JsonMatcher<JsonArray> matcher4 = builder.remove(0).add("c").exact();
		assertTrue(matcher4.test(parseJson("""
				[ "overwritten", "c" ]
				""")), "remove member (a), add member (c)");
		assertEquals("(exact)[\"overwritten\",\"c\"]", matcher4.toString());

		JsonMatcher<JsonArray> matcher5 = builder.reset().exact();
		assertTrue(matcher5.test(parseJson("""
				[]
				""")), "reset builder");
		assertEquals("(exact)[]", matcher5.toString());

		JsonMatcher<JsonArray> matcher6 = builder.add("new").containsIgnoreOrder();
		assertTrue(matcher6.test(parseJson("""
				[ "new", true, "extraMembersShouldBeIgnored", null ]
				""")), "add a new member to reset builder; use with containsIgnoreOrder");
		assertEquals("(containsIgnoreOrder)[\"new\"]", matcher6.toString());
	}

	@Test
	void arrayMatcher_overlapping_exactIgnoreOrder()
	{
		// This test checks the specific defined behaviour of the out-of-order matchers.
		// In order to ensure that these are deterministic, they are defined to process expectations in order, and each expectation
		// consumes the first value that it matches. When mixing custom and fixed matchers, this may mean that a custom matcher
		// consumes a value which would actually match a later fixed matcher, and the fixed matcher may then fail as a result.

		JsonMatcher<JsonArray> matcher = JsonMatcher.array()
				.add(TEST_MATCHER_POSITIVE_NUMBER)
				.add(1)
				.add(TEST_MATCHER_POSITIVE_NUMBER)
				.add(2)
				.add(TEST_MATCHER_NON_EMPTY_STRING)
				.exactIgnoreOrder();

		assertTrue(matcher.test(parseJson("""
				[ 9, 1, 8, 2, "a" ]
				""")), "exact value should match");

		assertTrue(matcher.test(parseJson("""
				[ "a", 9, 1, 8, 2 ]
				""")), "string element in different place, but should match");

		assertFalse(matcher.test(parseJson("""
				[ 1, 9, 8, 2, "a" ]
				""")), "wrong order should not match (first custom matcher consumes 1 value too early)");
	}

	@Test
	void duplicate()
	{
		JsonMatcher.ArrayBuilder orig = JsonMatcher.array()
				.add(1)
				.add("test")
				.add(true);

		JsonMatcher.ArrayBuilder copy = orig.duplicate();

		assertEquals("""
				(exact)[1,"test",true]\
				""", orig.exact().toString(), "original as expected");
		assertEquals("""
				(exact)[1,"test",true]\
				""", copy.exact().toString(), "copy as expected, same as original");

		orig.add(JsonValue.EMPTY_JSON_OBJECT);

		assertEquals("""
				(exactIgnoreOrder)[1,"test",true,{}]\
				""", orig.exactIgnoreOrder().toString(), "added member to original");
		assertEquals("""
				(exactIgnoreOrder)[1,"test",true]\
				""", copy.exactIgnoreOrder().toString(), "copy unchanged when original modified");

		copy.set(0, JsonValue.NULL);

		assertEquals("""
				(containsIgnoreOrder)[null,"test",true]\
				""", copy.containsIgnoreOrder().toString(), "overwritten member in copy");
		assertEquals("""
				(containsIgnoreOrder)[1,"test",true,{}]\
				""", orig.containsIgnoreOrder().toString(), "original unchanged when copy modified");
	}
}
