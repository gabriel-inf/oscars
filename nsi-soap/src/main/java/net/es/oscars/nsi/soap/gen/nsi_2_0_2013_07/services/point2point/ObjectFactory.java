
package net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.services.point2point;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.services.point2point package. 
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

    private final static QName _P2Ps_QNAME = new QName("http://schemas.ogf.org/nsi/2013/07/services/point2point", "p2ps");
    private final static QName _Evts_QNAME = new QName("http://schemas.ogf.org/nsi/2013/07/services/point2point", "evts");
    private final static QName _Capacity_QNAME = new QName("http://schemas.ogf.org/nsi/2013/07/services/point2point", "capacity");
    private final static QName _Ets_QNAME = new QName("http://schemas.ogf.org/nsi/2013/07/services/point2point", "ets");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.services.point2point
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EthernetBaseType }
     * 
     */
    public EthernetBaseType createEthernetBaseType() {
        return new EthernetBaseType();
    }

    /**
     * Create an instance of {@link EthernetVlanType }
     * 
     */
    public EthernetVlanType createEthernetVlanType() {
        return new EthernetVlanType();
    }

    /**
     * Create an instance of {@link P2PServiceBaseType }
     * 
     */
    public P2PServiceBaseType createP2PServiceBaseType() {
        return new P2PServiceBaseType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link P2PServiceBaseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2013/07/services/point2point", name = "p2ps")
    public JAXBElement<P2PServiceBaseType> createP2Ps(P2PServiceBaseType value) {
        return new JAXBElement<P2PServiceBaseType>(_P2Ps_QNAME, P2PServiceBaseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EthernetVlanType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2013/07/services/point2point", name = "evts")
    public JAXBElement<EthernetVlanType> createEvts(EthernetVlanType value) {
        return new JAXBElement<EthernetVlanType>(_Evts_QNAME, EthernetVlanType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2013/07/services/point2point", name = "capacity")
    public JAXBElement<Long> createCapacity(Long value) {
        return new JAXBElement<Long>(_Capacity_QNAME, Long.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EthernetBaseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2013/07/services/point2point", name = "ets")
    public JAXBElement<EthernetBaseType> createEts(EthernetBaseType value) {
        return new JAXBElement<EthernetBaseType>(_Ets_QNAME, EthernetBaseType.class, null, value);
    }

}
