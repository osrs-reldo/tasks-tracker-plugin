package com.tylerthardy.taskstracker.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tylerthardy.taskstracker.tasktypes.Task;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class TaskSerializer implements JsonSerializer<Task>
{
	@Override
	public JsonElement serialize(Task value, Type type, JsonSerializationContext context)
	{
		ArrayList<String> attributes = new ArrayList<>();
		attributes.add(value.getName());
		attributes.add(value.isCompleted() ? "1" : "0");
		attributes.add(value.isTracked() ? "1" : "0");

		return new JsonPrimitive(String.join("|", attributes));
	}
}
