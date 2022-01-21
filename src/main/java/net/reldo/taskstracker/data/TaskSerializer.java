package net.reldo.taskstracker.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class TaskSerializer implements JsonSerializer<TaskSave>
{
	@Override
	public JsonElement serialize(TaskSave value, Type type, JsonSerializationContext context)
	{
		ArrayList<String> attributes = new ArrayList<>();
		attributes.add(context.serialize(value.getCompletedOn()).getAsString());
		attributes.add(context.serialize(value.getTrackedOn()).getAsString());
		attributes.add(context.serialize(value.getIgnoredOn()).getAsString());

		// ID should remain the last attribute (see TaskDeserializer)
		// TODO: The entire task saving should no longer be necessary when the plugin switches to varbits only
		attributes.add(context.serialize(value.getId()).getAsString());

		return new JsonPrimitive(String.join("|", attributes));
	}
}
