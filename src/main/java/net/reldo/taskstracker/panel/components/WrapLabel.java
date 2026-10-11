package net.reldo.taskstracker.panel.components;

import java.awt.FontMetrics;
import java.awt.font.FontRenderContext;
import java.util.Objects;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class WrapLabel extends JPanel
{
	private String text = "";
	private final int labelWidth;

	public WrapLabel(String text, int labelWidth)
	{
		this.labelWidth = labelWidth;

		setBorder(new EmptyBorder(0, 0, 0, 0));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);

		setText(text);
	}

	public void setText(String text)
	{
		if (Objects.equals(this.text, text))
		{
			return;
		}

		this.text = text;
		this.removeAll();

		getFont().getStringBounds(text, new FontRenderContext(null, false, false));
		final FontMetrics fontMetrics = getFontMetrics(getFont());

		int stringWidth = fontMetrics.stringWidth(text);
		if (stringWidth <= labelWidth)
		{
			addLabel(text);
			return;
		}

		String[] parts = text.split("\\b");
		String partSum = "";

		for (String part : parts)
		{
			String nextPart = partSum + part;
			if (fontMetrics.stringWidth(nextPart) > labelWidth)
			{
				addLabel(partSum);
				partSum = part;
				continue;
			}
			partSum = nextPart;
		}

		if (!partSum.isEmpty())
		{
			addLabel(partSum);
		}
	}

	private void addLabel(String text)
	{
		JLabel label = new JLabel(text);
		label.setFont(getFont());
		label.setForeground(getForeground());

		add(label);
	}
}
