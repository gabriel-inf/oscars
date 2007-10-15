import net.es.oscars.bss.topology.*;

import java.util.*;

/**
 * This class updates the topology database.  It reads a single XML
 * file into the database. The file should be in the topology
 * control plane exchange schema. The XML importer will do all
 * necessary changes to the database. (currently only adding is\
 * supported)
 * @author Evangelos Chaniotakis (haniotak /at/ es dot net
 */

public class ImportXML {
    public static void main(String[] argv) {
        TopologyXMLFileReader reader = new TopologyXMLFileReader();
        String usage = "Usage:\nimportXMLFile.sh /path/to/file";
        String filename = "";

        if (argv.length != 1) {
            System.out.println(""+usage);
            System.exit(1);
        }
        filename = argv[0];

        reader.importFile(filename);
    }
}
