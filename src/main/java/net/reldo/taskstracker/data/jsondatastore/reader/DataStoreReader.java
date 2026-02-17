package net.reldo.taskstracker.data.jsondatastore.reader;

import java.io.InputStream;

public interface DataStoreReader
{
	InputStream readManifestData() throws Exception;

	InputStream readTaskTypes(String taskTypeFilename) throws Exception;

	InputStream readTasks(String jsonFilename) throws Exception;

	InputStream readFilterConfigs(String filterFilename) throws Exception;
}
