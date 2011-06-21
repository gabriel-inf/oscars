package net.es.oscars.api.forwarder;

import java.net.MalformedURLException;
import java.net.URL;
   // net.es.oscars.api.soap.gen.v06.

import net.es.oscars.api.soap.gen.v05.ResCreateContent;
import net.es.oscars.utils.clients.IDCClient05;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.api.compat.ForwardTypes;
import net.es.oscars.api.compat.SubscribeManager05;

import net.es.oscars.api.compat.DataTranslator05;

import org.apache.log4j.Logger;

/**
 * This abstract class is to be extended by the IDC message forwarders, implementing when necessary, protocol adaptation.
 * 
 * @author lomax
 *
 */

public class Forwarder05 extends Forwarder {
    
    private static Logger LOG = Logger.getLogger(Forwarder06.class.getName());
    private static final String DEFAULT_CONNTYPE = "x509";
    
    private IDCClient05 client       = null;
    private SubscribeManager05 subscribeManager;
    
    public Forwarder05 (String destDomainId, URL url) throws OSCARSServiceException {
        super (destDomainId, url);
        // Instantiates Forwarder06 client
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_API);
   
        URL wsdl;
        try {
            wsdl = cc.getWSDLPath(ServiceNames.SVC_API,"0.5");
            this.client = IDCClient05.getClient(this.url, wsdl, Forwarder05.DEFAULT_CONNTYPE);
        } catch (MalformedURLException e) {
            throw new OSCARSServiceException ("Cannot create IDC 0.5 client for " + this.url);
        }
        
        try {
            this.subscribeManager = SubscribeManager05.getInstance();
        } catch (ConfigException e) {
            throw new OSCARSServiceException ("Cannot create a subscription management class: " + e.getMessage());
        };
        
    }

    /* (non-Javadoc)
     * @see OSCARSInternalPortType#createPath(CreatePathContent  createPath ,)String  destDomainId )*
     */
    public net.es.oscars.api.soap.gen.v06.CreatePathResponseContent
        createPath(net.es.oscars.api.soap.gen.v06.CreatePathContent createPath06) throws OSCARSServiceException {

        net.es.oscars.api.soap.gen.v05.ForwardReply forwardReply = null;

        net.es.oscars.api.soap.gen.v05.CreatePathContent createPath05 = DataTranslator05.translate (createPath06);
        forwardReply = this.forward (createPath05);

        return DataTranslator05.translate (forwardReply.getCreatePath());
    }

    /* (non-Javadoc)
     * @see OSCARSInternalPortType#notify(EventContent  notify ,)String  destDomainId )*
     */
    public void notify(net.es.oscars.api.soap.gen.v06.InterDomainEventContent notify06) throws OSCARSServiceException {
        throw new RuntimeException ("Not yet implemented");
    }

    /* (non-Javadoc)
     * @see OSCARSInternalPortType#cancelReservation(GlobalReservationId  cancelReservation ,)String  destDomainId )*
     */
    public net.es.oscars.api.soap.gen.v06.CancelResReply
        cancelReservation(net.es.oscars.api.soap.gen.v06.CancelResContent cancelReservation06) throws OSCARSServiceException {

        net.es.oscars.api.soap.gen.v05.ForwardReply forwardReply = null;

        net.es.oscars.api.soap.gen.v05.GlobalReservationId gri05 = DataTranslator05.translate (cancelReservation06);
        forwardReply = this.forward (gri05);

        return DataTranslator05.translate (forwardReply.getCancelReservation());
    }

    /* (non-Javadoc)
     * @see OSCARSInternalPortType#teardownPath(TeardownPathContent  teardownPath ,)String  destDomainId )*
     */
    public net.es.oscars.api.soap.gen.v06.TeardownPathResponseContent
        teardownPath(net.es.oscars.api.soap.gen.v06.TeardownPathContent teardownPath06) throws OSCARSServiceException {

        net.es.oscars.api.soap.gen.v05.ForwardReply forwardReply = null;

        net.es.oscars.api.soap.gen.v05.TeardownPathContent teardownPath05 = DataTranslator05.translate (teardownPath06);
        forwardReply = this.forward (teardownPath05);

        return DataTranslator05.translate (forwardReply.getTeardownPath());
    }

    /* (non-Javadoc)
     * @see OSCARSInternalPortType#queryReservation(GlobalReservationId  queryReservation ,)String  destDomainId )*
     */
    public net.es.oscars.api.soap.gen.v06.QueryResReply
        queryReservation(net.es.oscars.api.soap.gen.v06.QueryResContent queryReservation06) throws OSCARSServiceException {

        throw new RuntimeException ("Method is not implemented");
    }

    /* (non-Javadoc)
     * @see OSCARSInternalPortType#createReservation(ResCreateContent  createReservation ,)String  destDomainId )*
     */
    public net.es.oscars.api.soap.gen.v06.CreateReply
        createReservation(net.es.oscars.api.soap.gen.v06.ResCreateContent createReservation06) throws OSCARSServiceException {

        net.es.oscars.api.soap.gen.v05.ForwardReply forwardReply = null;

        net.es.oscars.api.soap.gen.v05.ResCreateContent createReservation05 = DataTranslator05.translate (createReservation06);
        forwardReply = this.forward (createReservation05);

        return DataTranslator05.translate (forwardReply.getCreateReservation());
    }

    /* (non-Javadoc)
     * @see OSCARSInternalPortType#listReservations(ListRequest  listReservations ,)String  destDomainId )*
     */
    public net.es.oscars.api.soap.gen.v06.ListReply
        listReservations(net.es.oscars.api.soap.gen.v06.ListRequest listReservations06) throws OSCARSServiceException {

        throw new RuntimeException ("Method is not implemented");
    }
    /* (non-Javadoc)
     * @see OSCARSInternalPortType#refreshPath(RefreshPathContent  refreshPath ,)String  destDomainId )*
     */
    public net.es.oscars.api.soap.gen.v06.RefreshPathResponseContent
        refreshPath(net.es.oscars.api.soap.gen.v06.RefreshPathContent refreshPath06) throws OSCARSServiceException {

        throw new RuntimeException ("Method is not implemented");
    }

    /* (non-Javadoc)
     * @see OSCARSInternalPortType#getNetworkTopology(GetTopologyContent  getNetworkTopology ,)String  destDomainId )*
     */
    public net.es.oscars.api.soap.gen.v06.GetTopologyResponseContent
        getNetworkTopology(net.es.oscars.api.soap.gen.v06.GetTopologyContent getNetworkTopology06) throws OSCARSServiceException {

        throw new RuntimeException ("Method is not implemented");
    }

    /* (non-Javadoc)
     * @see OSCARSInternalPortType#modifyReservation(ModifyResContent  modifyReservation ,)String  destDomainId )*
     */
    public net.es.oscars.api.soap.gen.v06.ModifyResReply
        modifyReservation(net.es.oscars.api.soap.gen.v06.ModifyResContent modifyReservation06) throws OSCARSServiceException    {

        net.es.oscars.api.soap.gen.v05.ForwardReply forwardReply = null;

        net.es.oscars.api.soap.gen.v05.ModifyResContent modifyReservation05 = DataTranslator05.translate (modifyReservation06);
        forwardReply = this.forward (modifyReservation05);

        return DataTranslator05.translate (forwardReply.getModifyReservation());
    }
    
    public String getDestDomainId () {
        return this.destDomainId;
    }

    public URL getDestURL () {
        return this.url;
    }
    private net.es.oscars.api.soap.gen.v05.ForwardReply forward (net.es.oscars.api.soap.gen.v05.ForwardPayload forwardPayload05)
            throws OSCARSServiceException {
        //make sure we have a subscription
        if(!this.subscribeManager.hasSubscription(this.getDestDomainId())){
            throw new OSCARSServiceException("Unable to establish a subscription with " + this.getDestDomainId());
        }
        net.es.oscars.api.soap.gen.v05.Forward forward05 = new net.es.oscars.api.soap.gen.v05.Forward();
        forward05.setPayloadSender(this.getDestDomainId());
        forward05.setPayload(forwardPayload05);

        Object[] req = {forward05};
        Object[] res = this.client.invoke("forward",req);

        if ((res == null) || (res[0] == null)) {
            throw new OSCARSServiceException ("no response from IDC " + this.getDestDomainId() + " url= " + this.getDestURL(), "system");
        }
        net.es.oscars.api.soap.gen.v05.ForwardReply response = (net.es.oscars.api.soap.gen.v05.ForwardReply) res[0];

        return response;
    }

    private net.es.oscars.api.soap.gen.v05.ForwardReply
        forward (net.es.oscars.api.soap.gen.v05.ModifyResContent modifyResContent05) throws OSCARSServiceException {

        net.es.oscars.api.soap.gen.v05.ForwardPayload forwardPayload05 = new net.es.oscars.api.soap.gen.v05.ForwardPayload();

        forwardPayload05.setModifyReservation (modifyResContent05);
        forwardPayload05.setContentType(ForwardTypes.MODIFY_RESERVATION);

        return this.forward (forwardPayload05);
    }

    private net.es.oscars.api.soap.gen.v05.ForwardReply
        forward (net.es.oscars.api.soap.gen.v05.ResCreateContent resCreateContent05) throws OSCARSServiceException {

        net.es.oscars.api.soap.gen.v05.ForwardPayload forwardPayload05 = new net.es.oscars.api.soap.gen.v05.ForwardPayload();

        forwardPayload05.setCreateReservation(resCreateContent05);
        forwardPayload05.setContentType(ForwardTypes.CREATE_RESERVATION);

        return this.forward (forwardPayload05);
    }

    private net.es.oscars.api.soap.gen.v05.ForwardReply
        forward (net.es.oscars.api.soap.gen.v05.TeardownPathContent teardownPathContent05) throws OSCARSServiceException {

        net.es.oscars.api.soap.gen.v05.ForwardPayload forwardPayload05 = new net.es.oscars.api.soap.gen.v05.ForwardPayload();

        forwardPayload05.setTeardownPath (teardownPathContent05);
        forwardPayload05.setContentType(ForwardTypes.TEARDOWN_PATH);

        return this.forward (forwardPayload05);
    }

    private net.es.oscars.api.soap.gen.v05.ForwardReply
        forward (net.es.oscars.api.soap.gen.v05.CreatePathContent createPathContent05) throws OSCARSServiceException {

        net.es.oscars.api.soap.gen.v05.ForwardPayload forwardPayload05 = new net.es.oscars.api.soap.gen.v05.ForwardPayload();

        forwardPayload05.setCreatePath (createPathContent05);
        forwardPayload05.setContentType(ForwardTypes.CREATE_PATH);

        return this.forward (forwardPayload05);
    }

    private net.es.oscars.api.soap.gen.v05.ForwardReply
        forward (net.es.oscars.api.soap.gen.v05.GlobalReservationId gri05) throws OSCARSServiceException {

        net.es.oscars.api.soap.gen.v05.ForwardPayload forwardPayload05 = new net.es.oscars.api.soap.gen.v05.ForwardPayload();

        forwardPayload05.setCancelReservation (gri05);
        forwardPayload05.setContentType(ForwardTypes.CANCEL_RESERVATION);

        return this.forward (forwardPayload05);
    }
}
