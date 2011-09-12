package net.es.oscars.nsibridge.util;

import java.util.Collection;
import java.util.HashMap;

import net.es.oscars.nsibridge.beans.NSAInfo;

public class NSAList {
    private static  NSAList instance;
    private HashMap<String, NSAInfo> nsasByAbbrv;
    private HashMap<String, NSAInfo> nsasByNSAId;
    
    private NSAList() { 
        nsasByAbbrv = new HashMap<String, NSAInfo>();
        nsasByNSAId = new HashMap<String, NSAInfo>();
    }
        
    public static  NSAList getInstance() {
        if (instance == null) {
            instance = new NSAList();
        }
        return instance;
    }
    public void addNSA(String abbrv, NSAInfo info) {
        nsasByAbbrv.put(abbrv, info);
        nsasByNSAId.put(info.getNsaId(), info);
    }
    public NSAInfo getNSAByAbbrv(String abbrv) {
        return (nsasByAbbrv.get(abbrv));
    }
    public NSAInfo getNSAByNSAId(String nsaId) {
        return (nsasByNSAId.get(nsaId));
    }
    
    public Collection<NSAInfo> getNSAs() {
        return nsasByNSAId.values();
    }
    
}
