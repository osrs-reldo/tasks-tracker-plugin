package net.reldo.taskstracker.data.jsondatastore.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class FileDataStoreReader implements DataStoreReader
{
	@Override
	public InputStream readManifestData() throws Exception
	{
		String workingDirectory = System.getProperty("user.dir");
		log.debug("Reading manifest data from current working directory: " + workingDirectory);
		try {
			String filePath = "../task-json-store/manifest.json";
			File file = new File(filePath);
			if (!file.exists() || !file.isFile()) {
				throw new IOException("File not found or not a valid file: " + filePath);
			}
			return new FileInputStream(file);
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public InputStream readTaskTypes(String taskTypeFilename) throws Exception
	{
		String workingDirectory = System.getProperty("user.dir");
		log.debug(String.format("Reading task types %s data from current working directory: %s", taskTypeFilename, workingDirectory));
		try {
			String filePath = "../task-json-store/" + taskTypeFilename; // TODO: Maybe simplify the manifest so we don't need this filename
			File file = new File(filePath);
			if (!file.exists() || !file.isFile()) {
				throw new IOException("File not found or not a valid file: " + filePath);
			}
			return new FileInputStream(file);
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public InputStream readTasks(String jsonFilename) throws Exception
	{
		String workingDirectory = System.getProperty("user.dir");
		log.debug(String.format("Reading tasks %s data from current working directory: %s", jsonFilename, workingDirectory));
		try {
			String filePath = String.format("../task-json-store/tasks/%s.min.json", jsonFilename);
			File file = new File(filePath);
			if (!file.exists() || !file.isFile()) {
				throw new IOException("File not found or not a valid file: " + filePath);
			}
			return new FileInputStream(file);
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}
}
