
package net.es.oscars.nsibridge.soap.gen.ifce;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.ogf.nsi.schema.connectionTypes.QueryConfirmedType;


/**
 * 
 *                         Provides transport envelope for the query confirmed
 *                         message.  Will map to a WSDL request (input) message type
 *                         in support of the NSI CS protocol.
 *                         
 *                         Elements:
 *                         correlationId - The identifier provided in the original
 *                         query request.
 *                         
 *                         queryConfirmed - query results may be empty if no
 *                         matches were found.
 *                     
 * 
 * <p>Java class for QueryConfirmedRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QueryConfirmedRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="correlationId" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}uuidType"/>
 *         &lt;element ref="{http://schemas.ogf.org/nsi/2011/10/connection/types}queryConfirmed"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryConfirmedRequestType", propOrder = {
    "correlationId",
    "queryConfirmed"
})
public class QueryConfirmedRequestType {

    @XmlElement(required = true)
    protected String correlationId;
    @XmlElement(namespace = "http://schemas.ogf.org/nsi/2011/10/connection/types", required = true)
    protected QueryConfirmedType queryConfirmed;

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
     * Gets the value of the queryConfirmed property.
     * 
     * @return
     *     possible object is
     *     {@link QueryConfirmedType }
     *     
     */
    public QueryConfirmedType getQueryConfirmed() {
        return queryConfirmed;
    }

    /**
     * Sets the value of the queryConfirmed property.
     * 
     * @param value
     *     allowed object is
     *     {@link QueryConfirmedType }
     *     
     */
    public void setQueryConfirmed(QueryConfirmedType value) {
        this.queryConfirmed = value;
    }

}
