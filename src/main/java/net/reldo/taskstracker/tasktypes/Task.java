package net.reldo.taskstracker.tasktypes;

import com.google.gson.annotations.Expose;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.reldo.ReldoTaskSave;
import net.reldo.taskstracker.panel.TaskPanel;
import java.time.Instant;
import lombok.Data;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;

@Data
public abstract class Task
{
	@Expose
	private final int id;
	private final String name;
	private final String description;
	private final String tier;

	@Expose
	private long completedOn;
	@Expose
	private long trackedOn;
	@Expose
	private long ignoredOn;

	public boolean isCompleted()
	{
		return completedOn > 0;
	}

	public void setCompleted(boolean completed)
	{
		long now = Instant.now().toEpochMilli();
		if (completed && completedOn > 0 && completedOn <= now)
		{
			return;
		}
		completedOn = completed ? now : 0;
	}

	public boolean isTracked()
	{
		return trackedOn > 0;
	}

	public void setTracked(boolean state)
	{
		long now = Instant.now().toEpochMilli();
		if (state && trackedOn > 0 && trackedOn <= now)
		{
			return;
		}
		trackedOn = state ? now : 0;
	}

	public boolean isIgnored()
	{
		return ignoredOn > 0;
	}

	public void setIgnored(boolean state)
	{
		long now = Instant.now().toEpochMilli();
		if (state && ignoredOn > 0 && ignoredOn <= now)
		{
			return;
		}
		ignoredOn = state ? now : 0;
	}

	public void loadSave(Task loadedData)
	{
		loadData(loadedData.getCompletedOn(), loadedData.getIgnoredOn(), loadedData.getTrackedOn());
	}

	public void loadReldoSave(ReldoTaskSave loadedData)
	{
		loadData(loadedData.getCompleted(), loadedData.getIgnored(), loadedData.getTodo());
	}

	private void loadData(long completedOn, long ignoredOn, long trackedOn)
	{
		if (completedOn > this.getCompletedOn())
		{
			this.setCompletedOn(completedOn);
		}
		if (ignoredOn > this.getIgnoredOn())
		{
			this.setIgnoredOn(ignoredOn);
		}
		if (trackedOn > this.getTrackedOn())
		{
			this.setTrackedOn(trackedOn);
		}
	}

	public abstract TaskType getType();

	public abstract TaskPanel generatePanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, SkillIconManager skillIconManager);
}
