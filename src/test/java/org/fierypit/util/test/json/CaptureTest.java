package org.fierypit.util.test.json;

import java.util.function.Consumer;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;

import org.easymock.Capture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.easymock.EasyMock.*;

class CaptureTest
{
	@Test
	void captureSimple()
	{
		// Creating the capture matcher should not invoke the consumer.
		Consumer<JsonString> mockConsumer = strictMock(Consumer.class);
		replay(mockConsumer);
		JsonMatcher<JsonString> matcher = JsonMatcher.value("test").capture(mockConsumer);
		verify(mockConsumer);
		assertEquals("(capture)\"test\"", matcher.toString());

		// If there is no match, there should be no capture either.
		reset(mockConsumer);
		replay(mockConsumer);
		assertFalse(matcher.test(Json.createValue("test2")));
		verify(mockConsumer);

		// If there is a match, there should be one capture, of the same object that is passed to the matcher.
		Capture<JsonString> consumerCapture = Capture.newInstance();
		reset(mockConsumer);
		mockConsumer.accept(capture(consumerCapture));
		replay(mockConsumer);
		JsonString testValue = Json.createValue("test");
		assertTrue(matcher.test(testValue));
		verify(mockConsumer);
		assertSame(testValue, consumerCapture.getValue());
	}

	@Test
	void captureWithinArray_exact()
	{
		// Creating the capture matcher should not invoke the consumer.
		Consumer<JsonNumber> mockConsumer = strictMock(Consumer.class);
		replay(mockConsumer);
		JsonMatcher<JsonArray> arrayMatcher = JsonMatcher.array()
				.add(1)
				.add(JsonMatcher.value(2).capture(mockConsumer))
				.add(3)
				.exact();
		verify(mockConsumer);

		// If the sub-matcher is not matched, there should be no capture.
		reset(mockConsumer);
		replay(mockConsumer);
		assertFalse(arrayMatcher.test(TestUtil.parseJson("[1,3]")));
		verify(mockConsumer);

		// If there is a match, there should be one capture, of the same object that is passed to the matcher.
		Capture<JsonNumber> consumerCapture = Capture.newInstance();
		reset(mockConsumer);
		mockConsumer.accept(capture(consumerCapture));
		replay(mockConsumer);
		JsonNumber testValue = Json.createValue(2);
		JsonArray testArray = Json.createArrayBuilder().add(1).add(testValue).add(3).build();
		assertTrue(arrayMatcher.test(testArray));
		verify(mockConsumer);
		assertSame(testValue, consumerCapture.getValue());

		// In cases where the sub-matcher could be matched while the overall matcher does not match, a value may or may not be
		// captured. We do not test this case here, as this is an implementation detail of array matching.
	}

	@Test
	void captureWithinArray_exactIgnoreOrder()
	{
		// Creating the capture matcher should not invoke the consumer.
		Consumer<JsonNumber> mockConsumer = strictMock(Consumer.class);
		replay(mockConsumer);
		JsonMatcher<JsonArray> arrayMatcher = JsonMatcher.array()
				.add(1)
				.add(JsonMatcher.value(2).capture(mockConsumer))
				.add(3)
				.exactIgnoreOrder();
		verify(mockConsumer);

		// If the sub-matcher is not matched, there should be no capture.
		reset(mockConsumer);
		replay(mockConsumer);
		assertFalse(arrayMatcher.test(TestUtil.parseJson("[1,3]")));
		verify(mockConsumer);

		// If there is a match, there should be one capture, of the same object that is passed to the matcher.
		Capture<JsonNumber> consumerCapture = Capture.newInstance();
		reset(mockConsumer);
		mockConsumer.accept(capture(consumerCapture));
		replay(mockConsumer);
		JsonNumber testValue = Json.createValue(2);
		JsonArray testArray = Json.createArrayBuilder().add(3).add(1).add(testValue).build();
		assertTrue(arrayMatcher.test(testArray));
		verify(mockConsumer);
		assertSame(testValue, consumerCapture.getValue());

		// In cases where the sub-matcher could be matched while the overall matcher does not match, a value may or may not be
		// captured. We do not test this case here, as this is an implementation detail of array matching.
	}

	@Test
	void captureWithinArray_containsIgnoreOrder()
	{
		// Creating the capture matcher should not invoke the consumer.
		Consumer<JsonNumber> mockConsumer = strictMock(Consumer.class);
		replay(mockConsumer);
		JsonMatcher<JsonArray> arrayMatcher = JsonMatcher.array()
				.add(1)
				.add(JsonMatcher.value(2).capture(mockConsumer))
				.add(3)
				.containsIgnoreOrder();
		verify(mockConsumer);

		// If the sub-matcher is not matched, there should be no capture.
		reset(mockConsumer);
		replay(mockConsumer);
		assertFalse(arrayMatcher.test(TestUtil.parseJson("[1,3]")));
		verify(mockConsumer);

		// If there is a match, there should be one capture, of the same object that is passed to the matcher.
		Capture<JsonNumber> consumerCapture = Capture.newInstance();
		reset(mockConsumer);
		mockConsumer.accept(capture(consumerCapture));
		replay(mockConsumer);
		JsonNumber testValue = Json.createValue(2);
		JsonArray testArray = Json.createArrayBuilder().add(4).add(3).add(1).add(testValue).build();
		assertTrue(arrayMatcher.test(testArray));
		verify(mockConsumer);
		assertSame(testValue, consumerCapture.getValue());

		// In cases where the sub-matcher could be matched while the overall matcher does not match, a value may or may not be
		// captured. We do not test this case here, as this is an implementation detail of array matching.
	}

	@Test
	void captureWithinObject_contains()
	{
		// Creating the capture matcher should not invoke the consumer.
		Consumer<JsonNumber> mockConsumer = strictMock(Consumer.class);
		replay(mockConsumer);
		JsonMatcher<JsonObject> objectMatcher = JsonMatcher.object()
				.add("a", 1)
				.add("b", JsonMatcher.value(2).capture(mockConsumer))
				.add("c", 3)
				.contains();
		verify(mockConsumer);

		// If the sub-matcher is not matched, there should be no capture.
		reset(mockConsumer);
		replay(mockConsumer);
		assertFalse(objectMatcher.test(TestUtil.parseJson("{\"a\":1,\"b\":-2,\"c\":3}")));
		verify(mockConsumer);

		// If there is a match, there should be one capture, of the same object that is passed to the matcher.
		Capture<JsonNumber> consumerCapture = Capture.newInstance();
		reset(mockConsumer);
		mockConsumer.accept(capture(consumerCapture));
		replay(mockConsumer);
		JsonNumber testValue = Json.createValue(2);
		JsonObject testObject = Json.createObjectBuilder()
				.add("extra", true)
				.add("a", 1)
				.add("b", testValue)
				.add("c", 3)
				.build();
		assertTrue(objectMatcher.test(testObject));
		verify(mockConsumer);
		assertSame(testValue, consumerCapture.getValue());

		// In cases where the sub-matcher could be matched while the overall matcher does not match, a value may or may not be
		// captured. We do not test this case here, as this is an implementation detail of object matching.
	}
}
