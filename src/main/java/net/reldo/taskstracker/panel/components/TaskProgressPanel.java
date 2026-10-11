package net.reldo.taskstracker.panel.components;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.jsondatastore.types.ProgressMode;
import net.reldo.taskstracker.data.jsondatastore.types.ProgressType;
import net.reldo.taskstracker.data.jsondatastore.types.TaskProgressDefinition;
import net.reldo.taskstracker.data.task.ITask;
import net.runelite.api.Skill;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

@Slf4j
public class TaskProgressPanel extends JPanel
{
	private final TasksTrackerPlugin plugin;
	private final ITask task;

	public TaskProgressPanel(TasksTrackerPlugin plugin, ITask task, List<TaskProgressDefinition> defs, int barHeight, boolean withText)
	{
		this.plugin = plugin;
		this.task = task;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);

		ProgressMode progressMode = task.getTaskDefinition().getProgressMode();
		if (progressMode == ProgressMode.SUM)
		{
			int totalCurrent = 0;
			int totalTarget = 0;
			for (TaskProgressDefinition def : defs)
			{
				totalCurrent += getCurrentValue(def);
				totalTarget += def.getTarget();
			}
			add(buildBar(totalCurrent, totalTarget, barHeight, withText));
		}
		else
		{
			for (TaskProgressDefinition def : defs)
			{
				add(buildBar(getCurrentValue(def), def.getTarget(), barHeight, withText));
			}
		}
	}

	private JProgressBar buildBar(int current, int target, int barHeight, boolean withText)
	{
		JProgressBar bar = new JProgressBar(0, target);
		bar.setValue(Math.min(current, target));
		bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, barHeight));
		bar.setPreferredSize(new Dimension(0, barHeight));
		bar.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
		bar.setForeground(new Color(0, 168, 0));
		bar.setBorderPainted(false);
		bar.setStringPainted(withText);
		if (withText)
		{
			bar.setString(formatProgressText(current, target));
			bar.setFont(FontManager.getRunescapeSmallFont());
		}
		return bar;
	}

	private int getCurrentValue(TaskProgressDefinition def)
	{
		switch (def.getType())
		{
			case EXPERIENCE:
				if (plugin.playerXp == null)
				{
					return 0;
				}
				try
				{
					Skill skill = Skill.valueOf(def.getSkill().toUpperCase());
					return plugin.playerXp[skill.ordinal()];
				}
				catch (IllegalArgumentException | NullPointerException ex)
				{
					log.warn("Unknown skill for EXPERIENCE progress: {}", def.getSkill());
					return 0;
				}
			case LEVEL:
				if (plugin.playerSkills == null)
				{
					return 0;
				}
				try
				{
					Skill skill = Skill.valueOf(def.getSkill().toUpperCase());
					return plugin.playerSkills[skill.ordinal()];
				}
				catch (IllegalArgumentException | NullPointerException ex)
				{
					log.warn("Unknown skill for LEVEL progress: {}", def.getSkill());
					return 0;
				}
			case VARP:
				if (def.getId() == null)
				{
					return 0;
				}
				return task.getProgressValue(ProgressType.VARP, def.getId());
			case VARBIT:
				if (def.getId() == null)
				{
					return 0;
				}
				return task.getProgressValue(ProgressType.VARBIT, def.getId());
			default:
				return 0;
		}
	}

	private String formatProgressText(int current, int target)
	{
		if (target >= 1_000_000)
		{
			return String.format("%s / %s", formatLargeNumber(current), formatLargeNumber(target));
		}
		return current + " / " + target;
	}

	private String formatLargeNumber(int value)
	{
		if (value >= 1_000_000)
		{
			return String.format("%.1fM", value / 1_000_000.0);
		}
		if (value >= 1_000)
		{
			return String.format("%.1fK", value / 1_000.0);
		}
		return String.valueOf(value);
	}
}
