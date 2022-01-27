package net.reldo.taskstracker.panel.subfilters;

import com.google.common.collect.ImmutableList;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.tasktypes.TaskType;
import net.runelite.client.hiscore.HiscoreSkill;
import static net.runelite.client.hiscore.HiscoreSkill.*;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

public class SkillFilterPanel extends FilterButtonPanel
{

    /**
     * Real skills, ordered in the way they should be displayed in the panel.
     */
    private static final List<HiscoreSkill> SKILLS = ImmutableList.of(
            ATTACK, HITPOINTS, MINING,
            STRENGTH, AGILITY, SMITHING,
            DEFENCE, HERBLORE, FISHING,
            RANGED, THIEVING, COOKING,
            PRAYER, CRAFTING, FIREMAKING,
            MAGIC, FLETCHING, WOODCUTTING,
            RUNECRAFT, SLAYER, FARMING,
            CONSTRUCTION, HUNTER
    );

    public SkillFilterPanel(TasksTrackerPlugin plugin)
    {
        super(plugin);
        this.configKey = "skillFilter";

        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        redraw();
    }

    @Override
    protected JPanel makeButtonPanel()
    {
        // Panel that holds skill icons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(8, 3));
        buttonPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        LinkedHashMap<String, BufferedImage> skillImages = getIconImages();

        // For each skill on the in-game skill panel, create a button and add it to the UI
        skillImages.forEach((name, image) -> {
            JToggleButton button = makeButton(name, image);
            buttons.put(name, button);
            buttonPanel.add(button);
        });

        buttons.get("noskill").setToolTipText("No skill requirements.");

        return buttonPanel;
    }

    @Override
    protected LinkedHashMap<String, BufferedImage> getIconImages()
    {
        LinkedHashMap<String, BufferedImage> images = new LinkedHashMap<>();
        String skillName;
        BufferedImage skillImage;

        for (HiscoreSkill skill : SKILLS)
        {
            skillName = skill.name().toLowerCase();

            String directory = "/skill_icons_small/";
            String skillIcon = directory + skillName + ".png";

            skillImage = ImageUtil.loadImageResource(getClass(), skillIcon);

            images.put(skillName, skillImage);
        }

        skillName = "noskill";
        skillImage = ImageUtil.loadImageResource(TasksTrackerPlugin.class, "panel/components/no_skill.png");
        images.put(skillName, skillImage);

        return images;
    }

    @Override
    public void redraw()
    {
        if(plugin.selectedTaskType != null)
            this.setVisible(!plugin.selectedTaskType.equals(TaskType.COMBAT));

        super.redraw();
    }
}
