package net.es.oscars.nsibridge.bridge;

import net.es.oscars.nsibridge.soap.gen.ifce.ReserveConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReserveFailedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReserveRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.ifce.ProvisionConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ProvisionFailedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.QueryConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.QueryFailedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReleaseConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReleaseFailedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.TerminateConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.TerminateFailedRequestType;

public class SimpleRequesterNSA implements RequesterAPI {
    protected ConnectionHolder connectionHolder = new ConnectionHolder();

    /******************
     *                *
     * REQUESTER SIDE *
     *                *
     ******************/
    
    
    public void reserveConfirmed(ReserveConfirmedRequestType rcrt) throws ServiceException {
        connectionHolder.re_reservationConfirmed(rcrt);
    }
    public void reserveFailed(ReserveFailedRequestType rfrt) throws ServiceException {
        connectionHolder.re_reservationFailed(rfrt);
    }
    public void provisionConfirmed(ProvisionConfirmedRequestType pcrt) throws ServiceException {
        connectionHolder.re_provisionConfirmed(pcrt);
    }
    public void provisionFailed(ProvisionFailedRequestType pfrt) throws ServiceException {
        connectionHolder.re_provisionFailed(pfrt);
    }
    public void releaseConfirmed(ReleaseConfirmedRequestType rcrt) throws ServiceException {
        connectionHolder.re_releaseConfirmed(rcrt);
    }
    public void releaseFailed(ReleaseFailedRequestType rfrt) throws ServiceException {
        connectionHolder.re_releaseFailed(rfrt);
    }
    public void terminateConfirmed(TerminateConfirmedRequestType tcrt) throws ServiceException {
        connectionHolder.re_terminateConfirmed(tcrt);
    }
    public void terminateFailed(TerminateFailedRequestType tfrt) throws ServiceException {
        connectionHolder.re_terminateFailed(tfrt);
    }
    public void queryConfirmed(QueryConfirmedRequestType qcrt) throws ServiceException {
    }
    public void queryFailed(QueryFailedRequestType qcrt) throws ServiceException {
    }
    
    public void initiateResvRequest(ReserveRequestType rrt) throws ServiceException {
        connectionHolder.re_initiateReservation(rrt);
    }
}
