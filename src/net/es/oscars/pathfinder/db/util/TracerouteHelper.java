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
import net.es.oscars.bss.topology.*;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;

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
        Link srcLink = this.findClosestLocalLinkTo(srcHost);
        Link dstLink = this.findClosestLocalLinkTo(dstHost);

        result.srcLink = srcLink;
        result.dstLink = dstLink;
        return result;

    }

    private Link findClosestLocalLinkTo(String ipaddress) throws PathfinderException {
        Link link = null;
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);

        String defaultRouter = this.props.getProperty("jnxSource");
        // run the traceroute from our network core to the host
        List<String> hops = this.traceroute(defaultRouter, ipaddress);

        for (String hop : hops) {
            Link tmpLink = null;
            Ipaddr ipaddr = ipaddrDAO.queryByParam("IP", hop);
            if (ipaddr != null) {
                tmpLink = ipaddr.getLink();
                if (!tmpLink.isValid()) {
                    throw new PathfinderException("L3 path goes through an invalid link!");
                }
                if (tmpLink.getPort().getNode().getDomain().isLocal()) {
                    link = tmpLink;
                }
            }
        }

        if (link == null) {
            throw new PathfinderException("No local links found on L3 path!");
        }

        return link;
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
