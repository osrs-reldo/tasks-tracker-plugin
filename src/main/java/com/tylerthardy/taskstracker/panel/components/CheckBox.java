package com.tylerthardy.taskstracker.panel.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.client.ui.ColorScheme;
import static net.runelite.client.ui.PluginPanel.PANEL_WIDTH;

public class CheckBox extends JPanel
{
	private final JCheckBox jCheckBox = new JCheckBox();

	public CheckBox(String name)
	{
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(PANEL_WIDTH, 0));

		JLabel label = new JLabel(name);
		add(label, BorderLayout.CENTER);

		jCheckBox.setBackground(ColorScheme.LIGHT_GRAY_COLOR);
		add(jCheckBox, BorderLayout.EAST);
	}

	public boolean isSelected()
	{
		return jCheckBox.isSelected();
	}

	public void setSelected(boolean selected)
	{
		jCheckBox.setSelected(selected);
	}

	public void addActionListener(ActionListener l)
	{
		jCheckBox.addActionListener(l);
	}
}
