
package net.es.oscars.nsibridge.soap.gen.ifce;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.ogf.nsi.schema.connectionTypes.GenericConfirmedType;


/**
 * 
 *                         Provides transport envelope for the provision confirmed
 *                         message.  Will map to a WSDL request (input) message type
 *                         in support of the NSI CS protocol.
 *                         
 *                         Elements:
 *                         correlationId - The identifier provided in the original
 *                         provision request.
 *                         
 *                         provisionConfirmed - positive provision result.
 *                     
 * 
 * <p>Java class for ProvisionConfirmedRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProvisionConfirmedRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="correlationId" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}uuidType"/>
 *         &lt;element ref="{http://schemas.ogf.org/nsi/2011/10/connection/types}provisionConfirmed"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProvisionConfirmedRequestType", propOrder = {
    "correlationId",
    "provisionConfirmed"
})
public class ProvisionConfirmedRequestType {

    @XmlElement(required = true)
    protected String correlationId;
    @XmlElement(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/types", required = true)
    protected GenericConfirmedType provisionConfirmed;

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
     * Gets the value of the provisionConfirmed property.
     * 
     * @return
     *     possible object is
     *     {@link GenericConfirmedType }
     *     
     */
    public GenericConfirmedType getProvisionConfirmed() {
        return provisionConfirmed;
    }

    /**
     * Sets the value of the provisionConfirmed property.
     * 
     * @param value
     *     allowed object is
     *     {@link GenericConfirmedType }
     *     
     */
    public void setProvisionConfirmed(GenericConfirmedType value) {
        this.provisionConfirmed = value;
    }

}
