package com.tylerthardy.taskstracker.data;

import com.google.gson.Gson;
import com.tylerthardy.taskstracker.tasktypes.AbstractTaskManager;
import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.TaskType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import lombok.Data;

@Data
public class TrackerSave
{
	public HashMap<TaskType, ArrayList<Task>> tasks = new HashMap<>();

	TaskType selectedTaskType;
	String username;
	TrackerWorldType worldType;
	long timestamp;

	public TrackerSave(HashMap<TaskType, AbstractTaskManager> taskManagers, String username, TrackerWorldType worldType, TaskType selectedTaskType)
	{
		// Tasks
		taskManagers.forEach((taskType, manager) -> tasks.put(taskType, manager.tasks));
		// Quests
		// TODO
		// Diaries
		// TODO
		// Relics
		// TODO
		// Unlocks
		// TODO
		// Boss KC
		// TODO

		this.username = username;
		this.worldType = worldType;
		this.timestamp = Instant.now().toEpochMilli();
		this.selectedTaskType = selectedTaskType;
	}

	public String toJson()
	{
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
