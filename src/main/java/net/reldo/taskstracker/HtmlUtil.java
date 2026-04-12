package net.reldo.taskstracker;

import java.awt.Color;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HtmlUtil
{
	public static String HTML_LINE_BREAK = "<br>";
	public static String HORIZONTAL_RULE = "<hr />";

	public static String wrapWithHtml(String text)
	{
		return "<html>" + text + "</html>";
	}

	public static String wrapWithWrappingHeading(String text, int width)
	{
		return "<h1 width=\"" + width + "\">" + text + "</h1>";
	}

	public static String wrapWithWrappingSubHeading(String text, int width)
	{
		return "<h2 width=\"" + width + "\">" + text + "</h2>";
	}

	public static String wrapWithWrappingParagraph(String text, int width)
	{
		return "<p width=\"" + width + "\">" + text + "</p>";
	}

	public static String wrapWithUnorderedList(String text)
	{
		return "<ul style=\"margin-left: 25px;\">" + text + "</ul>";
	}

	public static String wrapWithOrderedList(String text)
	{
		return "<ol style=\"margin-left: 25px;\">" + text + "</ol>";
	}

	public static String wrapWithListItem(String text, int width)
	{
		return "<li style=\"margin-bottom: 5px;width: " + width + ";\">" + text + "</li>";
	}

	public static String wrapWithItalics(String text)
	{
		return "<i>" + text + "</i>";
	}

	public static String wrapWithBold(String text)
	{
		return "<b>" + text + "</b>";
	}

	public static String imageTag(URL url)
	{
		return "<img src=\"" + url + "\">";
	}

	public static String colorTag(String color, String text)
	{
		return "<span style=\"color: " + color + "\">" + text + "</span>";
	}

	public static String colorTag(Color color, String text)
	{
		String buf = Integer.toHexString(color.getRGB());
		String hex = "#" + buf.substring(buf.length() - 6);
		return colorTag(hex, text);
	}
}
