package net.es.oscars.nsibridge.soap.impl;

import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.nsibridge.config.OscarsStubConfig;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.oscars.OscarsProxy;
import org.apache.cxf.binding.soap.interceptor.SoapHeaderInterceptor;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.log4j.Logger;


import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Map;

public class OscarsHttpBasicAuthNInInterceptor extends SoapHeaderInterceptor {

    protected static final Logger log = Logger.getLogger(OscarsHttpBasicAuthNInInterceptor.class);
    private static OscarsHttpBasicAuthNInInterceptor instance;
    private OscarsHttpBasicAuthNInInterceptor() {}
    public static OscarsHttpBasicAuthNInInterceptor getInstance() {
        if (instance == null) instance = new OscarsHttpBasicAuthNInInterceptor();
        return instance;
    }



    @Override
    public void handleMessage(Message message) throws Fault {
        OscarsSecurityContext.getInstance().setSubjectAttributes(null);
        // This is set by CXF
        AuthorizationPolicy policy = message.get(AuthorizationPolicy.class);
        // If the policy is not set, the user did not specify
        // credentials. A 401 is sent to the client to indicate
        // that authentication is required
        if (policy == null) {
            log.info("no HTTP Basic credentials set from user");
            sendErrorResponse(message, HttpURLConnection.HTTP_UNAUTHORIZED);
            return;
        }
        String username = policy.getUserName();
        String password = policy.getPassword();
        if (!checkLogin(username, password)) {
            log.warn("Invalid username or password");
            sendErrorResponse(message, HttpURLConnection.HTTP_FORBIDDEN);
        }
    }


    private boolean checkLogin(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        log.debug("checking login for "+username);
        OscarsStubConfig oscarsStubConfig = SpringContext.getInstance().getContext().getBean("oscarsStubConfig", OscarsStubConfig.class);
        if (oscarsStubConfig.isStub()) {
            OscarsSecurityContext.getInstance().setSubjectAttributes(new SubjectAttributes());

            return true;
        }

        try {
            MessagePropertiesType mp = OscarsProxy.getInstance().makeMessageProps();
            SubjectAttributes attrs = OscarsProxy.getInstance().sendAuthNLoginRequest(mp, username, password);
            if (attrs == null || attrs.getSubjectAttribute() == null || attrs.getSubjectAttribute().isEmpty()) {
                log.info("no user attributes found");
                return false;
            }
            OscarsSecurityContext.getInstance().setSubjectAttributes(attrs);

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }

        return true;
    }


    private void sendErrorResponse(Message message, int responseCode) {
        Message outMessage = getOutMessage(message);
        outMessage.put(Message.RESPONSE_CODE, responseCode);
        // Set the response headers
        Map responseHeaders = (Map) message.get(Message.PROTOCOL_HEADERS);
        if (responseHeaders != null) {
            responseHeaders.put("WWW-Authenticate", Arrays.asList(new String[]{"Basic realm=realm"}));
            responseHeaders.put("Content-length", Arrays.asList(new String[]{"0"}));
        }
        message.getInterceptorChain().abort();
        try {
            getConduit(message).prepare(outMessage);
            close(outMessage);
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
    }

    private Message getOutMessage(Message inMessage) {
        Exchange exchange = inMessage.getExchange();
        Message outMessage = exchange.getOutMessage();
        if (outMessage == null) {
            Endpoint endpoint = exchange.get(Endpoint.class);
            outMessage = endpoint.getBinding().createMessage();
            exchange.setOutMessage(outMessage);
        }
        outMessage.putAll(inMessage);
        return outMessage;
    }

    private Conduit getConduit(Message inMessage) throws IOException {
        Exchange exchange = inMessage.getExchange();
        EndpointReferenceType target = exchange.get(EndpointReferenceType.class);
        Conduit conduit = exchange.getDestination().getBackChannel(inMessage, null, target);
        exchange.setConduit(conduit);
        return conduit;
    }

    private void close(Message outMessage) throws IOException {
        OutputStream os = outMessage.getContent(OutputStream.class);
        os.flush();
        os.close();
    }

}