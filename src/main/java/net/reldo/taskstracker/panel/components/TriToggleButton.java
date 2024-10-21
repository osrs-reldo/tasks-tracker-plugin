package net.reldo.taskstracker.panel.components;

import javax.swing.Icon;

public class TriToggleButton extends MultiToggleButton
{
	public TriToggleButton()
	{
		super(3);
	}

	public boolean setIcons(Icon icon0, Icon icon1, Icon icon2)
	{
		if (icon0 == null || icon1 == null || icon2 == null)
		{
			return false;
		}

		setIcon(icon0, 0);
		setIcon(icon1, 1);
		setIcon(icon2, 2);

		return true;
	}

	public boolean setToolTips(String tooltip0, String tooltip1, String tooltip2)
	{
		if (tooltip0 == null || tooltip1 == null || tooltip2 == null)
		{
			return false;
		}

		setToolTip(tooltip0, 0);
		setToolTip(tooltip1, 1);
		setToolTip(tooltip2, 2);

		return true;
	}
}
