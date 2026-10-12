package net.reldo.taskstracker.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.HtmlUtil;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.config.ConfigValues;
import net.reldo.taskstracker.data.TrackerGlobalConfigStore;
import net.reldo.taskstracker.data.route.CustomRoute;
import net.reldo.taskstracker.data.route.CustomRouteItem;
import net.reldo.taskstracker.data.route.RouteItem;
import net.runelite.api.Quest;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.util.SwingUtil;

@Slf4j
public class CustomItemPanel extends JPanel
{
	private static final Color BORDER_COLOR = new Color(90, 90, 90);
	private static final Color BACKGROUND_DEFAULT = ColorScheme.DARKER_GRAY_COLOR;
	private static final Color BACKGROUND_COMPLETED = new Color(40, 60, 40);

	private static final int MAX_ICON_DIMENSION = 22;

	private static final String WIKI_BASE_URL = "https://oldschool.runescape.wiki/w/";

	// Quests whose RuneLite name doesn't match their wiki page title
	private static final Map<Quest, String> QUEST_WIKI_PAGE_OVERRIDES = Map.ofEntries(
		Map.entry(Quest.RECIPE_FOR_DISASTER__ANOTHER_COOKS_QUEST, "Recipe for Disaster/Another Cook's Quest"),
		Map.entry(Quest.RECIPE_FOR_DISASTER__MOUNTAIN_DWARF, "Recipe for Disaster/Freeing the Mountain Dwarf"),
		Map.entry(Quest.RECIPE_FOR_DISASTER__WARTFACE__BENTNOZE, "Recipe for Disaster/Freeing the Goblin generals"),
		Map.entry(Quest.RECIPE_FOR_DISASTER__PIRATE_PETE, "Recipe for Disaster/Freeing Pirate Pete"),
		Map.entry(Quest.RECIPE_FOR_DISASTER__LUMBRIDGE_GUIDE, "Recipe for Disaster/Freeing the Lumbridge Guide"),
		Map.entry(Quest.RECIPE_FOR_DISASTER__EVIL_DAVE, "Recipe for Disaster/Freeing Evil Dave"),
		Map.entry(Quest.RECIPE_FOR_DISASTER__SKRACH_UGLOGWEE, "Recipe for Disaster/Freeing Skrach Uglogwee"),
		Map.entry(Quest.RECIPE_FOR_DISASTER__SIR_AMIK_VARZE, "Recipe for Disaster/Freeing Sir Amik Varze"),
		Map.entry(Quest.RECIPE_FOR_DISASTER__KING_AWOWOGEI, "Recipe for Disaster/Freeing King Awowogei"),
		Map.entry(Quest.RECIPE_FOR_DISASTER__CULINAROMANCER, "Recipe for Disaster/Defeating the Culinaromancer")
	);

	// Quests without a "<wiki page>/Quick guide" subpage
	private static final Map<Quest, String> QUEST_QUICK_GUIDE_OVERRIDES = Map.of(
		Quest.RECIPE_FOR_DISASTER__CULINAROMANCER, "Recipe for Disaster/Quick guide#The final battles"
	);

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
			new EmptyBorder(7, 2, 6, 0)
		));
		container.setBackground(BACKGROUND_DEFAULT);

		iconLabel = new JLabel();
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
		plugin.getSpriteManager().getSpriteAsync(Icons.UNCHECKED_ICON, 0, img -> {
			SwingUtilities.invokeLater(() -> {
				completeToggle.setIcon(new ImageIcon(img));
			});
		});
		plugin.getSpriteManager().getSpriteAsync(Icons.CHECKED_ICON, 0, img -> {
			SwingUtilities.invokeLater(() -> {
				completeToggle.setSelectedIcon(new ImageIcon(img));
			});
		});
		completeToggle.setPreferredSize(new Dimension(17, 17));
		completeToggle.setBorder(new EmptyBorder(0, 0, 0, 0));
		completeToggle.setFocusable(false);
		completeToggle.setToolTipText("Toggle completion");
		completeToggle.addActionListener(e -> onCompletionToggled());
		SwingUtil.removeButtonDecorations(completeToggle);

		if (customItem.getQuestId() != null)
		{
			completeToggle.setVisible(false);

			Quest quest = findQuest(customItem.getQuestId());
			if (quest != null)
			{
				setComponentPopupMenu(createQuestPopupMenu(quest));
			}
		}

		JPanel body = new JPanel(new BorderLayout());
		body.setOpaque(false);
		body.setBorder(new EmptyBorder(0, 1, 0, 0));
		body.add(nameLabel, BorderLayout.NORTH);
		body.add(descriptionLabel, BorderLayout.CENTER);

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.setOpaque(false);
		rightPanel.setBorder(new EmptyBorder(0, 0, 0, 7));
		rightPanel.add(completeToggle, BorderLayout.CENTER);

		container.add(iconLabel, BorderLayout.WEST);
		container.add(body, BorderLayout.CENTER);
		container.add(rightPanel, BorderLayout.EAST);

		add(container, BorderLayout.NORTH);

		ToolTipManager.sharedInstance().registerComponent(this);
	}

	private static Quest findQuest(int questId)
	{
		return Arrays.stream(Quest.values())
			.filter(quest -> quest.getId() == questId)
			.findFirst()
			.orElse(null);
	}

	private JPopupMenu createQuestPopupMenu(Quest quest)
	{
		JPopupMenu popupMenu = new JPopupMenu();

		String wikiPage = QUEST_WIKI_PAGE_OVERRIDES.getOrDefault(quest, quest.getName());
		String quickGuidePage = QUEST_QUICK_GUIDE_OVERRIDES.getOrDefault(quest, wikiPage + "/Quick guide");

		JMenuItem openWikiItem = new JMenuItem("Wiki");
		openWikiItem.addActionListener(e -> LinkBrowser.browse(getWikiUrl(wikiPage)));
		popupMenu.add(openWikiItem);

		JMenuItem openQuickGuideItem = new JMenuItem("Wiki Quick Guide");
		openQuickGuideItem.addActionListener(e -> LinkBrowser.browse(getWikiUrl(quickGuidePage)));
		popupMenu.add(openQuickGuideItem);

		JMenuItem startQuestHelperItem = new JMenuItem("Start Quest Helper");
		startQuestHelperItem.addActionListener(e -> plugin.getQuestHelperService().startQuestHelper(quest));
		popupMenu.add(startQuestHelperItem);

		// Only offer Quest Helper while it is installed and enabled
		popupMenu.addPopupMenuListener(new PopupMenuListener()
		{
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e)
			{
				startQuestHelperItem.setVisible(plugin.getQuestHelperService().isQuestHelperEnabled());
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
			{
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e)
			{
			}
		});

		return popupMenu;
	}

	private static String getWikiUrl(String page)
	{
		String anchor = "";
		int anchorIndex = page.indexOf('#');
		if (anchorIndex >= 0)
		{
			anchor = page.substring(anchorIndex).replace(' ', '_');
			page = page.substring(0, anchorIndex);
		}

		// Encode each path segment separately so subpage separators are preserved
		String path = Arrays.stream(page.replace(' ', '_').split("/"))
			.map(segment -> URLEncoder.encode(segment, StandardCharsets.UTF_8))
			.collect(Collectors.joining("/"));

		return WIKI_BASE_URL + path + anchor;
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
			else if ("Quest".equals(label))
			{
				color = new Color(135, 151, 230);
			}
		}

		iconLabel.setIcon(new ImageIcon(Icons.createTextIcon(letter, color)));
		iconLabel.setBorder(new EmptyBorder(0, 2, 0, 4));
	}

	private void loadSpriteIcon()
	{
		Integer spriteId = customItem.getIcon();
		if (spriteId == null)
		{
			return;
		}
		plugin.getSpriteManager().getSpriteAsync(spriteId, 0, img -> {
			SwingUtilities.invokeLater(() -> {
				BufferedImage finalImg = img;
				if (img.getHeight() > MAX_ICON_DIMENSION || img.getWidth() > MAX_ICON_DIMENSION)
				{
					finalImg = ImageUtil.resizeImage(img, MAX_ICON_DIMENSION, MAX_ICON_DIMENSION);
				}

				iconLabel.setIcon(new ImageIcon(finalImg));

				int offsetX = (MAX_ICON_DIMENSION - finalImg.getWidth());
				int offsetLeft = (int)Math.floor((double)offsetX / 2);
				int offsetRight = offsetX - offsetLeft;
				iconLabel.setBorder(new EmptyBorder(0, offsetLeft, 0, offsetRight));
			});
		});
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
		Set<String> completedIds = configStore.loadCustomItemCompletion(taskType, activeRoute.getId());
		if (completed)
		{
			completedIds.add(customItem.getId());
		}
		else
		{
			completedIds.remove(customItem.getId());
		}
		configStore.saveCustomItemCompletion(taskType, activeRoute.getId(), completedIds);

		// Full redraw to update section progress and overlay priority
		plugin.refreshAllTasks();
	}

	private void updateAppearance()
	{
		container.setBackground(completed ? BACKGROUND_COMPLETED : BACKGROUND_DEFAULT);
	}

	public void setCompleted(boolean completed)
	{
		completeToggle.setSelected(completed);
		onCompletionToggled();
	}

	public void setCompletedSilent(boolean completed)
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
		setCompletedSilent(isComplete);
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
			sb.append(HtmlUtil.HTML_LINE_BREAK).append(customItem.getDescription().replace("\n", HtmlUtil.HTML_LINE_BREAK));
		}

		String note = routeItem.getNote();
		if (note != null && !note.isEmpty())
		{
			sb.append(HtmlUtil.HTML_LINE_BREAK).append(HtmlUtil.HTML_LINE_BREAK);
			sb.append(HtmlUtil.wrapWithItalics(note.replace("\n", HtmlUtil.HTML_LINE_BREAK)));
		}

		return HtmlUtil.wrapWithHtml(
			HtmlUtil.wrapWithWrappingParagraph(sb.toString(), 200)
		);
	}

	public void buildOverlayText(PanelComponent panelComponent)
	{
		panelComponent.setBackgroundColor(ComponentConstants.STANDARD_BACKGROUND_COLOR);

		if (plugin.getConfig().dynamicOverlayPanelColourEnabled())
		{
			Color taskColour = container.getBackground();
			Color overlayColour = new Color(
				taskColour.getRed(),
				taskColour.getGreen(),
				taskColour.getBlue(),
				ComponentConstants.STANDARD_BACKGROUND_COLOR.getAlpha());
			panelComponent.setBackgroundColor(overlayColour);
		}
		else
		{
			panelComponent.setBackgroundColor(ComponentConstants.STANDARD_BACKGROUND_COLOR);
		}

		String title = customItem.getDisplayLabel();
		String desc = customItem.getDescription();

		panelComponent.setPreferredSize(new Dimension(ComponentConstants.STANDARD_WIDTH, 0));
		panelComponent.getChildren().add(LineComponent.builder()
			.left(title)
			.build());

		if (desc != null && !desc.isEmpty())
		{
			panelComponent.getChildren().add(LineComponent.builder().build());
			for (String line : desc.split("\n"))
			{
				panelComponent.getChildren().add(LineComponent.builder()
					.left(line)
					.build());
			}
		}

		String note = routeItem.getNote();
		if (note != null && !note.isEmpty())
		{
			panelComponent.getChildren().add(LineComponent.builder().build());
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Note:")
				.build());
			for (String line : note.split("\n"))
			{
				panelComponent.getChildren().add(LineComponent.builder()
					.left(line)
					.leftFont(FontManager.getRunescapeFont().deriveFont(java.awt.Font.ITALIC))
					.build());
			}
		}
	}
}
