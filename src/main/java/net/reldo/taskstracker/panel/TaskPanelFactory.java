package net.reldo.taskstracker.panel;

import net.reldo.taskstracker.data.task.ITask;

public interface TaskPanelFactory
{
	TaskPanel create(ITask task);
}
