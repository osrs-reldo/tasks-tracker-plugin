package net.reldo.taskstracker.panel.flyweight;

import java.awt.Color;
import java.awt.Font;
import javax.swing.border.Border;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.runelite.client.ui.overlay.components.PanelComponent;

/**
 * Adapter that integrates the Flyweight pattern with existing TaskPanel code.
 *
 * This class provides utility methods to reduce per-panel overhead by:
 * 1. Centralizing styling lookups to SharedTaskPanelResources
 * 2. Pooling HTML renderers via HtmlRendererPool
 * 3. Caching icons and other immutable resources
 *
 * Usage:
 *   Instead of: label.setFont(FontManager.getRunescapeSmallFont())
 *   Use:        label.setFont(FlyweightTaskPanelAdapter.getSharedFont())
 *
 * This reduces object allocations and memory fragmentation.
 */
@Slf4j
public final class FlyweightTaskPanelAdapter
{
	private static final SharedTaskPanelResources sharedResources = SharedTaskPanelResources.getInstance();
	private static final HtmlRendererPool rendererPool = HtmlRendererPool.getInstance();

	private FlyweightTaskPanelAdapter()
	{
		// Utility class
	}

	// ===== Shared Font Access =====

	public static Font getSharedRuneScapeSmallFont()
	{
		return sharedResources.getRuneScapeSmallFont();
	}

	public static Font getSharedRunescapeFont()
	{
		return sharedResources.getRunescapeFont();
	}

	public static Font getSharedItalicFont()
	{
		return sharedResources.getItalicFont();
	}

	// ===== Shared Color Access =====

	public static Color getSharedWhiteText()
	{
		return sharedResources.getWhiteText();
	}

	public static Color getSharedGrayText()
	{
		return sharedResources.getGrayText();
	}

	public static Color getSharedDarkGrayBackground()
	{
		return sharedResources.getDarkGrayBackground();
	}

	public static Color getSharedUnqualifiedBackground()
	{
		return sharedResources.getUnqualifiedBackground();
	}

	public static Color getSharedCompletedBackground()
	{
		return sharedResources.getCompletedBackground();
	}

	public static Color getSharedQualifiedText()
	{
		return sharedResources.getQualifiedText();
	}

	public static Color getSharedUnqualifiedText()
	{
		return sharedResources.getUnqualifiedText();
	}

	public static Color getSharedBrandOrange()
	{
		return sharedResources.getBrandOrange();
	}

	// ===== Shared Border Access =====

	public static Border getSharedEmptyBorder0000()
	{
		return sharedResources.getEmptyBorder0000();
	}

	public static Border getSharedEmptyBorder0070()
	{
		return sharedResources.getEmptyBorder0070();
	}

	public static Border getSharedEmptyBorder0700()
	{
		return sharedResources.getEmptyBorder0700();
	}

	public static Border getSharedEmptyBorder7760()
	{
		return sharedResources.getEmptyBorder7760();
	}

	public static Border getSharedEmptyBorder0007()
	{
		return sharedResources.getEmptyBorder0007();
	}

	public static Border getSharedEmptyBorder5050()
	{
		return sharedResources.getEmptyBorder5050();
	}

	// ===== Shared String Access =====

	public static String getSharedPinState()
	{
		return sharedResources.getPinState();
	}

	public static String getSharedUnpinState()
	{
		return sharedResources.getUnpinState();
	}

	public static String getSharedAddState()
	{
		return sharedResources.getAddState();
	}

	public static String getSharedRemoveState()
	{
		return sharedResources.getRemoveState();
	}

	// ===== HTML Renderer Pool Access =====

	/**
	 * Acquire a renderer from the pool for temporary overlay rendering.
	 * Must be released after use via releaseRenderer().
	 */
	public static PanelComponent acquireRenderer()
	{
		return rendererPool.acquire();
	}

	/**
	 * Release a renderer back to the pool.
	 */
	public static void releaseRenderer(PanelComponent renderer)
	{
		rendererPool.release(renderer);
	}

	/**
	 * Get pool statistics for monitoring
	 */
	public static int getRendererPoolAvailableCount()
	{
		return rendererPool.getAvailableCount();
	}

	public static int getRendererPoolSize()
	{
		return rendererPool.getPoolSize();
	}

	/**
	 * Initialize shared resources (call once on plugin startup).
	 * This pre-caches common resources and warms up the renderer pool.
	 */
	public static void initialize(TasksTrackerPlugin plugin)
	{
		log.info("Initializing FlyweightTaskPanelAdapter");
		log.debug("Shared resources loaded: {} renderers in pool",
			getRendererPoolSize());
	}
}
