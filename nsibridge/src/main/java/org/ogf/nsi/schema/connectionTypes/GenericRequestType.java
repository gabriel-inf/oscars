
package org.ogf.nsi.schema.connectionTypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;


/**
 * 
 *                 This is a generic type definition for request messages such as
 *                 provision, release, terminate, and forcedEnd that only need a
 *                 connectionId as a request parameter.
 * 
 *                 Elements:
 * 
 *                 requesterNSA - NSA that requested the NSI connection service
 *                 operation (Requester Agent).
 * 
 *                 providerNSA - NSA that services the NSI connection service
 *                 operation (Provider Agent).
 * 
 *                 sessionSecurityAttr - Security attributes associated with the NSI
 *                 connection services session between a Requester/Provider NSA pair.
 * 
 *                 connectionId - The unique identifier for a connection known between
 *                 a Requesting and Provider NSA pair.
 *          
 * 
 * <p>Java class for GenericRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GenericRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="requesterNSA" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}NsaIdType"/>
 *         &lt;element name="providerNSA" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}NsaIdType"/>
 *         &lt;element name="sessionSecurityAttr" type="{urn:oasis:names:tc:SAML:2.0:assertion}AttributeStatementType" minOccurs="0"/>
 *         &lt;element name="connectionId" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}ConnectionIdType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GenericRequestType", propOrder = {
    "requesterNSA",
    "providerNSA",
    "sessionSecurityAttr",
    "connectionId"
})
public class GenericRequestType {

    @XmlElement(required = true)
    protected String requesterNSA;
    @XmlElement(required = true)
    protected String providerNSA;
    protected AttributeStatementType sessionSecurityAttr;
    @XmlElement(required = true)
    protected String connectionId;

    /**
     * Gets the value of the requesterNSA property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequesterNSA() {
        return requesterNSA;
    }

    /**
     * Sets the value of the requesterNSA property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequesterNSA(String value) {
        this.requesterNSA = value;
    }

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
     * Gets the value of the sessionSecurityAttr property.
     * 
     * @return
     *     possible object is
     *     {@link AttributeStatementType }
     *     
     */
    public AttributeStatementType getSessionSecurityAttr() {
        return sessionSecurityAttr;
    }

    /**
     * Sets the value of the sessionSecurityAttr property.
     * 
     * @param value
     *     allowed object is
     *     {@link AttributeStatementType }
     *     
     */
    public void setSessionSecurityAttr(AttributeStatementType value) {
        this.sessionSecurityAttr = value;
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

}
