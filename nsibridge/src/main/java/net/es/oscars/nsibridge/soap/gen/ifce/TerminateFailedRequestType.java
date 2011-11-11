
package net.es.oscars.nsibridge.soap.gen.ifce;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.ogf.nsi.schema.connectionTypes.GenericFailedType;


/**
 * 
 *                         Provides transport envelope for the terminate failed
 *                         message.  Will map to a WSDL request (input) message type
 *                         in support of the NSI CS protocol.
 *                         
 *                         Elements:
 *                         correlationId - The identifier provided in the original
 *                         terminate request.
 *                         
 *                         terminateFailed - negative terminate result.
 *                     
 * 
 * <p>Java class for TerminateFailedRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TerminateFailedRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="correlationId" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}uuidType"/>
 *         &lt;element ref="{http://schemas.ogf.org/nsi/2011/10/connection/types}terminateFailed"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TerminateFailedRequestType", propOrder = {
    "correlationId",
    "terminateFailed"
})
public class TerminateFailedRequestType {

    @XmlElement(required = true)
    protected String correlationId;
    @XmlElement(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/types", required = true)
    protected GenericFailedType terminateFailed;

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
     * Gets the value of the terminateFailed property.
     * 
     * @return
     *     possible object is
     *     {@link GenericFailedType }
     *     
     */
    public GenericFailedType getTerminateFailed() {
        return terminateFailed;
    }

    /**
     * Sets the value of the terminateFailed property.
     * 
     * @param value
     *     allowed object is
     *     {@link GenericFailedType }
     *     
     */
    public void setTerminateFailed(GenericFailedType value) {
        this.terminateFailed = value;
    }

}
