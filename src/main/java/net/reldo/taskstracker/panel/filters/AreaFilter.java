package net.reldo.taskstracker.panel.filters;

import net.reldo.taskstracker.TasksTrackerConfig;
import net.reldo.taskstracker.tasktypes.Task;

public class AreaFilter extends Filter
{

    private final TasksTrackerConfig config;

    public AreaFilter(TasksTrackerConfig config)
    {
        this.config = config;
    }

    @Override
    public boolean meetsCriteria(Task task) {
        String areaFilter = config.areaFilter();

        boolean globalFilter = areaFilter.contains("f-" +task.getArea().toLowerCase());
        boolean generalFilter = areaFilter.contains("f-" +task.getArea().replace("General", "Global").toLowerCase());

        return globalFilter || generalFilter;
    }
}
