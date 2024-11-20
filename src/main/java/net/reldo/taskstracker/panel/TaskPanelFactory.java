package net.reldo.taskstracker.panel;

import net.reldo.taskstracker.data.task.TaskFromStruct;

public interface TaskPanelFactory
{
	TaskPanel create(TaskFromStruct task);
}
