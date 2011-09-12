
package net.es.oscars.nsibridge.soap.gen.ifce;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.ogf.nsi.schema.connectionTypes.GenericFailedType;


/**
 * 
 *                         Provides transport envelope for the reservation failed
 *                         message.  Will map to a WSDL request (input) message type
 *                         in support of the NSI CS protocol.
 *                         
 *                         Elements:
 *                         correlationId - The identifier provided in the original
 *                         reservation request.
 *                         
 *                         reservationFailed - negative reservation result.
 *                     
 * 
 * <p>Java class for ReservationFailedRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReservationFailedRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="correlationId" type="{http://schemas.ogf.org/nsi/2011/07/connection/types}uuidType"/>
 *         &lt;element ref="{http://schemas.ogf.org/nsi/2011/07/connection/types}reservationFailed"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReservationFailedRequestType", propOrder = {
    "correlationId",
    "reservationFailed"
})
public class ReservationFailedRequestType {

    @XmlElement(required = true)
    protected String correlationId;
    @XmlElement(namespace = "http://schemas.ogf.org/nsi/2011/07/connection/types", required = true)
    protected GenericFailedType reservationFailed;

    /**
     * Gets the value of the correlationId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Sets the value of the correlationId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCorrelationId(String value) {
        this.correlationId = value;
    }

    /**
     * Gets the value of the reservationFailed property.
     * 
     * @return
     *     possible object is
     *     {@link GenericFailedType }
     *     
     */
    public GenericFailedType getReservationFailed() {
        return reservationFailed;
    }

    /**
     * Sets the value of the reservationFailed property.
     * 
     * @param value
     *     allowed object is
     *     {@link GenericFailedType }
     *     
     */
    public void setReservationFailed(GenericFailedType value) {
        this.reservationFailed = value;
    }

}
