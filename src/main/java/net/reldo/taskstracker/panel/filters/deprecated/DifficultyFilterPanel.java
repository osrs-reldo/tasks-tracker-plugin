//package net.reldo.taskstracker.panel.subfilters;
//
//import java.awt.BorderLayout;
//import java.awt.GridLayout;
//import java.awt.image.BufferedImage;
//import java.util.LinkedHashMap;
//import javax.swing.JPanel;
//import javax.swing.JToggleButton;
//import javax.swing.border.EmptyBorder;
//import net.reldo.taskstracker.TasksTrackerPlugin;
//import net.reldo.taskstracker.data.task.TaskType;
//import net.runelite.client.game.SpriteManager;
//import net.runelite.client.ui.ColorScheme;
//
//public class DifficultyFilterPanel extends FilterButtonPanel
//{
//
//    public DifficultyFilterPanel(TasksTrackerPlugin plugin, SpriteManager spriteManager)
//    {
//        super(plugin, "Tier");
//        this.configKey = "tierFilter";
//
//        setLayout(new BorderLayout());
//        setBackground(ColorScheme.DARKER_GRAY_COLOR);
//        setBorder(new EmptyBorder(10, 10, 10, 10));
//
//        redraw();
//    }
//
//    @Override
//    protected JPanel makeButtonPanel()
//    {
//        // Panel that holds tier icons
//        JPanel buttonPanel = new JPanel();
//        buttonPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
//
//        LinkedHashMap<String, BufferedImage> buttonImages = getIconImages();
//
//        buttonPanel.setLayout(new GridLayout(buttonImages.size() / 3, 3));
//
//        // For each difficulty tier create a button and add it to the UI
//        buttonImages.forEach((name, image) -> {
//            JToggleButton button = makeButton(name, image);
//            buttons.put(name, button);
//            buttonPanel.add(button);
//        });
//
//        return buttonPanel;
//    }
//
//    //@todo (maybe) change this to use SpriteManager after it has apologised to me
//    @Override
//    protected LinkedHashMap<String, BufferedImage> getIconImages(TaskType taskType)
//    {
//        LinkedHashMap<String, BufferedImage> images = new LinkedHashMap<>();
//        String tierName;
//        BufferedImage tierImage;
//        String directory = "panel/components/task_tiers/";
//
//        if (taskType == null) return images;
//
//        if (taskType.getTaskJsonName().equals("LEAGUE_4"))
//        {
//            directory += "league3/";
//
//            //@fixme Hardcoded for league 4
//            for (League4TaskTier tier : League4TaskTier.values())
//            {
//                tierName = tier.name().toLowerCase();
//                String tierIcon = directory + tierName + ".png";
//                tierImage = ImageUtil.loadImageResource(TasksTrackerPlugin.class, tierIcon);
//
//                images.put(tierName, tierImage);
//            }
//        }
//
//        if (plugin.getConfig().taskType().equals(TaskType.COMBAT))
//        {
//            directory += "combat/";
//
////            for (CombatTaskTier tier : CombatTaskTier.values())
////            {
////                tierName = tier.name().toLowerCase();
////                String tierIcon = directory + tierName + ".png";
////                tierImage = ImageUtil.loadImageResource(TasksTrackerPlugin.class, tierIcon);
////
////                images.put(tierName, tierImage);
////            }
//        }
//
//        return images;
//    }
//}
