package net.reldo.taskstracker.data.jsondatastore;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.data.jsondatastore.reader.DataStoreReader;
import net.reldo.taskstracker.data.jsondatastore.types.Manifest;
import okhttp3.OkHttpClient;

@Singleton
@Slf4j
public class ManifestClient
{
	@Inject	private OkHttpClient okHttpClient;
	@Inject private Gson gson;
	@Inject private DataStoreReader dataStoreReader;

	private static Manifest _manifest = null;

	public ManifestClient()
	{
		log.debug("init manifestclient");
	}

	public Manifest getManifest() throws Exception
	{
		if (_manifest != null) {
			return _manifest;
		}
		InputStream stream = this.dataStoreReader.readManifestData();
		InputStreamReader responseReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
		String manifestJson = CharStreams.toString(responseReader); // ew, why not a stream? not working...
		_manifest = this.gson.fromJson(manifestJson, Manifest.class);
		log.debug("_manifest = " + _manifest);
		return _manifest;
	}
}
