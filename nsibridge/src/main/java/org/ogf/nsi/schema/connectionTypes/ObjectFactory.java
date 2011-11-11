
package org.ogf.nsi.schema.connectionTypes;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.ogf.nsi.schema.connectionTypes package. 
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

    private final static QName _QueryConfirmed_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/types", "queryConfirmed");
    private final static QName _ForcedEnd_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/types", "forcedEnd");
    private final static QName _TerminateFailed_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/types", "terminateFailed");
    private final static QName _ProvisionFailed_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/types", "provisionFailed");
    private final static QName _Reserve_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/types", "reserve");
    private final static QName _ReserveConfirmed_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/types", "reserveConfirmed");
    private final static QName _Query_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/types", "query");
    private final static QName _TerminateConfirmed_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/types", "terminateConfirmed");
    private final static QName _ReleaseConfirmed_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/types", "releaseConfirmed");
    private final static QName _ProvisionConfirmed_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/types", "provisionConfirmed");
    private final static QName _Release_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/types", "release");
    private final static QName _ReleaseFailed_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/types", "releaseFailed");
    private final static QName _Terminate_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/types", "terminate");
    private final static QName _QueryFailed_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/types", "queryFailed");
    private final static QName _ReserveFailed_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/types", "reserveFailed");
    private final static QName _Provision_QNAME = new QName("http://schemas.ogf.org/nsi/2011/10/connection/types", "provision");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.ogf.nsi.schema.connectionTypes
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link QueryDetailsResultType }
     * 
     */
    public QueryDetailsResultType createQueryDetailsResultType() {
        return new QueryDetailsResultType();
    }

    /**
     * Create an instance of {@link GenericFailedType }
     * 
     */
    public GenericFailedType createGenericFailedType() {
        return new GenericFailedType();
    }

    /**
     * Create an instance of {@link DetailedPathType }
     * 
     */
    public DetailedPathType createDetailedPathType() {
        return new DetailedPathType();
    }

    /**
     * Create an instance of {@link TechnologySpecificAttributesType }
     * 
     */
    public TechnologySpecificAttributesType createTechnologySpecificAttributesType() {
        return new TechnologySpecificAttributesType();
    }

    /**
     * Create an instance of {@link ReserveType }
     * 
     */
    public ReserveType createReserveType() {
        return new ReserveType();
    }

    /**
     * Create an instance of {@link ScheduleType }
     * 
     */
    public ScheduleType createScheduleType() {
        return new ScheduleType();
    }

    /**
     * Create an instance of {@link QuerySummaryResultType }
     * 
     */
    public QuerySummaryResultType createQuerySummaryResultType() {
        return new QuerySummaryResultType();
    }

    /**
     * Create an instance of {@link ReserveConfirmedType }
     * 
     */
    public ReserveConfirmedType createReserveConfirmedType() {
        return new ReserveConfirmedType();
    }

    /**
     * Create an instance of {@link GenericRequestType }
     * 
     */
    public GenericRequestType createGenericRequestType() {
        return new GenericRequestType();
    }

    /**
     * Create an instance of {@link QueryFailedType }
     * 
     */
    public QueryFailedType createQueryFailedType() {
        return new QueryFailedType();
    }

    /**
     * Create an instance of {@link StpListType }
     * 
     */
    public StpListType createStpListType() {
        return new StpListType();
    }

    /**
     * Create an instance of {@link OrderedServiceTerminationPointType }
     * 
     */
    public OrderedServiceTerminationPointType createOrderedServiceTerminationPointType() {
        return new OrderedServiceTerminationPointType();
    }

    /**
     * Create an instance of {@link ServiceTerminationPointType }
     * 
     */
    public ServiceTerminationPointType createServiceTerminationPointType() {
        return new ServiceTerminationPointType();
    }

    /**
     * Create an instance of {@link ChildListType }
     * 
     */
    public ChildListType createChildListType() {
        return new ChildListType();
    }

    /**
     * Create an instance of {@link ServiceParametersType }
     * 
     */
    public ServiceParametersType createServiceParametersType() {
        return new ServiceParametersType();
    }

    /**
     * Create an instance of {@link BandwidthType }
     * 
     */
    public BandwidthType createBandwidthType() {
        return new BandwidthType();
    }

    /**
     * Create an instance of {@link PathType }
     * 
     */
    public PathType createPathType() {
        return new PathType();
    }

    /**
     * Create an instance of {@link GenericConfirmedType }
     * 
     */
    public GenericConfirmedType createGenericConfirmedType() {
        return new GenericConfirmedType();
    }

    /**
     * Create an instance of {@link QueryFilterType }
     * 
     */
    public QueryFilterType createQueryFilterType() {
        return new QueryFilterType();
    }

    /**
     * Create an instance of {@link ReservationInfoType }
     * 
     */
    public ReservationInfoType createReservationInfoType() {
        return new ReservationInfoType();
    }

    /**
     * Create an instance of {@link ComponentPathType }
     * 
     */
    public ComponentPathType createComponentPathType() {
        return new ComponentPathType();
    }

    /**
     * Create an instance of {@link ServiceExceptionType }
     * 
     */
    public ServiceExceptionType createServiceExceptionType() {
        return new ServiceExceptionType();
    }

    /**
     * Create an instance of {@link QueryConfirmedType }
     * 
     */
    public QueryConfirmedType createQueryConfirmedType() {
        return new QueryConfirmedType();
    }

    /**
     * Create an instance of {@link QueryType }
     * 
     */
    public QueryType createQueryType() {
        return new QueryType();
    }

    /**
     * Create an instance of {@link PathListType }
     * 
     */
    public PathListType createPathListType() {
        return new PathListType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryConfirmedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/types", name = "queryConfirmed")
    public JAXBElement<QueryConfirmedType> createQueryConfirmed(QueryConfirmedType value) {
        return new JAXBElement<QueryConfirmedType>(_QueryConfirmed_QNAME, QueryConfirmedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/types", name = "forcedEnd")
    public JAXBElement<GenericRequestType> createForcedEnd(GenericRequestType value) {
        return new JAXBElement<GenericRequestType>(_ForcedEnd_QNAME, GenericRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericFailedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/types", name = "terminateFailed")
    public JAXBElement<GenericFailedType> createTerminateFailed(GenericFailedType value) {
        return new JAXBElement<GenericFailedType>(_TerminateFailed_QNAME, GenericFailedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericFailedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/types", name = "provisionFailed")
    public JAXBElement<GenericFailedType> createProvisionFailed(GenericFailedType value) {
        return new JAXBElement<GenericFailedType>(_ProvisionFailed_QNAME, GenericFailedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReserveType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/types", name = "reserve")
    public JAXBElement<ReserveType> createReserve(ReserveType value) {
        return new JAXBElement<ReserveType>(_Reserve_QNAME, ReserveType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReserveConfirmedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/types", name = "reserveConfirmed")
    public JAXBElement<ReserveConfirmedType> createReserveConfirmed(ReserveConfirmedType value) {
        return new JAXBElement<ReserveConfirmedType>(_ReserveConfirmed_QNAME, ReserveConfirmedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/types", name = "query")
    public JAXBElement<QueryType> createQuery(QueryType value) {
        return new JAXBElement<QueryType>(_Query_QNAME, QueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericConfirmedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/types", name = "terminateConfirmed")
    public JAXBElement<GenericConfirmedType> createTerminateConfirmed(GenericConfirmedType value) {
        return new JAXBElement<GenericConfirmedType>(_TerminateConfirmed_QNAME, GenericConfirmedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericConfirmedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/types", name = "releaseConfirmed")
    public JAXBElement<GenericConfirmedType> createReleaseConfirmed(GenericConfirmedType value) {
        return new JAXBElement<GenericConfirmedType>(_ReleaseConfirmed_QNAME, GenericConfirmedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericConfirmedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/types", name = "provisionConfirmed")
    public JAXBElement<GenericConfirmedType> createProvisionConfirmed(GenericConfirmedType value) {
        return new JAXBElement<GenericConfirmedType>(_ProvisionConfirmed_QNAME, GenericConfirmedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/types", name = "release")
    public JAXBElement<GenericRequestType> createRelease(GenericRequestType value) {
        return new JAXBElement<GenericRequestType>(_Release_QNAME, GenericRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericFailedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/types", name = "releaseFailed")
    public JAXBElement<GenericFailedType> createReleaseFailed(GenericFailedType value) {
        return new JAXBElement<GenericFailedType>(_ReleaseFailed_QNAME, GenericFailedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/types", name = "terminate")
    public JAXBElement<GenericRequestType> createTerminate(GenericRequestType value) {
        return new JAXBElement<GenericRequestType>(_Terminate_QNAME, GenericRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryFailedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/types", name = "queryFailed")
    public JAXBElement<QueryFailedType> createQueryFailed(QueryFailedType value) {
        return new JAXBElement<QueryFailedType>(_QueryFailed_QNAME, QueryFailedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericFailedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/types", name = "reserveFailed")
    public JAXBElement<GenericFailedType> createReserveFailed(GenericFailedType value) {
        return new JAXBElement<GenericFailedType>(_ReserveFailed_QNAME, GenericFailedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/types", name = "provision")
    public JAXBElement<GenericRequestType> createProvision(GenericRequestType value) {
        return new JAXBElement<GenericRequestType>(_Provision_QNAME, GenericRequestType.class, null, value);
    }

}
