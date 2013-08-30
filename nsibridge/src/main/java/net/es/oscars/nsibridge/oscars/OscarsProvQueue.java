package net.es.oscars.nsibridge.oscars;

import java.util.HashMap;

public class OscarsProvQueue {
    private static OscarsProvQueue ourInstance = new OscarsProvQueue();
    protected HashMap<String, String> inspect = new HashMap<String, String>();

    public static OscarsProvQueue getInstance() {
        return ourInstance;
    }

    private OscarsProvQueue() {
    }

    public HashMap<String, String> getInspect() {
        return inspect;
    }
}
