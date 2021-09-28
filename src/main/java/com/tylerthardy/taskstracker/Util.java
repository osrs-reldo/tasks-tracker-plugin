package com.tylerthardy.taskstracker;

public class Util
{
	public static String HTML_LINE_BREAK = "<br>";

	public static String wrapWithHtml(String text) {
		return "<html>" + text + "</html>";
	}

	public static String wrapWithWrappingParagraph(String text, int width) {
		return "<p width=\"" + width + "\">" + text + "</p>";
	}

	public static String wrapWithBold(String text) {
		return "<b>" + text + "</b>";
	}
}
