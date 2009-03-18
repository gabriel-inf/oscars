package net.es.oscars.pss.vendor.jnx;

import java.util.List;

import org.jdom.*;
import org.jdom.xpath.*;

/**
 * JnxReplyHandler performs general parsing tasks for reply from router.
 *
 * @author David Robertson
 */
public class JnxReplyHandler {

    public static List getElements(Document doc, String elementName)
            throws JDOMException {

        Element root = doc.getRootElement();
        // NOTE WELL: if response format changes, this won't work
        Element rpcReply = (Element) root.getChildren().get(0);
        Element firstChild = (Element) rpcReply.getChildren().get(0);
        String uri = firstChild.getNamespaceURI();
        // XPath doesn't have way to name default namespace
        Namespace ns = Namespace.getNamespace("ns", uri);
        // find all connections with status info
        XPath xpath = XPath.newInstance("//ns:" + elementName);
        xpath.addNamespace(ns);
        List elementList = xpath.selectNodes(doc);
        return elementList;
    }
}
