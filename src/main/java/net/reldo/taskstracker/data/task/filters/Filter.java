package net.reldo.taskstracker.data.task.filters;

import net.reldo.taskstracker.data.task.ITask;

public abstract class Filter
{
	public abstract boolean meetsCriteria(ITask task);
}
