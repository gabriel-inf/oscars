package net.es.oscars.rmi.bss.xface;

import java.io.Serializable;
import java.util.List;

/**
 * Bean containing information for a RMI request to return reservations matching
 * given set of parameters.
 */
public class RmiListResRequest implements Serializable {
    // for version 0.5
    private static final long serialVersionUID = 50;

    private int numRequested;      // number of reservations to return
    private int resOffset;         // offset into reservations list
    private String sortBy;         //sort results by this field: field asc|desc
    private Long startTime;        // reservation must end after this time
    private Long endTime;          // reservation must start before this time
    private String login;          // constrain to reservations from this user
    private String description;    // description must match part of this string
    private List<String> statuses;     // statuses to search for
    // reservation must contain hop matching one of these strings
    private List<String> linkIds;
    // reservation must contain hop with VLAN tag matching one of these strings
    private List<String> vlanTags;

    public RmiListResRequest() {
    }

    /**
     * @return numRequested number of reservations to return
     */
    public int getNumRequested() {
        return this.numRequested;
    }

    /**
     * @param numRequested number of reservations to return
     */
    public void setNumRequested(int numRequested) {
        this.numRequested = numRequested;
    }

    /**
     * @return resOffset offset into total reservations list
     */
    public int getResOffset() {
        return this.resOffset;
    }

    /**
     * @param resOffset offset into total reservations list
     */
    public void setResOffset(int resOffset) {
        this.resOffset = resOffset;
    }
    
    /**
     * @return the field by which the list is sorted
     */
    public String getSortBy() {
        return this.sortBy;
    }

    /**
     * @param the field to sort the list by
     */
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    /**
     * @return startTime Long with time in seconds after reservation must end
     */
    public Long getStartTime() {
        return this.startTime;
    }

    /**
     * @param startTime Long with time in seconds after reservation must end
     */
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    /**
     * @return endTime Long with time in seconds before which reservation starts
     */
    public Long getEndTime() {
        return this.endTime;
    }

    /**
     * @param endTime Long with time in seconds after which reservation must end
     */
    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    /**
     * @return login string with user name which if non-empty constrains to
     * reservations made by that user
     */
    public String getLogin() {
        return this.login;
    }

    /**
     * @param login string with user name which if non-empty constrains to
     * reservations made by that user
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * @return description string which must match part of reservation's
     * description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description string which must match part of reservation's
     * description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return statuses list of strings one of which must match reservation's
     * status
     */
    public List<String> getStatuses() {
        return this.statuses;
    }

    /**
     * @param statuses list of strings one of which must match reservation's
     * status
     */
    public void setStatuses(List<String> statuses) {
        this.statuses = statuses;
    }

    /**
     * @return linkIds list of strings one of which must at least partially
     * match the topology identifier of a hop in reservation's path
     */
    public List<String> getLinkIds() {
        return this.linkIds;
    }

    /**
     * @param linkIds list of strings one of which must at least partially
     * match the topology identifier of a hop in reservation's path
     */
    public void setLinkIds(List<String> linkIds) {
        this.linkIds = linkIds;
    }

    /**
     * @return vlanTags list of strings one of which must at least partially
     * match the VLAN tag associated with  a hop in reservation's path
     */
    public List<String> getVlanTags() {
        return this.vlanTags;
    }

    /**
     * @return vlanTags list of strings one of which must at least partially
     * match the VLAN tag associated with  a hop in reservation's path
     */
    public void setVlanTags(List<String> vlanTags) {
        this.vlanTags = vlanTags;
    }
}
