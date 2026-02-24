package net.reldo.taskstracker.data.task.filters;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
			else
			{
				log.warn("Invalid regex pattern '{}': {}. Falling back to literal search.",
					pattern, regexMatcher.getErrorMessage());
				return new LiteralTextMatcher(pattern);
			}
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
