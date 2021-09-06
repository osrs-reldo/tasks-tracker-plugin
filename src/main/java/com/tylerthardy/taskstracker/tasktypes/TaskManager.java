package com.tylerthardy.taskstracker.tasktypes;

import com.google.common.base.Predicates;
import com.tylerthardy.combattaskstracker.widgets.CombatTasksWidgetID;
import com.tylerthardy.taskstracker.TaskDataLoader;
import com.tylerthardy.taskstracker.TasksTrackerPlugin;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskManager
{
    private static final Pattern COMPLETED_TASKS_LABEL_REGEX = Pattern.compile("Tasks Completed: \\d+/(\\d+)");
    private static final Pattern TASK_COMPLETED_CHAT_MESSAGE_REGEX = Pattern.compile("Congratulations, you've completed an? (.*) combat task: (.*)\\.");;
    private static final int COMPLETED_TASK_COLOR = 901389;

    // new
    public HashSet<Task> trackedTasks = new HashSet<>();
    public TaskType selectedTaskType = TaskType.TEST;
    public HashMap<TaskType, ArrayList<Task>> tasks = new HashMap<>();
    public String taskTextFilter;

    // old
    private Integer maxTaskCount;
    private final TasksTrackerPlugin plugin;
    private final Client client;
    private final ClientThread clientThread;

    private int previousTaskCount = -1;

    @Inject
    public TaskManager(TasksTrackerPlugin plugin, Client client, ClientThread clientThread)
    {
        this.plugin = plugin;
        this.client = client;
        this.clientThread = clientThread;
        setSelectedTaskType(TaskType.TEST);
    }

    public void refresh()
    {
        SwingUtilities.invokeLater(() -> plugin.pluginPanel.refresh());
    }

    public void setSelectedTaskType(TaskType type)
    {
        selectedTaskType = type;
        // FIXME: This is doing double duty to the task loader to store all tasks in a cache
        if (!tasks.containsKey(type)) {
            tasks.put(type, TaskDataLoader.getTasks(type));
        }
    }

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
        plugin.completeTask(taskName);
    }

    public void handleOnWidgetLoaded(WidgetLoaded widgetLoaded)
    {
        if (widgetLoaded.getGroupId() == CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID)
        {
            maxTaskCount = scrapeMaxTaskCount();
            updateTasksFollowingWidgetLoaded();
            setFilterClickListeners();
        }
    }

    private Integer scrapeMaxTaskCount() {
        Widget bar = client.getWidget(CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID, CombatTasksWidgetID.CombatAchievementsTasks.TASK_BAR);
        if (bar == null) return null;

        for (Widget dynamicChild : bar.getDynamicChildren()) {
            Matcher m = COMPLETED_TASKS_LABEL_REGEX.matcher(dynamicChild.getText());
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
        }

        return null;
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

    private LinkedHashMap<String, Boolean> getTaskProgressFromTitleColors() {
        Widget list = client.getWidget(CombatTasksWidgetID.COMBAT_ACHIEVEMENTS_TASKS_GROUP_ID, CombatTasksWidgetID.CombatAchievementsTasks.TASK_LIST_TITLES);
        if (list == null) return null;

        LinkedHashMap<String, Boolean> taskProgress = new LinkedHashMap<>();
        Widget[] titleWidgets = list.getDynamicChildren();
        for (Widget titleWidget : titleWidgets) {
            taskProgress.put(titleWidget.getText(), titleWidget.getTextColor() == COMPLETED_TASK_COLOR);
        }
        return taskProgress;
    }

    // TODO: Find better way to bring updateTasksFollowingDropdownChange/WidgetLoaded together
    private boolean updateTasksFollowingDropdownChange() {
        LinkedHashMap<String, Boolean> taskProgress = getTaskProgressFromTitleColors();
        if (taskProgress == null) return true;

        if (taskProgress.size() == previousTaskCount) return false;

        updateTaskProgress(taskProgress);
        refresh();
        return true;
    }

    private void updateTasksFollowingWidgetLoaded() {
        LinkedHashMap<String, Boolean> taskProgress = getTaskProgressFromTitleColors();
        if (taskProgress == null) return;

        updateTaskProgress(taskProgress);
        refresh();
    }

    private void updateTaskProgress(LinkedHashMap<String, Boolean> taskProgress) {
        // TODO: Hacky, come up with more performant solution & consider case sensitivity
        for (Task task : tasks.get(selectedTaskType)) {
            if (taskProgress.containsKey(task.getName()))
            {
                task.setCompleted(taskProgress.get(task.getName()));
            }
        }

        sendTaskUpdateMessage(taskProgress);
    }

    private void sendTaskUpdateMessage(LinkedHashMap<String, Boolean> taskProgress) {
        int currentCount = taskProgress.size();
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
        plugin.sendChatMessage(taskCount + " tasks stored for export" + helpMessage, messageColor);
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
}
