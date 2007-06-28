import java.util.*;

import org.apache.log4j.*;

import net.es.oscars.bss.TopologyManager;
import net.es.oscars.bss.topology.*;

/**
 * This class updates the topology database.  It reads all files in the given
 *   directory in order to build the new topology in memory.  It then updates
 *   the relevant tables in the database to take into account any new
 *   topology, while keeping old path info usable.
 */
public class TopologyUpdater {

    private Logger log;

    public static void main(String[] argv) {
        TopologyUpdater populater = new TopologyUpdater();
        populater.update(argv);
    }

    public void update(String[] argv) {

        String usage = "updatedb [-v] [-d] -f /path/to/files";
        int optind;

        String directoryName = null;
        String dryrun = null;

        // for now
        this.log = Logger.getLogger(this.getClass());
        this.log.info("TopologyUpdater starting");
        for (optind = 0; optind < argv.length; optind++) {
            if (argv[optind].equals("-d")) {
                dryrun = "active";
            } else if (argv[optind].equals("-f")) {
                directoryName = argv[++optind];
            } else if (argv[optind].startsWith("-")) {
                System.out.println("" + usage);
                System.out.println("-- Invalid option " + argv[optind]);
                System.exit(1);
            } else {
                break;
            }
        }
        if (directoryName == null) {
            System.out.println(usage);
            System.exit(1);
        }
        this.log.info("dryrun: " + dryrun);
        this.log.info("directory: " + directoryName);

        // nodes have associations with interfaces and ipaddrs tabble
        List<Node> newNodes = null;

        TopologyFiles newFiles = new TopologyFiles();
        TopologyManager topoMgr = new TopologyManager("bss");

        this.log.info("creating new topology in memory");
        try {
            // create a new topology in memory from list of files associated
            // with each node
            newNodes = newFiles.constructTopology(directoryName);
            this.log.info("new topology in memory created");
        } catch (Exception e) {
            this.log.error("Unknown exception: " + e.getMessage());
            System.out.println("Unknown exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        /*
        for (Node node: newNodes) {
            this.log.info("name: " + node.getName());
            Set<Port> ports = (Set<Port>) node.getPorts();
            for (Port port: ports) {
                String description = port.getDescription();
                if (description != null) {
                    this.log.info(description);
                } else {
                    this.log.info("null");
                }
            }
        }
        */
        if (dryrun != null) {
            this.log.info("dryrun.finish");
            System.exit(1);
        }
        this.log.info("updating database");
        topoMgr.updateDb(newNodes);
        this.log.info("TopologyUpdater finished: new topology in place");
    }
}
