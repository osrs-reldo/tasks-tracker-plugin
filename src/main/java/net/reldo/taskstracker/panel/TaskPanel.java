package net.reldo.taskstracker.panel;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerConfig;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.Util;
import net.reldo.taskstracker.config.ConfigValues.CompletedFilterValues;
import net.reldo.taskstracker.config.ConfigValues.IgnoredFilterValues;
import net.reldo.taskstracker.config.ConfigValues.TrackedFilterValues;
import net.reldo.taskstracker.panel.filters.Filter;
import net.reldo.taskstracker.tasktypes.RequiredSkill;
import net.reldo.taskstracker.tasktypes.Task;
import net.reldo.taskstracker.tasktypes.TaskType;
import net.reldo.taskstracker.tasktypes.combattask.CombatTask;
import net.reldo.taskstracker.tasktypes.combattask.CombatTaskTier;
import net.reldo.taskstracker.tasktypes.league4.League4Task;
import net.reldo.taskstracker.tasktypes.league4.League4TaskTier;
import net.runelite.api.Constants;
import net.runelite.api.Skill;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;
import net.runelite.client.util.SwingUtil;

@Slf4j
public class TaskPanel extends JPanel
{
	public final SpriteManager spriteManager;
	public final Task task;
	private final ClientThread clientThread;

	private final JLabel icon = new JLabel();
	private final JPanel container = new JPanel(new BorderLayout());
	private final JPanel body = new JPanel(new BorderLayout());
	private final JShadowedLabel name = new JShadowedLabel();
	private final JLabel description = new JLabel();
	private final JPanel buttons = new JPanel();
	private final JToggleButton toggleTrack = new JToggleButton();
	private final JToggleButton toggleIgnore = new JToggleButton();

	protected final ArrayList<Filter> filters = new ArrayList<>();

	protected TasksTrackerPlugin plugin;

	@AssistedInject
	public TaskPanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, @Assisted Task task)
	{
		super(new BorderLayout());
		this.plugin = plugin;
		this.clientThread = clientThread;
		this.spriteManager = spriteManager;
		this.task = task;
		createPanel(task);
		setComponentPopupMenu(getPopupMenu());
		ToolTipManager.sharedInstance().registerComponent(this);
		refresh();
//		this.filters.add(new SkillFilter(plugin.getConfig()));
//		this.filters.add(new TierFilter(plugin.getConfig()));
//		this.filters.add(new AreaFilter(plugin.getConfig()));
//		this.filters.add(new CategoryFilter(plugin.getConfig()));
	}

	public JPopupMenu getPopupMenu()
	{
		return null;
	}

	public String getTaskTooltip()
	{
		String text = null;
		if (task.getType() == TaskType.LEAGUE_4) {
			League4Task task = (League4Task) this.task;
			text = Util.wrapWithBold(task.getName()) + Util.HTML_LINE_BREAK +
				task.getTier() + getPointsTooltipText() + Util.HTML_LINE_BREAK +
				task.getDescription() +
				getSkillSectionHtml();

			text = Util.wrapWithWrappingParagraph(text, 200);
		} else if (task.getType() == TaskType.COMBAT) {
			String datePattern = "MM-dd-yyyy hh:mma";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
			CombatTask task = (CombatTask) this.task;
			text = Util.wrapWithBold(task.getName()) + Util.HTML_LINE_BREAK +
				task.getTier() + Util.HTML_LINE_BREAK +
				task.getMonster() + Util.HTML_LINE_BREAK +
				task.getDescription();

			if (task.isCompleted())
			{
				text += Util.HTML_LINE_BREAK + Util.HTML_LINE_BREAK + "✔ " + simpleDateFormat.format(new Date(task.getCompletedOn()));
			}

			text = Util.wrapWithWrappingParagraph(text, 200);
		}

		return Util.wrapWithHtml(text);
	}

	public BufferedImage getIcon()
	{
		int archiveId = -1;
		if (task.getType() == TaskType.COMBAT) {
			switch (task.getTier().toLowerCase()) {
				case "easy":
					archiveId = CombatTaskTier.EASY.spriteId;
					break;
				case "medium":
					archiveId = CombatTaskTier.MEDIUM.spriteId;
					break;
				case "hard":
					archiveId = CombatTaskTier.HARD.spriteId;
					break;
				case "elite":
					archiveId = CombatTaskTier.ELITE.spriteId;
					break;
				case "master":
					archiveId = CombatTaskTier.MASTER.spriteId;
					break;
				case "grandmaster":
					archiveId = CombatTaskTier.GRANDMASTER.spriteId;
					break;
			}
		} else if (task.getType() == TaskType.LEAGUE_4) {
			switch (task.getTier().toLowerCase()) {
				case "easy":
					archiveId = League4TaskTier.EASY.spriteId;
					break;
				case "medium":
					archiveId = League4TaskTier.MEDIUM.spriteId;
					break;
				case "hard":
					archiveId = League4TaskTier.HARD.spriteId;
					break;
				case "elite":
					archiveId = League4TaskTier.ELITE.spriteId;
					break;
				case "master":
					archiveId = League4TaskTier.MASTER.spriteId;
					break;
			}
		}
		// TODO: this
		if (archiveId == -1) {
			return null;
		}
		return spriteManager.getSprite(archiveId, 0);
	}

	public Color getTaskBackgroundColor(Task task, int[] playerSkills)
	{
//		// TODO (1/29/22): The required skill loop code is repeated in getSkillSectionHtml
//		//  Ideally, checking skill requirements would be a responsibility of Task
//		//  Current issue is that Task is instantiated by Gson in multiple places, so plugin may not be injected/accessible
//		@Override
//		public Color getTaskBackgroundColor(Task task, int[] playerSkills)
//		{
//			if (playerSkills == null)
//			{
//				return ColorScheme.DARKER_GRAY_COLOR;
//			}
//
//			if (task.isCompleted())
//			{
//				return Colors.COMPLETED_BACKGROUND_COLOR;
//			}
//
//			for (RequiredSkill requiredSkill : ((League4Task) task).getSkills())
//			{
//				Skill skill;
//				// FIXME: Shouldn't use exception for control flow
//				try
//				{
//					skill = Skill.valueOf(requiredSkill.skill.toUpperCase());
//				}
//				catch (IllegalArgumentException ex)
//				{
//					continue;
//				}
//
//				int level;
//				// FIXME: Shouldn't use exception for control flow
//				try
//				{
//					level = Integer.parseInt(requiredSkill.level);
//				}
//				catch (NumberFormatException ex)
//				{
//					continue;
//				}
//
//				if (playerSkills[skill.ordinal()] < level)
//				{
//					return Colors.UNQUALIFIED_BACKGROUND_COLOR;
//				}
//			}
//
//			return ColorScheme.DARKER_GRAY_COLOR;
//		}
		return task.isCompleted() ? Colors.COMPLETED_BACKGROUND_COLOR : ColorScheme.DARKER_GRAY_COLOR;
	}

	public void createPanel(Task task)
	{
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(0, 0, 7, 0));

		container.setBorder(new EmptyBorder(7, 7, 6, 0));

		// Body

		name.setFont(FontManager.getRunescapeSmallFont());
		name.setForeground(Color.WHITE);
		body.add(name, BorderLayout.NORTH);

		description.setFont(FontManager.getRunescapeSmallFont());
		description.setForeground(Color.GRAY);
		body.add(description, BorderLayout.CENTER);

		// Buttons
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
		buttons.setBorder(new EmptyBorder(0, 0, 0, 7));

		toggleTrack.setPreferredSize(new Dimension(8, 8));
		toggleTrack.setIcon(Icons.PLUS_ICON);
		toggleTrack.setSelectedIcon(Icons.MINUS_ICON);
		toggleTrack.setBorder(new EmptyBorder(5, 0, 5, 0));
		toggleTrack.addActionListener(e -> {
			task.setTracked(toggleTrack.isSelected());
			plugin.pluginPanel.taskListPanel.refresh(task);
			plugin.saveCurrentTaskData();
		});
		SwingUtil.removeButtonDecorations(toggleTrack);

		toggleIgnore.setPreferredSize(new Dimension(8, 8));
		toggleIgnore.setIcon(Icons.EYE_CROSS_GREY);
		toggleIgnore.setSelectedIcon(Icons.EYE_ICON);
		SwingUtil.addModalTooltip(toggleIgnore, "Unignore", "Ignore");
		toggleIgnore.setBorder(new EmptyBorder(5, 0, 5, 0));
		toggleIgnore.addActionListener(e -> {
			task.setIgnored(!task.isIgnored());
			plugin.pluginPanel.taskListPanel.refresh(task);
			plugin.saveCurrentTaskData();
		});
		SwingUtil.removeButtonDecorations(toggleIgnore);

		buttons.add(toggleTrack);
		buttons.add(toggleIgnore);

		// Full
		container.add(icon, BorderLayout.WEST);
		container.add(body, BorderLayout.CENTER);
		container.add(buttons, BorderLayout.EAST);

		clientThread.invoke(() -> {
			if (getIcon() != null)
			{
				icon.setMinimumSize(new Dimension(Constants.ITEM_SPRITE_WIDTH, Constants.ITEM_SPRITE_HEIGHT));
				icon.setIcon(new ImageIcon(getIcon()));
				icon.setBorder(new EmptyBorder(0, 0, 0, 5));
			}
			else
			{
				icon.setBorder(new EmptyBorder(0, 0, 0, 0));
			}
		});

		add(container, BorderLayout.NORTH);
	}

	public void refresh()
	{
		name.setText(Util.wrapWithHtml(task.getName()));
		description.setText(Util.wrapWithHtml(task.getDescription()));
		setBackgroundColor(getTaskBackgroundColor(task, plugin.playerSkills));
		toggleTrack.setSelected(task.isTracked());
		toggleIgnore.setSelected(task.isIgnored());

		setVisible(meetsFilterCriteria());

		revalidate();
	}

	protected boolean meetsFilterCriteria()
	{
		String nameLowercase = task.getName().toLowerCase();
		String descriptionLowercase = task.getDescription().toLowerCase();
		if (plugin.taskTextFilter != null &&
			!nameLowercase.contains(plugin.taskTextFilter) &&
			!descriptionLowercase.contains(plugin.taskTextFilter))
		{
			return false;
		}

		TasksTrackerConfig config = plugin.getConfig();

		for (Filter filter : filters)
		{
			if (!filter.meetsCriteria(task))
			{
				return false;
			}
		}

		if (config.completedFilter().equals(CompletedFilterValues.INCOMPLETE) && task.isCompleted())
		{
			return false;
		}
		if (config.completedFilter().equals(CompletedFilterValues.COMPLETE) && !task.isCompleted())
		{
			return false;
		}

		if (config.ignoredFilter().equals(IgnoredFilterValues.NOT_IGNORED) && task.isIgnored())
		{
			return false;
		}
		if (config.ignoredFilter().equals(IgnoredFilterValues.IGNORED) && !task.isIgnored())
		{
			return false;
		}

		if (config.trackedFilter().equals(TrackedFilterValues.UNTRACKED) && task.isTracked())
		{
			return false;
		}
		return !config.trackedFilter().equals(TrackedFilterValues.TRACKED) || task.isTracked();
	}

	private void setBackgroundColor(Color color)
	{
		container.setBackground(color);
		body.setBackground(color);
		buttons.setBackground(color);
	}

	@Override
	public Dimension getMaximumSize()
	{
		return new Dimension(PluginPanel.PANEL_WIDTH, getPreferredSize().height);
	}

	@Override
	public JToolTip createToolTip()
	{
		JToolTip customTooltip = new JToolTip();
		customTooltip.setFont(FontManager.getRunescapeSmallFont());
		return customTooltip;
	}

	@Override
	public String getToolTipText(MouseEvent mouseEvent)
	{
		return getTaskTooltip();
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
		Color color = playerLevel >= requiredLevel ? Colors.QUALIFIED_TEXT_COLOR : Colors.UNQUALIFIED_TEXT_COLOR;
		return Util.imageTag(url) + " " + Util.colorTag(color, playerLevel + "/" + requiredLevel);
	}

	private String getPointsTooltipText()
	{
		int points = this.task.getPoints();
		if (points == 0)
		{
			return "";
		}
		return " - " + points + " points";
	}
}
