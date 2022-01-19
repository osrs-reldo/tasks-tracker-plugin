package net.reldo.taskstracker.data.reldo;

import lombok.Data;
import net.reldo.taskstracker.data.TaskSave;

@Data
public class ReldoTaskSave
{
	long completed;
	long todo;
	long ignored;
	int order;
	String notes;
	long lastUpdated;

	public TaskSave toTaskSave()
	{
		TaskSave taskSave = new TaskSave();
		taskSave.setCompletedOn(this.completed);
		taskSave.setIgnoredOn(this.ignored);
		taskSave.setTrackedOn(this.todo);
		return taskSave;
	}
}
