package net.es.oscars.nsibridge.oscars;

import java.util.HashMap;

public class OscarsProvQueue {
    private static OscarsProvQueue ourInstance = new OscarsProvQueue();
    protected HashMap<String, OscarsOps> inspect = new HashMap<String, OscarsOps>();

    public static OscarsProvQueue getInstance() {
        return ourInstance;
    }

    private OscarsProvQueue() {
    }

    public HashMap<String, OscarsOps> getInspect() {
        return inspect;
    }
}
