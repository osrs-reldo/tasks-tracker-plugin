package net.reldo.taskstracker.panel.flyweight;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.overlay.components.PanelComponent;

/**
 * Flyweight: Object pool for HTML renderers (PanelComponent).
 * Reduces memory footprint by sharing a small pool of renderer instances
 * across ~2000 task panels instead of allocating 4000+ renderer instances.
 *
 * Implements a simple object pool pattern with thread-safe acquire/release.
 * Pool size is configurable but typically 2-4 renderers are sufficient since
 * only one or two panels render simultaneously.
 *
 * Thread-safe.
 */
@Slf4j
public final class HtmlRendererPool
{
	private static final int DEFAULT_POOL_SIZE = 4;
	private static final HtmlRendererPool INSTANCE = new HtmlRendererPool(DEFAULT_POOL_SIZE);

	private final Deque<PanelComponent> availableRenderers;
	private final int poolSize;
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	private HtmlRendererPool(int poolSize)
	{
		this.poolSize = poolSize;
		this.availableRenderers = new ArrayDeque<>(poolSize);

		// Pre-allocate pool
		for (int i = 0; i < poolSize; i++)
		{
			availableRenderers.addLast(new PanelComponent());
		}

		log.debug("HtmlRendererPool initialized with {} renderers", poolSize);
	}

	public static HtmlRendererPool getInstance()
	{
		return INSTANCE;
	}

	/**
	 * Acquire a renderer from the pool. If none available, creates one.
	 * Caller is responsible for calling release() when done.
	 */
	public PanelComponent acquire()
	{
		lock.writeLock().lock();
		try
		{
			PanelComponent renderer = availableRenderers.pollFirst();
			if (renderer == null)
			{
				log.warn("Renderer pool depleted, creating additional renderer beyond pool size");
				renderer = new PanelComponent();
			}
			else
			{
				renderer.getChildren().clear(); // Reset for reuse
			}
			return renderer;
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * Release a renderer back to the pool for reuse.
	 */
	public void release(PanelComponent renderer)
	{
		if (renderer == null)
		{
			return;
		}

		lock.writeLock().lock();
		try
		{
			if (availableRenderers.size() < poolSize)
			{
				renderer.getChildren().clear(); // Clean before returning to pool
				availableRenderers.addLast(renderer);
			}
			// Otherwise discard (exceeds pool size)
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * Get current pool statistics (for debugging/monitoring)
	 */
	public int getAvailableCount()
	{
		lock.readLock().lock();
		try
		{
			return availableRenderers.size();
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	public int getPoolSize()
	{
		return poolSize;
	}
}
