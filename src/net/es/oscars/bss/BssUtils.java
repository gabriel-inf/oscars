package net.es.oscars.bss;

import java.util.*;

import org.apache.log4j.*;

import net.es.oscars.bss.topology.*;

/**
 * @author David Robertson (dwrobertson@lbl.gov)
 *
 * This class contains utility methods for use by the reservation manager.
 */
public class BssUtils {
    private static Logger log = Logger.getLogger(BssUtils.class);

    // utils class, do not instantiate
    private BssUtils() {
    }

    /**
     * Gets path depending on reservation state and set of paths
     * available.  Used where need to choose just one of the paths,
     * for example in list and query.
     *
     * @param resv Reservation with set of paths
     * @return path Path chosen
     */
    public static Path getPath(Reservation resv) throws BSSException {
        String status = resv.getStatus();
        Path requestedPath = resv.getPath(PathType.REQUESTED);
        if ((requestedPath != null) && (status.equals("SUBMITTED") ||
            status.equals("ACCEPTED"))) {
            return requestedPath;
        }
        Path interdomainPath = resv.getPath(PathType.INTERDOMAIN);
        if (interdomainPath != null) {
            return interdomainPath;
        }
        Path localPath = resv.getPath(PathType.LOCAL);
        if (localPath != null) {
            return localPath;
        }
        // punt; may be null.  TODO:  throw exception if so?
        return requestedPath;
    }

    /**
     * Copies fields that for now are in common to various types of paths.
     * Currently pathSetupMode and layer specific information is in common
     * 
     * @param Path Path with information to copy
     * @param updatePath Path with information to update
     */
    public static void copyPathFields(Path path, Path updatePath)
            throws BSSException {
        
        updatePath.setPathSetupMode(path.getPathSetupMode());
        if (path.getLayer2Data() != null) {
            Layer2Data layer2DataCopy = path.getLayer2Data().copy();
            updatePath.setLayer2Data(layer2DataCopy);
        }
        if (path.getLayer3Data() != null) {
            Layer3Data layer3DataCopy = path.getLayer3Data().copy();
            updatePath.setLayer3Data(layer3DataCopy);
        }
        if (path.getMplsData() != null) {
            MPLSData mplsDataCopy = path.getMplsData().copy();
            updatePath.setMplsData(mplsDataCopy);
        }
    }

    /**
     * Converts data associated with a Hibernate path to a series of strings.
     *
     * @param path path to convert to string
     * @return pathDataStr path data in string format
     * @throws BSSException
     */
    public static String pathDataToString(Path path) throws BSSException {
        StringBuilder sb =  new StringBuilder();
        if (path.getPathSetupMode() != null) {
            sb.append("path setup mode: " + path.getPathSetupMode() + "\n");
        }
        Layer2Data layer2Data = path.getLayer2Data();
        if (layer2Data != null) {
            sb.append("layer: 2\n");
            if (layer2Data.getSrcEndpoint() != null) {
                sb.append("source endpoint: " +
                      layer2Data.getSrcEndpoint() + "\n");
            }
            if (layer2Data.getDestEndpoint() != null) {
                sb.append("dest endpoint: " +
                          layer2Data.getDestEndpoint() + "\n");
            }
            List<PathElem> pathElems = path.getPathElems();
            if (!pathElems.isEmpty()) {
                PathElem pathElem = pathElems.get(0);
                PathElemParam pep =
                    pathElem.getPathElemParam(PathElemParamSwcap.L2SC,
                                         PathElemParamType.L2SC_VLAN_RANGE);
                String linkDescr = pep.getValue();
                if (linkDescr != null) {
                    sb.append("VLAN tag: " + linkDescr + "\n");
                }
            }
        }
        Layer3Data layer3Data = path.getLayer3Data();
        if (layer3Data != null) {
            sb.append("layer: 3\n");
            if (layer3Data.getSrcHost() != null) {
                sb.append("source host: " + layer3Data.getSrcHost() + "\n");
            }
            if (layer3Data.getDestHost() != null) {
                sb.append("dest host: " + layer3Data.getDestHost() + "\n");
            }
            if (layer3Data.getProtocol() != null) {
                sb.append("protocol: " + layer3Data.getProtocol() + "\n");
            }
            if ((layer3Data.getSrcIpPort() != null) &&
                (layer3Data.getSrcIpPort() != 0)) {
                sb.append("src IP port: " + layer3Data.getSrcIpPort() + "\n");
            }
            if ((layer3Data.getDestIpPort() != null) &&
                (layer3Data.getDestIpPort() != 0)) {
                sb.append("dest IP port: " +
                          layer3Data.getDestIpPort() + "\n");
            }
            if (layer3Data.getDscp() != null) {
                sb.append("dscp: " +  layer3Data.getDscp() + "\n");
            }
        }
        MPLSData mplsData = path.getMplsData();
        if (mplsData != null) {
            if (mplsData.getBurstLimit() != null) {
                sb.append("burst limit: " + mplsData.getBurstLimit() + "\n");
            }
            if (mplsData.getLspClass() != null) {
                sb.append("LSP class: " + mplsData.getLspClass() + "\n");
            }
        }
        return sb.toString();
    }

    /**
     * Converts Hibernate path to a series of identifier strings.
     *
     * @param path path to convert to string
     * @param interDomain boolean for intra or interdomain path
     * @return pathStr converted path
     */
    public static String pathToString(Path path, boolean interDomain) {

        Ipaddr ipaddr = null;
        String nodeName = null;
        String param = null;

        // FIXME:  more null checks may be necessary
        if (path == null) {
            return "";
        }
        List<PathElem> pathElems = path.getPathElems();
        if (pathElems == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int sz = pathElems.size();
        int i = 0;
        for (PathElem pathElem: pathElems) {
            Link link = pathElem.getLink();
            if(i != 0){
                sb.append("\n");
            }
            //  send back topology identifier in both layer 2 and layer 3 case
            if (link != null) {
                String fqti = link.getFQTI();
                sb.append(fqti);
            } else {
                sb.append(pathElem.getUrn());
            }
            i++;
        }
        // in this case, all hops are local
        if (interDomain && (sz == 2)) {
            return "";
        // internal path has not been set up
        // NOTE:  this depends on the current implementation sometimes having
        //        one hop in the path from when the reservation has been in
        //        the ACCEPTED state, but the path has not been or may never
        //        be set up.
        } else if (!interDomain && (sz == 1)) {
            return "";
        }
        String pathStr = sb.toString();
        return pathStr;
    }

    /**
     * Gets VLAN tag given path.  Assumes just one VLAN tag in path for now.
     *
     * @param path Path with reservation's page
     * @return vlanTags list of strings with VLAN tag for each hop, if any
     * @throws BSSException
     */
    public static List<String> getVlanTags(Path path) throws BSSException {
        List<PathElem> pathElems = path.getPathElems();
        List<String> vlanTags = new ArrayList<String>();
        if ((pathElems == null) || pathElems.isEmpty()) {
            log.info("null or empty");
            return vlanTags;
        }
        for (PathElem pathElem: pathElems) {
            pathElem.initializePathElemParams();
            PathElemParam pep =
                pathElem.getPathElemParam(PathElemParamSwcap.L2SC,
                                          PathElemParamType.L2SC_VLAN_RANGE);
            if (pep == null) {
                log.info("pep is null");
                vlanTags.add(null);
            } else {
                String vlanTag = pep.getValue();
                vlanTags.add(vlanTag);
            }
        }
        return vlanTags;
    }

    /**
     * String joiner
     * @param s a Collection of objects to join (uses toString())
     * @param delimiter the delimiter
     * @param quote a string to prefix each object with (null for none)
     * @param unquote a string to postfix each object with (null for none)
     * @return joined the objects, quoted & unquoted, joined by the delimiter
     */
    public static String join(Collection s, String delimiter, String quote, String unquote) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        Iterator iter = s.iterator();
        while (iter.hasNext()) {
            buffer.append(quote);
            buffer.append(iter.next());
            buffer.append(unquote);
            if (iter.hasNext()) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }
}
