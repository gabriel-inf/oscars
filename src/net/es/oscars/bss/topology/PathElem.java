package net.es.oscars.bss.topology;

import java.util.*;
import java.io.Serializable;

import org.hibernate.Hibernate;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.bss.BSSException;
import net.es.oscars.database.HibernateBean;

/**
 * PathElem is the Hibernate bean for the bss.pathElems table.
 */
public class PathElem extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private int seqNumber;

    /** nullable persistent field */
    private String urn;

    /** nullable persistent field */
    private String userName;


    /** persistent field */
    private Link link;

    private Set<PathElemParam> pathElemParams = new HashSet<PathElemParam>();

    private HashMap<String, PathElemParam> pathElemParamMap = new HashMap<String, PathElemParam>();

    /** default constructor */
    public PathElem() { }

    public void initializePathElemParams() {
        if (!this.pathElemParamMap.isEmpty()) {
            return;
        }
        Iterator<PathElemParam> pathElemParamsIterator = this.pathElemParams.iterator();
        while (pathElemParamsIterator.hasNext()) {
            PathElemParam param = (PathElemParam) pathElemParamsIterator.next();
            String key = param.getSwcap() + param.getType();
            this.pathElemParamMap.put(key, param);
        }
    }
    
    /**
     * @return seqNumber int with this path element's position in list
     */
    public int getSeqNumber() {
        return this.seqNumber;
    }

    /**
     * @param num not actually settable
     */
    public void setSeqNumber(int num) {
        this.seqNumber = num;
    }


    /**
     * @return urn a string with this path element's associated urn
     */
    public String getUrn() { return this.urn; }

    /**
     * @param urn string with path element's associated urn
     */
    public void setUrn(String urn) {
        this.urn = urn;
    }


    /**
     * @return userName a string with element's associated user name
     */
    public String getUserName() { return this.userName; }

    /**
     * @param userName string with path element's associated user name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }


    /**
     * @return link link instance associated with this path element
     */
    public Link getLink() { return this.link; }

    /**
     * @param link link instance associated with this path element
     */
    public void setLink(Link link) { this.link = link; }


    /**
     * @return set of path elem parameters
     */
    public Set<PathElemParam> getPathElemParams() { return this.pathElemParams; }

    /**
     * @param swcap Generate a HashMap only with parameters of this swcap
     * @return HashMap keyed by type for each parameter with given swcap type
     * @throws BSSException
     */
    public PathElemParam getPathElemParam(String swcap, String type) throws BSSException {
        this.initializePathElemParams();
        if(!PathElemParamSwcap.isValid(swcap)){
            throw new BSSException("Invalid PathElemParam swcap '" + swcap + "'");
        }else if(!PathElemParamType.isValid(type)){
            throw new BSSException("Invalid PathElemParam type '" + type + "'");
        }

        if(!pathElemParamMap.containsKey(swcap+type)){
            return null;
        }

        return pathElemParamMap.get(swcap+type);
    }

    /**
     * @param pathElemParams set of path elem parameters
     */
    public void setPathElemParams(Set <PathElemParam>pathElemParams) {
        this.pathElemParams = pathElemParams;
    }

    public boolean addPathElemParam(PathElemParam pathElemParam) {
        if (this.pathElemParams.add(pathElemParam)) {
            pathElemParamMap.put(pathElemParam.getSwcap()+pathElemParam.getType(), pathElemParam);
            return true;
        } else {
            return false;
        }

    }

    public void removePathElemParam(PathElemParam pathElemParam) {
        this.pathElemParams.remove(pathElemParam);
        this.pathElemParamMap.remove(pathElemParam);
    }

    // need to override superclass because dealing with transient
    // instances as well
    public boolean equals(Object o) {
        if (this == o) { return true; }
        Class thisClass = Hibernate.getClass(this);
        if (o == null || thisClass != Hibernate.getClass(o)) {
            return false;
        }
        PathElem castOther = (PathElem) o;
        // if both of these have been saved to the database
        if ((this.getId() != null) &&
            (castOther.getId() != null)) {
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        } else {
            return new EqualsBuilder()
                .append(this.getLink(), castOther.getLink())
                .isEquals();
        }
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    /**
     * Copies a pathElem; will not copy id and seqNumber.
     *
     * @param pe the pathElem to copy
     * @return the copy
     * @throws BSSException 
     */
    public static PathElem copyPathElem(PathElem pathElem) throws BSSException {
        PathElem copy = new PathElem();
        copy.setLink(pathElem.getLink());
        copy.setUrn(pathElem.getUrn());
        copy.setUserName(pathElem.getUserName());
        PathElem.copyPathElemParams(copy, pathElem, null);
        return copy;
    }

    /** Creates a copies of the PathElemParams of this object that match the swcap given
     *
     *@param dest the location to get the copied params
     * @param src the PathElem with the params to copy
     * @param swcap the type of PathElem params to copy. null if all params should be copied.
     * @throws BSSException 
     */
    public static void copyPathElemParams(PathElem dest, PathElem src, String swcap) throws BSSException{
        Iterator<PathElemParam> paramIterator = src.getPathElemParams().iterator();
        while(paramIterator.hasNext()){
            PathElemParam param = (PathElemParam) paramIterator.next();
            //swcap == null means copy all
            if(swcap != null && !swcap.equals(param.getSwcap())){
                continue;
            }
            PathElemParam paramCopy = dest.getPathElemParam(param.getSwcap(), param.getType());
            if(paramCopy == null){
                paramCopy = new PathElemParam();
                paramCopy.setSwcap(param.getSwcap());
                paramCopy.setType(param.getType());
                dest.addPathElemParam(paramCopy);
            }
            paramCopy.setValue(param.getValue());
        }
    }

}
