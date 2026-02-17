package net.reldo.taskstracker.panel.components;


import javax.swing.JMenuItem;
import java.awt.event.ActionListener;


public class TabMenuItem extends JMenuItem
{
	private final String labelText;
	private final TriToggleButton filterBtn;

	public TabMenuItem(String label, TriToggleButton filterBtn, ActionListener actionListener)
	{
		super(label);
		this.labelText = label;
		this.filterBtn = filterBtn;
		this.addActionListener(actionListener);
	}

	@Override
	public String getText()
	{
		if (filterBtn != null)
		{
			return filterBtn.isEnabled() ? "Lock " + labelText : "Unlock " + labelText;
		}
		return labelText;
	}
}
