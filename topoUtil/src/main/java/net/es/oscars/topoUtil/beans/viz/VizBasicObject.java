package net.es.oscars.topoUtil.beans.viz;

import java.util.ArrayList;

public class VizBasicObject {
    protected String name;
    protected ArrayList<VizBasicObject> children = new ArrayList<VizBasicObject>();

    public ArrayList<VizBasicObject> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<VizBasicObject> children) {
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
