package com.tylerthardy.taskstracker.panel;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class LoggedOutPanel extends JPanel
{
	public LoggedOutPanel()
	{
		this.add(new JLabel("Log into an account to track tasks."));
	}
}
