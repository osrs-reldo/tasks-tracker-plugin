package net.reldo.taskstracker.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;
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
	private static final Color LABEL_COLOR = Color.WHITE;
	private static final Color NAME_COLOR = Color.GRAY;
	private static final int ICON_SIZE = 16;

	private final TasksTrackerPlugin plugin;
	private final RouteItem routeItem;
	private final CustomRouteItem customItem;
	private final HashMap<Integer, BufferedImage> spriteCache;

	private final JLabel iconLabel;
	private final JCheckBox completionCheckbox;
	private boolean iconLoaded = false;

	@Getter
	private boolean completed = false;

	public CustomItemPanel(TasksTrackerPlugin plugin, RouteItem routeItem, HashMap<Integer, BufferedImage> spriteCache)
	{
		super(new BorderLayout());
		this.plugin = plugin;
		this.routeItem = routeItem;
		this.customItem = routeItem.getCustomItem();
		this.spriteCache = spriteCache;

		// Outer panel: transparent, provides bottom gap (matching TaskPanel)
		setOpaque(false);
		setBorder(new EmptyBorder(0, 0, 7, 0));

		// Inner container with left accent border
		JPanel container = new JPanel(new BorderLayout());
		container.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		container.setBorder(new MatteBorder(0, 3, 0, 0, BORDER_COLOR));

		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.setOpaque(false);
		contentPanel.setBorder(new EmptyBorder(7, 7, 6, 5));

		// Icon (WEST)
		iconLabel = createIconLabel();
		iconLabel.setBorder(new EmptyBorder(0, 0, 0, 5));
		contentPanel.add(iconLabel, BorderLayout.WEST);

		// Body (CENTER) - label + name stacked
		JPanel body = new JPanel();
		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
		body.setOpaque(false);

		JLabel labelText = new JLabel(customItem.getDisplayLabel());
		labelText.setFont(FontManager.getRunescapeBoldFont());
		labelText.setForeground(LABEL_COLOR);
		body.add(labelText);

		if (customItem.getDescription() != null && !customItem.getDescription().isEmpty())
		{
			JLabel nameText = new JLabel(customItem.getDescription());
			nameText.setFont(FontManager.getRunescapeSmallFont());
			nameText.setForeground(NAME_COLOR);
			body.add(nameText);
		}

		contentPanel.add(body, BorderLayout.CENTER);

		// Completion checkbox (EAST) - scaled down to ~66%
		completionCheckbox = new JCheckBox();
		completionCheckbox.setOpaque(false);
		completionCheckbox.setBorderPainted(false);
		completionCheckbox.setFocusPainted(false);
		completionCheckbox.setPreferredSize(new Dimension(14, 14));
		completionCheckbox.setMaximumSize(new Dimension(14, 14));
		completionCheckbox.addActionListener(e -> onCompletionToggled());

		JPanel checkboxWrapper = new JPanel(new BorderLayout());
		checkboxWrapper.setOpaque(false);
		checkboxWrapper.setBorder(new EmptyBorder(0, 0, 0, 2));
		checkboxWrapper.add(completionCheckbox, BorderLayout.NORTH);

		contentPanel.add(checkboxWrapper, BorderLayout.EAST);

		container.add(contentPanel, BorderLayout.CENTER);
		add(container, BorderLayout.NORTH);

		ToolTipManager.sharedInstance().registerComponent(this);
	}

	private JLabel createIconLabel()
	{
		JLabel label = new JLabel();
		label.setMinimumSize(new Dimension(ICON_SIZE, ICON_SIZE));
		label.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
		setIconFromCache(label);
		return label;
	}

	/**
	 * Tries to load the sprite from cache. If not available, renders a letter fallback.
	 * Called on creation and again from updateIcon() when sprites finish loading.
	 */
	private void setIconFromCache(JLabel label)
	{
		if (customItem.getIcon() != null)
		{
			BufferedImage sprite = spriteCache.get(customItem.getIcon());
			if (sprite != null)
			{
				label.setIcon(new ImageIcon(sprite));
				iconLoaded = true;
				return;
			}
		}

		// Letter fallback: first letter of label on a colored square
		String displayLabel = customItem.getDisplayLabel();
		if (!displayLabel.isEmpty())
		{
			BufferedImage letterIcon = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = letterIcon.createGraphics();
			g.setColor(BORDER_COLOR);
			g.fillRect(0, 0, ICON_SIZE, ICON_SIZE);
			g.setColor(Color.WHITE);
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
			FontMetrics fm = g.getFontMetrics();
			String letter = displayLabel.substring(0, 1).toUpperCase();
			int x = (ICON_SIZE - fm.stringWidth(letter)) / 2;
			int y = (ICON_SIZE + fm.getAscent() - fm.getDescent()) / 2;
			g.drawString(letter, x, y);
			g.dispose();
			label.setIcon(new ImageIcon(letterIcon));
		}
	}

	/**
	 * Re-checks the sprite cache and updates the icon if sprites have loaded since creation.
	 */
	public void updateIcon()
	{
		if (!iconLoaded && customItem.getIcon() != null)
		{
			setIconFromCache(iconLabel);
		}
	}

	private void onCompletionToggled()
	{
		completed = completionCheckbox.isSelected();

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

	public void setCompleted(boolean completed)
	{
		this.completed = completed;
		completionCheckbox.setSelected(completed);
	}

	public String getCustomItemId()
	{
		return customItem.getId();
	}

	public void refresh(Set<String> completedIds)
	{
		boolean isComplete = completedIds.contains(customItem.getId());
		setCompleted(isComplete);
		updateIcon();
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
	}
}
