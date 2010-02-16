package net.es.oscars.bss;

import java.util.ArrayList;

public class VlanRange {
    public static final Integer MAX_VLAN = 4096;
    
    private boolean[] map;

    public boolean[] getMap() {
        return map;
    }

    public void setMap(boolean[] map) {
        this.map = map;
    }
    /*
    public static void main(String[] args) {
        try {
            VlanRange one = new VlanRange("300-600");
            VlanRange other = new VlanRange("500");
            System.out.println("ONE: "+one+ " OTHER: "+other);
            VlanRange tmp = VlanRange.and(one, other);
            System.out.println("AND: "+tmp);
            tmp = VlanRange.subtract(one, other);
            System.out.println("SUBTRACT: "+tmp);
        } catch (BSSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    */
    
    private void init() {
        map = new boolean[MAX_VLAN];
        for (int i = 0; i < MAX_VLAN; i++) {
            map[i] = false;
        }
    }
    
    public VlanRange() {
        init();
    }
    public VlanRange(String range) throws BSSException {
        init();
        
        if (range == null) {
            return;
        }
        range = range.trim();
        if (range.equals("")) {
            return;
        }
        
        String[] rangeList = range.split(",");
        try {
            for(int i = 0; i < rangeList.length; i++){
                if (rangeList[i].trim().equals("")) {
                    continue;
                }
                String[] rangeEnds = rangeList[i].trim().split("-");
                if (rangeEnds.length == 1){
                    int tag = Integer.parseInt(rangeEnds[0].trim());
                    map[tag] = true;
                } else if(rangeEnds.length == 2 && "".equals(rangeEnds[0])){
                    int tag = Integer.parseInt(rangeEnds[1].trim());
                    map[tag] = true;
                } else if(rangeEnds.length == 2){
                    int start = Integer.parseInt(rangeEnds[0].trim());
                    int end = Integer.parseInt(rangeEnds[1].trim());
                    if (end < start) {
                        throw new BSSException("Invalid range: end < start: "+range);
                    }
                    for (int k = start; k <= end; k++) {
                        map[k] = true;
                    }
                }
            }
        } catch (NumberFormatException ex) {
            throw new BSSException("Invalid VLAN range format   \n"+ ex.getMessage());
        }        

        
    }
    
    public boolean isEmpty() {
        for (int i = 0; i < MAX_VLAN; i++) {
            if (map[i]) return false;
        }
        return true;
    }
    
    public int getFirst() {
        for (int i = 0; i < MAX_VLAN; i++) {
            if (map[i]) return i;
        }
        return -1;
    }
    public String toString() {
        String range = "";
        int start = 0;
        
        for (int i = 0; i < MAX_VLAN; i++) {
            if (map[i]) {
                start = i;
                break;
            }
        }
        
        if (start == (MAX_VLAN - 1)) {
            return range;
        }
        
        ArrayList<int[]> intervals = new ArrayList<int[]>();
        int[] interval = new int[2];
        interval[0] = start;
        interval[1] = MAX_VLAN - 1;
        boolean prev = true;
        for (int i = start+1; i < MAX_VLAN; i++) {
            if (prev != map[i]) {
                if (prev) {
                    interval[1] = i - 1;
                    intervals.add(interval);
                } else {
                    interval = new int[2];
                    interval[0] = i;
                }
            }
            prev = map[i];
        }
        
        for (int i = 0; i < intervals.size(); i++) {
            int[] tmp = intervals.get(i);
            if (tmp[0] == tmp[1]) {
                range += tmp[0];
            } else {
                range += tmp[0]+"-"+tmp[1];
            }
            if (i < intervals.size() -1) {
                range += ",";
            }
        }
        
        return range;
    }
    
    /**
     * @param a a VlanRange 
     * @param b another VlanRange 
     * @return a new VlanRange containing only the VLANs set in both a and b
     */
    public static VlanRange and(VlanRange a, VlanRange b) {
        VlanRange result = new VlanRange();
        for (int i = 0; i < MAX_VLAN; i++) {
            if (a.getMap()[i] && b.getMap()[i]) {
                result.getMap()[i] = true;
            }
        }
        return result;
    }
    
    /**
     * @param a a VlanRange 
     * @param b another VlanRange 
     * @return a new VlanRange containing the VLANs set in a but not in b
     */
    public static VlanRange subtract(VlanRange a, VlanRange b) {
        VlanRange result = new VlanRange();
        for (int i = 0; i < MAX_VLAN; i++) {
            if (a.getMap()[i] && !b.getMap()[i]) {
                result.getMap()[i] = true;
            }
        }
        return result;
    }
    
    public static VlanRange copy(VlanRange a) {
        VlanRange result = new VlanRange();
        for (int i = 0; i < MAX_VLAN; i++) {
            result.getMap()[i] = a.getMap()[i];
        }
        return result;
    }
    

}
