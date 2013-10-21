package net.es.oscars.nsibridge.beans.db;


import net.es.oscars.nsibridge.ifces.CallbackMessages;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.types.EventEnumType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class NotificationRecord {
    protected Long id;
    protected Date timestamp;
    protected CallbackMessages notificationType;
    protected EventEnumType eventType;

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public CallbackMessages getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(CallbackMessages notificationType) {
        this.notificationType = notificationType;
    }

    public EventEnumType getEventType() {
        return eventType;
    }

    public void setEventType(EventEnumType eventType) {
        this.eventType = eventType;
    }
}
