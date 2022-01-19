package com.tylerthardy.taskstracker.panel;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.panel.tabs.AllTaskListPanel;
import com.tylerthardy.taskstracker.panel.tabs.TrackedTaskListPanel;
import com.tylerthardy.taskstracker.tasktypes.Task;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.border.EmptyBorder;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;

@Slf4j
public class TasksTrackerPluginPanel extends PluginPanel
{
	public AllTaskListPanel allTasksPanel;
    public TrackedTaskListPanel trackedTaskListPanel;
    private TasksTrackerPlugin plugin;
    private final ClientThread clientThread;
    private final SpriteManager spriteManager;
    private SkillIconManager skillIconManager;
	private final LoggedInPanel loggedInPanel;
	private boolean loggedIn = false;
	private final PluginErrorPanel errorPanel = new PluginErrorPanel();
	private final LoggedOutPanel loggedOutPanel = new LoggedOutPanel();

	public TasksTrackerPluginPanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, SkillIconManager skillIconManager)
    {
        super(false);
        this.plugin = plugin;
        this.clientThread = clientThread;
        this.spriteManager = spriteManager;
        this.skillIconManager = skillIconManager;

		setBorder(new EmptyBorder(6, 6, 6, 6));
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new BorderLayout());

		loggedInPanel = new LoggedInPanel(plugin, clientThread, spriteManager, skillIconManager);
		allTasksPanel = loggedInPanel.allTasksPanel;
		trackedTaskListPanel = loggedInPanel.trackedTaskListPanel;
		add(loggedInPanel, BorderLayout.NORTH);
		loggedInPanel.setVisible(false);

		// Add error pane
		// @todo: refactor LoggedInPanel into this (so to not require super(false);) and switch to errorPanel
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
			} else {
				loggedInPanel.setVisible(false);
				loggedOutPanel.setVisible(true);
			}

			validate();
			repaint();
		}
		this.loggedIn = loggedIn;
	}
}