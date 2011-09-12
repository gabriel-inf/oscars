package net.es.oscars.nsibridge.bridge;

import net.es.oscars.nsibridge.soap.gen.ifce.NSIServiceException;
import net.es.oscars.nsibridge.soap.gen.ifce.ProvisionRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReleaseRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReservationRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.TerminateRequestType;

public interface LocalControllerAPI {

    public boolean localReservation(ReservationRequestType rrt) throws NSIServiceException;
    
    public boolean localProvision(ProvisionRequestType prt) throws NSIServiceException;
    public boolean localRelease(ReleaseRequestType rrt) throws NSIServiceException;
    public boolean localTerminate(TerminateRequestType trt) throws NSIServiceException;

}
