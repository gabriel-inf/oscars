
package org.ogf.nsi.schema.connectionTypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *             The Service Termination Point (STP) type used for path selection.
 *              
 *             Elements:
 *             
 *             stpId - the unique identifier for the STP.
 *             
 *             techSpecAttrs - technology specific attributes associated with
 *             the Service Termination Point.
 *             
 * 
 * <p>Java class for ServiceTerminationPointType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ServiceTerminationPointType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="stpId" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}StpIdType"/>
 *         &lt;element name="stpSpecAttrs" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}TechnologySpecificAttributesType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceTerminationPointType", propOrder = {
    "stpId",
    "stpSpecAttrs"
})
public class ServiceTerminationPointType {

    @XmlElement(required = true)
    protected String stpId;
    protected TechnologySpecificAttributesType stpSpecAttrs;

    /**
     * Gets the value of the stpId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStpId() {
        return stpId;
    }

    /**
     * Sets the value of the stpId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStpId(String value) {
        this.stpId = value;
    }

    /**
     * Gets the value of the stpSpecAttrs property.
     * 
     * @return
     *     possible object is
     *     {@link TechnologySpecificAttributesType }
     *     
     */
    public TechnologySpecificAttributesType getStpSpecAttrs() {
        return stpSpecAttrs;
    }

    /**
     * Sets the value of the stpSpecAttrs property.
     * 
     * @param value
     *     allowed object is
     *     {@link TechnologySpecificAttributesType }
     *     
     */
    public void setStpSpecAttrs(TechnologySpecificAttributesType value) {
        this.stpSpecAttrs = value;
    }

}
