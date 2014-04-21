package net.es.nsi.client.util;


import java.util.HashMap;

public class ListenerHolder {
    private static ListenerHolder instance;
    public static ListenerHolder getInstance() {
        if (instance == null) instance = new ListenerHolder();
        return instance;
    }
    private ListenerHolder() {

    }



    private HashMap<String, NsiRequesterPortListener> listeners = new HashMap<String, NsiRequesterPortListener>();

    public HashMap<String, NsiRequesterPortListener> getListeners() {
        return listeners;
    }

    public void setListeners(HashMap<String, NsiRequesterPortListener> listeners) {
        this.listeners = listeners;
    }
}
