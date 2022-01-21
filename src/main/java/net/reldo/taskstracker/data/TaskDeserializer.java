package net.reldo.taskstracker.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskDeserializer implements JsonDeserializer<TaskSave>
{
	private static final int LENGTH_WITHOUT_ID = 3;

	@Override
	public TaskSave deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException
	{
		String input = jsonElement.getAsString();
		String[] attributes = input.split("\\|");
		TaskSave taskSave = new TaskSave();
		taskSave.setCompletedOn(Long.parseLong(attributes[0]));
		taskSave.setTrackedOn(Long.parseLong(attributes[1]));
		taskSave.setIgnoredOn(Long.parseLong(attributes[2]));

		// ID should remain the last attribute so that new save attributes can be added without issue
		// TODO: The entire task saving should no longer be necessary when the plugin switches to varbits only
		if (attributes.length > LENGTH_WITHOUT_ID)
		{
			taskSave.setId(Integer.parseInt(attributes[LENGTH_WITHOUT_ID]));
		}
		return taskSave;
	}
}
