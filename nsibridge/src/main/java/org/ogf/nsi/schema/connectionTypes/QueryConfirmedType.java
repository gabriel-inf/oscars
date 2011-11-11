
package org.ogf.nsi.schema.connectionTypes;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 This is the type definition for the queryConfirmed message.  An
 *                 NSA sends this positive queryRequest response to the NSA that
 *                 issued the original request message.
 * 
 *                 Elements:
 * 
 *                 requesterNSA - NSA that requested the NSI connection service
 *                 operation (Requester Agent).
 * 
 *                 providerNSA - NSA that services the NSI connection service
 *                 operation (Provider Agent).
 * 
 *                 Choice of reservation element:
 *                 reservationSummary - Resulting summary set of connection
 *                 reservations matching the query criteria.  Will be provided
 *                 if the original query operation parameter was for "Summary".
 *                 
 *                 reservationDetails - Resulting detailed set of connection
 *                 reservations matching the query criteria.  Will be provided
 *                 if the original query operation parameter was for "Details".
 * 
 *                 If there were no matches to the query then no reservation
 *                 elements will be present.
 *             
 * 
 * <p>Java class for QueryConfirmedType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QueryConfirmedType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="requesterNSA" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}NsaIdType"/>
 *         &lt;element name="providerNSA" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}NsaIdType"/>
 *         &lt;choice>
 *           &lt;element name="reservationSummary" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}QuerySummaryResultType" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="reservationDetails" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}QueryDetailsResultType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryConfirmedType", propOrder = {
    "requesterNSA",
    "providerNSA",
    "reservationSummary",
    "reservationDetails"
})
public class QueryConfirmedType {

    @XmlElement(required = true)
    protected String requesterNSA;
    @XmlElement(required = true)
    protected String providerNSA;
    protected List<QuerySummaryResultType> reservationSummary;
    protected List<QueryDetailsResultType> reservationDetails;

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
     * Gets the value of the reservationSummary property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the reservationSummary property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReservationSummary().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QuerySummaryResultType }
     * 
     * 
     */
    public List<QuerySummaryResultType> getReservationSummary() {
        if (reservationSummary == null) {
            reservationSummary = new ArrayList<QuerySummaryResultType>();
        }
        return this.reservationSummary;
    }

    /**
     * Gets the value of the reservationDetails property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the reservationDetails property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReservationDetails().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QueryDetailsResultType }
     * 
     * 
     */
    public List<QueryDetailsResultType> getReservationDetails() {
        if (reservationDetails == null) {
            reservationDetails = new ArrayList<QueryDetailsResultType>();
        }
        return this.reservationDetails;
    }

}
