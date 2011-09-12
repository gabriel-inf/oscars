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

public class SimpleRequesterNSA implements RequesterAPI {
    protected ConnectionHolder connectionHolder = new ConnectionHolder();

    /******************
     *                *
     * REQUESTER SIDE *
     *                *
     ******************/
    
    
    public void reservationConfirmed(ReservationConfirmedRequestType rcrt) throws NSIServiceException {
        connectionHolder.re_reservationConfirmed(rcrt);
    }
    public void reservationFailed(ReservationFailedRequestType rfrt) throws NSIServiceException {
        connectionHolder.re_reservationFailed(rfrt);
    }
    public void provisionConfirmed(ProvisionConfirmedRequestType pcrt) throws NSIServiceException {
        connectionHolder.re_provisionConfirmed(pcrt);
    }
    public void provisionFailed(ProvisionFailedRequestType pfrt) throws NSIServiceException {
        connectionHolder.re_provisionFailed(pfrt);
    }
    public void releaseConfirmed(ReleaseConfirmedRequestType rcrt) throws NSIServiceException {
        connectionHolder.re_releaseConfirmed(rcrt);
    }
    public void releaseFailed(ReleaseFailedRequestType rfrt) throws NSIServiceException {
        connectionHolder.re_releaseFailed(rfrt);
    }
    public void terminateConfirmed(TerminateConfirmedRequestType tcrt) throws NSIServiceException {
        connectionHolder.re_terminateConfirmed(tcrt);
    }
    public void terminateFailed(TerminateFailedRequestType tfrt) throws NSIServiceException {
        connectionHolder.re_terminateFailed(tfrt);
    }
    public void queryConfirmed(QueryConfirmedRequestType qcrt) throws NSIServiceException {
    }
    public void queryFailed(QueryFailedRequestType qcrt) throws NSIServiceException {
    }
    
    public void initiateResvRequest(ReservationRequestType rrt) throws NSIServiceException {
        connectionHolder.re_initiateReservation(rrt);
    }
}
