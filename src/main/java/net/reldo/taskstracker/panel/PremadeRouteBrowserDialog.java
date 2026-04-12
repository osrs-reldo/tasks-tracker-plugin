package net.reldo.taskstracker.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import net.reldo.taskstracker.data.jsondatastore.types.PremadeRouteEntry;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

public class PremadeRouteBrowserDialog
{
	/**
	 * Shows a dialog for browsing and selecting a premade route.
	 * @param parent the parent component for dialog positioning
	 * @param entries the premade route entries to display
	 * @return the selected entry, or null if the user cancelled
	 */
	public static PremadeRouteEntry show(Component parent, List<PremadeRouteEntry> entries)
	{
		DefaultListModel<PremadeRouteEntry> listModel = new DefaultListModel<>();
		entries.forEach(listModel::addElement);

		JList<PremadeRouteEntry> list = new JList<>(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new PremadeRouteListRenderer());
		list.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setPreferredSize(new Dimension(340, 300));

		JButton downloadButton = new JButton("Download");
		downloadButton.setEnabled(false);
		JButton cancelButton = new JButton("Cancel");

		list.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting())
			{
				downloadButton.setEnabled(list.getSelectedValue() != null);
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(downloadButton);
		buttonPanel.add(cancelButton);

		JPanel content = new JPanel(new BorderLayout(0, 8));
		content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		content.add(scrollPane, BorderLayout.CENTER);
		content.add(buttonPanel, BorderLayout.SOUTH);

		JDialog dialog = new JDialog();
		dialog.setTitle("Premade Routes");
		dialog.setModal(true);
		dialog.setContentPane(content);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setAlwaysOnTop(true);

		final PremadeRouteEntry[] selected = {null};

		downloadButton.addActionListener(e -> {
			selected[0] = list.getSelectedValue();
			dialog.dispose();
		});

		cancelButton.addActionListener(e -> dialog.dispose());

		// Double-click to download
		list.addMouseListener(new java.awt.event.MouseAdapter()
		{
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e)
			{
				if (e.getClickCount() == 2 && list.getSelectedValue() != null)
				{
					selected[0] = list.getSelectedValue();
					dialog.dispose();
				}
			}
		});

		dialog.setVisible(true);
		return selected[0];
	}

	private static class PremadeRouteListRenderer extends JPanel implements ListCellRenderer<PremadeRouteEntry>
	{
		private final JLabel nameLabel = new JLabel();
		private final JLabel authorLabel = new JLabel();
		private final JLabel descriptionLabel = new JLabel();

		PremadeRouteListRenderer()
		{
			setLayout(new BorderLayout(0, 2));
			setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

			nameLabel.setFont(FontManager.getRunescapeBoldFont());
			authorLabel.setFont(FontManager.getRunescapeSmallFont());
			authorLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			descriptionLabel.setFont(FontManager.getRunescapeSmallFont());
			descriptionLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

			JPanel topRow = new JPanel(new BorderLayout());
			topRow.setOpaque(false);
			topRow.add(nameLabel, BorderLayout.WEST);
			topRow.add(authorLabel, BorderLayout.EAST);

			add(topRow, BorderLayout.NORTH);
			add(descriptionLabel, BorderLayout.CENTER);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends PremadeRouteEntry> list, PremadeRouteEntry entry, int index, boolean isSelected, boolean cellHasFocus)
		{
			nameLabel.setText(entry.getName());
			authorLabel.setText(entry.getAuthor() != null ? "by " + entry.getAuthor() : "");
			descriptionLabel.setText(entry.getDescription() != null ? entry.getDescription() : "");

			if (isSelected)
			{
				setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
				nameLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
				authorLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			}
			else
			{
				setBackground(index % 2 == 0 ? ColorScheme.DARKER_GRAY_COLOR : ColorScheme.DARK_GRAY_COLOR);
				nameLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
				authorLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
			}

			return this;
		}
	}
}
