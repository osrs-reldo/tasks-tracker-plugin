package net.reldo.taskstracker.panel.components;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

public class WrappingLabel extends JPanel
{
	private String text;

	public WrappingLabel(String text)
	{
		this.text = text;
		setOpaque(false); // Optional: make background transparent
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		FontMetrics fm = g2d.getFontMetrics();
		int lineHeight = fm.getAscent() + fm.getDescent() + fm.getLeading();

		int x = 0;
		int y = fm.getAscent();

		// Wrap text and draw each line
		for (String line : wrapText(text, getWidth(), fm))
		{
			g2d.drawString(line, x, y);
			y += lineHeight;
		}
	}

	@Override
	public Dimension getPreferredSize()
	{
		FontMetrics fm = getFontMetrics(getFont());
		int lineHeight = fm.getAscent() + fm.getDescent() + fm.getLeading();

		// Calculate number of lines
		int lines = wrapText(text, getWidth(), fm).size();
		int height = lines * lineHeight;

		return new Dimension(getWidth(), height);
	}

	private List<String> wrapText(String text, int maxWidth, FontMetrics fm)
	{
		List<String> lines = new ArrayList<>();
		StringBuilder sb = new StringBuilder();

		for (String word : text.split(" "))
		{
			if (fm.stringWidth(sb + " " + word) > maxWidth)
			{
				lines.add(sb.toString());
				sb = new StringBuilder(word);
			}
			else
			{
				if (sb.length() > 0)
				{
					sb.append(" ");
				}
				sb.append(word);
			}
		}
		if (sb.length() > 0)
		{
			lines.add(sb.toString());
		}
		return lines;
	}

	public void setText(String text)
	{
		this.text = text;
		revalidate(); // Notify parent to re-layout
		repaint();    // Redraw the component
	}

	public String getText()
	{
		return text;
	}
}
