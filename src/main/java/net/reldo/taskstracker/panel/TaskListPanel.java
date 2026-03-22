package net.reldo.taskstracker.panel;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntConsumer;
import javax.swing.BoxLayout;
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
import net.reldo.taskstracker.data.route.RouteItem;
import net.reldo.taskstracker.data.route.RouteSection;
import net.reldo.taskstracker.data.task.TaskFromStruct;
import net.reldo.taskstracker.data.task.TaskService;
import net.reldo.taskstracker.panel.components.FixedWidthPanel;
import net.reldo.taskstracker.panel.components.SectionHeaderPanel;
import net.runelite.api.Skill;
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
	private boolean forceUpdatePriorityTaskFlag = false;

	/** Section header panels keyed by section name */
	private final Map<String, SectionHeaderPanel> sectionHeaderPanels = new HashMap<>();

	/** Names of currently collapsed sections */
	private final Set<String> collapsedSections = new HashSet<>();

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
			taskPanel.refresh();
			// Re-hide tasks in collapsed sections after refresh resets visibility
			if (taskPanel.isVisible() && isTaskInCollapsedSection(taskPanel.task))
			{
				taskPanel.setVisible(false);
			}
		}
		refreshEmptyPanel();
		updatePriorityTaskAfterRefresh();
	}

	/**
	 * Checks if a task belongs to a currently collapsed section.
	 */
	private boolean isTaskInCollapsedSection(TaskFromStruct task)
	{
		ConfigValues.TaskListTabs currentTab = plugin.getConfig().taskListTab();
		CustomRoute activeRoute = taskService.getActiveRoute(currentTab);
		if (activeRoute == null || activeRoute.getSections() == null)
		{
			return false;
		}

		int structId = task.getStructId();
		for (RouteSection section : activeRoute.getSections())
		{
			if (section.getTaskIds() != null && section.getTaskIds().contains(structId))
			{
				String sectionKey = section.getName() != null ? section.getName() : "Section";
				return collapsedSections.contains(sectionKey);
			}
		}

		return false;
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
			panel.refresh();
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

	private void refreshEmptyPanel()
	{
		boolean isAnyTaskPanelVisible = taskPanelsByStructId.values().stream()
			.anyMatch(TaskPanel::isVisible);

		emptyTasks.setVisible(!isAnyTaskPanelVisible);
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

		Optional<TaskPanel> optionalTaskPanel = taskPanels.stream().filter(Component::isVisible).
			min((panel1, panel2) ->
				Integer.compare(getCurrentTaskListListPanel().getComponentZOrder(panel1),
					getCurrentTaskListListPanel().getComponentZOrder(panel2)));
		priorityTaskPanel = optionalTaskPanel.orElse(null);
	}

	public String getEmptyTaskListMessage()
	{
		return "No tasks match the current filters.";
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

				// Clear section headers from previous task type
				for (SectionHeaderPanel header : sectionHeaderPanels.values())
				{
					remove(header);
				}
				sectionHeaderPanels.clear();
				collapsedSections.clear();

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
					emptyTasks.setVisible(true);
					return;
				}

				ConfigValues.TaskListTabs currentTab = plugin.getConfig().taskListTab();
				CustomRoute activeRoute = taskService.getActiveRoute(currentTab);

				// Hide all section headers before redraw
				for (SectionHeaderPanel header : sectionHeaderPanels.values())
				{
					header.setVisible(false);
				}

				if (activeRoute != null && activeRoute.getSections() != null)
				{
					redrawWithSections(activeRoute);
				}
				else
				{
					redrawWithoutSections();
				}

				SwingUtilities.invokeLater(TaskListPanel.this::refreshAllTasks);
			}
			else
			{
				log.error("Task list panel redraw failed - not event dispatch thread.");
			}
		}

		private void redrawWithoutSections()
		{
			// Remove section headers
			for (SectionHeaderPanel header : sectionHeaderPanels.values())
			{
				remove(header);
			}
			sectionHeaderPanels.clear();

			int numberOfPinnedTasks = 0;
			Integer pinnedTaskStructId = null;
			if (plugin.getConfig().pinnedTaskId() != 0)
			{
				pinnedTaskStructId = plugin.getConfig().pinnedTaskId();
				setComponentZOrder(taskPanelsByStructId.get(pinnedTaskStructId), 0);
				numberOfPinnedTasks++;
			}

			for (int indexPosition = 0; indexPosition < taskPanels.size(); indexPosition++)
			{
				int adjustedIndexPosition = indexPosition;
				if (plugin.getConfig().sortDirection().equals(ConfigValues.SortDirections.DESCENDING))
				{
					adjustedIndexPosition = taskPanels.size() - (indexPosition + 1);
				}
				TaskPanel taskPanel = taskPanels.get(taskService.getSortedTaskIndex(plugin.getConfig().sortCriteria(), adjustedIndexPosition));

				if (pinnedTaskStructId != null && pinnedTaskStructId.equals(taskPanel.task.getStructId()))
				{
					priorityTaskPanel = taskPanel;
					continue;
				}

				if (indexPosition + numberOfPinnedTasks == 0)
				{
					priorityTaskPanel = taskPanel;
				}

				setComponentZOrder(taskPanel, indexPosition + numberOfPinnedTasks);
			}
		}

		private void redrawWithSections(CustomRoute route)
		{
			int componentPosition = 0;

			// Iterate through sections and position components
			for (RouteSection section : route.getSections())
			{
				List<RouteItem> items = section.getItems();
				if (items == null || items.isEmpty())
				{
					continue;
				}

				// Count visible/completed items for this section
				int visibleTaskCount = 0;
				int completedTaskCount = 0;
				List<Component> visiblePanels = new ArrayList<>();

				for (RouteItem item : items)
				{
					if (item.isTask())
					{
						TaskPanel tp = taskPanelsByStructId.get(item.getTaskId());
						if (tp != null)
						{
							if (meetsFilterCriteria(tp))
							{
								visibleTaskCount++;
								visiblePanels.add(tp);
								if (tp.task.isCompleted())
								{
									completedTaskCount++;
								}
							}
						}
					}
				}

				// Skip sections with no visible items
				if (visiblePanels.isEmpty())
				{
					continue;
				}

				// Get or create section header
				String sectionKey = section.getName() != null ? section.getName() : "Section";
				SectionHeaderPanel header = sectionHeaderPanels.get(sectionKey);
				if (header == null)
				{
					header = new SectionHeaderPanel(sectionKey, section.getDescription());
					sectionHeaderPanels.put(sectionKey, header);
					add(header);
				}

				// Update progress
				header.setProgress(completedTaskCount, visibleTaskCount);

				// Restore collapsed state and set up callback
				boolean isCollapsed = collapsedSections.contains(sectionKey);
				header.setCollapsedSilent(isCollapsed);
				final String finalSectionKey = sectionKey;
				final List<Component> finalVisiblePanels = visiblePanels;
				header.setCollapseCallback(collapsed -> {
					if (collapsed)
					{
						collapsedSections.add(finalSectionKey);
					}
					else
					{
						collapsedSections.remove(finalSectionKey);
					}
					// Toggle visibility of panels in this section directly
					for (Component panel : finalVisiblePanels)
					{
						if (panel instanceof TaskPanel)
						{
							TaskPanel tp = (TaskPanel) panel;
							tp.setVisible(!collapsed && meetsFilterCriteria(tp));
						}
						else
						{
							panel.setVisible(!collapsed);
						}
					}
					revalidate();
					repaint();
				});

				header.setVisible(true);
				setComponentZOrder(header, componentPosition++);

				// Position all visible panels in this section
				// Hide them if section is collapsed
				for (Component panel : visiblePanels)
				{
					setComponentZOrder(panel, componentPosition++);
					if (isCollapsed)
					{
						panel.setVisible(false);
					}
				}
			}

			// Hide tasks not in the route
			Set<Integer> routeStructIds = new HashSet<>(route.getFlattenedOrder());
			for (TaskPanel tp : taskPanels)
			{
				if (!routeStructIds.contains(tp.task.getStructId()))
				{
					tp.setVisible(false);
				}
			}
		}

		/**
		 * Helper to check if a task panel meets filter criteria.
		 * Returns true for all route tasks when a route is active — route mode bypasses filters.
		 * Only called for tasks already in the route (from redrawWithSections iteration).
		 */
		private boolean meetsFilterCriteria(TaskPanel tp)
		{
			ConfigValues.TaskListTabs currentTab = plugin.getConfig().taskListTab();
			CustomRoute activeRoute = taskService.getActiveRoute(currentTab);
			if (activeRoute != null)
			{
				return true;
			}
			if (plugin.getFilterMatcher() == null)
			{
				return true;
			}
			return plugin.getFilterMatcher().meetsFilterCriteria(tp.task, plugin.taskTextMatcher);
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
				SwingUtilities.invokeLater(this::redraw);
			}
		}
	}
}
