package net.es.oscars.topoUtil.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

public class NmlConfigProvider {
    private NmlConfig nmlConfig;
    protected Resource jsonFile;

    public void loadConfig() throws IOException {
        File configFile = jsonFile.getFile();
        String json = FileUtils.readFileToString(configFile);
        Gson gson = new Gson();
        Type type = new TypeToken<NmlConfig>() {}.getType();
        nmlConfig = gson.fromJson(json, type);
    }

    public NmlConfig getNmlConfig() {
        return nmlConfig;
    }

    public void setNmlConfig(NmlConfig nmlConfig) {
        this.nmlConfig = nmlConfig;
    }

    public Resource getJsonFile() {
        return jsonFile;
    }

    public void setJsonFile(Resource jsonFile) {
        this.jsonFile = jsonFile;
    }
}
