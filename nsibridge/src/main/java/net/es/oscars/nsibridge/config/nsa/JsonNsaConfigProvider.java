package net.es.oscars.nsibridge.config.nsa;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.es.oscars.nsibridge.config.JsonConfigProvider;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;

public class JsonNsaConfigProvider extends JsonConfigProvider implements NsaConfigProvider {
    private HashMap<String, NsaConfig> configs = new HashMap<String, NsaConfig>();


    public void loadConfig() throws Exception {

        File configFile = new File(this.getFilename());
        String json = FileUtils.readFileToString(configFile);
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, NsaConfig>>() {}.getType();

        configs = gson.fromJson(json, type);
    }

    public NsaConfig getConfig(String id) {
        return configs.get(id);
    }
}
