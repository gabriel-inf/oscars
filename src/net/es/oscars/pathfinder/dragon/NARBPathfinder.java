package net.es.oscars.pathfinder.dragon;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;
import org.hibernate.*;

import net.es.oscars.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.wsdlTypes.ExplicitPath;
import net.es.oscars.wsdlTypes.Hop;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.topology.*;

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
                         String ingressRouterIP, String egressRouterIP, ExplicitPath reqPath)
            throws PathfinderException {
            
		List<String> localHops = null;
		
		/* Determine whether there is a user requested path then calculate */
		if(reqPath != null){
			/* Find hops on local domain */
			localHops = this.getLocalHops(reqPath);
        }else{
			/* Ask NARB to calculate path */
        	List<String> hops = this.findNARBPath(srcHost, destHost, ingressRouterIP, egressRouterIP);
        	
        	/* Get Local Path */
        	localHops = this.getLocalHops(hops);
		} 
        
        /* Set Ingress and Egress */
        ingressRouterIP = this.getLoopback(localHops.get(0));
        egressRouterIP = this.getLoopback(localHops.get(localHops.size()-1));
        this.log.info("findNARBPath.ingressRouterIP", ingressRouterIP);
        this.log.info("findNARBPath.egressRouterIP", egressRouterIP);

        return this.checkPath(localHops, ingressRouterIP, egressRouterIP);
    }
	
	/**
	 * Returns first hop outside domain that was identified during path calculation process
	 * 
	 *@return first hop outside the domain
	 */
    public String getNextHop() {
        return this.nextHop;
    }
    
    /**
     * Returns list of hops that are in the local domain given a list of hops as Strings. Also sets
     * nextHop to the first hop past the local domain.
     *
     * @param hops List of Strings representing hops
     * @return returns a list of string representing only hops within the domain
     */
    public List<String> getLocalHops(List<String> hops) throws PathfinderException{
        Session session = HibernateUtil.getSessionFactory("bss").getCurrentSession();
        RouterDAO routerDAO = new RouterDAO();
        routerDAO.setSession(session);
        ArrayList<String> localHops = new ArrayList<String>();
        boolean ingressFound = false;
        
        /* Parse hops */
        for (String hop: hops)  {
            Router router = routerDAO.fromIp(hop);
            if ((router != null) && (router.getName() != null)) {
                this.log.info("localHop: ", hop);
                localHops.add(hop);
                ingressFound = true;
            }else if(ingressFound){
            	this.log.info("nextHop: ", hop);
            	this.nextHop = hop;
            	break;
            }
        }
        
        /* Throw error if no local path found */
        if (!ingressFound) { 
            throw new PathfinderException(
                "No ingress loopback found in path");
        }
        
        return localHops;
    }
    
    /**
     * Returns list of hops that are in the local domain given an ExplicitPath. Also sets
     * nextHop to the first hop past the local domain.
     *
     * @param reqPath ExplicitPath containing hops of entire path
     * @return returns a list of string representing only hops within the domain
     */
    public List<String> getLocalHops(ExplicitPath reqPath) throws PathfinderException{
    	Session session = HibernateUtil.getSessionFactory("bss").getCurrentSession();
        RouterDAO routerDAO = new RouterDAO();
        routerDAO.setSession(session);
       	List<String> localHops = new ArrayList<String>();
        boolean ingressFound = false;
        
        /* Parse hops */
        Hop[] hops = reqPath.getHops().getHop();
        Hop prevHop = null;
        for (Hop hop: hops)  {
            Router router = routerDAO.fromIp(hop.getValue());
            if ((router != null) && (router.getName() != null)) {
                this.log.info("localHop: ", hop.getValue());
                ingressFound = true;
                
                /* Expand Hops */
                if(prevHop != null){
					if(hop.getLoose() || prevHop.getLoose()){
						/* Get loopback addresses - will this ever cause incorrect path? */
						String src = this.getLoopback(prevHop.getValue());
						String dst = this.getLoopback(hop.getValue());
						List<String> expandedHops = null;
						
						/* Expand path */
						if(src.equals(dst)){ //if interfaces on same router
							expandedHops.add(prevHop.getValue());
							expandedHops.add(hop.getValue());
						}else{
							expandedHops = this.findNARBPath(src, dst, null, null);
							/* Make sure outgoing interface is added to list */
							if(!expandedHops.get(expandedHops.size() - 1).equals(hop.getValue())){
								expandedHops.add(hop.getValue());
							}
						}
						
						/* Check edges to make sure correct incoming interface is used */
						if(localHops.isEmpty() && (!expandedHops.isEmpty()) &&
								(!expandedHops.get(0).equals(prevHop.getValue()))){
							/* Add hop if incoming interface not included in list */
							expandedHops.add(0, prevHop.getValue());
						}else if((!localHops.isEmpty()) && 
								localHops.get(localHops.size() - 1).equals(expandedHops.get(0))){
							/* Remove first hop if already in the list */
							expandedHops.remove(0);
						}
						
						/* Append expanded path to list */
						for(String expandedHop : expandedHops){
							localHops.add(expandedHop);
						}
					}else{
						/* Check if in database */
						//TODO: Assumes that if ip exists locally it has link to previous hop
						localHops.add(hop.getValue());
					}
				}
                
                prevHop = hop;
            }else if(ingressFound){
            	this.log.info("nextHop: ", hop.getValue());
            	this.nextHop = hop.getValue();
            	break;
            }
        }
        
        /* Throw error if no local path found */
        if (!ingressFound) { 
            throw new PathfinderException(
                "No ingress loopback found in path");
        }
        
        return localHops;
    }
    
    /**
     * Returns router loopback address of a given interface IP address
     *
     * @param src IP address of interface to indentify router loopback
     * @return IP of router loopback associated with given interface
     */
    public String getLoopback(String hop){
        Session session = HibernateUtil.getSessionFactory("bss").getCurrentSession();
        RouterDAO routerDAO = new RouterDAO();
        routerDAO.setSession(session);
        IpaddrDAO ipaddrDAO = new IpaddrDAO();
        ipaddrDAO.setSession(session);
		String loopback = null;
		
        Router router = routerDAO.fromIp(hop);
        if ((router != null) && (router.getName() != null)) {
        	loopback = ipaddrDAO.getIpType(router.getName(), "loopback");
         }

        return loopback;
    }

    /**
     * Retrieves path calculation from DRAGON NARB
     *
     * @param src string with IP address of source host
     * @param dst string with IP address of destination host
     * @param ingress string with IP address of desired ingress router loopback
     * @param egress string with IP address of desired egress router loopback
     * @return list of hops in path
     * @throws PathfinderException
     */
    public List<String>
        findNARBPath(String src, String dst, String ingress, String egress)
            throws PathfinderException {

        List<String> hops = new ArrayList<String>();
        String narbURL = this.props.getProperty("url");
        String hostDst = null;
        
        /* Determine ingress if specified */
        if(ingress != null){
        	src = ingress;
        }
        
        /* determine egress */
        if(egress != null){
        	hostDst = dst;
        	dst = egress;
        }
        
		/* Calculate path */
        try {
            this.log.info("findNARBPath", "start");
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
			
			/* if egress specified, add hops after egress  so next domain can be found */
			if(egress != null){
				List<String> egressHops = this.findNARBPath(egress, hostDst, null, null);
				hops.addAll(egressHops);
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
