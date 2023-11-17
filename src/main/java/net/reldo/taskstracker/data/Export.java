package net.reldo.taskstracker.data;

import com.google.gson.annotations.Expose;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import net.reldo.taskstracker.bosses.BossData;
import net.reldo.taskstracker.quests.DiaryData;
import net.reldo.taskstracker.quests.QuestData;
import net.reldo.taskstracker.tasktypes.Task;
import net.reldo.taskstracker.tasktypes.TaskType;
import net.reldo.taskstracker.tasktypes.league3.League3TaskVarps;
import net.reldo.taskstracker.tasktypes.league3.League3Varbits;
import net.reldo.taskstracker.tasktypes.league3.League3Varps;
import net.reldo.taskstracker.tasktypes.league4.League4Varbits;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginManager;

@Getter
public class Export
{
	private final Client client;

	@Expose	private final QuestData quests;
	@Expose	private final DiaryData diaries;
	@Expose	private final BossData bosses;
	@Expose	private String displayName;
	@Expose	private final int runescapeVersion;
	@Expose	private final String runeliteVersion;
	@Expose	private final long timestamp;
	@Expose	private final String taskType;
	@Expose	private final HashMap<Integer, Integer> varbits;
	@Expose	private final HashMap<Integer, Integer> varps;
	// TODO: Setter until property is ready to deprecate when web accepts varbits
	@Setter @Expose	private HashMap<String, Task> tasks;

	public Export(TaskType taskType, String runeliteVersion, Client client, PluginManager pluginManager, ConfigManager configManager)
	{
		this.client = client;
		Actor localPlayer = client.getLocalPlayer();
		if (localPlayer != null)
		{
			this.displayName = localPlayer.getName();
		}
		quests = new QuestData(client);
		diaries = new DiaryData(client);
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
		if (taskType == TaskType.LEAGUE_4)
		{
			varbitIds = League4Varbits.getAllVarbitIds();
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
