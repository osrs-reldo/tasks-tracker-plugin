package net.reldo.taskstracker.data;

import com.google.gson.annotations.Expose;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import lombok.Getter;
import net.reldo.taskstracker.data.task.ConfigTaskSave;
import net.reldo.taskstracker.data.task.TaskFromStruct;
import net.reldo.taskstracker.data.task.TaskType;
import net.reldo.taskstracker.quests.DiaryData;
import net.reldo.taskstracker.quests.QuestData;
import net.runelite.api.Actor;
import net.runelite.api.Client;

@Getter
public class Export
{
	private final Client client;

	@Expose
	private final QuestData quests;
	@Expose
	private final DiaryData diaries;
	@Expose
	private String displayName;
	@Expose
	private final int runescapeVersion;
	@Expose
	private final String runeliteVersion;
	@Expose
	private final long timestamp;
	@Expose
	private final String taskType;
	@Expose
	private final HashMap<Integer, Integer> varbits;
	@Expose
	private final HashMap<Integer, Integer> varps;
	@Expose
	private final HashMap<String, ConfigTaskSave> tasks;

	public Export(TaskType taskType, List<TaskFromStruct> tasks, String runeliteVersion, Client client)
	{
		this.client = client;
		Actor localPlayer = client.getLocalPlayer();
		if (localPlayer != null)
		{
			this.displayName = localPlayer.getName();
		}
		quests = new QuestData(client);
		diaries = new DiaryData(client);
		runescapeVersion = client.getRevision();
		this.runeliteVersion = runeliteVersion;
		timestamp = Instant.now().toEpochMilli();
		this.taskType = taskType.getTaskJsonName();
		varbits = getVarbits(taskType);
		varps = getVarps(taskType);
		this.tasks = getTaskSavesById(tasks);
	}

	private HashMap<Integer, Integer> getVarbits(TaskType taskType)
	{
		assert client.isClientThread();

		HashMap<Integer, Integer> varbitValueMap = new HashMap<>();
		for (int varbitId : taskType.getVarbits())
		{
			varbitValueMap.put(varbitId, client.getVarbitValue(varbitId));
		}

		return varbitValueMap;
	}

	public HashMap<Integer, Integer> getVarps(TaskType taskType)
	{
		assert client.isClientThread();

		HashMap<Integer, Integer> varpValueMap = new HashMap<>();
		for (int varpId : taskType.getTaskVarps())
		{
			varpValueMap.put(varpId, client.getVarpValue(varpId));
		}
		for (int varpId : taskType.getOtherVarps())
		{
			varpValueMap.put(varpId, client.getVarpValue(varpId));
		}

		return varpValueMap;
	}

	public HashMap<String, ConfigTaskSave> getTaskSavesById(List<TaskFromStruct> tasks)
	{
		HashMap<String, ConfigTaskSave> taskSavesById = new HashMap<>();
		for (TaskFromStruct task : tasks)
		{
			if (task.getCompletedOn() == 0 && task.getIgnoredOn() == 0 && task.getTrackedOn() == 0)
			{
				continue;
			}
			taskSavesById.put(String.valueOf(task.getIntParam("id")), task.getSaveData());
		}
		return taskSavesById;
	}
}
