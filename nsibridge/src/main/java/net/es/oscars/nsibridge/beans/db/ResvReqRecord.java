package net.es.oscars.nsibridge.beans.db;
import javax.persistence.*;
import java.lang.Long;import java.lang.String;

@Entity
public class ResvReqRecord {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    public Long getId() {
        return id;
    }

}
