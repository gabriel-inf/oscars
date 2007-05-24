package net.es.oscars.bss.topology;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.log4j.*;

import net.es.oscars.bss.BSSException;

public class TopologyFiles {
    private Logger log;
    private List<Router> routersList;

    public TopologyFiles() {
        this.log = Logger.getLogger(this.getClass());
        this.routersList = new ArrayList<Router>();
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
        }
        return this.routersList;
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

        while ((line = in.readLine()) != null) {
            matcher = pattern.matcher(line);
            if (matcher.matches()) { lastSnapshotLine = lineNumber; }
            lineNumber++;
        }
        in.close();
        // Increment past line containing "sysUpTimeInstance", and blank
        // line, if there are any lines in the file.
        if (lastSnapshotLine != -1) {
            lastSnapshotLine += 2;
        }
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
        Interface xface = null;
        Matcher matcher = null;
        Matcher oscarsMatcher = null;
        Matcher wanMatcher = null;
        String[] columns = null;
        String line = null;
        String varColumn = null;
        String dataString = null;
        String snmpVar = null;
        String snmpValue = null;
        Long speed = 0L;
        Long highSpeed = 0L;
        int lineNumber = 0;
        boolean valid = false;

        this.log.debug("updateRouter.start reading file: " + fname);

        Router router = new Router();
        String rname = fname.substring(0,fname.length()-4);
        router.setName(rname);
        // create set to hold interfaces
        router.setInterfaces(new HashSet<Interface>());
        this.routersList.add(router);

        // if empty file
        if (lastSnapshot == -1) {
            this.log.debug("file for router " + rname + " is empty");
            router.setValid(false);
            return;
        }

        router.setValid(true);
        Pattern pattern = Pattern.compile("([\\w]*)\\.{1}(.*)");
        Pattern oscarsPattern = Pattern.compile("134\\.55\\.75\\..*");
        Pattern wanPattern = Pattern.compile("134\\.55\\..*");
        fname = dname + "/" + fname;     // construct the full path

        BufferedReader in = new BufferedReader(new FileReader(fname));
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

            if (snmpVar.equals("ifDescr")) {
                xface = new Interface();
                xface.setDescription(dataString);
                xface.setValid(true);
                xface.setSnmpIndex(Integer.parseInt(snmpValue.trim()));
                // create set to hold Ipaddrs
                xface.setIpaddrs(new HashSet<Ipaddr>());
                xfaces.put(snmpValue, xface);

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

                oscarsMatcher = oscarsPattern.matcher(snmpValue);
                if (oscarsMatcher.matches()) {
                    ipaddr.setDescription("oscars-loopback");
                } else {
                    wanMatcher = wanPattern.matcher(snmpValue);
                    if (wanMatcher.matches()) {
                        ipaddr.setDescription("wan-loopback");
                    }
                }
                ipaddr.setIP(snmpValue);
                ipaddr.setValid(true);
                // add to the interface's set of ipaddrs
                xface.addIpaddr(ipaddr);
            }
        }
        in.close();
        // need to do here so equality check is better;
        // set can't have duplicates
        for (Interface xf: xfaces.values()) {
            router.addInterface(xf);
        }
        this.log.debug("updateRouter.finish router: " + router.getName());
    }
}
