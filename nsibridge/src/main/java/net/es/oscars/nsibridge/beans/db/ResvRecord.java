package net.es.oscars.nsibridge.beans.db;

import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ReservationStateEnumType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class ResvRecord {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    protected Long id;
    protected int version;
    protected Date date;

    protected ReservationStateEnumType reservationState;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReservationStateEnumType getReservationState() {
        return reservationState;
    }

    public void setReservationState(ReservationStateEnumType reservationState) {
        this.reservationState = reservationState;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
