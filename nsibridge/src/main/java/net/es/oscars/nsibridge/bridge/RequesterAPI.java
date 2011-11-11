package net.es.oscars.nsibridge.bridge;

import net.es.oscars.nsibridge.soap.gen.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.ifce.ProvisionConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ProvisionFailedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.QueryConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.QueryFailedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReleaseConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReleaseFailedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReserveConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReserveFailedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReserveRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.TerminateConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.TerminateFailedRequestType;

public interface RequesterAPI {
    public void initiateResvRequest(ReserveRequestType rrt) throws ServiceException;
    
    public void queryConfirmed(QueryConfirmedRequestType qcrt) throws ServiceException;
    public void queryFailed(QueryFailedRequestType qcrt) throws ServiceException; 
    public void reserveConfirmed(ReserveConfirmedRequestType rcrt) throws ServiceException;
    public void reserveFailed(ReserveFailedRequestType rfrt) throws ServiceException;
    public void provisionConfirmed(ProvisionConfirmedRequestType pcrt) throws ServiceException;
    public void provisionFailed(ProvisionFailedRequestType pfrt) throws ServiceException;
    public void releaseConfirmed(ReleaseConfirmedRequestType rcrt) throws ServiceException;
    public void releaseFailed(ReleaseFailedRequestType parameters) throws ServiceException;
    public void terminateConfirmed(TerminateConfirmedRequestType tcrt) throws ServiceException;
    public void terminateFailed(TerminateFailedRequestType tfrt) throws ServiceException;
}
