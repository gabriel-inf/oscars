package net.es.oscars.nsibridge.test.requester;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import javax.xml.datatype.XMLGregorianCalendar;

import net.es.oscars.nsibridge.beans.NSAInfo;
import net.es.oscars.nsibridge.bridge.NSAAPI;
import net.es.oscars.nsibridge.bridge.NSAFactory;
import net.es.oscars.nsibridge.bridge.OSCARS_0_5_NSA;
import net.es.oscars.nsibridge.bridge.SimpleNSA;
import net.es.oscars.nsibridge.common.JettyContainer;
import net.es.oscars.nsibridge.provider.ConnectionProviderImpl;
import net.es.oscars.nsibridge.requester.ConnectionRequesterImpl;
import net.es.oscars.nsibridge.soap.gen.ifce.ReserveRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.ifce.ProvisionRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReleaseRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.TerminateRequestType;
import net.es.oscars.nsibridge.soap.gen.provider.ConnectionProviderPort;
import net.es.oscars.nsibridge.util.NSAList;
import net.es.oscars.nsibridge.util.RequestBuilder;
import net.es.oscars.nsibridge.util.RequestPrinter;

import org.apache.log4j.Logger;
import org.ogf.nsi.schema.connectionTypes.PathType;
import org.ogf.nsi.schema.connectionTypes.ScheduleType;
import org.ogf.nsi.schema.connectionTypes.ServiceParametersType;
import org.testng.annotations.Test;
@Test
public class LifecycleTest {
    private static final Logger LOG = Logger.getLogger(LifecycleTest.class);

    /*
    @Test
    public void testGlambdaRequest() throws MalformedURLException, ServiceException, InterruptedException {
        setupJettyInstances();
        setupNSAs();
        NSAInfo nsaD = NSAList.getInstance().getNSAByAbbrv("dominica");
        NSAInfo nsaG = NSAList.getInstance().getNSAByAbbrv("grenada");

        ConnectionProviderPort prvGPort = RequestBuilder.getProviderClient(nsaG); 
        String corrId = "urn:uuid:"+UUID.randomUUID().toString();
        String connId = "urn:uuid:"+UUID.randomUUID().toString();
        String gri = "urn:ogf:network:service:dominica-1121";
        
        String src = "urn:ogf:network:stp:Grenada:G1";
        String dst = "urn:ogf:network:stp:Grenada:G2";
        PathType pt = RequestBuilder.makePathType(src, dst);
        String desc = "test request Dominica -> Grenada, G1 to G2";
        Date now = new Date();
        XMLGregorianCalendar start = RequestBuilder.getXMLGC(now, 60);
        
        XMLGregorianCalendar end = RequestBuilder.getXMLGC(now, 60+300);

        
        ScheduleType st = RequestBuilder.makeStartEndSchedule(start, end);
        ServiceParametersType spt = RequestBuilder.makeServiceParametersType(1000, st);
        
        
        ReservationRequestType rrt = RequestBuilder.makeReservationRequestType(nsaD, nsaG, connId, corrId, gri, desc, pt, spt);
        // make sure local RE is ready to receive confirmation
        NSAFactory.getInstance().getNSA(nsaD.getNsaId()).initiateResvRequest(rrt);
        System.out.println("sending req: "+RequestPrinter.printResvReq(rrt));
        prvGPort.reservation(rrt);

        boolean pendingRequests = true;
        while (pendingRequests) {
            // pendingRequests = false;
            for (NSAAPI nsa : NSAFactory.getInstance().getNSAs()) {
                if (nsa.isWorking()) {
                    pendingRequests = true;
                    nsa.tick();
                }
            }
            Thread.sleep(1000);
        }
    }
    */
    public void testSubmitRequest() throws ServiceException, MalformedURLException, InterruptedException {
        setupJettyInstances(false);
        setupNSAs(false);
        NSAInfo nsaA = NSAList.getInstance().getNSAByAbbrv("alpha");
        NSAInfo nsaB = NSAList.getInstance().getNSAByAbbrv("bravo");
        ConnectionProviderPort prvAPort = RequestBuilder.getProviderClient(nsaA); 
        
        String corrId = "urn:uuid:"+UUID.randomUUID().toString();
        String connId = "urn:uuid:"+UUID.randomUUID().toString();
        String gri = "urn:ogf:network:service:alpha-1121";
        
        String src = "urn:ogf:network:stp:Dominica:D1";
        String dst = "urn:ogf:network:stp:Dominica:D2";
        PathType pt = RequestBuilder.makePathType(src, dst);
        
        String desc = "test request D1 to D2";
        Date now = new Date();
        XMLGregorianCalendar start = RequestBuilder.getXMLGC(now, 60);
        
        XMLGregorianCalendar end = RequestBuilder.getXMLGC(now, 60+300);

        
        ScheduleType st = RequestBuilder.makeStartEndSchedule(start, end);
        ServiceParametersType spt = RequestBuilder.makeServiceParametersType(1000, st);
        
        
        ReserveRequestType rrt = RequestBuilder.makeReserveRequestType(nsaB, nsaA, connId, corrId, gri, desc, pt, spt);
        NSAFactory.getInstance().getNSA(nsaB.getNsaId()).initiateResvRequest(rrt);
        prvAPort.reserve(rrt);
        
        boolean pendingRequests = true;
        while (pendingRequests) {
            pendingRequests = false;
            for (NSAAPI nsa : NSAFactory.getInstance().getNSAs()) {
                if (nsa.isWorking()) {
                    pendingRequests = true;
                    nsa.tick();
                }
            }
            Thread.sleep(1000);
        }
        
        pendingRequests = true;
        ProvisionRequestType prt = RequestBuilder.makeProvisionRequestType(nsaB, nsaA, connId, corrId);
        prvAPort.provision(prt);
        while (pendingRequests) {
            pendingRequests = false;
            for (NSAAPI nsa : NSAFactory.getInstance().getNSAs()) {
                if (nsa.isWorking()) {
                    pendingRequests = true;
                    nsa.tick();
                }
            }
            Thread.sleep(1000);
        }
        
        pendingRequests = true;
        ReleaseRequestType relrt = RequestBuilder.makeReleaseRequestType(nsaB, nsaA, connId, corrId);
        prvAPort.release(relrt);
        while (pendingRequests) {
            pendingRequests = false;
            for (NSAAPI nsa : NSAFactory.getInstance().getNSAs()) {
                if (nsa.isWorking()) {
                    pendingRequests = true;
                    nsa.tick();
                }
            }
            Thread.sleep(1000);
        }
        

        pendingRequests = true;
        TerminateRequestType trt = RequestBuilder.makeTerminateRequestType(nsaB, nsaA, connId, corrId);
        prvAPort.terminate(trt);
        while (pendingRequests) {
            pendingRequests = false;
            for (NSAAPI nsa : NSAFactory.getInstance().getNSAs()) {
                if (nsa.isWorking()) {
                    pendingRequests = true;
                    nsa.tick();
                }
            }
            Thread.sleep(1000);
        }
        
    }

    private void setupJettyInstances(boolean external) {
        int portA = 8001;
        int portB = 8002;
        int portD = 8088;
        
        HashMap<String, Object> servicesA = new HashMap<String, Object>();
        HashMap<String, Object> servicesB = new HashMap<String, Object>();
        HashMap<String, Object> servicesD = new HashMap<String, Object>();
        servicesA.put("ConnectionRequester", new ConnectionRequesterImpl());
        servicesA.put("ConnectionProvider", new ConnectionProviderImpl());
        
        servicesB.put("ConnectionRequester", new ConnectionRequesterImpl());
        servicesB.put("ConnectionProvider", new ConnectionProviderImpl());
        
        servicesD.put("ConnectionRequester", new ConnectionRequesterImpl());
        servicesD.put("ConnectionProvider", new ConnectionProviderImpl());
        
        JettyContainer jcA = new JettyContainer();
        jcA.setPort(portA);
        jcA.setHostname("localhost");
        jcA.setServicesToExpose(servicesA);
        
        JettyContainer jcB = new JettyContainer();
        jcB.setPort(portB);
        jcB.setHostname("localhost");
        jcB.setServicesToExpose(servicesB);
        
        JettyContainer jcD = new JettyContainer();
        jcD.setPort(portD);
        jcD.setHostname("jupiter.es.net");
        jcD.setServicesToExpose(servicesD);

        LOG.info("starting Jettys");
        if (external) {
            jcD.startServer();
        } else {
            jcA.startServer();
            jcB.startServer();
        }
    }
    private void setupNSAs(boolean external) {

        if (external) {
            NSAInfo nsaD = new NSAInfo();
            nsaD.setNsaId("urn:ogf:network:nsa:Dominica-OSCARS");
            nsaD.setProviderWsdl("http://jupiter.es.net:8088/ConnectionProvider?wsdl");
            nsaD.setRequesterUrl("http://jupiter.es.net:8088/ConnectionRequester");
            NSAList.getInstance().addNSA("dominica", nsaD);
            NSAFactory.getInstance().addNSA(nsaD.getNsaId(), new SimpleNSA(nsaD.getNsaId()));
            
            NSAInfo nsaG = new NSAInfo();
            nsaG.setNsaId("urn:ogf:network:nsa:Grenada-GLAMBDA-AIST");
            nsaG.setProviderWsdl("http://163.220.30.174:8090/nsi_gl_proxy/services/ConnectionServiceProvider?wsdl");
            NSAList.getInstance().addNSA("grenada", nsaG);
        } else {
            NSAInfo nsaA = new NSAInfo();
            nsaA.setNsaId("urn:ogf:network:nsa:alpha-nsa");
            nsaA.setProviderWsdl("http://localhost:8001/ConnectionProvider?wsdl");
            nsaA.setRequesterUrl("http://localhost:8001/ConnectionRequester");
            NSAList.getInstance().addNSA("alpha", nsaA);
            NSAFactory.getInstance().addNSA(nsaA.getNsaId(), new SimpleNSA(nsaA.getNsaId()));

            NSAInfo nsaB = new NSAInfo();
            nsaB.setNsaId("urn:ogf:network:nsa:bravo-nsa");
            nsaB.setProviderWsdl("http://localhost:8002/ConnectionProvider?wsdl");
            nsaB.setRequesterUrl("http://localhost:8002/ConnectionRequester");
            NSAList.getInstance().addNSA("bravo", nsaB);
            NSAFactory.getInstance().addNSA(nsaB.getNsaId(), new SimpleNSA(nsaB.getNsaId()));
        }
    }
}
