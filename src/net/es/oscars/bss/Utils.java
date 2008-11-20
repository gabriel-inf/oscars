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
     * Converts data associated with a Hibernate path to a series of strings.
     *
     * @param path path to convert to string
     * @return pathDataStr path data in string format
     */
    public String pathDataToString(Path path) {
        StringBuilder sb =  new StringBuilder();
        if (path.getPathSetupMode() != null) {
            sb.append("path setup mode: " + path.getPathSetupMode() + "\n");
        }
        Layer2Data layer2Data = path.getLayer2Data();
        if (layer2Data != null) {
            sb.append("layer: 2\n");
            if (layer2Data.getSrcEndpoint() != null) {
                sb.append("source endpoint: " +
                      layer2Data.getSrcEndpoint() + "\n");
            }
            if (layer2Data.getDestEndpoint() != null) {
                sb.append("dest endpoint: " +
                          layer2Data.getDestEndpoint() + "\n");
            }
            List<PathElem> pathElems = path.getPathElems();
            if (!pathElems.isEmpty()) {
                String linkDescr = pathElems.get(0).getLinkDescr();
                if (linkDescr != null) {
                    sb.append("VLAN tag: " + linkDescr + "\n");
                }
            }
        }
        Layer3Data layer3Data = path.getLayer3Data();
        if (layer3Data != null) {
            sb.append("layer: 3\n");
            if (layer3Data.getSrcHost() != null) {
                sb.append("source host: " + layer3Data.getSrcHost() + "\n");
            }
            if (layer3Data.getDestHost() != null) {
                sb.append("dest host: " + layer3Data.getDestHost() + "\n");
            }
            if (layer3Data.getProtocol() != null) {
                sb.append("protocol: " + layer3Data.getProtocol() + "\n");
            }
            if ((layer3Data.getSrcIpPort() != null) &&
                (layer3Data.getSrcIpPort() != 0)) {
                sb.append("src IP port: " + layer3Data.getSrcIpPort() + "\n");
            }
            if ((layer3Data.getDestIpPort() != null) &&
                (layer3Data.getDestIpPort() != 0)) {
                sb.append("dest IP port: " +
                          layer3Data.getDestIpPort() + "\n");
            }
            if (layer3Data.getDscp() != null) {
                sb.append("dscp: " +  layer3Data.getDscp() + "\n");
            }
        }
        MPLSData mplsData = path.getMplsData();
        if (mplsData != null) {
            if (mplsData.getBurstLimit() != null) {
                sb.append("burst limit: " + mplsData.getBurstLimit() + "\n");
            }
            if (mplsData.getLspClass() != null) {
                sb.append("LSP class: " + mplsData.getLspClass() + "\n");
            }
        }
        return sb.toString();
    }

    /**
     * Converts Hibernate path to a series of identifier strings.
     *
     * @param path path to convert to string
     * @param interDomain boolean for intra or interdomain path
     * @return pathStr converted path
     */
    public String pathToString(Path path, boolean interDomain) {

        Ipaddr ipaddr = null;
        String nodeName = null;
        String param = null;

        // TODO:  more null checks may be necessary
        StringBuilder sb = new StringBuilder();
        List<PathElem> pathElems = path.getPathElems();
        int sz = pathElems.size();
        int i = 0;
        for (PathElem pathElem: pathElems) {
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
            i++;
        }
        // in this case, all hops are local
        if (interDomain && (sz == 2)) {
            return "";
        // internal path has not been set up
        // NOTE:  this depends on the current implementation sometimes having
        //        one hop in the path from when the reservation has been in
        //        the ACCEPTED state, but the path has not been or may never
        //        be set up.
        } else if (!interDomain && (sz == 1)) {
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
        List<PathElem> pathElems = path.getPathElems();
        for (PathElem pathElem: pathElems) {
            if (pathElem.getDescription() != null) {
                if (pathElem.getDescription().equals("ingress")) {
                    vlanTag = pathElem.getLinkDescr();
                    break;
                }
            }
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
