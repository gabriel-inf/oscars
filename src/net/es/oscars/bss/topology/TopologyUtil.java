package net.es.oscars.bss.topology;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.*;

/**
 * This class contains static helper methods for initializing
 * OSCARS topology database objects
 * @author Evangelos Chaniotakis (haniotak@es.net)
 */
public class TopologyUtil {
    /**
     * This will initialize a Domain object with all the required
     * fields filled in with placeholder data, and return it.
     * @return the new Domain object
     */
    public static Domain initDomain() {
        Domain domDB = new Domain();
        domDB.setUrl("Unknown");
        domDB.setLocal(false);
        domDB.setAbbrev("Unknown");
        domDB.setTopologyIdent("Unknown");
        domDB.setName("Unknown");
        domDB.setNodes(new HashSet());

        return domDB;
    }

    /**
     * This will initialize a Node object with all the required
     * fields filled in with placeholder data, and return it.
     * It will also associate it with its parent Domain object.
     * @param domDB the parent Domain
     * @return the new Node object
     */
    public static Node initNode(Domain domDB) {
        Node nodeDB = new Node();
        nodeDB.setValid(true);
        nodeDB.setTopologyIdent("changeme");
        nodeDB.setDomain(domDB);
        nodeDB.setPorts(new HashSet());

        return nodeDB;
    }

    /**
     * This will initialize a NodeAddress object with all the required
     * fields filled in with placeholder data, and return it.
     * It will also associate it with its parent Node object.
     * @param nodeDB the parent Node
     * @return the new NodeAddress bject
     */
    public static NodeAddress initNodeAddress(Node nodeDB) {
        NodeAddress nodeAddressDB = new NodeAddress();
        nodeAddressDB.setAddress("changeme");
        nodeAddressDB.setNode(nodeDB);

        return nodeAddressDB;
    }

    /**
     * This will initialize a Port object with all the required
     * fields filled in with placeholder data, and return it.
     * It will also associate it with its parent Node object.
     * @param nodeDB the parent Node
     * @return the new Port object
     */
    public static Port initPort(Node nodeDB) {
        Port portDB = new Port();
        portDB.setValid(true);
        portDB.setTopologyIdent("changeme");
        portDB.setCapacity(0L);
        portDB.setMaximumReservableCapacity(0L);
        portDB.setMinimumReservableCapacity(0L);
        portDB.setUnreservedCapacity(0L);
        portDB.setGranularity(0L);
        portDB.setAlias("changeme");
        portDB.setSnmpIndex(1);
        portDB.setLinks(new HashSet());
        portDB.setNode(nodeDB);

        //   nodeDB.addPort(portDB);
        return portDB;
    }

    /**
     * This will initialize a Link object with all the required
     * fields filled in with placeholder data, and return it.
     * It will also associate it with its parent Port object.
     * @param portDB the parent Port
     * @return the new Link object
     */
    public static Link initLink(Port portDB) {
        Link linkDB = new Link();
        linkDB.setValid(true);
        linkDB.setTopologyIdent("changeme");
        linkDB.setAlias("changeme");

        linkDB.setSnmpIndex(1); // we don't have this info
        linkDB.setTrafficEngineeringMetric("latency"); // what should this be?

        linkDB.setCapacity(0L);
        linkDB.setMaximumReservableCapacity(0L);
        linkDB.setMinimumReservableCapacity(0L);
        linkDB.setUnreservedCapacity(0L);
        linkDB.setGranularity(0L);
        linkDB.setPort(portDB);
        linkDB.setRemoteLink(null);
        linkDB.setIpaddrs(new HashSet());

        //  portDB.addLink(linkDB);
        return linkDB;
    }

    /**
     * Constructs the local topology identifier from a fully qualified
     * one
     * @param domDB the domain object
     * @return the topology identifier
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
     * Constructs the fully qualified topology identifier
     * @param domDB the domain object
     * @return the topology identifier
     */
    public static String getFQTI(Domain domDB) {
    	if (domDB == null) {
    		return "";
    	}
        String topoId = domDB.getTopologyIdent();
        String fqti;
        String prefix_a = "urn:ogf:network:domain=";
        String prefix_b = "urn:ogf:network:";

        if (topoId.startsWith(prefix_a)) {
            fqti = topoId;
        } else if (topoId.startsWith(prefix_b)) {
            fqti = topoId.replaceAll(prefix_b, prefix_a);
        } else {
            fqti = prefix_a + topoId;
        }

        return fqti;
    }

    /**
     * Constructs the fully qualified topology identifier
     * @param nodeDB the node object
     * @return the topology identifier
     */
    public static String getFQTI(Node nodeDB) {
    	if (nodeDB == null) {
    		return "";
    	}
        String parentFqti = TopologyUtil.getFQTI(nodeDB.getDomain());
        String topoId = TopologyUtil.getLSTI(nodeDB.getTopologyIdent(), "Node");

        return (parentFqti + ":node=" + topoId);
    }

    /**
     * Constructs the fully qualified topology identifier
     * @param portDB the port object
     * @return the topology identifier
     */
    public static String getFQTI(Port portDB) {
    	if (portDB == null) {
    		return "";
    	}
        String parentFqti = TopologyUtil.getFQTI(portDB.getNode());
        String topoId = TopologyUtil.getLSTI(portDB.getTopologyIdent(), "Port");

        return (parentFqti + ":port=" + topoId);
    }

    /**
     * Constructs the fully qualified topology identifier
     * @param linkDB the link object
     * @return the topology identifier
     */
    public static String getFQTI(Link linkDB) {
    	if (linkDB == null) {
    		return "";
    	}
        String parentFqti = TopologyUtil.getFQTI(linkDB.getPort());
        String topoId = TopologyUtil.getLSTI(linkDB.getTopologyIdent(), "Link");

        return (parentFqti + ":link=" + topoId);
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
     * This method parses a topology identifier and returns useful information
     * in a hashtable. The hash keys are as follows:
     * type: one of "domain", "node", "port", "link", "ipv4address", "ipv6address", "unknown"
     * 
     * domainId: the domain id component (if it exists)
     * nodeId: the node id component (if it exists)
     * portId: the port id component (if it exists)
     * linkId: the link id component (if it exists)
     * 
     * fqti: the fully qualified topology identifier (if applicable)
     * 
     * @param topoIdent the topology identifier to parse
     * @return a Hashtable with the parse results
     */
    public static Hashtable<String, String> parseTopoIdent(String topoIdent) {
    	
    	
    	topoIdent = topoIdent.trim();
    	
//    	System.out.println("looking at: ["+topoIdent+"]");

    	
    	Hashtable<String, String> regexps = new Hashtable<String, String>();
    	regexps.put("domainFull", "^urn:ogf:network:domain=([^:]+)$");
    	regexps.put("domain", "^urn:ogf:network:([^:=]+)$");
    	
    	regexps.put("nodeFull", "^urn:ogf:network:domain=([^:]+):node=([^:]+)$");
    	regexps.put("node", "^urn:ogf:network:([^:=]+):([^:=]+)$");
    	
    	regexps.put("portFull", "^urn:ogf:network:domain=([^:]+):node=([^:]+):port=([^:]+)$");
    	regexps.put("port", "^urn:ogf:network:([^:=]+):([^:=]+):([^:=]+)$");

    	regexps.put("linkFull", "^urn:ogf:network:domain=([^:]+):node=([^:]+):port=([^:]+):link=([^:]+)$");
    	regexps.put("link", "^urn:ogf:network:([^:=]+):([^:=]+):([^:=]+):([^:=]+)$");
    	
    	String domainId = "";
    	String nodeId = "";
    	String portId = "";
    	String linkId = "";
    	
    	String matched = "";

    	Matcher matcher = null;
    	
    	
    	for (String key: regexps.keySet()) {
    		Pattern p = Pattern.compile(regexps.get(key));
			matcher = p.matcher(topoIdent);
			if (matcher.matches()) {
        		if (key.equals("domain") || key.equals("domainFull")) {
	    			matched = "domain";
	    			domainId = matcher.group(1);
	    		} else if (key.equals("node") || key.equals("nodeFull") ) {
	    			matched = "node";
	    			domainId = matcher.group(1);
	    			nodeId = matcher.group(2);
	    		} else if (key.equals("port") || key.equals("portFull") ) {
	    			matched = "port";
	    			domainId = matcher.group(1);
	    			nodeId = matcher.group(2);
	    			portId = matcher.group(3);
	    		} else if (key.equals("link") || key.equals("linkFull") ) {
	    			matched = "link";
	    			domainId = matcher.group(1);
	    			nodeId = matcher.group(2);
	    			portId = matcher.group(3);
	    			linkId = matcher.group(4);
	    		}
			}
    	}

//    	TODO: make a class for the results?
    	Hashtable<String, String> result = new Hashtable<String, String>();
    	
    	if (topoIdent == null || topoIdent.equals("")) {
    		result.put("type", "empty");
    		return result;
    	}
    	
    	String compactForm = null;
    	String fqti = null;
    	String addressType = "";

    	try {
    		InetAddress[] addrs = InetAddress.getAllByName(topoIdent);
 			System.out.print("[Success]:");
 			for (int i =0; i < addrs.length;i++){
 				addressType = addrs[i].getClass().getName();
 			}

 			if (addressType.equals("java.net.Inet6Address")) {
 				addressType = "ipv6address";
 			} else if (addressType.equals("java.net.Inet4Address")) {
 				addressType = "ipv4address";
 			} else {
 				addressType = "unknown";
 			}
	 		result.put("type", addressType);	 		
      	    matched = "address";
 		} catch(UnknownHostException e){
 			if (matched == null) {
		 		result.put("type", "unknown");	 		
	      	    return result;
 			}
 		}

    	if (matched.equals("domain")) {
    		fqti = "urn:ogf:network:domain="+domainId;
    		compactForm = "urn:ogf:network:"+domainId;
    	  	result.put("compact", compactForm);
    	  	result.put("type", "domain");
    	  	result.put("fqti", fqti);
    	  	result.put("domainId", domainId);
    	} else if (matched.equals("node")) {
    		fqti = "urn:ogf:network:domain="+domainId+":node="+nodeId;
    		compactForm = "urn:ogf:network:"+domainId+":"+nodeId;
    	  	result.put("compact", compactForm);
      	  	result.put("type", "node");
    	  	result.put("fqti", fqti);
    	  	result.put("domainId", domainId);
    	  	result.put("nodeId", nodeId);
    	} else if (matched.equals("port")) {
    		fqti = "urn:ogf:network:domain="+domainId+":node="+nodeId+":port="+portId;
    		compactForm = "urn:ogf:network:"+domainId+":"+nodeId+":"+portId;
    	  	result.put("compact", compactForm);
			result.put("type", "port");
			result.put("fqti", fqti);
    	  	result.put("domainId", domainId);
    	  	result.put("nodeId", nodeId);
    	  	result.put("portId", portId);
    	} else if (matched.equals("link")) {
    		fqti = "urn:ogf:network:domain="+domainId+":node="+nodeId+":port="+portId+":link="+linkId;
    		compactForm = "urn:ogf:network:"+domainId+":"+nodeId+":"+portId+":"+linkId;
    	  	result.put("compact", compactForm);
			result.put("type", "link");
			result.put("fqti", fqti);
    	  	result.put("domainId", domainId);
    	  	result.put("nodeId", nodeId);
    	  	result.put("portId", portId);
    	  	result.put("linkId", linkId);
    	}
 		return result; 
    }
    

    /**
     * Checks to see if a hop id is a topology identifier, or an IP address.
     *
     * @param hopId string with hop id
     * @return boolean indicating whether hop is a topology identifier
     */
    public static boolean isTopologyIdentifier(String hopId) {
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
}
