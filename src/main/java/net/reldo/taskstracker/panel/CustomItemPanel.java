package net.reldo.taskstracker.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.HtmlUtil;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.config.ConfigValues;
import net.reldo.taskstracker.data.TrackerGlobalConfigStore;
import net.reldo.taskstracker.data.route.CustomRoute;
import net.reldo.taskstracker.data.route.CustomRouteItem;
import net.reldo.taskstracker.data.route.RouteItem;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

@Slf4j
public class CustomItemPanel extends JPanel
{
	private static final Color BORDER_COLOR = new Color(90, 90, 90);
	private static final Color BACKGROUND_DEFAULT = ColorScheme.DARKER_GRAY_COLOR;
	private static final Color BACKGROUND_HOVER = ColorScheme.DARK_GRAY_HOVER_COLOR;
	private static final Color BACKGROUND_COMPLETED = new Color(40, 60, 40);

	private final TasksTrackerPlugin plugin;
	private final RouteItem routeItem;
	private final CustomRouteItem customItem;
	private final JPanel container;
	private final JLabel iconLabel;
	private final JToggleButton completeToggle;

	@Getter
	private boolean completed = false;

	public CustomItemPanel(TasksTrackerPlugin plugin, RouteItem routeItem)
	{
		super(new BorderLayout());
		this.plugin = plugin;
		this.routeItem = routeItem;
		this.customItem = routeItem.getCustomItem();

		setOpaque(false);
		setBorder(new EmptyBorder(0, 0, 7, 0));

		container = new JPanel(new BorderLayout());
		container.setBorder(new CompoundBorder(
			new MatteBorder(0, 3, 0, 0, BORDER_COLOR),
			new EmptyBorder(7, 4, 6, 0)
		));
		container.setBackground(BACKGROUND_DEFAULT);

		iconLabel = new JLabel();
		iconLabel.setPreferredSize(new Dimension(16, 16));
		setFallbackIcon();
		loadSpriteIcon();

		JLabel nameLabel = new JLabel();
		nameLabel.setFont(FontManager.getRunescapeSmallFont());
		nameLabel.setForeground(Color.WHITE);
		nameLabel.setText(customItem.getDisplayLabel());

		JLabel descriptionLabel = new JLabel();
		descriptionLabel.setFont(FontManager.getRunescapeSmallFont());
		descriptionLabel.setForeground(Color.GRAY);
		String desc = customItem.getDescription();
		if (desc != null && !desc.isEmpty())
		{
			descriptionLabel.setText("<html>" + desc + "</html>");
		}

		completeToggle = new JToggleButton();
		completeToggle.setIcon(Icons.INCOMPLETE_ONLY_ICON);
		completeToggle.setSelectedIcon(Icons.COMPLETE_ONLY_ICON);
		completeToggle.setPreferredSize(new Dimension(20, 20));
		completeToggle.setBorder(new EmptyBorder(0, 0, 0, 0));
		completeToggle.setBorderPainted(false);
		completeToggle.setFocusPainted(false);
		completeToggle.setContentAreaFilled(false);
		completeToggle.setToolTipText("Toggle completion");
		completeToggle.addActionListener(e -> onCompletionToggled());

		JPanel body = new JPanel(new BorderLayout());
		body.setOpaque(false);
		body.setBorder(new EmptyBorder(0, 6, 0, 0));
		body.add(nameLabel, BorderLayout.NORTH);
		body.add(descriptionLabel, BorderLayout.CENTER);

		JPanel left = new JPanel(new BorderLayout());
		left.setOpaque(false);
		left.add(iconLabel, BorderLayout.WEST);
		left.add(body, BorderLayout.CENTER);

		container.add(left, BorderLayout.CENTER);
		container.add(completeToggle, BorderLayout.EAST);

		add(container, BorderLayout.NORTH);

		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				if (!CustomItemPanel.this.completed)
				{
					container.setBackground(BACKGROUND_HOVER);
				}
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				container.setBackground(CustomItemPanel.this.completed ? BACKGROUND_COMPLETED : BACKGROUND_DEFAULT);
			}
		});

		ToolTipManager.sharedInstance().registerComponent(this);
	}

	private void setFallbackIcon()
	{
		String letter = "C";
		Color color = new Color(180, 180, 180);

		String label = customItem.getLabel();
		if (label != null && !label.isEmpty())
		{
			letter = label.substring(0, 1).toUpperCase();
			if ("Bank".equals(label))
			{
				color = new Color(255, 215, 0);
			}
			else if ("Teleport".equals(label))
			{
				color = new Color(100, 200, 255);
			}
		}

		iconLabel.setIcon(new ImageIcon(Icons.createTextIcon(letter, color)));
	}

	private void loadSpriteIcon()
	{
		Integer spriteId = customItem.getIcon();
		if (spriteId == null)
		{
			return;
		}
		plugin.getSpriteManager().addSpriteTo(iconLabel, spriteId, 0);
	}

	private void onCompletionToggled()
	{
		completed = completeToggle.isSelected();
		updateAppearance();

		String taskType = plugin.getTaskService().getCurrentTaskType().getTaskJsonName();
		ConfigValues.TaskListTabs currentTab = plugin.getConfig().taskListTab();
		CustomRoute activeRoute = plugin.getTaskService().getActiveRoute(currentTab);
		if (activeRoute == null)
		{
			return;
		}

		TrackerGlobalConfigStore configStore = plugin.getTrackerGlobalConfigStore();
		Set<String> completedIds = configStore.loadCustomItemCompletion(taskType, activeRoute.getName());
		if (completed)
		{
			completedIds.add(customItem.getId());
		}
		else
		{
			completedIds.remove(customItem.getId());
		}
		configStore.saveCustomItemCompletion(taskType, activeRoute.getName(), completedIds);

		// Full redraw to update section progress and overlay priority
		plugin.pluginPanel.redraw();
	}

	private void updateAppearance()
	{
		container.setBackground(completed ? BACKGROUND_COMPLETED : BACKGROUND_DEFAULT);
	}

	public void setCompleted(boolean completed)
	{
		this.completed = completed;
		completeToggle.setSelected(completed);
		updateAppearance();
	}

	public String getCustomItemId()
	{
		return customItem.getId();
	}

	public WorldPoint getWorldLocation()
	{
		return routeItem.getLocation();
	}

	public void refresh(Set<String> completedIds)
	{
		boolean isComplete = completedIds.contains(customItem.getId());
		setCompleted(isComplete);
	}

	@Override
	public JToolTip createToolTip()
	{
		JToolTip tooltip = super.createToolTip();
		tooltip.setBorder(new EmptyBorder(5, 5, 5, 5));
		return tooltip;
	}

	@Override
	public String getToolTipText()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(HtmlUtil.wrapWithBold(customItem.getDisplayLabel()));
		if (customItem.getDescription() != null && !customItem.getDescription().isEmpty())
		{
			sb.append(HtmlUtil.HTML_LINE_BREAK).append(customItem.getDescription());
		}

		String note = routeItem.getNote();
		if (note != null && !note.isEmpty())
		{
			sb.append(HtmlUtil.HTML_LINE_BREAK).append(HtmlUtil.HTML_LINE_BREAK);
			sb.append(HtmlUtil.wrapWithItalics(note));
		}

		return HtmlUtil.wrapWithHtml(
			HtmlUtil.wrapWithWrappingParagraph(sb.toString(), 200)
		);
	}

	public void buildOverlayText(Graphics2D graphics, PanelComponent panelComponent)
	{
		panelComponent.setBackgroundColor(ComponentConstants.STANDARD_BACKGROUND_COLOR);

		String title = customItem.getDisplayLabel();
		String desc = customItem.getDescription();

		final FontMetrics fontMetrics = graphics.getFontMetrics();
		int panelWidth = Math.max(ComponentConstants.STANDARD_WIDTH, fontMetrics.stringWidth(title) +
			ComponentConstants.STANDARD_BORDER + ComponentConstants.STANDARD_BORDER);

		panelComponent.setPreferredSize(new Dimension(panelWidth, 0));
		panelComponent.getChildren().add(TitleComponent.builder()
			.text(title)
			.build());

		if (desc != null && !desc.isEmpty())
		{
			panelComponent.getChildren().add(LineComponent.builder().build());
			panelComponent.getChildren().add(LineComponent.builder()
				.left(desc)
				.build());
		}

		String note = routeItem.getNote();
		if (note != null && !note.isEmpty())
		{
			panelComponent.getChildren().add(LineComponent.builder().build());
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Note:")
				.build());
			panelComponent.getChildren().add(LineComponent.builder()
				.left(note)
				.leftFont(FontManager.getRunescapeFont().deriveFont(java.awt.Font.ITALIC))
				.build());
		}
	}
}
