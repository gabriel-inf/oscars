package net.es.oscars.bss.topology;

public class PathElemParamSwcap {
    public static final String L2SC = "l2sc";
    
    static public boolean isValid(String swcap){
        if(L2SC.equals(swcap)){
            return true;
        }
        return false;
    }
}
