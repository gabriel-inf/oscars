package net.es.oscars.bss.topology;

import net.es.oscars.*;
import net.es.oscars.bss.BSSException;
import net.es.oscars.database.*;
import net.es.oscars.wsdlTypes.*;

import org.apache.log4j.*;

import org.hibernate.*;

import org.hibernate.cfg.*;

import java.io.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// TODO: write javadocs
public class TopologyFlatFileReader {
    private Logger log;
    private Session ses;
    private Properties props;
    private String localDomain;
    private String rootTopoId;

    public TopologyFlatFileReader() {
        this.log = Logger.getLogger(this.getClass());

        PropHandler propHandler = new PropHandler("oscars.properties");

        this.props = propHandler.getPropertyGroup("topo", true);

        this.setLocalDomain(this.props.getProperty("localdomain").trim());
        this.setRootTopoId(this.props.getProperty("roottopoid").trim());
    }

    public String getRootTopoId() {
        return this.rootTopoId;
    }

    public void setRootTopoId(String topoId) {
        this.rootTopoId = topoId;
    }

    public String getLocalDomain() {
        return this.localDomain;
    }

    public void setLocalDomain(String domainId) {
        this.localDomain = domainId;
    }

    public void importFile(String fname)
        throws FileNotFoundException, IOException {
        this.log.debug("Start");

        Initializer initializer = new Initializer();
        List<String> dbnames = new ArrayList<String>();
        dbnames.add("bss");
        initializer.initDatabase(dbnames);

        this.ses = HibernateUtil.getSessionFactory("bss").getCurrentSession();

        Transaction tx = this.ses.beginTransaction();

        BufferedReader in = null;
        in = new BufferedReader(new FileReader(fname));

        this.parseFlatfile(in);
        tx.commit();

        this.log.debug("End");
    }

    protected void parseFlatfile(BufferedReader in) throws IOException {
        this.log.debug("Parsing flatfile");

        String[] cols = null;
        String line;

        while ((line = in.readLine()) != null) {
        	Long capacity = 0L;
            cols = line.split("\\s+");

            if (cols.length == 3) { 
            	capacity = TopologyUtil.understandBandwidth(cols[2]);
            }

            Link lftLink = this.parseEntry(cols[0], capacity);
            Link rgtLink = this.parseEntry(cols[1], capacity);

            if (lftLink != null) {
                lftLink.setRemoteLink(rgtLink);
                this.ses.save(lftLink);
            }

            if (rgtLink != null) {
                rgtLink.setRemoteLink(lftLink);
                this.ses.save(rgtLink);
            }
        }
    }

    protected Link parseEntry(String column, Long capacity) {

        if (column.equals("")) {
            return null;
        }

        this.log.debug("column: [" + column + "]");

//        String[] subcols = column.split(":");

        
        String domTopoId = this.rootTopoId + ":domain=" + TopologyUtil.getLSTI(column, "Domain");
        

        DomainDAO domainDAO = new DomainDAO("bss");
        List<Domain> currentDomains = domainDAO.list();
        
        Domain domDB = null;

        for (Domain currentDomain : currentDomains) {
            String fqti = TopologyUtil.getFQTI(currentDomain);
            if (domTopoId.equals(fqti)) {
            	domDB = currentDomain;
            	break;
            }
        }

        if (domDB == null) {
            domDB = TopologyUtil.initDomain();
            domDB.setName(TopologyUtil.getLSTI(column, "Domain"));
            domDB.setAbbrev(TopologyUtil.getLSTI(column, "Domain"));

            if (TopologyUtil.getLSTI(column, "Domain").equals(this.localDomain)) {
                domDB.setLocal(true);
            }

            domDB.setTopologyIdent(TopologyUtil.getLSTI(column, "Domain"));
            this.log.debug("New domain DB for topoIdent: [" + domTopoId + "]");
        } else {
            this.log.debug("Found domain DB for topoIdent: [" + domTopoId + "]");
        }

        if (domDB == null) {
            this.log.error("Domain is null!");

            return null;
        }

        this.ses.save(domDB);

        
        NodeDAO nodeDAO = new NodeDAO("bss");

        String nodeLSTI = TopologyUtil.getLSTI(column, "Node");;
        String nodeFQTI = domTopoId + ":node=" + nodeLSTI;
        Node nodeDB = nodeDAO.fromTopologyIdent(nodeLSTI, domDB);

        if (nodeDB == null) {
            nodeDB = TopologyUtil.initNode(domDB);
            nodeDB.setTopologyIdent(TopologyUtil.getLSTI(column, "Node"));
            nodeDAO.create(nodeDB);
            this.log.debug("New node DB for topoIdent: [" + nodeFQTI + "]");
        } else {
            this.log.debug("Found node DB for topoIdent: [" + nodeFQTI + "]");
        }

        
        
        PortDAO portDAO = new PortDAO("bss");
        String portLSTI = TopologyUtil.getLSTI(column, "Port");;
        String portFQTI = nodeFQTI + ":port=" + portLSTI;
        Port portDB = portDAO.fromTopologyIdent(portLSTI, nodeDB);

        if (portDB == null) {
        	portDB = TopologyUtil.initPort(nodeDB);
        	portDB.setTopologyIdent(TopologyUtil.getLSTI(column, "Port"));
            portDB.setAlias(TopologyUtil.getLSTI(column, "Port"));
            portDB.setCapacity(capacity);
            portDAO.create(portDB);
            this.log.debug("New port DB for topoIdent: [" + portFQTI + "], capacity: ["+capacity.toString()+"]");
        } else {
            this.log.debug("Found port DB for topoIdent: [" + portFQTI + "]");
        }


       
        LinkDAO linkDAO = new LinkDAO("bss");
        String linkLSTI = TopologyUtil.getLSTI(column, "Link");;
        String linkFQTI = portLSTI + ":link=" + linkLSTI;
        

        Link linkDB = linkDAO.fromTopologyIdent(linkLSTI, portDB);

        if (linkDB == null) {
            linkDB = TopologyUtil.initLink(portDB);
            linkDB.setTopologyIdent(TopologyUtil.getLSTI(column, "Link"));
            linkDB.setAlias(TopologyUtil.getLSTI(column, "Link"));
            linkDAO.create(linkDB);
            this.log.debug("New link DB for topoIdent: [" + linkFQTI + "]");
        } else {
            this.log.debug("Found link DB for topoIdent: [" + linkFQTI + "]");
        }


        return linkDB;
    }
}
