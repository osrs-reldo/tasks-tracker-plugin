package net.reldo.taskstracker.panel.components;

import java.awt.Insets;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class WrapLabel extends JTextArea
{
	public WrapLabel(String text)
	{
		super(text);

		setLineWrap(true);
		setWrapStyleWord(true);

		// Format area to look like JLabel
		setEditable(false);
		setFocusable(false);
		setOpaque(false);
		setBorder(new EmptyBorder(0, 0, 0, 0));
		setMargin(new Insets(0, 0, 0, 0));

		setHighlighter(null);
	}
}
