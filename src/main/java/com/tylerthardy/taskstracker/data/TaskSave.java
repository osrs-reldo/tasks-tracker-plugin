package com.tylerthardy.taskstracker.data;

import lombok.Data;

@Data
public class TaskSave
{
	long completedOn;
	long trackedOn;
	boolean isIgnored;

	public boolean isCompleted()
	{
		return completedOn > 0;
	}

	public boolean isTracked()
	{
		return trackedOn > 0;
	}
}
