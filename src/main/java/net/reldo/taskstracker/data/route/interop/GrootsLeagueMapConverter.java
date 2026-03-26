package net.reldo.taskstracker.data.route.interop;

import java.util.ArrayList;
import javax.swing.JOptionPane;
import net.reldo.taskstracker.data.route.CustomRoute;
import net.reldo.taskstracker.data.route.RouteItem;
import net.reldo.taskstracker.data.route.RouteSection;
import net.reldo.taskstracker.data.task.TaskService;

public class GrootsLeagueMapConverter implements RouteConverter
{
	private static final String SOURCE = "GrootsLeagueMap";
	private static final int CURRENT_VERSION = 1;

	@Override
	public String getSource()
	{
		return SOURCE;
	}

	@Override
	public CustomRoute beforeImport(CustomRoute route)
	{
		String name = JOptionPane.showInputDialog(
			null,
			"Enter a name for this GrootsLeagueMap route:",
			"Import GrootsLeagueMap Route",
			JOptionPane.PLAIN_MESSAGE
		);

		if (name == null || name.trim().isEmpty())
		{
			return null;
		}

		route.setName(name.trim());
		return route;
	}

	@Override
	public void convertToNative(CustomRoute route, TaskService taskService)
	{
		// No transformation needed — opaque fields (pinCoords, comments)
		// are preserved on RouteItem for round-trip re-export.
	}

	@Override
	public CustomRoute beforeExport(CustomRoute route)
	{
		return route;
	}

	@Override
	public void convertToExternal(CustomRoute route, TaskService taskService)
	{
		route.setSource(SOURCE);
		route.setVersion(CURRENT_VERSION);

		if (route.getSections() == null)
		{
			return;
		}

		for (RouteSection section : route.getSections())
		{
			if (section.getItems() == null)
			{
				continue;
			}

			for (RouteItem item : section.getItems())
			{
				// Split note back into comments list for web tool compatibility
				if (item.getComments() == null)
				{
					item.setComments(new ArrayList<>());
				}

				// pinCoords are already preserved on the item from import
			}
		}
	}
}
