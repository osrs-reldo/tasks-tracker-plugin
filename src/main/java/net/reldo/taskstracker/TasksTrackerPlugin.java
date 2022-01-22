package net.reldo.taskstracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.bosses.BossData;
import net.reldo.taskstracker.data.Export;
import net.reldo.taskstracker.data.LongSerializer;
import net.reldo.taskstracker.data.TaskSave;
import net.reldo.taskstracker.data.TrackerDataStore;
import net.reldo.taskstracker.data.reldo.ReldoImport;
import net.reldo.taskstracker.panel.TasksTrackerPluginPanel;
import net.reldo.taskstracker.quests.DiaryAndMiniQuestData;
import net.reldo.taskstracker.quests.QuestData;
import net.reldo.taskstracker.tasktypes.AbstractTaskManager;
import net.reldo.taskstracker.tasktypes.Task;
import net.reldo.taskstracker.tasktypes.TaskType;
import net.reldo.taskstracker.tasktypes.combattask.CombatTaskManager;
import net.reldo.taskstracker.tasktypes.generic.GenericTaskManager;
import net.reldo.taskstracker.tasktypes.league3.League3TaskManager;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneScapeProfileType;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Tasks Tracker"
)
public class TasksTrackerPlugin extends Plugin
{
	public int[] playerSkills;
	public HashMap<TaskType, AbstractTaskManager> taskManagers = new HashMap<>();

	public TaskType selectedTaskType;
	public String taskTextFilter;
	public boolean isIncompleteFilter = true;
	public boolean isCompleteFilter = true;
	public boolean isIgnoredFilter = false;
	public boolean isNotIgnoredFilter = true;
	public boolean isTrackedFilter = true;
	public boolean isUntrackedFilter = true;

	public TasksTrackerPluginPanel pluginPanel;

	private NavigationButton navButton;

	@Inject	@Named("runelite.version") private String runeliteVersion;
	@Inject	private Client client;
	@Inject	private SpriteManager spriteManager;
	@Inject	private PluginManager pluginManager;
	@Inject	private ConfigManager configManager;
	@Inject	private SkillIconManager skillIconManager;
	@Inject	private ClientToolbar clientToolbar;
	@Inject	private ClientThread clientThread;
	@Inject	private ChatMessageManager chatMessageManager;
	@Inject	private TasksTrackerConfig config;

	@Inject private TrackerDataStore trackerDataStore;
	private boolean shouldGetName;
	private RuneScapeProfileType currentProfileType;

	@Provides
	TasksTrackerConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TasksTrackerConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		pluginPanel = new TasksTrackerPluginPanel(this, clientThread, spriteManager, skillIconManager);

		SwingUtilities.invokeLater(() -> pluginPanel.setLoggedIn(isLoggedInState(client.getGameState())));

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "panel_icon.png");
		navButton = NavigationButton.builder()
			.tooltip("Task Tracker")
			.icon(icon)
			.priority(5)
			.panel(pluginPanel)
			.build();
		clientToolbar.addNavigation(navButton);

		log.info("Tasks Tracker started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		pluginPanel = null;
		clientToolbar.removeNavigation(navButton);
		log.info("Tasks Tracker stopped!");
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		handleOnChatMessage(chatMessage);
	}

	private void handleOnChatMessage(ChatMessage chatMessage)
	{
		taskManagers.values().forEach(tm -> tm.handleChatMessage(chatMessage));
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		handleOnGameStateChanged(gameStateChanged);
	}

	private void handleOnGameStateChanged(GameStateChanged gameStateChanged)
	{
		// FIXME: This entire logic being wrapped in invokeLater is a smell
		SwingUtilities.invokeLater(() -> {
			GameState newGameState = gameStateChanged.getGameState();
			RuneScapeProfileType newProfileType = RuneScapeProfileType.getCurrent(client);

			pluginPanel.setLoggedIn(isLoggedInState(newGameState));

			if (newGameState == GameState.LOGGING_IN || (isLoggedInState(newGameState) && currentProfileType != newProfileType))
			{
				taskManagers.clear();
				trackerDataStore.loadProfile();
				shouldGetName = true;
				TaskType selectedType = trackerDataStore.currentData.settings.selectedTaskType;
				setSelectedTaskType(selectedType != null ? selectedType : TaskType.COMBAT);
				pluginPanel.redraw();
			}

			if (!isLoggedInState(newGameState))
			{
				taskManagers.clear();
			}

			currentProfileType = newProfileType;
		});
	}

	private String getDisplayName()
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null)
		{
			return null;
		}
		return localPlayer.getName();
	}

	private boolean isLoggedInState(GameState gameState)
	{
		return gameState != GameState.LOGIN_SCREEN && gameState != GameState.LOGIN_SCREEN_AUTHENTICATOR;
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		handleOnGameTick(gameTick);
	}

	private void handleOnGameTick(GameTick gameTick)
	{
		int[] newSkills = client.getRealSkillLevels();
		boolean changed = !Arrays.equals(playerSkills, newSkills);
		if (changed)
		{
			playerSkills = client.getRealSkillLevels();
			SwingUtilities.invokeLater(() -> pluginPanel.refresh(null));
		}

		if (shouldGetName)
		{
			trackerDataStore.currentData.settings.displayName = getDisplayName();
			shouldGetName = false;
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		handleOnWidgetLoaded(widgetLoaded);
	}

	private void handleOnWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		taskManagers.values().forEach(tm -> tm.handleOnWidgetLoaded(widgetLoaded));
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired scriptPostFired)
	{
		handleOnScriptPostFired(scriptPostFired);
	}

	private void handleOnScriptPostFired(ScriptPostFired scriptPostFired)
	{
		taskManagers.values().forEach(tm -> tm.handleOnScriptPostFired(scriptPostFired));
	}

	public void setSelectedTaskType(TaskType type)
	{
		selectedTaskType = type;
		trackerDataStore.currentData.settings.selectedTaskType = type;
		if (!taskManagers.containsKey(type))
		{
			taskManagers.put(type, getTaskTypeManager(type));
		}
	}

	public void refresh()
	{
		taskManagers.get(selectedTaskType).refresh(null);
	}

	public void sendChatMessage(String chatMessage, Color color)
	{
		final String message = new ChatMessageBuilder()
			.append(color, "Task Tracker: ")
			.append(color, chatMessage)
			.build();

		chatMessageManager.queue(
			QueuedMessage.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage(message)
				.build());
	}

	private AbstractTaskManager getTaskTypeManager(TaskType type)
	{
		if (type == TaskType.COMBAT)
		{
			return new CombatTaskManager(client, clientThread, this, trackerDataStore);
		}
		if (type == TaskType.LEAGUE_3)
		{
			return new League3TaskManager(client, clientThread, this, trackerDataStore);
		}
		return new GenericTaskManager(type, this, trackerDataStore);
	}

	public void trackTask(Task task)
	{
		// TODO: Move this responsibility; not correct to be here
		trackerDataStore.saveTask(task);
	}

	public void ignoreTask(Task task)
	{
		// TODO: Move this responsibility; not correct to be here
		trackerDataStore.saveTask(task);
	}

	public void openImportJsonDialog()
	{
		String json = JOptionPane.showInputDialog(null,
			"Paste import data into the text field below to import task tracker data.",
			"Import Tasks Input",
			JOptionPane.INFORMATION_MESSAGE);

		ReldoImport reldoImport;
		try
		{
			reldoImport = ReldoImport.fromJson(json);
		}
		catch (Exception ex)
		{
			showMessageBox("Import Tasks Error", "There was an issue importing task tracker data. " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
			log.error("There was an issue importing task tracker data.", ex);
			log.info("reldoImport json: {}", json);
			return;
		}

		if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null,
			"Importing tasks will overwrite task tracker settings and cannot be undone. Are you sure you want to import tasks?",
			"Import Tasks Overwrite Confirmation",
			JOptionPane.YES_NO_OPTION))
		{
			trackerDataStore.importTasksFromReldo(reldoImport, (League3TaskManager) taskManagers.get(TaskType.LEAGUE_3));
			pluginPanel.redraw();
		}
	}

	public void copyJsonToClipboard(TaskType taskType)
	{
		clientThread.invokeLater(() -> {
			String exportJson = exportToJson(taskType);
			final StringSelection stringSelection = new StringSelection(exportJson);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

			showMessageBox(
				"Data Exported!",
				"Exported " + taskType.getDisplayString() + " data copied to clipboard!",
				JOptionPane.INFORMATION_MESSAGE);
		});
	}

	private String exportToJson(TaskType taskType)
	{
		Gson gson = new GsonBuilder()
			.registerTypeAdapter(float.class, new LongSerializer())
			.create();

		if (taskType == null)
		{
			return gson.toJson(trackerDataStore.currentData);
		}
		else
		{
			Export export = new Export();
			export.setQuests(new QuestData(client));
			export.setDiariesAndMiniQuests(new DiaryAndMiniQuestData(client));
			export.setBosses(new BossData(pluginManager, configManager));
			export.setDisplayName(trackerDataStore.currentData.settings.displayName);
			export.setRunescapeVersion(client.getRevision());
			export.setRuneliteVersion(runeliteVersion);
			export.setTimestamp(Instant.now().toEpochMilli());
			export.setTaskType(taskType.name());
			export.setVarbits(taskManagers.get(selectedTaskType).getVarbits());
			export.setVarps(taskManagers.get(selectedTaskType).getVarps());

			// TODO: Hello God, I am so sorry for this code. I will clean it up.
			// TODO: Grab ids for other task types and skip this kludge between string/int
			HashMap<String, TaskSave> taskSaves = trackerDataStore.currentData.tasksByType.get(taskType);
			if (taskType == TaskType.LEAGUE_3)
			{
				HashMap<String, TaskSave> tasksById = new HashMap<>();
				taskSaves.forEach((key, value) -> tasksById.put(String.valueOf(value.getId()), value));
				export.setTasks(tasksById);
			} else {
				export.setTasks(trackerDataStore.currentData.tasksByType.get(taskType));
			}

			return gson.toJson(export);
		}
	}

	private static void showMessageBox(final String title, final String message, int messageType)
	{
		SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, message, title, messageType));
	}
}
