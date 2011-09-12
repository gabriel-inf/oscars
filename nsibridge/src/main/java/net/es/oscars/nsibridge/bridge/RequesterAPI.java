package net.es.oscars.nsibridge.bridge;

import net.es.oscars.nsibridge.soap.gen.ifce.NSIServiceException;
import net.es.oscars.nsibridge.soap.gen.ifce.ProvisionConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ProvisionFailedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.QueryConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.QueryFailedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReleaseConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReleaseFailedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReservationConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReservationFailedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReservationRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.TerminateConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.TerminateFailedRequestType;

public interface RequesterAPI {
    public void initiateResvRequest(ReservationRequestType rrt) throws NSIServiceException;
    
    public void queryConfirmed(QueryConfirmedRequestType qcrt) throws NSIServiceException;
    public void queryFailed(QueryFailedRequestType qcrt) throws NSIServiceException; 
    public void reservationConfirmed(ReservationConfirmedRequestType rcrt) throws NSIServiceException;
    public void reservationFailed(ReservationFailedRequestType rfrt) throws NSIServiceException;
    public void provisionConfirmed(ProvisionConfirmedRequestType pcrt) throws NSIServiceException;
    public void provisionFailed(ProvisionFailedRequestType pfrt) throws NSIServiceException;
    public void releaseConfirmed(ReleaseConfirmedRequestType rcrt) throws NSIServiceException;
    public void releaseFailed(ReleaseFailedRequestType parameters) throws NSIServiceException;
    public void terminateConfirmed(TerminateConfirmedRequestType tcrt) throws NSIServiceException;
    public void terminateFailed(TerminateFailedRequestType tfrt) throws NSIServiceException;
}
