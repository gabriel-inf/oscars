use aaa;

-- Changes between release 2 and 3
-- Added siteAdmin attributes
-- Unique indices on institutions, attributes and authorizations
-- run with mysql -u <user> -p --force < upgradeTables.2-3.sql

-- Add unique index on institution name
CREATE UNIQUE INDEX instName ON institutions(name(9));

-- Add Site Administrator attribute- Can manage all reservations starting or terminating at a site
CREATE UNIQUE INDEX attrName ON attributes(name(9));
INSERT IGNORE INTO attributes VALUES(NULL, "OSCARS-siteAdmin", "group", "manage all reservations starting or ending at site");

-- Add authorizations for Site Administrator
CREATE UNIQUE INDEX row ON authorizations (attrId,resourceId,permissionId,constraintName(9));
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-siteAdmin"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="query"),
     "my-site",1);
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-siteAdmin"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="modify"),
     "my-site",1); 
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-siteAdmin"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="list"),
     "my-site",1); 
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-siteAdmin"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="create"),
     "my-site",1);
INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-siteAdmin"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="signal"),
     "my-site",1);
     
 -- Add a few more authorizations that were overlooked
 INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="modify"),
     "specify-path-elements", 1); 
 INSERT IGNORE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="modify"),
     "specify-path-elements", 1); 
      
