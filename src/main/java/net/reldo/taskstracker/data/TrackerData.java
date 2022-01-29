package net.reldo.taskstracker.data;

import net.reldo.taskstracker.tasktypes.Task;
import net.reldo.taskstracker.tasktypes.TaskType;
import java.time.Instant;
import java.util.HashMap;

public class TrackerData
{
	public HashMap<TaskType, HashMap<Integer, Task>> tasksByType = new HashMap<>();
	public Long timestamp;

	public TrackerData()
	{
		this.timestamp = Instant.now().toEpochMilli();
	}
}
