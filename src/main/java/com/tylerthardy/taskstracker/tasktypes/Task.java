package com.tylerthardy.taskstracker.tasktypes;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.panel.TaskPanel;
import java.time.Instant;
import lombok.Data;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;

@Data
public abstract class Task
{
	private final String name;
	private final String description;
	private final String tier;

	private long completedOn;
	private long trackedOn;
	private long ignoredOn;

	public void setCompletedOn(long completedOn)
	{
		this.completedOn = completedOn;
	}

	public boolean isCompleted()
	{
		return completedOn > 0;
	}

	public void setCompleted(boolean state)
	{
		if (state && completedOn > 0)
		{
			return;
		}
		completedOn = state ? Instant.now().toEpochMilli() : -1;
	}

	public boolean isTracked()
	{
		return trackedOn > 0;
	}

	public void setTracked(boolean state)
	{
		if (state && trackedOn > 0)
		{
			return;
		}
		trackedOn = state ? Instant.now().toEpochMilli() : -1;
	}

	public boolean isIgnored()
	{
		return ignoredOn > 0;
	}

	public void setIgnored(boolean state)
	{
		if (state && ignoredOn > 0)
		{
			return;
		}
		ignoredOn = state ? Instant.now().toEpochMilli() : -1;
	}

	public abstract TaskType getType();

	public abstract TaskPanel generatePanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, SkillIconManager skillIconManager);
}
