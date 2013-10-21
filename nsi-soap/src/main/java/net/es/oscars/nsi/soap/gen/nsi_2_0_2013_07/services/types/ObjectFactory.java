
package net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.services.types;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.services.types package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _StpList_QNAME = new QName("http://schemas.ogf.org/nsi/2013/07/services/types", "stpList");
    private final static QName _Stp_QNAME = new QName("http://schemas.ogf.org/nsi/2013/07/services/types", "stp");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.services.types
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link StpListType }
     * 
     */
    public StpListType createStpListType() {
        return new StpListType();
    }

    /**
     * Create an instance of {@link StpType }
     * 
     */
    public StpType createStpType() {
        return new StpType();
    }

    /**
     * Create an instance of {@link OrderedStpType }
     * 
     */
    public OrderedStpType createOrderedStpType() {
        return new OrderedStpType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StpListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2013/07/services/types", name = "stpList")
    public JAXBElement<StpListType> createStpList(StpListType value) {
        return new JAXBElement<StpListType>(_StpList_QNAME, StpListType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StpType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2013/07/services/types", name = "stp")
    public JAXBElement<StpType> createStp(StpType value) {
        return new JAXBElement<StpType>(_Stp_QNAME, StpType.class, null, value);
    }

}
