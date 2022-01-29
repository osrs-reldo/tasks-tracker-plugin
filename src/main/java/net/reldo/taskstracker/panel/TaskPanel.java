package net.reldo.taskstracker.panel;

import net.reldo.taskstracker.TasksTrackerConfig;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.Util;
import net.reldo.taskstracker.config.ConfigValues.IgnoredFilterValues;
import net.reldo.taskstracker.config.ConfigValues.CompletedFilterValues;
import net.reldo.taskstracker.config.ConfigValues.TrackedFilterValues;
import net.reldo.taskstracker.tasktypes.Task;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
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
import net.runelite.api.Constants;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;

@Slf4j
public abstract class TaskPanel extends JPanel
{
	public static Color QUALIFIED_TEXT_COLOR = new Color(34, 177, 77);
	public static Color UNQUALIFIED_TEXT_COLOR = new Color(251, 93, 93);
	public static Color COMPLETED_BACKGROUND_COLOR = new Color(0, 50, 0);
	public static Color UNQUALIFIED_BACKGROUND_COLOR = new Color(70, 30, 0);

	public static ImageIcon PLUS_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, "plus.png"));
	public static ImageIcon MINUS_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, "minus.png"));
	public static ImageIcon EYE_ICON = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, "eye.png"));
	public static ImageIcon EYE_CROSS_GREY = new ImageIcon(ImageUtil.loadImageResource(TasksTrackerPlugin.class, "eye-cross-grey.png"));

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

	protected TasksTrackerPlugin plugin;

	public TaskPanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, Task task)
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
	}

	public abstract JPopupMenu getPopupMenu();

	public abstract String getTaskTooltip();

	public abstract BufferedImage getIcon();

	public Color getTaskBackgroundColor(Task task, int[] playerSkills)
	{
		return task.isCompleted() ? COMPLETED_BACKGROUND_COLOR : ColorScheme.DARKER_GRAY_COLOR;
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
		toggleTrack.setIcon(PLUS_ICON);
		toggleTrack.setSelectedIcon(MINUS_ICON);
		toggleTrack.setBorder(new EmptyBorder(5, 0, 5, 0));
		toggleTrack.addActionListener(e -> {
			task.setTracked(toggleTrack.isSelected());
			plugin.pluginPanel.taskListPanel.refresh(task);
			plugin.saveCurrentTaskData();
		});
		SwingUtil.removeButtonDecorations(toggleTrack);

		toggleIgnore.setPreferredSize(new Dimension(8, 8));
		toggleIgnore.setIcon(EYE_CROSS_GREY);
		toggleIgnore.setSelectedIcon(EYE_ICON);
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
}
