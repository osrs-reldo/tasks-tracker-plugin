package net.reldo.taskstracker.tasktypes.league4;

import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.Util;
import net.reldo.taskstracker.panel.TaskPanel;
import net.reldo.taskstracker.panel.filters.AreaFilter;
import net.reldo.taskstracker.panel.filters.SkillFilter;
import net.reldo.taskstracker.panel.filters.TierFilter;
import net.reldo.taskstracker.tasktypes.RequiredSkill;
import net.reldo.taskstracker.tasktypes.Task;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.swing.JPopupMenu;
import net.runelite.api.Skill;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;

public class League4TaskPanel extends TaskPanel
{
	public League4TaskPanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, Task task)
	{
		super(plugin, clientThread, spriteManager, task);
		filters.add(new SkillFilter(plugin.getConfig()));
		filters.add(new TierFilter(plugin.getConfig()));
		filters.add(new AreaFilter(plugin.getConfig()));
	}

	@Override
	public JPopupMenu getPopupMenu()
	{
		return null;
	}

	@Override
	public String getTaskTooltip()
	{
		League4Task task = (League4Task) this.task;
		String text = Util.wrapWithBold(task.getName()) + Util.HTML_LINE_BREAK +
			task.getTier() + Util.HTML_LINE_BREAK +
			task.getDescription() +
			getSkillSectionHtml();

		text = Util.wrapWithWrappingParagraph(text, 200);

		return Util.wrapWithHtml(text);
	}

	@Override
	public BufferedImage getIcon()
	{
		League4TaskTier tier = League4TaskTier.getTierByName(task.getTier());
		if (tier == null)
		{
			return null;
		}

		return spriteManager.getSprite(tier.spriteId, 0);
	}

	// TODO (1/29/22): The required skill loop code is repeated in getSkillSectionHtml
	//  Ideally, checking skill requirements would be a responsibility of Task
	//  Current issue is that Task is instantiated by Gson in multiple places, so plugin may not be injected/accessible
	@Override
	public Color getTaskBackgroundColor(Task task, int[] playerSkills)
	{
		if (playerSkills == null)
		{
			return ColorScheme.DARKER_GRAY_COLOR;
		}

		if (task.isCompleted())
		{
			return COMPLETED_BACKGROUND_COLOR;
		}

		for (RequiredSkill requiredSkill : ((League4Task) task).getSkills())
		{
			Skill skill;
			// FIXME: Shouldn't use exception for control flow
			try
			{
				skill = Skill.valueOf(requiredSkill.skill.toUpperCase());
			}
			catch (IllegalArgumentException ex)
			{
				continue;
			}

			int level;
			// FIXME: Shouldn't use exception for control flow
			try
			{
				level = Integer.parseInt(requiredSkill.level);
			}
			catch (NumberFormatException ex)
			{
				continue;
			}

			if (playerSkills[skill.ordinal()] < level)
			{
				return UNQUALIFIED_BACKGROUND_COLOR;
			}
		}

		return ColorScheme.DARKER_GRAY_COLOR;
	}

	private String getSkillSectionHtml()
	{
		StringBuilder skillSection = new StringBuilder();
		League4Task task = (League4Task) this.task;
		for (RequiredSkill requiredSkill : task.getSkills())
		{
			Skill skill;
			// FIXME: Shouldn't use exception for control flow
			try
			{
				skill = Skill.valueOf(requiredSkill.skill.toUpperCase());
			}
			catch (IllegalArgumentException ex)
			{
				continue;
			}

			int level;
			// FIXME: Shouldn't use exception for control flow
			try
			{
				level = Integer.parseInt(requiredSkill.level);
			}
			catch (NumberFormatException ex)
			{
				continue;
			}

			skillSection.append(Util.HTML_LINE_BREAK);

			int playerLevel = 255;
			if (this.plugin.playerSkills != null)
			{
				playerLevel = this.plugin.playerSkills[skill.ordinal()];
			}
			skillSection.append(getSkillRequirementHtml(requiredSkill.getSkill().toLowerCase(), playerLevel, level));
		}

		return skillSection.toString();
	}

	private String getSkillRequirementHtml(String skillName, int playerLevel, int requiredLevel)
	{
		String skillIconPath = "/skill_icons_small/" + skillName + ".png";
		URL url = SkillIconManager.class.getResource(skillIconPath);
		Color color = playerLevel > requiredLevel ? QUALIFIED_TEXT_COLOR : UNQUALIFIED_TEXT_COLOR;
		return Util.imageTag(url) + " " + Util.colorTag(color, playerLevel + "/" + requiredLevel);
	}
}
