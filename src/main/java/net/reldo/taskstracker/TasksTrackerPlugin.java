package net.reldo.taskstracker;

import com.google.gson.Gson;
import com.google.inject.Provides;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.data.CallbackCommand;
import net.reldo.taskstracker.data.Export;
import net.reldo.taskstracker.data.LongSerializer;
import net.reldo.taskstracker.data.TaskDataClient;
import net.reldo.taskstracker.data.TrackerDataStore;
import net.reldo.taskstracker.data.reldo.ReldoImport;
import net.reldo.taskstracker.panel.TasksTrackerPluginPanel;
import net.reldo.taskstracker.tasktypes.Task;
import net.reldo.taskstracker.tasktypes.TaskManager;
import net.reldo.taskstracker.tasktypes.TaskType;
import net.reldo.taskstracker.tasktypes.combattask.CombatTaskVarps;
import net.reldo.taskstracker.tasktypes.league3.League3TaskVarps;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
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
import net.runelite.client.util.LinkBrowser;

@Slf4j
@PluginDescriptor(
	name = "Tasks Tracker"
)
public class TasksTrackerPlugin extends Plugin
{
	public static final String CONFIG_GROUP_NAME = "tasks-tracker";

	public int[] playerSkills;
	public HashMap<TaskType, TaskManager> taskManagers = new HashMap<>();

	public String taskTextFilter;

	public TasksTrackerPluginPanel pluginPanel;

	private boolean forceUpdateVarpsFlag = false;
	private NavigationButton navButton;
	private RuneScapeProfileType currentProfileType;

	@Inject	@Named("runelite.version") private String runeliteVersion;
	@Inject private Gson gson;
	@Inject	private Client client;
	@Inject	private SpriteManager spriteManager;
	@Inject	private PluginManager pluginManager;
	@Inject	private SkillIconManager skillIconManager;
	@Inject	private ClientToolbar clientToolbar;
	@Inject	private ClientThread clientThread;
	@Inject	private ChatMessageManager chatMessageManager;
	@Getter	@Inject	private ConfigManager configManager;
	@Getter @Inject	private TasksTrackerConfig config;

	@Inject private TaskDataClient taskDataClient;
	@Inject private TrackerDataStore trackerDataStore;

	@Provides
	TasksTrackerConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TasksTrackerConfig.class);
	}

	@Override
	protected void startUp()
	{
		forceUpdateVarpsFlag = false;

		pluginPanel = new TasksTrackerPluginPanel(this, config, clientThread, spriteManager, skillIconManager);

		boolean isLoggedIn = isLoggedInState(client.getGameState());
		pluginPanel.setLoggedIn(isLoggedIn);

		// Load task managers
		for (TaskType taskType : TaskType.values())
		{
			TaskManager taskManager = new TaskManager(taskType, taskDataClient);
			taskManagers.put(taskType, taskManager);

			taskManager.asyncLoadTaskSourceData((tasks) -> {
				// If the player is already logged in when the plugin is started, treat like a new login
				if (isLoggedIn && taskType == config.taskType())
				{
					forceUpdateVarpsFlag = true;
				}
			});
		}

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
	protected void shutDown()
	{
		pluginPanel = null;
		taskManagers = new HashMap<>();
		clientToolbar.removeNavigation(navButton);
		log.info("Tasks Tracker stopped!");
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		if (forceUpdateVarpsFlag)
		{
			// Force update is coming on next game tick, so ignore varbit change events
			return;
		}
		processVarpAndUpdateTasks(varbitChanged.getVarpId(), processed -> {
			if (processed)
			{
				this.saveCurrentTaskData();
			}
		});
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (!configChanged.getGroup().equals(CONFIG_GROUP_NAME))
		{
			return;
		}
		log.debug("onConfigChanged {} {}", configChanged.getKey(), configChanged.getNewValue());
		if (configChanged.getKey().equals("untrackUponCompletion") && config.untrackUponCompletion())
		{
			forceVarpUpdate();
		}

		if (configChanged.getKey().equals("filterPanelCollapsible"))
		{
			pluginPanel.redraw();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		log.debug("onGameStateChanged {}", gameStateChanged.getGameState().toString());
		GameState newGameState = gameStateChanged.getGameState();
		RuneScapeProfileType newProfileType = RuneScapeProfileType.getCurrent(client);

		SwingUtilities.invokeLater(() -> pluginPanel.setLoggedIn(isLoggedInState(newGameState)));

		// Logged in
		if (newGameState == GameState.LOGGING_IN)
		{
			forceUpdateVarpsFlag = true;
		}
		// Changed game mode
		if (isLoggedInState(newGameState) && currentProfileType != null && currentProfileType != newProfileType)
		{
			forceUpdateVarpsFlag = true;
		}

		currentProfileType = newProfileType;
	}

	private boolean isLoggedInState(GameState gameState)
	{
		return gameState == GameState.LOGGED_IN || gameState == GameState.HOPPING || gameState == GameState.LOADING;
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (forceUpdateVarpsFlag)
		{
			log.debug("loginFlag game tick");
			loadSavedTaskTypeData(config.taskType());
			forceVarpUpdate();
			SwingUtilities.invokeLater(() -> pluginPanel.redraw());
			forceUpdateVarpsFlag = false;
		}

		int[] newSkills = client.getRealSkillLevels();
		boolean changed = !Arrays.equals(playerSkills, newSkills);
		if (changed)
		{
			playerSkills = client.getRealSkillLevels();
			SwingUtilities.invokeLater(() -> pluginPanel.refresh(null));
		}
	}

	public void refresh()
	{
		pluginPanel.refresh(null);
	}

	public void saveCurrentTaskData()
	{
		log.debug("saveCurrentTaskData");
		trackerDataStore.saveTaskTypeToConfig(config.taskType(), taskManagers.get(config.taskType()).tasks.values());
	}

	public void openImportJsonDialog()
	{
		JOptionPane optionPane = new JOptionPane("Paste import data into the text field below to import task tracker data.", JOptionPane.INFORMATION_MESSAGE);
		optionPane.setWantsInput(true);
		JDialog inputDialog = optionPane.createDialog(pluginPanel, "Import Tasks Input");
		inputDialog.setAlwaysOnTop(true);
		inputDialog.setVisible(true);

		if(optionPane.getInputValue().equals("") || optionPane.getInputValue().equals("uninitializedValue"))
		{
			showMessageBox("Import Tasks Error", "Input was empty so no data has been imported.", JOptionPane.ERROR_MESSAGE, false);
			return;
		}

		String json = "";
		ReldoImport reldoImport;
		try
		{
			json = (String) optionPane.getInputValue();
			reldoImport = this.gson.fromJson(json, ReldoImport.class);
		}
		catch (Exception ex)
		{
			showMessageBox("Import Tasks Error", "There was an issue importing task tracker data. " + ex.getMessage(), JOptionPane.ERROR_MESSAGE, false);
			log.error("There was an issue importing task tracker data.", ex);
			log.debug("reldoImport json: {}", json);
			return;
		}

		optionPane = new JOptionPane("Importing tasks will overwrite task tracker settings and cannot be undone. Are you sure you want to import tasks?", JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION);
		JDialog confirmDialog = optionPane.createDialog(pluginPanel, "Import Tasks Overwrite Confirmation");
		confirmDialog.setAlwaysOnTop(true);
		confirmDialog.setVisible(true);

		Object selectedValue = optionPane.getValue();
		if (selectedValue == null) return;

		if (selectedValue.equals(JOptionPane.YES_OPTION))
		{
			// FIXME: Hardcoded for league 4 only
			reldoImport.getTasks().forEach((id, reldoTaskSave) -> {
				Task task = taskManagers.get(TaskType.LEAGUE_4).tasks.get(id);
				task.loadReldoSave(reldoTaskSave);
			});
			trackerDataStore.saveTaskTypeToConfig(TaskType.LEAGUE_4, taskManagers.get(TaskType.LEAGUE_4).tasks.values());
			pluginPanel.redraw();
		}
	}

	public void copyJsonToClipboard(TaskType taskType)
	{
		clientThread.invokeLater(() -> {
			String exportJson = exportToJson(taskType);
			final StringSelection stringSelection = new StringSelection(exportJson);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

			String message = "Exported " + taskType.getDisplayString() + " data copied to clipboard!";

			showMessageBox("Data Exported!", message, JOptionPane.INFORMATION_MESSAGE, true);
		});
	}

	private void loadSavedTaskTypeData(TaskType taskType)
	{
		log.debug("loadSavedTaskTypeData {}", taskType.name());
		HashMap<Integer, Task> taskData = trackerDataStore.loadTaskTypeFromConfig(taskType);

		taskManagers.get(taskType).applyTrackerSave(taskData);

		trackerDataStore.saveTaskTypeToConfig(taskType, taskManagers.get(taskType).tasks.values());
	}

	private void forceVarpUpdate()
	{
		log.debug("forceVarpUpdate");
		List<Integer> allVarbitIds = new ArrayList<>();
		allVarbitIds.addAll(League3TaskVarps.getIdToVarpMap().keySet());
		allVarbitIds.addAll(CombatTaskVarps.getIdToVarpMap().keySet());
		allVarbitIds.forEach((id) -> this.processVarpAndUpdateTasks(id, processed -> {
			if (processed)
			{
				this.saveCurrentTaskData();
			}
		}));
	}

	private void processVarpAndUpdateTasks(int varpId, CallbackCommand<Boolean> resultCallback)
	{
		int ordinal = -1;
		TaskType taskType = null;

		League3TaskVarps leagueVarp = League3TaskVarps.getIdToVarpMap().get(varpId);
		if (leagueVarp != null)
		{
			ordinal = leagueVarp.ordinal();
			taskType = TaskType.LEAGUE_3;
		}

		CombatTaskVarps combatTaskVarp = CombatTaskVarps.getIdToVarpMap().get(varpId);
		if (combatTaskVarp != null)
		{
			ordinal = combatTaskVarp.ordinal();
			taskType = TaskType.COMBAT;
		}

		if (taskType == null)
		{
			resultCallback.execute(false);
			return;
		}

		HashMap<Integer, Boolean> completionById = new HashMap<>();

		int finalOrdinal = ordinal;
		TaskType finalTaskType = taskType;
		clientThread.invokeLater(() -> {
			// We don't use the VarbitChanged event value because it may not be the latest value
			// Instead we refetch the varp value
			BigInteger varpValue = BigInteger.valueOf(client.getVarpValue(varpId));
			log.debug("processVarpAndUpdateTasks {} {}", varpId, varpValue);
			int minTaskId = finalOrdinal * 32;
			int maxTaskId = minTaskId + 31;

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
					log.error("Bit test failed {} {}", varpId, bitIndex, ex);
					isTaskVarbitCompleted = false;
				}

				completionById.put(i, isTaskVarbitCompleted);
			}

			for (Map.Entry<Integer, Boolean> taskCompletion : completionById.entrySet())
			{
				int id = taskCompletion.getKey();
				boolean completed = taskCompletion.getValue();
				Task task = taskManagers.get(finalTaskType).tasks.get(id);
				if (task == null)
				{
					continue;
				}

				task.setCompleted(completed);
				if (completed && config.untrackUponCompletion())
				{
					task.setTracked(false);
				}
				SwingUtilities.invokeLater(() -> pluginPanel.refresh(task));
			}

			resultCallback.execute(true);
		});
	}

	private String exportToJson(TaskType taskType)
	{
		Gson gson = this.gson.newBuilder()
			.excludeFieldsWithoutExposeAnnotation()
			.registerTypeAdapter(float.class, new LongSerializer())
			.create();

		if (taskType == null)
		{
			return gson.toJson(taskManagers);
		}
		else
		{
			Export export = new Export(taskType, runeliteVersion, client, pluginManager, configManager);

			// TODO: This is a holdover for tasks until the web is ready to accept varbits
			// TODO: We already export the varbits, so ready to go
			HashMap<String, Task> tasksById = new HashMap<>();
			taskManagers.get(taskType).tasks.values().forEach((task) -> tasksById.put(String.valueOf(task.getId()), task));
			export.setTasks(tasksById);

			return gson.toJson(export);
		}
	}

	private void showMessageBox(final String title, final String message, int messageType, boolean showOpenLeagueTools)
	{
		SwingUtilities.invokeLater(() -> {
			JOptionPane optionPane;
			JDialog dialog;

			if(showOpenLeagueTools)
			{
				String[] options = {"Open OS League Tools", "Ok"};

				optionPane = new JOptionPane(message, messageType, JOptionPane.YES_NO_OPTION, null, options, options[1]);
			}
			else
			{
				optionPane = new JOptionPane(message, messageType);
			}

			dialog = optionPane.createDialog(pluginPanel, title);
			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);

			Object selectedValue = optionPane.getValue();
			if(selectedValue == null) return;

			if (selectedValue.equals("Open OS League Tools"))
			{
				LinkBrowser.browse("https://www.osleague.tools/tracker?open=import&tab=tasks");
			}
		});
	}
}
