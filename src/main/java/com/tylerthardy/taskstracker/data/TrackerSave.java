package com.tylerthardy.taskstracker.data;

import com.tylerthardy.taskstracker.tasktypes.TaskType;
import java.time.Instant;
import java.util.HashMap;
import lombok.Data;

@Data
public class TrackerSave
{
	public HashMap<TaskType, HashMap<String, TaskSave>> tasks = new HashMap<>();

	TaskType selectedTaskType;
	String username;
	TrackerWorldType worldType;
	long timestamp;

	public TrackerSave(String username, TrackerWorldType worldType)
	{
		this.username = username;
		this.worldType = worldType;
		this.timestamp = Instant.now().toEpochMilli();
	}
}
