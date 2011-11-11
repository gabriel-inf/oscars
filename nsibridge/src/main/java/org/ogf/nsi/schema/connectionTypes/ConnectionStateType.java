
package org.ogf.nsi.schema.connectionTypes;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ConnectionStateType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ConnectionStateType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Initial"/>
 *     &lt;enumeration value="Reserving"/>
 *     &lt;enumeration value="Reserved"/>
 *     &lt;enumeration value="Auto-Provision"/>
 *     &lt;enumeration value="Scheduled"/>
 *     &lt;enumeration value="Provisioning"/>
 *     &lt;enumeration value="Provisioned"/>
 *     &lt;enumeration value="Releasing"/>
 *     &lt;enumeration value="Cleaning"/>
 *     &lt;enumeration value="Terminating"/>
 *     &lt;enumeration value="Terminated"/>
 *     &lt;enumeration value="Unknown"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ConnectionStateType")
@XmlEnum
public enum ConnectionStateType {

    @XmlEnumValue("Initial")
    INITIAL("Initial"),
    @XmlEnumValue("Reserving")
    RESERVING("Reserving"),
    @XmlEnumValue("Reserved")
    RESERVED("Reserved"),
    @XmlEnumValue("Auto-Provision")
    AUTO_PROVISION("Auto-Provision"),
    @XmlEnumValue("Scheduled")
    SCHEDULED("Scheduled"),
    @XmlEnumValue("Provisioning")
    PROVISIONING("Provisioning"),
    @XmlEnumValue("Provisioned")
    PROVISIONED("Provisioned"),
    @XmlEnumValue("Releasing")
    RELEASING("Releasing"),
    @XmlEnumValue("Cleaning")
    CLEANING("Cleaning"),
    @XmlEnumValue("Terminating")
    TERMINATING("Terminating"),
    @XmlEnumValue("Terminated")
    TERMINATED("Terminated"),
    @XmlEnumValue("Unknown")
    UNKNOWN("Unknown");
    private final String value;

    ConnectionStateType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ConnectionStateType fromValue(String v) {
        for (ConnectionStateType c: ConnectionStateType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
