
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import net.es.oscars.nsibridge.soap.gen.saml.assertion.AttributeStatementType;


/**
 * 
 *             Technology specific attributes supporting both mandatory and
 *             optional values.
 * 
 *             Elements:
 * 
 *             mandatory - Attributes that MUST be met by the service.
 * 
 *             optional - Attributes that MAY be met by the service.
 *          
 * 
 * <p>Java class for TechnologySpecificAttributesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TechnologySpecificAttributesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mandatory" type="{urn:oasis:names:tc:SAML:2.0:assertion}AttributeStatementType" minOccurs="0"/>
 *         &lt;element name="optional" type="{urn:oasis:names:tc:SAML:2.0:assertion}AttributeStatementType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TechnologySpecificAttributesType", propOrder = {
    "mandatory",
    "optional"
})
public class TechnologySpecificAttributesType {

    protected AttributeStatementType mandatory;
    protected AttributeStatementType optional;

    /**
     * Gets the value of the mandatory property.
     * 
     * @return
     *     possible object is
     *     {@link AttributeStatementType }
     *     
     */
    public AttributeStatementType getMandatory() {
        return mandatory;
    }

    /**
     * Sets the value of the mandatory property.
     * 
     * @param value
     *     allowed object is
     *     {@link AttributeStatementType }
     *     
     */
    public void setMandatory(AttributeStatementType value) {
        this.mandatory = value;
    }

    /**
     * Gets the value of the optional property.
     * 
     * @return
     *     possible object is
     *     {@link AttributeStatementType }
     *     
     */
    public AttributeStatementType getOptional() {
        return optional;
    }

    /**
     * Sets the value of the optional property.
     * 
     * @param value
     *     allowed object is
     *     {@link AttributeStatementType }
     *     
     */
    public void setOptional(AttributeStatementType value) {
        this.optional = value;
    }

}
