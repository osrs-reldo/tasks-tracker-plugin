package net.reldo.taskstracker.data.jsondatastore.types;

public enum ProgressType
{
	/** Progress is the player's experience in a skill versus a target XP value. */
	EXPERIENCE,

	/** Progress is the player's level in a skill versus a target level. */
	LEVEL,

	/** Progress is the raw value of a VarPlayer ID versus a target value. */
	VARP,

	/** Progress is the raw value of a Varbit ID versus a target value. */
	VARBIT
}
