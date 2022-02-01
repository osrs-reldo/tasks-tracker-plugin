package net.reldo.taskstracker.tasktypes;

import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.panel.TaskPanel;
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

	private RequiredSkill[] skills = null;

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

	public abstract TaskType getType();

	public abstract TaskPanel generatePanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, SkillIconManager skillIconManager);
}
