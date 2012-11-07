package net.es.oscars.nsibridge.beans.config;

import java.util.ArrayList;
import java.util.List;

public class JettyServiceConfig {
    private String path;
    private String implementor;
    private List<String> inInterceptors = new ArrayList<String>();
    public JettyServiceConfig() {

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getImplementor() {
        return implementor;
    }

    public void setImplementor(String implementor) {
        this.implementor = implementor;
    }


    public List<String> getInInterceptors() {
        return inInterceptors;
    }

    public void setInInterceptors(List<String> inInterceptors) {
        this.inInterceptors = inInterceptors;
    }
}
