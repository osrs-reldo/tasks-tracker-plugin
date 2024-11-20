package net.reldo.taskstracker.panel;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
	public final ArrayList<TaskPanel> taskPanels = new ArrayList<>();
	private final TaskListListPanel taskList;
	private final TaskService taskService;
	private final JLabel emptyTasks = new JLabel();

	public TaskListPanel(TasksTrackerPlugin plugin, TaskPanelFactory taskPanelFactory, TaskService taskService)
	{
		this.plugin = plugin;

		taskList = new TaskListListPanel(taskPanelFactory);
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

	public void refresh(TaskFromStruct task)
	{
		if(SwingUtilities.isEventDispatchThread())
		{
			emptyTasks.setVisible(false);

			if (task != null)
			{
				Optional<TaskPanel> panel = taskPanels.stream()
					.filter(tp -> tp.task.getName().equalsIgnoreCase(task.getName()))
					.findFirst();
				panel.ifPresent(TaskPanel::refresh);
			}
			else
			{
				for (TaskPanel taskPanel : taskPanels)
				{
					taskPanel.refresh();
				}
			}

			Optional<TaskPanel> visibleTaskPanel = taskPanels.stream()
					.filter(TaskPanel::isVisible)
					.findFirst();

			if (visibleTaskPanel.isEmpty())
			{
				emptyTasks.setVisible(true);
			}
		}
		else
		{
			log.error("Task list panel refresh failed - not event dispatch thread.");
		}
	}

	public void refreshTaskPanelsWithSkill(Skill skill)
	{
		// Refresh all task panels for tasks with 'skill' or
		// 'SKILLS' (any skill) or 'TOTAL LEVEL' as a requirement.
		taskPanels.stream()
				.filter(tp ->
				{
					List<TaskDefinitionSkill> skillsList = tp.task.getTaskDefinition().getSkills();
					if(skillsList == null || skillsList.isEmpty())
					{
						return false;
					}

					return skillsList.stream()
						.map(TaskDefinitionSkill::getSkill)
						.anyMatch(s ->  s.equalsIgnoreCase(skill.getName()) ||
                                        s.equalsIgnoreCase("SKILLS") ||
                                        s.equalsIgnoreCase("TOTAL LEVEL")
						);
				})
				.forEach(TaskPanel::refresh);
	}

	private class TaskListListPanel extends FixedWidthPanel
	{
		private final TaskPanelFactory taskPanelFactory;

		public TaskListListPanel(TaskPanelFactory taskPanelFactory)
		{
			this.taskPanelFactory = taskPanelFactory;
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
				taskPanels.clear();
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
						adjustedIndexPosition = tasks.size() - (adjustedIndexPosition + 1);
					TaskPanel taskPanel = taskPanelFactory.create(tasks.get(taskService.getSortedTaskIndex(plugin.getConfig().sortCriteria(), adjustedIndexPosition)));
					add(taskPanel);
					taskPanels.add(taskPanel);
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
