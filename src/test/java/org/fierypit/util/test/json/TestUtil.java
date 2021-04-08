package org.fierypit.util.test.json;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonValue;

class TestUtil
{
	TestUtil() {}

	static JsonValue parseJson(String json)
	{
		return Json.createReader(new StringReader(json)).readValue();
	}
}
