package net.es.oscars.bss.topology;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.hibernate.*;
import net.es.oscars.database.HibernateUtil;

import net.es.oscars.LogWrapper;
import net.es.oscars.bss.BSSException;

public class TopologyFiles {
    private LogWrapper log;
    private Session session;
    private List<Router> routersList;
    private List<Interface> interfacesList;
    private List<Ipaddr> ipaddrsList;
    private Integer routerId;
    private Integer interfaceId;
    private Integer ipaddrId;

    public TopologyFiles() {
        this.log = new LogWrapper(this.getClass());
        this.routersList = new ArrayList<Router>();
        this.interfacesList = new ArrayList<Interface>();
        this.ipaddrsList = new ArrayList<Ipaddr>();

        this.routerId=1;
        this.interfaceId=1;
        this.ipaddrId=1;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    // construct what the db should look like based
    // on the *.out snmp files

    public List<Router> constructTopology(String dirName)
            throws FileNotFoundException, IOException {

        String[] dataFileList = null;
        int lastSnapshot = -1;

        File snmpDir = new File(dirName);
        dataFileList = snmpDir.list(
            new FilenameFilter() {
                public boolean accept(File d, String name) {
                    return name.endsWith(".out");
                }
            } );

        for (String fname: dataFileList) {
            lastSnapshot = this.findLastSnapshot(dirName + "/" + fname);
            this.updateRouter(dirName, fname, lastSnapshot);
            routerId++;
        }

        System.out.println("\nFinished " + routerId + " files.");

        return routersList;
    }

    /**
     * Finds line number of last snapshot of router information.
     * @param fname A String containing the name of the output file.
     * @return lastSnapshotLine An int with the line number of the last
     *     router information snapshot.
     * @throws FileNotFoundException
     * @throws IOException
     *
     * An output file has a number of snapshots of router info at given times
     * in the current day.  The sections are delineated by a line containing
     * "sysUpTimeInstance".  The last snapshot is the only one used.  The
     * return value contains the line number of the first line containing
     * "ifDescr" past the last line with "sysUpTimeInstance".
     *
     * TODO:  make only one pass through the file.
     */
    private int findLastSnapshot(String fname)
        throws FileNotFoundException, IOException {

        BufferedReader in = null;
        Matcher matcher = null;
        String line = null;
        int lastSnapshotLine = -1;
        int lineNumber = 0;

        Pattern pattern = Pattern.compile(".*sysUpTimeInstance.*");

        in = new BufferedReader(new FileReader(fname));

        // System.out.println("working on file: " + fname);
        System.out.print(".");

        while ((line = in.readLine()) != null) {
            matcher = pattern.matcher(line);
            if (matcher.matches()) { lastSnapshotLine = lineNumber; }
            lineNumber++;
        }
        in.close();
        // Increment past line containing "sysUpTimeInstance", and blank
        // line.
        lastSnapshotLine += 2;
        return lastSnapshotLine;
    }

    /**
     * Reads data for one snmp output file (one per router)
     *
     * @param fname A String containing the name of the output file.
     * @param lastSnapshot An int with the line number of the last router info
     *     snapshot.  Only the last snapshot is processed.
     * @throws FileNotFoundException
     * @throws IOException
     *
     * SNMP data must be in the following format.
     *
     * time          var                          data
     * ...
     * 1117523019    sysUpTimeInstance.           1191745822
     * 
     * 1117523019    ifDescr.116                  lo0.0
     * ...
     * 1117523019    ifAlias.116                  snv2-sdn1::loopback:show:na
     * ...
     * 1117523019    ifSpeed.116                  0
     * ...
     * 1117523019    ifHighSpeed.116              0
     * 1117523019    ipAdEntIfIndex.127.0.0.1     116
     *
     * NOTE:  A better method of getting router information would be
     *        desirable.
     */
    private void updateRouter(String dname, String fname, int lastSnapshot)
            throws FileNotFoundException, IOException {

        Map<String,Interface> xfaces = new HashMap<String,Interface>();
        Map<String,Ipaddr> ipaddrs = new HashMap<String,Ipaddr>();
        Interface xface = null;
        Pattern pattern = null;
        Pattern loopbackPattern = null;
        Matcher matcher = null;
        Matcher loopbackMatcher = null;
        BufferedReader in = null;
        String[] columns = null;
        String line = null;
        String varColumn = null;
        String dataString = null;
        String snmpVar = null;
        String snmpValue = null;
        String mplsLoopback = null;
        Long speed = 0L;
        Long highSpeed = 0L;
        int lineNumber = 0;
        boolean valid = false;


        Set<Interface> xface_set = new HashSet<Interface>();
        Set<Ipaddr> ipaddr_set = new HashSet<Ipaddr>();

        this.log.debug("updateRouter.start", "routerName: " + fname);
        //System.out.println("updateRouter.start routerName: " + fname);

        Router router = new Router();
        router.setInterfaces(xface_set);

        /* setup */
        String rname = fname.substring(0,fname.length()-4);
        router.setName(rname);
        router.setId(routerId);
        router.setValid(true);
        routersList.add(router);

        pattern = Pattern.compile("([\\w]*)\\.{1}(.*)");
        loopbackPattern = Pattern.compile("134\\.55\\.75\\..*");


        // construct the full path
        fname = dname + "/" + fname;

        in = new BufferedReader(new FileReader(fname));
        // skip to last snapshot
        while (lineNumber < lastSnapshot) {
            in.readLine();
            lineNumber++;
        }

        while ((line = in.readLine()) != null) {

            columns = line.split("\\s+");

            // time column unused, but must be at least three columns
            if (columns.length < 3) { continue; }
            varColumn = columns[1];
            dataString = columns[2];

            // Splits on first period (couldn't get String.split to work
            // properly on escaped period).
            matcher = pattern.matcher(varColumn);
            if (!matcher.matches()) { continue; }

            snmpVar = matcher.group(1);
            snmpValue = matcher.group(2);
            // new Interface created for every line in this section
            // note that sections are expected in thie order
            // TODO:  less fragile

            //System.out.println("snmpVar == " + snmpVar);

            if (snmpVar.equals("ifDescr")) {
                xface = new Interface();

                xface.setDescription(dataString);
                xfaces.put(snmpValue, xface);
                interfacesList.add(xface);
                // add to the routers list 
                router.addInterface(xface);
                xface.setId(interfaceId);
                // create set to hold Ipaddrs
                xface.setIpaddrs(new HashSet<Ipaddr>());
                
                interfaceId++;

            } else if (snmpVar.equals("ifAlias")) {
                xface = xfaces.get(snmpValue);
                xface.setAlias(dataString);
            } else if (snmpVar.equals("ifSpeed")) {
                xface = xfaces.get(snmpValue);
                speed = Long.parseLong(dataString);
                if (speed == 0) { 
                    xface.setSpeed(speed);
                    continue; 
                }
                // note this may be overwritten in ifHighSpeed section
                xface.setSpeed(speed);
            } else if (snmpVar.equals("ifHighSpeed")) {
                highSpeed = Long.parseLong(dataString);
                xface = xfaces.get(snmpValue);
                if (highSpeed == 0) { 
                    xface.setSpeed(highSpeed);
                    continue; 
                }
                speed = xface.getSpeed();
                if (speed < (highSpeed * 1000000)) {
                    xface.setSpeed(highSpeed * 1000000);
                }
            } else if (snmpVar.equals("ipAdEntIfIndex")) {
        
                Ipaddr ipaddr = new Ipaddr();

                xface = xfaces.get(dataString);
                ipaddr.setInterface(xface);

                loopbackMatcher = loopbackPattern.matcher(snmpValue);

                //System.out.println("snmpvalue ["+snmpValue+"]");

                if (loopbackMatcher.matches()) {
                    //System.out.println("Setting loopback for " + snmpValue);
                    mplsLoopback = snmpValue;
                    ipaddr.setDescription("loopback");
                }

                ipaddrsList.add(ipaddr);
                ipaddr.setId(ipaddrId);
                ipaddr.setIp(snmpValue);

                xface.addIpaddr(ipaddr);
                ipaddrId++;
                
            }
        }
       in.close();
       this.log.debug("updateRouter.finish","routerName: "+router.getName());
    }

    public void listInterfaces(Router router) {

       System.out.println("Router: " + router.getName());

       Set<Interface> ifaces = router.getInterfaces();
       System.out.println("topologyFiles: number of xfaces is "+ifaces.size());

        for ( Interface i: ifaces) {
                System.out.println("    Interface: " + i.getDescription() + 
                        "  (" + i.getId() + ")" );
                Integer id = i.getId();
                Set<Ipaddr> iplist = i.getIpaddrs();
                for (Ipaddr ip : iplist) {
                     System.out.println("       IP: " + ip.getIp()
                             + " (" + ip.getId() + ")");
                }
        }
    }

    /*
     * Finds the IPaddrID of the IP address
     */
    public Integer findIp(String ipString) {
        
        for (Ipaddr i: ipaddrsList ) {
            String dbIp = i.getIp();
            if ( 0 == ipString.compareTo(dbIp) ) {
                return i.getId();
            }
        }
        System.out.println("Not found: " + ipString);
        return -1;
    }

    public Ipaddr getIpAddr(String ipString) {

        for (Ipaddr i: ipaddrsList ) {
            String dbIp = i.getIp();
            if ( 0 == ipString.compareTo(dbIp) ) {
                return i;
            }
        }
        System.out.println("Not found: " + ipString);
        return null;
    }
}
