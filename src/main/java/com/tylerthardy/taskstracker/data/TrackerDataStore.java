package com.tylerthardy.taskstracker.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.tylerthardy.taskstracker.tasktypes.Task;
import java.lang.reflect.Type;
import java.util.HashMap;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;

public class TrackerDataStore
{
	private static final String PLUGIN_BASE_GROUP = "tasksTracker";
	private static final String TRACKER_SAVES_KEY = "trackerSaves";

	private ConfigManager configManager;

	private HashMap<String, HashMap<TrackerWorldType, TrackerSave>> saves;

	@Inject
	public TrackerDataStore(ConfigManager configManager)
	{
		this.configManager = configManager;
		HashMap<String, HashMap<TrackerWorldType, TrackerSave>> data = loadDataFromConfig();
		saves = data != null ? data : new HashMap<>();
	}

	private HashMap<String, HashMap<TrackerWorldType, TrackerSave>> loadDataFromConfig()
	{
		Type deserializeType = new TypeToken<HashMap<String, HashMap<TrackerWorldType, TrackerSave>>>(){}.getType();
		String jsonString = configManager.getConfiguration(PLUGIN_BASE_GROUP, TrackerDataStore.TRACKER_SAVES_KEY);
		if (jsonString == null)
		{
			// Never set before
			return null;
		}

		Gson gson = new GsonBuilder()
			.registerTypeAdapter(Task.class, new TaskDeserializer())
			.create();
		try
		{
			return gson.fromJson(jsonString, deserializeType);
		}
		catch (JsonParseException ex)
		{
			// log.error("{} json invalid. All is lost", TrackerDataStore.TRACKER_SAVES_KEY, ex);
			configManager.unsetConfiguration(PLUGIN_BASE_GROUP, TrackerDataStore.TRACKER_SAVES_KEY);
			return null;
		}
	}

	public TrackerSave load(String username, TrackerWorldType worldType)
	{
		HashMap<TrackerWorldType, TrackerSave> playerSaves = saves.get(username);
		if (playerSaves == null) return null;
		return playerSaves.get(worldType);
	}

	public void save(TrackerSave newSave)
	{
		// Check if there is a current tracker save for existing login and remove it
		if (saves.containsKey(newSave.getUsername()))
		{
			if (saves.get(newSave.getUsername()).containsKey(newSave.getWorldType()))
			{
				saves.get(newSave.getUsername()).remove(newSave.getWorldType());
			}
		}
		else
		{
			saves.put(newSave.getUsername(), new HashMap<>());
		}

		// Re-save
		saves.get(newSave.getUsername()).put(newSave.getWorldType(), newSave);

		// Set configuration
		Gson gson = new GsonBuilder()
			.registerTypeAdapter(Task.class, new TaskSerializer())
			.create();
		configManager.setConfiguration(PLUGIN_BASE_GROUP, TRACKER_SAVES_KEY, gson.toJson(saves));
	}
}
