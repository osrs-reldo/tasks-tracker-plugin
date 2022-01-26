package net.reldo.taskstracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.data.Export;
import net.reldo.taskstracker.data.LongSerializer;
import net.reldo.taskstracker.data.TaskDataClient;
import net.reldo.taskstracker.data.TaskSave;
import net.reldo.taskstracker.data.TrackerDataStore;
import net.reldo.taskstracker.data.reldo.ReldoImport;
import net.reldo.taskstracker.panel.TasksTrackerPluginPanel;
import net.reldo.taskstracker.tasktypes.AbstractTaskManager;
import net.reldo.taskstracker.tasktypes.Task;
import net.reldo.taskstracker.tasktypes.TaskType;
import net.reldo.taskstracker.tasktypes.combattask.CombatTaskManager;
import net.reldo.taskstracker.tasktypes.combattask.CombatTaskVarps;
import net.reldo.taskstracker.tasktypes.league3.League3TaskManager;
import net.reldo.taskstracker.tasktypes.league3.League3TaskVarps;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneScapeProfileType;
import net.runelite.client.eventbus.Subscribe;
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

	@Inject private TaskDataClient taskDataClient;
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
		// Load task managers
		for (TaskType taskType : TaskType.values())
		{
			AbstractTaskManager taskManager = getTaskTypeManager(taskType);
			if (taskManager == null)
			{
				continue;
			}
			taskManager.loadTaskSourceData();
			taskManagers.put(taskType, taskManager);
		}

		pluginPanel = new TasksTrackerPluginPanel(this, clientThread, spriteManager, skillIconManager);

		boolean isLoggedIn = isLoggedInState(client.getGameState());
		if (isLoggedIn)
		{
			loadProfile();
			forceVarbitUpdate();
		}
		SwingUtilities.invokeLater(() -> pluginPanel.setLoggedIn(isLoggedIn));

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

	private void loadProfile()
	{
		shouldGetName = true;
		trackerDataStore.loadProfile();

		TaskType selectedType = trackerDataStore.currentData.settings.selectedTaskType;
		setSelectedTaskType(selectedType != null ? selectedType : TaskType.COMBAT);

		for (TaskType taskType : TaskType.values())
		{
			taskManagers.get(taskType).applyTrackerSave();
		}
		pluginPanel.redraw();
	}

	private void forceVarbitUpdate()
	{
		List<Integer> allVarbitIds = new ArrayList<>();
		allVarbitIds.addAll(League3TaskVarps.getIdToVarpMap().keySet());
		allVarbitIds.addAll(CombatTaskVarps.getIdToVarpMap().keySet());
		allVarbitIds.forEach(id -> {
			VarbitChanged spoofedChange = new VarbitChanged();
			spoofedChange.setIndex(id);
			onVarbitChanged(spoofedChange);
		});
	}

	@Override
	protected void shutDown() throws Exception
	{
		pluginPanel = null;
		taskManagers = new HashMap<>();
		clientToolbar.removeNavigation(navButton);
		log.info("Tasks Tracker stopped!");
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		int index = varbitChanged.getIndex();
		processTaskVarp(index);
	}

	private void processTaskVarp(int index)
	{
		int ordinal = -1;

		League3TaskVarps leagueVarp = League3TaskVarps.getIdToVarpMap().get(index);
		if (leagueVarp != null)
		{
			ordinal = leagueVarp.ordinal();
		}

		CombatTaskVarps combatTaskVarp = CombatTaskVarps.getIdToVarpMap().get(index);
		if (combatTaskVarp != null)
		{
			ordinal = combatTaskVarp.ordinal();
		}

		if (ordinal < 0)
		{
			return;
		}

		BigInteger varpValue = BigInteger.valueOf(client.getVarpValue(index));
		int minTaskId = ordinal * 32;
		int maxTaskId = minTaskId + 31;
		int taskProgressEnumIndex = minTaskId / 32;
		log.debug("Varp {} = {}", index, varpValue);

		for (int i = minTaskId; i <= maxTaskId; i++)
		{
			boolean isTaskVarbitCompleted;
			int bitIndex = i % 32;
			try
			{
				isTaskVarbitCompleted = varpValue.testBit(bitIndex);
			}
			catch (IllegalArgumentException ex)
			{
				log.error("League 3 task progress enum not found {}", taskProgressEnumIndex, ex);
				isTaskVarbitCompleted = false;
			}

			Task foundTask = null;
			for (Task task : taskManagers.get(TaskType.LEAGUE_3).tasks)
			{
				if (task.getId() == i)
				{
					foundTask = task;
					break;
				}
			}

			if (foundTask == null)
			{
				continue;
			}

			log.debug("{}:{}:{}:{}", taskProgressEnumIndex, i, foundTask.getName(), isTaskVarbitCompleted);
			foundTask.setCompleted(isTaskVarbitCompleted);
			Task finalFoundTask = foundTask; // FIXME: foundTask can be final somehow
			SwingUtilities.invokeLater(() -> pluginPanel.refresh(finalFoundTask));
		}
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
				loadProfile();
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

	public void setSelectedTaskType(TaskType type)
	{
		selectedTaskType = type;
		trackerDataStore.currentData.settings.selectedTaskType = type;
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
			return new CombatTaskManager(client, clientThread, this, trackerDataStore, taskDataClient);
		}
		if (type == TaskType.LEAGUE_3)
		{
			return new League3TaskManager(client, clientThread, this, trackerDataStore, taskDataClient);
		}
		return null;
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
		JOptionPane optionPane = new JOptionPane("Paste import data into the text field below to import task tracker data.", JOptionPane.INFORMATION_MESSAGE);
		optionPane.setWantsInput(true);
		JDialog inputDialog = optionPane.createDialog("Import Tasks Input");
		inputDialog.setAlwaysOnTop(true);
		inputDialog.setVisible(true);

		String json = "";
		ReldoImport reldoImport;
		try
		{
			json = (String) optionPane.getInputValue();
			reldoImport = ReldoImport.fromJson(json);
		}
		catch (Exception ex)
		{
			showMessageBox("Import Tasks Error", "There was an issue importing task tracker data. " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
			log.error("There was an issue importing task tracker data.", ex);
			log.info("reldoImport json: {}", json);
			return;
		}

		optionPane = new JOptionPane("Importing tasks will overwrite task tracker settings and cannot be undone. Are you sure you want to import tasks?", JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION);
		JDialog confirmDialog = optionPane.createDialog("Import Tasks Overwrite Confirmation");
		confirmDialog.setAlwaysOnTop(true);
		confirmDialog.setVisible(true);

		Object selectedValue = optionPane.getValue();
		if(selectedValue == null) return;

		if (selectedValue.equals(JOptionPane.YES_OPTION))
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
			Export export = new Export(taskType, runeliteVersion, client, pluginManager, configManager);

			// TODO: This is a holdover for tasks until the web is ready to accept varbits
			// TODO: We already export the varbits, so ready to go
			HashMap<String, TaskSave> taskSaves = trackerDataStore.currentData.tasksByType.get(taskType);
			if (taskType == TaskType.LEAGUE_3)
			{
				HashMap<String, TaskSave> tasksById = new HashMap<>();
				taskSaves.forEach((key, value) -> tasksById.put(String.valueOf(value.getId()), value));
				export.setTasks(tasksById);
			}
			else
			{
				export.setTasks(taskSaves);
			}

			return gson.toJson(export);
		}
	}

	private static void showMessageBox(final String title, final String message, int messageType)
	{
		SwingUtilities.invokeLater(() -> {
			JOptionPane optionPane = new JOptionPane(message, messageType);
			JDialog dialog = optionPane.createDialog(title);
			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);
		});
	}
}
