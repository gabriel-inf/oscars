package net.es.oscars.nsibridge.beans.db;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.LifecycleStateEnumType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.ProvisionStateEnumType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.ReservationStateEnumType;

import javax.persistence.*;
import java.lang.Long;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class ConnectionRecord {
    protected Long id;
    protected String connectionId;
    protected String oscarsGri;
    protected String requesterNSA;
    protected String notifyUrl;
    protected String nsiGlobalGri;


    protected String oscarsAttributes;



    protected Set<NotificationRecord> notificationRecords= new HashSet<NotificationRecord>();
    protected Set<DataplaneStatusRecord> dataplaneStatusRecords = new HashSet<DataplaneStatusRecord>();
    protected Set<ResvRecord> resvRecords = new HashSet<ResvRecord>();
    protected Set<ExceptionRecord> exceptionRecords = new HashSet<ExceptionRecord>();


    protected OscarsStatusRecord oscarsStatusRecord;

    protected ProvisionStateEnumType provisionState;
    protected LifecycleStateEnumType lifecycleState;

    public ReservationStateEnumType getReserveState() {
        return reserveState;
    }

    public void setReserveState(ReservationStateEnumType reserveState) {
        this.reserveState = reserveState;
    }

    protected ReservationStateEnumType reserveState;


    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }


    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable
    public Set<ExceptionRecord> getExceptionRecords() {
        return exceptionRecords;
    }

    public void setExceptionRecords(Set<ExceptionRecord> exceptionRecords) {
        this.exceptionRecords = exceptionRecords;
    }

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable
    public Set<NotificationRecord> getNotificationRecords() {
        return notificationRecords;
    }

    public void setNotificationRecords(Set<NotificationRecord> notificationRecords) {
        this.notificationRecords = notificationRecords;
    }

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable
    public Set<DataplaneStatusRecord> getDataplaneStatusRecords() {
        return dataplaneStatusRecords;
    }

    public void setDataplaneStatusRecords(Set<DataplaneStatusRecord> dataplaneStatusRecords) {
        this.dataplaneStatusRecords = dataplaneStatusRecords;
    }

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable
    public Set<ResvRecord> getResvRecords() {
        return resvRecords;
    }

    public void setResvRecords(Set<ResvRecord> resvRecords) {
        this.resvRecords = resvRecords;
    }

    public ProvisionStateEnumType getProvisionState() {
        return provisionState;
    }

    public void setProvisionState(ProvisionStateEnumType provisionState) {
        this.provisionState = provisionState;
    }

    public LifecycleStateEnumType getLifecycleState() {
        return lifecycleState;
    }

    public void setLifecycleState(LifecycleStateEnumType lifecycleState) {
        this.lifecycleState = lifecycleState;
    }

    public String getOscarsGri() {
        return oscarsGri;
    }

    public void setOscarsGri(String oscarsGri) {
        this.oscarsGri = oscarsGri;
    }

    @OneToOne(cascade = CascadeType.ALL)
    public OscarsStatusRecord getOscarsStatusRecord() {
        return oscarsStatusRecord;
    }

    public void setOscarsStatusRecord(OscarsStatusRecord oscarsStatusRecord) {
        this.oscarsStatusRecord = oscarsStatusRecord;
    }

    public String getRequesterNSA() {
        return requesterNSA;
    }

    public void setRequesterNSA(String requesterNSA) {
        this.requesterNSA = requesterNSA;
    }

    public String getNsiGlobalGri() {
        return nsiGlobalGri;
    }

    public void setNsiGlobalGri(String nsiGlobalGri) {
        this.nsiGlobalGri = nsiGlobalGri;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }





    public static ResvRecord getCommittedResvRecord(ConnectionRecord cr) {
        ResvRecord res = null;
        for (ResvRecord or : cr.getResvRecords()) {
            if (or.isCommitted()) {
                if (res == null) {
                    res = or;
                } else {
                    if (res.getVersion() > or.getVersion()) {
                        res = or;
                    }
                }
            }
        }
        return res;
    }

    public static List<ResvRecord> getUncommittedResvRecords(ConnectionRecord cr) {
        ArrayList<ResvRecord> results = new ArrayList<ResvRecord>();
        for (ResvRecord or : cr.getResvRecords()) {
            if (!or.isCommitted()) {
                results.add(or);
            }
        }
        return results;
    }

    @Lob
    @Column(length=65535)
    public String getOscarsAttributes() {
        return oscarsAttributes;
    }

    public void setOscarsAttributes(String oscarsAttributes) {
        this.oscarsAttributes = oscarsAttributes;
    }
}
