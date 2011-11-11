
package org.ogf.nsi.schema.connectionTypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *             Type containing the set of reservation parameters associated with
 *             a "Summary" query result.
 * 
 *             Elements:
 * 
 *             ReservationInfoGroup - Element group containing the common
 *             reservation elements and detailed path data.
 * 
 *             connectionState - The reservation's overall connection state.
 *             Individual connection states for each component in the connection
 *             hierarchy are not provided in this result set.
 *             
 *             path - The source and destination end points of the service.
 *             Can optionally hold additional path segments for the path
 *             but this is up to the NSA to populate.
 *          
 * 
 * <p>Java class for QuerySummaryResultType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QuerySummaryResultType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.ogf.org/nsi/2011/10/connection/types}ReservationInfoGroup"/>
 *         &lt;element name="connectionState" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}ConnectionStateType"/>
 *         &lt;element name="path" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}PathType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuerySummaryResultType", propOrder = {
    "globalReservationId",
    "description",
    "connectionId",
    "serviceParameters",
    "connectionState",
    "path"
})
public class QuerySummaryResultType {

    @XmlElement(required = true, nillable = true)
    protected String globalReservationId;
    protected String description;
    @XmlElement(required = true)
    protected String connectionId;
    @XmlElement(required = true)
    protected ServiceParametersType serviceParameters;
    @XmlElement(required = true)
    protected ConnectionStateType connectionState;
    @XmlElement(required = true)
    protected PathType path;

    /**
     * Gets the value of the globalReservationId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGlobalReservationId() {
        return globalReservationId;
    }

    /**
     * Sets the value of the globalReservationId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGlobalReservationId(String value) {
        this.globalReservationId = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the connectionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConnectionId() {
        return connectionId;
    }

    /**
     * Sets the value of the connectionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConnectionId(String value) {
        this.connectionId = value;
    }

    /**
     * Gets the value of the serviceParameters property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceParametersType }
     *     
     */
    public ServiceParametersType getServiceParameters() {
        return serviceParameters;
    }

    /**
     * Sets the value of the serviceParameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceParametersType }
     *     
     */
    public void setServiceParameters(ServiceParametersType value) {
        this.serviceParameters = value;
    }

    /**
     * Gets the value of the connectionState property.
     * 
     * @return
     *     possible object is
     *     {@link ConnectionStateType }
     *     
     */
    public ConnectionStateType getConnectionState() {
        return connectionState;
    }

    /**
     * Sets the value of the connectionState property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConnectionStateType }
     *     
     */
    public void setConnectionState(ConnectionStateType value) {
        this.connectionState = value;
    }

    /**
     * Gets the value of the path property.
     * 
     * @return
     *     possible object is
     *     {@link PathType }
     *     
     */
    public PathType getPath() {
        return path;
    }

    /**
     * Sets the value of the path property.
     * 
     * @param value
     *     allowed object is
     *     {@link PathType }
     *     
     */
    public void setPath(PathType value) {
        this.path = value;
    }

}
