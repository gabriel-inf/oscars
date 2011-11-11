
package org.ogf.nsi.schema.connectionTypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;


/**
 * 
 *                 Type definition for the reserve message that allows a
 *                 Requester NSA to reserve network resources for a connection
 *                 between two STP's constrained by a certain service parameters.
 * 
 *                 Elements:
 * 
 *                 requesterNSA - NSA that requested the NSI connection service
 *                 operation (Requester Agent).
 * 
 *                 providerNSA - NSA that services the NSI connection service
 *                 operation (Provider Agent).
 * 
 *                 sessionSecurityAttr - Security attributes associated with the
 *                 NSI connection services session between a Requester/Provider
 *                 NSA pair.
 * 
 *                 reservation - Parameters specifying the criteria for the
 *                 connection reservation.
 *             
 * 
 * <p>Java class for ReserveType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReserveType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="requesterNSA" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}NsaIdType"/>
 *         &lt;element name="providerNSA" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}NsaIdType"/>
 *         &lt;element name="sessionSecurityAttr" type="{urn:oasis:names:tc:SAML:2.0:assertion}AttributeStatementType" minOccurs="0"/>
 *         &lt;element name="reservation" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}ReservationInfoType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReserveType", propOrder = {
    "requesterNSA",
    "providerNSA",
    "sessionSecurityAttr",
    "reservation"
})
public class ReserveType {

    @XmlElement(required = true)
    protected String requesterNSA;
    @XmlElement(required = true)
    protected String providerNSA;
    protected AttributeStatementType sessionSecurityAttr;
    @XmlElement(required = true)
    protected ReservationInfoType reservation;

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
     * Gets the value of the reservation property.
     * 
     * @return
     *     possible object is
     *     {@link ReservationInfoType }
     *     
     */
    public ReservationInfoType getReservation() {
        return reservation;
    }

    /**
     * Sets the value of the reservation property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReservationInfoType }
     *     
     */
    public void setReservation(ReservationInfoType value) {
        this.reservation = value;
    }

}
