package net.reldo.taskstracker.data;

import java.util.HashMap;
import lombok.Data;
import net.reldo.taskstracker.bosses.BossData;
import net.reldo.taskstracker.quests.DiaryAndMiniQuestData;
import net.reldo.taskstracker.quests.QuestData;

@Data
public class Export
{
	public QuestData quests;
	public DiaryAndMiniQuestData diariesAndMiniQuests;
	public BossData bosses;
	public String displayName;
	public int runescapeVersion;
	public String runeliteVersion;
	public long timestamp;
	public HashMap<String, TaskSave> tasks;
	public String taskType;
	public HashMap<Integer, Integer> varbits;
	public HashMap<Integer, Integer> varps;
	public HashMap<String, Object> leagueData;
}
