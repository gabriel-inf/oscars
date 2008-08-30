package net.es.oscars.bss.topology;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.*;
import net.es.oscars.bss.*;


/**
 * This class contains static helper methods for initializing
 * OSCARS topology database objects
 * @author Evangelos Chaniotakis (haniotak@es.net)
 */
public class TopologyUtil {
    public final static int DOMAIN_URN = 4;
    public final static int NODE_URN = 5;
    public final static int PORT_URN = 6;
    public final static int LINK_URN = 7;


    /**
     * Constructs the local topology identifier from a fully qualified
     * one
     * @param topoId String containing a fully qualified topology identifier
     * @param objType String containing one of: Domain,Node,Port,Link
     * @return String containing a local topology identifier
     */
    public static String getLSTI(String topoId, String objType) {
        String prefix = "urn:ogf:network:";
        if (topoId == null || topoId == "") {
            return null;
        }

        if (!topoId.startsWith(prefix)) {
            return (topoId);
        }

        // throw away the prefix(es)
        topoId = topoId.replaceAll(prefix, "");

        String[] parts = null;
        String splitter = ":";
        int offset;

        if (objType.equals("Domain")) {
            offset = 0;
            splitter = "domain=";
        } else if (objType.equals("Node")) {
            splitter = "node=";
            offset = 1;
        } else if (objType.equals("Port")) {
            splitter = "port=";
            offset = 2;
        } else if (objType.equals("Link")) {
            splitter = "link=";
            offset = 3;
        } else {
            return null;
        }

        boolean longFormat = true;
        parts = topoId.split(splitter);

        if (parts.length == 1) {
            // this means our string was NOT split; so it should look like
            // domainId:nodeId:portId:linkId (or a substring thereof)
            // so re-split, on ":" now and hope for the best...
            parts = topoId.split(":");
            longFormat = false;
        }

        if (parts.length == 1) {
            // haven't managed to split the string yet by anything
            // so return all of it
            topoId = parts[0].replaceAll("domain=", "");
            topoId = topoId.replaceAll("node=", "");
            topoId = topoId.replaceAll("port=", "");
            topoId = topoId.replaceAll("link=", "");
            return topoId;
        }

        // so we managed to split it
        if (longFormat) {
            if (parts.length == 2) {
                parts = parts[1].split(":");
                return parts[0];
            } else {
                // something went wrong
                return null;
            }
        } else {
            if (parts.length > offset) {
                return parts[offset];
            } else {
                // something went wrong
                return null;
            }
        }
    }



    /**
     * Utility method to convert a string representation of bandwidth
     * to a Long. We expect it to look like one of the following:
     * 10Mbps / 10M / 10000000 / 10000000bps
     * If we can't parse it, we return 0L.
     *
     * @param strBandwidth the bandwidth as a string
     * @return bandwidth the bandwidth as a Long
     */

    public static Long understandBandwidth(String strBandwidth) {
        Matcher matcher = null;
        Long bandwidth =  new Long(0L);
//        this.log.debug("finding bandwidth for [" + strBandwidth + "]");

        // TODO: compile only once?
        Pattern pattern = Pattern.compile("(\\d+)(G|M|K)(bps)?");
        matcher = pattern.matcher(strBandwidth);

        if (!matcher.matches()) {
            pattern = Pattern.compile("(\\d+)(bps)?");
            matcher = pattern.matcher(strBandwidth);
            if (matcher.matches()) {
                bandwidth = new Long(matcher.group(1));
                return bandwidth;
            } else {
                return new Long(0L);
            }
        }



        Long sig = new Long(matcher.group(1));
        String mag = matcher.group(2);

        if (mag.equals("G")) {
            bandwidth = new Long(sig.longValue() * 1000000000L);
        } else if (mag.equals("M")) {
            bandwidth = new Long(sig.longValue() * 1000000L);
        } else if (mag.equals("K")) {
            bandwidth = new Long(sig.longValue() * 1000L);
        }
/*
        this.log.debug("bandwidth for [" + strBandwidth + "] is: [" +
            bandwidth.toString() + "]");
*/
        return bandwidth;
    }

    /**
     * Gets a Domain object from a topology identifier
     *
     * @param topoIdent the topology identifier
     * @param dbname the bss database name (typically bss)
     * @return the domain object
     * @throws BSSException
     */
    public static Domain getDomain(String topoIdent, String dbname) throws BSSException {
        Hashtable<String, String> results = URNParser.parseTopoIdent(topoIdent);
        if (!results.get("type").equals("domain")) {
            throw new BSSException("Invalid topoIdent type; need domain, is: "+results.get("type"));
        }
        DomainDAO domDAO = new DomainDAO(dbname);
        Domain domain = domDAO.fromTopologyIdent(results.get("domainId"));
        if (domain == null) {
            throw new BSSException("Domain not found for domain id: "+results.get("domainId"));
        }
        return domain;
    }

    /**
     * Gets a Node object from a topology identifier
     *
     * @param topoIdent the topology identifier
     * @param dbname the bss database name (typically bss)
     * @return the node object
     * @throws BSSException
     */
    public static Node getNode(String topoIdent, String dbname) throws BSSException {
        Hashtable<String, String> results = URNParser.parseTopoIdent(topoIdent);
        if (!results.get("type").equals("node")) {
            throw new BSSException("Invalid topoIdent type; need node, is: "+results.get("type"));
        }
        DomainDAO domDAO = new DomainDAO(dbname);
        NodeDAO nodeDAO = new NodeDAO(dbname);
        Domain domain = domDAO.fromTopologyIdent(results.get("domainId"));
        if (domain == null) {
            throw new BSSException("Domain not found for domain id: "+results.get("domainId"));
        }
        Node node = nodeDAO.fromTopologyIdent(results.get("nodeId"), domain);
        if (node == null) {
            throw new BSSException("Node not found for node id: "+results.get("nodeId"));
        }
        return node;
    }

    /**
     * Gets a Port object from a topology identifier
     *
     * @param topoIdent the topology identifier
     * @param dbname the bss database name (typically bss)
     * @return the port object
     * @throws BSSException
     */
    public static Port getPort(String topoIdent, String dbname) throws BSSException {
        Hashtable<String, String> results = URNParser.parseTopoIdent(topoIdent);
        if (!results.get("type").equals("port")) {
            throw new BSSException("Invalid topoIdent type; need port, is: "+results.get("type"));
        }
        DomainDAO domDAO = new DomainDAO(dbname);
        NodeDAO nodeDAO = new NodeDAO(dbname);
        PortDAO portDAO = new PortDAO(dbname);
        Domain domain = domDAO.fromTopologyIdent(results.get("domainId"));
        if (domain == null) {
            throw new BSSException("Domain not found for domain id: "+results.get("domainId"));
        }
        Node node = nodeDAO.fromTopologyIdent(results.get("nodeId"), domain);
        if (node == null) {
            throw new BSSException("Node not found for node id: "+results.get("nodeId"));
        }
        Port port = portDAO.fromTopologyIdent(results.get("portId"), node);
        if (port == null) {
            throw new BSSException("Port not found for port id: "+results.get("portId"));
        }
        return port;

    }

    /**
     * Gets a Link object from a topology identifier
     *
     * @param topoIdent the topology identifier
     * @param dbname the bss database name (typically bss)
     * @return the link object
     * @throws BSSException
     */
    public static Link getLink(String topoIdent, String dbname) throws BSSException {
        Hashtable<String, String> results = URNParser.parseTopoIdent(topoIdent);
        if (!results.get("type").equals("link")) {
            throw new BSSException("Invalid topoIdent type; need link, is: "+results.get("type"));
        }
        DomainDAO domDAO = new DomainDAO(dbname);
        NodeDAO nodeDAO = new NodeDAO(dbname);
        PortDAO portDAO = new PortDAO(dbname);
        LinkDAO linkDAO = new LinkDAO(dbname);
        Domain domain = domDAO.fromTopologyIdent(results.get("domainId"));
        if (domain == null) {
            throw new BSSException("Domain not found for domain id: ["+results.get("domainId")+"], fqti:"+topoIdent);
        }
        Node node = nodeDAO.fromTopologyIdent(results.get("nodeId"), domain);
        if (node == null) {
            throw new BSSException("Node not found for node id: ["+results.get("nodeId")+"], fqti:"+topoIdent);
        }
        Port port = portDAO.fromTopologyIdent(results.get("portId"), node);
        if (port == null) {
            throw new BSSException("Port not found for port id: ["+results.get("portId")+"], fqti:"+topoIdent);
        }
        Link link = linkDAO.fromTopologyIdent(results.get("linkId"), port);
        if (link == null) {
            throw new BSSException("Link not found for link id: ["+results.get("linkId")+"], fqti:"+topoIdent);
        }
        return link;
    }




    /**
     * Checks to see if a hop id is a topology identifier, or an IP address.
     *
     * @param hopId string with hop id
     * @return boolean indicating whether hop is a topology identifier
     */
    public static boolean isTopologyIdentifier(String hopId) {
        if (hopId == null) { return false; }
        // assume an IPv6 address
        if (hopId.matches(".*::.*")) { return false; }
        String[] componentList = hopId.split(":");
        // if contains no colons, assume not a topology id
        if (componentList.length == 1) { return false; }
        // if seven sections delimited by single colons, a topology ident
        if (componentList.length == 7) { return true; }
        // TODO:  test for fully qualified link in new format
        return false;
    }

    /**
     * Returns the type (domain, node, port, or link) of the given urn
     *
     * @param urn the URN with the type to be determined
     * @return the type of the URN. corresponds to the constants in this class.
     */
     public static int getURNType(String urn){
        if(urn == null){
            return 0;
        }

        return urn.split(":").length;
     }

     /**
     * Returns the domain id of the given link
     *
     * @param urn the URN with the domain id to be extracted
     * @return the domain ID in the URN
     */
     public static String getURNDomainId(String urn){
        String[] componentList = urn.split(":");
        if(componentList.length < 4){
            return null;
        }
        return componentList[3].replaceAll("domain=", "");
     }
}
