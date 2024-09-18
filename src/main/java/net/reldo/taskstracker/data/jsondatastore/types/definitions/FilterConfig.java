package net.reldo.taskstracker.data.jsondatastore.types.definitions;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents the configuration for a filter
 */
@Data
@AllArgsConstructor
public class FilterConfig {

	/**
	 * The filter type key, see the types of filters supported for key.
	 */
	private String key;

	/**
	 * The source of the value(s) to use for the filter.
	 * Possible values: "PARAM", "SKILL", "METADATA".
	 * TODO: Use an enum here instead
	 */
	private String valueType;

	/**
	 * The name of the param or metadata property to use for the filter.
	 * Can be left null for SKILL value type
	 */
	private String valueName;
}
