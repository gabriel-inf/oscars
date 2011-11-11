
package org.ogf.nsi.schema.connectionTypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 A Service Termination Point (STP) which can be ordered in a list for
 *                 use in PathObject definition.
 * 
 *                 Attributes:
 * 
 *                 order - Order attribute is provided only when the STP is part of an
 *                 orderedStpList.
 * 
 *                 Elements:
 * 
 *                 stpId - the unique identifier for the STP.
 *          
 * 
 * <p>Java class for OrderedServiceTerminationPointType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OrderedServiceTerminationPointType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="stpId" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}StpIdType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="order" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OrderedServiceTerminationPointType", propOrder = {
    "stpId"
})
public class OrderedServiceTerminationPointType {

    @XmlElement(required = true)
    protected String stpId;
    @XmlAttribute(name = "order")
    protected Integer order;

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
     * Gets the value of the order property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getOrder() {
        return order;
    }

    /**
     * Sets the value of the order property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setOrder(Integer value) {
        this.order = value;
    }

}
