package net.reldo.taskstracker.data;

import net.reldo.taskstracker.tasktypes.TaskType;
import java.time.Instant;
import java.util.HashMap;

public class TrackerData
{
	public HashMap<TaskType, HashMap<String, TaskSave>> tasksByType = new HashMap<>();
	public Long timestamp;
	public TrackerSettings settings;

	public TrackerData()
	{
		this.timestamp = Instant.now().toEpochMilli();
		this.settings = new TrackerSettings();
	}
}
