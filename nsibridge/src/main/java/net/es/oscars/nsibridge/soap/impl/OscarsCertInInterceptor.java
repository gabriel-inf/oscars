package net.es.oscars.nsibridge.soap.impl;


import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.nsibridge.config.HttpConfig;
import net.es.oscars.nsibridge.config.OscarsStubConfig;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.oscars.OscarsProxy;
import net.es.oscars.nsibridge.oscars.OscarsUtil;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.security.transport.TLSSessionInfo;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.http.UntrustedURLConnectionIOException;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;


public class OscarsCertInInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final Logger log = Logger.getLogger(OscarsCertInInterceptor.class);

    private static OscarsCertInInterceptor instance;
    public static OscarsCertInInterceptor getInstance() {
        if (instance == null) {
            instance = new OscarsCertInInterceptor();
        }
        return instance;
    }

    private OscarsCertInInterceptor() {
        super(Phase.PRE_STREAM);
        OscarsStubConfig oscarsStubConfig = SpringContext.getInstance().getContext().getBean("oscarsStubConfig", OscarsStubConfig.class);
        if (oscarsStubConfig.isStub()) {
            OscarsSecurityContext.getInstance().setSubjectAttributes(new SubjectAttributes());
        }
    }

    private boolean httpBasicFailover = false;

    private final String idnHeader = "SSL_CLIENT_I_DN";
    private final String sdnHeader = "SSL_CLIENT_S_DN";


    public void handleMessage(Message message) throws Fault {
        OscarsSecurityContext.getInstance().setSubjectAttributes(null);

        HttpConfig config = SpringContext.getInstance().getContext().getBean("httpConfig", HttpConfig.class);
        String trustedProxy = config.getTrustedSSLProxy();
        if (trustedProxy  != null && !trustedProxy.isEmpty() ) {
            log.debug("trusting an SSL proxy to set SSL headers");
            trustedProxy = trustedProxy.toLowerCase().trim();


            HttpServletRequest httpRequest = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);
            String remoteHost = httpRequest.getRemoteHost().toLowerCase().trim();
            log.debug("remote: "+remoteHost+" trusted: "+trustedProxy);

            if (!remoteHost.equals(trustedProxy)) {
                UntrustedURLConnectionIOException ex = new UntrustedURLConnectionIOException("incoming request not from trusted SSL proxy");
                throw new Fault(ex);
            }

            String issuerDN = httpRequest.getHeader(idnHeader);
            String subjectDN = httpRequest.getHeader(sdnHeader);
            if (!verifyDNs(subjectDN, issuerDN)) {
                if (!httpBasicFailover) {
                    UntrustedURLConnectionIOException ex = new UntrustedURLConnectionIOException("untrusted subject / issuer DN headers set by SSL proxy");
                    throw new Fault(ex);
                } else {
                    log.debug("Client cert auth failed, failing over to HTTP-Basic");
                }
            }

        } else {

            try {
                TLSSessionInfo tlsInfo = message.get(TLSSessionInfo.class);
                final Certificate[] certs = tlsInfo.getPeerCertificates();
                if (certs == null || certs.length == 0) {
                    throw new UntrustedURLConnectionIOException("No client certificates were found");
                } else {
                    X509Certificate[] x509Certs = (X509Certificate[])certs;
                    if (!verifyCert(x509Certs[0])) {
                        throw new UntrustedURLConnectionIOException(
                            "The client certificate does not match the defined cert constraints");
                    }
                }
            } catch (UntrustedURLConnectionIOException ex) {
                if (!httpBasicFailover) {
                    throw new Fault(ex);
                } else {
                    log.debug("Client cert auth failed, failing over to HTTP-Basic");
                }
            }
        }



    }

    private boolean verifyCert(X509Certificate cert) {
        String certSubjectDN = cert.getSubjectDN().toString();
        String certIssuerDN = cert.getIssuerDN().toString();

        log.debug("s: "+certSubjectDN+" i: "+certIssuerDN);
        certSubjectDN  = OscarsUtil.normalizeDN(certSubjectDN);
        certIssuerDN = OscarsUtil.normalizeDN(certIssuerDN);

        log.debug("normalized: s: "+certSubjectDN+" i: "+certIssuerDN);


        try {
            MessagePropertiesType mp = OscarsProxy.getInstance().makeMessageProps();
            SubjectAttributes attrs = OscarsProxy.getInstance().sendAuthNverifyDNRequest(mp, certSubjectDN, certIssuerDN);
            if (attrs == null || attrs.getSubjectAttribute() == null || attrs.getSubjectAttribute().isEmpty()) {
                log.info("no user attributes found");
                return false;
            }
            OscarsSecurityContext.getInstance().setSubjectAttributes(attrs);

        } catch (Exception ex) {
            log.error(ex);
            return false;
        }
        return true;
    }



    private boolean verifyDNs(String subjectDN, String issuerDN) {
        log.debug("s: "+subjectDN+" i: "+issuerDN);
        issuerDN  = OscarsUtil.normalizeDN(issuerDN);
        subjectDN = OscarsUtil.normalizeDN(subjectDN);

        log.debug("normalized: s: "+subjectDN+" i: "+issuerDN);


        try {
            MessagePropertiesType mp = OscarsProxy.getInstance().makeMessageProps();
            SubjectAttributes attrs = OscarsProxy.getInstance().sendAuthNverifyDNRequest(mp, subjectDN, issuerDN);
            if (attrs == null || attrs.getSubjectAttribute() == null || attrs.getSubjectAttribute().isEmpty()) {
                log.info("no user attributes found");
                return false;
            }
            OscarsSecurityContext.getInstance().setSubjectAttributes(attrs);

        } catch (Exception ex) {
            log.error(ex);
            return false;
        }
        return true;
    }




    public boolean isHttpBasicFailover() {
        return httpBasicFailover;
    }

    public void setHttpBasicFailover(boolean httpBasicFailover) {
        this.httpBasicFailover = httpBasicFailover;
    }


}
