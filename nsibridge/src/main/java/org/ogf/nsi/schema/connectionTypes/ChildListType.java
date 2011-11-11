
package org.ogf.nsi.schema.connectionTypes;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 A holder element providing an evelope which will contain the
 *                 list of child NSA and associated connection information.
 *                 
 *                 Elements:
 *                 
 *                 child - The detailed path information for a child NSA.  Each
 *                 child element is ordered and contains a connection segment in
 *                 the overall path.
 *             
 * 
 * <p>Java class for ChildListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ChildListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="child" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}DetailedPathType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChildListType", propOrder = {
    "child"
})
public class ChildListType {

    protected List<DetailedPathType> child;

    /**
     * Gets the value of the child property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the child property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChild().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DetailedPathType }
     * 
     * 
     */
    public List<DetailedPathType> getChild() {
        if (child == null) {
            child = new ArrayList<DetailedPathType>();
        }
        return this.child;
    }

}
