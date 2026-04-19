package net.reldo.taskstracker.panel.filters;

import com.google.common.collect.ImmutableList;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.jsondatastore.types.FilterConfig;
import net.reldo.taskstracker.data.jsondatastore.types.FilterCustomItem;
import net.reldo.taskstracker.data.jsondatastore.types.FilterValueType;
import net.reldo.taskstracker.data.task.ITaskType;
import net.runelite.client.hiscore.HiscoreSkill;
import static net.runelite.client.hiscore.HiscoreSkill.*;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

public class SkillButtonFilterPanel extends FilterButtonPanel
{
	private final FilterConfig filterConfig;

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

	public SkillButtonFilterPanel(TasksTrackerPlugin plugin, FilterConfig filterConfig, ITaskType taskType)
	{
		super(plugin, filterConfig.getLabel());
		this.filterConfig = filterConfig;
		this.configKey = taskType.getFilterConfigPrefix() + filterConfig.getConfigKey();

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(new EmptyBorder(10, 10, 10, 10));

		redraw();
	}

	@Override
	protected JPanel makeButtonPanel()
	{
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		LinkedHashMap<String, BufferedImage> buttonImages = getIconImages();
		LinkedHashMap<String, String> buttonTooltips = getTooltips();

		buttonPanel.setLayout(new GridLayout(buttonImages.size() / 3, 3));

		buttonImages.forEach((key, image) -> {
			String tooltip = buttonTooltips.get(key);
			FilterButton button = makeButton(key, tooltip, image);
			button.setSelected(getConfigButtonState(key));
			buttons.put(key, button);
			buttonPanel.add(button);
		});

		return buttonPanel;
	}

	@Override
	protected LinkedHashMap<String, BufferedImage> getIconImages()
	{
		LinkedHashMap<String, BufferedImage> images = new LinkedHashMap<>();

		if (filterConfig.getValueType() == FilterValueType.SKILL)
		{
			for (HiscoreSkill skill : SKILLS)
			{
				String key = skill.name();
				images.put(key, ImageUtil.loadImageResource(getClass(), "/skill_icons_small/" + skill.name().toLowerCase() + ".png"));
			}
			images.put("NONE", ImageUtil.loadImageResource(TasksTrackerPlugin.class, "panel/components/no_skill.png"));
		}
		else
		{
			String skillName;
			BufferedImage skillImage;
			int index = 0;

			for (FilterCustomItem customItem : filterConfig.getCustomItems())
			{
				if (customItem.getValue() != 0)
				{
					skillName = SKILLS.get(index).name().toLowerCase();

					String directory = "/skill_icons_small/";
					String skillIcon = directory + skillName + ".png";

					skillImage = ImageUtil.loadImageResource(getClass(), skillIcon);
				}
				else
				{
					skillImage = ImageUtil.loadImageResource(TasksTrackerPlugin.class, "panel/components/no_skill.png");
				}

				String key = customItem.getValue().toString();
				images.put(key, skillImage);
				index++;
			}
		}

		return images;
	}

	private LinkedHashMap<String, String> getTooltips()
	{
		LinkedHashMap<String, String> tooltips = new LinkedHashMap<>();

		if (filterConfig.getValueType() == FilterValueType.SKILL)
		{
			for (HiscoreSkill skill : SKILLS)
			{
				String name = skill.name();
				tooltips.put(name, name.charAt(0) + name.substring(1).toLowerCase());
			}
			tooltips.put("NONE", "No skill requirement");
		}
		else
		{
			for (FilterCustomItem customItem : filterConfig.getCustomItems())
			{
				String key = customItem.getValue().toString();
				tooltips.put(key, customItem.getTooltip());
			}
		}

		return tooltips;
	}
}
