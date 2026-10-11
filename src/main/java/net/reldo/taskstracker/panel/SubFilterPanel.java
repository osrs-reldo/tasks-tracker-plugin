package net.reldo.taskstracker.panel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.SwingUtilities;
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
import net.reldo.taskstracker.panel.filters.SkillButtonFilterPanel;
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
		filterPanels.addAll(getFilterPanels(taskService.getCurrentTaskType().getFilters()));
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
		if (filterConfig.getFilterType() == null)
		{
			log.warn("Skipping filter panel for {} - filterType is null", filterConfig.getConfigKey());
			return null;
		}
		switch (filterConfig.getFilterType())
		{
			case BUTTON_FILTER:
				if (filterConfig.getValueType() == FilterValueType.SKILL || filterConfig.getConfigKey().equals("skill"))
				{
					return new SkillButtonFilterPanel(plugin, filterConfig, taskService.getCurrentTaskType());
				}
				return new DynamicButtonFilterPanel(plugin, filterConfig, taskService.getCurrentTaskType());
			case DROPDOWN_FILTER:
				ComboItem[] dropdownItems = getDropdownItems(filterConfig);
				DynamicDropdownFilterPanel dropdownPanel = new DynamicDropdownFilterPanel(plugin, filterConfig, taskService.getCurrentTaskType(), dropdownItems);
				populateDropdownItemsAsync(filterConfig, dropdownPanel);
				return dropdownPanel;
			default:
				log.error("invalid filter type " + filterConfig.getFilterType());
				return null;
		}
	}

	private ComboItem[] getDropdownItems(FilterConfig filterConfig)
	{
		if (filterConfig.getValueType() == null)
		{
			throw new Error("invalid filterConfig for dropdown items");
		}
		if (filterConfig.getValueType().equals(FilterValueType.PARAM_INTEGER) && !filterConfig.getOptionLabelEnum().isEmpty())
		{
			ArrayList<ComboItem<Integer>> options = new ArrayList<>();
			options.add(new ComboItem<>(-1, ""));
			return options.toArray(new ComboItem[0]);
		}

		return new ComboItem[0];
	}

	private void populateDropdownItemsAsync(FilterConfig filterConfig, DynamicDropdownFilterPanel dropdownPanel)
	{
		if (filterConfig.getValueType() != FilterValueType.PARAM_INTEGER)
		{
			return;
		}
		String enumName = filterConfig.getOptionLabelEnum();
		if (enumName == null || enumName.isEmpty())
		{
			return;
		}

		taskService.getStringEnumValuesAsync(enumName).thenAccept(enumEntries -> {
			ArrayList<ComboItem<Integer>> options = new ArrayList<>();
			options.add(new ComboItem<>(-1, ""));
			for (Map.Entry<Integer, String> entry : enumEntries.entrySet())
			{
				if (filterConfig.getValueName().equals("tier"))
				{
					if (entry.getValue().equals("All") || entry.getValue().equals("Tier"))
					{
						continue;
					}
				}
				options.add(new ComboItem<>(entry.getKey(), entry.getValue()));
			}
			SwingUtilities.invokeLater(() -> dropdownPanel.setItems(options.toArray(new ComboItem[0])));
		}).exceptionally(ex -> {
			log.error("error populating dropdown items for {}", filterConfig.getConfigKey(), ex);
			return null;
		});
	}
}
