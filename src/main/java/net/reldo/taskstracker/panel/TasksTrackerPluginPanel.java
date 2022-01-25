package net.reldo.taskstracker.panel;

import net.reldo.taskstracker.TasksTrackerConfig;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.panel.tabs.AllTaskListPanel;
import net.reldo.taskstracker.panel.tabs.TrackedTaskListPanel;
import net.reldo.taskstracker.tasktypes.Task;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

@Slf4j
public class TasksTrackerPluginPanel extends PluginPanel
{
	private final ClientThread clientThread;
	private final SpriteManager spriteManager;
	private final TasksTrackerPlugin plugin;
	private final SkillIconManager skillIconManager;

	private final LoggedInPanel loggedInPanel;
	private final LoggedOutPanel loggedOutPanel = new LoggedOutPanel();

	public AllTaskListPanel allTasksPanel;
	public TrackedTaskListPanel trackedTaskListPanel;

	private boolean loggedIn = false;

	public TasksTrackerPluginPanel(TasksTrackerPlugin plugin, TasksTrackerConfig config, ClientThread clientThread, SpriteManager spriteManager, SkillIconManager skillIconManager)
	{
		super(false);
		this.plugin = plugin;
		this.clientThread = clientThread;
		this.spriteManager = spriteManager;
		this.skillIconManager = skillIconManager;

		setBorder(new EmptyBorder(6, 6, 6, 6));
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new BorderLayout());

		loggedInPanel = new LoggedInPanel(plugin, config, clientThread, spriteManager, skillIconManager);
		allTasksPanel = loggedInPanel.allTasksPanel;
		trackedTaskListPanel = loggedInPanel.trackedTaskListPanel;
		add(loggedInPanel, BorderLayout.NORTH);
		loggedInPanel.setVisible(false);

		// Add error pane
		add(loggedOutPanel);
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(PANEL_WIDTH + SCROLLBAR_WIDTH, super.getPreferredSize().height);
	}

	public void redraw()
	{
		if (loggedIn)
		{
			loggedInPanel.redraw();
		}
	}

	public void refresh(Task task)
	{
		if (loggedIn)
		{
			loggedInPanel.refresh(task);
		}
	}

	public void setLoggedIn(boolean loggedIn)
	{
		assert SwingUtilities.isEventDispatchThread();

		if (loggedIn != this.loggedIn)
		{
			if (loggedIn)
			{
				loggedOutPanel.setVisible(false);
				loggedInPanel.setVisible(true);
			}
			else
			{
				loggedInPanel.setVisible(false);
				loggedOutPanel.setVisible(true);
			}

			validate();
			repaint();
		}
		this.loggedIn = loggedIn;
	}
}