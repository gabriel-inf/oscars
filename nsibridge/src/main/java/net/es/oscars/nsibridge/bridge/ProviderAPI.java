package net.es.oscars.nsibridge.bridge;

import net.es.oscars.nsibridge.soap.gen.ifce.NSIServiceException;
import net.es.oscars.nsibridge.soap.gen.ifce.ProvisionRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.QueryRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReleaseRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReservationRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.TerminateRequestType;

public interface ProviderAPI {
    public void tick() throws NSIServiceException;

    public void addQueryRequest(QueryRequestType rrt);
    public void addReservationRequest(ReservationRequestType rrt);
    public void addProvisionRequest(ProvisionRequestType rrt);
    public void addTerminateRequest(TerminateRequestType rrt);
    public void addReleaseRequest(ReleaseRequestType rrt);
    


}
