
package net.es.oscars.nsibridge.soap.gen.ifce;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.ogf.nsi.schema.connectionTypes.QueryFailedType;


/**
 * 
 *                         Provides transport envelope for the query failed
 *                         message.  This message is generated for erro cases
 *                         such at authorization failures and not for a query
 *                         that would return empty results.  This will map to
 *                         a WSDL request (input) message type in support of
 *                         the NSI CS protocol.
 *                         
 *                         Elements:
 *                         correlationId - The identifier provided in the original
 *                         query request.
 *                         
 *                         terminateFailed - negative terminate result.
 *                     
 * 
 * <p>Java class for QueryFailedRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QueryFailedRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="correlationId" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}uuidType"/>
 *         &lt;element ref="{http://schemas.ogf.org/nsi/2011/10/connection/types}queryFailed"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryFailedRequestType", propOrder = {
    "correlationId",
    "queryFailed"
})
public class QueryFailedRequestType {

    @XmlElement(required = true)
    protected String correlationId;
    @XmlElement(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/types", required = true)
    protected QueryFailedType queryFailed;

    /**
     * Gets the value of the correlationId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Sets the value of the correlationId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCorrelationId(String value) {
        this.correlationId = value;
    }

    /**
     * Gets the value of the queryFailed property.
     * 
     * @return
     *     possible object is
     *     {@link QueryFailedType }
     *     
     */
    public QueryFailedType getQueryFailed() {
        return queryFailed;
    }

    /**
     * Sets the value of the queryFailed property.
     * 
     * @param value
     *     allowed object is
     *     {@link QueryFailedType }
     *     
     */
    public void setQueryFailed(QueryFailedType value) {
        this.queryFailed = value;
    }

}
