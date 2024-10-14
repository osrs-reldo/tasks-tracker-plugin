package net.reldo.taskstracker.data;

import com.google.gson.annotations.Expose;
import java.time.Instant;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;
import net.reldo.taskstracker.bosses.BossData;
import net.reldo.taskstracker.data.jsondatastore.types.TaskTypeDefinition;
import net.reldo.taskstracker.data.task.TaskFromStruct;
import net.reldo.taskstracker.quests.DiaryData;
import net.reldo.taskstracker.quests.QuestData;
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
	@Setter @Expose	private HashMap<String, TaskFromStruct> tasks;

	public Export(TaskTypeDefinition taskType, String runeliteVersion, Client client, PluginManager pluginManager, ConfigManager configManager)
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
		this.taskType = taskType.getTaskJsonName();
		varbits = getVarbits(taskType);
		varps = getVarps(taskType);
	}

	private HashMap<Integer, Integer> getVarbits(TaskTypeDefinition taskType)
	{
		assert client.isClientThread();

		HashMap<Integer, Integer> varbitValueMap = new HashMap<>();
		for (int varbitId : taskType.getVarbits())
		{
			varbitValueMap.put(varbitId, client.getVarbitValue(varbitId));
		}

		return varbitValueMap;
	}

	public HashMap<Integer, Integer> getVarps(TaskTypeDefinition taskType)
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
}
