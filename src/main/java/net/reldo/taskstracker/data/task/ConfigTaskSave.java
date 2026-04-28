package net.reldo.taskstracker.data.task;

import com.google.gson.annotations.Expose;
import javax.annotation.Nullable;
import net.runelite.api.coords.WorldPoint;

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
	@Expose
	public final CompletionLocation completionLocation;

	public ConfigTaskSave(ITask task)
	{
		completed = task.getCompletedOn();
		tracked = task.getTrackedOn();
		ignored = task.getIgnoredOn();
		structId = task.getTaskId();
		note = task.getNote();
		WorldPoint loc = task.getCompletionLocation();
		completionLocation = loc != null ? new CompletionLocation(loc.getX(), loc.getY(), loc.getPlane()) : null;
	}

	public static class CompletionLocation
	{
		@Expose
		public final int x;
		@Expose
		public final int y;
		@Expose
		public final int plane;

		public CompletionLocation(int x, int y, int plane)
		{
			this.x = x;
			this.y = y;
			this.plane = plane;
		}

		@Nullable
		public WorldPoint toWorldPoint()
		{
			return new WorldPoint(x, y, plane);
		}
	}
}
