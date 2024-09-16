package net.reldo.taskstracker.data.jsondatastore.types;

import lombok.Data;

@Data
public class TaskTypePropertyDefinition
{
	public String key;
	public String name;
	public String filter; // nullable
}
