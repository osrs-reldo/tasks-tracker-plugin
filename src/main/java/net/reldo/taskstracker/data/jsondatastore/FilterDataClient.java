package net.reldo.taskstracker.data.jsondatastore;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.reldo.taskstracker.data.jsondatastore.reader.DataStoreReader;
import net.reldo.taskstracker.data.jsondatastore.types.FilterConfig;
import okhttp3.OkHttpClient;

@Singleton
@Slf4j
public class FilterDataClient
{
    @Inject private ManifestClient manifestClient;
    @Inject	private OkHttpClient okHttpClient;
    @Inject private Gson gson;
    @Inject private DataStoreReader dataStoreReader;

    public FilterDataClient()
    {
        log.debug("init filter data client");
    }

    public HashMap<String, FilterConfig> getFilterConfigs() throws Exception
    {
        log.debug("get filter configs");
        InputStream stream = this.dataStoreReader.readFilterConfigs(this.manifestClient.getManifest().filterMetadata);
        InputStreamReader responseReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        Type listType = TypeToken.getParameterized(ArrayList.class, FilterConfig.class).getType();

        List<FilterConfig> filterConfigs = this.gson.fromJson(responseReader, listType);
        HashMap<String, FilterConfig> filterConfigsByConfigKey = new HashMap<>();
        for (FilterConfig filterConfig : filterConfigs)
        {
            filterConfigsByConfigKey.put(filterConfig.getConfigKey(), filterConfig);
        }
        responseReader.close();
        stream.close();
        return filterConfigsByConfigKey;
    }
}
