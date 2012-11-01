
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 Type definition for the reserveConfirmed message. A Provider NSA
 *                 sends this positive reserveRequest response to the Requester NSA
 *                 that issued the original request message.
 * 
 *                 Elements:
 *                 
 *                 reservation - Parameters chosen for the connection reservation.
 *             
 * 
 * <p>Java class for ReserveConfirmedType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReserveConfirmedType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="reservation" type="{http://schemas.ogf.org/nsi/2012/03/connection/types}ReservationInfoType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReserveConfirmedType", propOrder = {
    "reservation"
})
public class ReserveConfirmedType {

    @XmlElement(required = true)
    protected ReservationInfoType reservation;

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
