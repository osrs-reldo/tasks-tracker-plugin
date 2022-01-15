package com.tylerthardy.taskstracker.data;

import com.google.gson.Gson;
import java.util.HashMap;
import javafx.util.Pair;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;

public class TrackerDataStore
{
	private static final String PLUGIN_BASE_GROUP = "tasksTracker";
	private static final String TRACKER_SAVES_KEY = "trackerSaves";
	private static final String SNAPSHOT_LIST_KEY = "snapshotList";
	private static final String NAME_MAP_KEY = "nameMap";

	@Inject	private ConfigManager configManager;

	private final HashMap<Pair<String, TrackerWorldType>, TrackerSave> saves = new HashMap<>();

	public TrackerSave getData(String username, TrackerWorldType worldType)
	{
		return saves.get(new Pair<>(username, worldType));
	}

	public void save(TrackerSave newSave)
	{
		// Check if there is a current tracker save for existing login and remove it
		Pair<String, TrackerWorldType> key = new Pair<>(newSave.getUsername(), newSave.getWorldType());
		saves.remove(key);

		// Re-save
		saves.put(key, newSave);

		// Set configuration
		Gson gson = new Gson();
		configManager.setConfiguration(PLUGIN_BASE_GROUP, TRACKER_SAVES_KEY, gson.toJson(newSave));
	}
}
