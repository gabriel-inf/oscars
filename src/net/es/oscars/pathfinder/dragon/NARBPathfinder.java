package net.es.oscars.pathfinder.dragon;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

import org.hibernate.Session;

import net.es.oscars.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.database.HibernateUtil;

import edu.internet2.hopi.dragon.narb.NARBWSClient;
import edu.internet2.hopi.dragon.narb.ws.client.NARBStub;
import edu.internet2.hopi.dragon.narb.ws.client.NARBStub.FindPathContent;
import edu.internet2.hopi.dragon.narb.ws.client.NARBStub.FindPathResponseContent;
import edu.internet2.hopi.dragon.narb.ws.client.NARBFaultMessageException;

/**
 * NARBPathfinder that uses NARB to calculate path
 *
 */
//public class NARBPathfinder implements PCE{
public class NARBPathfinder {
	private Properties props;
	private LogWrapper log;
	
	
	/**
     * Constructor that initializes NARB properties from oscars.properties file
     *
     */
	public NARBPathfinder(){
		PropHandler propHandler = new PropHandler("oscars.properties");
		this.props = propHandler.getPropertyGroup("narb", true);
		this.log = new LogWrapper(this.getClass());
	}
	
	/**
     * Retrieves path calculation from DRAGON NARB
     *
     * @param src string with IP address of source host
     * @param dst string with IP address of destination host
     * @return list of hops in path
     * @throws BSSException
     */
    public ArrayList<String> findPath(String src, String dst, String ingress, String egress) throws BSSException {
    	ArrayList<String> hops = new ArrayList<String>();
    	String narbURL = props.getProperty("url");
    	try{
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
			for(int i = 0; i < path.length; i++){
				this.log.info("narbPath.hop", path[i].getString());
				hops.add(path[i].getString());
			}

			this.log.info("narbPath.end", "end");
		}catch(UnknownHostException e){
			throw new BSSException(e.getMessage());
		}catch(IOException e){
			throw new BSSException(e.getMessage());
		}catch(NARBFaultMessageException e){
			throw new BSSException(e.getFaultMessage().getMsg());
		}
		
		
		return hops;
    }
}