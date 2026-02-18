package net.reldo.taskstracker.data.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.jsondatastore.ManifestClient;
import net.reldo.taskstracker.data.jsondatastore.TaskDataClient;
import net.reldo.taskstracker.data.jsondatastore.types.FilterConfig;
import net.reldo.taskstracker.data.jsondatastore.types.FilterValueType;
import net.reldo.taskstracker.data.jsondatastore.types.TaskDefinition;
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
	private final HashMap<String, int[]> sortedIndexes = new HashMap<>();
	private HashMap<String, TaskType> _taskTypes = new HashMap<>();
	private HashSet<Integer> currentTaskTypeVarps = new HashSet<>();
	private final ExecutorService futureExecutor = Executors.newSingleThreadExecutor();

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
		int[] sortedIndex = new int[tasks.size()];
		for (int i = 0; i < sortedTasks.size(); i++)
		{
			sortedIndex[i] = tasks.indexOf(sortedTasks.get(i));
		}
		sortedIndexes.put(paramName, sortedIndex);
	}

	public int getSortedTaskIndex(String sortCriteria, int position)
	{
		if (sortedIndexes.containsKey(sortCriteria))
		{
			return sortedIndexes.get(sortCriteria)[position];
		}
		else
		{
			return position;
		}
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
}
