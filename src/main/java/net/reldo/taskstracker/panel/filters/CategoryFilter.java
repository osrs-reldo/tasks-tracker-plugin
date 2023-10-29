package net.reldo.taskstracker.panel.filters;

import net.reldo.taskstracker.TasksTrackerConfig;
import net.reldo.taskstracker.tasktypes.Task;

public class CategoryFilter extends Filter
{

    private final TasksTrackerConfig config;

    public CategoryFilter(TasksTrackerConfig config)
    {
        this.config = config;
    }

    @Override
    public boolean meetsCriteria(Task task) {
        String areaFilter = config.areaFilter();

        return areaFilter.contains("f-" +task.getCategory().toLowerCase());
    }
}
