
package org.ogf.nsi.schema.connectionTypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *             Type modeling parameters relating to the requested service.
 *             
 *             schedule - time parameters specifying the life of the service.
 *             
 *             bandwidth - bandwidth of the service.
 *             
 *             serviceAttrs - Technology specific attributes relating to the
 *             service.
 *         
 * 
 * <p>Java class for ServiceParametersType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ServiceParametersType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="schedule" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}ScheduleType"/>
 *         &lt;element name="bandwidth" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}BandwidthType"/>
 *         &lt;element name="serviceAttributes" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}TechnologySpecificAttributesType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceParametersType", propOrder = {
    "schedule",
    "bandwidth",
    "serviceAttributes"
})
public class ServiceParametersType {

    @XmlElement(required = true)
    protected ScheduleType schedule;
    @XmlElement(required = true)
    protected BandwidthType bandwidth;
    protected TechnologySpecificAttributesType serviceAttributes;

    /**
     * Gets the value of the schedule property.
     * 
     * @return
     *     possible object is
     *     {@link ScheduleType }
     *     
     */
    public ScheduleType getSchedule() {
        return schedule;
    }

    /**
     * Sets the value of the schedule property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScheduleType }
     *     
     */
    public void setSchedule(ScheduleType value) {
        this.schedule = value;
    }

    /**
     * Gets the value of the bandwidth property.
     * 
     * @return
     *     possible object is
     *     {@link BandwidthType }
     *     
     */
    public BandwidthType getBandwidth() {
        return bandwidth;
    }

    /**
     * Sets the value of the bandwidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link BandwidthType }
     *     
     */
    public void setBandwidth(BandwidthType value) {
        this.bandwidth = value;
    }

    /**
     * Gets the value of the serviceAttributes property.
     * 
     * @return
     *     possible object is
     *     {@link TechnologySpecificAttributesType }
     *     
     */
    public TechnologySpecificAttributesType getServiceAttributes() {
        return serviceAttributes;
    }

    /**
     * Sets the value of the serviceAttributes property.
     * 
     * @param value
     *     allowed object is
     *     {@link TechnologySpecificAttributesType }
     *     
     */
    public void setServiceAttributes(TechnologySpecificAttributesType value) {
        this.serviceAttributes = value;
    }

}
