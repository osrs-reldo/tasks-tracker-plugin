package com.tylerthardy.taskstracker.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.TaskType;
import java.lang.reflect.Type;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskDeserializer implements JsonDeserializer<Task>
{
	@Override
	public Task deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException
	{
		String input = jsonElement.getAsString();
		String[] attributes = input.split("\\|");
		Type taskClassType = TaskType.valueOf(attributes[0]).getClassType();
		try
		{
			return (Task) taskClassType.getClass().newInstance();
		} catch (Exception ex)
		{
			log.error("Error instantiating {} task: {}", taskClassType.getTypeName(), input);
		}

		return null;
	}
}
