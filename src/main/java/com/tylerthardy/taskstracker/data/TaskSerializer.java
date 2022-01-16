package com.tylerthardy.taskstracker.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class TaskSerializer implements JsonSerializer<TaskSave>
{
	@Override
	public JsonElement serialize(TaskSave value, Type type, JsonSerializationContext context)
	{
		ArrayList<String> attributes = new ArrayList<>();
		attributes.add(value.isCompleted() ? "1" : "0");
		attributes.add(value.isTracked() ? "1" : "0");
		attributes.add(new DecimalFormat("#").format(value.getTimestamp()));

		return new JsonPrimitive(String.join("|", attributes));
	}
}
