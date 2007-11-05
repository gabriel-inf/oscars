package net.es.oscars.pss.dragon;

import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.log4j.*;
import edu.internet2.hopi.dragon.*;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import net.es.oscars.PropHandler;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.pss.*;


/**
 * A PSS that accesses DRAGON VLSR via its command line interface
 *  
 * @author Andrew Lake (alake@internet2.edu)
 */
public class VlsrPSS implements PSS{
    private Properties props;
    private Logger log;
    
    /** Constructor */
    public VlsrPSS(){
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("pss.dragon", true);
    }
    
    /**
     * Creates a the LSP for the given reservation.
     *
     * @param resv the reservation whose path will be created
     * @throws PSSException
     */
    public String createPath(Reservation resv) throws PSSException{
        this.log.info("vlsr.create.start");
        DragonCSA csa = new DragonCSA();
        JSch jsch = new JSch();
        String password = this.props.getProperty("password");
        String sshPortForward = this.props.getProperty("ssh.portForward");
        String sshUser = this.props.getProperty("ssh.user");
        String sshKey = this.props.getProperty("ssh.key");
        Path path = resv.getPath();
        Layer2Data layer2Data = path.getLayer2Data();
        Link ingressLink = path.getPathElem().getLink();
        Link egressLink= this.getEgressLink(path);
        int ingressLinkDescr = 
            Integer.parseInt(path.getPathElem().getLinkDescr());
        int egressLinkDescr= Integer.parseInt(this.getEgressLinkDescr(path));
        String ingressPortTopoId = ingressLink.getPort().getTopologyIdent();
        String egressPortTopoId = egressLink.getPort().getTopologyIdent();
        
        String telnetAddress = this.findTelnetAddress(ingressLink);
        String telnetAddressDest = this.findTelnetAddress(egressLink);
        int port = this.findTelnetPort();
        int portDest = this.findTelnetPort();
        int remotePort = Integer.parseInt(this.props.getProperty("remotePort"));
        Session ingressSshSess = null;
        Session egressSshSess = null;
         
        /* Get source and destination node address */
        InetAddress ingress = this.linkToNodeInetAddress(ingressLink);
        this.log.info("vlsr.create.ingress=" + ingress.getHostAddress());
        InetAddress egress = this.linkToNodeInetAddress(egressLink);
        this.log.info("vlsr.create.egress=" + egress.getHostAddress());
        
        /* Get source and destination local ID */
        DragonLocalID ingLocalId = this.linkToLocalId(ingressLink, 
                                                        ingressLinkDescr);
        DragonLocalID egrLocalId = this.linkToLocalId(egressLink, 
                                                        egressLinkDescr);
        int ingLocalIdIface = this.intefaceToLocalIdNum(ingressPortTopoId);
        int egrLocalIdIface = this.intefaceToLocalIdNum(egressPortTopoId);
        
        /* Initialize LSP */
        DragonLSP lsp = new DragonLSP(ingress, ingLocalId, egress,
                                            egrLocalId, null, 0);
        String gri = resv.getGlobalReservationId();
        lsp.setLSPName(gri);
        
        /* Set layer specific params */
        if(layer2Data != null){
            String bandwidth = this.prepareL2Bandwidth(resv.getBandwidth());
            lsp.setBandwidth(bandwidth);
            int vtag = Integer.parseInt(path.getPathElem().getLinkDescr());
            if(vtag < 0 && 
                ingLocalId.getType().equals(DragonLocalID.SUBNET_INTERFACE)){
                vtag = -1;
            }else{
                vtag = Math.abs(vtag);
            }
            lsp.setVTAG(vtag);
            lsp.setSrcLocalID(ingLocalId);
            lsp.setDstLocalID(egrLocalId);
            lsp.setSWCAP(DragonLSP.SWCAP_L2SC);
            lsp.setEncoding(DragonLSP.ENCODING_ETHERNET);
            lsp.setGPID(DragonLSP.GPID_ETHERNET);
        }else{
            throw new PSSException("Currently only layer2 reservations " +                     
                    "supported by this instance of OSCARS-DRAGON");
        }
        
        /* Initialize the CSA */
        csa.setPromptPattern(".*vlsr.*[>#]");
        
        /* Initialize ssh client */
        try{
            if(sshKey != null){
                jsch.addIdentity(sshKey);
             }
        }catch(JSchException e){
            this.log.error("SSH Error: " + e.getMessage());
            throw new PSSException(e.getMessage());
        }
        
        /* Create egress local id unless a subnet interface*/
        if(egrLocalId.getType() != DragonLocalID.SUBNET_INTERFACE){
            /* Create egress ssh tunnel */
            if(sshPortForward != null && sshPortForward.equals("1")){
                try{
                    String sshAddress = this.findSshAddress(egressLink);
                    egressSshSess = jsch.getSession(sshUser, sshAddress, 22);
                    Properties config = new Properties();
                    config.put("StrictHostKeyChecking", "no");
                    egressSshSess.setConfig(config);
                    egressSshSess.connect();
                    egressSshSess.setPortForwardingL(portDest, telnetAddressDest, remotePort);
                }catch(JSchException e){
                    this.log.error("Unable to create egress SSH tunnel: " + e.getMessage());
                    throw new PSSException(e.getMessage());
                }
            }
            
            /* Log into egress VLSR */
            this.log.info("logging into dstVLSR " + telnetAddressDest + " " + portDest);
    
            if(!csa.login(telnetAddressDest, portDest, password)){
                this.log.error("unable to login to dest VLSR " + lsp.getLSPName()
                        + ": " + csa.getError());
                throw new PSSException("Unable to create LSP: Dest local-id " + 
                    csa.getError());
            }
            
            /* Create egress local id */
            csa.deleteLocalId(egrLocalId);
            if(csa.createLocalId(egrLocalId, egrLocalIdIface)){
                this.log.info("Created local-id " + egrLocalId.getType() + " " +
                    egrLocalId.getNumber());
            }else{
                this.log.error("unable to create dest local-id " + 
                    lsp.getLSPName() + ": " + csa.getError());
                throw new PSSException("Unable to create LSP: Dest local-id " + 
                    csa.getError());
            }
    
            /* Remove port forwarding */
            if(sshPortForward != null && sshPortForward.equals("1")){
                egressSshSess.disconnect();
            }
            
            /* Logout */
             csa.disconnect();
        }
        
        /* Create ingress ssh tunnel */
        if(sshPortForward != null && sshPortForward.equals("1")){
            try{
                String sshAddress = this.findSshAddress(ingressLink);
                ingressSshSess = jsch.getSession(sshUser, sshAddress, 22);
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                ingressSshSess.setConfig(config);
                ingressSshSess.connect();
                ingressSshSess.setPortForwardingL(port, telnetAddress, remotePort);
            }catch (JSchException e) {
                this.log.error("Unable to create ingress SSH tunnel: " + e.getMessage());
                throw new PSSException(e.getMessage());
            }
        }
      
        /* Log into ingress VLSR */
        this.log.info("logging into VLSR " + telnetAddress + " " + port);
        if(!csa.login(telnetAddress, port, password)){
            this.log.error("unable to login to " + telnetAddress
                + ": " + csa.getError());
            if(ingressSshSess != null){
                ingressSshSess.disconnect();
            }
            throw new PSSException("Unable to reach ingress VLSR");
        }
        
        /* Create ingress local-id */
        if(ingLocalId.getType() != DragonLocalID.SUBNET_INTERFACE){
            
            /* Delete local-id if ingress and egress not the same */
            if(!(telnetAddress.equals(telnetAddressDest) && 
                ingLocalId.getNumber() == egrLocalId.getNumber())){
                csa.deleteLocalId(ingLocalId);
            }
         
            if(csa.createLocalId(ingLocalId, ingLocalIdIface)){
                this.log.info("Created local-id " + ingLocalId.getType() + " " +
                    ingLocalId.getNumber());
            }else{
                this.log.error("unable to create src local-id " + 
                    lsp.getLSPName() + ": " + csa.getError());
                if(ingressSshSess != null){
                    ingressSshSess.disconnect();
                }
                throw new PSSException("Unable to create LSP: src local-id " + 
                    csa.getError());
            }
        }
        
        /* Create LSP */
        if(csa.setupLSP(lsp)){
            this.log.info("created lsp " + lsp.getLSPName());
        }else{
            this.log.error("unable to create LSP " + lsp.getLSPName()
                + ": " + csa.getError());
            if(ingressSshSess != null){
                ingressSshSess.disconnect();
            }
            throw new PSSException("Unable to create LSP: " + 
                csa.getError());
        }
        
        /* Check lsp every few seconds */
        for(int i = 1; i <= 12; i++){
            String status = null;
            lsp = csa.getLSPByName(gri);
            
            /* Verify LSP still exists */
            if(lsp == null){
                this.log.error("LSP " + gri + " failed. Status: LSP could not" +                     
                    " be found");
                resv.setStatus("Failed");
                if(ingressSshSess != null){
                    ingressSshSess.disconnect();
                }
                throw new PSSException("Path failure occured.");
            }
            
            /* Check if LSP status */
            status = lsp.getStatus();
            if(status.equals(DragonLSP.STATUS_INSERVICE)){
                this.log.info(gri + " is IN SERVICE");
                break;
            }else if(!status.equals(DragonLSP.STATUS_COMMIT) || i == 12){
                this.log.error("Path setup failed. Status=" + lsp.getStatus());
                resv.setStatus("Failed");
                if(ingressSshSess != null){
                    ingressSshSess.disconnect();
                }
                if(csa.teardownLSP(gri)){
                    this.log.info("Deleted " + gri + " LSP after error");
                }else{
                    this.log.error("Unable to delete LSP after error: " + 
                        csa.getError());
                }
                throw new PSSException("LSP creation failed. There may be" + 
                    "an error in the underlying network.");
            }
            
            /* Sleep for 5 seconds */
            try{
                Thread.sleep(5000);
            }catch(Exception e){
                throw new PSSException("Could not sleep to wait for circuit");
            }
        }
        
        /* Remove port forwarding */
        if(sshPortForward != null && sshPortForward.equals("1")){
            ingressSshSess.disconnect();
        }
        
        /* SET RESERVATION STATUS */
        resv.setStatus("ACTIVE");
        
        this.log.info("vlsr.create.end");
        
        return resv.getStatus();
    }
    
    /**
     * Verifies LSP is still active by running a VLSR "show lsp" command
     *
     * @param resv the reservation whose path will be refreshed
     * @throws PSSException
     */
    public String refreshPath(Reservation resv) throws PSSException{
        this.log.info("vlsr.refresh.start");
        
        DragonCSA csa = new DragonCSA();
        DragonLSP lsp = null;
        JSch jsch = new JSch();
        String password = this.props.getProperty("password");
        String sshPortForward = this.props.getProperty("ssh.portForward");
        String sshUser = this.props.getProperty("ssh.user");
        String sshKey = this.props.getProperty("ssh.key");
        Path path = resv.getPath();
        Link ingressLink = path.getPathElem().getLink(); 
        String telnetAddress = this.findTelnetAddress(ingressLink);
        int port = this.findTelnetPort();
        int remotePort = Integer.parseInt(this.props.getProperty("remotePort"));
        String gri = resv.getGlobalReservationId();
        Session sshSession = null;
        
        /* Initialize ssh client */
        try{
            if(sshKey != null){
                jsch.addIdentity(sshKey);
             }
        }catch(JSchException e){
            this.log.error("SSH Error: " + e.getMessage());
            throw new PSSException(e.getMessage());
        }
        
        /* Create  ssh tunnel */
        if(sshPortForward != null && sshPortForward.equals("1")){
            try{
                String sshAddress = this.findSshAddress(ingressLink);
                sshSession = jsch.getSession(sshUser, sshAddress, 22);
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                sshSession.setConfig(config);
                sshSession.connect();
                sshSession.setPortForwardingL(port, telnetAddress, remotePort);
            }catch (JSchException e) {
                this.log.error("Unable to create SSH tunnel: " + e.getMessage());
                throw new PSSException(e.getMessage());
            }
        }
        
        /* Refresh LSP */
        this.log.info("logging into " + telnetAddress);
        csa.setPromptPattern(".*vlsr.*[>#]");
        if(csa.login(telnetAddress, port, password)){
            lsp = csa.getLSPByName(gri);
            if(lsp == null ||
                (!lsp.getStatus().equals(DragonLSP.STATUS_INSERVICE))){
                this.log.error("LSP " + gri + " failed. Status: " + 
                    lsp.getStatus());
                resv.setStatus("Failed");
                throw new PSSException("Path failure occured. LSP status is " + 
                                        lsp.getStatus());
            }
        }else{
            this.log.error("unable to login to " + telnetAddress + 
                ": " + csa.getError());
            if(sshSession != null){
                sshSession.disconnect();
             }
            throw new PSSException("Unable to reach ingress VLSR");
        }
        
        /* Remove port forwarding */
        if(sshPortForward != null && sshPortForward.equals("1")){
            sshSession.disconnect();
        }
       
        this.log.info("vlsr.refresh.end");
       
        return resv.getStatus();
    }
    
    /**
     * Removes LSP by running a VLSR "delete lsp" command
     *
     * @param resv the reservation whose path will be removed
     * @throws PSSException
     */
    public String teardownPath(Reservation resv) throws PSSException{
        this.log.info("vlsr.teardown.start");
        DragonCSA csa = new DragonCSA();
        JSch jsch = new JSch();
        String password = this.props.getProperty("password");
        String sshPortForward = this.props.getProperty("ssh.portForward");
        String sshUser = this.props.getProperty("ssh.user");
        String sshKey = this.props.getProperty("ssh.key");
        Path path = resv.getPath();
        Link ingressLink = path.getPathElem().getLink();  
        String gri = resv.getGlobalReservationId();
        String telnetAddress = this.findTelnetAddress(ingressLink);
        int port = this.findTelnetPort();
        int remotePort = Integer.parseInt(this.props.getProperty("remotePort"));
        Session sshSession = null;
        String prevStatus = resv.getStatus();
        
        /* Initialize ssh client */
        try{
            if(sshKey != null){
                jsch.addIdentity(sshKey);
            }
        }catch(JSchException e){
            this.log.error("SSH Error: " + e.getMessage());
            throw new PSSException(e.getMessage());
        }
        
        /* Create  ssh tunnel */
        if(sshPortForward != null && sshPortForward.equals("1")){
            try{
                String sshAddress = this.findSshAddress(ingressLink);
                sshSession = jsch.getSession(sshUser, sshAddress, 22);
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                sshSession.setConfig(config);
                sshSession.connect();
                sshSession.setPortForwardingL(port, telnetAddress, remotePort);
            }catch (JSchException e) {
                this.log.error("Unable to create SSH tunnel: " + e.getMessage());
                throw new PSSException(e.getMessage());
            }
        }
        
        /* Teardown LSP */
        csa.setPromptPattern(".*vlsr.*[>#]");
        if(csa.login(telnetAddress, port, password)){
            if(csa.teardownLSP(gri)){
                this.log.info("tore down lsp " + gri);
            }else{
                this.log.error("unable to teardown LSP " + gri
                    + ": " + csa.getError());
                resv.setStatus("FAILED");
                if(sshSession != null){
                    sshSession.disconnect();
                }
                throw new PSSException("Unable to teardown LSP: " + 
                    csa.getError());
            }
        }else{
            this.log.error("unable to login to " + telnetAddress
                + ": " + csa.getError());
             if(sshSession != null){
                sshSession.disconnect();
             }
            throw new PSSException("Unable to reach ingress VLSR");
        }
        
        /* Remove port forwarding */
        if(sshPortForward != null && sshPortForward.equals("1")){
            sshSession.disconnect();
        }
        
        /* Set the status of reservation */
        long currTime = System.currentTimeMillis();
        if(prevStatus.equals("PRECANCEL")){
            resv.setStatus("CANCELLED");
        }else if(currTime >= resv.getEndTime()){
            resv.setStatus("FINISHED");
        }else{
            resv.setStatus("PENDING");
        }
        
        this.log.info("vlsr.teardown.end");
        
        return resv.getStatus();
    }
    
    /**
     * Private utility method for retrieving the last hop 
     * in a stored path.
     *
     * @param path the path from which the egress will be retrieved
     * @return the egress link
     *
     */
    private Link getEgressLink(Path path){
        PathElem elem = path.getPathElem();
        PathElem prevElem = null;
        
        while(elem != null){
            prevElem = elem;
            elem = elem.getNextElem();
        }
        
        return prevElem.getLink();
    }
    
    /**
     * Private utility method for retrieving the last hop linkDescr
     * in a stored path.
     *
     * @param path the path from which the egress will be retrieved
     * @return the egress link
     *
     */
    private String getEgressLinkDescr(Path path){
        PathElem elem = path.getPathElem();
        PathElem prevElem = null;
        
        while(elem != null){
            prevElem = elem;
            elem = elem.getNextElem();
        }
        
        return prevElem.getLinkDescr();
    }
    
    /**
     * Retrieves an InetAddress version of the node address given a link.
     * 
     * @param link the Link from which the node address will be retrieved
     * @return node address of link as an InetAddress
     * @throws PSSException
     */
    private InetAddress linkToNodeInetAddress(Link link) throws PSSException{
        Node node = link.getPort().getNode();
        NodeAddress nodeAddr = node.getNodeAddress();
        InetAddress address = null;
        
        try{
            address = InetAddress.getByName(nodeAddr.getAddress());
        }catch (UnknownHostException e) {
			throw  new PSSException("unable to locate VLSR address");
		}
		
        return address;
    }
    
    /**
     * Converts a link to a local ID. Currently uses port as 
     * to get local ID value.
     *
     * @param link link whose port will be used to make local-id
     * @param tagged boolean that is true if local id is tagged
     *
     */
    private DragonLocalID linkToLocalId(Link link, int vtag) 
        throws PSSException{
        String portTopoId = link.getPort().getTopologyIdent();
        boolean tagged = (vtag >= 0);
        String type = null;
        int number = 0;
        
        /* Get Type */
        if(portTopoId.split("-").length == 2){
            this.log.info("subnet-interface local-id");
            type = DragonLocalID.SUBNET_INTERFACE;
            number = this.intefaceToLocalIdNum(portTopoId);
            this.log.info("local-id value " + number);
        }else if(tagged){
            this.log.info("tagged local-id");
            type = DragonLocalID.TAGGED_PORT_GROUP;
            number = vtag;
        }else{
            this.log.info("untagged local-id");
            /* Get number */
            number = this.intefaceToLocalIdNum(portTopoId);
            type = DragonLocalID.UNTAGGED_PORT;
        }
        return new DragonLocalID(number, type);
    }
    
    /**
     * Converts a port ID to a number useable by a local ID
     *
     * @param portTopoId the id to convert
     * @returns converted number
     * @throws PSSExcepion
     */
    private int intefaceToLocalIdNum(String portTopoId) throws PSSException{
        String[] componentList = portTopoId.split("-");
        int number = 0;
        
        if(componentList.length == 1){
            number = Integer.parseInt(componentList[0]);
        }else if(componentList.length == 2){
            number = (Integer.parseInt(componentList[1]) << 8) + 255;
        }else if(componentList.length == 3){
            int k = Integer.parseInt(componentList[0]);
            int m = Integer.parseInt(componentList[1]);
            int n = Integer.parseInt(componentList[2]);
            
            number = (k << 12) + (m << 8) + n;
        }else{
            throw new PSSException("Port ID must be in form L or K-M-N where" + 
            " L = (K << 12) + (M << 8) + N");
        }
        
        return number;
    }
    
    /**
     * Converts Long bandwidth value from database to a string recognized by the 
     * DRAGON CLI for ethernet requests. 
     *
     * @param bw Long bandwidth value from database
     * @return String value of given bandwidth understood by VLSR CLI
     */
    private String prepareL2Bandwidth(Long bw) throws PSSException{
        long bw100 =  (long)bw.longValue()/(100 * 1000000);
        String bwString = null;
        
        if(bw100 <= 1){
            bwString = DragonLSP.BANDWIDTH_ETHERNET_100M;
        }else if(bw <= 150000000){
            //special case of 150M
            bwString = DragonLSP.BANDWIDTH_ETHERNET_150M;
        }else if(bw100 <= 2){
            bwString = DragonLSP.BANDWIDTH_ETHERNET_200M;
        }else if(bw100 <= 3){
            bwString = DragonLSP.BANDWIDTH_ETHERNET_300M;
        }else if(bw100 <= 4){
            bwString = DragonLSP.BANDWIDTH_ETHERNET_400M;
        }else if(bw100 <= 5){
            bwString = DragonLSP.BANDWIDTH_ETHERNET_500M;
        }else if(bw100 <= 6){
            bwString = DragonLSP.BANDWIDTH_ETHERNET_600M;
        }else if(bw100 <= 7){
            bwString = DragonLSP.BANDWIDTH_ETHERNET_700M;
        }else if(bw100 <= 8){
            bwString = DragonLSP.BANDWIDTH_ETHERNET_800M;
        }else if(bw100 <= 9){
            bwString = DragonLSP.BANDWIDTH_ETHERNET_900M;
        }else if(bw100 <= 10){
            bwString = DragonLSP.BANDWIDTH_GIGE;
        }else if(bw100 <= 20){
            bwString = DragonLSP.BANDWIDTH_2GIGE;
        }else if(bw100 <= 30){
            bwString = DragonLSP.BANDWIDTH_3GIGE;
        }else if(bw100 <= 40){
            bwString = DragonLSP.BANDWIDTH_4GIGE;
        }else if(bw100 <= 50){
            bwString = DragonLSP.BANDWIDTH_5GIGE;
        }else if(bw100 <= 60){
            bwString = DragonLSP.BANDWIDTH_6GIGE;
        }else if(bw100 <= 70){
            bwString = DragonLSP.BANDWIDTH_7GIGE;
        }else if(bw100 <= 80){
            bwString = DragonLSP.BANDWIDTH_8GIGE;
        }else if(bw100 <= 90){
            bwString = DragonLSP.BANDWIDTH_9GIGE;
        }else if(bw100 <= 100){
            bwString = DragonLSP.BANDWIDTH_10G;
        }else{
            throw new PSSException("Unsupported bandwidth value");
        }
        
        return bwString;
    }
    
    /**
     * Retrieves the address used to login to the VLSR CLI. Checks the 
     * properties file first for a pss.dragon.<nodeId>=<telnetAddress> property 
     * where <nodeId> is the topology identifier of the node and telnetAddress 
     * is the IP used to access the VLSR CLI of that node. If null set to the * * node's address from the nodeAddresses tables.
     *
     * @param link the ingress link
     * @return string of IP Address used to access VLSR CLI
     * @throws PSSException
     */
    private String findTelnetAddress(Link link) throws PSSException{
        Node node = link.getPort().getNode();
        String telnetAddress = this.props.getProperty(node.getTopologyIdent());
        if(telnetAddress == null){
            telnetAddress = this.linkToNodeInetAddress(link).getHostAddress();
        }
        
        return telnetAddress;
    }
    
    /**
     * Retrieves the port used to login to the VLSR CLI. Checks the 
     * properties file first for a pss.dragon.<nodeId>.port=<port> property 
     * where <nodeId> is the topology identifier of the node and port 
     * is the port used to access the VLSR CLI of that node. If null set to the  
     * remotePort. Setting a specific port is most useful when using SSH tunneling.
     *
     * @param link the ingress link
     * @return string of port used to access VLSR CLI
     * @throws PSSException
     */
    private int findTelnetPort() throws PSSException{
        int port = Integer.parseInt(this.props.getProperty("remotePort"));
        String portProp = this.props.getProperty("localPort");
        if(portProp != null){
            port = Integer.parseInt(portProp);
        }
        
        return port;
    }
    
    /**
     * Retrieves the ssh address used for port forwarding. It looks in
     * properties file first for a pss.dragon.<nodeId>.ssh=<address> property 
     * where <nodeId> is the topology identifier of the node and address 
     * is the address used to access the VLSR via ssh.
     *
     * @param link the ingress link
     * @return string of IP Address used to access VLSR via ssh
     * @throws PSSException
     */
    private String findSshAddress(Link link) throws PSSException{
        Node node = link.getPort().getNode();
        String sshAddress = this.props.getProperty(node.getTopologyIdent() + ".ssh");
        if(sshAddress == null){
            this.log.error("SSH port forwarding selected but no ssh address configured for " +
                                node.getTopologyIdent() + " in oscars.properties");
            throw new PSSException("Unable to create circuit. There was a configuration error on"+
                                " the server side. Please contact IDC administrator");
        }
        
        return sshAddress;
    }
}
