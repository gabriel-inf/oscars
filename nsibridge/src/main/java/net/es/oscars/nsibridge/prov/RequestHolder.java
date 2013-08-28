package net.es.oscars.nsibridge.prov;


import net.es.oscars.nsibridge.beans.*;

import java.util.ArrayList;
import java.util.List;

public class RequestHolder {
    private List<ResvRequest>   resvRequests    = new ArrayList<ResvRequest>();
    private List<QueryRequest>  queryRequests   = new ArrayList<QueryRequest>();
    private List<SimpleRequest>  simpleRequests = new ArrayList<SimpleRequest>();


    private static RequestHolder instance;
    private RequestHolder() {}

    public static RequestHolder getInstance() {
        if (instance == null) instance = new RequestHolder();
        return instance;
    }

    public ResvRequest findResvRequest(String connId) {
        for (ResvRequest rr : resvRequests) {
            if (rr.getReserveType().getConnectionId().equals(connId)) return rr;
        }
        return null;
    }

    public void removeResvRequest(String connId) {
        int idx = 0;
        for (ResvRequest rr : resvRequests) {
            if (rr.getReserveType().getConnectionId().equals(connId)) {
                resvRequests.remove(idx);
                return;
            }
            idx++;
        }
    }


    public SimpleRequest findSimpleRequest(String connId, SimpleRequestType type) {
        for (SimpleRequest sr : simpleRequests) {
            if (sr.getConnectionId().equals(connId) && sr.getRequestType().equals(type)) {

                return sr;
            }
        }
        return null;
    }


    public void removeSimpleRequest(String connId, SimpleRequestType type) {
        int idx = 0;
        for (SimpleRequest sr : simpleRequests) {
            if (sr.getConnectionId().equals(connId) && sr.getRequestType().equals(type)) {
                simpleRequests.remove(idx);
                return;
            }
            idx++;
        }
    }



    public List<ResvRequest> getResvRequests() {
        return this.resvRequests;
    }


    public void setResvRequests(List<ResvRequest> resvRequests) {
        this.resvRequests = resvRequests;
    }

    public List<QueryRequest> getQueryRequests() {
        return queryRequests;
    }

    public void setQueryRequests(List<QueryRequest> queryRequests) {
        this.queryRequests = queryRequests;
    }

    public List<SimpleRequest> getSimpleRequests() {
        return simpleRequests;
    }

    public void setSimpleRequests(List<SimpleRequest> simpleRequests) {
        this.simpleRequests = simpleRequests;
    }
}
