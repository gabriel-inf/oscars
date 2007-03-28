package net.es.oscars.pathfinder.dragon;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

import net.es.oscars.*;
import net.es.oscars.pathfinder.*;

import edu.internet2.hopi.dragon.narb.NARBWSClient;
import edu.internet2.hopi.dragon.narb.ws.client.NARBStub;
import edu.internet2.hopi.dragon.narb.ws.client.NARBStub.FindPathContent;
import edu.internet2.hopi.dragon.narb.ws.client.NARBStub.FindPathResponseContent;
import edu.internet2.hopi.dragon.narb.ws.client.NARBFaultMessageException;

/**
 * NARBPathfinder that uses NARB to calculate path
 *
 */
public class NARBPathfinder extends Pathfinder implements PCE {
    private LogWrapper log;
    private Properties props;
    private String nextHop;
	
    /**
     * Constructor that initializes NARB properties from oscars.properties file
     *
     */
    public NARBPathfinder() {
        this.log = new LogWrapper(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("narb", true);
    }


    /**
     * Finds path from source to destination using the NARB web service interface, 
     * taking into account ingress and egress routers if specified by user.
     *
     * @param srcHost string with address of source host
     * @param destHost string with address of destination host
     * @param ingressRouterIP string with address of ingress router, if any
     * @param egressRouterIP string with address of egress router, if any
     * @return hops A list of strings containing IP addresses
     * @throws PathfinderException
     */
    public Path findPath(String srcHost, String destHost,
                         String ingressRouterIP, String egressRouterIP)
            throws PathfinderException {
        List<String> hops = null;

        /* NARB find path */
        hops = this.findNARBPath(srcHost, destHost,
                                 ingressRouterIP, egressRouterIP);
        this.nextHop = this.nextExternalHop(hops);
        /* create reverse list */
        List<String> ingressList = new ArrayList<String>();
        for (int i = (hops.size() - 1); i >= 0; i--) {
            ingressList.add(hops.get(i));
        }
        ingressRouterIP = this.lastLoopback(ingressList);
        egressRouterIP = this.lastLoopback(hops);

        /* get ingress to egress path */
        List<String> inegHops = new ArrayList<String>();
        boolean ingressFound = false;
        boolean egressFound = false;
        for (int i = 0; (!egressFound) && i < hops.size(); i++) {
            if (hops.get(i).equals(egressRouterIP)) {
                egressFound = true;
                inegHops.add(hops.get(i));
            } else if(hops.get(i).equals(ingressRouterIP)) {
                ingressFound = true;
                inegHops.add(hops.get(i));
            } else if(ingressFound) {
                inegHops.add(hops.get(i));
            }
        }
        this.log.debug("findNARBPath.firstHop", hops.get(0));
        this.log.debug("findNARBPath.ingressRouterIP", ingressRouterIP);
        this.log.debug("findNARBPath.egressRouterIP", egressRouterIP);

        return this.checkPath(inegHops, ingressRouterIP, egressRouterIP);
    }

    public String getNextHop() {
        return this.nextHop;
    }

    /**
     * Retrieves path calculation from DRAGON NARB
     *
     * @param src string with IP address of source host
     * @param dst string with IP address of destination host
     * @return list of hops in path
     * @throws PathfinderException
     */
    public List<String>
        findNARBPath(String src, String dst, String ingress, String egress)
            throws PathfinderException {

        List<String> hops = new ArrayList<String>();
        String narbURL = this.props.getProperty("url");
        try {
            this.log.info("narbPath.start", "start");
            NARBWSClient client = new NARBWSClient(narbURL);
            FindPathContent request = new FindPathContent();

            request.setSrcHost(src);
            request.setDstHost(dst);
            request.setBandwidth(100);
            request.setPreferred(true);
            request.setStrict(true);
            FindPathResponseContent response = client.sendRequest(request);
            NARBStub.Hop[] path = response.getPath().getHops().getHop();
            for (int i = 0; i < path.length; i++) {
                this.log.info("narbPath.hop", path[i].getString());
                hops.add(path[i].getString());
            }

            this.log.info("narbPath.end", "end");
        } catch(UnknownHostException e) {
            throw new PathfinderException(e.getMessage());
        } catch(IOException e) {
            throw new PathfinderException(e.getMessage());
        } catch(NARBFaultMessageException e) {
            throw new PathfinderException(e.getFaultMessage().getMsg());
        }
        return hops;
    }
}
