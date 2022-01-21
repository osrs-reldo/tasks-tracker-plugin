package net.reldo.taskstracker.panel.components;

import javax.swing.Icon;
import javax.swing.JButton;
import lombok.Getter;

public class TriToggleButton extends JButton
{

	// Icons
	private final Icon[] icons = new Icon[3];
	// Tooltips
	private final String[] tooltips = new String[3];
	@Getter
	private int state = 0;

	public TriToggleButton()
	{
		super();
	}

	/* Action listener must include changeState() */

	public void setIcon(Icon icon, int state)
	{
		if (state < 0 || state > 3 || icon == null)
		{
			return;
		}

		icons[state] = icon;

		setIconState();
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

		setIconState();
		return true;
	}

	public void setToolTip(String tooltip, int state)
	{
		if (state < 0 || state > 3 || tooltip == null)
		{
			return;
		}

		tooltips[state] = tooltip;

		setTooltipState();
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

		setTooltipState();
		return true;
	}

	public void changeState()
	{
		setState((state + 1) % 3);
	}

	private void setIconState()
	{
		super.setIcon(icons[state]);
	}

	private void setTooltipState()
	{
		super.setToolTipText(tooltips[state]);
	}

	public void setState(int state)
	{
		this.state = state;
		setIconState();
		setTooltipState();
	}
}
