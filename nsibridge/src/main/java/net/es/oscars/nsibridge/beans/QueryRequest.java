package net.es.oscars.nsibridge.beans;

import net.es.nsi.lib.soap.gen.nsi_2_0_r117.connection.types.QueryType;

public class QueryRequest extends GenericRequest {

    private QueryType query;

    public QueryType getQuery() {
        return query;
    }

    public void setQuery(QueryType query) {
        this.query = query;
    }
}
