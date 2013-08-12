
package net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.point2point;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 Point-to-Point Ethernet VLAN service definition.
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
 *                 sourceVLAN -  Specifies the VLAN identifier for the source port
 *                 (1 - 4095).
 *                 
 *                 destVLAN -  Specifies the VLAN identifier for the destination port
 *                 (1 - 4095).
 *             
 * 
 * <p>Java class for EthernetVlanType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EthernetVlanType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.ogf.org/nsi/2013/07/services/point2point}EthernetBaseType">
 *       &lt;sequence>
 *         &lt;element name="sourceVLAN" type="{http://schemas.ogf.org/nsi/2013/07/services/point2point}vlanIdType"/>
 *         &lt;element name="destVLAN" type="{http://schemas.ogf.org/nsi/2013/07/services/point2point}vlanIdType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EthernetVlanType", propOrder = {
    "sourceVLAN",
    "destVLAN"
})
public class EthernetVlanType
    extends EthernetBaseType
{

    protected int sourceVLAN;
    protected int destVLAN;

    /**
     * Gets the value of the sourceVLAN property.
     * 
     */
    public int getSourceVLAN() {
        return sourceVLAN;
    }

    /**
     * Sets the value of the sourceVLAN property.
     * 
     */
    public void setSourceVLAN(int value) {
        this.sourceVLAN = value;
    }

    /**
     * Gets the value of the destVLAN property.
     * 
     */
    public int getDestVLAN() {
        return destVLAN;
    }

    /**
     * Sets the value of the destVLAN property.
     * 
     */
    public void setDestVLAN(int value) {
        this.destVLAN = value;
    }

}
