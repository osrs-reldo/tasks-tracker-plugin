package net.reldo.taskstracker.data.jsondatastore.types;

/**
 * Controls how multiple progress bars on a single task are displayed.
 */
public enum ProgressMode
{
	/**
	 * Each progress bar is displayed individually (default).
	 */
	SEPARATE,

	/**
	 * All progress bars are summed into a single bar showing total current / total target.
	 */
	SUM
}
