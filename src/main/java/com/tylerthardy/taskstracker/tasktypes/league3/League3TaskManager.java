package com.tylerthardy.taskstracker.tasktypes.league3;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.data.TrackerDataStore;
import com.tylerthardy.taskstracker.tasktypes.AbstractTaskManager;
import com.tylerthardy.taskstracker.tasktypes.TaskType;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.util.Text;

@Slf4j
public class League3TaskManager extends AbstractTaskManager
{
    private static final Pattern FRAGMENT_UNLOCK_CHAT_MESSAGE_REGEX = Pattern.compile("Congratulations! You've unlocked the (.*) Fragment!");
    private static final Pattern TIER_UNLOCK_CHAT_MESSAGE_REGEX = Pattern.compile("Congratulations! You have reached a new Tier! Check the Fragments Panel for the new Passive Effects you have unlocked!");
    private static final Pattern COMPLETED_TASKS_LABEL_REGEX = Pattern.compile("Tasks Completed: \\d+/(\\d+)");
    private static final Pattern TASK_COMPLETED_CHAT_MESSAGE_REGEX = Pattern.compile("Congratulations, you've completed an? (.*) task: (.*)\\.");
    private static final Pattern EARNED_CHAT_MESSAGE_REGEX = Pattern.compile("You have earned (.*) League Points and (.*) Sage's Renown.");
    private static final Pattern CURRENCY_CHAT_MESSAGE_REGEX = Pattern.compile("You now have (.*) League Points and (.*) Sage's Renown in total.");
    private static final int COMPLETED_TASK_COLOR = 2752445;

    private final Client client;
    private ClientThread clientThread;

    private int previousTaskCount = -1;

    public League3TaskManager(Client client, ClientThread clientThread, TasksTrackerPlugin plugin, TrackerDataStore trackerDataStore)
    {
        super(TaskType.LEAGUE_3, plugin, trackerDataStore);
        this.client = client;
        this.clientThread = clientThread;
    }

    @Override
    public void handleOnScriptPostFired(ScriptPostFired scriptPostFired)
    {
        if (scriptPostFired.getScriptId() == League3ScriptID.league3_tasks_draw_list)
        {
            maxTaskCount = scrapeTotalCount();
            updateTasksFollowingWidgetLoaded();
            setFilterClickListeners();
        }
    }

    @Override
    public void handleOnWidgetLoaded(WidgetLoaded widgetLoaded)
    {
        if (widgetLoaded.getGroupId() == League3WidgetID.LEAGUE_3_TASKS_GROUP_ID)
        {
            setFilterClickListeners();
        }
        if (widgetLoaded.getGroupId() == League3WidgetID.LEAGUE_3_SUMMARY_TAB)
        {
            trackerDataStore.leagueData.put("leaguePoints", scrapeLeaguePoints());
            trackerDataStore.leagueData.put("leaguePointsTimestamp", Instant.now().toEpochMilli());
            trackerDataStore.leagueData.put("tasksCompleted", scrapeTaskCompletedCount());
            trackerDataStore.leagueData.put("tasksCompletedTimestamp", Instant.now().toEpochMilli());
            trackerDataStore.leagueData.put("sagesRenown", scrapeSagesRenown());
            trackerDataStore.leagueData.put("sagesRenownTimestamp", Instant.now().toEpochMilli());
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
        Widget list = client.getWidget(League3WidgetID.LEAGUE_3_TASKS_GROUP_ID, League3WidgetID.League3Tasks.TASK_LIST_TITLES);
        if (list == null)
        {
            return null;
        }

        LinkedHashMap<String, Boolean> taskProgress = new LinkedHashMap<>();
        Widget[] titleWidgets = list.getDynamicChildren();
        for (Widget titleWidget : titleWidgets) {
            taskProgress.put(titleWidget.getText(), titleWidget.getTextColor() == COMPLETED_TASK_COLOR);
        }
        return taskProgress;
    }

    public int scrapeTotalCount() {
        Widget bar = client.getWidget(League3WidgetID.LEAGUE_3_TASKS_GROUP_ID, League3WidgetID.League3Tasks.TASK_BAR);
        if (bar == null) return -1;

        for (Widget dynamicChild : bar.getDynamicChildren()) {
            Matcher m = COMPLETED_TASKS_LABEL_REGEX.matcher(dynamicChild.getText());
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
        }

        return -1;
    }

    private int scrapeTaskCompletedCount() {
        Widget totalCount = client.getWidget(League3WidgetID.LEAGUE_3_SUMMARY_TAB, League3WidgetID.League3SummaryTab.TASKS_COMPLETED_VALUE);
        if (totalCount == null) return -1;

        return Integer.parseInt(totalCount.getText());
    }

    private int scrapeLeaguePoints() {
        Widget totalCount = client.getWidget(League3WidgetID.LEAGUE_3_SUMMARY_TAB, League3WidgetID.League3SummaryTab.LEAGUE_POINTS_VALUE);
        if (totalCount == null) return -1;

        return Integer.parseInt(totalCount.getText());

    }

    private int scrapeSagesRenown() {
        Widget totalCount = client.getWidget(League3WidgetID.LEAGUE_3_SUMMARY_TAB, League3WidgetID.League3SummaryTab.SAGE_RENOWN_VALUE);
        if (totalCount == null) return -1;

        return Integer.parseInt(totalCount.getText());

    }

    private void setFilterClickListeners()
    {
        client.getWidget(League3WidgetID.LEAGUE_3_TASKS_GROUP_ID, League3WidgetID.League3Tasks.FILTER_TIER)
                .setOnClickListener((JavaScriptCallback) e -> clientThread.invokeLater(() -> setFilterDropdownListener(League3WidgetID.League3Tasks.FILTER_DROPDOWN_TIER_VALUES)));
        client.getWidget(League3WidgetID.LEAGUE_3_TASKS_GROUP_ID, League3WidgetID.League3Tasks.FILTER_TYPE)
                .setOnClickListener((JavaScriptCallback) e -> clientThread.invokeLater(() -> setFilterDropdownListener(League3WidgetID.League3Tasks.FILTER_DROPDOWN_TYPE_VALUES)));
        client.getWidget(League3WidgetID.LEAGUE_3_TASKS_GROUP_ID, League3WidgetID.League3Tasks.FILTER_AREA)
                .setOnClickListener((JavaScriptCallback) e -> clientThread.invokeLater(() -> setFilterDropdownListener(League3WidgetID.League3Tasks.FILTER_DROPDOWN_AREA_VALUES)));
        client.getWidget(League3WidgetID.LEAGUE_3_TASKS_GROUP_ID, League3WidgetID.League3Tasks.FILTER_SKILL)
                .setOnClickListener((JavaScriptCallback) e -> clientThread.invokeLater(() -> setFilterDropdownListener(League3WidgetID.League3Tasks.FILTER_DROPDOWN_SKILL_VALUES)));
        client.getWidget(League3WidgetID.LEAGUE_3_TASKS_GROUP_ID, League3WidgetID.League3Tasks.FILTER_COMPLETED)
                .setOnClickListener((JavaScriptCallback) e -> clientThread.invokeLater(() -> setFilterDropdownListener(League3WidgetID.League3Tasks.FILTER_DROPDOWN_COMPLETED_VALUES)));
    }

    private boolean setFilterDropdownListener(int widgetId) {
        Widget dropdown = client.getWidget(League3WidgetID.LEAGUE_3_TASKS_GROUP_ID, widgetId);
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
