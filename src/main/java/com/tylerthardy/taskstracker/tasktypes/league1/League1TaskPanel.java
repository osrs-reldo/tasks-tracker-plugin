package com.tylerthardy.taskstracker.tasktypes.league1;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.tasktypes.RequiredSkill;
import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.TaskPanel;
import net.runelite.api.Skill;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;

import javax.swing.JPopupMenu;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class League1TaskPanel extends TaskPanel
{
    public League1TaskPanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, Task task) {
        super(plugin, clientThread, spriteManager, task);
    }

    @Override
    public JPopupMenu getPopupMenu() {
        return null;
    }

    @Override
    public String getTaskTooltip() {
        return task.getDescription();
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
}
