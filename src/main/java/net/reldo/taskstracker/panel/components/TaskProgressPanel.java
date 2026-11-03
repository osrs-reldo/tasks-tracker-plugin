package net.reldo.taskstracker.panel.components;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import lombok.Value;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.jsondatastore.types.ProgressMode;
import net.reldo.taskstracker.data.jsondatastore.types.TaskProgressDefinition;
import net.reldo.taskstracker.data.task.ITask;
import net.runelite.api.Skill;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

public class TaskProgressPanel extends JPanel
{
	@Value
	public static class BarValue
	{
		int current;
		int target;
	}

	public TaskProgressPanel(TasksTrackerPlugin plugin, ITask task, List<TaskProgressDefinition> defs, int barHeight, boolean withText)
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);

		for (BarValue barValue : getBarValues(plugin, task, defs))
		{
			add(buildBar(barValue.getCurrent(), barValue.getTarget(), barHeight, withText));
		}
	}

	/**
	 * The bars to draw for a task's progress: one summed bar in SUM mode, otherwise one per definition.
	 * Shared by the side panel and the overlay.
	 */
	public static List<BarValue> getBarValues(TasksTrackerPlugin plugin, ITask task, List<TaskProgressDefinition> defs)
	{
		List<BarValue> barValues = new ArrayList<>();
		if (task.getTaskDefinition().getProgressMode() == ProgressMode.SUM)
		{
			int totalCurrent = 0;
			int totalTarget = 0;
			for (TaskProgressDefinition def : defs)
			{
				totalCurrent += getCurrentValue(plugin, task, def);
				totalTarget += def.getTarget();
			}
			barValues.add(new BarValue(totalCurrent, totalTarget));
		}
		else
		{
			for (TaskProgressDefinition def : defs)
			{
				barValues.add(new BarValue(getCurrentValue(plugin, task, def), def.getTarget()));
			}
		}
		return barValues;
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

	private static int getCurrentValue(TasksTrackerPlugin plugin, ITask task, TaskProgressDefinition def)
	{
		switch (def.getType())
		{
			case EXPERIENCE:
			{
				Skill skill = def.getResolvedSkill();
				return skill == null ? 0 : plugin.getCachedSkillXp(skill);
			}
			case LEVEL:
			{
				Skill skill = def.getResolvedSkill();
				return skill == null ? 0 : plugin.getCachedSkillLevel(skill);
			}
			case VARP:
				if (def.getId() == null)
				{
					return 0;
				}
				return plugin.getTaskService().getProgressVarpValue(def.getId());
			case VARBIT:
				if (def.getId() == null)
				{
					return 0;
				}
				return plugin.getTaskService().getProgressVarbitValue(def.getId());
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
