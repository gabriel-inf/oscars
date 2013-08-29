package net.es.oscars.nsibridge.prov;


import net.es.oscars.nsibridge.beans.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RequestHolder {
    private HashMap<String, ResvRequest>    resvRequests    = new HashMap<String, ResvRequest>();
    private HashMap<String, QueryRequest>   queryRequests   = new HashMap<String, QueryRequest>();
    private HashMap<String, SimpleRequest>  simpleRequests  = new HashMap<String, SimpleRequest>();

    private static RequestHolder instance;
    private RequestHolder() {}


    public String findConnectionId(String correlationId) {
        if (resvRequests.containsKey(correlationId)) {
            return resvRequests.get(correlationId).getReserveType().getConnectionId();
        }
        if (simpleRequests.containsKey(correlationId)) {
            return simpleRequests.get(correlationId).getConnectionId();
        }
        return null;
    }


    public void removeRequest(String correlationId) {
        if (resvRequests.containsKey(correlationId)) {
            resvRequests.remove(correlationId);
        }
        if (simpleRequests.containsKey(correlationId)) {
            simpleRequests.remove(correlationId);
        }
    }

    public static RequestHolder getInstance() {
        if (instance == null) instance = new RequestHolder();
        return instance;
    }

    public ResvRequest findResvRequest(String correlationId) {
        return resvRequests.get(correlationId);
    }

    public void removeResvRequest(String correlationId) {
        resvRequests.remove(correlationId);
    }


    public SimpleRequest findSimpleRequest(String correlationId) {
        return simpleRequests.get(correlationId);
    }


    public void removeSimpleRequest(String correlationId) {
        simpleRequests.remove(correlationId);
    }

    public HashMap<String, ResvRequest> getResvRequests() {
        return resvRequests;
    }

    public HashMap<String, QueryRequest> getQueryRequests() {
        return queryRequests;
    }

    public HashMap<String, SimpleRequest> getSimpleRequests() {
        return simpleRequests;
    }


}
