package net.reldo.taskstracker.data;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import net.reldo.taskstracker.bosses.BossData;
import net.reldo.taskstracker.quests.DiaryAndMiniQuestData;
import net.reldo.taskstracker.quests.QuestData;
import net.reldo.taskstracker.tasktypes.Task;
import net.reldo.taskstracker.tasktypes.TaskType;
import net.reldo.taskstracker.tasktypes.league3.League3TaskVarps;
import net.reldo.taskstracker.tasktypes.league3.League3Varbits;
import net.reldo.taskstracker.tasktypes.league3.League3Varps;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginManager;

@Getter
public class Export
{
	private final Client client;

	private final QuestData quests;
	private final DiaryAndMiniQuestData diariesAndMiniQuests;
	private final BossData bosses;
	private String displayName;
	private final int runescapeVersion;
	private final String runeliteVersion;
	private final long timestamp;
	private final String taskType;
	private final HashMap<Integer, Integer> varbits;
	private final HashMap<Integer, Integer> varps;
	@Setter // TODO: Setter until property is ready to deprecate when web accepts varbits
	private HashMap<String, Task> tasks;

	public Export(TaskType taskType, String runeliteVersion, Client client, PluginManager pluginManager, ConfigManager configManager)
	{
		this.client = client;
		Actor localPlayer = client.getLocalPlayer();
		if (localPlayer != null)
		{
			this.displayName = localPlayer.getName();
		}
		quests = new QuestData(client);
		diariesAndMiniQuests = new DiaryAndMiniQuestData(client);
		bosses = new BossData(pluginManager, configManager);
		runescapeVersion = client.getRevision();
		this.runeliteVersion = runeliteVersion;
		timestamp = Instant.now().toEpochMilli();
		this.taskType = taskType.name();
		varbits = getVarbits(taskType);
		varps = getVarps(taskType);
	}

	private HashMap<Integer, Integer> getVarbits(TaskType taskType)
	{
		assert client.isClientThread();

		List<Integer> varbitIds = null;
		if (taskType == TaskType.LEAGUE_3)
		{
			varbitIds = League3Varbits.getAllVarbitIds();
		}

		if (varbitIds == null)
		{
			return null;
		}

		HashMap<Integer, Integer> varbitValueMap = new HashMap<>();
		for (int varbitId : varbitIds)
		{
			varbitValueMap.put(varbitId, client.getVarbitValue(varbitId));
		}

		return varbitValueMap;
	}

	public HashMap<Integer, Integer> getVarps(TaskType taskType)
	{
		assert client.isClientThread();

		List<Integer> varpIds = null;
		if (taskType == TaskType.LEAGUE_3)
		{
			varpIds = League3Varps.getAllVarpIds();
			varpIds.addAll(League3TaskVarps.getAllVarpIds());
		}
		if (taskType == TaskType.COMBAT)
		{
			varpIds = League3Varps.getAllVarpIds();
		}

		if (varpIds == null)
		{
			return null;
		}

		HashMap<Integer, Integer> varpValueMap = new HashMap<>();
		for (int varpId : varpIds)
		{
			varpValueMap.put(varpId, client.getVarpValue(varpId));
		}

		return varpValueMap;
	}
}
