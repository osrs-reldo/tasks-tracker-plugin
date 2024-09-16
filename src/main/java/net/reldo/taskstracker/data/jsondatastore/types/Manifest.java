package net.reldo.taskstracker.data.jsondatastore.types;

import lombok.Data;

@Data
public class Manifest
{
	public String taskTypeMetadataFilename;
	public String filterMetadataFilename;
	public int[] diaryVarbits;
	public int[] additionalVarbits;
}
