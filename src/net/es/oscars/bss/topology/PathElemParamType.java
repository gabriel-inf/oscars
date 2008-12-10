package net.es.oscars.bss.topology;

public class PathElemParamType {
    
    
    public static final String L2SC_VLAN_RANGE = "vlanRangeAvailability";
    public static final String L2SC_SUGGESTED_VLAN = "suggestedVlan";
    
    static public boolean isValid(String type){
        if(L2SC_VLAN_RANGE.equals(type)){
            return true;
        }else if(L2SC_SUGGESTED_VLAN.equals(type)){
            return true;
        }
        return false;
    }
}
