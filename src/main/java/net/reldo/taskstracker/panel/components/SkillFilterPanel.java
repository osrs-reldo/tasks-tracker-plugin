package net.reldo.taskstracker.panel.components;

import com.google.common.collect.ImmutableList;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.hiscore.HiscoreSkill;
import static net.runelite.client.hiscore.HiscoreSkill.*;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

public class SkillFilterPanel extends FixedWidthPanel
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

    private final TasksTrackerPlugin plugin;

    private final Map<String, JToggleButton> skillButtons = new HashMap<>();

    public SkillFilterPanel(TasksTrackerPlugin plugin)
    {
        this.plugin = plugin;

        // Panel that holds skill icons
        setLayout(new GridLayout(8, 3));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // For each skill on the in-game skill panel, create a button and add it to the UI
        for (HiscoreSkill skill : SKILLS)
        {
            JToggleButton button = makeSkillButton(skill);
            skillButtons.put(skill.getName().toLowerCase(), button);
            add(button);
        }

        JToggleButton button = makeSkillButton(null);
        button.setToolTipText("No skill requirements.");
        skillButtons.put("noskill", button);
        add(button);

        updateSkillFilter();
    }

    private JToggleButton makeSkillButton(HiscoreSkill skill)
    {
        String skillName;

        skillName = (skill == null) ? "noskill" :  skill.name().toLowerCase();

        JToggleButton button = new JToggleButton();
        button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        button.setBorder(new EmptyBorder(2, 0, 2, 0));

        String directory = "/skill_icons_small/";//_small
        String skillIcon = directory + skillName + ".png";

        BufferedImage skillImage;
        if(skillName.equals("noskill"))
        {
            skillImage = ImageUtil.loadImageResource(TasksTrackerPlugin.class, "panel/components/no_skill.png");
        }
        else
        {
            skillImage = ImageUtil.loadImageResource(getClass(), skillIcon);
        }

        ImageIcon selectedIcon = new ImageIcon(skillImage);
        ImageIcon deselectedIcon = new ImageIcon(ImageUtil.alphaOffset(skillImage, -180));

        button.setIcon(deselectedIcon);
        button.setSelectedIcon(selectedIcon);
        button.setToolTipText(skillName.substring(0,1).toUpperCase() + skillName.substring(1).toLowerCase());

        button.addActionListener(e -> {
            updateSkillFilter();
            plugin.refresh();
        });

        button.setSelected(true);

        return button;
    }

    private void updateSkillFilter()
    {
        String skillFilter = skillButtons.entrySet().stream()
                                .filter(e -> e.getValue().isSelected())
                                .map(Map.Entry::getKey)
//                                .reduce("", (a, b) -> a + "," + b);
                                .collect(Collectors.joining(","));

        plugin.getConfigManager().setConfiguration("tasks-tracker", "skillFilter", skillFilter);
    }

    private void setAllSelected(boolean state)
    {
        skillButtons.values().forEach(button -> button.setSelected(state));
    }
}
