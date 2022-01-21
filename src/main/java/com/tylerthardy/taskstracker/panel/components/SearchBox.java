package com.tylerthardy.taskstracker.panel.components;

import java.awt.Dimension;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;

public class SearchBox extends IconTextField
{
	private SearchBoxCallback fn;

	public SearchBox()
	{
		this.setIcon(IconTextField.Icon.SEARCH);
		this.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
		this.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		this.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
		this.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent documentEvent)
			{
				fn.call();
			}

			@Override
			public void removeUpdate(DocumentEvent documentEvent)
			{
				fn.call();
			}

			@Override
			public void changedUpdate(DocumentEvent documentEvent)
			{
			}
		});

		this.addActionListener(e -> fn.call()
		);
	}

	public void addTextChangedListener(SearchBoxCallback fn)
	{
		this.fn = fn;
	}

	public interface SearchBoxCallback
	{
		void call();
	}
}
