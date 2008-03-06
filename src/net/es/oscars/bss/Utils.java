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
        String param = null;

        // TODO:  more null checks may be necessary
        StringBuilder sb = new StringBuilder();
        PathElem pathElem  = path.getPathElem();
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        int i = 0;
        while (pathElem != null) {
            Link link = pathElem.getLink();
            if(i != 0){
                sb.append(", ");
            }
            // if layer 2, send back topology identifier
            if (path.getLayer2Data() != null) {
                String fqti = link.getFQTI();
                sb.append(fqti);
                param = pathElem.getDescription();
                if (param != null) {
                    sb.append(", desc: ["+pathElem.getDescription()+"]");
                }
                sb.append(", VLAN: ["+pathElem.getLinkDescr()+"]");
            // otherwise, send back host name/IP address pair
            } else {
                nodeName = link.getPort().getNode().getTopologyIdent();
                ipaddr = ipaddrDAO.fromLink(link);
                sb.append(nodeName + ": " + ipaddr.getIP());
            }
            pathElem = pathElem.getNextElem();
            i++;
        }
        String pathStr = sb.toString();
        return pathStr;
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
