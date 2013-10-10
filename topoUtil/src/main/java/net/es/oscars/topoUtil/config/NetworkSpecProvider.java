package net.es.oscars.topoUtil.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.es.oscars.topoUtil.beans.spec.NetworkSpec;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

public class NetworkSpecProvider {
    private NetworkSpec networkSpec;
    protected Resource jsonFile;

    public void loadConfig() throws IOException {
        File configFile = jsonFile.getFile();
        String json = FileUtils.readFileToString(configFile);
        Gson gson = new Gson();
        Type type = new TypeToken<NetworkSpec>() {}.getType();
        networkSpec = gson.fromJson(json, type);
    }

    public NetworkSpec getNetworkSpec() {
        return networkSpec;
    }

    public void setNetworkSpec(NetworkSpec networkSpec) {
        this.networkSpec = networkSpec;
    }

    public Resource getJsonFile() {
        return jsonFile;
    }

    public void setJsonFile(Resource jsonFile) {
        this.jsonFile = jsonFile;
    }
}

