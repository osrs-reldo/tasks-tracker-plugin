package com.tylerthardy.taskstracker.tasktypes;

import com.tylerthardy.taskstracker.bosses.BossData;
import com.tylerthardy.taskstracker.data.TaskSave;
import com.tylerthardy.taskstracker.quests.QuestData;
import java.util.HashMap;
import lombok.Data;

@Data
public class Export
{
	public QuestData quests;
	public BossData bosses;
	public String displayName;
    public int runescapeVersion;
    public String runeliteVersion;
    public long timestamp;
    public HashMap<String, TaskSave> tasks;
    public String taskType;
    public HashMap<String, Object> leagueData;
}
