package net.reldo.taskstracker.data.task;

import com.google.gson.annotations.Expose;

public class ConfigTaskSave
{
	@Expose
	public final long completed;
	@Expose
	public final long tracked;
	@Expose
	public final Integer structId;
	@Expose
	public final long ignored;
	@Expose
	public final String note;

	public ConfigTaskSave(ITask task)
	{
		completed = task.getCompletedOn();
		tracked = task.getTrackedOn();
		ignored = task.getIgnoredOn();
		structId = task.getTaskId();
		note = task.getNote();
	}
}
