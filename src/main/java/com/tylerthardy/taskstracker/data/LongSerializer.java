package com.tylerthardy.taskstracker.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.text.DecimalFormat;

public class LongSerializer implements JsonSerializer<Long>
{
	@Override
	public JsonElement serialize(Long value, Type type, JsonSerializationContext jsonSerializationContext)
	{
		return new JsonPrimitive(new DecimalFormat("#").format(value));
	}
}
