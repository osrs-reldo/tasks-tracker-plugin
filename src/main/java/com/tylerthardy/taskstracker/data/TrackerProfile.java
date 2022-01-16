package com.tylerthardy.taskstracker.data;

import com.tylerthardy.taskstracker.tasktypes.TaskType;
import java.time.Instant;
import java.util.HashMap;

public class TrackerProfile
{
	public HashMap<TaskType, HashMap<String, TaskSave>> tasksByType = new HashMap<>();
	public Long timestamp;
	public String displayName;

	public TrackerProfile()
	{
		this.timestamp = Instant.now().toEpochMilli();
	}
}
