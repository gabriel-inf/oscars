package net.es.oscars.nsibridge.prov;

import net.es.nsi.lib.client.config.ClientConfig;
import net.es.nsi.lib.client.util.ClientUtil;
import net.es.nsi.lib.soap.gen.nsi_2_0_r117.connection.requester.ConnectionRequesterPort;
import net.es.oscars.nsibridge.beans.db.*;
import net.es.oscars.nsibridge.config.HttpConfig;
import net.es.oscars.nsibridge.config.RequestersConfig;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.nsa.JsonNsaConfigProvider;
import net.es.oscars.nsibridge.config.nsa.NsaConfig;
import net.es.oscars.nsibridge.config.nsa.NsaConfigProvider;
import net.es.oscars.nsibridge.oscars.OscarsStates;
import net.es.nsi.lib.soap.gen.nsi_2_0_r117.connection.ifce.ServiceException;
import net.es.nsi.lib.soap.gen.nsi_2_0_r117.framework.headers.CommonHeaderType;
import net.es.nsi.lib.soap.gen.nsi_2_0_r117.framework.types.ServiceExceptionType;
import org.apache.commons.io.FileUtils;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class NSI_Util {

    private static final Logger log = Logger.getLogger(NSI_Util.class);


    public static void makeNewStateMachines(String connId) throws ServiceException {
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        if (!smh.hasStateMachines(connId)) {
            smh.makeStateMachines(connId);
        }

    }


    public static boolean needNewOscarsResv(String connId) throws ServiceException {
        ConnectionRecord cr = DB_Util.getConnectionRecord(connId);
        OscarsStatusRecord or = cr.getOscarsStatusRecord();
        if (or == null) {
            return true;

        } else {
            if (or.getStatus().equals(OscarsStates.CANCELLED)) {
                return true;
            } else if (or.getStatus().equals(OscarsStates.FAILED)) {
                return true;
            } else if (or.getStatus().equals(OscarsStates.UNKNOWN)) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static ServiceExceptionType makeServiceException(String connectionId, String correlationId) throws ServiceException  {
        ExceptionRecord er = DB_Util.getExceptionRecord(connectionId, correlationId);
        if (er == null) {
            throw new ServiceException("could not locate exception record for connId: "+connectionId+" corrId: "+correlationId);
        }

        NsaConfig nsaCfg = SpringContext.getInstance().getContext().getBean("nsaConfigProvider", NsaConfigProvider.class).getConfig("local");

        ServiceExceptionType st = new ServiceExceptionType();
        st.setText(er.getExceptionString());
        st.setNsaId(nsaCfg.getNsaId());
        st.setServiceType(nsaCfg.getServiceType());
        st.setErrorId(er.getErrorId().toString());
        st.setConnectionId(connectionId);
        return st;
    }

    public static void prepareRequesterClients() {
        ApplicationContext ax = SpringContext.getInstance().getContext();


        RequestersConfig rc = SpringContext.getInstance().getContext().getBean("requestersConfig", RequestersConfig.class);
        if (rc == null) {
            log.error("could not get requester config");
        }

        for (String requesterUrl : rc.getRequesters().keySet()) {
            log.debug("attempting to get client for "+requesterUrl);
            ClientConfig cc = rc.getClientConfig(requesterUrl);
            if (cc == null) {
                log.error("could not get client config for requester URL "+requesterUrl);
            } else {
                log.debug("client bus config for "+requesterUrl+" at "+cc.getBusConfigPath());
                File f = new File(cc.getBusConfigPath());
                try {
                    String busConfig = FileUtils.readFileToString(f);
                    log.debug(busConfig);
                } catch (IOException e) {
                    log.error(e);
                }
            }

            ConnectionRequesterPort port = ClientUtil.getInstance().getRequesterPort(requesterUrl, cc);
            Client client = ClientProxy.getClient(port);
            HTTPConduit conduit = (HTTPConduit) client.getConduit();
            TLSClientParameters params = conduit.getTlsClientParameters();
            log.debug("loaded config for requester URL " + requesterUrl);
            if (params != null) {
                log.debug("have TLS params");
            }

        }

    }




    public static CommonHeaderType makeNsiOutgoingHeader(String requesterNsa) {
        CommonHeaderType ht = new CommonHeaderType();
        ApplicationContext ax = SpringContext.getInstance().getContext();
        NsaConfig cfg = ax.getBean("nsaConfigProvider", JsonNsaConfigProvider.class).getConfig("local");


        ht.setCorrelationId("urn:uuid:"+UUID.randomUUID().toString());
        ht.setProtocolVersion(cfg.getProtocolVersion());
        ht.setProviderNSA(cfg.getNsaId());
        ht.setRequesterNSA(requesterNsa);
        return ht;
    }

    public static CommonHeaderType makeNsiOutgoingHeader(CommonHeaderType ph) {

        CommonHeaderType ht = makeNsiOutgoingHeader(ph.getRequesterNSA());
        ht.setCorrelationId(ph.getCorrelationId());

        SpringContext sc = SpringContext.getInstance();
        ApplicationContext ax = sc.getContext();
        HttpConfig hc = ax.getBean("httpConfig", HttpConfig.class);

        if (hc.getProxyUrl() != null && !hc.getProxyUrl().isEmpty()) {
            ht.setReplyTo(hc.getProxyUrl()+"/ConnectionService");
        } else {
            ht.setReplyTo(hc.getUrl()+"/ConnectionService");
        }


        return ht;
    }


    public static void isConnectionOK(String connectionId) throws ServiceException {

        ConnectionRecord cr = DB_Util.getConnectionRecord(connectionId);


        RequestHolder rh = RequestHolder.getInstance();
        if (rh == null) {
            throw new ServiceException("no requestHolder");
        }
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        if (smh == null) {
            throw new ServiceException("no stateMachineHolder");

        }
        if (!smh.hasStateMachines(connectionId)) {
            throw new ServiceException("no stateMachines for "+connectionId);
        }

    }

}
