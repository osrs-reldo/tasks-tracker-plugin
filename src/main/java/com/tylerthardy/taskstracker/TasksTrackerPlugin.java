package com.tylerthardy.taskstracker;

import com.google.inject.Provides;
import com.tylerthardy.taskstracker.data.TrackerDataStore;
import com.tylerthardy.taskstracker.data.TrackerSave;
import com.tylerthardy.taskstracker.data.TrackerWorldType;
import com.tylerthardy.taskstracker.panel.TasksTrackerPluginPanel;
import com.tylerthardy.taskstracker.tasktypes.AbstractTaskManager;
import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.TaskType;
import com.tylerthardy.taskstracker.tasktypes.combattask.CombatTaskManager;
import com.tylerthardy.taskstracker.tasktypes.generic.GenericTaskManager;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
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
	public boolean isIncompleteFilter;
	public TrackerSave latestDisplayedData;

	public TasksTrackerPluginPanel pluginPanel;

	private NavigationButton navButton;

	@Inject	private Client client;
	@Inject	private SpriteManager spriteManager;
	@Inject	private SkillIconManager skillIconManager;
	@Inject	private ClientToolbar clientToolbar;
	@Inject	private ClientThread clientThread;
	@Inject	private ChatMessageManager chatMessageManager;
	@Inject	private TasksTrackerConfig config;

	@Inject private TrackerDataStore trackerDataStore;

	@Provides
	TasksTrackerConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TasksTrackerConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		pluginPanel = new TasksTrackerPluginPanel(this, clientThread, spriteManager, skillIconManager);
		pluginPanel.setLoggedIn(true);//isLoggedInState(client.getGameState()));
		trackerDataStore.load("gangrenepotato@googlemail.com", TrackerWorldType.DEFAULT);

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
	public void onChatMessage(ChatMessage chatMessage) {
		handleOnChatMessage(chatMessage);
	}
	private void handleOnChatMessage(ChatMessage chatMessage) {
		taskManagers.values().forEach(tm -> tm.handleChatMessage(chatMessage));
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged) {
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		handleOnGameStateChanged(gameStateChanged);
	}
	private void handleOnGameStateChanged(GameStateChanged gameStateChanged)
	{
		SwingUtilities.invokeLater(() -> {
			pluginPanel.setLoggedIn(isLoggedInState(gameStateChanged.getGameState()));
		});

		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			TrackerWorldType worldType = TrackerWorldType.forWorld(client.getWorldType());
			trackerDataStore.load(client.getUsername(), worldType);
		}
	}

	private boolean isLoggedInState(GameState gameState)
	{
		return gameState != GameState.LOGIN_SCREEN && gameState != GameState.LOGIN_SCREEN_AUTHENTICATOR && gameState != GameState.LOGGING_IN;
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

	public void setSelectedTaskType(TaskType type)
	{
		selectedTaskType = type;
		// FIXME: This is doing double duty to the task loader to store all tasks in a cache
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
		return new GenericTaskManager(type, this, trackerDataStore);
	}

	public void trackTask(Task task)
	{
		// TODO: Move this responsibility; not correct to be here
		trackerDataStore.saveTask(task);
	}

//	private void handleTrackerData(TrackerSave newData, Task task)
//	{
//		boolean shouldRedraw = isDifferentIdentityThanLast(newData);
//		boolean shouldUpdatePanel = shouldRedraw || task != null;
//		if (shouldUpdatePanel) {
//			newData.tasks.forEach((taskType, tasks) -> {
//				if (taskManagers.get(taskType) == null)
//				{
//					taskManagers.put(taskType, getTaskTypeManager(taskType));
//				}
//				taskManagers.get(taskType).tasks = tasks;
//			});
//			setSelectedTaskType(newData.getSelectedTaskType());
//		}
//		SwingUtilities.invokeLater(() -> {
//			if (shouldRedraw) {
//				pluginPanel.redraw();
//			}
//			if (shouldUpdatePanel) {
//				pluginPanel.allTasksPanel.refresh(task);
//				pluginPanel.trackedTaskListPanel.redraw();
//			}
//		});
//		latestDisplayedData = newData;
//	}
//
//	private boolean isDifferentIdentityThanLast(TrackerSave data) {
//		if (latestDisplayedData == null) {
//			return true;
//		}
//		boolean userNamesSame = latestDisplayedData.getUsername().equalsIgnoreCase(data.getUsername());
//		boolean worldTypesSame = latestDisplayedData.getWorldType() == data.getWorldType();
//		return !userNamesSame || !worldTypesSame;
//	}
}
