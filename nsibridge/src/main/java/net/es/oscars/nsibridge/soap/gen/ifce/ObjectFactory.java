
package net.es.oscars.nsibridge.soap.gen.ifce;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.ogf.nsi.schema.connectionTypes.ServiceExceptionType;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the net.es.oscars.nsibridge.soap.gen.ifce package. 
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

    private final static QName _TerminateConfirmed_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/interface", "terminateConfirmed");
    private final static QName _ReleaseConfirmed_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/interface", "releaseConfirmed");
    private final static QName _TerminateRequest_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/interface", "terminateRequest");
    private final static QName _ProvisionConfirmed_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/interface", "provisionConfirmed");
    private final static QName _QueryRequest_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/interface", "queryRequest");
    private final static QName _QueryConfirmed_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/interface", "queryConfirmed");
    private final static QName _ProvisionFailed_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/interface", "provisionFailed");
    private final static QName _TerminateFailed_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/interface", "terminateFailed");
    private final static QName _ReleaseRequest_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/interface", "releaseRequest");
    private final static QName _ReserveConfirmed_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/interface", "reserveConfirmed");
    private final static QName _ReleaseFailed_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/interface", "releaseFailed");
    private final static QName _QueryFailed_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/interface", "queryFailed");
    private final static QName _ForcedEndRequest_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/interface", "forcedEndRequest");
    private final static QName _ReserveFailed_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/interface", "reserveFailed");
    private final static QName _Acknowledgment_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/interface", "acknowledgment");
    private final static QName _ReserveRequest_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/interface", "reserveRequest");
    private final static QName _ProvisionRequest_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/interface", "provisionRequest");
    private final static QName _ServiceException_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/interface", "serviceException");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.es.oscars.nsibridge.soap.gen.ifce
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ReleaseConfirmedRequestType }
     * 
     */
    public ReleaseConfirmedRequestType createReleaseConfirmedRequestType() {
        return new ReleaseConfirmedRequestType();
    }

    /**
     * Create an instance of {@link QueryFailedRequestType }
     * 
     */
    public QueryFailedRequestType createQueryFailedRequestType() {
        return new QueryFailedRequestType();
    }

    /**
     * Create an instance of {@link ReserveFailedRequestType }
     * 
     */
    public ReserveFailedRequestType createReserveFailedRequestType() {
        return new ReserveFailedRequestType();
    }

    /**
     * Create an instance of {@link ProvisionConfirmedRequestType }
     * 
     */
    public ProvisionConfirmedRequestType createProvisionConfirmedRequestType() {
        return new ProvisionConfirmedRequestType();
    }

    /**
     * Create an instance of {@link ReleaseRequestType }
     * 
     */
    public ReleaseRequestType createReleaseRequestType() {
        return new ReleaseRequestType();
    }

    /**
     * Create an instance of {@link TerminateFailedRequestType }
     * 
     */
    public TerminateFailedRequestType createTerminateFailedRequestType() {
        return new TerminateFailedRequestType();
    }

    /**
     * Create an instance of {@link ReserveConfirmedRequestType }
     * 
     */
    public ReserveConfirmedRequestType createReserveConfirmedRequestType() {
        return new ReserveConfirmedRequestType();
    }

    /**
     * Create an instance of {@link QueryConfirmedRequestType }
     * 
     */
    public QueryConfirmedRequestType createQueryConfirmedRequestType() {
        return new QueryConfirmedRequestType();
    }

    /**
     * Create an instance of {@link ProvisionFailedRequestType }
     * 
     */
    public ProvisionFailedRequestType createProvisionFailedRequestType() {
        return new ProvisionFailedRequestType();
    }

    /**
     * Create an instance of {@link ProvisionRequestType }
     * 
     */
    public ProvisionRequestType createProvisionRequestType() {
        return new ProvisionRequestType();
    }

    /**
     * Create an instance of {@link ForcedEndRequestType }
     * 
     */
    public ForcedEndRequestType createForcedEndRequestType() {
        return new ForcedEndRequestType();
    }

    /**
     * Create an instance of {@link ReleaseFailedRequestType }
     * 
     */
    public ReleaseFailedRequestType createReleaseFailedRequestType() {
        return new ReleaseFailedRequestType();
    }

    /**
     * Create an instance of {@link GenericAcknowledgmentType }
     * 
     */
    public GenericAcknowledgmentType createGenericAcknowledgmentType() {
        return new GenericAcknowledgmentType();
    }

    /**
     * Create an instance of {@link QueryRequestType }
     * 
     */
    public QueryRequestType createQueryRequestType() {
        return new QueryRequestType();
    }

    /**
     * Create an instance of {@link TerminateRequestType }
     * 
     */
    public TerminateRequestType createTerminateRequestType() {
        return new TerminateRequestType();
    }

    /**
     * Create an instance of {@link ReserveRequestType }
     * 
     */
    public ReserveRequestType createReserveRequestType() {
        return new ReserveRequestType();
    }

    /**
     * Create an instance of {@link TerminateConfirmedRequestType }
     * 
     */
    public TerminateConfirmedRequestType createTerminateConfirmedRequestType() {
        return new TerminateConfirmedRequestType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TerminateConfirmedRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/interface", name = "terminateConfirmed")
    public JAXBElement<TerminateConfirmedRequestType> createTerminateConfirmed(TerminateConfirmedRequestType value) {
        return new JAXBElement<TerminateConfirmedRequestType>(_TerminateConfirmed_QNAME, TerminateConfirmedRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReleaseConfirmedRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/interface", name = "releaseConfirmed")
    public JAXBElement<ReleaseConfirmedRequestType> createReleaseConfirmed(ReleaseConfirmedRequestType value) {
        return new JAXBElement<ReleaseConfirmedRequestType>(_ReleaseConfirmed_QNAME, ReleaseConfirmedRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TerminateRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/interface", name = "terminateRequest")
    public JAXBElement<TerminateRequestType> createTerminateRequest(TerminateRequestType value) {
        return new JAXBElement<TerminateRequestType>(_TerminateRequest_QNAME, TerminateRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProvisionConfirmedRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/interface", name = "provisionConfirmed")
    public JAXBElement<ProvisionConfirmedRequestType> createProvisionConfirmed(ProvisionConfirmedRequestType value) {
        return new JAXBElement<ProvisionConfirmedRequestType>(_ProvisionConfirmed_QNAME, ProvisionConfirmedRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/interface", name = "queryRequest")
    public JAXBElement<QueryRequestType> createQueryRequest(QueryRequestType value) {
        return new JAXBElement<QueryRequestType>(_QueryRequest_QNAME, QueryRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryConfirmedRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/interface", name = "queryConfirmed")
    public JAXBElement<QueryConfirmedRequestType> createQueryConfirmed(QueryConfirmedRequestType value) {
        return new JAXBElement<QueryConfirmedRequestType>(_QueryConfirmed_QNAME, QueryConfirmedRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProvisionFailedRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/interface", name = "provisionFailed")
    public JAXBElement<ProvisionFailedRequestType> createProvisionFailed(ProvisionFailedRequestType value) {
        return new JAXBElement<ProvisionFailedRequestType>(_ProvisionFailed_QNAME, ProvisionFailedRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TerminateFailedRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/interface", name = "terminateFailed")
    public JAXBElement<TerminateFailedRequestType> createTerminateFailed(TerminateFailedRequestType value) {
        return new JAXBElement<TerminateFailedRequestType>(_TerminateFailed_QNAME, TerminateFailedRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReleaseRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/interface", name = "releaseRequest")
    public JAXBElement<ReleaseRequestType> createReleaseRequest(ReleaseRequestType value) {
        return new JAXBElement<ReleaseRequestType>(_ReleaseRequest_QNAME, ReleaseRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReserveConfirmedRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/interface", name = "reserveConfirmed")
    public JAXBElement<ReserveConfirmedRequestType> createReserveConfirmed(ReserveConfirmedRequestType value) {
        return new JAXBElement<ReserveConfirmedRequestType>(_ReserveConfirmed_QNAME, ReserveConfirmedRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReleaseFailedRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/interface", name = "releaseFailed")
    public JAXBElement<ReleaseFailedRequestType> createReleaseFailed(ReleaseFailedRequestType value) {
        return new JAXBElement<ReleaseFailedRequestType>(_ReleaseFailed_QNAME, ReleaseFailedRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryFailedRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/interface", name = "queryFailed")
    public JAXBElement<QueryFailedRequestType> createQueryFailed(QueryFailedRequestType value) {
        return new JAXBElement<QueryFailedRequestType>(_QueryFailed_QNAME, QueryFailedRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ForcedEndRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/interface", name = "forcedEndRequest")
    public JAXBElement<ForcedEndRequestType> createForcedEndRequest(ForcedEndRequestType value) {
        return new JAXBElement<ForcedEndRequestType>(_ForcedEndRequest_QNAME, ForcedEndRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReserveFailedRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/interface", name = "reserveFailed")
    public JAXBElement<ReserveFailedRequestType> createReserveFailed(ReserveFailedRequestType value) {
        return new JAXBElement<ReserveFailedRequestType>(_ReserveFailed_QNAME, ReserveFailedRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericAcknowledgmentType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/interface", name = "acknowledgment")
    public JAXBElement<GenericAcknowledgmentType> createAcknowledgment(GenericAcknowledgmentType value) {
        return new JAXBElement<GenericAcknowledgmentType>(_Acknowledgment_QNAME, GenericAcknowledgmentType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReserveRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/interface", name = "reserveRequest")
    public JAXBElement<ReserveRequestType> createReserveRequest(ReserveRequestType value) {
        return new JAXBElement<ReserveRequestType>(_ReserveRequest_QNAME, ReserveRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProvisionRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/interface", name = "provisionRequest")
    public JAXBElement<ProvisionRequestType> createProvisionRequest(ProvisionRequestType value) {
        return new JAXBElement<ProvisionRequestType>(_ProvisionRequest_QNAME, ProvisionRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServiceExceptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/interface", name = "serviceException")
    public JAXBElement<ServiceExceptionType> createServiceException(ServiceExceptionType value) {
        return new JAXBElement<ServiceExceptionType>(_ServiceException_QNAME, ServiceExceptionType.class, null, value);
    }

}
