package net.reldo.taskstracker.data.task;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import net.reldo.taskstracker.data.jsondatastore.types.FilterConfig;
import net.reldo.taskstracker.data.jsondatastore.types.TaskCompletionCountTierDefinition;
import net.reldo.taskstracker.data.jsondatastore.types.TaskPointTierDefinition;
import net.reldo.taskstracker.data.jsondatastore.types.TaskTypeDefinition;

public interface ITaskType
{
	String getTaskJsonName();

	String getName();

	ArrayList<FilterConfig> getFilters();

	ArrayList<Integer> getTaskVarps();

	int[] getOtherVarps();

	int[] getVarbits();

	int getTaskCompletedScriptId();

	HashMap<String, Integer> getIntParamMap();

	HashMap<String, Integer> getStringParamMap();

	HashMap<String, Integer> getStringEnumMap();

	HashMap<Integer, BufferedImage> getSpritesById();

	HashMap<Integer, BufferedImage> getTierSprites();

	HashMap<Integer, Integer> getTierPoints();

	String getFilterConfigPrefix();

	ArrayList<TaskPointTierDefinition> getTaskPointTiers();

	ArrayList<TaskCompletionCountTierDefinition> getTaskCompletionCountTiers();

	CompletableFuture<Boolean> loadTaskTypeDataAsync();

	TaskTypeDefinition getTaskTypeDefinition();
}
