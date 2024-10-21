package net.reldo.taskstracker.panel.components;

import javax.swing.Icon;
import javax.swing.JButton;
import lombok.Getter;

public class MultiToggleButton extends JButton
{

    // Icons
    private final Icon[] icons;
    // Tooltips
    private final String[] tooltips;
    private final int stateCount;
    @Getter
    private int state = 0;

    public MultiToggleButton(int stateCount)
    {
        super();
        this.stateCount = stateCount;
        icons = new Icon[stateCount];
        tooltips = new String[stateCount];
    }

    /* Action listener must include changeState() */

    public void setIcon(Icon icon, int state)
    {
        if (state < 0 || state > stateCount || icon == null)
        {
            return;
        }

        icons[state] = icon;

        if (state == this.state) setIconState();
    }

    public boolean setIcons(Icon[] icons)
    {
        if (icons == null || icons.length == 0)
        {
            return false;
        }

        for (int i = 0; i < icons.length; i++)
        {
            setIcon(icons[i], i);
        }

        return true;
    }

    public void setToolTip(String tooltip, int state)
    {
        if (state < 0 || state > 3 || tooltip == null)
        {
            return;
        }

        tooltips[state] = tooltip;

        if (state == this.state) setTooltipState();
    }

    public boolean setToolTips(String[] tooltips)
    {
        if (tooltips == null || tooltips.length == 0)
        {
            return false;
        }

        for (int i = 0; i < tooltips.length; i++)
        {
            setToolTip(tooltips[i], i);
        }

        return true;
    }

    public void changeState()
    {
        setState((state++) % stateCount);
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
