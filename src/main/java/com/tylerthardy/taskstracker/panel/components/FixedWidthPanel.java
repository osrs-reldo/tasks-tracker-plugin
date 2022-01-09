package com.tylerthardy.taskstracker.panel.components;

import net.runelite.client.ui.PluginPanel;

import javax.swing.JPanel;
import java.awt.Dimension;

public class FixedWidthPanel extends JPanel
{
    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension(PluginPanel.PANEL_WIDTH, super.getPreferredSize().height);
    }

}
