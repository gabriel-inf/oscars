
package org.ogf.nsi.schema.connectionTypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 Path of the service represented by a list of STP.
 *                 
 *                 Attributes:
 *                 
 *                 order - an optional attribute used if specification of ordered
 *                 path elements is required.
 *                 
 *                 Elements:
 *                 
 *                 directionality - (uni or bi) directionality of the service.
 *                 
 *                 sourceSTP - Source STP of the service.
 *                 
 *                 destSTP - Destination STP of the service.
 *                 
 *                 stpList - A possible hop-by-hop ordered list of STP from
 *                 sourceSTP to destSTP. List does not include sourceSTP and
 *                 destSTP.  List may only be partial.
 *                 
 *                 children - If provided this element will contain the list of
 *                 connections in the context of all direct children NSA involved
 *                 in the connection path.
 *             
 * 
 * <p>Java class for ComponentPathType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ComponentPathType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="directionality" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}DirectionalityType"/>
 *         &lt;element name="sourceSTP" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}ServiceTerminationPointType"/>
 *         &lt;element name="destSTP" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}ServiceTerminationPointType"/>
 *         &lt;element name="stpList" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}StpListType" minOccurs="0"/>
 *         &lt;element name="children" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}ChildListType" minOccurs="0"/>
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
@XmlType(name = "ComponentPathType", propOrder = {
    "directionality",
    "sourceSTP",
    "destSTP",
    "stpList",
    "children"
})
public class ComponentPathType {

    @XmlElement(required = true, defaultValue = "Bidirectional")
    protected DirectionalityType directionality;
    @XmlElement(required = true)
    protected ServiceTerminationPointType sourceSTP;
    @XmlElement(required = true)
    protected ServiceTerminationPointType destSTP;
    protected StpListType stpList;
    protected ChildListType children;
    @XmlAttribute(name = "order")
    protected Integer order;

    /**
     * Gets the value of the directionality property.
     * 
     * @return
     *     possible object is
     *     {@link DirectionalityType }
     *     
     */
    public DirectionalityType getDirectionality() {
        return directionality;
    }

    /**
     * Sets the value of the directionality property.
     * 
     * @param value
     *     allowed object is
     *     {@link DirectionalityType }
     *     
     */
    public void setDirectionality(DirectionalityType value) {
        this.directionality = value;
    }

    /**
     * Gets the value of the sourceSTP property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceTerminationPointType }
     *     
     */
    public ServiceTerminationPointType getSourceSTP() {
        return sourceSTP;
    }

    /**
     * Sets the value of the sourceSTP property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceTerminationPointType }
     *     
     */
    public void setSourceSTP(ServiceTerminationPointType value) {
        this.sourceSTP = value;
    }

    /**
     * Gets the value of the destSTP property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceTerminationPointType }
     *     
     */
    public ServiceTerminationPointType getDestSTP() {
        return destSTP;
    }

    /**
     * Sets the value of the destSTP property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceTerminationPointType }
     *     
     */
    public void setDestSTP(ServiceTerminationPointType value) {
        this.destSTP = value;
    }

    /**
     * Gets the value of the stpList property.
     * 
     * @return
     *     possible object is
     *     {@link StpListType }
     *     
     */
    public StpListType getStpList() {
        return stpList;
    }

    /**
     * Sets the value of the stpList property.
     * 
     * @param value
     *     allowed object is
     *     {@link StpListType }
     *     
     */
    public void setStpList(StpListType value) {
        this.stpList = value;
    }

    /**
     * Gets the value of the children property.
     * 
     * @return
     *     possible object is
     *     {@link ChildListType }
     *     
     */
    public ChildListType getChildren() {
        return children;
    }

    /**
     * Sets the value of the children property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChildListType }
     *     
     */
    public void setChildren(ChildListType value) {
        this.children = value;
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
