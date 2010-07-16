package net.es.oscars.pss.common;


public class PSSGriDirection {

    private String gri;
    private PSSDirection direction;
    public void setGri(String gri) {
        this.gri = gri;
    }
    public String getGri() {
        return gri;
    }
    public void setDirection(PSSDirection dir) {
        this.direction = dir;
    }
    public PSSDirection getDirection() {
        return direction;
    }
    public boolean equals(PSSGriDirection other) {
        if (other == null) return false;
        
        return (this.equalDir(other.getDirection()) && this.equalGri(other.getGri()));
    }
    
    private boolean equalDir(PSSDirection otherDir) {
        if (direction == null) {
            if (otherDir == null) return true;
            return false;
        } else {
            return direction.equals(otherDir);
        }
        
    }
    private boolean equalGri(String otherGri) {
        if (gri == null) {
            if (otherGri == null) return true;
            return false;
        } else {
            return gri.equals(otherGri);
        }
        
    }
}
