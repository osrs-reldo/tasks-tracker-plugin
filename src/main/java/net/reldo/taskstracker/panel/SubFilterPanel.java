package net.reldo.taskstracker.panel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.BoxLayout;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.jsondatastore.types.FilterConfig;
import net.reldo.taskstracker.data.jsondatastore.types.FilterValueType;
import net.reldo.taskstracker.data.task.TaskService;
import net.reldo.taskstracker.panel.components.FixedWidthPanel;
import net.reldo.taskstracker.panel.filters.ComboItem;
import net.reldo.taskstracker.panel.filters.DynamicButtonFilterPanel;
import net.reldo.taskstracker.panel.filters.DynamicDropdownFilterPanel;
import net.reldo.taskstracker.panel.filters.FilterPanel;
import net.runelite.client.ui.ColorScheme;

@Slf4j
public class SubFilterPanel extends FixedWidthPanel
{
	private final List<FilterPanel> filterPanels = new ArrayList<>();
	private TasksTrackerPlugin plugin;
	private TaskService taskService;

	public SubFilterPanel(TasksTrackerPlugin plugin, TaskService taskService)
	{
		this.plugin = plugin;
		this.taskService = taskService;
		log.debug("SubFilterPanel.constructor");
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new EmptyBorder(0, 0, 0, 0));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setVisible(false);
	}

	public void redraw()
	{
		log.debug("SubFilterPanel.redraw"); // TODO: figure out why this calls multiple times upon switching task type

		removeAll();

		filterPanels.clear();
		filterPanels.addAll(getFilterPanels(taskService.getCurrentTaskTypeDefinition().getFilters()));
		filterPanels.forEach((filterPanel) -> {
			add(filterPanel);
			filterPanel.redraw();
		});
	}

	private List<FilterPanel> getFilterPanels(ArrayList<FilterConfig> filterConfigs)
	{
		List<FilterPanel> filterPanels = new ArrayList<>();
		for (FilterConfig filterConfig : filterConfigs)
		{
			try
			{
				FilterPanel filterPanel = createDynamicFilterPanel(filterConfig);
				if (filterPanel == null)
				{
					continue;
				}
				filterPanels.add(filterPanel);
			}
			catch (Exception ex)
			{
				log.error("error creating filter panel {} {}", filterConfig.getConfigKey(), ex);
			}
		}
		return filterPanels;
	}

	private FilterPanel createDynamicFilterPanel(FilterConfig filterConfig) throws Exception
	{
		switch (filterConfig.getFilterType())
		{
			case BUTTON_FILTER:
				return new DynamicButtonFilterPanel(plugin, filterConfig, taskService.getCurrentTaskType());
			case DROPDOWN_FILTER:
				ComboItem[] dropdownItems = getDropdownItems(filterConfig);
				return new DynamicDropdownFilterPanel(plugin, filterConfig, taskService.getCurrentTaskType(), dropdownItems);
			default:
				log.error("invalid filter type " + filterConfig.getFilterType());
				return null;
		}
	}

	private ComboItem[] getDropdownItems(FilterConfig filterConfig) throws ExecutionException, InterruptedException
	{
		if (filterConfig.getValueType() == null)
		{
			throw new Error("invalid filterConfig for dropdown items");
		}
		if (filterConfig.getValueType().equals(FilterValueType.PARAM_INTEGER))
		{
			String enumName = filterConfig.getOptionLabelEnum();
			if (!enumName.isEmpty())
			{
				HashMap<Integer, String> enumEntries = taskService.getStringEnumValuesAsync(enumName).get(); // TODO: blocking call
				ArrayList<ComboItem> options = new ArrayList<>();
				options.add(new ComboItem(-1, ""));
				for (Map.Entry<Integer, String> entry : enumEntries.entrySet())
				{
					if (filterConfig.getValueName().equals("tier"))
					{
						if (entry.getValue().equals("All") || entry.getValue().equals("Tier"))
						{
							continue;
						}
					}
					options.add(new ComboItem(entry.getKey(), entry.getValue()));
				}
				return options.toArray(new ComboItem[options.size()]);
			}
		}

		return new ComboItem[0];
	}
}
