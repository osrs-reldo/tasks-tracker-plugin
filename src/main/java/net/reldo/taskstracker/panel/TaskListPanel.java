package net.reldo.taskstracker.panel;

import java.awt.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.config.ConfigValues;
import net.reldo.taskstracker.data.jsondatastore.types.TaskDefinitionSkill;
import net.reldo.taskstracker.data.task.TaskFromStruct;
import net.reldo.taskstracker.data.task.TaskService;
import net.reldo.taskstracker.panel.components.FixedWidthPanel;
import net.runelite.api.Skill;
import net.runelite.client.ui.FontManager;

@Slf4j
public class TaskListPanel extends JScrollPane
{
	public TasksTrackerPlugin plugin;
	private final HashMap<Integer, TaskPanel> taskPanelsByStructId = new HashMap<>();
	private final TaskListListPanel taskList;
	private final TaskService taskService;
	private final JLabel emptyTasks = new JLabel();

	public TaskListPanel(TasksTrackerPlugin plugin, TaskService taskService)
	{
		this.plugin = plugin;

		taskList = new TaskListListPanel(plugin);
		this.taskService = taskService;

		setViewportView(taskList);
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	}

	public String getEmptyTaskListMessage()
	{
		return "No tasks match the current filters.";
	}

	public void redraw()
	{
		taskList.redraw();
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
		}
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
			refresh(task);
		}
	}

	public void refreshTask(TaskFromStruct task)
	{
		log.debug("TaskListPanel.refreshMultipleTasks {}", task.getName());
		refresh(task);
	}

	private void refresh(TaskFromStruct task)
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

		boolean isAnyTaskPanelVisible = taskPanelsByStructId.values().stream()
			.anyMatch(TaskPanel::isVisible);

		if (!isAnyTaskPanelVisible)
		{
			emptyTasks.setVisible(true);
		}
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

	private class TaskListListPanel extends FixedWidthPanel
	{
		private final TasksTrackerPlugin plugin;

		public TaskListListPanel(TasksTrackerPlugin plugin)
		{
			this.plugin = plugin;

			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBorder(new EmptyBorder(0, 10, 10, 10));
			setAlignmentX(Component.LEFT_ALIGNMENT);

			emptyTasks.setBorder(new EmptyBorder(10,0,10,0));
			emptyTasks.setText("<html><center>" + getEmptyTaskListMessage() + "</center></html>");
			emptyTasks.setFont(FontManager.getRunescapeSmallFont());
			emptyTasks.setHorizontalAlignment(JLabel.CENTER);
			emptyTasks.setVerticalAlignment(JLabel.CENTER);
			add(emptyTasks);
			emptyTasks.setVisible(false);
		}

		public void redraw()
		{
			log.debug("TaskListPanel.redraw");
			if(SwingUtilities.isEventDispatchThread())
			{
				removeAll();
				taskPanelsByStructId.clear();
				add(emptyTasks);
				emptyTasks.setVisible(false);

				log.debug("TaskListPanel creating panels");
				List<TaskFromStruct> tasks = taskService.getTasks();
				if (tasks == null || tasks.isEmpty())
				{
					emptyTasks.setVisible(true);
					return;
				}

				for (int indexPosition = 0; indexPosition < tasks.size(); indexPosition++)
				{
					int adjustedIndexPosition = indexPosition;
					if (plugin.getConfig().sortDirection().equals(ConfigValues.SortDirections.DESCENDING))
					{
						adjustedIndexPosition = tasks.size() - (adjustedIndexPosition + 1);
					}
					TaskFromStruct task = tasks.get(taskService.getSortedTaskIndex(plugin.getConfig().sortCriteria(), adjustedIndexPosition));
					TaskPanel taskPanel = new TaskPanel(plugin, task);
					add(taskPanel);
					taskPanelsByStructId.put(task.getStructId(), taskPanel);
				}

				log.debug("TaskListPanel validate and repaint");
				validate();
				repaint();
			}
			else
			{
				log.error("Task list panel redraw failed - not event dispatch thread.");
			}
		}
	}
}
