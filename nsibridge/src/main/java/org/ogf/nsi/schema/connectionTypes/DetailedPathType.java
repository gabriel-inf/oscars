
package org.ogf.nsi.schema.connectionTypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 This type is used to model a connection reservation's detailed
 *                 path information.  The structure is recursive so it is
 *                 possible to model both an ordered list of connection segments,
 *                 as well as the hierarchical connection segments created on
 *                 children NSA in either a tree and chain configuration.
 *                 
 *                 Attributes:
 *                 
 *                 order - an optional attribute used if specification of ordered
 *                 path elements is required.
 *                 
 *                 Elements:
 *                 
 *                 providerNSA - The provider NSA holding the connection
 *                 information associated with this instance of data.
 *                 
 *                 connectionId - The connection identifier associated with the
 *                 reservation and path segment.
 *                 
 *                 pathList - The detailed path information associated with the
 *                 connection reservation.  This element can model multiple local
 *                 path segments if required to realize the reservation, as well
 *                 as the full connection hierarchy associated with each path
 *                 segment.
 *             
 * 
 * <p>Java class for DetailedPathType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DetailedPathType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="providerNSA" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}NsaIdType"/>
 *         &lt;element name="connectionId" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}ConnectionIdType"/>
 *         &lt;element name="connectionState" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}ConnectionStateType"/>
 *         &lt;element name="pathList" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}PathListType" minOccurs="0"/>
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
@XmlType(name = "DetailedPathType", propOrder = {
    "providerNSA",
    "connectionId",
    "connectionState",
    "pathList"
})
public class DetailedPathType {

    @XmlElement(required = true)
    protected String providerNSA;
    @XmlElement(required = true)
    protected String connectionId;
    @XmlElement(required = true)
    protected ConnectionStateType connectionState;
    protected PathListType pathList;
    @XmlAttribute(name = "order")
    protected Integer order;

    /**
     * Gets the value of the providerNSA property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProviderNSA() {
        return providerNSA;
    }

    /**
     * Sets the value of the providerNSA property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProviderNSA(String value) {
        this.providerNSA = value;
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
     * Gets the value of the pathList property.
     * 
     * @return
     *     possible object is
     *     {@link PathListType }
     *     
     */
    public PathListType getPathList() {
        return pathList;
    }

    /**
     * Sets the value of the pathList property.
     * 
     * @param value
     *     allowed object is
     *     {@link PathListType }
     *     
     */
    public void setPathList(PathListType value) {
        this.pathList = value;
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
