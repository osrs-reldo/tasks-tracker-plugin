package net.reldo.taskstracker.tasktypes.league3;

import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.Util;
import net.reldo.taskstracker.panel.TaskPanel;
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

public class League3TaskPanel extends TaskPanel
{
	public League3TaskPanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, Task task)
	{
		super(plugin, clientThread, spriteManager, task);
	}

	//TODO: This code is wet fucking spaghetti

	@Override
	public JPopupMenu getPopupMenu()
	{
		return null;
	}

	@Override
	public String getTaskTooltip()
	{
		League3Task task = (League3Task) this.task;
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
		League3TaskTier tier = League3TaskTier.getTierByName(task.getTier());
		if (tier == null)
		{
			return null;
		}

		return spriteManager.getSprite(tier.spriteId, 0);
	}

	// TODO: Fix this file because it makes me want to vomit

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

		for (RequiredSkill requiredSkill : ((League3Task) task).skills)
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

	// TODO: Fix this later, league is in 6 hours and i need sleep

	private String getSkillSectionHtml()
	{
		StringBuilder skillSection = new StringBuilder();
		League3Task task = (League3Task) this.task;
		for (RequiredSkill requiredSkill : task.skills)
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

			//TODO: REMOVE THIS CRAP AND MAKE IT WAIT FOR SKILLS
			int playerLevel = 99;
			if (this.plugin.playerSkills != null)
			{
				playerLevel = this.plugin.playerSkills[skill.ordinal()];
			}
			skillSection.append(getSkillRequirementHtml(requiredSkill.getSkill().toLowerCase(), playerLevel, level));
		}

		return skillSection.toString();
	}

	// TODO: Yeeeeeehaw; move these responsibilities out like they're a 40 year old living w their mom

	private String getSkillRequirementHtml(String skillName, int playerLevel, int requiredLevel)
	{
		String skillIconPath = "/skill_icons_small/" + skillName + ".png";
		URL url = SkillIconManager.class.getResource(skillIconPath);
		Color color = playerLevel > requiredLevel ? QUALIFIED_TEXT_COLOR : UNQUALIFIED_TEXT_COLOR;
		return Util.imageTag(url) + " " + Util.colorTag(color, playerLevel + "/" + requiredLevel);
	}
}
