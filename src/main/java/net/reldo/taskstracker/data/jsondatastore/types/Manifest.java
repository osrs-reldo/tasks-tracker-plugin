package net.reldo.taskstracker.data.jsondatastore.types;

import lombok.Data;

@Data
public class Manifest
{
	public String taskTypeMetadata;
	public String filterMetadata;
	public int[] diaryVarbits;
	public int[] additionalVarbits;
}
