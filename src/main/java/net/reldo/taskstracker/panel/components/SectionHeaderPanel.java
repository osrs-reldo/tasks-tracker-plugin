package net.reldo.taskstracker.panel.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.client.ui.FontManager;

public class SectionHeaderPanel extends JPanel
{
	private static final Color BACKGROUND_COLOR = new Color(60, 63, 65);
	private static final Color HOVER_COLOR = new Color(70, 73, 75);
	private static final Color TEXT_COLOR = Color.WHITE;
	private static final Color PROGRESS_COLOR = new Color(180, 180, 180);
	private static final Color PROGRESS_COMPLETE_COLOR = new Color(100, 200, 100);

	private static final String ARROW_EXPANDED = "\u25BC";
	private static final String ARROW_COLLAPSED = "\u25B6";

	@Getter
	private final String sectionName;

	private boolean collapsed = false;

	private final JLabel titleLabel;
	private final JLabel progressLabel;
	private final String description;

	private Consumer<Boolean> collapseCallback;

	public SectionHeaderPanel(String sectionName, String description)
	{
		this.sectionName = sectionName;
		this.description = description;

		setLayout(new BorderLayout());
		setBackground(BACKGROUND_COLOR);
		setBorder(new EmptyBorder(8, 12, 8, 12));
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		// Title with arrow and optional description
		titleLabel = new JLabel();
		titleLabel.setForeground(TEXT_COLOR);
		titleLabel.setFont(FontManager.getRunescapeBoldFont());
		updateTitleText();

		// Progress label (right side)
		progressLabel = new JLabel();
		progressLabel.setForeground(PROGRESS_COLOR);
		progressLabel.setFont(FontManager.getRunescapeSmallFont());

		add(titleLabel, BorderLayout.CENTER);
		add(progressLabel, BorderLayout.EAST);

		// Click to toggle collapse
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				toggleCollapse();
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				setBackground(HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				setBackground(BACKGROUND_COLOR);
			}
		});
	}

	public void setProgress(int completed, int total)
	{
		progressLabel.setText(completed + "/" + total);

		if (completed >= total && total > 0)
		{
			progressLabel.setForeground(PROGRESS_COMPLETE_COLOR);
		}
		else
		{
			progressLabel.setForeground(PROGRESS_COLOR);
		}
	}

	public void setCollapseCallback(Consumer<Boolean> callback)
	{
		this.collapseCallback = callback;
	}

	private void toggleCollapse()
	{
		collapsed = !collapsed;
		updateTitleText();

		if (collapseCallback != null)
		{
			collapseCallback.accept(collapsed);
		}
	}

	private void updateTitleText()
	{
		String arrow = collapsed ? ARROW_COLLAPSED : ARROW_EXPANDED;
		StringBuilder html = new StringBuilder();
		html.append("<html>").append(arrow).append(" ").append(sectionName);

		if (description != null && !description.isEmpty())
		{
			html.append(" <span style='color: rgb(150,150,150); font-style: italic;'>").append("\u2014 ").append(description)
				.append("</span>");
		}

		html.append("</html>");
		titleLabel.setText(html.toString());
	}

	/**
	 * Sets collapse state without triggering callback.
	 */
	public void setCollapsedSilent(boolean collapsed)
	{
		this.collapsed = collapsed;
		updateTitleText();
	}
}
