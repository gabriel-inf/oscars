package net.es.oscars.nsibridge.bridge;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.ogf.nsi.schema.connectionTypes.ComponentPathType;
import org.ogf.nsi.schema.connectionTypes.ConnectionStateType;
import org.ogf.nsi.schema.connectionTypes.DetailedPathType;
import org.ogf.nsi.schema.connectionTypes.DirectionalityType;
import org.ogf.nsi.schema.connectionTypes.PathListType;

import net.es.oscars.nsibridge.beans.NSAConnection;
import net.es.oscars.nsibridge.soap.gen.ifce.NSIServiceException;
import net.es.oscars.nsibridge.soap.gen.ifce.ProvisionConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ProvisionFailedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ProvisionRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReleaseConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReleaseFailedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReleaseRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReservationConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReservationFailedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReservationRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.TerminateConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.TerminateFailedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.TerminateRequestType;

public class ConnectionHolder {
    Logger log = Logger.getLogger(ConnectionHolder.class);
    private HashMap<String, NSAConnection> connections = new HashMap<String, NSAConnection>();


    public Collection<NSAConnection> getConnections() {
        return connections.values();
    }
    
    
    public void pe_Reservation(ReservationRequestType rrt) throws NSIServiceException {
        NSAConnection conn = new NSAConnection();
        conn.setConnectionId(rrt.getReservation().getReservation().getConnectionId());
        conn.setDescription(rrt.getReservation().getReservation().getDescription());
        conn.setGri(rrt.getReservation().getReservation().getGlobalReservationId());
        conn.setPath(rrt.getReservation().getReservation().getPath());
        conn.setProviderNSA(rrt.getReservation().getProviderNSA());
        conn.setRequesterNSA(rrt.getReservation().getRequesterNSA());
        conn.setServiceParams(rrt.getReservation().getReservation().getServiceParameters());
        conn.setState(ConnectionStateType.RESERVING);
        
        //
        DetailedPathType dpt = new DetailedPathType();
        dpt.setConnectionId(conn.getConnectionId());
        dpt.setConnectionState(ConnectionStateType.RESERVING);
        dpt.setOrder(BigInteger.valueOf(1));
        dpt.setProviderNSA(conn.getProviderNSA());
        PathListType plt = new PathListType();
        ComponentPathType cpt = new ComponentPathType();
        cpt.setDirectionality(DirectionalityType.BIDIRECTIONAL);
        cpt.setSourceSTP(conn.getPath().getSourceSTP());
        cpt.setDestSTP(conn.getPath().getDestSTP());
        plt.getPath().add(cpt);
        dpt.setPathList(plt);
        conn.setDetailedPath(dpt);
        connections.put(rrt.getReservation().getReservation().getConnectionId(), conn);
        log.info("PE: new resv request, added a new connection with id "+conn.getConnectionId()+" state: RESERVING");
    }

    public void pe_Provision(ProvisionRequestType prt) throws NSIServiceException {
        String connectionId = prt.getProvision().getConnectionId();
        NSAConnection conn = this.connections.get(connectionId);
        if (conn != null) {
            log.info("PE: received provision request for "+connectionId+" state: "+conn.getState()+" --> PROVISIONING");
            conn.setState(ConnectionStateType.PROVISIONING);
            conn.getDetailedPath().setConnectionState(ConnectionStateType.PROVISIONING);
        } else {
            throw new NSIServiceException("connection with id "+connectionId+" not found");
        }
        
    }

    public void pe_Release(ReleaseRequestType rrt) throws NSIServiceException {
        String connectionId = rrt.getRelease().getConnectionId();
        NSAConnection conn = this.connections.get(connectionId);
        if (conn != null) {
            log.info("PE: received release request for "+connectionId+" state: "+conn.getState()+" --> RELEASING");
            conn.setState(ConnectionStateType.RELEASING);
            conn.getDetailedPath().setConnectionState(ConnectionStateType.RELEASING);
        } else {
            throw new NSIServiceException("connection with id "+connectionId+" not found");
        }
        
    }

    public void pe_Terminate(TerminateRequestType trt) throws NSIServiceException {
        String connectionId = trt.getTerminate().getConnectionId();
        NSAConnection conn = this.connections.get(connectionId);
        if (conn != null) {
            log.info("PE: received terminate request for "+connectionId+" state: "+conn.getState()+" --> TERMINATEING");
            conn.setState(ConnectionStateType.TERMINATEING);
            conn.getDetailedPath().setConnectionState(ConnectionStateType.TERMINATEING);
        } else {
            throw new NSIServiceException("connection with id "+connectionId+" not found");
        }
        
    }
    public void re_initiateReservation(ReservationRequestType rrt) throws NSIServiceException {
        NSAConnection conn = new NSAConnection();
        conn.setConnectionId(rrt.getReservation().getReservation().getConnectionId());
        conn.setDescription(rrt.getReservation().getReservation().getDescription());
        conn.setGri(rrt.getReservation().getReservation().getGlobalReservationId());
        conn.setPath(rrt.getReservation().getReservation().getPath());
        conn.setProviderNSA(rrt.getReservation().getProviderNSA());
        conn.setRequesterNSA(rrt.getReservation().getRequesterNSA());
        conn.setServiceParams(rrt.getReservation().getReservation().getServiceParameters());
        conn.setState(ConnectionStateType.RESERVING);
        
        //
        DetailedPathType dpt = new DetailedPathType();
        dpt.setConnectionId(conn.getConnectionId());
        dpt.setConnectionState(ConnectionStateType.RESERVING);
        dpt.setOrder(BigInteger.valueOf(1));
        dpt.setProviderNSA(conn.getProviderNSA());
        PathListType plt = new PathListType();
        ComponentPathType cpt = new ComponentPathType();
        cpt.setDirectionality(DirectionalityType.BIDIRECTIONAL);
        cpt.setSourceSTP(conn.getPath().getSourceSTP());
        cpt.setDestSTP(conn.getPath().getDestSTP());
        plt.getPath().add(cpt);
        dpt.setPathList(plt);
        conn.setDetailedPath(dpt);
        connections.put(rrt.getReservation().getReservation().getConnectionId(), conn);
        log.info("RE: new resv request, added a new connection with id "+conn.getConnectionId()+" state: RESERVING");
    }

    
    public void re_reservationConfirmed(ReservationConfirmedRequestType rcrt) throws NSIServiceException {
        String connectionId = rcrt.getReservationConfirmed().getReservation().getConnectionId();
        NSAConnection conn = this.connections.get(connectionId);
        if (conn != null) {
            log.info("RE: received resv confirmed for "+connectionId+" state: "+conn.getState()+" --> RESERVED");
            conn.setState(ConnectionStateType.RESERVED);
            conn.getDetailedPath().setConnectionState(ConnectionStateType.RESERVED);
        } else {
            throw new NSIServiceException("connection with id "+connectionId+" not found");
        }
        
    }


    public void re_reservationFailed(ReservationFailedRequestType rfrt) throws NSIServiceException {
        String connectionId = rfrt.getReservationFailed().getConnectionId();
        NSAConnection conn = this.connections.get(connectionId);
        if (conn != null) {
            log.info("RE: received resv failed for "+connectionId+" state: "+conn.getState()+" --> UNKNOWN");
            conn.setState(ConnectionStateType.UNKNOWN);
            conn.getDetailedPath().setConnectionState(ConnectionStateType.UNKNOWN);
        } else {
            throw new NSIServiceException("connection with id "+connectionId+" not found");
        }
    }
    
    


    public void re_provisionConfirmed(ProvisionConfirmedRequestType pcrt) throws NSIServiceException {
        String connectionId = pcrt.getProvisionConfirmed().getConnectionId();
        NSAConnection conn = this.connections.get(connectionId);
        if (conn != null) {
            log.info("RE: received provision confirm for "+connectionId+" state: "+conn.getState()+" --> PROVISIONED");
            conn.setState(ConnectionStateType.PROVISIONED);
            conn.getDetailedPath().setConnectionState(ConnectionStateType.PROVISIONED);
        } else {
            throw new NSIServiceException("connection with id "+connectionId+" not found");
        }
        
        
    }


    public void re_provisionFailed(ProvisionFailedRequestType pfrt) throws NSIServiceException {
        String connectionId = pfrt.getProvisionFailed().getConnectionId();
        NSAConnection conn = this.connections.get(connectionId);
        if (conn != null) {
            log.info("RE: received provision failed for "+connectionId+" state: "+conn.getState()+" --> RESERVED");
            conn.setState(ConnectionStateType.RESERVED);
            conn.getDetailedPath().setConnectionState(ConnectionStateType.RESERVED);
        } else {
            throw new NSIServiceException("connection with id "+connectionId+" not found");
        }
    }


    public void re_releaseConfirmed(ReleaseConfirmedRequestType rcrt) throws NSIServiceException {
        String connectionId = rcrt.getReleaseConfirmed().getConnectionId();
        NSAConnection conn = this.connections.get(connectionId);
        if (conn != null) {
            log.info("RE: received release confirm for "+connectionId+" state: "+conn.getState()+" --> RESERVED");
            conn.setState(ConnectionStateType.RESERVED);
            conn.getDetailedPath().setConnectionState(ConnectionStateType.RESERVED);
        } else {
            throw new NSIServiceException("connection with id "+connectionId+" not found");
        }
        
    }


    public void re_releaseFailed(ReleaseFailedRequestType rfrt) throws NSIServiceException {
        String connectionId = rfrt.getReleaseFailed().getConnectionId();
        NSAConnection conn = this.connections.get(connectionId);
        if (conn != null) {
            log.info("RE: received release failed for "+connectionId+" state: "+conn.getState()+" --> PROVISIONED");
            conn.setState(ConnectionStateType.PROVISIONED);
            conn.getDetailedPath().setConnectionState(ConnectionStateType.PROVISIONED);
        } else {
            throw new NSIServiceException("connection with id "+connectionId+" not found");
        }        
    }


    public void re_terminateConfirmed(TerminateConfirmedRequestType tcrt) throws NSIServiceException {
        String connectionId = tcrt.getTerminateConfirmed().getConnectionId();
        NSAConnection conn = this.connections.get(connectionId);
        if (conn != null) {
            log.info("RE: received terminate confirm for "+connectionId+" state: "+conn.getState()+" --> TERMINATED");
            conn.setState(ConnectionStateType.TERMINATED);
            conn.getDetailedPath().setConnectionState(ConnectionStateType.TERMINATED);
        } else {
            throw new NSIServiceException("connection with id "+connectionId+" not found");
        }    
    }


    public void re_terminateFailed(TerminateFailedRequestType tfrt) throws NSIServiceException {
        String connectionId = tfrt.getTerminateFailed().getConnectionId();
        NSAConnection conn = this.connections.get(connectionId);
        if (conn != null) {
            log.info("RE: received terminate failed for "+connectionId+" state: "+conn.getState()+" --> .RESERVED");
            conn.setState(ConnectionStateType.RESERVED);
            conn.getDetailedPath().setConnectionState(ConnectionStateType.RESERVED);
        } else {
            throw new NSIServiceException("connection with id "+connectionId+" not found");
        }        
    }

}
