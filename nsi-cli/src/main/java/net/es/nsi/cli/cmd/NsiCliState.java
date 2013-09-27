package net.es.nsi.cli.cmd;

import net.es.nsi.cli.config.DefaultProfiles;
import net.es.nsi.cli.config.RequesterProfile;
import net.es.nsi.cli.config.ProviderProfile;
import net.es.nsi.cli.config.ResvProfile;

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
    protected ProviderProfile provProfile = null;
    protected RequesterProfile requesterProfile = null;
    protected String connectionId = null;
    protected boolean listenerStarted = false;
    protected boolean listenerStartable = false;
    protected boolean nsiAvailable = false;
    protected boolean verbose = true;

    protected HashMap<String, Boolean> nsiConfirmed = new HashMap<String, Boolean>();
    protected HashMap<String, Boolean> nsiCommitted  = new HashMap<String, Boolean>();
    protected HashMap<String, Boolean> nsiProvisioned = new HashMap<String, Boolean>();

    public void setConfirmed(String connectionId, boolean confirmed) {
        nsiConfirmed.put(connectionId, confirmed);
    }

    public void setCommitted(String connectionId, boolean confirmed) {
        nsiCommitted.put(connectionId, confirmed);
    }
    public void setProvisioned(String connectionId, boolean confirmed) {
        nsiProvisioned.put(connectionId, confirmed);
    }

    public boolean isConfirmed(String connectionId) {
        if (!nsiConfirmed.containsKey(connectionId)) return false;
        return nsiConfirmed.get(connectionId);
    }

    public boolean isCommitted(String connectionId) {
        if (!nsiCommitted.containsKey(connectionId)) return false;
        return nsiCommitted.get(connectionId);
    }

    public boolean isProvisioned(String connectionId) {
        if (!nsiProvisioned.containsKey(connectionId)) return false;
        return nsiProvisioned.get(connectionId);
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

    public ProviderProfile getProvProfile() {
        return provProfile;
    }

    public void setProvProfile(ProviderProfile provProfile) {
        this.provProfile = provProfile;
    }

    public RequesterProfile getRequesterProfile() {
        return requesterProfile;
    }

    public void setRequesterProfile(RequesterProfile requesterProfile) {
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
