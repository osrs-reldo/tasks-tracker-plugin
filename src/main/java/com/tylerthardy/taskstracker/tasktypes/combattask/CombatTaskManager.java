package com.tylerthardy.taskstracker.tasktypes.combattask;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.data.TrackerDataStore;
import com.tylerthardy.taskstracker.tasktypes.AbstractTaskManager;
import com.tylerthardy.taskstracker.tasktypes.TaskType;
import com.tylerthardy.taskstracker.widgets.CombatTasksWidgetID;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.util.Text;

import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CombatTaskManager extends AbstractTaskManager
{
    private static final Pattern COMPLETED_TASKS_LABEL_REGEX = Pattern.compile("Tasks Completed: \\d+/(\\d+)");
    private static final Pattern TASK_COMPLETED_CHAT_MESSAGE_REGEX = Pattern.compile("Congratulations, you've completed an? (.*) combat task: (.*)\\.");
    private static final int COMPLETED_TASK_COLOR = 901389;

    private final Client client;
    private ClientThread clientThread;

    private int previousTaskCount = -1;

    public CombatTaskManager(Client client, ClientThread clientThread, TasksTrackerPlugin plugin, TrackerDataStore trackerDataStore)
    {
        super(TaskType.COMBAT, plugin, trackerDataStore);
        this.client = client;
        this.clientThread = clientThread;
    }

    @Override
    public void handleOnWidgetLoaded(WidgetLoaded widgetLoaded)
    {
        if (widgetLoaded.getGroupId() == CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID)
        {
            maxTaskCount = scrapeTotalCount();
            updateTasksFollowingWidgetLoaded();
            setFilterClickListeners();
        }
    }

    @Override
    public void handleChatMessage(ChatMessage chatMessage)
    {
        if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE) {
            return;
        }
        String strippedMessage = Text.removeFormattingTags(chatMessage.getMessage());
        Matcher m = TASK_COMPLETED_CHAT_MESSAGE_REGEX.matcher(strippedMessage);
        if (!m.find()) {
            return;
        }

        String tier = m.group(1);
        String taskName = m.group(2);
        completeTask(taskName);
    }

    public LinkedHashMap<String, Boolean> scrapeTaskCompletedData()
    {
        Widget list = client.getWidget(CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID, CombatTasksWidgetID.CombatAchievementsTasks.TASK_LIST_TITLES);
        if (list == null) return null;

        LinkedHashMap<String, Boolean> taskProgress = new LinkedHashMap<>();
        Widget[] titleWidgets = list.getDynamicChildren();
        for (Widget titleWidget : titleWidgets) {
            taskProgress.put(titleWidget.getText(), titleWidget.getTextColor() == COMPLETED_TASK_COLOR);
        }
        return taskProgress;
    }

    public int scrapeTotalCount() {
        Widget bar = client.getWidget(CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID, CombatTasksWidgetID.CombatAchievementsTasks.TASK_BAR);
        if (bar == null) return -1;

        for (Widget dynamicChild : bar.getDynamicChildren()) {
            Matcher m = COMPLETED_TASKS_LABEL_REGEX.matcher(dynamicChild.getText());
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
        }

        return -1;
    }

    private void setFilterClickListeners()
    {
        client.getWidget(CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID, CombatTasksWidgetID.CombatAchievementsTasks.FILTER_TIER)
                .setOnClickListener((JavaScriptCallback) e -> clientThread.invokeLater(() -> setFilterDropdownListener(CombatTasksWidgetID.CombatAchievementsTasks.FILTER_DROPDOWN_TIER)));
        client.getWidget(CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID, CombatTasksWidgetID.CombatAchievementsTasks.FILTER_TYPE)
                .setOnClickListener((JavaScriptCallback) e -> clientThread.invokeLater(() -> setFilterDropdownListener(CombatTasksWidgetID.CombatAchievementsTasks.FILTER_DROPDOWN_TYPE)));
        client.getWidget(CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID, CombatTasksWidgetID.CombatAchievementsTasks.FILTER_MONSTER)
                .setOnClickListener((JavaScriptCallback) e -> clientThread.invokeLater(() -> setFilterDropdownListener(CombatTasksWidgetID.CombatAchievementsTasks.FILTER_DROPDOWN_MONSTER)));
        client.getWidget(CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID, CombatTasksWidgetID.CombatAchievementsTasks.FILTER_COMPLETED)
                .setOnClickListener((JavaScriptCallback) e -> clientThread.invokeLater(() -> setFilterDropdownListener(CombatTasksWidgetID.CombatAchievementsTasks.FILTER_DROPDOWN_COMPLETED)));
    }

    private boolean setFilterDropdownListener(int widgetId) {
        Widget dropdown = client.getWidget(CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID, widgetId);
        if (dropdown == null) return false;

        Widget[] options = dropdown.getDynamicChildren();
        if (options.length == 0) return false;

        for (Widget option : options) {
            option.setOnClickListener((JavaScriptCallback) e -> clientThread.invokeLater(this::updateTasksFollowingDropdownChange));
        }
        return true;
    }

    // TODO: Find better way to bring updateTasksFollowingDropdownChange/WidgetLoaded together
    private boolean updateTasksFollowingDropdownChange() {
        LinkedHashMap<String, Boolean> taskProgress = scrapeTaskCompletedData();
        if (taskProgress == null) return true;

        if (taskProgress.size() == previousTaskCount) return false;

        updateTaskProgress(taskProgress);
        refresh(null);
        return true;
    }

    private void updateTasksFollowingWidgetLoaded() {
        LinkedHashMap<String, Boolean> taskProgress = scrapeTaskCompletedData();
        if (taskProgress == null) return;

        updateTaskProgress(taskProgress);
        refresh(null);
    }
}
