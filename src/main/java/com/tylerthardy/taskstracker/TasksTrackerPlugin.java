package com.tylerthardy.taskstracker;

import com.google.inject.Provides;
import com.tylerthardy.combattaskstracker.CombatTask;
import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.TaskType;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

@Slf4j
@PluginDescriptor(
	name = "Tasks Tracker"
)
public class TasksTrackerPlugin extends Plugin
{
	public TaskType selectedTaskType = TaskType.TEST;

	private static final Pattern completedTasksLabelRegex = Pattern.compile("Tasks Completed: \\d+/(\\d+)");
	private static final Pattern taskCompletedChatMessageRegex = Pattern.compile("Congratulations, you've completed an? (.*) combat task: (.*)\\.");;

	public LinkedHashMap<String, Integer> taskTitleColors;
	public HashSet<Task> trackedTasks = new HashSet<>();
	private TasksTrackerPluginPanel pluginPanel;
	private NavigationButton navButton;
	private Integer maxTaskCount;
	private int previousTaskCount = -1;

	@Inject
	private Client client;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private TasksTrackerConfig config;

	@Inject
	@Named("developerMode")
	boolean developerMode;

	@Provides
	TasksTrackerConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TasksTrackerConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		taskTitleColors = new LinkedHashMap<>();
		pluginPanel = new TasksTrackerPluginPanel(this, clientThread, spriteManager, developerMode);
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "panel_icon.png");
		navButton = NavigationButton.builder()
				.tooltip("Task Tracker")
				.icon(icon)
				.priority(5)
				.panel(pluginPanel)
				.build();

		clientToolbar.addNavigation(navButton);

		log.info("Combat Tasks Tracker started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		log.info("Combat Tasks Tracker stopped!");
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage) {
		handleOnChatMessage(chatMessage);
	}
	private void handleOnChatMessage(ChatMessage chatMessage) {
		// handle completion messages per task type
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged) {
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired scriptPostFired)
	{
		handleScriptPostFired(scriptPostFired);
	}
	private void handleScriptPostFired(ScriptPostFired scriptPostFired)
	{
		// handle widget scripts per task type
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		handleOnGameStateChanged(gameStateChanged);
	}
	private void handleOnGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			taskTitleColors = new LinkedHashMap<>();
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		handleOnWidgetLoaded(widgetLoaded);
	}
	private void handleOnWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		// do widget stuff per task type
	}
}
