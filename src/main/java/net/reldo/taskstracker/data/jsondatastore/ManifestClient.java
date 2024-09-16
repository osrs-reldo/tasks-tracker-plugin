package net.reldo.taskstracker.data.jsondatastore;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.data.jsondatastore.types.Manifest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Singleton
@Slf4j
public class ManifestClient
{
	@Inject	private OkHttpClient okHttpClient;
	@Inject private Gson gson;

	private Manifest _manifest = null;

	public ManifestClient()
	{
		System.out.println("init manifestclient");
	}

	public Manifest getManifest() throws Exception
	{
		if (this._manifest != null) {
			return this._manifest;
		}
		String manifestUrl = JsonDataStore.baseUrl + "/manifest.json";
		log.debug("getManifest json from {} ...", manifestUrl);
		Request request = new Request.Builder()
			.url(manifestUrl)
			.build();
		Response response = this.okHttpClient.newCall(request).execute();
		if (!response.isSuccessful())
		{
			String unsuccessful = "getManifest json request unsuccessful with status " + response.code();
			if (response.body() != null)
			{
				unsuccessful += " and body \n" + response.body();
			}
			log.error(unsuccessful);
			throw new Exception(unsuccessful);
		}
		if (response.body() == null)
		{
			log.error("getManifest returned without body");
			throw new Exception("getManifest returned without body");
		}
		log.debug("getManifest json fetched successfully, deserializing result");
		InputStream stream = response.body().byteStream();
		InputStreamReader responseReader = new InputStreamReader(stream, StandardCharsets.UTF_8);

		Manifest manifest = this.gson.fromJson(responseReader, Manifest.class);
		this._manifest = manifest;
		return manifest;
	}
}
