package net.reldo.taskstracker.panel.filters;

import net.reldo.taskstracker.tasktypes.Task;

public abstract class Filter {

    public abstract boolean meetsCriteria(Task task);
}
