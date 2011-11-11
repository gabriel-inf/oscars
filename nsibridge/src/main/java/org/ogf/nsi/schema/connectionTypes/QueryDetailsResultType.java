
package org.ogf.nsi.schema.connectionTypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 Type containing the common reservation elements and detailed
 *                 path data for "Detailed" query results.
 *                 
 *                 Elements:
 *                 
 *                 ReservationInfoGroup - The common reservation information
 *                 elements.
 *                 
 *                 detailedPath - Minimally provides the source and destination
 *                 end points of the service, however, can hold detailed path
 *                 information for the reservation encompassing the entire
 *                 connection hierarchy.  The level of detail include is left up
 *                 to the individual NSA and their authorization policies.
 *             
 * 
 * <p>Java class for QueryDetailsResultType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QueryDetailsResultType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.ogf.org/nsi/2011/10/connection/types}ReservationInfoGroup"/>
 *         &lt;element name="detailedPath" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}DetailedPathType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryDetailsResultType", propOrder = {
    "globalReservationId",
    "description",
    "connectionId",
    "serviceParameters",
    "detailedPath"
})
public class QueryDetailsResultType {

    @XmlElement(required = true, nillable = true)
    protected String globalReservationId;
    protected String description;
    @XmlElement(required = true)
    protected String connectionId;
    @XmlElement(required = true)
    protected ServiceParametersType serviceParameters;
    @XmlElement(required = true)
    protected DetailedPathType detailedPath;

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
     * Gets the value of the detailedPath property.
     * 
     * @return
     *     possible object is
     *     {@link DetailedPathType }
     *     
     */
    public DetailedPathType getDetailedPath() {
        return detailedPath;
    }

    /**
     * Sets the value of the detailedPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link DetailedPathType }
     *     
     */
    public void setDetailedPath(DetailedPathType value) {
        this.detailedPath = value;
    }

}
