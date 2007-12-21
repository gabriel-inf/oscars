import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.database.Initializer;
import net.es.oscars.wsdlTypes.*;

import java.io.*;
import java.text.DateFormat;
import java.util.*;

import org.hibernate.*;
import org.hibernate.cfg.*;
import org.hibernate.Transaction;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;


/**
 * This class will read a file with reservation creation constraints 
 * and reply whether the reservation is possible, and give feedback
 * with new constraints.
 * 
 * Constraints file format is:
 * 
 * layer=2 | 3						# currently this only supports L2
 * duration=N 						# the reservation duration in hours 
 * start_time=yyyy-mm-dd hh:ii:ss 	# the earliest it could possibly start 
 * end_time=yyyy-mm-dd hh:ii:ss 	# the latest it could possibly end, end - start must be > duration
 * bandwidth=N 						# the desired bandwidth in Mbps
 * ero_1=urn:ogf:network:....		# multiple (>=2) lines with topology identifiers / IPs
 * ero_2=urn:ogf:network:....		# if exactly 2 lines for local domain, will use pathfinder to find the path,
 * ero_3=urn:ogf:network:....		# else we assume the user has given us the full ERO
 * ero_4=urn:ogf:network:....		# parts of the ERO for non-local domains will be ignored
 * 
 *    
 * # L2 specific:
 * vtag=K-L,M-N,..					# the vlan range the user can use, can be "any"
 *  
 * Will output results to stdout.
 * 
 * @author Evangelos Chaniotakis (haniotak@es.net)
 */
public class CheckConstraints {
	
    public static void main(String[] args) {
    	
    	// Variable declarations
        String usage = "Usage:\ncheckConstraints.sh [-f /path/to/constraints/file]\n" +
        				" -f default is constraints.properties\n";
        			   
        String propFileName = "constraints.properties";
        Properties props = new Properties();
        String layer = null;
        Layer2Info layer2Info = null;
        Layer3Info layer3Info = null;
        MplsInfo mplsInfo = null;
        Long bandwidth = null;
        Long startTime = null;
        Long endTime = null;
        Long duration = null;
        Long timeGranularity = null;

    	// Read args
        for(int i = 0; i < args.length; i++){
            if (args[i].equals("-help") ||
                args[i].equals("-h")) {
                System.out.println(usage);
                System.exit(0);
            }
            if (args[i].equals("-f")) {
            	if (args.length >= i+1) {
            		propFileName = args[i+1];
            	} else {
                    System.out.println(usage);
                    System.exit(0);
            	}
            }
        }
        
    	// Read properties file
        try {
            FileInputStream in = new FileInputStream(propFileName);
            props.load(in);
            in.close();
        } catch (IOException e) {
            System.out.println(" Properties file not found: " + propFileName);
        }
        
    	// Init db
        Initializer initializer = new Initializer();
        List<String> dbnames = new ArrayList<String>();
        dbnames.add("bss");
        initializer.initDatabase(dbnames);
        Session ses = HibernateUtil.getSessionFactory("bss").getCurrentSession();
        Transaction tx = ses.beginTransaction();

    	// Read properties
        layer = props.getProperty("layer").trim();
        if (layer == null) {
            System.err.println("layer property is null; exiting");
            System.exit(0);
        } else if (!layer.equals("2") && !layer.equals("3")) {
            System.err.println("layer must be 2 or 3; exiting");
            System.exit(0);
        }
        bandwidth = Long.parseLong(props.getProperty("bandwidth").trim());
        
        double dseconds = Double.valueOf(props.getProperty("duration").trim()) * 3600.0;
        duration = (long)dseconds * 1000; // milliseconds
        
        dseconds = Double.valueOf(props.getProperty("timegranularity").trim()) * 60.0;
        timeGranularity = (long)dseconds * 1000; // milliseconds

        startTime = Date.parse(props.getProperty("startTime").trim());
        endTime = Date.parse(props.getProperty("endTime").trim());

        
    	// Init objects
        ReservationManager rm = new ReservationManager("bss"); 
        Reservation resv = new Reservation();
        CtrlPlanePathContent path = new CtrlPlanePathContent();
        PathInfo pathInfo = new PathInfo();
        CreateReply fwdReply = new CreateReply();
        
        // configure objects
        if (layer.equals("2")) {
            layer2Info = new Layer2Info();
            VlanTag srcVtag = new VlanTag();
            srcVtag.setString(props.getProperty("vtag").trim());
            srcVtag.setTagged(true);
            VlanTag destVtag = new VlanTag();
            destVtag.setString(props.getProperty("vtag").trim());
            destVtag.setTagged(true);
            
            layer2Info.setSrcEndpoint(props.getProperty("sourceEndpoint").trim());
            layer2Info.setDestEndpoint(props.getProperty("destEndpoint").trim());
            layer2Info.setSrcVtag(srcVtag);
            layer2Info.setDestVtag(destVtag);
            
            pathInfo.setLayer2Info(layer2Info);
        } else {
            System.err.println("Only layer 2 this version; exiting");
            System.exit(0);
        }

        path.setId("userPath");//id doesn't matter in this context
        boolean hasEro = false;
        // TODO:  FIX limitation
        for (int i = 0; i < 20 ; i++) {
            String propName = "ero_"+Integer.toString(i);
            String hopId = props.getProperty(propName);
            if (hopId != null) {
                hopId = hopId.trim();
                hasEro = true;
                CtrlPlaneHopContent hop = new CtrlPlaneHopContent();
                hop.setId(i + "");
                hop.setLinkIdRef(hopId);
                path.addHop(hop);
            }
        }
        if (hasEro) {
            pathInfo.setPath(path);
        }
        
        
        // do the work
        resv.setBandwidth(bandwidth);
        
        Long thisStart = startTime;
        Long thisEnd = startTime+duration;
        
        while (thisEnd < endTime - duration) {
        	Date sd = new Date(thisStart);
        	Date ed = new Date(thisEnd);
        	System.out.println("Checking start: "+sd.toString()+" end: "+ed.toString());		
        	
	        resv.setStartTime(thisStart);
	        resv.setEndTime(thisEnd);
	        try {
	        	Path modPath = rm.getPath(resv, pathInfo);
	            resv.setPath(modPath);
	        } catch (BSSException ex) {
	        	System.err.println("Error: "+ex.getMessage());
	        	continue;
	        }
	        
	        CtrlPlanePathContent fp = pathInfo.getPath();
	
	    	Layer2Info l2i = pathInfo.getLayer2Info();
	        String svt = l2i.getSrcVtag().getString();
	    	String dvt = l2i.getDestVtag().getString();
	    	System.out.println("Available vlan tags, source: ["+svt+"]  destination:["+dvt+"]");
	    	thisStart += timeGranularity;
	    	thisEnd += timeGranularity;
        }
        
        // don't store anything
        tx.rollback();
    }
    

}
