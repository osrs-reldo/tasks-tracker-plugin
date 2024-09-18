package net.reldo.taskstracker.data.jsondatastore.jsonreader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

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
}
