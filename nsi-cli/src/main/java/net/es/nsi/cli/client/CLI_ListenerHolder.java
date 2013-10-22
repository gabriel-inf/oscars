package net.es.nsi.cli.client;


import java.util.HashMap;

public class CLI_ListenerHolder {
    private static CLI_ListenerHolder instance;
    public static CLI_ListenerHolder getInstance() {
        if (instance == null) instance = new CLI_ListenerHolder();
        return instance;
    }
    private CLI_ListenerHolder() {

    }



    private HashMap<String, CLIListener> listeners = new HashMap<String, CLIListener>();

    public HashMap<String, CLIListener> getListeners() {
        return listeners;
    }

    public void setListeners(HashMap<String, CLIListener> listeners) {
        this.listeners = listeners;
    }
}
