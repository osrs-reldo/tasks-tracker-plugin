package net.reldo.taskstracker.panel.filters;

import net.reldo.taskstracker.TasksTrackerConfig;
import net.reldo.taskstracker.tasktypes.Task;

public class TierFilter extends Filter
{

    private final TasksTrackerConfig config;

    public TierFilter(TasksTrackerConfig config)
    {
        this.config = config;
    }

    @Override
    public boolean meetsCriteria(Task task) {
        String tierFilter = config.tierFilter();

        return tierFilter.contains("f-" +task.getTier().toLowerCase());
    }
}
