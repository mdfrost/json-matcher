# json-matcher

Java library providing flexible matching of `javax.json` JSON values. This is intended to be used in automated tests.

## Why is this needed?

The `javax.json` API supports the standard Java `equals` method, if you need to compare that a JSON value is exactly equal to an expected value - for example, in a JUnit `assertEquals` call. However, this does not cover all uses, especially in automated tests.

JSON arrays are only considered equal if both contain exactly the same values, in exactly the same order. This is fine if the values are genuinely ordered arrays, but it is common to use JSON arrays to represent sets or "bags" of values, where the order is not important. When writing a unit test for a method producing such a value, the usual choices are:
* to write the test to expect the specific ordering known to be produced by the current implementation (which means the test is tied to that implementation and will reject perfectly valid alternative orderings of the values);
* to convert both the expected and actual values to `java.lang.Set`, since the general contract for the `Set` will compare these ignoring order (which only works if duplicate values are not expected);
* to sort both arrays before comparing (which only works if the values are comparable).

The second and third of these options are also not possible when comparing an array embedded within another JSON structure (which is a common occurrence when JSON is used for complex network responses).

Secondly, unit tests (and certainly tests of larger assemblies) may not want to test every single value in a complex object (say, a microservice response). It may be that a particular test only really cares about the content of a single specific member in a JSON object (as the other members will be tested by other tests). Being able to concisely express this assertion can make tests much cleaner.

Note that order-independent array matchers can be slow due to the matching algorithm used. (This library is intended for use in automated testing, not high-performance production services.) This is done in order to make these matchers deterministic even if there is a mixture of potentially overlapping sub-matchers within the array being matched.

## Examples

```java
JsonMatcher<JsonObject> matcher = JsonMatcher.object()
    .add("a", "value")                  // member "a" must be the fixed string "value"
    .add("b", JsonMatcher.ANY_NUMBER)   // member "b" must be a number, but any number will do
    .contains();
```

Because this matcher is built with `contains()`, it will match any object that contains appropriate "a" and "b" members, whether or not other members are present:
* `{ "b": 0, "a": "value" }` will match
* `{ "a": "value", "c": null, "b": -999 }` will match (the "c" member is ignored)
* `{ "a": "value", b: "1" }` will not match (value for "b" is a JSON string, not a JSON number)

```java
JsonMatcher<JsonObject> matcher = JsonMatcher.object()
    .add("msg-type", "get-available-response")
    .add("available-values", JsonMatcher.array()
        .add(1)
        .add(2)
        .add(3)
        .exactIgnoreOrder())
    .exact();
```

This matcher illustrates a simple example of the type of thing that often occurs with JSON responses to network requests. In this case, suppose that this object is a response to a "get-available" network API call - perhaps this is a call used by client UI to highlight valid choices for a user to select. The response is a JSON object with a fixed "msg-type" member, but the "available-values" member is an array where the order of elements does not matter.
* `{ "msg-type": "get-available-response", "available-values": [ 3, 2, 1 ] }` will match

The similarity with construction of JSON structures using `javax.json.Json.createArrayBuilder()` is intentional. However, the builders in this library are more flexible, and can be re-used to create multiple related matchers. In addition, the methods that take an arbitrary object or existing collection will accept a wider range of datatypes than those in the `javax.json` library (which do not even permit existing `JsonValue` objects).

## Requirements

The library requires Java 16, primarily for the new "pattern matching instanceof" operator. (The code could easily be modified to avoid this, if anyone has a requirement to support an earlier version.) In addition, since the library already requires Java 16, the tests for the library make use of text blocks, mostly because this makes JSON strings easier to read since " characters no longer need to be escaped.

The only runtime dependency outside `java.base` is on the `javax.json` API, and without that the library is not much use anyway!
