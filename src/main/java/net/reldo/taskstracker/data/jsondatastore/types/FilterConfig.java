package net.reldo.taskstracker.data.jsondatastore.types;

import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents the configuration for a filter
 */
@Data
@AllArgsConstructor
public class FilterConfig
{
	/**
	 * Key under which to store the filter's selected values, generally prefixed by
	 * task type
	 */
	private String configKey;

	/**
	 * The label displayed in the UI with the filter.
	 */
	private String label;

	/**
	 * The filter type, see enum for types of filters supported.
	 */
	private FilterType filterType;

	/**
	 * The source of the value(s) to use for the filter, see enum for types of
	 * values supported. If global is specified then configKey must match a filter
	 * config defined in filters.json
	 */
	private FilterValueType valueType;

	/**
	 * The name of the param or metadata property to use for the filter. Can be left
	 * null for SKILL value type
	 */
	private String valueName;

	/**
	 * Name of an enum specified in `TaskTypeDefinition.stringEnumMap` to provide
	 * labels for the filter Specifying this property will override the displayed
	 * integer value of `valueName`
	 */
	private String optionLabelEnum;

	/**
	 * Item values in a button filter (dropdown not yet supported)
	 */
	private ArrayList<FilterCustomItem> customItems;

}
