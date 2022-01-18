package com.tylerthardy.taskstracker.panel;

import com.tylerthardy.taskstracker.TasksTrackerPlugin;
import com.tylerthardy.taskstracker.panel.tabs.AllTaskListPanel;
import com.tylerthardy.taskstracker.panel.tabs.TrackedTaskListPanel;
import com.tylerthardy.taskstracker.tasktypes.Task;
import java.awt.Dimension;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.PluginPanel;

@Slf4j
public class TasksTrackerPluginPanel extends PluginPanel
{
	private final LoggedOutPanel loggedOutPanel;
	public AllTaskListPanel allTasksPanel;
    public TrackedTaskListPanel trackedTaskListPanel;
    private TasksTrackerPlugin plugin;
    private final ClientThread clientThread;
    private final SpriteManager spriteManager;
    private SkillIconManager skillIconManager;
	private LoggedInPanel loggedInPanel;
	private boolean loggedIn = false;

	public TasksTrackerPluginPanel(TasksTrackerPlugin plugin, ClientThread clientThread, SpriteManager spriteManager, SkillIconManager skillIconManager)
    {
        super(false);
        this.plugin = plugin;
        this.clientThread = clientThread;
        this.spriteManager = spriteManager;
        this.skillIconManager = skillIconManager;

        loggedOutPanel = new LoggedOutPanel();
        this.add(loggedOutPanel);
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
			this.removeAll();
			if (loggedIn)
			{
				if (loggedInPanel == null)
				{
					loggedInPanel = new LoggedInPanel(plugin, clientThread, spriteManager, skillIconManager);
					allTasksPanel = loggedInPanel.allTasksPanel;
					trackedTaskListPanel = loggedInPanel.trackedTaskListPanel;
				}
				this.add(loggedInPanel);
			} else {
				this.add(loggedOutPanel);
			}

			validate();
			repaint();
		}
		this.loggedIn = loggedIn;
	}
}