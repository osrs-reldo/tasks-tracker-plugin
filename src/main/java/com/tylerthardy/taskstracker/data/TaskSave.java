package com.tylerthardy.taskstracker.data;

import java.time.Instant;
import lombok.Data;

@Data
public class TaskSave
{
	boolean isCompleted;
	boolean isTracked;
	boolean isIgnored;
	Long timestamp;

	public TaskSave()
	{
		timestamp = Instant.now().toEpochMilli();
	}
}
