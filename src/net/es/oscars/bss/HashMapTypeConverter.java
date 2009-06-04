/**
 * Performs and supports conversion operations on Hibernate Paths.
 *
 * @author Andrew Lake, David Robertson
 */
package net.es.oscars.bss;

import java.util.*;

import org.apache.log4j.*;

import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.Token;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.topology.*;


/**
 * Has methods to perform and support conversion operations between hash
 * maps and beans.
 */
public class HashMapTypeConverter {

    private static Logger log = Logger.getLogger(HashMapTypeConverter.class);
    final public static String DEFAULT_TE_METRIC = "10";

    // do not instantiate
    private HashMapTypeConverter() {
    }

    /**
     * Converts Reservation Hibernate bean to a HashMap
     *
     * @param resv the Reservation to convert
     * @return the converted HashMap
     * @throws BSSException
     */
    public static HashMap<String, String[]>
                    reservationToHashMap(Reservation resv) throws BSSException{

        HashMap<String, String[]> map = new HashMap<String, String[]>();
        if(resv == null){
            return map;
        }

        map.put("startSeconds", genHashVal(resv.getStartTime() + ""));
        map.put("endSeconds", genHashVal(resv.getEndTime() + ""));
        map.put("createSeconds", genHashVal(resv.getCreatedTime() + ""));
        map.put("bandwidth", genHashVal(resv.getBandwidth() + ""));
        map.put("status", genHashVal(resv.getStatus()));
        map.put("description", genHashVal(resv.getDescription()));
        map.put("gri", genHashVal(resv.getGlobalReservationId()));
        map.put("userLogin", genHashVal(resv.getLogin()));

        //set Token
        Token token = resv.getToken();
        if (token != null) {
            map.put("token", genHashVal(token.getValue()));
        }

        //set path
        try {
            if(resv.getPath(PathType.INTERDOMAIN) == null && resv.getPath(PathType.LOCAL) == null){
                map.putAll(pathToHashMap(resv.getPath(PathType.REQUESTED)));
            }else{
                map.putAll(pathToHashMap(resv.getPath(PathType.LOCAL)));
                map.putAll(pathToHashMap(resv.getPath(PathType.INTERDOMAIN)));
            }
        }catch (Exception e){
            log.error ("caught Exception trying to get path " + e.getMessage() );
        }
        return map;
    }


    /**
     * Converts Path Hibernate bean to a HashMap
     *
     * @param path the Path to convert
     * @return map the converted HashMap
     * @throws BSSException
     * @throws BSSException
     */
    public static HashMap<String, String[]> pathToHashMap(Path path) throws BSSException {

        HashMap<String, String[]> map = new HashMap<String, String[]>();
        ArrayList<String> layers = new ArrayList<String>();
        if (path == null) {
            return map;
        }
        Domain nextDomain = path.getNextDomain();
        Layer2Data layer2Data = path.getLayer2Data();
        Layer3Data layer3Data = path.getLayer3Data();
        MPLSData mplsData = path.getMplsData();
        List<PathElem> pathElems = path.getPathElems();
        ArrayList<String> pathListStr = new ArrayList<String>();
        String src = null;
        String dest = null;

        map.put("pathSetupMode", genHashVal(path.getPathSetupMode()));
        if(nextDomain != null){
            map.put("nextDomain", genHashVal(nextDomain.getTopologyIdent()));
        }
        if(path.isLayer3()){
            src = layer3Data.getSrcHost();
            dest = layer3Data.getDestHost();
            map.put("source", genHashVal(src));
            map.put("destination", genHashVal(dest));
            //these are in the TCP/UDP headers, not IP headers, hence L4
            map.put("srcPort", genHashVal(layer3Data.getSrcIpPort() + ""));
            map.put("destPort", genHashVal(layer3Data.getDestIpPort() + ""));
            map.put("protocol", genHashVal(layer3Data.getProtocol()));
            map.put("dscp", genHashVal(layer3Data.getDscp()));
            map.put("layer", genHashVal("3"));
            layers.add("3");
        }

        if(path.isLayer2()){
            src = layer2Data.getSrcEndpoint();
            dest = layer2Data.getDestEndpoint();
            map.put("source", genHashVal(src));
            map.put("destination", genHashVal(dest));
            layers.add("2");
        }
        map.put("layer", layers.toArray(new String[layers.size()]));

        if(mplsData != null){
            map.put("burstLimit", genHashVal(mplsData.getBurstLimit() + ""));
            map.put("lspClass", genHashVal(mplsData.getLspClass()));
        }

        String pathType = path.getPathHopType() == null ? "strict" : path.getPathHopType();
        map.put("pathType", genHashVal(pathType));

        ArrayList<String> pathHopInfo = new ArrayList<String>();
        for (PathElem pathElem: pathElems) {
            String linkId = pathElem.getUrn();
            pathListStr.add(linkId);
            if (path.isLayer2()) {
                pathHopInfo.add(getPathElemInfo(pathElem));
                map.putAll(vlanToHashMap(pathElem, src, dest, layer2Data));
            }
        }
        if (path.getPathType().equals(PathType.LOCAL)) {
            map.put("intradomainPath", pathListStr.toArray(new String[pathListStr.size()]));
            map.put("intradomainHopInfo", pathHopInfo.toArray(new String[pathHopInfo.size()]));
        } else {
            map.put("interdomainPath", pathListStr.toArray(new String[pathListStr.size()]));
            map.put("interdomainHopInfo", pathHopInfo.toArray(new String[pathHopInfo.size()]));
        }
        return map;
    }

    /**
     * Creates a ';' delimited String with detailed information about each hop
     * in a path.
     *
     * @param pathElem the pathElem for which to generate information
     * @return a ';' delimited String with detailed information about each hop
     * @throws BSSException
     */
    private static String getPathElemInfo(PathElem pathElem) throws BSSException {
        String infoVal = "";
        String defaulSwcapType = L2SwitchingCapType.DEFAULT_SWCAP_TYPE;
        String defaulEncType = L2SwitchingCapType.DEFAULT_ENC_TYPE;
        String mtu = L2SwitchingCapType.DEFAULT_MTU +"";
        if(pathElem.getLink() == null){
            infoVal = HashMapTypeConverter.DEFAULT_TE_METRIC;
        }else{
            infoVal = pathElem.getLink().getTrafficEngineeringMetric();
            if(pathElem.getLink().getL2SwitchingCapabilityData() != null){
                mtu = pathElem.getLink().getL2SwitchingCapabilityData().getInterfaceMTU() + "";
            }
        }

        //Check for layer 2 params
        PathElemParam pepVlanRange = pathElem.getPathElemParam(PathElemParamSwcap.L2SC,
                PathElemParamType.L2SC_VLAN_RANGE);
        if(pepVlanRange != null){
            String vlanRangeVal = pepVlanRange.getValue();
            if(vlanRangeVal != null && vlanRangeVal.startsWith("-")){
                vlanRangeVal = "0";
            }
            //TEMetric;swcap;enc;MTU;VLANRangeAvail;SuggestedVLANRange
            infoVal += ";l2sc;ethernet";
            infoVal += ";" + mtu;
            infoVal += ";" + vlanRangeVal;
            PathElemParam pepSugVlan =
                pathElem.getPathElemParam(PathElemParamSwcap.L2SC,
                        PathElemParamType.L2SC_SUGGESTED_VLAN);
            if(pepSugVlan==null){
                infoVal += ";null";
            }else if("0".equals(vlanRangeVal)){
                infoVal += ";0";
            }else{
                infoVal += (";" + pepSugVlan.getValue());
            }
        }else{
            //TEMetric;swcap;enc;MTU;capbility
            infoVal += ";" + defaulSwcapType + ";" + defaulEncType + ";unimplemented";
        }

        return infoVal;
    }

    /**
     * Converts PathElem Hibernate bean of a layer2 link to a HashMap
     *
     * @param elem the PathElem to convert
     * @param src the source URN of the reservation
     * @param dest the destination URN of the reservation
     * @param layer2Data the layer 2 data associated with a reservation
     * @return the converted HashMap
     */
    private static HashMap<String, String[]> vlanToHashMap(PathElem elem, String src,
                                                    String dest,
                                                    Layer2Data layer2Data){
        HashMap<String, String[]> map = new HashMap<String, String[]>();
        if(layer2Data == null){
            return map;
        }

        PathElemParam descr = null;
        try {
            descr = elem.getPathElemParam(PathElemParamSwcap.L2SC, 
                        PathElemParamType.L2SC_VLAN_RANGE);
        } catch (BSSException e1) {
            return map;
        }
        if(descr == null || descr.getValue() == null){
            return map;
        }
        
        String linkId = elem.getUrn();
        String tagField = "";
        if(linkId.equals(src)){
            tagField = "tagSrcPort";
            try{
                int vtag = Integer.parseInt(descr.getValue());
                map.put(tagField, genHashVal(vtag > 0 ? "true" : "false"));
                map.put("srcVtag", genHashVal(Math.abs(vtag)+""));
            }catch(Exception e){}
        }else if(linkId.equals(dest)){
            tagField = "tagDestPort";
            try{
                int vtag = Integer.parseInt(descr.getValue());
                map.put(tagField, genHashVal(vtag > 0 ? "true" : "false"));
                map.put("destVtag", genHashVal(Math.abs(vtag)+""));
            }catch(Exception e){}
        }

        return map;
    }

    /**
     * Generates a String array from a String
     *
     * @param value the String to convert
     * @return the converted array
     */
    private static String[] genHashVal(String value){
        if(value == null){
            return null;
        }
        String[] array = new String[1];
        array[0] = value;
        return array;
    }

    /**
     * Extracts a String from a String array
     *
     * @param array the String[] to extract
     * @return the converted array
     */
    public static String extractHashVal(String[] array){
        if(array == null || array.length < 1){
            return null;
        }
        return array[0];
    }

    /**
     * Extracts a long from a String array
     *
     * @param array the String[] to extract
     * @return the converted array
     */
    public static long extractHashLongVal(String[] array){
        long longVal = 0;
        if(array == null || array.length < 1){
            return 0;
        }
        try{
            longVal = Long.parseLong(array[0]);
        }catch(Exception e){}

        return longVal;
    }

    /**
     * Extracts a int from a String array
     *
     * @param array the String[] to extract
     * @return the converted array
     */
    public static int extractHashIntVal(String[] array){
        int intVal = 0;
        if(array == null || array.length < 1){
            return 0;
        }
        try{
            intVal = Integer.parseInt(array[0]);
        }catch(Exception e){}

        return intVal;
    }

}
