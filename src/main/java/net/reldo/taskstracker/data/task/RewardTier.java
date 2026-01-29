package net.reldo.taskstracker.data.task;

import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;

/**
 * Combat Achievement reward tiers with their point thresholds.
 * Can be expanded to include leagues tiers once known
 *
 * Thresholds can be loaded from game varbits via {@link #initializeThresholds(Client)}.
 * Falls back to hardcoded values if varbits haven't been loaded or return invalid data.
 */
@Slf4j
@Getter
public enum RewardTier
{
	NONE("None", 0, -1),
	EASY("Easy", 38, 4132),
	MEDIUM("Medium", 148, 10660),
	HARD("Hard", 394, 10661),
	ELITE("Elite", 1038, 14812),
	MASTER("Master", 1878, 14813),
	GRANDMASTER("Grandmaster", 2604, 14814);

	private final String displayName;
	private final int defaultPointsRequired;  // Hardcoded fallback
	private final int varbitId;               // Varbit to read threshold from game

	/**
	 * Cached thresholds loaded from game varbits.
	 * If null, fallbacks are used.
	 */
	private static Map<RewardTier, Integer> cachedThresholds = null;

	RewardTier(String displayName, int defaultPointsRequired, int varbitId)
	{
		this.displayName = displayName;
		this.defaultPointsRequired = defaultPointsRequired;
		this.varbitId = varbitId;
	}

	/**
	 * Initializes tier thresholds by reading from game varbits.
	 * Should be called on the client thread when logged in.
	 *
	 * @param client The RuneLite client
	 */
	public static void initializeThresholds(Client client)
	{
		Map<RewardTier, Integer> thresholds = new EnumMap<>(RewardTier.class);

		for (RewardTier tier : values())
		{
			if (tier == NONE || tier.varbitId < 0)
			{
				thresholds.put(tier, tier.defaultPointsRequired);
				continue;
			}

			try
			{
				int value = client.getVarbitValue(tier.varbitId);
				if (value > 0)
				{
					thresholds.put(tier, value);
					log.debug("Loaded {} tier threshold: {} from varbit {}", tier.displayName, value, tier.varbitId);
				}
				else
				{
					// Varbit returned 0 or negative, use default
					thresholds.put(tier, tier.defaultPointsRequired);
					log.debug("Varbit {} returned {}, using default {} for {}", tier.varbitId, value, tier.defaultPointsRequired, tier.displayName);
				}
			}
			catch (Exception e)
			{
				thresholds.put(tier, tier.defaultPointsRequired);
				log.warn("Failed to read varbit {} for {} tier, using default: {}", tier.varbitId, tier.displayName, e.getMessage());
			}
		}

		cachedThresholds = thresholds;
		log.debug("Initialized tier thresholds: {}", cachedThresholds);
	}

	/**
	 * Clears cached thresholds. Call on logout or when switching profiles.
	 */
	public static void clearThresholds()
	{
		cachedThresholds = null;
		log.debug("Cleared tier thresholds cache");
	}

	/**
	 * Gets the points required for this tier.
	 * Uses cached game data if available, otherwise returns hardcoded default.
	 *
	 * @return Points required to unlock this tier
	 */
	public int getPointsRequired()
	{
		if (cachedThresholds != null && cachedThresholds.containsKey(this))
		{
			return cachedThresholds.get(this);
		}
		return defaultPointsRequired;
	}

	/**
	 * Gets the current reward tier based on total completed points.
	 *
	 * @param totalPoints The player's total completed points
	 * @return The highest tier achieved
	 */
	public static RewardTier getTierForPoints(int totalPoints)
	{
		RewardTier result = NONE;
		for (RewardTier tier : values())
		{
			if (totalPoints >= tier.getPointsRequired())
			{
				result = tier;
			}
			else
			{
				break;
			}
		}
		return result;
	}

	/**
	 * Gets the next reward tier after this one.
	 *
	 * @return The next tier, or null if at GRANDMASTER
	 */
	@Nullable
	public RewardTier getNextTier()
	{
		int nextOrdinal = this.ordinal() + 1;
		if (nextOrdinal >= values().length)
		{
			return null;
		}
		return values()[nextOrdinal];
	}

	/**
	 * Gets the points needed to reach the next tier.
	 *
	 * @param currentPoints The player's current total points
	 * @return Points needed for next tier, or 0 if at GRANDMASTER
	 */
	public int getPointsToNextTier(int currentPoints)
	{
		RewardTier next = getNextTier();
		if (next == null)
		{
			return 0;
		}
		return next.getPointsRequired() - currentPoints;
	}

	/**
	 * Checks if thresholds have been loaded from the game.
	 *
	 * @return true if thresholds are cached, false if using defaults
	 */
	public static boolean isInitialized()
	{
		return cachedThresholds != null;
	}
}
