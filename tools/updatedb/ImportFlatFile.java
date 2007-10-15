import net.es.oscars.bss.topology.*;

import java.io.*;
import java.util.*;


/**
 * This class updates the topology database. It will read a single
 * file of links, row by row into the database. This class is meant to 
 * assist OSCARS administrators in importing static domain edge topology 
 * files, but can be used to import any sort of links.
 * File format looks like this:
 * domId:nodeId:portId:linkId remDomId:remNodeid:remPortId:remLinkId
 * Examples:
 * 293:sunn-rt1:Ge1/1:* 844:router-3:Ge2/0:*
 * 293:pitt-rt3:Ge3/2:Ge3/2.200 712:router-9:Ge3/0:Ge3/0.200
 * 
 * @author Evangelos Chaniotakis (haniotak /at/ es dot net
 */
public class ImportFlatFile {
    public static void main(String[] argv) {
        TopologyFlatFileReader reader = new TopologyFlatFileReader();
        String usage = "Usage:\nimportFlatFile.sh /path/to/file";
        String filename = "";

        if (argv.length != 1) {
            System.out.println(""+usage);
            System.exit(1);
        }
        filename = argv[0];

        try {
            reader.importFile(filename);
        } catch (IOException ex) {
            System.out.println("Error reading file ["+filename+"].");
            System.exit(1);
        }
    }
}
