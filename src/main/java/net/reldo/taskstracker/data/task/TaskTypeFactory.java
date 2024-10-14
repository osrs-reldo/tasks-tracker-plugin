package net.reldo.taskstracker.data.task;

import net.reldo.taskstracker.data.jsondatastore.types.TaskTypeDefinition;

public interface TaskTypeFactory
{
	TaskType create(TaskTypeDefinition taskTypeDefinition);
}
