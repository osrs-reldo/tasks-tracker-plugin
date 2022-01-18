package com.tylerthardy.taskstracker.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskDeserializer implements JsonDeserializer<TaskSave>
{
	@Override
	public TaskSave deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException
	{
		String input = jsonElement.getAsString();
		String[] attributes = input.split("\\|");
		TaskSave taskSave = new TaskSave();
		taskSave.setCompleted(attributes[0].equals("1"));
		taskSave.setTracked(attributes[1].equals("1"));
		taskSave.setIgnored(attributes[2].equals("1"));
		taskSave.setTimestamp(Long.parseLong(attributes[3]));
		return taskSave;
	}
}
