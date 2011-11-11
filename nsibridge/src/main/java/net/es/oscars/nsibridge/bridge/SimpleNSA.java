package net.es.oscars.nsibridge.bridge;

import java.net.MalformedURLException;
import java.util.ArrayList;

import org.ogf.nsi.schema.connectionTypes.QueryDetailsResultType;
import org.ogf.nsi.schema.connectionTypes.QueryOperationType;
import org.ogf.nsi.schema.connectionTypes.QuerySummaryResultType;

import net.es.oscars.nsibridge.beans.NSAConnection;
import net.es.oscars.nsibridge.beans.NSAInfo;
import net.es.oscars.nsibridge.schedule.RequestsQueue;
import net.es.oscars.nsibridge.soap.gen.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.ifce.ProvisionRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.QueryRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReleaseRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReserveRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.ifce.TerminateRequestType;
import net.es.oscars.nsibridge.soap.gen.requester.ConnectionRequesterPort;
import net.es.oscars.nsibridge.util.NSAList;
import net.es.oscars.nsibridge.util.RequestBuilder;
import net.es.oscars.nsibridge.util.RequestPrinter;

public class SimpleNSA extends SimpleRequesterNSA implements ProviderAPI, RequesterAPI, NSAAPI, LocalControllerAPI {
    protected RequestsQueue rq;
    protected String nsaId;
    protected boolean isWorking = false;
    
    
    public SimpleNSA(String nsaId) {
        rq = new RequestsQueue();
        this.nsaId = nsaId;
    }
    public synchronized boolean isWorking() {
        return this.isWorking;
    }
    public NSAInfo getNSAInfo() {
        NSAInfo info = NSAList.getInstance().getNSAByNSAId(nsaId);
        return info;
    }
    
    
    
    /**************
     *            *
     * LOCAL SIDE *
     *            *
     *************/

    public boolean localReservation(ReserveRequestType rrt) throws ServiceException {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }
    
    public boolean localProvision(ProvisionRequestType prt) throws ServiceException {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }
    public boolean localRelease(ReleaseRequestType rrt) throws ServiceException {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }
    public boolean localTerminate(TerminateRequestType trt) throws ServiceException {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }
    
    
    /*****************
     *               *
     * PROVIDER SIDE *
     *               *
     *****************/
    
    public void addReservationRequest(ReserveRequestType rrt) {
        System.out.println(nsaId+" received reservation request [PROV]:" + RequestPrinter.printResvReq(rrt));
        this.isWorking = true;
        rq.getPendingResvReqs().add(rrt);
    }
    public void addProvisionRequest(ProvisionRequestType prt) {
        System.out.println(nsaId+" received provision request [PROV]:");
        this.isWorking = true;
        rq.getPendingProvReqs().add(prt);
    }
    public void addTerminateRequest(TerminateRequestType trt) {
        System.out.println(nsaId+" received terminate request [PROV]:");
        this.isWorking = true;
        rq.getPendingTermReqs().add(trt);
    }

    public void addReleaseRequest(ReleaseRequestType rrt) {
        System.out.println(nsaId+" received release request [PROV]:");
        this.isWorking = true;
        rq.getPendingRelReqs().add(rrt);
        
    }
    public void addQueryRequest(QueryRequestType qrt) {
        System.out.println(nsaId+" received query request [PROV]:");
        this.isWorking = true;
        rq.getPendingQueryReqs().add(qrt);
    }
    
    
    

    

    /***********
     * PA TICK *
     ***********/
    
    public synchronized void tick() throws ServiceException {
        for (QueryRequestType qrt : rq.getPendingQueryReqs()) {
            this.processQuery(qrt);
        }
        
        
        for (ReserveRequestType rrt : rq.getPendingResvReqs()) {
            this.connectionHolder.pe_Reservation(rrt);
            String reqNSA = rrt.getReserve().getRequesterNSA();
            System.out.println("Starting to process a reservation requested by: "+reqNSA);
            NSAInfo nsa = NSAList.getInstance().getNSAByNSAId(reqNSA);
            if (nsa == null) {
                System.out.println("Unknown / unpeered requester NSA: "+reqNSA);
            }
            try {
                this.localReservation(rrt);
                ConnectionRequesterPort reqPort = RequestBuilder.getRequesterClient(rrt.getReplyTo());
                reqPort.reserveConfirmed(RequestBuilder.makeReservationConfirmedRequestType(rrt));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            }
            
        }
        rq.getPendingResvReqs().clear();

        for (ProvisionRequestType prt : rq.getPendingProvReqs()) {
            this.connectionHolder.pe_Provision(prt);
            String reqNSA = prt.getProvision().getRequesterNSA();
            System.out.println("Starting to process a provision requested by: "+reqNSA);
            NSAInfo nsa = NSAList.getInstance().getNSAByNSAId(reqNSA);
            if (nsa == null) {
                System.out.println("Unknown / unpeered requester NSA: "+reqNSA);
            }
            try {
                this.localProvision(prt);
                ConnectionRequesterPort reqPort = RequestBuilder.getRequesterClient(prt.getReplyTo());
                reqPort.provisionConfirmed(RequestBuilder.makeProvisionConfirmedRequestType(prt));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            }
        }
        rq.getPendingProvReqs().clear();

        for (TerminateRequestType trt : rq.getPendingTermReqs()) {
            String reqNSA = trt.getTerminate().getRequesterNSA();
            System.out.println("Starting to process a terminate requested by: "+reqNSA);
            NSAInfo nsa = NSAList.getInstance().getNSAByNSAId(reqNSA);
            if (nsa == null) {
                System.out.println("Unknown / unpeered requester NSA: "+reqNSA);
            }
            try {
                this.localTerminate(trt);
                ConnectionRequesterPort reqPort = RequestBuilder.getRequesterClient(trt.getReplyTo());
                reqPort.terminateConfirmed(RequestBuilder.makeTerminateConfirmedRequestType(trt));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        rq.getPendingTermReqs().clear();

        for (ReleaseRequestType rrt : rq.getPendingRelReqs()) {
            String reqNSA = rrt.getRelease().getRequesterNSA();
            System.out.println("Starting to process a release requested by: "+reqNSA);
            NSAInfo nsa = NSAList.getInstance().getNSAByNSAId(reqNSA);
            if (nsa == null) {
                System.out.println("Unknown / unpeered requester NSA: "+reqNSA);
            }
            try {
                this.localRelease(rrt);
                ConnectionRequesterPort reqPort = RequestBuilder.getRequesterClient(rrt.getReplyTo());
                reqPort.releaseConfirmed(RequestBuilder.makeReleaseConfirmedRequestType(rrt));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        rq.getPendingRelReqs().clear();
        this.isWorking = false;
    }

    /**
     * TODO: query is not working
     * @param qrt
     */
    private void processQuery(QueryRequestType qrt) {
        String reqNSA = qrt.getQuery().getRequesterNSA();
        System.out.println("Starting to process a query requested by: "+reqNSA);
        NSAInfo nsa = NSAList.getInstance().getNSAByNSAId(reqNSA);
        if (nsa == null) {
            System.out.println("Unknown / unpeered requester NSA: "+reqNSA);
        }
        ArrayList<QueryDetailsResultType> qdrts = new ArrayList<QueryDetailsResultType>();
        
        ArrayList<QuerySummaryResultType> qsrts = new ArrayList<QuerySummaryResultType>();
        try {
            if (qrt.getQuery().getOperation().equals(QueryOperationType.DETAILS)) {
                if (qrt.getQuery().getQueryFilter() == null) {
                    for (NSAConnection conn : connectionHolder.getConnections()) {
                        QueryDetailsResultType qdrt = new QueryDetailsResultType();
                        qdrt.setConnectionId(conn.getConnectionId());
                        qdrt.setDescription(conn.getDescription());
                        qdrt.setGlobalReservationId(conn.getGri());
                        qdrt.setDetailedPath(conn.getDetailedPath());
                        qdrt.setServiceParameters(conn.getServiceParams());
                        qdrts.add(qdrt);
                    }
                } else if (qrt.getQuery().getQueryFilter().getConnectionId() != null) {
                    for (NSAConnection conn : connectionHolder.getConnections()) {
                        if (conn.getConnectionId().equals(qrt.getQuery().getQueryFilter().getConnectionId())) {
                            QueryDetailsResultType qdrt = new QueryDetailsResultType();
                            qdrt.setConnectionId(conn.getConnectionId());
                            qdrt.setDescription(conn.getDescription());
                            qdrt.setGlobalReservationId(conn.getGri());
                            qdrt.setDetailedPath(conn.getDetailedPath());
                            qdrt.setServiceParameters(conn.getServiceParams());
                            qdrts.add(qdrt);
                        }
                    }
                    
                }
            } else if (qrt.getQuery().getOperation().equals(QueryOperationType.SUMMARY)) {
                if (qrt.getQuery().getQueryFilter() == null) {
                    for (NSAConnection conn : connectionHolder.getConnections()) {
                        QuerySummaryResultType qsrt = new QuerySummaryResultType();
                        qsrt.setConnectionId(conn.getConnectionId());
                        qsrt.setConnectionState(conn.getState());
                        qsrt.setDescription(conn.getDescription());
                        qsrt.setGlobalReservationId(conn.getGri());
                        qsrt.setPath(conn.getPath());
                        qsrt.setServiceParameters(conn.getServiceParams());
                        qsrts.add(qsrt);
                    }
                } else if (qrt.getQuery().getQueryFilter().getConnectionId() != null) {
                    for (NSAConnection conn : connectionHolder.getConnections()) {
                        if (conn.getConnectionId().equals(qrt.getQuery().getQueryFilter().getConnectionId())) {
                            QuerySummaryResultType qsrt = new QuerySummaryResultType();
                            qsrt.setConnectionId(conn.getConnectionId());
                            qsrt.setConnectionState(conn.getState());
                            qsrt.setDescription(conn.getDescription());
                            qsrt.setGlobalReservationId(conn.getGri());
                            qsrt.setPath(conn.getPath());
                            qsrt.setServiceParameters(conn.getServiceParams());
                            qsrts.add(qsrt);
                        }
                    }
                }
            }
            ConnectionRequesterPort reqPort = RequestBuilder.getRequesterClient(qrt.getReplyTo());
            reqPort.queryConfirmed(RequestBuilder.makeQueryConfirmedRequestType(qrt, qdrts, qsrts));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }
    
}
