
package net.es.oscars.nsibridge.soap.gen.ifce;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import org.ogf.nsi.schema.connectionTypes.QueryType;


/**
 * 
 *                         Provides transport envelope for the query request
 *                         message.  Will map to a WSDL request (input) message
 *                         type in support of the NSI CS protocol.
 *                         
 *                         Elements:
 *                         correlationId - An identifier provided by the requesting
 *                         NSA used to correlate to an asynchronous response from the
 *                         responder. It is recommended that a Universally Unique
 *                         Identifier (UUID) URN as per IETF RFC 4122 be used as a
 *                         globally unique value.
 *                         
 *                         replyTo - The Requester NSA's SOAP end point address to
 *                         which the asynchronous Confirmed or Failed message
 *                         associated with this request will be delivered.
 *                         
 *                         query - query message specific parameters.
 *                     
 * 
 * <p>Java class for QueryRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QueryRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="correlationId" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}uuidType"/>
 *         &lt;element name="replyTo" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *         &lt;element ref="{http://schemas.ogf.org/nsi/2011/10/connection/types}query"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryRequestType", propOrder = {
    "correlationId",
    "replyTo",
    "query"
})
public class QueryRequestType {

    @XmlElement(required = true)
    protected String correlationId;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String replyTo;
    @XmlElement(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/types", required = true)
    protected QueryType query;

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
     * Gets the value of the replyTo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReplyTo() {
        return replyTo;
    }

    /**
     * Sets the value of the replyTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReplyTo(String value) {
        this.replyTo = value;
    }

    /**
     * Gets the value of the query property.
     * 
     * @return
     *     possible object is
     *     {@link QueryType }
     *     
     */
    public QueryType getQuery() {
        return query;
    }

    /**
     * Sets the value of the query property.
     * 
     * @param value
     *     allowed object is
     *     {@link QueryType }
     *     
     */
    public void setQuery(QueryType value) {
        this.query = value;
    }

}
