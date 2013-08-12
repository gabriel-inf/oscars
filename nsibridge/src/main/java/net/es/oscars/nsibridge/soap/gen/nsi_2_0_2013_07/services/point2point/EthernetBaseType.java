
package net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.point2point;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 Point-to-Point Ethernet service definition.
 *                 
 *                 Elements:
 *                 
 *                 capacity - Capacity of the service in Mb/s including ethernet
 *                 framing headers.
 *                 
 *                 directionality - The (uni or bi) directionality of the service.
 *                 
 *                 symmetricPath - An indication that both directions of a bidirectional
 *                 circuit must fallow the same path.  Only applicable when
 *                 directionality is "Bidirectional".  If not specified then value
 *                 is assumed to be false.
 *                 
 *                 sourceSTP - Source STP of the service.
 *                 
 *                 destSTP - Destination STP of the service.
 *                 
 *                 ero - Hop-by-hop ordered list of STP from sourceSTP to
 *                 destSTP. List does not include sourceSTP and destSTP.
 *                 
 *                 mtu - Specifies the maximum transmission unit size in bits.
 *                 
 *                 burstsize - Specifies the maximum number of bits that can be
 *                 send to the interface before the sender must wait before
 *                 sending again.
 *             
 * 
 * <p>Java class for EthernetBaseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EthernetBaseType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.ogf.org/nsi/2013/07/services/point2point}P2PServiceBaseType">
 *       &lt;sequence>
 *         &lt;element name="mtu" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="burstsize" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EthernetBaseType", propOrder = {
    "mtu",
    "burstsize"
})
@XmlSeeAlso({
    EthernetVlanType.class
})
public class EthernetBaseType
    extends P2PServiceBaseType
{

    protected Integer mtu;
    protected Long burstsize;

    /**
     * Gets the value of the mtu property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMtu() {
        return mtu;
    }

    /**
     * Sets the value of the mtu property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMtu(Integer value) {
        this.mtu = value;
    }

    /**
     * Gets the value of the burstsize property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getBurstsize() {
        return burstsize;
    }

    /**
     * Sets the value of the burstsize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setBurstsize(Long value) {
        this.burstsize = value;
    }

}
