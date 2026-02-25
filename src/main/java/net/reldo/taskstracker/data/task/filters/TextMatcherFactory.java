package net.reldo.taskstracker.data.task.filters;

public class TextMatcherFactory
{
	/** Characters that trigger regex mode when auto-detection is enabled. */
	private static final String REGEX_METACHARACTERS = ".*+?[]{}()^$|\\";

	public static TextMatcher create(String pattern, boolean allowRegex)
	{
		if (pattern == null || pattern.isEmpty())
		{
			return new LiteralTextMatcher("");
		}

		if (allowRegex && containsRegexMetacharacter(pattern))
		{
			RegexTextMatcher regexMatcher = new RegexTextMatcher(pattern);
			if (regexMatcher.isValid())
			{
				return regexMatcher;
			}
			return new LiteralTextMatcher(pattern,
				"Invalid regex: " + regexMatcher.getErrorMessage() + ". Using literal search.");
		}

		return new LiteralTextMatcher(pattern);
	}

	private static boolean containsRegexMetacharacter(String pattern)
	{
		for (char c : pattern.toCharArray())
		{
			if (REGEX_METACHARACTERS.indexOf(c) != -1)
			{
				return true;
			}
		}
		return false;
	}

	public static TextMatcher createLiteral(String pattern)
	{
		return new LiteralTextMatcher(pattern);
	}

	public static TextMatcher createRegex(String pattern)
	{
		return new RegexTextMatcher(pattern);
	}
}
