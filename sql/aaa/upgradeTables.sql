USE aaa;

-- attribute for an IDC in an adjacent network domain. It's attributes implement
-- an SLA between domains.  Currently set to all permissions on reservations and 
-- query permissions for domains, no permissions on users
INSERT INTO attributes VALUES(NULL, "OSCARS-service", "group");

-- authorizations for service user 
-- note that all the reservations fowarded by a service are owned by the service
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="domains"),
     (select id from permissions where name="query"),
     NULL, NULL);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="query"),
     NULL, NULL);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="modify"),
     NULL, NULL); 
-- list is only used in debugging interdomain interactions
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="list"),
     NULL, NULL); 
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="create"),
     NULL, NULL);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="create"),
     "specify-path-elements", 1); 
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="create"),
     "specify-gri", 1); 
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="signal"),
     NULL, NULL);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="domains"),
     (select id from permissions where name="query"),
     NULL, NULL);
 INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="domains"),
     (select id from permissions where name="modify"),
     NULL, NULL);
     
-- for use by NOC operators. Can see all reservations.
INSERT INTO attributes VALUES(NULL, "OSCARS-operator", "group");

-- NOC operators
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-operator"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="list"),
     "all-users", 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-operator"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="query"),
     "all-users", 1); 
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-operator"),
     (select id from resources where name="users"),
     (select id from permissions where name="list"),
    "all-users", 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-operator"),
     (select id from resources where name="users"),
     (select id from permissions where name="query"),
    "all-users", 0);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-operator"),
     (select id from resources where name="users"),
     (select id from permissions where name="modify"),
    "all-users", 0);