package net.es.oscars.pathfinder.overlay;

import java.util.*;
import java.io.IOException;

import org.apache.log4j.*;

import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;

import net.es.oscars.PropHandler;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.bss.Reservation;

/**
 * OverlayPathfinder currently only handles explicit layer 2 and layer 3
 * routes with a complete path.  It will handle the choose of optimal
 * routes, and the specification of only the ingress and egress at layer 2,
 * later.
 */
public class OverlayPathfinder extends Pathfinder implements PCE {
    private Logger log;
    private Properties props;
    private Utils utils;

    public OverlayPathfinder(String dbname) {
        super(dbname);
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("overlay", true);
        this.utils = new Utils(dbname);
    }

    /**
     * Currently just handles conversions, if necessary, in topology
     * identifiers.
     *
     * @param pathInfo PathInfo instance containing all path information
     * @return boolean indicating ERO used (necessary for interface)
     * @throws PathfinderException
     */
    public PathInfo findPath(PathInfo pathInfo, Reservation reservation) throws PathfinderException {

        String ingressNodeId = null;
        String egressNodeId = null;

        this.log.debug("findPath.start");
        CtrlPlanePathContent path = pathInfo.getPath();
        if (path != null) {
            DomainDAO domainDAO = new DomainDAO(this.dbname);
            CtrlPlaneHopContent[] ctrlPlaneHops = path.getHop();
            // if an ERO, not just ingress and/or egress
            if (ctrlPlaneHops.length > 2) {
               this.log.info("handling explicit route object");
               if (pathInfo.getLayer2Info() != null) {
                   this.handleLayer2ERO(pathInfo);
                } else if (pathInfo.getLayer3Info() != null) {
                    this.handleLayer3ERO(pathInfo);
                } else {
                    throw new PathfinderException(
                        "An ERO must have associated layer 2 or layer 3 info");
                }
            // handle case where only ingress, egress, or both given
            } else if (ctrlPlaneHops.length == 2) {
                throw new PathfinderException(
                        "An ERO with just two hops (ingress and egress) " +
                        "is temporarily not handled");
            }
        }
        this.log.debug("findPath.End");
        return pathInfo; //return same path to conform to interface
    }

    /**
     * Handles a layer 2 or layer 3 explicit route object that may or may not
     * contain hops internal to this domain.  Hops in the explicit route
     * must currently be topology identifiers in external or local format.
     * At the end, all hops are in the local topology identifier format.
     *
     * @param pathInfo PathInfo instance with complete path
     * @throws PathfinderException
     */
    private void handleLayer2ERO(PathInfo pathInfo)
            throws PathfinderException {

        int numHops = 0;

        this.log.debug("handleLayer2ERO.start");
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        Domain domain = domainDAO.getLocalDomain();
        CtrlPlanePathContent ero = pathInfo.getPath();
        CtrlPlaneHopContent[] hops = ero.getHop();
        for (int i=0; i < hops.length; i++) {
            CtrlPlaneHopContent ctrlPlaneHop = hops[i];
            String hopId = ctrlPlaneHop.getLinkIdRef();
            if (!TopologyUtil.isTopologyIdentifier(hopId)) {
                throw new PathfinderException(
                    "layer 2 ERO must currently be made up of " +
                    "topology identifiers");
            }
            this.log.debug("hop id (original):["+hopId+"]");

            Hashtable<String, String> parseResults = TopologyUtil.parseTopoIdent(hopId);
            hopId = parseResults.get("compact");
            String domainId = parseResults.get("domainId");

            this.log.debug("hop id (local):["+hopId+"]");
            if (domainDAO.isLocal(domainId)) {
                numHops++;
            }
            // reset id ref to local topology identifier
            ctrlPlaneHop.setLinkIdRef(hopId);
            // make schema validator happy
            ctrlPlaneHop.setId(hopId);
        }
        // throw error if no local path found
        if (numHops == 0) {
            throw new PathfinderException("No local hops given in ERO");
        }
        this.log.debug("handleExplicitRouteObject.finish");
    }

    /**
     * Handles a layer 3 explicit route object that may or may not contain
     * hops internal to this domain.  Hops in the explicit route object
     * must currently be either DNS names, IPv4 or IPv6 addresses.  At the
     * end, hops are in the local topology identifier format.
     *
     * @param pathInfo PathInfo instance with complete path
     * @throws PathfinderException
     */
    private void handleLayer3ERO(PathInfo pathInfo) throws PathfinderException {

        String fqn = null;
        String hop = null;
        String ingressNodeIP = null;
        String prevHop = null;
        boolean hopFound = false;
        int numHops = 0;

        this.log.debug("handleLayer3ERO.start");
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        Domain domain = domainDAO.getLocalDomain();
        CtrlPlanePathContent ero = pathInfo.getPath();
        CtrlPlaneHopContent[] hops = ero.getHop();
        // only need IP addresses at this point in case only loose hops
        for (int i=0; i < hops.length; i++) {
            CtrlPlaneHopContent ctrlPlaneHop = hops[i];
            String hopId = ctrlPlaneHop.getLinkIdRef();
            if (TopologyUtil.isTopologyIdentifier(hopId)) {
                throw new PathfinderException(
                    "layer 3 ERO cannot currently be made up of " +
                    "topology identifiers");
            }
            hop = this.utils.getIP(hopId);
            // non-local
            if (ipaddrDAO.queryByParam("IP", hop) == null) {
                hop = null;
            }
            if (hop != null) {
                // used to check for valid ingress
                if (!hopFound) { ingressNodeIP = hop; }
                hopFound = true;
                fqn = domainDAO.setFullyQualifiedLink(
                        domain.getTopologyIdent(), hop);
                numHops++;
            } else {
                fqn = domainDAO.setFullyQualifiedLink("other", hop);
            }
            // reset id ref to local topology identifier
            ctrlPlaneHop.setLinkIdRef(fqn);
            // make schema validator happy
            ctrlPlaneHop.setId(fqn);
            prevHop = hop;
        }
        // throw error if no local path found
        if (numHops == 0) {
            throw new PathfinderException("No local hops given in ERO");
        }
        // check ingress for validity
        this.checkIngress(ingressNodeIP);
        this.log.debug("handleLayer3ERO.finish");
    }

    /**
     * For layer 3, checks ingress, if given, for validity.  It must be
     * associated with a Juniper router for now.
     *
     * @param ingressNodeIP string with IP of ingress to check
     */
    private void checkIngress(String ingressNodeIP) throws PathfinderException {

        String loopbackIP = null;
        // if not placeholder allowing only one of ingress or egress to
        // be set
        if (!ingressNodeIP.equals("*")) {
             // make sure the given ingress node is a Juniper router
             loopbackIP =
                     this.utils.getLoopback(ingressNodeIP, "Juniper");
             if (loopbackIP == null) {
                 throw new PathfinderException("given ingress node " +
                         ingressNodeIP + " is not a Juniper router");
             }
        }
    }
}
