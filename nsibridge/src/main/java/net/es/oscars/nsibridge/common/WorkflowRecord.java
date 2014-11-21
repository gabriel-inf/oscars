package net.es.oscars.nsibridge.common;

import java.util.HashMap;

public class WorkflowRecord {
    private WorkflowRecord() {

    }
    private static WorkflowRecord instance;
    public static WorkflowRecord getInstance() {
        if (instance == null) {
            instance = new WorkflowRecord();
        }
        return instance;
    }

    private HashMap<String, HashMap<WorkflowAction, Object>> records = new HashMap<String, HashMap<WorkflowAction, Object>>();

    public void setRecord(String connectionId, WorkflowAction action, Object record) {
        HashMap<WorkflowAction, Object> connectionRecords = records.get(connectionId);

        if (connectionRecords == null) {
            connectionRecords = new HashMap<WorkflowAction, Object>();
            records.put(connectionId, connectionRecords);
        }

        connectionRecords.put(action, record);
    }

    public Object getRecord(String connectionId, WorkflowAction action) throws NullPointerException {
        HashMap<WorkflowAction, Object> connectionRecords = records.get(connectionId);
        if (connectionRecords == null) {
            throw new NullPointerException();
        }
        if (connectionRecords.get(action) == null) {
            throw new NullPointerException();
        }
        return connectionRecords.get(action);

    }

}
