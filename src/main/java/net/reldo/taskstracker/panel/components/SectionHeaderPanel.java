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
import lombok.Setter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

public class SectionHeaderPanel extends JPanel
{
	private static final Color BACKGROUND_COLOR = ColorScheme.DARKER_GRAY_COLOR.darker();
	private static final Color HOVER_COLOR = ColorScheme.DARKER_GRAY_COLOR;
	private static final Color TEXT_COLOR = Color.WHITE;
	private static final Color PROGRESS_COLOR = new Color(180, 180, 180);
	private static final Color PROGRESS_COMPLETE_COLOR = new Color(100, 200, 100);

	private static final String ARROW_EXPANDED = "\u25BC";
	private static final String ARROW_COLLAPSED = "\u25B6";

	@Getter
	private final String sectionName;

	@Getter
	private boolean collapsed = false;

	private final JPanel container;
	private final JLabel titleLabel;
	private final JLabel progressLabel;
	private final String description;

	@Setter
	private Consumer<Boolean> collapseCallback;

	public SectionHeaderPanel(String sectionName, String description)
	{
		this.sectionName = sectionName;
		this.description = description;

		setLayout(new BorderLayout());
		setOpaque(false);
		setBorder(new EmptyBorder(0, 0, 4, 0));

		container = new JPanel(new BorderLayout());
		container.setBackground(BACKGROUND_COLOR);
		container.setBorder(new EmptyBorder(6, 10, 6, 10));
		container.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		// Title with arrow and optional description
		titleLabel = new JLabel();
		titleLabel.setForeground(TEXT_COLOR);
		titleLabel.setFont(FontManager.getRunescapeFont());
		updateTitleText();

		// Progress label (right side)
		progressLabel = new JLabel();
		progressLabel.setForeground(PROGRESS_COLOR);
		progressLabel.setFont(FontManager.getRunescapeSmallFont());

		container.add(titleLabel, BorderLayout.CENTER);
		container.add(progressLabel, BorderLayout.EAST);

		add(container, BorderLayout.CENTER);

		// Click to toggle collapse
		container.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				toggleCollapse();
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				container.setBackground(HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				container.setBackground(BACKGROUND_COLOR);
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
			html.append(" <span style='color: rgb(120,120,120); font-style: italic;'>- ").append(description)
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
