
package org.ogf.nsi.schema.connectionTypes;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 A simple ordered list if list of Service Termination Point (STP).
 *                 List order is determined by the interger order attribute in the
 *                 OrderedServiceTerminationPointType.
 * 
 *                 Elements:
 * 
 *                 stp - A list of STP ordered 0..n by their integer order attribute.
 *             
 * 
 * <p>Java class for StpListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StpListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="stp" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}OrderedServiceTerminationPointType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StpListType", propOrder = {
    "stp"
})
public class StpListType {

    protected List<OrderedServiceTerminationPointType> stp;

    /**
     * Gets the value of the stp property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the stp property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStp().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OrderedServiceTerminationPointType }
     * 
     * 
     */
    public List<OrderedServiceTerminationPointType> getStp() {
        if (stp == null) {
            stp = new ArrayList<OrderedServiceTerminationPointType>();
        }
        return this.stp;
    }

}
