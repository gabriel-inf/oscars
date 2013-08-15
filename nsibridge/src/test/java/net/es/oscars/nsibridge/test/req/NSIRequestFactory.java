package net.es.oscars.nsibridge.test.req;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import net.es.oscars.nsibridge.beans.QueryRequest;
import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.beans.SimpleRequest;
import net.es.oscars.nsibridge.beans.SimpleRequestType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.types.*;

import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.point2point.*;


import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.types.TypeValuePairListType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.types.TypeValuePairType;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;


public class NSIRequestFactory {

    public static QueryRequest getQueryRequest() {
        QueryRequest req = new QueryRequest();
        QueryType q = new QueryType();
        req.setQuery(q);

        CommonHeaderType inHeader = makeHeader();
        req.setInHeader(inHeader);
        return req;
    }

    public static SimpleRequest getSimpleRequest(ResvRequest resvReq, SimpleRequestType type) {
        SimpleRequest pq = new SimpleRequest ();
        pq.setConnectionId(resvReq.getReserveType().getConnectionId());
        CommonHeaderType inHeader = makeHeader();
        pq.setInHeader(inHeader);
        pq.setRequestType(SimpleRequestType.PROVISION);
        return pq;
    }

    public static ResvRequest getRequest() throws DatatypeConfigurationException {
        Long threeMins = 3 * 60 * 1000L;
        Long tenMins = 10 * 60 * 1000L;
        Date now = new Date();
        Date sDate = new Date();
        sDate.setTime(now.getTime() + threeMins);
        Date eDate = new Date();
        eDate.setTime(sDate.getTime() + tenMins);

        ResvRequest req = new ResvRequest();
        ReservationRequestCriteriaType crit = new ReservationRequestCriteriaType();
        // PathType pt = new PathType();
        StpType srcStp = new StpType();
        StpType dstStp = new StpType();
        ScheduleType sch = new ScheduleType();

        XMLGregorianCalendar sTime = asXMLGregorianCalendar(sDate);
        XMLGregorianCalendar eTime = asXMLGregorianCalendar(eDate);


        sch.setStartTime(sTime);
        sch.setEndTime(eTime);

        srcStp.setLocalId("urn:ogf:network:stp:esnet.ets:chi-80");
        TypeValuePairType srcTvp = new TypeValuePairType();
        srcTvp.setType("VLAN");
        srcTvp.getValue().add("850");

        TypeValuePairListType slbls = new TypeValuePairListType();
        srcStp.setLabels(slbls);
        slbls.getAttribute().add(srcTvp);

        dstStp.setLocalId("urn:ogf:network:stp:esnet.ets:ps-80");
        TypeValuePairListType dlbls = new TypeValuePairListType();
        dstStp.setLabels(dlbls);

        TypeValuePairType dstTvp = new TypeValuePairType();
        dstTvp.setType("VLAN");
        dstTvp.getValue().add("850");
        dstStp.getLabels().getAttribute().add(dstTvp);

        /*
        pt.setDirectionality(DirectionalityType.BIDIRECTIONAL);
        pt.setSourceSTP(srcStp);
        pt.setDestSTP(dstStp);
        pt.setDirectionality(DirectionalityType.BIDIRECTIONAL);

        crit.setBandwidth(100);
        crit.setPath(pt);
        */
        crit.setSchedule(sch);


        String connId = UUID.randomUUID().toString();
        req.getReserveType().setConnectionId(connId);
        req.getReserveType().setDescription("test description");
        req.getReserveType().setGlobalReservationId("some GRI");
        req.getReserveType().setCriteria(crit);


        CommonHeaderType inHeader = makeHeader();
        req.setInHeader(inHeader);
        return req;
    }

    public static CommonHeaderType makeHeader() {
        CommonHeaderType inHeader = new CommonHeaderType();
        inHeader.setProtocolVersion("http://schemas.ogf.org/nsi/2012/03/connection");
        inHeader.setCorrelationId("urn:" + UUID.randomUUID().toString());
        inHeader.setRequesterNSA("urn:ogf:network:nsa:starlight");
        inHeader.setProviderNSA("urn:ogf:network:nsa:esnet");




        inHeader.setReplyTo("http://localhost:8088/ConnectionRequester");
        return inHeader;
    }


    public static XMLGregorianCalendar asXMLGregorianCalendar(java.util.Date date) throws DatatypeConfigurationException {
        DatatypeFactory df = DatatypeFactory.newInstance();

        if (date == null) {
            return null;
        } else {
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTimeInMillis(date.getTime());
            return df.newXMLGregorianCalendar(gc);
        }
    }

}
