package net.reldo.taskstracker.panel.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.reldo.taskstracker.data.route.CustomRoute;
import net.runelite.client.ui.ColorScheme;

public class RouteSelector extends JPanel
{
	private static final CustomRoute NO_ROUTE_OPTION = new CustomRoute("ThisIsNotARoute", "(none)", "AllTaskTypes" );

	private final JComboBox<CustomRoute> routeDropdown;
	private final JButton manageButton;
	private boolean isUpdating = false;

	public RouteSelector()
	{
		setLayout(new BorderLayout(5, 0));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel label = new JLabel(" L");
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

		routeDropdown = new JComboBox<>();
		routeDropdown.setToolTipText("Select active route");
		routeDropdown.setFocusable(false);
		routeDropdown.setPreferredSize(new Dimension(150, 25));

		manageButton = new JButton("...");
		manageButton.setToolTipText("Manage routes");
		manageButton.setFocusable(false);
		manageButton.setPreferredSize(new Dimension(30, 25));

		add(label, BorderLayout.WEST);
		add(routeDropdown, BorderLayout.CENTER);
		add(manageButton, BorderLayout.EAST);
	}

	public void setRoutes(List<CustomRoute> routes, String activeRouteId)
	{
		isUpdating = true;
		try
		{
			routeDropdown.removeAllItems();
			routeDropdown.addItem(NO_ROUTE_OPTION);

			if (routes != null)
			{
				for (CustomRoute route : routes)
				{
					routeDropdown.addItem(route);
				}

				routes.stream()
					.filter(route -> route.getId().equals(activeRouteId))
					.findFirst()
					.ifPresentOrElse(routeDropdown::setSelectedItem, () -> routeDropdown.setSelectedItem(NO_ROUTE_OPTION));
			}
			else
			{
				routeDropdown.setSelectedIndex(0);
			}
		}
		finally
		{
			isUpdating = false;
		}
	}

	public CustomRoute getSelectedRoute()
	{
		CustomRoute selected = (CustomRoute) routeDropdown.getSelectedItem();
		return NO_ROUTE_OPTION.equals(selected) ? null : selected;
	}

	public String getSelectedRouteId()
	{
		CustomRoute selected = (CustomRoute) routeDropdown.getSelectedItem();
		return (selected == null || NO_ROUTE_OPTION.equals(selected)) ? null : selected.getId();
	}

	public String getSelectedRouteName()
	{
		CustomRoute selected = (CustomRoute) routeDropdown.getSelectedItem();
		return (selected == null || NO_ROUTE_OPTION.equals(selected)) ? null : selected.getName();
	}

	public void addRouteChangeListener(ActionListener listener)
	{
		routeDropdown.addActionListener(e -> {
			if (!isUpdating)
			{
				listener.actionPerformed(e);
			}
		});
	}

	public void addManageListener(ActionListener listener)
	{
		manageButton.addActionListener(listener);
	}
}
