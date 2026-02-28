package net.reldo.taskstracker.data.task.filters;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexTextMatcher implements TextMatcher
{
	private final String patternString;
	private final Pattern pattern;
	private final String errorMessage;

	public RegexTextMatcher(String patternString)
	{
		this.patternString = patternString != null ? patternString : "";
		Pattern compiled = null;
		String error = null;

		if (!this.patternString.isEmpty())
		{
			try
			{
				compiled = Pattern.compile(this.patternString, Pattern.CASE_INSENSITIVE);
			}
			catch (PatternSyntaxException e)
			{
				error = e.getDescription();
			}
		}

		this.pattern = compiled;
		this.errorMessage = error;
	}

	@Override
	public boolean matches(String text)
	{
		if (!isValid())
		{
			return false;
		}
		if (patternString.isEmpty())
		{
			return true;
		}
		return pattern.matcher(text).find();
	}

	@Override
	public String getDescription()
	{
		return "Regex: /" + patternString + "/i";
	}

	@Override
	public boolean isValid()
	{
		return patternString.isEmpty() || pattern != null;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}
}
