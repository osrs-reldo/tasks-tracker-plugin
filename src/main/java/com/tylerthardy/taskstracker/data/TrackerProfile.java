package com.tylerthardy.taskstracker.data;

import com.tylerthardy.taskstracker.tasktypes.TaskType;
import java.time.Instant;
import java.util.HashMap;
import net.runelite.api.QuestState;

public class TrackerProfile
{
	public HashMap<TaskType, HashMap<String, TaskSave>> tasksByType = new HashMap<>();
	public HashMap<String, QuestState> quests = new HashMap<>();
	public Long timestamp;
	public String displayName;

	public TrackerProfile()
	{
		this.timestamp = Instant.now().toEpochMilli();
	}
}
