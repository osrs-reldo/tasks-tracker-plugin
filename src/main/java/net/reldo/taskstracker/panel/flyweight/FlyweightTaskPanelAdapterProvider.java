package net.reldo.taskstracker.panel.flyweight;

import com.google.inject.Provider;
import lombok.extern.slf4j.Slf4j;

/**
 * Guice Provider for FlyweightTaskPanelAdapter.
 * Ensures the adapter's shared resources and renderer pool are initialized once at startup.
 */
@Slf4j
public class FlyweightTaskPanelAdapterProvider implements Provider<FlyweightTaskPanelAdapter>
{
	private static final FlyweightTaskPanelAdapter INSTANCE;

	static
	{
		log.info("Initializing Flyweight TaskPanel Adapter (Shared Resources + Renderer Pool)");
		INSTANCE = new FlyweightTaskPanelAdapter();
		log.debug("Flyweight adapter initialized: {} renderers in pool", 
			INSTANCE.getRendererPoolSize());
	}

	@Override
	public FlyweightTaskPanelAdapter get()
	{
		return INSTANCE;
	}
}
