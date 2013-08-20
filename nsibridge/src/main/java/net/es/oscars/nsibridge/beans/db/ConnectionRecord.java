package net.es.oscars.nsibridge.beans.db;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.LifecycleStateEnumType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ProvisionStateEnumType;

import javax.persistence.*;
import java.lang.Long;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;

@Entity
public class ConnectionRecord {
    protected Long id;
    protected String connectionId;
    protected String oscarsGri;

    protected Set<DataplaneStatusRecord> dataplaneStatusRecords = new HashSet<DataplaneStatusRecord>();
    protected Set<ResvRecord> resvRecords = new HashSet<ResvRecord>();
    protected Set<OscarsStatusRecord> oscarsStatusRecords = new HashSet<OscarsStatusRecord>();
    protected ProvisionStateEnumType provisionState;
    protected LifecycleStateEnumType lifecycleState;


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

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable
    public Set<OscarsStatusRecord> getOscarsStatusRecords() {
        return oscarsStatusRecords;
    }

    public void setOscarsStatusRecords(Set<OscarsStatusRecord> oscarsStatusRecords) {
        this.oscarsStatusRecords = oscarsStatusRecords;
    }






    // helper methods here

    public static OscarsStatusRecord getLatestStatusRecord(ConnectionRecord cr) {
        OscarsStatusRecord res = null;
        for (OscarsStatusRecord or : cr.getOscarsStatusRecords()) {
            if (res == null) {
                res = or;
            } else {
                if (or.getDate().after(res.getDate())) {
                    res = or;
                }
            }
        }
        return res;
    }


}
