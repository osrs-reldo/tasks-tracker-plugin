package net.reldo.taskstracker.data.jsondatastore.types;

import java.util.HashMap;
import lombok.Data;

@Data
public class FilterDefinition
{
	// TODO: Check all if needed
	public String slug;
	public String name;
	public String controlType;
	public HashMap<String, String> options;
	public FilterValueDefinition[] values;
}
