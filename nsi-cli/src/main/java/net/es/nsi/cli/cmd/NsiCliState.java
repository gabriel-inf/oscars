package net.es.nsi.cli.cmd;

import net.es.nsi.cli.config.CliProviderProfile;
import net.es.nsi.cli.config.CliRequesterProfile;
import net.es.nsi.cli.config.DefaultProfiles;
import net.es.nsi.cli.config.ResvProfile;
import net.es.nsi.client.types.NsiCallbackHandler;

import java.util.HashMap;

public class NsiCliState {
    private static NsiCliState instance;
    public static NsiCliState getInstance() {
        if (instance == null) {
            instance = new NsiCliState();
        }
        return instance;
    }

    private NsiCliState(){};

    protected DefaultProfiles defs;


    protected ResvProfile resvProfile = null;
    protected CliProviderProfile provProfile = null;
    protected CliRequesterProfile requesterProfile = null;
    protected String connectionId = null;


    protected boolean listenerStarted = false;
    protected boolean listenerStartable = false;
    protected boolean nsiAvailable = false;
    protected boolean verbose = true;

    protected HashMap<String, NsiRequestState> states = new HashMap<String, NsiRequestState>();

    public NsiRequestState getState(String connectionId) {
        return states.get(connectionId);
    }
    public void setState(String connectionId, NsiRequestState state) {
        states.put(connectionId, state);
    }
    public NsiRequestState getNewState() {
        return new NsiRequestState();
    }


    public boolean isNsiAvailable() {
        return nsiAvailable;
    }

    public void setNsiAvailable(boolean nsiAvailable) {
        this.nsiAvailable = nsiAvailable;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public ResvProfile getResvProfile() {
        return resvProfile;
    }

    public void setResvProfile(ResvProfile resvProfile) {
        this.resvProfile = resvProfile;
    }

    public CliProviderProfile getProvProfile() {
        return provProfile;
    }

    public void setProvProfile(CliProviderProfile provProfile) {
        this.provProfile = provProfile;
    }

    public CliRequesterProfile getRequesterProfile() {
        return requesterProfile;
    }

    public void setRequesterProfile(CliRequesterProfile requesterProfile) {
        this.requesterProfile = requesterProfile;
    }

    public boolean isListenerStarted() {
        return listenerStarted;
    }

    public void setListenerStarted(boolean listenerStarted) {
        this.listenerStarted = listenerStarted;
    }

    public boolean isListenerStartable() {
        return listenerStartable;
    }

    public void setListenerStartable(boolean listenerStartable) {
        this.listenerStartable = listenerStartable;
    }

    public DefaultProfiles getDefs() {
        return defs;
    }

    public void setDefs(DefaultProfiles defs) {
        this.defs = defs;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
