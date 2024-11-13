package net.reldo.taskstracker.data.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.data.jsondatastore.ManifestClient;
import net.reldo.taskstracker.data.jsondatastore.TaskDataClient;
import net.reldo.taskstracker.data.jsondatastore.types.FilterConfig;
import net.reldo.taskstracker.data.jsondatastore.types.FilterValueType;
import net.reldo.taskstracker.data.jsondatastore.types.TaskDefinition;
import net.reldo.taskstracker.data.jsondatastore.types.TaskTypeDefinition;
import net.reldo.taskstracker.data.task.filters.FilterService;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.client.callback.ClientThread;

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

	@Getter
	@Setter
	private boolean taskTypeChanged = false;
	@Getter
	private TaskType currentTaskType; // TODO: should be config driven
	@Getter
	private final HashMap<Integer, List<TaskFromStruct>> currentTasksByVarp = new HashMap<>();
	// TODO: Build the filter on getTasks
	@Getter
	private final List<TaskFromStruct> tasks = new ArrayList<>();
	@Getter
	private final HashMap<String, int[]> sortedIndexes = new HashMap<>();
	private HashMap<String, TaskType> _taskTypes = new HashMap<>();
	private HashSet<Integer> currentTaskTypeVarps = new HashSet<>();

	public void setTaskType(String taskTypeName)
	{
		// TODO: cache all task types?
		try
		{
			TaskType newTaskType = getTaskTypes().get(taskTypeName);
			if (newTaskType == null)
			{
				log.error("unsupported task type {}, falling back to COMBAT", taskTypeName);
				newTaskType = getTaskTypes().get("COMBAT");
			}
			tasks.clear();
			currentTaskType = newTaskType;

            // Complete creation of any GLOBAL value type filterConfigs
			for (FilterConfig filterConfig : currentTaskType.getFilters())
			{
				if (filterConfig.getValueType().equals(FilterValueType.GLOBAL))
				{
					// Set valueType to the one required by the global filter
					FilterConfig globalFilterConfig = filterService.getGlobalFilterByKey(filterConfig.getConfigKey());
					filterConfig.setValueType(globalFilterConfig.getValueType());

					// Set any filterConfig fields not already specified
					Optional.ofNullable(filterConfig.getLabel()).ifPresentOrElse(val -> {}, () -> filterConfig.setLabel(globalFilterConfig.getLabel()));
					Optional.ofNullable(filterConfig.getFilterType()).ifPresentOrElse(val -> {}, () -> filterConfig.setFilterType(globalFilterConfig.getFilterType()));
					Optional.ofNullable(filterConfig.getValueName()).ifPresentOrElse(val -> {}, () -> filterConfig.setValueName(globalFilterConfig.getValueName()));
					Optional.ofNullable(filterConfig.getOptionLabelEnum()).ifPresentOrElse(val -> {}, () -> filterConfig.setOptionLabelEnum(globalFilterConfig.getOptionLabelEnum()));
					Optional.ofNullable(filterConfig.getCustomItems()).ifPresentOrElse(val -> {}, () -> filterConfig.setCustomItems(globalFilterConfig.getCustomItems()));
				}
			}

			boolean loaded = currentTaskType.loadTaskTypeDataAsync().get(); // TODO: blocking
			if (!loaded)
			{
				throw new Exception("LOADING TASKTYPE ERROR");
			}

			currentTaskTypeVarps.clear();
			currentTaskTypeVarps = new HashSet<>(currentTaskType.getTaskVarps());

			currentTasksByVarp.clear();
			Collection<TaskDefinition> taskDefinitions = taskDataClient.getTaskDefinitions(currentTaskType.getTaskJsonName());
			for (TaskDefinition definition : taskDefinitions)
			{
				TaskFromStruct task = new TaskFromStruct(currentTaskType, definition);
				tasks.add(task);
				clientThread.invoke(() -> task.loadStructData(client));
				addVarpLookup(task);
			}

			// Index task list for each property @todo check if clientThread.invoke guarantees all task data will be loaded before sorting
			sortedIndexes.clear();
			currentTaskType.getIntParamMap().keySet().forEach(paramName ->
					clientThread.invoke(() ->
							addSortedIndex(paramName, Comparator.comparingInt((TaskFromStruct task) -> task.getIntParam(paramName)))
					)
			);
			currentTaskType.getStringParamMap().keySet().forEach(paramName ->
					clientThread.invoke(() ->
							addSortedIndex(paramName, Comparator.comparing((TaskFromStruct task) -> task.getStringParam(paramName)))
					)
			);
//			clientThread.invoke(() ->
//						addSortedIndex("completion %",
//								Comparator.comparing((TaskFromStruct task) -> task.getTaskDefinition().getCompletionPercent()))
//			);
			clientThread.invoke(() ->// todo: make this less of a special case.
					addSortedIndex("completion %",
							(TaskFromStruct task1, TaskFromStruct task2) ->
							{
								Float comp1 = task1.getTaskDefinition().getCompletionPercent() != null ? task1.getTaskDefinition().getCompletionPercent() : 0;
								Float comp2 = task2.getTaskDefinition().getCompletionPercent() != null ? task2.getTaskDefinition().getCompletionPercent() : 0;
								return comp1.compareTo(comp2);
							})
			);

			taskTypeChanged = true;
		}
		catch (Exception ex)
		{
			log.error("Unable to set task type", ex);
		}
	}

	private void addSortedIndex(String paramName, Comparator<TaskFromStruct> comparator)
	{
		List<TaskFromStruct> sortedTasks = tasks.stream()
				.sorted(comparator)
				.collect(Collectors.toCollection(ArrayList::new));
		int[] sortedIndex = new int[tasks.size()];
		for(int i = 0; i < sortedTasks.size(); i++)
		{
			sortedIndex[i] = tasks.indexOf(sortedTasks.get(i));
		}
		sortedIndexes.put(paramName, sortedIndex);
	}

	public int getSortedTaskIndex(String sortCriteria, int position)
	{
		if(sortedIndexes.containsKey(sortCriteria))
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
	public HashMap<String, TaskType> getTaskTypes()
	{
		// TODO: Consider a cache refresh on a regular-interval
		if (_taskTypes.size() > 0)
		{
			return _taskTypes;
		}

		try
		{
			_taskTypes = taskDataClient.getTaskTypes();
			return _taskTypes;
		}
		catch (Exception ex)
		{
			log.error("Unable to populate task types from data client", ex);
			return new HashMap<>();
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

	private void addVarpLookup(TaskFromStruct task)
	{
		if (!currentTasksByVarp.containsKey(task.getTaskVarp()))
		{
			currentTasksByVarp.put(task.getTaskVarp(), new ArrayList<>());
		}
		currentTasksByVarp.get(task.getTaskVarp()).add(task);
	}

	public void applySave(TaskType saveTaskType, HashMap<Integer, ConfigTaskSave> saveData)
	{
		String currentTaskTypeName = currentTaskType.getTaskJsonName();
		String saveTaskTypeName = saveTaskType.getTaskJsonName();
		if (!currentTaskTypeName.equals(saveTaskTypeName))
		{
			log.warn("cannot apply save, task types do not match current={} save={}", currentTaskTypeName, saveTaskTypeName);
			return;
		}

		for (TaskFromStruct task : getTasks())
		{
			ConfigTaskSave configTaskSave = saveData.get(task.getStructId());
			task.loadConfigSave(configTaskSave);
		}
	}
}
