package net.reldo.taskstracker.data.task;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import net.reldo.taskstracker.data.jsondatastore.types.FilterConfig;
import net.reldo.taskstracker.data.jsondatastore.types.FilterType;
import net.reldo.taskstracker.data.jsondatastore.types.TaskTypeDefinition;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;

public class TaskType
{
	@Getter
	private final HashMap<Integer, BufferedImage> spritesById = new HashMap<>();
	@Getter
	private final HashMap<Integer, BufferedImage> tierSprites = new HashMap<>();
	@Getter
	private final HashMap<Integer, Integer> tierPoints = new HashMap<>();

	private final Client client;
	private final ClientThread clientThread;
	private final SpriteManager spriteManager;
	private final TaskTypeDefinition definition;

	public TaskType(Client client, ClientThread clientThread, SpriteManager spriteManager,
		TaskTypeDefinition taskTypeDefinition)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.spriteManager = spriteManager;
		this.definition = taskTypeDefinition;
	}

	public CompletableFuture<Boolean> loadTaskTypeDataAsync()
	{
		CompletableFuture<Boolean> future = new CompletableFuture<>();
		clientThread.invoke(() -> {
			try {
				getButtonFiltersSpriteIds().forEach((spriteId) -> {
					BufferedImage spriteImage = spriteManager.getSprite(spriteId, 0);
					spritesById.put(spriteId, spriteImage);
				});
				definition.getTierSpriteIdMap().forEach((idKey, spriteId) -> {
					Integer tierId = Integer.parseInt(idKey);
					BufferedImage spriteImage = spriteManager.getSprite(spriteId, 0);
					tierSprites.put(tierId, spriteImage);
				});
				if (definition.getIntEnumMap().containsKey("tierPoints")) {
					int enumId = definition.getIntEnumMap().get("tierPoints");
					EnumComposition enumComposition = client.getEnum(enumId);
					int[] keys = enumComposition.getKeys();
					for (int key : keys) {
						tierPoints.put(key, enumComposition.getIntValue(key));
					}
				}
				future.complete(true);
			}
			catch (Exception e) {
				future.completeExceptionally(e);
			}
		});

		return future;
	}

	public String getFilterConfigPrefix()
	{
		return definition.getTaskJsonName() + ".";
	}

	private HashSet<Integer> getButtonFiltersSpriteIds()
	{
		HashSet<Integer> sprites = new HashSet<>();
		definition.getFilters().stream()
			.filter((filterConfig) -> filterConfig.getFilterType().equals(FilterType.BUTTON_FILTER))
			.forEach((filterConfig) -> {
				if (filterConfig.getCustomItems() != null) {
					filterConfig.getCustomItems().forEach((customSprite) -> {
						Integer spriteId = customSprite.getSpriteId();
						if (spriteId == null) {
							return;
						}
						sprites.add(spriteId);
					});
				}
			});
		return sprites;
	}

	public ArrayList<Integer> getTaskVarps()
	{
		return definition.getTaskVarps();
	}

	public String getTaskJsonName()
	{
		return definition.getTaskJsonName();
	}

	public HashMap<String, Integer> getIntParamMap()
	{
		return definition.getIntParamMap();
	}

	public HashMap<String, Integer> getStringParamMap()
	{
		return definition.getStringParamMap();
	}

	public HashMap<String, Integer> getStringEnumMap()
	{
		return definition.getStringEnumMap();
	}

	public String getName()
	{
		return definition.getName();
	}

	public ArrayList<FilterConfig> getFilters()
	{
		return definition.getFilters();
	}

	public int[] getOtherVarps()
	{
		return definition.getOtherVarps();
	}

	public int[] getVarbits()
	{
		return definition.getVarbits();
	}

	public int getTaskCompletedScriptId()
	{
		return definition.getTaskCompletedScriptId();
	}
}
