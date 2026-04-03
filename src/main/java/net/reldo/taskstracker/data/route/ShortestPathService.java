package net.reldo.taskstracker.data.route;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerConfig;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.PluginMessage;

@Slf4j
@Singleton
public class ShortestPathService
{
	@Inject
	private EventBus eventBus;
	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	private TasksTrackerConfig config;

	private final AtomicReference<WorldPoint> currentGpsTarget = new AtomicReference<>(null);

	/**
	 * Updates the GPS target to the given location, or clears if null.
	 * Skips redundant updates if the target hasn't changed.
	 */
	public void setGpsTarget(WorldPoint target)
	{
		if (!config.useShortestPath() || !config.showOverlay())
		{
			clearGps();
			return;
		}

		if (target == null)
		{
			clearGps();
			return;
		}

		if (target.equals(currentGpsTarget.get()))
		{
			return;
		}

		currentGpsTarget.set(target);
		requestPath(target);
	}

	public void clearGps()
	{
		currentGpsTarget.set(null);
		eventBus.post(new PluginMessage("shortestpath", "clear"));
	}

	private void requestPath(WorldPoint target)
	{
		clientThread.invokeLater(() ->
		{
			if (client.getLocalPlayer() == null)
			{
				return;
			}

			WorldPoint playerWp = client.getLocalPlayer().getWorldLocation();
			Map<String, Object> data = new HashMap<>();
			data.put("start", playerWp);
			data.put("target", target);
			eventBus.post(new PluginMessage("shortestpath", "path", data));
		});
	}
}
