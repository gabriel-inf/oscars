package net.es.oscars.pss.common;

import java.util.List;

public class PSSActionDirections {
    private PSSAction action;
    private List<PSSDirection> directions;
    public void setDirections(List<PSSDirection> directions) {
        this.directions = directions;
    }
    public List<PSSDirection> getDirections() {
        return directions;
    }
    public void setAction(PSSAction action) {
        this.action = action;
    }
    public PSSAction getAction() {
        return action;
    }
}
