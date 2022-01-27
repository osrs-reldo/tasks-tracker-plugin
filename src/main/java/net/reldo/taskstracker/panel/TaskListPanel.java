package net.reldo.taskstracker.panel;

import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.panel.components.FixedWidthPanel;
import net.reldo.taskstracker.tasktypes.Task;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Optional;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.FontManager;

@Slf4j
public class TaskListPanel extends JScrollPane
{
	public TasksTrackerPlugin plugin;
	public final ArrayList<TaskPanel> taskPanels = new ArrayList<>();
	private final ClientThread clientThread;
	private final SpriteManager spriteManager;
	private final SkillIconManager skillIconManager;
	private final TaskListListPanel taskList;
	private final JLabel emptyTasks = new JLabel();

	public TaskListPanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, SkillIconManager skillIconManager)
	{
		this.plugin = plugin;
		this.clientThread = clientThread;
		this.spriteManager = spriteManager;
		this.skillIconManager = skillIconManager;

		taskList = new TaskListListPanel();

		setViewportView(taskList);
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	}

	public ArrayList<Task> getTasks()
	{
		// TODO: Build a filter service
		if (plugin.selectedTaskType == null)
		{
			return null;
		}
		return plugin.taskManagers.get(plugin.selectedTaskType).tasks;
	}

	public String getEmptyTaskListMessage()
	{
		return "No tasks match the current filters.";
	}

	public void redraw()
	{
		taskList.redraw();
	}

	public void refresh(Task task)
	{
		assert SwingUtilities.isEventDispatchThread();

		emptyTasks.setVisible(false);

		if (task != null)
		{
			Optional<TaskPanel> panel = taskPanels.stream()
				.filter(tp -> tp.task.getName().equalsIgnoreCase(task.getName()))
				.findFirst();
			panel.ifPresent(TaskPanel::refresh);
			return;
		}

		for (TaskPanel taskPanel : taskPanels)
		{
			taskPanel.refresh();
		}

		Optional<TaskPanel> visibleTaskPanel = taskPanels.stream()
				.filter(TaskPanel::isVisible)
				.findFirst();

		if (!visibleTaskPanel.isPresent())
		{
			emptyTasks.setVisible(true);
		}
	}

	private class TaskListListPanel extends FixedWidthPanel
	{
		public TaskListListPanel()
		{
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
			assert SwingUtilities.isEventDispatchThread();
			removeAll();
			taskPanels.clear();
			add(emptyTasks);
			emptyTasks.setVisible(false);

			log.debug(" Creating panels...");
			ArrayList<Task> tasks = getTasks();
			if (tasks == null || tasks.size() == 0)
			{
				emptyTasks.setVisible(true);
				return;
			}
			{
				for (Task task : tasks)
				{
					TaskPanel taskPanel = task.generatePanel(plugin, clientThread, spriteManager, skillIconManager);
					add(taskPanel);
					taskPanels.add(taskPanel);
				}
			}
			log.debug("Validated and repaint...");
			validate();
			repaint();
		}
	}
}
