package com.tylerthardy.taskstracker.tasktypes.league1;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.Util;
import com.tylerthardy.taskstracker.tasktypes.RequiredSkill;
import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.panel.TaskPanel;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.swing.JPopupMenu;
import net.runelite.api.Skill;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;

public class League1TaskPanel extends TaskPanel
{
	private SkillIconManager skillIconManager;

	public League1TaskPanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, SkillIconManager skillIconManager, Task task) {
        super(plugin, clientThread, spriteManager, task);
		this.skillIconManager = skillIconManager;
	}

    @Override
    public JPopupMenu getPopupMenu() {
        return null;
    }

    @Override
    public String getTaskTooltip() {
		League1Task task = (League1Task) this.task;
		String text = Util.wrapWithBold(task.getName()) + Util.HTML_LINE_BREAK +
			task.getTier() + Util.HTML_LINE_BREAK +
			task.getDescription() +
			getSkillSectionHtml();

		text = Util.wrapWithWrappingParagraph(text, 200);

		return Util.wrapWithHtml(text);
    }

    @Override
    public BufferedImage getIcon() {
        League1TaskTier tier = League1TaskTier.getTierByName(task.getTier());
        if (tier == null)
        {
            return null;
        }

        return spriteManager.getSprite(tier.spriteId, 0);
    }

    @Override
    public Color getTaskBackgroundColor(Task task, int[] playerSkills)
    {
        if (playerSkills == null) return ColorScheme.DARKER_GRAY_COLOR;

        if (task.isCompleted()) return COMPLETED_COLOR;

        for (RequiredSkill requiredSkill : ((League1Task) task).skills) {
            Skill skill;
            // FIXME: Shouldn't use exception for control flow
            try {
                skill = Skill.valueOf(requiredSkill.skill.toUpperCase());
            } catch (IllegalArgumentException ex) {
                continue;
            }

            int level;
            // FIXME: Shouldn't use exception for control flow
            try {
                level = Integer.parseInt(requiredSkill.level);
            } catch (NumberFormatException ex) {
                continue;
            }

            if (playerSkills[skill.ordinal()] < level)
            {
                setToolTipText(playerSkills[skill.ordinal()] + ":" + level);
                return UNQUALIFIED_COLOR;
            }
        }

        return ColorScheme.DARKER_GRAY_COLOR;
    }

    private String getSkillSectionHtml()
	{
		StringBuilder skillSection = new StringBuilder();
		League1Task task = (League1Task) this.task;
		for (RequiredSkill requiredSkill : task.skills)
		{
			skillSection.append(Util.HTML_LINE_BREAK);
			skillSection.append(getSkillRequirementHtml(requiredSkill.getSkill().toLowerCase(), requiredSkill.getLevel()));
		}

		return skillSection.toString();
	}

	private String getSkillRequirementHtml(String skillName, String level)
	{
		String skillIconPath = "/skill_icons_small/" + skillName + ".png";
		URL url = SkillIconManager.class.getResource(skillIconPath);
		return Util.imageTag(url) + " " + level;
	}
}
