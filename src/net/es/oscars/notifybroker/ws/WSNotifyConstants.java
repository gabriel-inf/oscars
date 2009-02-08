package net.es.oscars.notifybroker.ws;

import org.jdom.Namespace;

public class WSNotifyConstants {
    /* Constants */
    public static final String WS_TOPIC_SIMPLE = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple";
    public static final String WS_TOPIC_CONCRETE= "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Concrete";
    public static final String WS_TOPIC_FULL = "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Full";
    public static final String XPATH_URI = "http://www.w3.org/TR/1999/REC-xpath-19991116";
    public static final Namespace IDC_NS = Namespace.getNamespace("http://oscars.es.net/OSCARS");
    public static final Namespace NMWG_CP = Namespace.getNamespace("http://ogf.org/schema/network/topology/ctrlPlane/20080828/");
    public static final Namespace WSA_NS = Namespace.getNamespace("http://www.w3.org/2005/08/addressing");
    public static final Namespace WSN_NS = Namespace.getNamespace("http://docs.oasis-open.org/wsn/b-2");
    public static final Namespace SOAP_NS = Namespace.getNamespace("http://www.w3.org/2003/05/soap-envelope");
}
