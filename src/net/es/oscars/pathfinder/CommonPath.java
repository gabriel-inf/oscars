package net.es.oscars.pathfinder;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * CommonPath stores a path that is not Axis2 or Hibernate dependent.
 * It is used during all steps of reservation creation up to persistence
 * to the database, at which point it is converted to a Path.
 */
public class CommonPath {

    private List<CommonPathElem> elems;
    private Integer vlanId;
    private String url;
    private Boolean explicit;

    /** default constructor */
    public CommonPath() { }

    /**
     * @return the list of path elements
     */ 
    public List<CommonPathElem> getElems() { return this.elems; }

    /**
     * @param elems the list of elements in the path
     */ 
    public void setElems(List<CommonPathElem> elems) { this.elems = elems; }


    /**
     * @return vlan An Integer with the reservation's associated vlan
     */ 
    public Integer getVlanId() { return this.vlanId; }

    /**
     * @param vlan An Integer with the reservation's desired vlan
     */ 
    public void setVlanId(Integer vlan) {
        this.vlanId = vlan;
    }


    /**
     * @return url of the next domain controller in the path
     */ 
    public String getUrl() { return this.url; }

    /**
     * @param url the url of the next domain controller in the path
     */ 
    public void setUrl(String url) { this.url = url; }


    /**
     * @return boolean indicating whether this is an explicit path
     */ 
    public boolean isExplicit() { return this.explicit; }

    /**
     * @param explicit boolean indicating whether this is an explicit path
     */ 
    public void setExplicit(boolean explicit) { this.explicit = explicit; }


    public String toString() {
        return new ToStringBuilder(this)
            .append("url", getUrl())
            .toString();
    }
}
