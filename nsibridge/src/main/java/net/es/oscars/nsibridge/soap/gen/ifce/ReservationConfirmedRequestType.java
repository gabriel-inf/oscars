
package net.es.oscars.nsibridge.soap.gen.ifce;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.ogf.nsi.schema.connectionTypes.ReservationConfirmedType;


/**
 * 
 *                         Provides transport envelope for the reservation confirmed
 *                         message.  Will map to a WSDL request (input) message type
 *                         in support of the NSI CS protocol.
 *                         
 *                         Elements:
 *                         correlationId - The identifier provided in the original
 *                         reservation request.
 *                         
 *                         reservationConfirmed - positive reservation result.
 *                     
 * 
 * <p>Java class for ReservationConfirmedRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReservationConfirmedRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="correlationId" type="{http://schemas.ogf.org/nsi/2011/07/connection/types}uuidType"/>
 *         &lt;element ref="{http://schemas.ogf.org/nsi/2011/07/connection/types}reservationConfirmed"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReservationConfirmedRequestType", propOrder = {
    "correlationId",
    "reservationConfirmed"
})
public class ReservationConfirmedRequestType {

    @XmlElement(required = true)
    protected String correlationId;
    @XmlElement(namespace = "http://schemas.ogf.org/nsi/2011/07/connection/types", required = true)
    protected ReservationConfirmedType reservationConfirmed;

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
     * Gets the value of the reservationConfirmed property.
     * 
     * @return
     *     possible object is
     *     {@link ReservationConfirmedType }
     *     
     */
    public ReservationConfirmedType getReservationConfirmed() {
        return reservationConfirmed;
    }

    /**
     * Sets the value of the reservationConfirmed property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReservationConfirmedType }
     *     
     */
    public void setReservationConfirmed(ReservationConfirmedType value) {
        this.reservationConfirmed = value;
    }

}
