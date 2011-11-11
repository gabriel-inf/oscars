package net.es.oscars.nsibridge.schedule;

import java.util.ArrayList;

import net.es.oscars.nsibridge.soap.gen.ifce.ProvisionRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.QueryRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReleaseRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReserveRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.TerminateRequestType;

public class RequestsQueue {
    ArrayList<ReserveRequestType> pendingResvReqs = new ArrayList<ReserveRequestType>();
    ArrayList<ProvisionRequestType> pendingProvReqs = new ArrayList<ProvisionRequestType>();
    ArrayList<TerminateRequestType> pendingTermReqs = new ArrayList<TerminateRequestType>();
    ArrayList<ReleaseRequestType> pendingRelReqs = new ArrayList<ReleaseRequestType>();
    ArrayList<QueryRequestType> pendingQueryReqs = new ArrayList<QueryRequestType>();
    
    public ArrayList<QueryRequestType> getPendingQueryReqs() {
        return pendingQueryReqs;
    }
    public void setPendingQueryReqs(ArrayList<QueryRequestType> pendingQueryReqs) {
        this.pendingQueryReqs = pendingQueryReqs;
    }
    public ArrayList<ReserveRequestType> getPendingResvReqs() {
        return pendingResvReqs;
    }
    public void setPendingResvReqs(ArrayList<ReserveRequestType> pendingResvReqs) {
        this.pendingResvReqs = pendingResvReqs;
    }
    public ArrayList<ProvisionRequestType> getPendingProvReqs() {
        return pendingProvReqs;
    }
    public void setPendingProvReqs(ArrayList<ProvisionRequestType> pendingProvReqs) {
        this.pendingProvReqs = pendingProvReqs;
    }
    public ArrayList<TerminateRequestType> getPendingTermReqs() {
        return pendingTermReqs;
    }
    public void setPendingTermReqs(ArrayList<TerminateRequestType> pendingTermReqs) {
        this.pendingTermReqs = pendingTermReqs;
    }
    public ArrayList<ReleaseRequestType> getPendingRelReqs() {
        return pendingRelReqs;
    }
    public void setPendingRelReqs(ArrayList<ReleaseRequestType> pendingRelReqs) {
        this.pendingRelReqs = pendingRelReqs;
    }
    
    
}
