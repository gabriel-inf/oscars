package net.es.oscars.nsibridge.soap.impl;


import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.nsibridge.config.OscarsStubConfig;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.oscars.OscarsProxy;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.security.transport.TLSSessionInfo;
import org.apache.cxf.transport.http.MessageTrustDecider;
import org.apache.cxf.transport.http.URLConnectionInfo;
import org.apache.cxf.transport.http.UntrustedURLConnectionIOException;
import org.apache.cxf.transport.https.HttpsURLConnectionInfo;
import org.apache.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
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
            subjectDN = oscarsStubConfig.getSecConfig().getUserDN();
            issuerDN = oscarsStubConfig.getSecConfig().getIssuerDN();
        }
    }

    private String subjectDN;
    private String issuerDN;

    public void handleMessage(Message message) throws Fault {


        if (isRequestor(message)) {
            try {
                HttpURLConnection connection =
                        (HttpURLConnection) message.get("http.connection");

                if (connection instanceof HttpsURLConnection) {
                    final MessageTrustDecider orig = message.get(MessageTrustDecider.class);
                    MessageTrustDecider trust = new MessageTrustDecider() {
                        public void establishTrust(String conduitName,
                                                   URLConnectionInfo connectionInfo,
                                                   Message message)
                                throws UntrustedURLConnectionIOException {
                            if (orig != null) {
                                orig.establishTrust(conduitName, connectionInfo, message);
                            }
                            HttpsURLConnectionInfo info = (HttpsURLConnectionInfo)connectionInfo;

                            if (info.getServerCertificates() == null
                                    || info.getServerCertificates().length == 0) {
                                throw new UntrustedURLConnectionIOException(
                                        "No server certificates were found"
                                );
                            } else {
                                X509Certificate[] x509Certs = (X509Certificate[])info.getServerCertificates();
                                if (!verifyCert(x509Certs[0])) {
                                    throw new UntrustedURLConnectionIOException(
                                            "The client certificate does not match the defined cert constraints"
                                    );
                                }
                            }
                        }
                    };
                    message.put(MessageTrustDecider.class, trust);
                } else {
                    throw new UntrustedURLConnectionIOException(
                            "TLS is not in use"
                    );
                }
            } catch (UntrustedURLConnectionIOException ex) {
                throw new Fault(ex);
            }
        } else {
            try {
                TLSSessionInfo tlsInfo = message.get(TLSSessionInfo.class);
                final Certificate[] certs = tlsInfo.getPeerCertificates();
                if (certs == null || certs.length == 0) {
                    throw new UntrustedURLConnectionIOException(
                            "No client certificates were found"
                    );
                } else {
                    X509Certificate[] x509Certs = (X509Certificate[])certs;
                    if (!verifyCert(x509Certs[0])) {
                        throw new UntrustedURLConnectionIOException(
                                "The client certificate does not match the defined cert constraints"
                        );
                    }
                }
            } catch (UntrustedURLConnectionIOException ex) {
                throw new Fault(ex);
            }
        }
    }

    private boolean verifyCert(X509Certificate cert) {

        log.debug("issuer: "+cert.getIssuerDN());
        log.debug("subject: "+cert.getSubjectDN());
        try {
            MessagePropertiesType mp = OscarsProxy.getInstance().makeMessageProps();
            SubjectAttributes attrs = OscarsProxy.getInstance().sendAuthNRequest(mp, cert.getSubjectDN().toString(), cert.getIssuerDN().toString());
            if (attrs == null || attrs.getSubjectAttribute() == null || attrs.getSubjectAttribute().isEmpty()) {
                log.info("no user attributes found");
                return false;
            }
            subjectDN = cert.getSubjectDN().toString();
            issuerDN = cert.getIssuerDN().toString();

        } catch (Exception ex) {
            log.error(ex);
            return false;
        }
        return true;
    }

    public String getSubjectDN() {
        return subjectDN;
    }

    public void setSubjectDN(String subjectDN) {
        this.subjectDN = subjectDN;
    }

    public String getIssuerDN() {
        return issuerDN;
    }

    public void setIssuerDN(String issuerDN) {
        this.issuerDN = issuerDN;
    }
}
