package net.reldo.taskstracker.data.task;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import lombok.Getter;
import net.reldo.taskstracker.data.jsondatastore.types.FilterType;
import net.reldo.taskstracker.data.jsondatastore.types.TaskTypeDefinition;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;

public class TaskType
{
	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	private SpriteManager spriteManager;

	@Getter
	private final TaskTypeDefinition taskTypeDefinition;
	@Getter
	private final HashMap<Integer, BufferedImage> spritesById = new HashMap<>();
	@Getter
	private final HashMap<Integer, BufferedImage> tierSprites = new HashMap<>();

	@AssistedInject
	public TaskType(@Assisted TaskTypeDefinition taskTypeDefinition)
	{
		this.taskTypeDefinition = taskTypeDefinition;
	}

	public CompletableFuture<Boolean> loadTaskTypeDataAsync()
	{
		CompletableFuture<Boolean> future = new CompletableFuture<>();
		clientThread.invokeLater(() -> {
			getButtonFiltersSpriteIds().forEach((spriteId) -> {
				BufferedImage spriteImage = spriteManager.getSprite(spriteId, 0);
				spritesById.put(spriteId, spriteImage);
			});
			taskTypeDefinition.getTierSpriteIdMap().forEach((idKey, spriteId) -> {
				Integer tierId = Integer.parseInt(idKey);
				BufferedImage spriteImage = spriteManager.getSprite(spriteId, 0);
				this.tierSprites.put(tierId, spriteImage);
			});
			future.complete(true);
		});

		return future;
	}

	public String getConfigPrefix()
	{
		return taskTypeDefinition.getTaskJsonName() + ".";
	}

	private HashSet<Integer> getButtonFiltersSpriteIds()
	{
		HashSet<Integer> sprites = new HashSet<>();
		taskTypeDefinition.getFilters().stream().filter(
			(filterConfig) -> filterConfig.getFilterType().equals(FilterType.BUTTON_FILTER)
		).forEach((filterConfig) -> {
			if (filterConfig.getCustomItems() != null)
			{
				filterConfig.getCustomItems().forEach((customSprite) -> {
					Integer spriteId = customSprite.getSpriteId();
					if (spriteId == null)
					{
						return;
					}
					sprites.add(spriteId);
				});
			}
		});
		return sprites;
	}
}
