package net.es.oscars.nsibridge.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import net.es.oscars.nsibridge.beans.NSAInfo;
import net.es.oscars.nsibridge.soap.gen.ifce.ProvisionConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ProvisionRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.QueryConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.QueryRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReleaseConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReleaseRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReserveConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReserveRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.TerminateConfirmedRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.TerminateRequestType;
import net.es.oscars.nsibridge.soap.gen.provider.ConnectionProviderPort;
import net.es.oscars.nsibridge.soap.gen.provider.ConnectionServiceProvider;
import net.es.oscars.nsibridge.soap.gen.requester.ConnectionRequesterPort;
import net.es.oscars.nsibridge.soap.gen.requester.ConnectionServiceRequester;

import org.ogf.nsi.schema.connectionTypes.BandwidthType;
import org.ogf.nsi.schema.connectionTypes.DirectionalityType;
import org.ogf.nsi.schema.connectionTypes.GenericConfirmedType;
import org.ogf.nsi.schema.connectionTypes.GenericRequestType;
import org.ogf.nsi.schema.connectionTypes.PathType;
import org.ogf.nsi.schema.connectionTypes.QueryConfirmedType;
import org.ogf.nsi.schema.connectionTypes.QueryDetailsResultType;
import org.ogf.nsi.schema.connectionTypes.QuerySummaryResultType;
import org.ogf.nsi.schema.connectionTypes.ReservationInfoType;
import org.ogf.nsi.schema.connectionTypes.ReserveConfirmedType;
import org.ogf.nsi.schema.connectionTypes.ReserveType;
import org.ogf.nsi.schema.connectionTypes.ScheduleType;
import org.ogf.nsi.schema.connectionTypes.ServiceParametersType;
import org.ogf.nsi.schema.connectionTypes.ServiceTerminationPointType;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

@SuppressWarnings("restriction")
public class RequestBuilder {
    public static PathType makePathType(String src, String dst) {

        PathType pt = new PathType();
        pt.setDirectionality(DirectionalityType.BIDIRECTIONAL);
        ServiceTerminationPointType srcStp = new ServiceTerminationPointType ();
        ServiceTerminationPointType dstStp = new ServiceTerminationPointType ();
        srcStp.setStpId(src);
        dstStp.setStpId(dst);
        
        pt.setSourceSTP(srcStp);
        pt.setDestSTP(dstStp);
        return pt;
    }
    
    public static ConnectionProviderPort getProviderClient(NSAInfo providerNSA) throws MalformedURLException {
        URL url = new URL(providerNSA.getProviderWsdl());
        ConnectionServiceProvider prvClient = new ConnectionServiceProvider(url);
        ConnectionProviderPort prvPort = prvClient.getConnectionServiceProviderPort();
        return prvPort;
    }
    
    public static ConnectionRequesterPort getRequesterClient(String replyTo) throws MalformedURLException {
        URL wsdlURL = RequestBuilder.class.getClassLoader().getResource("file:/Users/haniotak/helios/fenius/nsibridge/schema/ogf_nsi_connection_requester_v1_0.wsdl");
        QName serviceName = new QName("urn:service2", "MyService");
        QName portName = new QName("urn:service2", "ServicePort");
        ConnectionServiceRequester service = new ConnectionServiceRequester(wsdlURL, serviceName);
        service.addPort(portName, "http://schemas.xmlsoap.org/soap/", replyTo);
        ConnectionRequesterPort proxy = service.getPort(portName, ConnectionRequesterPort.class);
        return proxy;
    }
    
    public static XMLGregorianCalendar getXMLGC(Date date, long secondsToAdd) {
        GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
        gc.setTime(date);
        long ms = gc.getTimeInMillis();
        ms = ms + secondsToAdd*1000;
        gc.setTimeInMillis(ms);
        
        XMLGregorianCalendar xmlgc = new XMLGregorianCalendarImpl(gc);
        return xmlgc;
    }
    
    public static ScheduleType makeStartEndSchedule(XMLGregorianCalendar start, XMLGregorianCalendar end) {
        ScheduleType st = new ScheduleType();
        st.setStartTime(start);
        st.setEndTime(end);
        return st;
    }
    
    public static ServiceParametersType makeServiceParametersType(int bw, ScheduleType st) {
        ServiceParametersType spt = new ServiceParametersType();
        BandwidthType bpt = new BandwidthType();
        bpt.setDesired(bw);
        spt.setBandwidth(bpt);
        spt.setSchedule(st);
        return spt;
    }
    
    
    public static ProvisionRequestType makeProvisionRequestType(NSAInfo reqNSA, NSAInfo provNSA, String connId, String corrId) {
        ProvisionRequestType prt = new ProvisionRequestType();
        prt.setCorrelationId(corrId);
        prt.setReplyTo(reqNSA.getRequesterUrl());
        GenericRequestType grt = new GenericRequestType();
        grt.setConnectionId(connId);
        grt.setRequesterNSA(reqNSA.getNsaId());
        grt.setProviderNSA(provNSA.getNsaId());
        prt.setProvision(grt);
        
        return prt;
    }
    
    public static ReleaseRequestType makeReleaseRequestType(NSAInfo reqNSA, NSAInfo provNSA, String connId, String corrId) {
        ReleaseRequestType prt = new ReleaseRequestType();
        prt.setCorrelationId(corrId);
        prt.setReplyTo(reqNSA.getRequesterUrl());
        GenericRequestType grt = new GenericRequestType();
        grt.setConnectionId(connId);
        grt.setRequesterNSA(reqNSA.getNsaId());
        grt.setProviderNSA(provNSA.getNsaId());
        prt.setRelease(grt);
        
        return prt;
    }
    
    public static TerminateRequestType makeTerminateRequestType(NSAInfo reqNSA, NSAInfo provNSA, String connId, String corrId) {
        TerminateRequestType prt = new TerminateRequestType();
        prt.setCorrelationId(corrId);
        prt.setReplyTo(reqNSA.getRequesterUrl());
        GenericRequestType grt = new GenericRequestType();
        grt.setConnectionId(connId);
        grt.setRequesterNSA(reqNSA.getNsaId());
        grt.setProviderNSA(provNSA.getNsaId());
        prt.setTerminate(grt);
        
        return prt;
    }
    
    public static ReserveRequestType makeReserveRequestType(NSAInfo reqNSA, NSAInfo provNSA, String connId, String corrId, String gri, String description, PathType pt, ServiceParametersType spt) {
        ReserveRequestType rrt = new ReserveRequestType();
            
            
        rrt.setReplyTo(reqNSA.getRequesterUrl());
        rrt.setCorrelationId(corrId);
        ReserveType rt = new ReserveType();
        rrt.setReserve(rt);
        rt.setProviderNSA(provNSA.getNsaId());
        rt.setRequesterNSA(reqNSA.getNsaId());
        
        ReservationInfoType rit = new ReservationInfoType ();
        rit.setConnectionId(connId.toString());
        rit.setDescription(description);
        rit.setGlobalReservationId(gri);
        rit.setPath(pt);
        rt.setReservation(rit);
        rit.setServiceParameters(spt);
        
        return rrt;
    }

    public static ReserveConfirmedRequestType makeReservationConfirmedRequestType(ReserveRequestType rrt) {
        ReserveConfirmedRequestType rcrt = new ReserveConfirmedRequestType();
        ReserveConfirmedType rct = new ReserveConfirmedType();
        rct.setProviderNSA(rrt.getReserve().getProviderNSA());
        rct.setRequesterNSA(rrt.getReserve().getRequesterNSA());
        rct.setReservation(rrt.getReserve().getReservation());
        rcrt.setReserveConfirmed(rct);
        rcrt.setCorrelationId(rrt.getCorrelationId());
        return rcrt;
    }
    public static ProvisionConfirmedRequestType makeProvisionConfirmedRequestType(ProvisionRequestType prt) {
        ProvisionConfirmedRequestType pcrt = new ProvisionConfirmedRequestType();
        pcrt.setCorrelationId(prt.getCorrelationId());
        GenericConfirmedType gc = new GenericConfirmedType();
        gc.setConnectionId(prt.getProvision().getConnectionId());
        gc.setProviderNSA(prt.getProvision().getProviderNSA());
        gc.setRequesterNSA(prt.getProvision().getRequesterNSA());
        pcrt.setProvisionConfirmed(gc);
        return pcrt;
    }

    public static TerminateConfirmedRequestType makeTerminateConfirmedRequestType(TerminateRequestType trt) {
        TerminateConfirmedRequestType tcrt = new TerminateConfirmedRequestType();
        tcrt.setCorrelationId("urn:uuid:"+trt.getCorrelationId());
        GenericConfirmedType gc = new GenericConfirmedType();
        gc.setConnectionId(trt.getTerminate().getConnectionId());
        gc.setProviderNSA(trt.getTerminate().getProviderNSA());
        gc.setRequesterNSA(trt.getTerminate().getRequesterNSA());
        tcrt.setTerminateConfirmed(gc);
        return tcrt;
    }

    public static ReleaseConfirmedRequestType makeReleaseConfirmedRequestType(ReleaseRequestType rrt) {
        ReleaseConfirmedRequestType rcrt = new ReleaseConfirmedRequestType();
        rcrt.setCorrelationId(rrt.getCorrelationId());
        GenericConfirmedType gc = new GenericConfirmedType();
        gc.setConnectionId(rrt.getRelease().getConnectionId());
        gc.setProviderNSA(rrt.getRelease().getProviderNSA());
        gc.setRequesterNSA(rrt.getRelease().getRequesterNSA());
        rcrt.setReleaseConfirmed(gc);
        rcrt.setCorrelationId(rrt.getCorrelationId());
        return rcrt;
    }

    public static QueryConfirmedRequestType makeQueryConfirmedRequestType(QueryRequestType qrt, 
                List<QueryDetailsResultType> qdrts, List<QuerySummaryResultType> qsrts) {
        QueryConfirmedRequestType qcrt = new QueryConfirmedRequestType();
        qcrt.setCorrelationId(qrt.getCorrelationId());
        QueryConfirmedType qct = new QueryConfirmedType();
        qct.setProviderNSA(qrt.getQuery().getProviderNSA());
        qct.setRequesterNSA(qrt.getQuery().getRequesterNSA());
        for (QuerySummaryResultType qsrt : qsrts) {
            qct.getReservationSummary().add(qsrt);
        }

        for (QueryDetailsResultType qdrt : qdrts) {
            qct.getReservationDetails().add(qdrt);
        }
        qcrt.setQueryConfirmed(qct);
        return qcrt;
    }
}
