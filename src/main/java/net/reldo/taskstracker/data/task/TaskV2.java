package net.reldo.taskstracker.data.task;

import java.util.HashMap;
import java.util.List;
import lombok.Data;

@Data
public class TaskV2
{
	// These are properties that all tasks must have
	private final int id;
	private final int clientSortId;
	private final String name;
	private final String description;
	private final int points;
	private final List<RequiredSkillV2> skills;
	// These are metadata properties
	private final HashMap<String, String> properties;

	public String getPropertyValue(String propertyName)
	{
		return this.properties.get(propertyName);
	}
}
