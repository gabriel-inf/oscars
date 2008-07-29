-- upgrade aaa tables from 3.2 to 4.0

USE aaa;

-- add a unique index to userAttributes table
CREATE UNIQUE INDEX userAttr ON userAttributes(userId,attributeId);

-- Add a resource to control modification of attributes, authorizations and institutions
CREATE UNIQUE INDEX resourceName ON resources(name(6));
INSERT INTO resources VALUES(NULL, "AAA",
                        "Information about Institutions, Attributes and Authorizations",
                        NULL);
                        
REPLACE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-administrator"),
     (select id from resources where name="AAA"),
     (select id from permissions where name="list"),
     NULL,NULL); 
        
REPLACE INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-administrator"),
     (select id from resources where name="AAA"),
     (select id from permissions where name="modify"),
     NULL,NULL);
     
-- Create constraints table for use of the AAA web interface
CREATE TABLE IF NOT EXISTS constraints (
    id                  INT NOT NULL AUTO_INCREMENT,
    name                TEXT NOT NULL,
    description			TEXT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;
CREATE UNIQUE INDEX constraintName ON constraints(name(9)); 

INSERT INTO constraints VALUES (NULL, "all-users","allows access to reservations or details of all users");
INSERT INTO constraints VALUES (NULL, "max-bandwidth", "limits reservations to specified bandwidth");
INSERT INTO constraints VALUES (NULL, "max-duration", "limits reservations to specified duration");
INSERT INTO constraints VALUES (NULL, "my-site", "limits access to reservations to those starting or ending at users site");
INSERT INTO constraints VALUES (NULL, "specify-path-elements", "allows path elements to be specified for reservations");
INSERT INTO constraints VALUES (NULL, "specify-gri", "allows a gri to be specified on path creation");

-- replace resourcePermission table with resource-permission-constraint table
DROP TABLE resourcePermissions;

-- Create resource, permission, constraint (RPC) table which contains a list of the meaningful RPC tuples
CREATE TABLE IF NOT EXISTS rpc (
   id              		INT NOT NULL AUTO_INCREMENT,
   resourceId			INT NOT NULL,
   permissionId			INT NOT NULL,
   constraintId			INT NOT NULL,
   PRIMARY KEY (id)
) type=MyISAM;
CREATE UNIQUE INDEX const ON rpc(resourceId,permissionId,constraintId); 

-- all-users constraint
INSERT INTO rpc VALUES (NULL,
	(select id from resources where name="users"),
	(select id from permissions where name="list"),
	(select id from constraints where name="all-users"));
INSERT INTO rpc VALUES (NULL,
	(select id from resources where name="users"),
	(select id from permissions where name="query"),
	(select id from constraints where name="all-users"));
INSERT INTO rpc VALUES (NULL,
	(select id from resources where name="users"),
	(select id from permissions where name="modify"),
	(select id from constraints where name="all-users"));
INSERT INTO rpc VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="list"),
	(select id from constraints where name="all-users"));
INSERT INTO rpc VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="query"),
	(select id from constraints where name="all-users"));
INSERT INTO rpc VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="modify"),
	(select id from constraints where name="all-users"));
INSERT INTO rpc VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="signal"),
	(select id from constraints where name="all-users"));
	
--  my-site constraint
INSERT INTO rpc VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="list"),
	(select id from constraints where name="my-site"));
INSERT INTO rpc VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="query"),
	(select id from constraints where name="my-site"));
INSERT INTO rpc VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="modify"),
	(select id from constraints where name="my-site"));
INSERT INTO rpc VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="signal"),
	(select id from constraints where name="my-site"));
-- max-bandwidth	
 INSERT INTO rpc VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="create"),
	(select id from constraints where name="max-bandwidth"));
INSERT INTO rpc VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="modify"),
	(select id from constraints where name="max-bandwidth"));
-- max-duration
INSERT INTO rpc VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="create"),
	(select id from constraints where name="max-duration"));
INSERT INTO rpc VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="modify"),
	(select id from constraints where name="max-duration"));	
-- specify-path-elements
INSERT INTO rpc VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="create"),
	(select id from constraints where name="specify-path-elements"));
INSERT INTO rpc VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="modify"),
	(select id from constraints where name="specify-path-elements"));
-- specify-gri
INSERT INTO rpc VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="create"),
	(select id from constraints where name="specify-gri"));
 
-- Fill in description for attributes
ALTER IGNORE TABLE attributes ADD COLUMN description TEXT NOT NULL;

UPDATE  attributes SET description="make reservations" WHERE name="OSCARS-user" ;
UPDATE  attributes SET description="manage all reservations, view and update topology" WHERE name="OSCARS-engineer";
UPDATE  attributes SET description="view all reservations" WHERE name="OSCARS-operator";
UPDATE  attributes SET description="manage all reservations starting or ending at site" WHERE name="OSCARS-siteAdmin";
UPDATE  attributes SET description="make reservations and view topology" WHERE name="OSCARS-service";
UPDATE  attributes SET description="manage all users" WHERE name="OSCARS-administrator";

CREATE UNIQUE INDEX permName on permissions (name(6));

-- Add items to allow for publishing of events to a NotificationBroker
INSERT INTO attributes VALUES(NULL, "OSCARS-publisher", "group",
                        "publish events to external services");
                        
INSERT INTO permissions VALUES(NULL, "publish",
                        "post events or status information",
                        NULL);
                        
INSERT INTO resources VALUES(NULL, "Notifications",
                        "Information about events or status",
                        NULL);
 
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-publisher"),
     (select id from resources where name="Notifications"),
     (select id from permissions where name="publish"),
     NULL,NULL);