
package net.es.oscars.nsibridge.soap.gen.ifce;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.ogf.nsi.schema.connectionTypes.GenericRequestType;


/**
 * 
 *                         Provides transport envelope for the forcedEnd request
 *                         message.  Will map to a WSDL request (input) message
 *                         type in support of the NSI CS protocol.
 *                         
 *                         Elements:
 *                         correlationId - An identifier provided by the requesting
 *                         NSA used to correlate to the corresponding
 *                         acknowledgment.  There will be no asynchronous
 *                         response for this request. It is recommended that a
 *                         Universally Unique Identifier (UUID) URN as per IETF
 *                         RFC 4122 be used as a globally unique value.
 *                         
 *                         forcedEnd - forcedEnd message specific parameters.
 *                     
 * 
 * <p>Java class for ForcedEndRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ForcedEndRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="correlationId" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}uuidType"/>
 *         &lt;element ref="{http://schemas.ogf.org/nsi/2011/10/connection/types}forcedEnd"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ForcedEndRequestType", propOrder = {
    "correlationId",
    "forcedEnd"
})
public class ForcedEndRequestType {

    @XmlElement(required = true)
    protected String correlationId;
    @XmlElement(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/types", required = true)
    protected GenericRequestType forcedEnd;

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
     * Gets the value of the forcedEnd property.
     * 
     * @return
     *     possible object is
     *     {@link GenericRequestType }
     *     
     */
    public GenericRequestType getForcedEnd() {
        return forcedEnd;
    }

    /**
     * Sets the value of the forcedEnd property.
     * 
     * @param value
     *     allowed object is
     *     {@link GenericRequestType }
     *     
     */
    public void setForcedEnd(GenericRequestType value) {
        this.forcedEnd = value;
    }

}
