package net.es.oscars.pathfinder.db.util.vendor.jnx;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import org.jdom.*;
import org.jdom.xpath.*;

import org.apache.log4j.*;

import net.es.oscars.ConfigFinder;
import net.es.oscars.PropHandler;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.vendor.jnx.JnxConnection;
import net.es.oscars.pss.vendor.jnx.JnxReplyHandler;
import net.es.oscars.pss.vendor.jnx.TemplateHandler;
import net.es.oscars.bss.topology.*;

/**
 * JnxShowRoute finds outgoing interface from a Juniper node to a destination.
 *
 * @author David Robertson
 */
public class JnxShowRoute {

    private Properties props;
    private TemplateHandler th;
    private Logger log;

    public JnxShowRoute() {
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("pss.jnx", true);
        this.th = new TemplateHandler();
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * Gets the topology identifier of the outgoing port given by show route.
     *
     * @param node string with node identifier (assumed to be a host name)
     * @param table string with inet type
     * @param dest string with destination of show route
     * @return portIdent string with port topology identifier
     * @throws PSSException
     */
    public String showRoute(String node, String table, String dest)
            throws PSSException {

        this.log.debug("showRoute.start");
        String portIdent = null;
        try {
            HashMap<String, String> hm = new HashMap<String, String>();
            JnxConnection conn = new JnxConnection();
            hm.put("table", table);
            hm.put("destination", dest);
            conn.setupLogin(node, hm);
            conn.createSSHConnection(hm);
            String fname = ConfigFinder.getInstance().find(ConfigFinder.PSS_DIR,
                               this.props.getProperty("showRouteL3Template"));
            Document doc = this.th.fillTemplate(hm, null, fname);
            conn.sendCommand(doc);
            doc = conn.readResponse();
            portIdent = this.parseResponse(doc);
            conn.shutDown();
        } catch (IOException ex) {
            throw new PSSException(ex.getMessage());
        } catch (JDOMException ex) {
            throw new PSSException(ex.getMessage());
        }
        if (portIdent == null) {
            throw new PSSException("Cannot find outgoing port with show route");
        }
        this.log.debug("showRoute.finish");
        // just return the port name, not the subifce
        String[] portParts = portIdent.split("\\.");
        portIdent = portParts[0];
        return portIdent;
    }

    /**
     * Parse the DOM response to find the outgoing interface.
     *
     * @param doc XML document with response from server
     * @return String with topology ident of outgoing port
     * @throws IOException
     * @throws JDOMException
     */
    private String parseResponse(Document doc)
            throws IOException, JDOMException {

        this.log.debug("parseResponse.start");
        List entryList = JnxReplyHandler.getElements(doc, "rt-entry");
        boolean activeXface = false;
        for (Iterator i = entryList.iterator(); i.hasNext();) {
            Element entry = (Element) i.next();
            List entryChildren = entry.getChildren();
            for (Iterator j = entryChildren.iterator(); j.hasNext();) {
                Element e = (Element) j.next();
                if (e.getName().equals("active-tag")) {
                    if (e.getText().equals("*")) {
                        activeXface = true;
                    }
                } else if (e.getName().equals("nh")) {
                    if (!activeXface) {
                        continue;
                    }
                    List children = e.getChildren();
                    for (Iterator k = children.iterator(); k.hasNext();) {
                        Element childElem = (Element) k.next();
                        if (childElem.getName().equals("via")) {
                            this.log.debug("parseResponse.found " +
                                          childElem.getText());
                            return childElem.getText();
                        }
                    }
                }
            }
        }
        this.log.debug("parseResponse.fail");
        return null;
    }
}
