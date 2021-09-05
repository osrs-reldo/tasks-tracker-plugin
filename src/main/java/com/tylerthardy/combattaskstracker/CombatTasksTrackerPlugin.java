package com.tylerthardy.combattaskstracker;

import com.google.inject.Provides;
import com.tylerthardy.combattaskstracker.ui.CombatTasksTrackerPluginPanel;
import com.tylerthardy.combattaskstracker.widgets.CombatTasksWidgetID;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.SpriteID;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@PluginDescriptor(
	name = "Combat Tasks Tracker"
)
public class CombatTasksTrackerPlugin extends Plugin
{
	private static final Pattern completedTasksLabelRegex = Pattern.compile("Tasks Completed: \\d+/(\\d+)");
	private static final Pattern taskCompletedChatMessageRegex = Pattern.compile("Congratulations, you've completed an? (.*) combat task: (.*)\\.");;

	private Integer maxTaskCount;
	public LinkedHashMap<String, Integer> taskTitleColors;
	public HashSet<CombatTask> trackedTasks = new HashSet<>();
	private CombatTasksTrackerPluginPanel pluginPanel;
	private NavigationButton navButton;
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
	private CombatTasksTrackerConfig config;

	@Inject
	@Named("developerMode")
	boolean developerMode;

	@Provides
	CombatTasksTrackerConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CombatTasksTrackerConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		taskTitleColors = new LinkedHashMap<>();
		pluginPanel = new CombatTasksTrackerPluginPanel(this, spriteManager, developerMode);
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "panel_icon.png");
		navButton = NavigationButton.builder()
				.tooltip("Combat Tracker")
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
		if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE) {
			return;
		}

		String strippedMessage = Text.removeFormattingTags(chatMessage.getMessage());
		Matcher m = taskCompletedChatMessageRegex.matcher(strippedMessage);
		if (!m.find()) {
			return;
		}

		String tier = m.group(1);
		String taskName = m.group(2);
		completeTask(taskName);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged) {
		if (configChanged.getKey().equals("sprite_id"))
		{
			Widget expandButtonContainer = client.getWidget(CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID, CombatTasksWidgetID.CombatAchievementsTasks.TASK_LIST_EXPAND_BUTTONS);
			if (expandButtonContainer == null) return;
			Widget[] children = expandButtonContainer.getDynamicChildren();
			if (children.length == 0) return;
			children[0].setSpriteId(Integer.parseInt(configChanged.getNewValue()));
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired scriptPostFired)
	{
		handleScriptPostFired(scriptPostFired);
	}
	private void handleScriptPostFired(ScriptPostFired scriptPostFired)
	{
		if (scriptPostFired.getScriptId() == 4862) {
			addSaveButtons();
		}
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
		if (widgetLoaded.getGroupId() == CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID)
		{
			maxTaskCount = getMaxTaskCount();
			storeTasksFollowingWidgetLoaded();
			setFilterClickListeners();
			addSaveButtons();
		}
	}

	public boolean toggleTrackTask(String taskName)
	{
		CombatTask task = CombatTask.getTask(taskName);
		return toggleTrackTask(task);
	}

	private boolean toggleTrackTask(CombatTask task) {
		// If can't be found, can't track it
		if (task == null) return false;

		if (trackedTasks.contains(task)) {
			trackedTasks.remove(task);
		} else {
			trackedTasks.add(task);
		}

		SwingUtilities.invokeLater(() -> pluginPanel.refresh());

		return trackedTasks.contains(task);
	}

	private void completeTask(String taskName) {
		CombatTask task = CombatTask.getTask(taskName);
		if (trackedTasks.contains(task)) {
			toggleTrackTask(task);
		}
	}

	private void addSaveButtons() {
		Widget list = client.getWidget(CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID, CombatTasksWidgetID.CombatAchievementsTasks.TASK_LIST);
		Widget titles = client.getWidget(CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID, CombatTasksWidgetID.CombatAchievementsTasks.TASK_LIST_TITLES);

		if (list != null && titles != null)
		{
			Widget[] taskListTitles = titles.getDynamicChildren();
			Widget[] taskListItems = list.getDynamicChildren();
			if (taskListTitles.length != taskListItems.length)
			{
				log.error("Task list items & titles lengths do not match");
				return;
			}

			int yLocation = 14;
			for (int i = 0; i < taskListTitles.length; i++)
			{
				Widget taskListTitle = taskListTitles[i];
				Widget taskListItem = taskListItems[i];
				String taskName = taskListTitle.getText();
				CombatTask task = CombatTask.getTask(taskName);

				Widget trackTaskButton = list.createChild(-1, WidgetType.GRAPHIC);
				trackTaskButton.setSpriteId(getTrackTaskButtonSpriteId(trackedTasks.contains(task)));
				trackTaskButton.setOriginalWidth(13);
				trackTaskButton.setOriginalHeight(12);
				trackTaskButton.setOriginalX(300);
				trackTaskButton.setOriginalY(yLocation);
				trackTaskButton.setHasListener(true);
				trackTaskButton.setAction(1, "Track");
				trackTaskButton.setOnOpListener((JavaScriptCallback) e -> {
					boolean tracked = toggleTrackTask(taskName);
					trackTaskButton.setSpriteId(getTrackTaskButtonSpriteId(tracked));
				});
				trackTaskButton.setName(taskName);
				trackTaskButton.revalidate();

				yLocation += taskListItem.getHeight();
			}
		}
	}

	private int getTrackTaskButtonSpriteId(boolean tracked)
	{
		return tracked ? SpriteID.FAIRY_RING_REMOVE_FAVOURITE : SpriteID.FAIRY_RING_ADD_FAVOURITE;
	}

	private Integer getMaxTaskCount() {
		Widget bar = client.getWidget(CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID, CombatTasksWidgetID.CombatAchievementsTasks.TASK_BAR);
		if (bar == null) return null;

		for (Widget dynamicChild : bar.getDynamicChildren()) {
			Matcher m = completedTasksLabelRegex.matcher(dynamicChild.getText());
			if (m.find()) {
				return Integer.parseInt(m.group(1));
			}
		}

		return null;
	}

	private boolean storeTasksFollowingDropdownChange() {
		LinkedHashMap<String, Integer> colors = getTitleColors();
		if (colors == null) return true;

		if (colors.size() == previousTaskCount) return false;

		setTaskTitleColors(colors);
		return true;
	}

	private void storeTasksFollowingWidgetLoaded() {
		LinkedHashMap<String, Integer> colors = getTitleColors();
		if (colors == null) return;

		setTaskTitleColors(colors);
	}

	private void setTaskTitleColors(LinkedHashMap<String, Integer> colors) {
		taskTitleColors = colors;

		int currentCount = colors.size();
		previousTaskCount = currentCount;

		String taskCount = String.valueOf(previousTaskCount);
		String helpMessage = " (remove filters to get full export)";
		Color messageColor = Color.decode("#940B00");
		if (maxTaskCount != null) {
			taskCount += "/" + maxTaskCount;
			if (maxTaskCount == currentCount) {
				messageColor = Color.decode("#007517");
				helpMessage = "";
			}
		}
		sendChatMessage(taskCount + " tasks stored for export" + helpMessage, messageColor);
	}

	private boolean setFilterDropdownListener(int widgetId) {
		Widget dropdown = client.getWidget(CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID, widgetId);
		if (dropdown == null) return false;

		Widget[] options = dropdown.getDynamicChildren();
		if (options.length == 0) return false;

		for (Widget option : options) {
			option.setOnClickListener((JavaScriptCallback) e -> clientThread.invokeLater(this::storeTasksFollowingDropdownChange));
		}
		return true;
	}

	private void setFilterClickListeners() {
		client.getWidget(CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID, CombatTasksWidgetID.CombatAchievementsTasks.FILTER_TIER)
				.setOnClickListener((JavaScriptCallback) e -> clientThread.invokeLater(() -> setFilterDropdownListener(CombatTasksWidgetID.CombatAchievementsTasks.FILTER_DROPDOWN_TIER)));
		client.getWidget(CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID, CombatTasksWidgetID.CombatAchievementsTasks.FILTER_TYPE)
				.setOnClickListener((JavaScriptCallback) e -> clientThread.invokeLater(() -> setFilterDropdownListener(CombatTasksWidgetID.CombatAchievementsTasks.FILTER_DROPDOWN_TYPE)));
		client.getWidget(CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID, CombatTasksWidgetID.CombatAchievementsTasks.FILTER_MONSTER)
				.setOnClickListener((JavaScriptCallback) e -> clientThread.invokeLater(() -> setFilterDropdownListener(CombatTasksWidgetID.CombatAchievementsTasks.FILTER_DROPDOWN_MONSTER)));
		client.getWidget(CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID, CombatTasksWidgetID.CombatAchievementsTasks.FILTER_COMPLETED)
				.setOnClickListener((JavaScriptCallback) e -> clientThread.invokeLater(() -> setFilterDropdownListener(CombatTasksWidgetID.CombatAchievementsTasks.FILTER_DROPDOWN_COMPLETED)));
	}

	private LinkedHashMap<String, Integer> getTitleColors() {
		Widget list = client.getWidget(CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID, CombatTasksWidgetID.CombatAchievementsTasks.TASK_LIST_TITLES);
		if (list == null) return null;

		LinkedHashMap<String, Integer> titleColors = new LinkedHashMap<>();
		Widget[] titleWidgets = list.getDynamicChildren();
		for (Widget titleWidget : titleWidgets) {
			titleColors.put(titleWidget.getText(), titleWidget.getTextColor());
		}
		return titleColors;
	}

	private void sendChatMessage(String chatMessage, Color color)
	{
		final String message = new ChatMessageBuilder()
				.append(color, "Combat Task Tracker: ")
				.append(color, chatMessage)
				.build();

		chatMessageManager.queue(
				QueuedMessage.builder()
						.type(ChatMessageType.CONSOLE)
						.runeLiteFormattedMessage(message)
						.build());
	}
}
