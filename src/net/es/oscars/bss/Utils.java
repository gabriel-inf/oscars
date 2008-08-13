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
     * @param interDomain boolean for intra or interdomain path
     * @return pathStr converted path
     */
    public String pathToString(Path path, boolean interDomain) {

        Ipaddr ipaddr = null;
        String nodeName = null;
        String param = null;
        PathElem pathElem = null;
        int ctr = 0;

        // TODO:  more null checks may be necessary
        StringBuilder sb = new StringBuilder();
        if (!interDomain) {
            pathElem  = path.getPathElem();
        } else {
            pathElem = path.getInterPathElem();
        }
        int i = 0;
        while (pathElem != null) {
            ctr++;
            Link link = pathElem.getLink();
            if(i != 0){
                sb.append("\n");
            }
            // if layer 2, send back topology identifier
            if (path.getLayer2Data() != null) {
                String fqti = link.getFQTI();
                sb.append(fqti);
            // otherwise, send back host name/IP address pair
            } else {
                nodeName = link.getPort().getNode().getTopologyIdent();
                ipaddr = link.getValidIpaddr();
                if ((ipaddr == null) || (ipaddr.getIP() == null)) {
                    sb.append("*Out of date IP*");
                } else {
                    sb.append(ipaddr.getIP());
                }
            }
            pathElem = pathElem.getNextElem();
            i++;
        }
        // in this case, all hops are local
        if (interDomain && (ctr == 2)) {
            return "";
        // internal path has not been set up
        // NOTE:  this depends on the current implementation having one
        //        hop in the path from when the reservation has been in
        //        the ACCEPTED state, but the path has not been or may never
        //        be set up.
        } else if (!interDomain && (ctr == 1)) {
            return "";
        }
        String pathStr = sb.toString();
        return pathStr;
    }

    /**
     * Gets VLAN tag given path.  Assumes just one VLAN tag in path for now.
     *
     * @param path Path with reservation's page
     * @return vlanTag string with VLAN tag, if any
     */
    public String getVlanTag(Path path) {
        String vlanTag = null;
        PathElem pathElem = path.getPathElem();
        while (pathElem != null) {
            if (pathElem.getDescription() != null) {
                if (pathElem.getDescription().equals("ingress")) {
                    vlanTag = pathElem.getLinkDescr();
                    break;
                }
            }
            pathElem = pathElem.getNextElem();
        }
        return vlanTag;
    }

    /**
     * String joiner
     * @param s a Collection of objects to join (uses toString())
     * @param delimiter the delimiter
     * @param quote a string to prefix each object with (null for none)
     * @param unquote a string to postfix each object with (null for none)
     * @return joined the objects, quoted & unquoted, joined by the delimiter
     */
    public static String join(Collection s, String delimiter, String quote, String unquote) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        Iterator iter = s.iterator();
        while (iter.hasNext()) {
            buffer.append(quote);
            buffer.append(iter.next());
            buffer.append(unquote);
            if (iter.hasNext()) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }
}
