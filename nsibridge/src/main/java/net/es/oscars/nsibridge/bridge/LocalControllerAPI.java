package net.es.oscars.nsibridge.bridge;

import net.es.oscars.nsibridge.soap.gen.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.ifce.ProvisionRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReleaseRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReserveRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.TerminateRequestType;

public interface LocalControllerAPI {

    public boolean localReservation(ReserveRequestType rrt) throws ServiceException;
    
    public boolean localProvision(ProvisionRequestType prt) throws ServiceException;
    public boolean localRelease(ReleaseRequestType rrt) throws ServiceException;
    public boolean localTerminate(TerminateRequestType trt) throws ServiceException;

}
