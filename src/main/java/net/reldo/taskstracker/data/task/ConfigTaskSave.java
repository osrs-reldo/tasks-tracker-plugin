package net.reldo.taskstracker.data.task;

public class ConfigTaskSave
{
	public final long completed;
	public final long tracked;
	public final Integer structId;
	public final long ignored;

	public ConfigTaskSave(TaskFromStruct task)
	{
		completed = task.getCompletedOn();
		tracked = task.getTrackedOn();
		ignored = task.getIgnoredOn();
		structId = task.getStructId();
	}
}
