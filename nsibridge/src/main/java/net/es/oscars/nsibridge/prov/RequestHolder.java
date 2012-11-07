package net.es.oscars.nsibridge.prov;


import net.es.oscars.nsibridge.beans.ResvRequest;

import java.util.ArrayList;
import java.util.List;

public class RequestHolder {
    private List<ResvRequest> resvRequests = new ArrayList<ResvRequest>();

    private static RequestHolder instance;
    private RequestHolder() {}
    public static RequestHolder getInstance() {
        if (instance == null) instance = new RequestHolder();
        return instance;
    }


    public List<ResvRequest> getResvRequests() {
        return this.resvRequests;
    }



}
