package com.tylerthardy.combattaskstracker;

import javax.inject.Inject;
import com.tylerthardy.combattaskstracker.widgets.CombatTasksWidgetID;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@PluginDescriptor(
	name = "Combat Tasks Tracker"
)
public class CombatTasksTrackerPlugin extends Plugin
{
	private static final Pattern completedTasksRegex = Pattern.compile("Tasks Completed: \\d+/(\\d+)");

	public LinkedHashMap<String, Integer> taskTitleColors;
	private CombatTasksTrackerPanel pluginPanel;
	private NavigationButton navButton;
	private Integer maxTaskCount;
	private int previousTaskCount = -1;

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Override
	protected void startUp() throws Exception
	{
		taskTitleColors = new LinkedHashMap<>();
		pluginPanel = new CombatTasksTrackerPanel(this);
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "panel_icon.png");
		navButton = NavigationButton.builder()
				.tooltip("Hiscore")
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
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			taskTitleColors = new LinkedHashMap<>();
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if (widgetLoaded.getGroupId() == CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID)
		{
			maxTaskCount = getMaxTaskCount();
			storeFollowingWidgetLoaded();
			setFilterClickListeners();
		}
	}

	private Integer getMaxTaskCount() {
		Widget bar = client.getWidget(CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID, CombatTasksWidgetID.CombatAchievementsTasks.TASK_BAR);
		if (bar == null) return null;

		for (Widget dynamicChild : bar.getDynamicChildren()) {
			Matcher m = completedTasksRegex.matcher(dynamicChild.getText());
			if (m.find()) {
				return Integer.parseInt(m.group(1));
			}
		}

		return null;
	}

	private boolean storeFollowingDropdownChange() {
		LinkedHashMap<String, Integer> colors = getTitleColors();
		if (colors == null) return true;

		if (colors.size() == previousTaskCount) return false;

		setTaskTitleColors(colors);
		return true;
	}

	private void storeFollowingWidgetLoaded() {
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
			option.setOnClickListener((JavaScriptCallback) e -> clientThread.invokeLater(this::storeFollowingDropdownChange));
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
