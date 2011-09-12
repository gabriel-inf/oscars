
package org.ogf.nsi.schema.connectionTypes;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                Parameters relating to desired service bandwidth.
 * 
 *                Elements:
 * 
 *                desired - the desired bandwidth in Mb/s being requested.
 * 
 *                minimum - the minimum bandwidth in Mb/s acceptable by the request
 *                as an alternative to the desired.
 * 
 *                maximum - the maximum bandwidth in Mb/s acceptable by the request
 *                as an alternative to the desired.
 *          
 * 
 * <p>Java class for BandwidthType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BandwidthType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="desired" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="minimum" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="maximum" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BandwidthType", propOrder = {
    "desired",
    "minimum",
    "maximum"
})
public class BandwidthType {

    @XmlElement(required = true)
    protected BigInteger desired;
    protected BigInteger minimum;
    protected BigInteger maximum;

    /**
     * Gets the value of the desired property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDesired() {
        return desired;
    }

    /**
     * Sets the value of the desired property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDesired(BigInteger value) {
        this.desired = value;
    }

    /**
     * Gets the value of the minimum property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMinimum() {
        return minimum;
    }

    /**
     * Sets the value of the minimum property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMinimum(BigInteger value) {
        this.minimum = value;
    }

    /**
     * Gets the value of the maximum property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMaximum() {
        return maximum;
    }

    /**
     * Sets the value of the maximum property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMaximum(BigInteger value) {
        this.maximum = value;
    }

}
