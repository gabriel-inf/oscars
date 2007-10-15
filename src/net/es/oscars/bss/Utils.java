package net.es.oscars.bss;

import java.util.*;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.bss.topology.*;

/**
 * @author David Robertson (dwrobertson@lbl.gov)
 *
 * This class contains utility methods for use by the reservation manager.
 */
public class Utils {
    private String dbname;
    private Logger log;

    public Utils(String dbname) {
        this.dbname = dbname;
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * Converts Hibernate path to a series of identifiers.  Used by servlets.
     *
     * @param path path to convert to string
     * @return pathStr converted path
     */
    public String pathToString(Path path) {

        Ipaddr ipaddr = null;
        String nodeName = null;

        StringBuilder sb = new StringBuilder();
        PathElem pathElem  = path.getPathElem();
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        while (pathElem != null) {
            Link link = pathElem.getLink();
            // if layer 2, send back topology identifier
            if (path.getLayer2Data() != null) {
                Port port = link.getPort();
                Node node = port.getNode();
                Domain domain = node.getDomain();
                String fqn = "urn:ogf:network:" +
                     domain.getTopologyIdent() + ":" +
                     node.getTopologyIdent() + ":" +
                     port.getTopologyIdent() + ":" +
                     link.getTopologyIdent();
                sb.append(fqn + ", ");
            // otherwise, send back host name/IP address pair
            } else {
                nodeName = link.getPort().getNode().getTopologyIdent();
                ipaddr = ipaddrDAO.fromLink(link);
                sb.append(nodeName + ": " + ipaddr.getIP() + ", ");
            }
            pathElem = pathElem.getNextElem();
        }
        String pathStr = sb.toString();
        return pathStr.substring(0, pathStr.length() - 2);
    }
}
