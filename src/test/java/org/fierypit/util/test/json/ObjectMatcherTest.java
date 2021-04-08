package org.fierypit.util.test.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.junit.jupiter.api.Test;

import static org.fierypit.util.test.json.TestUtil.parseJson;

import static org.junit.jupiter.api.Assertions.*;

// These tests test both the builder and the matcher together.

class ObjectMatcherTest
{
	@Test
	void objectMatcher_empty_exact()
	{
		JsonMatcher<JsonObject> matcher = JsonMatcher.object().exact();

		assertTrue(matcher.test(parseJson("{}")));
		assertFalse(matcher.test(parseJson("{\"a\":123}")));
		assertFalse(matcher.test(parseJson("[]")));
		assertFalse(matcher.test(JsonValue.NULL));

		assertEquals("(exact){}", matcher.toString());
	}

	@Test
	void objectMatcher_empty_contains()
	{
		JsonMatcher<JsonObject> matcher = JsonMatcher.object().contains();

		assertTrue(matcher.test(parseJson("{}")));
		assertTrue(matcher.test(parseJson("{\"a\":123}")));
		assertFalse(matcher.test(parseJson("[]")));
		assertFalse(matcher.test(JsonValue.NULL));

		assertEquals("(contains){}", matcher.toString());
	}

	@Test
	void objectMatcher_simple_exact()
	{
		JsonMatcher<JsonObject> matcher = JsonMatcher.object()
				.add("string", "test")
				.add("int", 123)
				.add("long", 9876543210L)
				.add("double", 123.456)
				.add("BigInteger", new BigInteger("98765432109876543210"))
				.add("BigDecimal", new BigDecimal("98765432109876543210.123456789"))
				.add("boolean", true)
				.addNull("null")
				.exact();

		assertEquals("""
				(exact){"BigDecimal":98765432109876543210.123456789,"BigInteger":98765432109876543210,"boolean":true,"double":123.456,"int":123,"long":9876543210,"null":null,"string":"test"}\
				""", matcher.toString());

		assertTrue(matcher.test(parseJson("""
				{
					"null": null,
					"boolean": true,
					"BigDecimal": 98765432109876543210.123456789,
					"BigInteger": 98765432109876543210,
					"double": 123.456,
					"long": 9876543210,
					"int": 123,
					"string": "test"
				}
				""")), "same object should match, regardless of member order");

		assertFalse(matcher.test(parseJson("""
				{
					"boolean": true,
					"BigDecimal": 98765432109876543210.123456789,
					"BigInteger": 98765432109876543210,
					"double": 123.456,
					"long": 9876543210,
					"int": 123,
					"string": "test"
				}
				""")), "one member missing");

		assertFalse(matcher.test(parseJson("""
				{
					"null": null,
					"boolean": true,
					"BigDecimal": 98765432109876543210.123456789,
					"BigInteger": 98765432109876543210,
					"double": 123.456,
					"long": 9876543210,
					"int": 123,
					"string": "test wrong"
				}
				""")), "one member wrong value");

		// This match should fail for exact match, whereas it should succeed for contains match.
		assertFalse(matcher.test(parseJson("""
				{
					"null": null,
					"boolean": true,
					"BigDecimal": 98765432109876543210.123456789,
					"BigInteger": 98765432109876543210,
					"double": 123.456,
					"long": 9876543210,
					"int": 123,
					"string": "test",
					"string2": "extra"
				}
				""")), "extra member (invalid for exact match)");
	}

	@Test
	void objectMatcher_simple_contains()
	{
		JsonMatcher<JsonObject> matcher = JsonMatcher.object()
				.add("string", "test")
				.add("int", 123)
				.add("long", 9876543210L)
				.add("double", 123.456)
				.add("BigInteger", new BigInteger("98765432109876543210"))
				.add("BigDecimal", new BigDecimal("98765432109876543210.123456789"))
				.add("boolean", true)
				.addNull("null")
				.contains();

		assertEquals("""
				(contains){"BigDecimal":98765432109876543210.123456789,\
				"BigInteger":98765432109876543210,"boolean":true,\
				"double":123.456,\
				"int":123,\
				"long":9876543210,\
				"null":null,\
				"string":"test"}\
				""", matcher.toString());

		assertTrue(matcher.test(parseJson("""
				{
					"null": null,
					"boolean": true,
					"BigDecimal": 98765432109876543210.123456789,
					"BigInteger": 98765432109876543210,
					"double": 123.456,
					"long": 9876543210,
					"int": 123,
					"string": "test"
				}
				""")), "same object should match, regardless of member order");

		assertFalse(matcher.test(parseJson("""
				{
					"boolean": true,
					"BigDecimal": 98765432109876543210.123456789,
					"BigInteger": 98765432109876543210,
					"double": 123.456,
					"long": 9876543210,
					"int": 123,
					"string": "test"
				}
				""")), "one member missing");

		assertFalse(matcher.test(parseJson("""
				{
					"null": null,
					"boolean": true,
					"BigDecimal": 98765432109876543210.123456789,
					"BigInteger": 98765432109876543210,
					"double": 123.456,
					"long": 9876543210,
					"int": 123,
					"string": "test wrong"
				}
				""")), "one member wrong value");

		// This match should succeed for contains match, whereas it should fail for exact match.
		assertTrue(matcher.test(parseJson("""
				{
					"null": null,
					"boolean": true,
					"BigDecimal": 98765432109876543210.123456789,
					"BigInteger": 98765432109876543210,
					"double": 123.456,
					"long": 9876543210,
					"int": 123,
					"string": "test",
					"string2": "extra"
				}
				""")), "extra member (valid for contains match)");
	}

	@Test
	void objectMatcher_addAll()
	{
		// Custom matcher used to check that such things are accepted.
		JsonMatcher<?> customMatcher = new JsonMatcher<>() {
			@Override public boolean test(JsonValue value) { return true; }
			@Override public String toString() { return "(CUSTOM)"; }
		};

		// We use a limited value for the Float element, that we know can be represented exactly in single precision.

		JsonMatcher<JsonObject> matcher = JsonMatcher.object().addAll(Map.ofEntries(
				Map.entry("String", "test"),
				Map.entry("Integer", 123),
				Map.entry("Long", 9876543210L),
				Map.entry("Float", 1.25f),
				Map.entry("Double", 123.456),
				Map.entry("BigInteger", new BigInteger("98765432109876543210")),
				Map.entry("BigDecimal", new BigDecimal("98765432109876543210.123456789")),
				Map.entry("true", Boolean.TRUE),
				Map.entry("false", Boolean.FALSE),
				Map.entry("null", JsonValue.NULL),
				Map.entry("object", Map.of(
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
				)),
				Map.entry("array", List.of(
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
				)),
				Map.entry("matcher", customMatcher),
				Map.entry("objectValue", JsonValue.EMPTY_JSON_OBJECT),
				Map.entry("arrayValue", JsonValue.EMPTY_JSON_ARRAY),
				Map.entry("trueValue", JsonValue.TRUE),
				Map.entry("falseValue", JsonValue.FALSE)
		)).exact();

		// Rather than trying to actually test this matcher by matching values, we just check its string representation.
		// Note that object keys should be sorted within both the returned matcher and the matcher constructed from the nested Map.

		assertEquals("""
				(exact){\
				"BigDecimal":98765432109876543210.123456789,\
				"BigInteger":98765432109876543210,\
				"Double":123.456,\
				"Float":1.25,\
				"Integer":123,\
				"Long":9876543210,\
				"String":"test",\
				"array":(exact)["test",123,9876543210,1.25,123.456,98765432109876543210,98765432109876543210.123456789,true,false,null],\
				"arrayValue":[],\
				"false":false,\
				"falseValue":false,\
				"matcher":(CUSTOM),\
				"null":null,\
				"object":(exact){"BigDecimal":98765432109876543210.123456789,"BigInteger":98765432109876543210,"Double":123.456,"Float":1.25,"Integer":123,"Long":9876543210,"String":"test","false":false,"null":null,"true":true},\
				"objectValue":{},\
				"true":true,\
				"trueValue":true}\
				""", matcher.toString());
	}

	@Test
	void objectMatcher_nested_exact()
	{
		JsonObject nestedObjectValue = Json.createObjectBuilder()
				.add("f", 4)
				.add("g", "test")
				.build();

		JsonMatcher<JsonObject> matcher = JsonMatcher.object()
				.add("a", 1)
				.add("nestedObjectExact", JsonMatcher.object()
						.add("b", 2)
						.add("c", false)
						.exact())
				.add("nestedObjectContains", JsonMatcher.object()
						.add("d", 3)
						.add("e", true)
						.contains())
				.add("nestedObjectValue", nestedObjectValue)
				.exact();

		// Object matchers sort their keys, but we must use the actual toString() output for the embedded JsonObject.
		assertEquals(
			"(exact){"
					+ "\"a\":1,"
					+ "\"nestedObjectContains\":(contains){\"d\":3,\"e\":true},"
					+ "\"nestedObjectExact\":(exact){\"b\":2,\"c\":false},"
					+ "\"nestedObjectValue\":" + nestedObjectValue
				+ "}",
			matcher.toString()
		);

		assertTrue(matcher.test(parseJson("""
				{
					"a": 1,
					"nestedObjectExact": {
						"b": 2,
						"c": false
					},
					"nestedObjectContains": {
						"d": 3,
						"e": true
					},
					"nestedObjectValue": {
						"f": 4,
						"g": "test"
					}
				}
				""")), "object should match exactly");

		assertFalse(matcher.test(parseJson("""
				{
					"a": 1,
					"nestedObjectExact": {
						"b": -2,
						"c": false
					},
					"nestedObjectContains": {
						"d": 3,
						"e": true
					},
					"nestedObjectValue": {
						"f": 4,
						"g": "test"
					}
				}
				""")), "nestedObjectExact has invalid member value (b)");

		assertFalse(matcher.test(parseJson("""
				{
					"a": 1,
					"nestedObjectExact": {
						"b": 2,
						"c": false
					},
					"nestedObjectContains": {
						"d": -3,
						"e": true
					},
					"nestedObjectValue": {
						"f": 4,
						"g": "test"
					}
				}
				""")), "nestedObjectContains has invalid member value (d)");

		assertFalse(matcher.test(parseJson("""
				{
					"a": 1,
					"nestedObjectExact": {
						"b": 2,
						"c": false
					},
					"nestedObjectContains": {
						"d": 3,
						"e": true
					},
					"nestedObjectValue": {
						"f": 4,
						"g": "test wrong"
					}
				}
				""")), "nestedObjectValue has invalid member value (g)");

		assertTrue(matcher.test(parseJson("""
				{
					"a": 1,
					"nestedObjectExact": {
						"b": 2,
						"c": false
					},
					"nestedObjectContains": {
						"d": 3,
						"e": true,
						"x": "extra ok here"
					},
					"nestedObjectValue": {
						"f": 4,
						"g": "test"
					}
				}
				""")), "nestedObjectContains has extra member, which should still match");
	}

	@Test
	void objectMatcher_nested_contains()
	{
		JsonObject nestedObjectValue = Json.createObjectBuilder()
				.add("f", 4)
				.add("g", "test")
				.build();

		JsonMatcher<JsonObject> matcher = JsonMatcher.object()
				.add("a", 1)
				.add("nestedObjectExact", JsonMatcher.object()
						.add("b", 2)
						.add("c", false)
						.exact())
				.add("nestedObjectContains", JsonMatcher.object()
						.add("d", 3)
						.add("e", true)
						.contains())
				.add("nestedObjectValue", nestedObjectValue)
				.contains();

		// Object matchers sort their keys, but we must use the actual toString() output for the embedded JsonObject.
		assertEquals(
			"(contains){"
				+ "\"a\":1,"
				+ "\"nestedObjectContains\":(contains){\"d\":3,\"e\":true},"
				+ "\"nestedObjectExact\":(exact){\"b\":2,\"c\":false},"
				+ "\"nestedObjectValue\":" + nestedObjectValue
			+ "}",
			matcher.toString());

		assertTrue(matcher.test(parseJson("""
				{
					"a": 1,
					"nestedObjectExact": {
						"b": 2,
						"c": false
					},
					"nestedObjectContains": {
						"d": 3,
						"e": true
					},
					"nestedObjectValue": {
						"f": 4,
						"g": "test"
					}
				}
				""")), "exact object should match for contains too");

		assertFalse(matcher.test(parseJson("""
				{
					"a": 1,
					"nestedObjectExact": {
						"b": -2,
						"c": false
					},
					"nestedObjectContains": {
						"d": 3,
						"e": true
					},
					"nestedObjectValue": {
						"f": 4,
						"g": "test"
					}
				}
				""")), "nestedObjectExact has invalid member value (b)");

		assertFalse(matcher.test(parseJson("""
				{
					"a": 1,
					"nestedObjectExact": {
						"b": 2,
						"c": false
					},
					"nestedObjectContains": {
						"d": -3,
						"e": true
					},
					"nestedObjectValue": {
						"f": 4,
						"g": "test"
					}
				}
				""")), "nestedObjectContains has invalid member value (d)");

		assertFalse(matcher.test(parseJson("""
				{
					"a": 1,
					"nestedObjectExact": {
						"b": 2,
						"c": false
					},
					"nestedObjectContains": {
						"d": 3,
						"e": true
					},
					"nestedObjectValue": {
						"f": 4,
						"g": "test wrong"
					}
				}
				""")), "nestedObjectValue has invalid member value (g)");

		assertTrue(matcher.test(parseJson("""
				{
					"a": 1,
					"nestedObjectExact": {
						"b": 2,
						"c": false
					},
					"nestedObjectContains": {
						"d": 3,
						"e": true,
						"x": "extra ok here"
					},
					"nestedObjectValue": {
						"f": 4,
						"g": "test"
					}
				}
				""")), "nestedObjectContains has extra member, which should still match");

		assertTrue(matcher.test(parseJson("""
				{
					"a": 1,
					"nestedObjectExact": {
						"b": 2,
						"c": false
					},
					"nestedObjectContains": {
						"d": 3,
						"e": true,
						"x": "extra ok here"
					},
					"nestedObjectValue": {
						"f": 4,
						"g": "test"
					},
					"anotherNestedObjectIgnoredForContains": {
						"x": 123,
						"y": 456
					},
					"nestedArrayAlsoIgnored": [ 1, 2, 3, 4, 5, 6 ]
				}
				""")), "nestedObjectContains has 2 extra members, but should still match");
	}

	@Test
	void objectMatcher_nestedArray_exact()
	{
		JsonMatcher<JsonObject> matcher = JsonMatcher.object()
				.add("a", 1)
				.add("nestedArrayExact", JsonMatcher.array().add(1).add(2).add(3).exact())
				.add("nestedArrayExactIgnoreOrder", JsonMatcher.array().add(4).add(5).add(6).exactIgnoreOrder())
				.add("nestedArrayContainsIgnoreOrder", JsonMatcher.array().add(7).add(8).add(9).containsIgnoreOrder())
				.exact();

		assertEquals("""
				(exact){"a":1,\
				"nestedArrayContainsIgnoreOrder":(containsIgnoreOrder)[7,8,9],\
				"nestedArrayExact":(exact)[1,2,3],\
				"nestedArrayExactIgnoreOrder":(exactIgnoreOrder)[4,5,6]}\
				""", matcher.toString());

		assertTrue(matcher.test(parseJson("""
				{
					"a": 1,
					"nestedArrayExact": [ 1, 2, 3 ],
					"nestedArrayExactIgnoreOrder": [ 6, 5, 4 ],
					"nestedArrayContainsIgnoreOrder": [ 9, 8, 7, 0 ]
				}
				""")), "nested arrays; nestedArrayContainsIgnoreOrder has permitted extra member");

		assertFalse(matcher.test(parseJson("""
				{
					"a": 1,
					"nestedArrayExact": [ 1, 2, 3 ],
					"nestedArrayExactIgnoreOrder": [ 6, 5, 4, 0 ],
					"nestedArrayContainsIgnoreOrder": [ 9, 8, 7 ]
				}
				""")), "nested arrays; nestedArrayExactIgnoreOrder contains invalid extra member");
	}

	@Test
	void objectMatcher_reuseBuilder()
	{
		JsonMatcher.ObjectBuilder builder = JsonMatcher.object();

		JsonMatcher<JsonObject> matcher1 = builder.add("a", 123).exact();
		assertTrue(matcher1.test(parseJson("""
				{ "a": 123 }
				""")), "one member in builder (a)");
		assertEquals("(exact){\"a\":123}", matcher1.toString());

		JsonMatcher<JsonObject> matcher2 = builder.add("b", 456).exact();
		assertTrue(matcher2.test(parseJson("""
				{ "a": 123, "b": 456 }
				""")), "add second member (b)");
		assertTrue(matcher1.test(parseJson("""
				{ "a": 123 }
				""")), "retry matcher1");
		assertFalse(matcher1.test(parseJson("""
				{ "a": 123, "b": 456 }
				""")), "try matcher1 with object for matcher2");
		assertEquals("(exact){\"a\":123,\"b\":456}", matcher2.toString());

		JsonMatcher<JsonObject> matcher3 = builder.add("a", "overwritten").exact();
		assertTrue(matcher3.test(parseJson("""
				{ "a": "overwritten", "b": 456 }
				""")), "overwrite member (a)");
		assertEquals("(exact){\"a\":\"overwritten\",\"b\":456}", matcher3.toString());

		JsonMatcher<JsonObject> matcher4 = builder.remove("b").add("c", 789).exact();
		assertTrue(matcher4.test(parseJson("""
				{ "a": "overwritten", "c": 789 }
				""")), "remove member (b), add member (c)");
		assertEquals("(exact){\"a\":\"overwritten\",\"c\":789}", matcher4.toString());

		JsonMatcher<JsonObject> matcher5 = builder.reset().exact();
		assertTrue(matcher5.test(parseJson("""
				{}
				""")), "reset builder");
		assertEquals("(exact){}", matcher5.toString());

		JsonMatcher<JsonObject> matcher6 = builder.add("new", true).contains();
		assertTrue(matcher6.test(parseJson("""
				{ "new": true, "extraMemberShouldBeIgnored": null }
				""")));
		assertEquals("(contains){\"new\":true}", matcher6.toString());
	}

	@Test
	void duplicate()
	{
		JsonMatcher.ObjectBuilder orig = JsonMatcher.object()
				.add("a", 1)
				.add("b", "test")
				.add("c", true);

		JsonMatcher.ObjectBuilder copy = orig.duplicate();

		assertEquals("""
				(exact){"a":1,"b":"test","c":true}\
				""", orig.exact().toString(), "original as expected");
		assertEquals("""
				(exact){"a":1,"b":"test","c":true}\
				""", copy.exact().toString(), "copy as expected, same as original");

		orig.add("d", JsonValue.EMPTY_JSON_ARRAY);

		assertEquals("""
				(exact){"a":1,"b":"test","c":true,"d":[]}\
				""", orig.exact().toString(), "added member to original");
		assertEquals("""
				(exact){"a":1,"b":"test","c":true}\
				""", copy.exact().toString(), "copy unchanged when original modified");

		copy.add("a", JsonValue.NULL);

		assertEquals("""
				(contains){"a":null,"b":"test","c":true}\
				""", copy.contains().toString(), "overwritten member in copy");
		assertEquals("""
				(contains){"a":1,"b":"test","c":true,"d":[]}\
				""", orig.contains().toString(), "original unchanged when copy modified");
	}
}
