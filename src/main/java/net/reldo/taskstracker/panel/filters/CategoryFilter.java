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
        String area = task.getArea();
        if (area == null) {
        	return false;
        }
        return areaFilter.contains("f-" +task.getArea().toLowerCase());
    }
}
