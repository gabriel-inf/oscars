package net.es.oscars.nsibridge.bridge;

import net.es.oscars.nsibridge.beans.NSAInfo;


public interface NSAAPI extends ProviderAPI, RequesterAPI {
    public NSAInfo getNSAInfo();
    public boolean isWorking();
    
}
