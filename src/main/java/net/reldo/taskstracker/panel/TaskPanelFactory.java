package net.reldo.taskstracker.panel;

import net.reldo.taskstracker.tasktypes.Task;

public interface TaskPanelFactory
{
	TaskPanel create(Task task);
}
