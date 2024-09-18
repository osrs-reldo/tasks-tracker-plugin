package net.reldo.taskstracker.data.jsondatastore.jsonreader;

import java.io.InputStream;

public interface DataStoreReader
{
	InputStream readManifestData() throws Exception;
}
