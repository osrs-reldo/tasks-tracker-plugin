package net.reldo.taskstracker.data.task.filters;

import net.reldo.taskstracker.data.task.TaskFromStruct;

public abstract class Filter
{
	public abstract boolean meetsCriteria(TaskFromStruct task);
}
