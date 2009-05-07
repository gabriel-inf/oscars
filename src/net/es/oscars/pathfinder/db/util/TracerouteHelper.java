package net.es.oscars.pathfinder.db.util;

import java.util.*;
import java.io.IOException;

import org.apache.log4j.*;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import net.es.oscars.PropHandler;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.pathfinder.db.*;
import net.es.oscars.pathfinder.db.util.vendor.jnx.JnxShowRoute;
import net.es.oscars.bss.topology.*;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.pss.PSSException;

/**
 * TraceroutePathfinder performs traceroutes to find a layer 3 path from
 * source to destination within the local domain.
 */
public class TracerouteHelper {
    private Logger log = Logger.getLogger(TracerouteHelper.class);
    private Properties props;
    private String dbname;



    public TracerouteHelper(String dbname) {
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("traceroute", true);
        this.dbname = dbname;
    }

    public TracerouteResult findEdgeLinks(Path requestedPath) throws PathfinderException {



        TracerouteResult result = new TracerouteResult();
        String srcHost = requestedPath.getLayer3Data().getSrcHost();
        String dstHost = requestedPath.getLayer3Data().getDestHost();


        Link srcLink = this.findClosestLocalLinkTo(srcHost, null);
        Link dstLink = this.findClosestLocalLinkTo(dstHost, srcLink.getPort().getNode());

        result.srcLink = srcLink;
        result.dstLink = dstLink;
        return result;

    }

    private Link findClosestLocalLinkTo(String ipaddress, net.es.oscars.bss.topology.Node startFromHere) throws PathfinderException {
        Link link = null;
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        NodeAddressDAO naDAO = new NodeAddressDAO(this.dbname);

        String defaultRouter = this.props.getProperty("jnxSource");
        // run the traceroute from our network core to the host
        List<String> hops;
        if (startFromHere == null) {
            hops = this.traceroute(defaultRouter, ipaddress);
        } else {
            hops = this.traceroute(defaultRouter, startFromHere.getNodeAddress().getAddress());
        }

        Node node = null;
        for (String hop : hops) {
            this.log.info("hop: " + hop);
            Link tmpLink = null;

            Ipaddr ipaddr = ipaddrDAO.getValidIpaddr(hop);
            NodeAddress nodeAddr = naDAO.getNodeAddress(hop);

            if (nodeAddr != null) {
                if (!nodeAddr.getNode().isValid() ||
                    !nodeAddr.getNode().getDomain().isLocal() ) {
                    this.log.info("addr: " + hop+" invalid or non-local node:"+nodeAddr.getNode().getFQTI());
                } else if (nodeAddr.getNode().getDomain().isLocal()) {
                    this.log.info("addr: " + hop+" valid local node:"+nodeAddr.getNode().getFQTI());
                    node = nodeAddr.getNode();
                }
            } else if (ipaddr != null) {
                tmpLink = ipaddr.getLink();
                this.log.info("addr: " + hop+" node:"+tmpLink.getFQTI());
                if (!tmpLink.isValid()) {
                    throw new PathfinderException("L3 path goes through an invalid link!");
                }
                if (tmpLink.getPort().getNode().getDomain().isLocal()) {
                    link = tmpLink;
                }
            } else {
                this.log.info("invalid ipaddr for: " + hop);
            }
        }

        if (link == null && node == null) {
            throw new PathfinderException("No local links found on L3 path!");
        }

        if (node == null) {
            node = link.getPort().getNode();
        }


        JnxShowRoute sr = new JnxShowRoute();
        DomainDAO domDAO = new DomainDAO(this.dbname);
        try {
            String portId = sr.showRoute(node.getTopologyIdent(), "inet", ipaddress);
            String tmpLinkId = node.getFQTI()+":port="+portId+":link=*";
            this.log.info("L3 route to: "+ipaddress+" is: "+tmpLinkId);
            Link result = domDAO.getFullyQualifiedLink(tmpLinkId);
            return result;
        } catch (PSSException ex) {
            throw new PathfinderException(ex.getMessage());
        }

    }

    /**
     * Performs traceroute.
     *
     * @param src source string
     * @param dest destination string
     * @return hops list of strings with addresses in path
     * @throws PathfinderException
     */
    private List<String> traceroute(String srcIpAddress, String destIpAddress)
            throws PathfinderException {

        JnxTraceroute jnxTraceroute = null;
        List<String> hops = null;

        jnxTraceroute = new JnxTraceroute();
        try {
            hops = jnxTraceroute.traceroute(srcIpAddress, destIpAddress);
        } catch (IOException ex) {
            throw new PathfinderException(ex.getMessage());
        }

        return hops;
    }
}
