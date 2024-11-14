// TODO: Reimplement under new FilterConfig regime
//package net.reldo.taskstracker.panel.filters;
//
//
//import com.google.common.collect.ImmutableList;
//import java.awt.image.BufferedImage;
//import java.util.LinkedHashMap;
//import java.util.List;
//import net.reldo.taskstracker.TasksTrackerPlugin;
//import net.reldo.taskstracker.data.jsondatastore.types.FilterConfig;
//import net.reldo.taskstracker.data.jsondatastore.types.FilterCustomItem;
//import net.reldo.taskstracker.data.task.TaskType;
//import net.runelite.client.hiscore.HiscoreSkill;
//import static net.runelite.client.hiscore.HiscoreSkill.*;
//import net.runelite.client.util.ImageUtil;
//
//public class SkillFilterPanel extends DynamicButtonFilterPanel
//{
//
//    /**
//     * Real skills, ordered in the way they should be displayed in the panel.
//     */
//    private static final List<HiscoreSkill> SKILLS = ImmutableList.of(
//            ATTACK, HITPOINTS, MINING,
//            STRENGTH, AGILITY, SMITHING,
//            DEFENCE, HERBLORE, FISHING,
//            RANGED, THIEVING, COOKING,
//            PRAYER, CRAFTING, FIREMAKING,
//            MAGIC, FLETCHING, WOODCUTTING,
//            RUNECRAFT, SLAYER, FARMING,
//            CONSTRUCTION, HUNTER
//    );
//
//    public SkillFilterPanel(TasksTrackerPlugin plugin, FilterConfig filterConfig, TaskType taskType)
//    {
//        super(plugin, filterConfig, taskType);
//    }
//
//    @Override
//    protected LinkedHashMap<String, BufferedImage> getIconImages()
//    {
//        LinkedHashMap<String, BufferedImage> images = new LinkedHashMap<>();
//        String skillName;
//        BufferedImage skillImage;
//
//        for (HiscoreSkill skill : SKILLS)
//        {
//            skillName = skill.name().toLowerCase();
//
//            String directory = "/skill_icons_small/";
//            String skillIcon = directory + skillName + ".png";
//
//            skillImage = ImageUtil.loadImageResource(getClass(), skillIcon);
//
//            images.put(skillName, skillImage);
//        }
//
//        skillName = "noskill";
//        skillImage = ImageUtil.loadImageResource(TasksTrackerPlugin.class, "panel/components/no_skill.png");
//        images.put(skillName, skillImage);
//
//        return images;
//    }
//
//    @Override
//    public void redraw()
//    {
//        if (plugin.getConfig().taskType() != null)
//            this.setVisible(!plugin.getConfig().taskType().equals(TaskType.COMBAT));
//
//        super.redraw();
//    }
//}
