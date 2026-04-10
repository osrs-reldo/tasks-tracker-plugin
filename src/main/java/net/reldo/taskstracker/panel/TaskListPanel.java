package net.reldo.taskstracker.panel;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.config.ConfigValues;
import net.reldo.taskstracker.data.jsondatastore.types.TaskDefinitionSkill;
import net.reldo.taskstracker.data.route.CustomRoute;
import net.reldo.taskstracker.data.route.CustomRouteItem;
import net.reldo.taskstracker.data.route.RouteItem;
import net.reldo.taskstracker.data.route.RouteSection;
import net.reldo.taskstracker.data.task.TaskFromStruct;
import net.reldo.taskstracker.data.task.TaskService;
import net.reldo.taskstracker.panel.components.FixedWidthPanel;
import net.reldo.taskstracker.panel.components.SectionHeaderPanel;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.FontManager;

@Slf4j
public class TaskListPanel extends JScrollPane
{
	public TasksTrackerPlugin plugin;
	private final int TASK_LIST_BUFFER_COUNT = 2;
	private final HashMap<Integer, TaskPanel> taskPanelsByStructId = new HashMap<>();
	public ArrayList<TaskPanel> taskPanels = new ArrayList<>();
	private final ArrayList<TaskListListPanel> taskListBuffers = new ArrayList<>(TASK_LIST_BUFFER_COUNT);
	private int currentTaskListBufferIndex;
	private final TaskService taskService;
	private final JLabel emptyTasks = new JLabel();
	@Setter
	private int batchSize;
	@Getter
	private TaskPanel priorityTaskPanel = null;
	@Getter
	private CustomItemPanel priorityCustomItemPanel = null;
	private boolean forceUpdatePriorityTaskFlag = false;

	/** Section header panels keyed by route id then section id */
	private final HashMap<String, HashMap<String, SectionHeaderPanel>> sectionHeaderPanels = new HashMap<>();

	/** Custom item panels keyed by custom item ID */
	private final HashMap<String, CustomItemPanel> customItemPanels = new HashMap<>();

	/** Tracks which route's custom items are currently cached */
	private String cachedCustomItemRouteId = null;

	public TaskListPanel(TasksTrackerPlugin plugin, TaskService taskService)
	{
		this.plugin = plugin;
		this.taskService = taskService;
		batchSize = plugin.getConfig().taskPanelBatchSize();

		FixedWidthPanel taskListListPanelWrapper = new FixedWidthPanel();

		for (int i = 0; i < TASK_LIST_BUFFER_COUNT; i++)
		{
			taskListBuffers.add(new TaskListListPanel(plugin));
			taskListListPanelWrapper.add(taskListBuffers.get(i));
		}

		setViewportView(taskListListPanelWrapper);
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		setCurrentTaskListListPanel(0);
	}

	private void setCurrentTaskListListPanel(int index)
	{
		if (index != currentTaskListBufferIndex)
		{
			taskListBuffers.get(currentTaskListBufferIndex).setVisible(false);
			taskListBuffers.get(index).setVisible(true);
			currentTaskListBufferIndex = index;
		}
	}

	private TaskListListPanel getCurrentTaskListListPanel()
	{
		return taskListBuffers.get(currentTaskListBufferIndex);
	}

	private TaskListListPanel getNextTaskListListPanel()
	{
		return taskListBuffers.get(getNextBufferIndex());
	}

	private void showNextTaskListListPanel()
	{
		log.debug("Showing next task list list panel: {}", getNextBufferIndex());
		TaskListListPanel previousPanel = getCurrentTaskListListPanel();
		setCurrentTaskListListPanel(getNextBufferIndex());
		previousPanel.prepEmptyTaskListPanel();
	}

	private int getNextBufferIndex()
	{
		return (currentTaskListBufferIndex + 1) % TASK_LIST_BUFFER_COUNT;
	}

	public void drawNewTaskType()
	{
		log.debug("Drawing new Task Type taskListListPanel");
		getNextTaskListListPanel().drawNewTaskType();
	}

	public void redraw()
	{
		log.debug("Redrawing taskListListPanel");
		getCurrentTaskListListPanel().redraw();
	}

	public void refreshAllTasks()
	{
		log.debug("TaskListPanel.refreshAllTasks");
		if (!SwingUtilities.isEventDispatchThread())
		{
			log.error("Task list panel refresh failed - not event dispatch thread.");
			return;
		}
		for (TaskPanel taskPanel : taskPanelsByStructId.values())
		{
			refreshTaskPanel(taskPanel);
		}

		// Refresh custom item panels (visibility based on section collapse)
		if (plugin.isRouteMode())
		{
			ConfigValues.TaskListTabs currentTab = plugin.getConfig().taskListTab();
			CustomRoute activeRoute = taskService.getActiveRoute(currentTab);

			if (activeRoute != null)
			{
				String taskType = taskService.getCurrentTaskType().getTaskJsonName();
				Set<String> completedIds = plugin.getTrackerGlobalConfigStore()
					.loadCustomItemCompletion(taskType, activeRoute.getId());

				for (CustomItemPanel panel : customItemPanels.values())
				{
					refreshCustomItemPanel(panel, activeRoute);
				}

				// Count section progress (tasks + custom items)
				HashMap<String, SectionHeaderPanel> routeHeaders = sectionHeaderPanels.get(activeRoute.getId());
				if (routeHeaders != null)
				{
					for (RouteSection section : activeRoute.getSections())
					{
						SectionHeaderPanel header = routeHeaders.get(section.getId());
						if (header == null)
						{
							continue;
						}

						int total = section.getItemCount();
						int completed = 0;
						for (RouteItem item : section.getItems())
						{
							if (item.isTask())
							{
								TaskPanel tp = taskPanelsByStructId.get(item.getTaskId());
								if (tp != null && tp.task.isCompleted())
								{
									completed++;
								}
							}
							else if (item.getCustomItem() != null)
							{
								if (completedIds.contains(item.getCustomItem().getId()))
								{
									completed++;
								}
							}
						}
						header.setProgress(completed, total);
					}
				}
			}
		}

		refreshEmptyPanel();
		updatePriorityTaskAfterRefresh();
	}

	private void refreshCustomItemPanel(CustomItemPanel panel, CustomRoute activeRoute)
	{
		for (RouteSection section : activeRoute.getSections())
		{
			boolean found = section.getCustomItems().stream()
				.anyMatch(ci -> panel.getCustomItemId().equals(ci.getId()));
			if (found)
			{
				HashMap<String, SectionHeaderPanel> routeHeaders = sectionHeaderPanels.get(activeRoute.getId());
				if (routeHeaders != null)
				{
					SectionHeaderPanel header = routeHeaders.get(section.getId());
					boolean collapsed = header != null && header.isCollapsed();
					panel.setVisible(!collapsed);
				}
				return;
			}
		}
	}

	public void refreshMultipleStructIds(Collection<Integer> structIds)
	{
		refreshMultipleTasks(structIds.stream()
			.map(taskPanelsByStructId::get)
			.filter(Objects::nonNull)
			.map(panel -> panel.task)
			.collect(Collectors.toList()));
	}

	public void refreshMultipleTasks(Collection<TaskFromStruct> tasks)
	{
		log.debug("TaskListPanel.refreshMultipleTasks {}", tasks.size());
		if (!SwingUtilities.isEventDispatchThread())
		{
			log.error("Task list panel refresh failed - not event dispatch thread.");
			return;
		}
		for (TaskFromStruct task : tasks)
		{
			refresh(task, true);
		}
		if (forceUpdatePriorityTaskFlag)
		{
			updatePriorityTaskAfterRefresh();
		}
	}

	public void refreshTask(TaskFromStruct task)
	{
		log.debug("TaskListPanel.refreshTask {}", task.getName());
		refresh(task, false);
	}

	private void refresh(TaskFromStruct task, boolean delayPriorityTaskRefresh)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			log.error("Task list panel refresh failed - not event dispatch thread.");
			return;
		}
		if (task == null)
		{
			log.debug("Attempted to refresh null task");
			return;
		}

		emptyTasks.setVisible(false);

		TaskPanel panel = taskPanelsByStructId.get(task.getStructId());
		if (panel != null)
		{
			refreshTaskPanel(panel);
		}

		if (getCurrentTaskListListPanel().getComponentZOrder(panel) <=
			getCurrentTaskListListPanel().getComponentZOrder(priorityTaskPanel))
		{
			if (delayPriorityTaskRefresh)
			{
				forceUpdatePriorityTaskFlag = true;
			}
			else
			{
				updatePriorityTaskAfterRefresh();
			}
		}

		refreshEmptyPanel();

	}

	private void refreshTaskPanel(TaskPanel panel)
	{
		panel.refresh();

		// Only do route-specific hiding when in route mode
		if (!panel.isVisible() || !plugin.isRouteMode())
		{
			return;
		}

		// Check if there is an active route, whether the panel is in a route section, and if it is collapsed
		ConfigValues.TaskListTabs currentTab = plugin.getConfig().taskListTab();
		CustomRoute activeRoute = taskService.getActiveRoute(currentTab);

		if (activeRoute == null)
		{
			panel.setVisible(false);
			return;
		}

		RouteSection section = activeRoute.getSectionForTask(panel.task.getStructId());
		if (section == null)
		{
			panel.setVisible(false);
			return;
		}

		String sectionKey = section.getId();
		HashMap<String, SectionHeaderPanel> routeHeaders = sectionHeaderPanels.get(activeRoute.getId());
		if (routeHeaders == null)
		{
			return;
		}

		SectionHeaderPanel header = routeHeaders.get(sectionKey);
		if (header != null && header.isCollapsed())
		{
			panel.setVisible(false);
		}
	}

	private void refreshEmptyPanel()
	{
		boolean showEmptyPanel;
		String emptyPanelString;

		if (plugin.isRouteMode())
		{
			ConfigValues.TaskListTabs currentTab = plugin.getConfig().taskListTab();
			CustomRoute activeRoute = taskService.getActiveRoute(currentTab);

			if (activeRoute == null)
			{
				emptyPanelString = getNoRouteSelectedMessage();
				showEmptyPanel = true;
			}
			else
			{
				emptyPanelString = getEmptyRouteListMessage();
				showEmptyPanel = activeRoute.getSections().isEmpty();
			}
		}
		else
		{
			emptyPanelString = getEmptyTaskListMessage();
			showEmptyPanel = !taskPanelsByStructId.values().stream()
				.anyMatch(TaskPanel::isVisible);
		}

		emptyTasks.setText("<html><center>" + emptyPanelString + "</center></html>");
		emptyTasks.setVisible(showEmptyPanel);
	}

	public void refreshTaskPanelsWithSkill(Skill skill)
	{
		// Refresh all task panels for tasks with 'skill' or
		// 'SKILLS' (any skill) or 'TOTAL LEVEL' as a requirement.
		taskPanelsByStructId.values().stream()
			.filter(tp ->
			{
				List<TaskDefinitionSkill> skillsList = tp.task.getTaskDefinition().getSkills();
				if (skillsList == null || skillsList.isEmpty())
				{
					return false;
				}

				return skillsList.stream()
					.map(TaskDefinitionSkill::getSkill)
					.anyMatch(s -> s.equalsIgnoreCase(skill.getName()) ||
						s.equalsIgnoreCase("SKILLS") ||
						s.equalsIgnoreCase("TOTAL LEVEL")
					);
			})
			.forEach(TaskPanel::refresh);
	}

	public void updatePriorityTaskAfterRefresh()
	{
		forceUpdatePriorityTaskFlag = false;

		boolean routeModeActive = plugin.isRouteMode();

		Optional<TaskPanel> optionalTaskPanel = taskPanels.stream()
			.filter(Component::isVisible)
			.filter(panel -> !routeModeActive || !panel.task.isCompleted())
			.min((panel1, panel2) ->
				Integer.compare(getCurrentTaskListListPanel().getComponentZOrder(panel1),
					getCurrentTaskListListPanel().getComponentZOrder(panel2)));
		priorityTaskPanel = optionalTaskPanel.orElse(null);

		Optional<CustomItemPanel> optionalCustomPanel = customItemPanels.values().stream()
			.filter(Component::isVisible)
			.filter(p -> !p.isCompleted())
			.min((p1, p2) -> Integer.compare(
				getCurrentTaskListListPanel().getComponentZOrder(p1),
				getCurrentTaskListListPanel().getComponentZOrder(p2)));
		priorityCustomItemPanel = optionalCustomPanel.orElse(null);

		plugin.getShortestPathService().setGpsTarget(getGpsTargetLocation());
	}

	private WorldPoint getGpsTargetLocation()
	{
		JComponent priority = getPriorityPanel();
		if (priority instanceof CustomItemPanel)
		{
			return ((CustomItemPanel) priority).getWorldLocation();
		}
		if (priority instanceof TaskPanel)
		{
			TaskPanel taskPanel = (TaskPanel) priority;
			ConfigValues.TaskListTabs currentTab = plugin.getConfig().taskListTab();
			CustomRoute activeRoute = taskService.getActiveRoute(currentTab);
			if (activeRoute != null)
			{
				WorldPoint routeLocation = activeRoute.getFlattenedItems().stream()
					.filter(item -> item.isTask() && taskPanel.task.getStructId().equals(item.getTaskId()))
					.map(RouteItem::getLocation)
					.filter(java.util.Objects::nonNull)
					.findFirst()
					.orElse(null);
				if (routeLocation != null)
				{
					return routeLocation;
				}
			}
			// Fall back to store location (single-location tasks)
			return taskPanel.task.getTaskDefinition().getLocation();
		}
		return null;
	}

	/**
	 * Returns the priority panel (task or custom item) with the lowest z-order.
	 * Used by the overlay to show the next incomplete item.
	 */
	public JComponent getPriorityPanel()
	{
		if (priorityTaskPanel == null && priorityCustomItemPanel == null)
		{
			return null;
		}
		if (priorityTaskPanel == null)
		{
			return priorityCustomItemPanel;
		}
		if (priorityCustomItemPanel == null)
		{
			return priorityTaskPanel;
		}

		int taskZ = getCurrentTaskListListPanel().getComponentZOrder(priorityTaskPanel);
		int customZ = getCurrentTaskListListPanel().getComponentZOrder(priorityCustomItemPanel);
		return taskZ < customZ ? priorityTaskPanel : priorityCustomItemPanel;
	}

	public void collapseAllSections()
	{
		setAllSectionsCollapseState(true);
	}

	public void expandAllSections()
	{
		setAllSectionsCollapseState(false);
	}

	public void setAllSectionsCollapseState(boolean collapsed)
	{
		getActiveSectionHeaderPanels().ifPresent(headerPanels -> {
			headerPanels.values().forEach(headerPanel -> headerPanel.setCollapsedSilent(collapsed));
			refreshAllTasks();
		});
	}

	public void collapseAllExcept(String sectionId)
	{
		getActiveSectionHeaderPanels().ifPresent(headerPanels -> {
			headerPanels.values()
			.forEach(headerPanel -> headerPanel.setCollapsedSilent(!Objects.equals(headerPanel.getSectionId(), sectionId)));
			refreshAllTasks();
		});
	}

	public Optional<Map<String, SectionHeaderPanel>> getActiveSectionHeaderPanels()
	{
		CustomRoute activeRoute = taskService.getActiveRoute();
		if (activeRoute == null)
		{
			return Optional.empty();
		}
		if (sectionHeaderPanels == null)
		{
			return Optional.empty();
		}

		return Optional.of(sectionHeaderPanels.get(activeRoute.getId()));
	}

	public String getEmptyTaskListMessage()
	{
		return "No tasks match the current filters.";
	}

	public String getEmptyRouteListMessage()
	{
		return "This route has no tasks.";
	}

	public String getNoRouteSelectedMessage()
	{
		return "<b>Routes</b><br>"
			+ "A new feature for Leagues 6: Demonic Pacts<br><br>"
			+ "&#8226; Import a route from clipboard<br>"
			+ "&#8226; Create route directly in plugin<br>"
			+ "&nbsp;&nbsp;(coming soon!)";
	}

	/**
	 * Returns the task IDs of currently visible tasks in display order.
	 */
	public List<Integer> getVisibleTaskIds()
	{
		List<Integer> ids = new ArrayList<>();

		Component[] components = getCurrentTaskListListPanel().getComponents();

		for (Component comp : components)
		{
			if (comp instanceof TaskPanel && comp.isVisible())
			{
				TaskPanel panel = (TaskPanel) comp;
				ids.add(panel.task.getStructId());
			}
		}

		return ids;
	}

	private class TaskListListPanel extends FixedWidthPanel
	{
		private final TasksTrackerPlugin plugin;

		public TaskListListPanel(TasksTrackerPlugin plugin)
		{
			this.plugin = plugin;

			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBorder(new EmptyBorder(0, 10, 10, 10));
			setAlignmentX(Component.LEFT_ALIGNMENT);

			emptyTasks.setBorder(new EmptyBorder(10, 0, 10, 0));
			emptyTasks.setText("<html><center>" + getEmptyTaskListMessage() + "</center></html>");
			emptyTasks.setFont(FontManager.getRunescapeSmallFont());
			emptyTasks.setHorizontalAlignment(JLabel.CENTER);
			emptyTasks.setVerticalAlignment(JLabel.CENTER);
			add(emptyTasks);
			emptyTasks.setVisible(false);
		}

		public void prepEmptyTaskListPanel()
		{
			SwingUtilities.invokeLater(this::removeAll);
		}

		public void drawNewTaskType()
		{
			log.debug("TaskListPanel.drawNewTaskType");
			if (SwingUtilities.isEventDispatchThread())
			{
				log.debug("TaskListPanel creating panels");
				taskPanelsByStructId.clear();
				priorityTaskPanel = null;
				priorityCustomItemPanel = null;
				sectionHeaderPanels.clear();
				customItemPanels.clear();
				cachedCustomItemRouteId = null;

				add(emptyTasks);

				List<TaskFromStruct> tasks = taskService.getTasks();
				if (tasks == null || tasks.isEmpty())
				{
					emptyTasks.setVisible(true);
					return;
				}

				emptyTasks.setVisible(false);

				// Buffer to hold newly created task panels before they are swapped in
				ArrayList<TaskPanel> newTaskPanels = new ArrayList<>(tasks.size());

				processInBatches(tasks.size(), indexPosition ->
				{
					TaskFromStruct task = tasks.get(indexPosition);
					TaskPanel taskPanel = new TaskPanel(plugin, task, plugin.getFilterMatcher());
					add(taskPanel);
					newTaskPanels.add(taskPanel);
					taskPanelsByStructId.put(task.getStructId(), taskPanel);
					if (indexPosition == (batchSize - 1))
					{
						taskPanels = newTaskPanels; // replace taskPanels list at end of first batch
					}
				});
			}
			else
			{
				log.error("Task list panel drawNewTaskType failed - not event dispatch thread.");
			}
		}

		public void redraw()
		{
			log.debug("TaskListPanel.redraw");
			if (SwingUtilities.isEventDispatchThread())
			{
				if (taskPanels == null || taskPanels.isEmpty())
				{
					priorityTaskPanel = null;
					priorityCustomItemPanel = null;
					emptyTasks.setVisible(true);
					return;
				}

				// Check if route changed - clean up stale custom item panels
				ConfigValues.TaskListTabs currentTab = plugin.getConfig().taskListTab();
				CustomRoute currentRoute = taskService.getActiveRoute(currentTab);
				String currentRouteId = currentRoute != null ? currentRoute.getId() : null;

				if (!Objects.equals(currentRouteId, cachedCustomItemRouteId))
				{
					for (CustomItemPanel panel : customItemPanels.values())
					{
						remove(panel);
					}
					customItemPanels.clear();
					cachedCustomItemRouteId = currentRouteId;
				}

				// Hide all section headers and custom item panels before redraw
				sectionHeaderPanels.values()
					.forEach(sectionPanels -> sectionPanels.values()
						.forEach(sectionHeaderPanel -> sectionHeaderPanel.setVisible(false)));
				customItemPanels.values().forEach(p -> p.setVisible(false));

				redrawListItems();

				SwingUtilities.invokeLater(TaskListPanel.this::refreshAllTasks);
			}
			else
			{
				log.error("Task list panel redraw failed - not event dispatch thread.");
			}
		}

		private void redrawListItems()
		{

			int numberOfPinnedTasks = 0;
			Integer pinnedTaskStructId = null;
			CustomRoute activeRoute = null;

			int listSize = this.getComponentCount();
			Map<Integer, JComponent> listPanelsToDraw = new HashMap<>(listSize);

			boolean routeModeActive = plugin.isRouteMode();
			boolean hasActiveRoute = false;

			if (routeModeActive)
			{
				ConfigValues.TaskListTabs currentTab = plugin.getConfig().taskListTab();
				activeRoute = taskService.getActiveRoute(currentTab);
				hasActiveRoute = activeRoute != null;
			}

			String indexId = hasActiveRoute ? activeRoute.getId() : plugin.getConfig().sortCriteria();
			Boolean isAscending = plugin.getConfig().sortDirection().equals(ConfigValues.SortDirections.ASCENDING);

			// Set pinned task if route mode not active
			if (!routeModeActive && plugin.getConfig().pinnedTaskId() != 0)
			{
				pinnedTaskStructId = plugin.getConfig().pinnedTaskId();
				setComponentZOrder(taskPanelsByStructId.get(pinnedTaskStructId), 0);
				numberOfPinnedTasks++;
			}

			// Set section header panel positions
			if (hasActiveRoute)
			{
				sectionHeaderPanels.computeIfAbsent(activeRoute.getId(), k -> new HashMap<>());

				int sectionStartIndex = 0;
				for (RouteSection section : activeRoute.getSections())
				{
					// Get or create section header
					String sectionKey = section.getId();
					HashMap<String, SectionHeaderPanel> routeHeaders = sectionHeaderPanels.get(activeRoute.getId());
					if (routeHeaders == null)
					{
						return;
					}
					SectionHeaderPanel header = routeHeaders.get(sectionKey);
					if (header == null)
					{
						header = new SectionHeaderPanel(sectionKey, section.getName(), section.getDescription());
						sectionHeaderPanels.get(activeRoute.getId()).put(sectionKey, header);
						add(header);
					}
					header.setCollapseCallback(collapsed -> {
						SwingUtilities.invokeLater(TaskListPanel.this::refreshAllTasks);
					});
					header.setCollapseOthersCallback(sectionId -> {
						SwingUtilities.invokeLater(() -> collapseAllExcept(sectionId));
					});
					header.setVisible(true);

					listPanelsToDraw.put(sectionStartIndex, header);

					sectionStartIndex += section.getItems().size() + 1;
				}

				listSize = this.getComponentCount();

			}

			// Set custom item panel positions
			if (hasActiveRoute)
			{
				priorityCustomItemPanel = null;
				String taskType = taskService.getCurrentTaskType().getTaskJsonName();
				Set<String> completedIds = plugin.getTrackerGlobalConfigStore()
					.loadCustomItemCompletion(taskType, activeRoute.getId());

				for (RouteSection section : activeRoute.getSections())
				{
					for (RouteItem item : section.getItems())
					{
						if (item.isTask() || item.getCustomItem() == null)
						{
							continue;
						}

						CustomRouteItem customItem = item.getCustomItem();
						String customItemId = customItem.getId();

						CustomItemPanel customPanel = customItemPanels.get(customItemId);
						if (customPanel == null)
						{
							customPanel = new CustomItemPanel(plugin, item);
							customItemPanels.put(customItemId, customPanel);
							add(customPanel);
						}
						customPanel.setCompleted(completedIds.contains(customItemId));
						customPanel.setVisible(true);

						int indexPosition = taskService.getCustomItemIndex(activeRoute.getId(), customItemId);
						if (indexPosition >= 0)
						{
							indexPosition += numberOfPinnedTasks;
							listPanelsToDraw.put(indexPosition, customPanel);

							if (priorityCustomItemPanel == null && !customPanel.isCompleted())
							{
								priorityCustomItemPanel = customPanel;
							}
						}
					}
				}

				listSize = this.getComponentCount();
			}

			// Set task panel positions
			for (Integer taskStructId : taskPanelsByStructId.keySet())
			{
				TaskPanel taskPanel = taskPanelsByStructId.get(taskStructId);

				// ignore if structId matches pinned task
				if (pinnedTaskStructId != null && pinnedTaskStructId.equals(taskStructId))
				{
					priorityTaskPanel = taskPanel;
					continue;
				}

				// get sorted index for task
				int indexPosition = taskService.getTaskIndex(indexId, taskStructId, isAscending);
				indexPosition += numberOfPinnedTasks;

				// set priority task if not pinned
				if (indexPosition == 0)
				{
					priorityTaskPanel = taskPanel;
				}

				listPanelsToDraw.put(indexPosition, taskPanel);
			}

			int maxZIndex = Math.min(listPanelsToDraw.size(), listSize);
			for (int zIndex = 0; zIndex < maxZIndex; zIndex++)
			{
				if (listPanelsToDraw.containsKey(zIndex))
				{
					setComponentZOrder(listPanelsToDraw.get(zIndex), zIndex);
				}
			}
		}

		private void processInBatches(int objectCount, IntConsumer method)
		{
			processBatch(0, objectCount, method);
			showNextTaskListListPanel();
		}

		private void processBatch(int batch, int objectCount, IntConsumer method)
		{
			log.debug("TaskListPanel.processBatch {}", batch);

			for (int index = 0; index < batchSize; index++)
			{
				int indexPosition = index + (batch * batchSize);
				if (indexPosition < objectCount)
				{
					method.accept(indexPosition);
				}
				else
				{
					break;
				}
			}

			refreshEmptyPanel();
			validate();
			repaint();

			// queue next batch if not done or refresh after last batch
			int batchIndex = batch + 1;
			if (batchIndex * batchSize < objectCount)
			{
				SwingUtilities.invokeLater(() -> processBatch(batchIndex, objectCount, method));
			}
			else
			{
				plugin.enableTaskTypeDropdown();
				SwingUtilities.invokeLater(this::redraw);
			}
		}
	}
}
