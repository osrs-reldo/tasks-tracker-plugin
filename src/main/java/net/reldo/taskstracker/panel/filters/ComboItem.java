package net.reldo.taskstracker.panel.filters;

public class ComboItem<T>
{
	private T value;
	private String label;

	public ComboItem(T value, String label)
	{
		this.value = value;
		this.label = label;
	}

	public T getValue()
	{
		return this.value;
	}

	public String getLabel()
	{
		return this.label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
