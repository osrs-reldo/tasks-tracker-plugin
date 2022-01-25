package net.reldo.taskstracker.tasktypes.league3;

import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.TaskDataClient;
import net.reldo.taskstracker.data.TrackerDataStore;
import net.reldo.taskstracker.tasktypes.AbstractTaskManager;
import net.reldo.taskstracker.tasktypes.TaskType;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;

@Slf4j
public class League3TaskManager extends AbstractTaskManager
{
	private final Client client;

	public League3TaskManager(Client client, ClientThread clientThread, TasksTrackerPlugin plugin, TrackerDataStore trackerDataStore, TaskDataClient taskDataClient)
	{
		super(TaskType.LEAGUE_3, plugin, trackerDataStore, taskDataClient);
		this.client = client;
	}

	@Override
	public HashMap<Integer, Integer> getVarbits()
	{
		assert client.isClientThread();

		HashMap<Integer, Integer> varbitValueMap = new HashMap<>();
		for (int varbit : League3Varbits.getAllVarbitIds())
		{
			varbitValueMap.put(varbit, client.getVarbitValue(varbit));
		}

		return varbitValueMap;
	}

	@Override
	public HashMap<Integer, Integer> getVarps()
	{
		assert client.isClientThread();

		League3Varps[] varps = new League3Varps[]{
			League3Varps.LEAGUE_POINTS,
			League3Varps.SAGES_RENOWN
		};

		HashMap<Integer, Integer> varpValueMap = new HashMap<>();
		for (League3Varps varp : varps)
		{
			varpValueMap.put(varp.getVarpId(), client.getVarpValue(varp.getVarpId()));
		}

		return varpValueMap;
	}
}
