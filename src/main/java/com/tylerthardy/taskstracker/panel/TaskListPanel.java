package com.tylerthardy.taskstracker.panel;

import com.tylerthardy.taskstracker.tasktypes.Task;
import com.tylerthardy.taskstracker.tasktypes.TaskManager;
import com.tylerthardy.taskstracker.tasktypes.TaskPanel;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.FontManager;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;

@Slf4j
public abstract class TaskListPanel extends JScrollPane
{
    public abstract ArrayList<Task> getTasks();
    public abstract String getEmptyTaskListMessage();

    public final TaskManager taskManager;

    private final ClientThread clientThread;
    private final SpriteManager spriteManager;
    private final TaskListListPanel taskList;

    public TaskListPanel(TaskManager taskManager, ClientThread clientThread, SpriteManager spriteManager)
    {
        this.taskManager = taskManager;
        this.clientThread = clientThread;
        this.spriteManager = spriteManager;

        taskList = new TaskListListPanel();

        setViewportView(taskList);
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }


    public void refresh()
    {
        taskList.refresh();
    }

    private class TaskListListPanel extends FixedWidthPanel
    {
        public TaskListListPanel()
        {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(0, 10, 10, 10));
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        public void refresh()
        {
            assert SwingUtilities.isEventDispatchThread();
            removeAll();

            log.debug("Creating panels...");
            ArrayList<Task> tasks = getTasks();
            if (tasks == null || tasks.size() == 0)
            {
                JLabel emptyTasks = new JLabel();
                emptyTasks.setText("<html><center>" + getEmptyTaskListMessage() + "</center></html>");
                emptyTasks.setFont(FontManager.getRunescapeSmallFont());
                add(emptyTasks);
                return;
            }
            {
                for (Task task : tasks) {
                    TaskPanel taskPanel = task.generatePanel(taskManager, clientThread, spriteManager);
                    add(taskPanel);
                }
            }
            log.debug("Validated and repaint...");
            validate();
            repaint();
        }
    }
}
