package net.es.oscars.topoUtil.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.es.oscars.topoUtil.beans.spec.NetworkSpec;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

public class OscarsConfigProvider {
    private OscarsConfig oscarsConfig;
    protected Resource jsonFile;

    public void loadConfig() throws IOException {
        File configFile = jsonFile.getFile();
        String json = FileUtils.readFileToString(configFile);
        Gson gson = new Gson();
        Type type = new TypeToken<OscarsConfig>() {}.getType();
        oscarsConfig = gson.fromJson(json, type);
    }

    public OscarsConfig getOscarsConfig() {
        return oscarsConfig;
    }

    public void setOscarsConfig(OscarsConfig oscarsConfig) {
        this.oscarsConfig = oscarsConfig;
    }

    public Resource getJsonFile() {
        return jsonFile;
    }

    public void setJsonFile(Resource jsonFile) {
        this.jsonFile = jsonFile;
    }
}
