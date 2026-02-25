package net.reldo.taskstracker.data.task.filters;

/**
 * Text matching strategy. All implementations are case-insensitive.
 */
public interface TextMatcher
{
	boolean matches(String text);

	String getDescription();

	/** Returns false if the pattern failed to compile (regex only). */
	boolean isValid();

	String getErrorMessage();
}
