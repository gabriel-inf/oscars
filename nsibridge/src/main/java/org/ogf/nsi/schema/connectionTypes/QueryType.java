
package org.ogf.nsi.schema.connectionTypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;


/**
 * 
 *                 Type definition for the queryRequest message providing a mechanism
 *                 for either Requester or Provider NSA to query the other NSA for a
 *                 set of connection service reservation instances between the RA-PA
 *                 pair. This message can also be used as a status polling mechanism.
 * 
 *                 Elements:
 * 
 *                 requesterNSA - NSA that requested the NSI connection service
 *                 operation (Requester Agent).
 * 
 *                 providerNSA - NSA that services the NSI connection service
 *                 operation (Provider Agent).
 * 
 *                 sessionSecurityAttr - Security attributes associated with the NSI
 *                 connection services session between a Requester/Provider NSA pair.
 * 
 *                 queryFilter - Parameter specifying the query criteria to match
 *                 against reserved connections. Any matching connections must be
 *                 returned.
 *             
 * 
 * <p>Java class for QueryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QueryType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="requesterNSA" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}NsaIdType"/>
 *         &lt;element name="providerNSA" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}NsaIdType"/>
 *         &lt;element name="sessionSecurityAttr" type="{urn:oasis:names:tc:SAML:2.0:assertion}AttributeStatementType" minOccurs="0"/>
 *         &lt;element name="operation" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}QueryOperationType"/>
 *         &lt;element name="queryFilter" type="{http://schemas.ogf.org/nsi/2011/10/connection/types}QueryFilterType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryType", propOrder = {
    "requesterNSA",
    "providerNSA",
    "sessionSecurityAttr",
    "operation",
    "queryFilter"
})
public class QueryType {

    @XmlElement(required = true)
    protected String requesterNSA;
    @XmlElement(required = true)
    protected String providerNSA;
    protected AttributeStatementType sessionSecurityAttr;
    @XmlElement(required = true, defaultValue = "Summary")
    protected QueryOperationType operation;
    @XmlElement(required = true)
    protected QueryFilterType queryFilter;

    /**
     * Gets the value of the requesterNSA property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequesterNSA() {
        return requesterNSA;
    }

    /**
     * Sets the value of the requesterNSA property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequesterNSA(String value) {
        this.requesterNSA = value;
    }

    /**
     * Gets the value of the providerNSA property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProviderNSA() {
        return providerNSA;
    }

    /**
     * Sets the value of the providerNSA property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProviderNSA(String value) {
        this.providerNSA = value;
    }

    /**
     * Gets the value of the sessionSecurityAttr property.
     * 
     * @return
     *     possible object is
     *     {@link AttributeStatementType }
     *     
     */
    public AttributeStatementType getSessionSecurityAttr() {
        return sessionSecurityAttr;
    }

    /**
     * Sets the value of the sessionSecurityAttr property.
     * 
     * @param value
     *     allowed object is
     *     {@link AttributeStatementType }
     *     
     */
    public void setSessionSecurityAttr(AttributeStatementType value) {
        this.sessionSecurityAttr = value;
    }

    /**
     * Gets the value of the operation property.
     * 
     * @return
     *     possible object is
     *     {@link QueryOperationType }
     *     
     */
    public QueryOperationType getOperation() {
        return operation;
    }

    /**
     * Sets the value of the operation property.
     * 
     * @param value
     *     allowed object is
     *     {@link QueryOperationType }
     *     
     */
    public void setOperation(QueryOperationType value) {
        this.operation = value;
    }

    /**
     * Gets the value of the queryFilter property.
     * 
     * @return
     *     possible object is
     *     {@link QueryFilterType }
     *     
     */
    public QueryFilterType getQueryFilter() {
        return queryFilter;
    }

    /**
     * Sets the value of the queryFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link QueryFilterType }
     *     
     */
    public void setQueryFilter(QueryFilterType value) {
        this.queryFilter = value;
    }

}
