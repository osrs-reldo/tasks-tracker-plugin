package net.reldo.taskstracker.data.jsondatastore.types;

import lombok.Data;

@Data
public class PremadeRouteEntry
{
	public String filename;
	public String name;
	public String author;
	public String description;
	public String taskType;
}
