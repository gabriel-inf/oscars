package net.es.oscars.nsibridge.bridge;

import net.es.oscars.nsibridge.soap.gen.ifce.ProvisionRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.QueryRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReleaseRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReserveRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.ifce.TerminateRequestType;

public interface ProviderAPI {
    public void tick() throws ServiceException;

    public void addQueryRequest(QueryRequestType rrt);
    public void addReservationRequest(ReserveRequestType rrt);
    public void addProvisionRequest(ProvisionRequestType rrt);
    public void addTerminateRequest(TerminateRequestType rrt);
    public void addReleaseRequest(ReleaseRequestType rrt);
    


}
