
package org.ogf.nsi.schema.connectionTypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 A generic "Failed" message type sent as request in response to a
 *                 failure to process a previous protocol "Request" message.  This is
 *                 used in response to all request types that can return an error.
 * 
 *                 Elements:
 * 
 *                 requesterNSA - NSA that requested the NSI connection service
 *                 operation (Requester Agent).
 * 
 *                 providerNSA - NSA that services the NSI connection service
 *                 operation (Provider Agent).
 * 
 *                 globalReservationId - An optional global reservation id that was
 *                 originally provided in the reserve request.
 * 
 *                 connectionId - The locally unique identifier for a connection that
 *                 is known between a Requesting and Provider NSA pair.
 * 
 *                 connectionState - Overall connection state for the reservation.
 * 
 *                 ServiceException - Specific error condition - the reason for the
 *                 failure.
 *             
 * 
 * <p>Java class for GenericFailedType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GenericFailedType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="requesterNSA" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}NsaIdType"/>
 *         &lt;element name="providerNSA" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}NsaIdType"/>
 *         &lt;element name="globalReservationId" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}GlobalReservationIdType" minOccurs="0"/>
 *         &lt;element name="connectionId" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}ConnectionIdType"/>
 *         &lt;element name="connectionState" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}ConnectionStateType"/>
 *         &lt;element name="serviceException" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}ServiceExceptionType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GenericFailedType", propOrder = {
    "requesterNSA",
    "providerNSA",
    "globalReservationId",
    "connectionId",
    "connectionState",
    "serviceException"
})
public class GenericFailedType {

    @XmlElement(required = true)
    protected String requesterNSA;
    @XmlElement(required = true)
    protected String providerNSA;
    protected String globalReservationId;
    @XmlElement(required = true)
    protected String connectionId;
    @XmlElement(required = true)
    protected ConnectionStateType connectionState;
    @XmlElement(required = true)
    protected ServiceExceptionType serviceException;

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
     * Gets the value of the serviceException property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceExceptionType }
     *     
     */
    public ServiceExceptionType getServiceException() {
        return serviceException;
    }

    /**
     * Sets the value of the serviceException property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceExceptionType }
     *     
     */
    public void setServiceException(ServiceExceptionType value) {
        this.serviceException = value;
    }

}
