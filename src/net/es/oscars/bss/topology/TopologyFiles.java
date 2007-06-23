package net.es.oscars.bss.topology;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.log4j.*;

import net.es.oscars.bss.BSSException;

public class TopologyFiles {
    private Logger log;
    private List<Node> nodeList;

    public TopologyFiles() {
        this.log = Logger.getLogger(this.getClass());
        this.nodeList = new ArrayList<Node>();
    }

    // construct what the db should look like based
    // on the *.out snmp files

    public List<Node> constructTopology(String dirName)
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
            this.updateNode(dirName, fname, lastSnapshot);
        }
        return this.nodeList;
    }

    /**
     * Finds line number of last snapshot of node information.
     * @param fname A String containing the name of the output file.
     * @return lastSnapshotLine An int with the line number of the last
     *     node information snapshot.
     * @throws FileNotFoundException
     * @throws IOException
     *
     * An output file has a number of snapshots of node info at given times
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
     * Reads data for one snmp output file (one per node)
     *
     * @param fname A String containing the name of the output file.
     * @param lastSnapshot An int with the line number of the last node info
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
     * NOTE:  A better method of getting node information would be
     *        desirable.
     */
    private void updateNode(String dname, String fname, int lastSnapshot)
            throws FileNotFoundException, IOException {

        Map<String,Port> ports = new HashMap<String,Port>();
        Port port = null;
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

        this.log.debug("updateNode.start reading file: " + fname);

        Node node = new Node();
        String rname = fname.substring(0,fname.length()-4);
        node.setName(rname);
        // create set to hold ports
        node.setPorts(new HashSet<Port>());
        this.nodeList.add(node);

        // if empty file
        if (lastSnapshot == -1) {
            this.log.debug("file for node " + rname + " is empty");
            node.setValid(false);
            return;
        }

        node.setValid(true);
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
            // new Port created for every line in this section
            // note that sections are expected in thie order
            // TODO:  less fragile

            if (snmpVar.equals("ifDescr")) {
                port = new Port();
                port.setDescription(dataString);
                port.setValid(true);
                port.setSnmpIndex(Integer.parseInt(snmpValue.trim()));
                // create set to hold Ipaddrs
                port.setIpaddrs(new HashSet<Ipaddr>());
                ports.put(snmpValue, port);

            } else if (snmpVar.equals("ifAlias")) {
                port = ports.get(snmpValue);
                port.setAlias(dataString);
            } else if (snmpVar.equals("ifSpeed")) {
                port = ports.get(snmpValue);
                speed = Long.parseLong(dataString);
                if (speed == 0) { 
                    port.setMaximumCapacity(speed);
                    port.setMaximumReservableCapacity(
                        Long.valueOf((long)(port.getMaximumCapacity() / 2.0)));
                    continue; 
                }
                // note this may be overwritten in ifHighSpeed section
                port.setMaximumCapacity(speed);
                port.setMaximumReservableCapacity(
                        Long.valueOf((long)(port.getMaximumCapacity() / 2.0)));
            } else if (snmpVar.equals("ifHighSpeed")) {
                highSpeed = Long.parseLong(dataString);
                port = ports.get(snmpValue);
                if (highSpeed == 0) { 
                    port.setMaximumCapacity(highSpeed);
                    port.setMaximumReservableCapacity(
                        Long.valueOf((long)(port.getMaximumCapacity() / 2.0)));
                    continue; 
                }
                speed = port.getMaximumCapacity();
                if (speed < (highSpeed * 1000000)) {
                    port.setMaximumCapacity(highSpeed * 1000000);
                    port.setMaximumReservableCapacity(
                        Long.valueOf((long)(port.getMaximumCapacity() / 2.0)));
                }
            } else if (snmpVar.equals("ipAdEntIfIndex")) {
                Ipaddr ipaddr = new Ipaddr();
                port = ports.get(dataString);

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
                // add to the ports's set of ipaddrs
                port.addIpaddr(ipaddr);
            }
        }
        in.close();
        // need to do here so equality check is better;
        // set can't have duplicates
        for (Port p: ports.values()) {
            node.addPort(p);
        }
        this.log.debug("updateNode.finish node: " + node.getName());
    }
}
