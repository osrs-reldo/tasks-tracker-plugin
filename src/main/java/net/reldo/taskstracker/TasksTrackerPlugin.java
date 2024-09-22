package net.reldo.taskstracker;

import com.google.gson.Gson;
import com.google.inject.Binder;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.data.Export;
import net.reldo.taskstracker.data.LongSerializer;
import net.reldo.taskstracker.data.TaskDataClient;
import net.reldo.taskstracker.data.TrackerDataStore;
import net.reldo.taskstracker.data.jsondatastore.reader.DataStoreReader;
import net.reldo.taskstracker.data.jsondatastore.reader.FileDataStoreReader;
import net.reldo.taskstracker.data.jsondatastore.types.TaskFromStruct;
import net.reldo.taskstracker.data.jsondatastore.types.definitions.TaskTypeDefinition;
import net.reldo.taskstracker.data.reldo.ReldoImport;
import net.reldo.taskstracker.data.task.TaskService;
import net.reldo.taskstracker.panel.TaskPanelFactory;
import net.reldo.taskstracker.panel.TaskTrackerPanelModule;
import net.reldo.taskstracker.panel.TasksTrackerPluginPanel;
import net.reldo.taskstracker.tasktypes.Task;
import net.reldo.taskstracker.tasktypes.TaskManager;
import net.reldo.taskstracker.tasktypes.TaskType;
import net.reldo.taskstracker.tasktypes.TasksSummary;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
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

	private static final long VARP_UPDATE_THROTTLE_DELAY_MS = 7 * 1000;

	private boolean forceUpdateVarpsFlag = false;
	private Set<Integer> varpIdsToUpdate = new HashSet<>();
	private long lastVarpUpdate = 0;
	private NavigationButton navButton;
	private RuneScapeProfileType currentProfileType;

	@Inject
	@Named("runelite.version")
	private String runeliteVersion;
	@Inject
	private Gson gson;
	@Inject
	private Client client;
	@Inject
	private SpriteManager spriteManager;
	@Inject
	private PluginManager pluginManager;
	@Inject
	private SkillIconManager skillIconManager;
	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private ClientThread clientThread;
	@Inject
	private ChatMessageManager chatMessageManager;
	@Getter
	@Inject
	private ConfigManager configManager;
	@Getter
	@Inject
	private TasksTrackerConfig config;

	@Inject
	private TaskDataClient taskDataClient;
	@Inject
	private TrackerDataStore trackerDataStore;

	@Inject
	private TaskService taskService;
	@Inject
	private net.reldo.taskstracker.data.jsondatastore.TaskDataClient taskDataClientV2;
	@Inject
	private TaskPanelFactory taskPanelFactory;
	@Override
	public void configure(Binder binder)
	{
		binder.bind(DataStoreReader.class).to(FileDataStoreReader.class);
		binder.install(new TaskTrackerPanelModule());
		super.configure(binder);
	}

	@Provides
	TasksTrackerConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TasksTrackerConfig.class);
	}


	@Override
	protected void startUp()
	{
		try
		{
			TaskTypeDefinition combatTaskType = this.taskDataClientV2.getTaskTypes().get("COMBAT");
			this.taskService.setTaskType(combatTaskType);
		}
		catch (Exception ex)
		{
			log.error("error using new task services", ex);
		}

		this.forceUpdateVarpsFlag = false;

		// TODO: inject panel
		this.pluginPanel = new TasksTrackerPluginPanel(this, this.config, this.clientThread, this.spriteManager, this.skillIconManager, this.taskPanelFactory);

		boolean isLoggedIn = this.isLoggedInState(this.client.getGameState());
		this.pluginPanel.setLoggedIn(isLoggedIn);

		// Load task managers
		for (TaskType taskType : TaskType.values())
		{
			TaskManager taskManager = new TaskManager(taskType, this.taskDataClient);
			this.taskManagers.put(taskType, taskManager);

			taskManager.asyncLoadTaskSourceData().thenAccept((tasks) -> {
				// If the player is already logged in when the plugin is started, treat like a new login
				if (isLoggedIn && taskType == this.config.taskType())
				{
					this.forceUpdateVarpsFlag = true;
				}
			});
		}

		final BufferedImage icon = ImageUtil.loadImageResource(this.getClass(), "panel_icon.png");
		this.navButton = NavigationButton.builder()
			.tooltip("Task Tracker")
			.icon(icon)
			.priority(5)
			.panel(this.pluginPanel)
			.build();
		this.clientToolbar.addNavigation(this.navButton);

		log.info("Tasks Tracker started!");
	}

	@Override
	protected void shutDown()
	{
		this.pluginPanel = null;
		this.taskManagers = new HashMap<>();
		this.clientToolbar.removeNavigation(this.navButton);
		log.info("Tasks Tracker stopped!");
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		if (this.forceUpdateVarpsFlag)
		{
			// Force update is coming on next game tick, so ignore varbit change events
			return;
		}
		int varpId = varbitChanged.getVarpId();
		if (!this.taskService.getCurrentTaskTypeVarps().contains(varpId))
		{
			return;
		}
		this.varpIdsToUpdate.add(varbitChanged.getVarpId());
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (!configChanged.getGroup().equals(CONFIG_GROUP_NAME))
		{
			return;
		}
		log.debug("onConfigChanged {} {}", configChanged.getKey(), configChanged.getNewValue());
		if (configChanged.getKey().equals("untrackUponCompletion") && this.config.untrackUponCompletion())
		{
			this.forceVarpUpdate();
		}

		if (configChanged.getKey().equals("filterPanelCollapsible"))
		{
			this.pluginPanel.redraw();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		log.debug("onGameStateChanged {}", gameStateChanged.getGameState().toString());
		GameState newGameState = gameStateChanged.getGameState();
		RuneScapeProfileType newProfileType = RuneScapeProfileType.getCurrent(this.client);

		SwingUtilities.invokeLater(() -> this.pluginPanel.setLoggedIn(this.isLoggedInState(newGameState)));

		// Logged in
		if (newGameState == GameState.LOGGING_IN)
		{
			this.forceUpdateVarpsFlag = true;
		}
		// Changed game mode
		if (this.isLoggedInState(newGameState) && this.currentProfileType != null && this.currentProfileType != newProfileType)
		{
			this.forceUpdateVarpsFlag = true;
		}

		this.currentProfileType = newProfileType;
	}

	private boolean isLoggedInState(GameState gameState)
	{
		return gameState == GameState.LOGGED_IN || gameState == GameState.HOPPING || gameState == GameState.LOADING;
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (this.forceUpdateVarpsFlag)
		{
			log.debug("loginFlag game tick");
			this.loadSavedTaskTypeData(this.config.taskType());
			this.forceVarpUpdate();
			SwingUtilities.invokeLater(() -> this.pluginPanel.redraw());
			this.forceUpdateVarpsFlag = false;
		}

		// Flush throttled varp updates
		long currentTimeEpoch = System.currentTimeMillis();
		if (currentTimeEpoch - this.lastVarpUpdate > VARP_UPDATE_THROTTLE_DELAY_MS)
		{
			this.flushVarpUpdates(this.varpIdsToUpdate);
			this.varpIdsToUpdate = new HashSet<>();
			this.lastVarpUpdate = currentTimeEpoch;
		}

		int[] newSkills = this.client.getRealSkillLevels();
		boolean changed = !Arrays.equals(this.playerSkills, newSkills);
		if (changed)
		{
			this.playerSkills = this.client.getRealSkillLevels();
			SwingUtilities.invokeLater(() -> this.pluginPanel.refresh(null));
		}
	}

	public void refresh()
	{
		this.pluginPanel.refresh(null);
	}

	public void saveCurrentTaskData()
	{
		log.debug("saveCurrentTaskData");
		this.trackerDataStore.saveTaskTypeToConfig(this.config.taskType(), this.taskManagers.get(this.config.taskType()).tasks.values());
	}

	public void openImportJsonDialog()
	{
		JOptionPane optionPane = new JOptionPane("Paste import data into the text field below to import task tracker data.", JOptionPane.INFORMATION_MESSAGE);
		optionPane.setWantsInput(true);
		JDialog inputDialog = optionPane.createDialog(this.pluginPanel, "Import Tasks Input");
		inputDialog.setAlwaysOnTop(true);
		inputDialog.setVisible(true);

		if (optionPane.getInputValue().equals("") || optionPane.getInputValue().equals("uninitializedValue"))
		{
			this.showMessageBox("Import Tasks Error", "Input was empty so no data has been imported.", JOptionPane.ERROR_MESSAGE, false);
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
			this.showMessageBox("Import Tasks Error", "There was an issue importing task tracker data. " + ex.getMessage(), JOptionPane.ERROR_MESSAGE, false);
			log.error("There was an issue importing task tracker data.", ex);
			log.debug("reldoImport json: {}", json);
			return;
		}

		optionPane = new JOptionPane("Importing tasks will overwrite task tracker settings and cannot be undone. Are you sure you want to import tasks?", JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION);
		JDialog confirmDialog = optionPane.createDialog(this.pluginPanel, "Import Tasks Overwrite Confirmation");
		confirmDialog.setAlwaysOnTop(true);
		confirmDialog.setVisible(true);

		Object selectedValue = optionPane.getValue();
		if (selectedValue == null)
		{
			return;
		}

		if (selectedValue.equals(JOptionPane.YES_OPTION))
		{
			// FIXME: Hardcoded for league 4 only
			reldoImport.getTasks().forEach((id, reldoTaskSave) -> {
				Task task = this.taskManagers.get(TaskType.LEAGUE_4).tasks.get(id);
				task.loadReldoSave(reldoTaskSave);
			});
			this.trackerDataStore.saveTaskTypeToConfig(TaskType.LEAGUE_4, this.taskManagers.get(TaskType.LEAGUE_4).tasks.values());
			this.pluginPanel.redraw();
		}
	}

	public void sendTotalsToChat()
	{
		TasksSummary summary = this.taskManagers.get(this.config.taskType()).getSummary();
		int trackedTasks = summary.trackedTasksCount;
		int trackedPoints = summary.trackedTasksPoints;

		final String message = new ChatMessageBuilder()
			.append(Color.BLACK, String.format("Task Tracker - Tracked Tasks: %s | Tracked Points: %s", trackedTasks, trackedPoints))
			.build();

		this.chatMessageManager.queue(
			QueuedMessage.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage(message)
				.build());
	}

	public void copyJsonToClipboard(TaskType taskType)
	{
		this.clientThread.invokeLater(() -> {
			String exportJson = this.exportToJson(taskType);
			final StringSelection stringSelection = new StringSelection(exportJson);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

			String message = "Exported " + taskType.getDisplayString() + " data copied to clipboard!";

			this.showMessageBox("Data Exported!", message, JOptionPane.INFORMATION_MESSAGE, true);
		});
	}

	private void loadSavedTaskTypeData(TaskType taskType)
	{
		log.debug("loadSavedTaskTypeData {}", taskType.name());
		HashMap<Integer, Task> taskData = this.trackerDataStore.loadTaskTypeFromConfig(taskType);

		this.taskManagers.get(taskType).applyTrackerSave(taskData);

		this.trackerDataStore.saveTaskTypeToConfig(taskType, this.taskManagers.get(taskType).tasks.values());
	}

	private void forceVarpUpdate()
	{
		log.debug("forceVarpUpdate");
		this.processVarpAndUpdateTasks(null).thenAccept((processed) -> {
			if (processed)
			{
				this.saveCurrentTaskData();
			}
		});
	}

	private void flushVarpUpdates(Set<Integer> varpIds)
	{
		log.debug("Flushing throttled varp updates {}", varpIds);
		varpIds.forEach((id) -> this.processVarpAndUpdateTasks(id).thenAccept(processed -> {
			if (processed)
			{
				this.saveCurrentTaskData();
			}
		}));
	}

	private CompletableFuture<Boolean> processTaskStatus(Task taskV1, TaskFromStruct taskV2) {
		CompletableFuture<Boolean> future = new CompletableFuture<>();
		this.clientThread.invokeLater(() -> {
			try {
				int CA_TASK_COMPLETED_SCRIPT_ID = 4834;
//				log.debug("getting status taskFromStruct {}", taskV2.getSortId());
				this.client.runScript(CA_TASK_COMPLETED_SCRIPT_ID, taskV2.getSortId());
				boolean isTaskCompleted = this.client.getIntStack()[0] > 0;
				taskV1.setCompleted(isTaskCompleted);
				if (isTaskCompleted && this.config.untrackUponCompletion())
				{
					taskV1.setTracked(false);
				}
				future.complete(isTaskCompleted);
			} catch (Exception ex) {
				log.error("Error processing task status", ex);
				future.completeExceptionally(ex);
			}
		});
		return future;
	}

	private CompletableFuture<Boolean> processVarpAndUpdateTasks(@Nullable Integer varpId)
	{
		log.info("processVarpAndUpdateTasks: " + (varpId != null ? varpId : "all"));

		// TaskTypes for V1 and V2
		final TaskType taskTypeV1 = TaskType.COMBAT;
		if (!this.taskService.getCurrentTaskType().getName().equals("COMBAT"))
		{
			TaskTypeDefinition taskTypeV2 = this.taskService.getTaskTypes().get("COMBAT");
			this.taskService.setTaskType(taskTypeV2);
		}

		// If varpId specified, only get those tasks, otherwise get all
		List<TaskFromStruct> tasks = varpId != null ?
			this.taskService.getCurrentTasksByVarp().get(varpId) :
			this.taskService.getTasks();

		List<CompletableFuture<Boolean>> taskFutures = new ArrayList<>();
		for (TaskFromStruct taskV2 : tasks)
		{
			Task taskV1 = this.taskManagers.get(taskTypeV1).tasks.get(taskV2.getSortId());
			CompletableFuture<Boolean> taskFuture = processTaskStatus(taskV1, taskV2);
			taskFutures.add(taskFuture);
		}

		CompletableFuture<Void> allTasksFuture = CompletableFuture.allOf(taskFutures.toArray(new CompletableFuture[0]));
		return allTasksFuture.thenApply(v -> true);
	}

	private String exportToJson(TaskType taskType)
	{
		Gson gson = this.gson.newBuilder()
			.excludeFieldsWithoutExposeAnnotation()
			.registerTypeAdapter(float.class, new LongSerializer())
			.create();

		if (taskType == null)
		{
			return gson.toJson(this.taskManagers);
		}
		else
		{
			Export export = new Export(taskType, this.runeliteVersion, this.client, this.pluginManager, this.configManager);

			// TODO: This is a holdover for tasks until the web is ready to accept varbits
			// TODO: We already export the varbits, so ready to go
			HashMap<String, Task> tasksById = new HashMap<>();
			this.taskManagers.get(taskType).tasks.values().forEach((task) -> tasksById.put(String.valueOf(task.getId()), task));
			export.setTasks(tasksById);

			return gson.toJson(export);
		}
	}

	private void showMessageBox(final String title, final String message, int messageType, boolean showOpenLeagueTools)
	{
		SwingUtilities.invokeLater(() -> {
			JOptionPane optionPane;
			JDialog dialog;

			if (showOpenLeagueTools)
			{
				String[] options = {"Open OS League Tools", "Ok"};

				optionPane = new JOptionPane(message, messageType, JOptionPane.YES_NO_OPTION, null, options, options[1]);
			}
			else
			{
				optionPane = new JOptionPane(message, messageType);
			}

			dialog = optionPane.createDialog(this.pluginPanel, title);
			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);

			Object selectedValue = optionPane.getValue();
			if (selectedValue == null)
			{
				return;
			}

			if (selectedValue.equals("Open OS League Tools"))
			{
				LinkBrowser.browse("https://www.osleague.tools/tracker?open=import&tab=tasks");
			}
		});
	}
}
