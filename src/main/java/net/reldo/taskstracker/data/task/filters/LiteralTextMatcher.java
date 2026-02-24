package net.reldo.taskstracker.data.task.filters;

public class LiteralTextMatcher implements TextMatcher
{
	private final String pattern;

	public LiteralTextMatcher(String pattern)
	{
		this.pattern = pattern != null ? pattern.toLowerCase() : "";
	}

	@Override
	public boolean matches(String text)
	{
		if (pattern.isEmpty())
		{
			return true;
		}
		return text.toLowerCase().contains(pattern);
	}

	@Override
	public String getDescription()
	{
		return "Literal: \"" + pattern + "\"";
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@Override
	public String getErrorMessage()
	{
		return null;
	}
}
