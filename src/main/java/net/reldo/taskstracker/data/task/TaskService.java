package net.reldo.taskstracker.data.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerConfig;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.config.ConfigValues;
import net.reldo.taskstracker.data.jsondatastore.ManifestClient;
import net.reldo.taskstracker.data.jsondatastore.TaskDataClient;
import net.reldo.taskstracker.data.jsondatastore.types.FilterConfig;
import net.reldo.taskstracker.data.jsondatastore.types.FilterValueType;
import net.reldo.taskstracker.data.jsondatastore.types.TaskDefinition;
import net.reldo.taskstracker.data.route.CustomRoute;
import net.reldo.taskstracker.data.route.RouteItem;
import net.reldo.taskstracker.data.route.RouteSection;
import net.reldo.taskstracker.data.task.filters.FilterService;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;

@Singleton
@Slf4j
public class TaskService
{
	@Inject
	private ManifestClient manifestClient;
	@Inject
	private TaskDataClient taskDataClient;
	@Inject
	private ClientThread clientThread;
	@Inject
	private Client client;
	@Inject
	private FilterService filterService;
	@Inject
	private ConfigManager configManager;

	@Getter
	@Setter
	private boolean taskTypeChanged = false;
	@Getter
	private TaskType currentTaskType;
	@Getter
	private final List<TaskFromStruct> tasks = new ArrayList<>();
	@Getter
	private final HashMap<String, HashMap<Integer, Integer>> sortedIndexes = new HashMap<>();
	private HashMap<String, TaskType> _taskTypes = new HashMap<>();
	private HashSet<Integer> currentTaskTypeVarps = new HashSet<>();
	private final ExecutorService futureExecutor = Executors.newSingleThreadExecutor();

	// Route state: in-memory cache of active routes per tab and pre-computed sort indexes per route
	private final Map<ConfigValues.TaskListTabs, CustomRoute> tabActiveRoutes = new HashMap<>();
	@Getter
	private final HashMap<String, HashMap<Integer, Integer>> routeIndexes = new HashMap<>();
	@Getter
	private final HashMap<String, HashMap<String, Integer>> customItemRouteIndexes = new HashMap<>();

	public CompletableFuture<Boolean> setTaskType(String taskTypeJsonName)
	{
		return getTaskTypesByJsonName().thenCompose(taskTypes ->
		{
			TaskType newTaskType = taskTypes.get(taskTypeJsonName);
			if (newTaskType == null)
			{
				log.error("unsupported task type {}, falling back to COMBAT", taskTypeJsonName);
				newTaskType = taskTypes.get("COMBAT");
			}
			return this.setTaskType(newTaskType);
		});
	}

	private CompletableFuture<Boolean> loadAllTasksStructData(Collection<TaskFromStruct> tasks)
	{
		Collection<CompletableFuture<Boolean>> taskFutures = new ArrayList<>();
		for (TaskFromStruct task : tasks)
		{
			CompletableFuture<Boolean> taskFuture = new CompletableFuture<>();
			clientThread.invoke(() -> {
				boolean isTaskLoaded = task.loadStructData(client);
				taskFuture.complete(isTaskLoaded);
			});
			taskFutures.add(taskFuture);
		}
		return CompletableFuture.allOf(taskFutures.toArray(new CompletableFuture[0])).thenApply(v -> {
			for (CompletableFuture<Boolean> future : taskFutures)
			{
				if (!future.join())
				{
					return false;
				}
			}
			return true;
		});
	}

	public CompletableFuture<Boolean> setTaskType(TaskType newTaskType)
	{
		log.debug("setTaskType {}", newTaskType.getTaskJsonName());
		if (newTaskType.equals(currentTaskType))
		{
			log.debug("Skipping setTaskType, same task type selected");
			return CompletableFuture.completedFuture(false);
		}
		currentTaskType = newTaskType;
		configManager.setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, "taskTypeJsonName", newTaskType.getTaskJsonName());
		configManager.setConfiguration(TasksTrackerPlugin.CONFIG_GROUP_NAME, "pinnedTaskId", 0);

		// Complete creation of any GLOBAL value type filterConfigs
		for (FilterConfig filterConfig : currentTaskType.getFilters())
		{
			if (filterConfig.getValueType().equals(FilterValueType.GLOBAL))
			{
				// Set valueType to the one required by the global filter
				FilterConfig globalFilterConfig = filterService.getGlobalFilterByKey(filterConfig.getConfigKey());
				filterConfig.setValueType(globalFilterConfig.getValueType());

				// Set any filterConfig fields not already specified
				Optional.ofNullable(filterConfig.getLabel()).ifPresentOrElse(val -> {
				}, () -> filterConfig.setLabel(globalFilterConfig.getLabel()));
				Optional.ofNullable(filterConfig.getFilterType()).ifPresentOrElse(val -> {
				}, () -> filterConfig.setFilterType(globalFilterConfig.getFilterType()));
				Optional.ofNullable(filterConfig.getValueName()).ifPresentOrElse(val -> {
				}, () -> filterConfig.setValueName(globalFilterConfig.getValueName()));
				Optional.ofNullable(filterConfig.getOptionLabelEnum()).ifPresentOrElse(val -> {
				}, () -> filterConfig.setOptionLabelEnum(globalFilterConfig.getOptionLabelEnum()));
				Optional.ofNullable(filterConfig.getCustomItems()).ifPresentOrElse(val -> {
				}, () -> filterConfig.setCustomItems(globalFilterConfig.getCustomItems()));
			}
		}

		List<TaskFromStruct> newTasks = new ArrayList<>();
		return newTaskType.loadTaskTypeDataAsync().thenCompose((isTaskTypeLoaded) -> {
			if (!isTaskTypeLoaded)
			{
				log.error("Error loading task type during setTaskType");
				return CompletableFuture.completedFuture(false);
			}

			CompletableFuture<Boolean> future = new CompletableFuture<>();
			futureExecutor.submit(() -> {
				try
				{
					Collection<TaskDefinition> taskDefinitions = taskDataClient.getTaskDefinitions(currentTaskType.getTaskJsonName());
					for (TaskDefinition definition : taskDefinitions)
					{
						TaskFromStruct task = new TaskFromStruct(currentTaskType, definition);
						newTasks.add(task);
					}
					loadAllTasksStructData(newTasks).thenApply(future::complete);
				}
				catch (Exception e3)
				{
					future.completeExceptionally(e3);
				}
			});
			return future;
		}).thenCompose(areTasksLoaded -> {
			if (!areTasksLoaded)
			{
				return CompletableFuture.completedFuture(false);
			}

			tasks.clear();
			tasks.addAll(newTasks);

			// Clear route state from previous task type
			routeIndexes.clear();
			customItemRouteIndexes.clear();
			tabActiveRoutes.clear();

			// Index task list for each property
			sortedIndexes.clear();
			currentTaskType.getIntParamMap().keySet().forEach(paramName -> {
				sortedIndexes.put(paramName, null);
				addSortedIndex(paramName, Comparator.comparingInt((TaskFromStruct task) -> task.getIntParam(paramName)));
			});
			currentTaskType.getStringParamMap().keySet().forEach(paramName -> {
				sortedIndexes.put(paramName, null);
				addSortedIndex(paramName, Comparator.comparing((TaskFromStruct task) -> task.getStringParam(paramName)));
			});
			// todo: make this less of a special case.
			if (tasks.stream().anyMatch(task -> task.getCompletionPercent() != null))
			{
				sortedIndexes.put("completion %", null);
				addSortedIndex("completion %",
					(TaskFromStruct task1, TaskFromStruct task2) ->
					{
						Float comp1 = task1.getTaskDefinition().getCompletionPercent() != null ? task1.getTaskDefinition().getCompletionPercent() : 0;
						Float comp2 = task2.getTaskDefinition().getCompletionPercent() != null ? task2.getTaskDefinition().getCompletionPercent() : 0;
						return comp1.compareTo(comp2);
					});
			}

			currentTaskTypeVarps.clear();
			currentTaskTypeVarps = new HashSet<>(currentTaskType.getTaskVarps());

			taskTypeChanged = true;
			return CompletableFuture.completedFuture(true);
		});
	}

	private void addSortedIndex(String paramName, Comparator<TaskFromStruct> comparator)
	{
		List<TaskFromStruct> sortedTasks = tasks.stream()
			.sorted(comparator)
			.collect(Collectors.toCollection(ArrayList::new));
		HashMap<Integer, Integer> sortedIndex = new HashMap<>();
		for (TaskFromStruct task : tasks)
		{
			sortedIndex.put(task.getStructId(), sortedTasks.indexOf(task));
		}
		sortedIndexes.put(paramName, sortedIndex);
	}

	public void addRouteIndex(CustomRoute route)
	{
		List<RouteSection> sections = route.getSections();
		HashMap<Integer, Integer> routeIndex = new HashMap<>();
		HashMap<String, Integer> customIndex = new HashMap<>();
		int sectionStartIndex = 0;
		for (RouteSection section : sections)
		{
			List<RouteItem> items = section.getItems();
			for (int i = 0; i < items.size(); i++)
			{
				RouteItem item = items.get(i);
				if (item.isTask())
				{
					routeIndex.put(item.getTaskId(), i + sectionStartIndex + 1);
				}
			}
			sectionStartIndex += items.size() + 1;
		}

		int afterRouteIndex = route.getItemCount() + sections.size();
		for (TaskFromStruct task : tasks)
		{
			if (!route.getFlattenedOrder().contains(task.getStructId()))
			{
				routeIndex.put(task.getStructId(), ++afterRouteIndex);
			}
		}

		routeIndexes.put(route.getName(), routeIndex);
		customItemRouteIndexes.put(route.getName(), customIndex);
	}

	public int getTaskIndex(String sortCriteria, Integer taskStructId)
	{
		return getTaskIndex(sortCriteria, taskStructId, true);
	}

	public int getTaskIndex(String indexName, Integer taskStructId, Boolean ascending)
	{
		int position;
		ConfigValues.TaskListTabs currentTab = configManager.getConfig(TasksTrackerConfig.class).taskListTab();
		boolean activeRoute = hasActiveRoute(currentTab);

		if (!activeRoute && sortedIndexes.containsKey(indexName))
		{
			position = sortedIndexes.get(indexName).get(taskStructId);
		}
		else if (activeRoute && routeIndexes.containsKey(indexName))
		{
			position = routeIndexes.get(indexName).get(taskStructId);
		}
		else
		{
			// Fall back to game UI sort order
			position = getTaskByStructId(taskStructId).getSortId();
		}

		return ascending ? position : tasks.size() - (position + 1);
	}

	public int getCustomItemIndex(String routeName, String customItemId)
	{
		HashMap<String, Integer> index = customItemRouteIndexes.get(routeName);
		if (index == null || !index.containsKey(customItemId))
		{
			return -1;
		}
		return index.get(customItemId);
	}

	public boolean isVarpInCurrentTaskType(int varpId)
	{
		return currentTaskTypeVarps.contains(varpId);
	}

	public void clearTaskTypes()
	{
		this._taskTypes.clear();
	}

	/**
	 * Get a map of task type json names to task type
	 *
	 * @return Hashmap of TaskType indexed by task type json name
	 */
	public CompletableFuture<HashMap<String, TaskType>> getTaskTypesByJsonName()
	{
		if (_taskTypes.size() > 0)
		{
			return CompletableFuture.completedFuture(_taskTypes);
		}

		try
		{
			CompletableFuture<HashMap<String, TaskType>> future = new CompletableFuture<>();
			futureExecutor.submit(() ->
			{
				try
				{
					_taskTypes = taskDataClient.getTaskTypes();
					future.complete(_taskTypes);
				}
				catch (Exception e)
				{
					future.completeExceptionally(e);
				}
			});

			return future;
		}
		catch (Exception ex)
		{
			log.error("Unable to populate task types from data client", ex);
			return CompletableFuture.completedFuture(new HashMap<>());
		}
	}

	public CompletableFuture<HashMap<Integer, String>> getStringEnumValuesAsync(String enumName)
	{
		Integer enumId = currentTaskType.getStringEnumMap().get(enumName);
		if (enumId == null)
		{
			return CompletableFuture.completedFuture(new HashMap<>());
		}

		CompletableFuture<HashMap<Integer, String>> future = new CompletableFuture<>();
		clientThread.invoke(() -> {
			try
			{
				EnumComposition enumComposition = client.getEnum(enumId);
				int[] keys = enumComposition.getKeys();
				HashMap<Integer, String> map = new HashMap<>();
				for (int key : keys)
				{
					map.put(key, enumComposition.getStringValue(key));
				}
				future.complete(map);
			}
			catch (Exception ex)
			{
				log.error("Error getting string enum values", ex);
				future.completeExceptionally(ex);
			}
		});
		return future;
	}

	public void applySave(TaskType saveTaskType, HashMap<Integer, ConfigTaskSave> saveData)
	{
		String currentTaskTypeName = currentTaskType.getTaskJsonName();
		String saveTaskTypeName = saveTaskType.getTaskJsonName();
		if (!currentTaskTypeName.equals(saveTaskTypeName))
		{
			log.warn("Cannot apply save, task types do not match current={} save={}", currentTaskTypeName, saveTaskTypeName);
			return;
		}

		for (TaskFromStruct task : getTasks())
		{
			ConfigTaskSave configTaskSave = saveData.get(task.getStructId());
			if (configTaskSave == null)
			{
				continue;
			}
			task.loadConfigSave(configTaskSave);
		}
	}

	public List<TaskFromStruct> getTasksFromVarpId(Integer varpId)
	{
		int varpIndex = getCurrentTaskType().getTaskVarps().indexOf(varpId);
		int minTaskId = varpIndex * 32;
		int maxTaskId = minTaskId + 32;

		return getTasks().stream().filter(t -> {
			int taskId = t.getIntParam("id");
			return taskId >= minTaskId && taskId <= maxTaskId;
		}).collect(Collectors.toList());
	}

	// ========================================================================
	// Route State Management
	// Manages in-memory route state per tab. When a route is activated,
	// a sort index is pre-computed mapping positions to task indexes.
	// ========================================================================

	/**
	 * Sets the active route for a tab. Pass null to clear.
	 */
	public void setActiveRoute(ConfigValues.TaskListTabs tab, CustomRoute route)
	{
		if (route == null)
		{
			tabActiveRoutes.remove(tab);
		}
		else
		{
			tabActiveRoutes.put(tab, route);
			addRouteIndex(route); // @todo move this on on task type change and route modification only
		}
	}

	/** Returns the active route for a tab, or null if none. */
	public CustomRoute getActiveRoute(ConfigValues.TaskListTabs tab)
	{
		return tabActiveRoutes.get(tab);
	}

	/** Returns true if a route is currently active for the given tab. */
	public boolean hasActiveRoute(ConfigValues.TaskListTabs tab)
	{
		return tabActiveRoutes.containsKey(tab);
	}

	/** Clears the active route for a tab. */
	public void clearActiveRoute(ConfigValues.TaskListTabs tab)
	{
		setActiveRoute(tab, null);
	}

	/** Finds a task by its struct ID. Returns null if not found. */
	public TaskFromStruct getTaskByStructId(Integer taskStructId)
	{
		return tasks.stream()
			.filter(t -> t.getStructId().equals(taskStructId))
			.findFirst()
			.orElse(null);
	}
}
