-- upgrade aaa tables from 3.2 to 4.0

USE aaa;

-- ADD TABLES
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

-- CHANGES TO TABLES

-- RESOURCES TABLE
-- Add a resource to control modification of attributes, authorizations and institutions
INSERT INTO resources VALUES(NULL, "AAA",
                        "Information about Institutions, Attributes and Authorizations",
                        NULL);
-- Add resource to control publishing
INSERT INTO resources VALUES(NULL, "notifications",
                        "Information an entity wishes to communicate to another entity",
                        NULL);
INSERT INTO resources VALUES(NULL, "subscriptions",
                        "Information about the relationship between the producer and consumer of notifications",
                        NULL);
INSERT INTO resources VALUES(NULL, "publisherRegistrations",
                        "Information about the relationship between a Publisher and a NotificationBroker",
                        NULL);
                        
 CREATE UNIQUE INDEX resourceName ON resources(name(6));
 
-- Create resource, permission, constraint (RPC) table which contains a list of the meaningful RPC tuples
CREATE TABLE IF NOT EXISTS rpcs (
   id              		INT NOT NULL AUTO_INCREMENT,
   resourceId			INT NOT NULL,
   permissionId			INT NOT NULL,
   constraintId			INT NOT NULL,
   PRIMARY KEY (id)
) type=MyISAM;
CREATE UNIQUE INDEX const ON rpcs(resourceId,permissionId,constraintId); 

-- all-users constraint
INSERT INTO rpcs VALUES (NULL,
	(select id from resources where name="users"),
	(select id from permissions where name="list"),
	(select id from constraints where name="all-users"));
INSERT INTO rpcs VALUES (NULL,
	(select id from resources where name="users"),
	(select id from permissions where name="query"),
	(select id from constraints where name="all-users"));
INSERT INTO rpcs VALUES (NULL,
	(select id from resources where name="users"),
	(select id from permissions where name="modify"),
	(select id from constraints where name="all-users"));
INSERT INTO rpcs VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="list"),
	(select id from constraints where name="all-users"));
INSERT INTO rpcs VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="query"),
	(select id from constraints where name="all-users"));
INSERT INTO rpcs VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="modify"),
	(select id from constraints where name="all-users"));
INSERT INTO rpcs VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="signal"),
	(select id from constraints where name="all-users"));
	
--  my-site constraint
INSERT INTO rpcs VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="list"),
	(select id from constraints where name="my-site"));
INSERT INTO rpcs VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="query"),
	(select id from constraints where name="my-site"));
INSERT INTO rpcs VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="modify"),
	(select id from constraints where name="my-site"));
INSERT INTO rpcs VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="signal"),
	(select id from constraints where name="my-site"));
INSERT INTO rpcs VALUES (NULL,
	(select id from resources where name="notifications"),
	(select id from permissions where name="query"),
	(select id from constraints where name="my-site"));
	
-- max-bandwidth	
 INSERT INTO rpcs VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="create"),
	(select id from constraints where name="max-bandwidth"));
INSERT INTO rpcs VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="modify"),
	(select id from constraints where name="max-bandwidth"));
-- max-duration
INSERT INTO rpcs VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="create"),
	(select id from constraints where name="max-duration"));
INSERT INTO rpcs VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="modify"),
	(select id from constraints where name="max-duration"));	
-- specify-path-elements
INSERT INTO rpcs VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="create"),
	(select id from constraints where name="specify-path-elements"));
INSERT INTO rpcs VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="modify"),
	(select id from constraints where name="specify-path-elements"));
-- specify-gri
INSERT INTO rpcs VALUES (NULL,
	(select id from resources where name="reservations"),
	(select id from permissions where name="create"),
	(select id from constraints where name="specify-gri"));
	
-- DROP TABLE
-- resourcePermissions has been replaced by resource-permission-constraint table
DROP TABLE resourcePermissions;
	


-- PERMISSIONS TABLE                  
CREATE UNIQUE INDEX permName on permissions (name(6));

-- ATTRIBUTES TABLE
	                        
-- Add description description for attributes
ALTER IGNORE TABLE attributes ADD COLUMN description TEXT NOT NULL;

UPDATE  attributes SET description="make reservations" WHERE name="OSCARS-user" ;
UPDATE  attributes SET description="manage all reservations, view and update topology" WHERE name="OSCARS-engineer";
UPDATE  attributes SET description="view all reservations" WHERE name="OSCARS-operator";
UPDATE  attributes SET description="manage all reservations starting or ending at site" WHERE name="OSCARS-siteAdmin";
UPDATE  attributes SET description="make reservations and view topology" WHERE name="OSCARS-service";
UPDATE  attributes SET description="manage all users" WHERE name="OSCARS-administrator";

-- Add items to allow for publishing of events to a NotificationBroker
INSERT INTO attributes VALUES(NULL, "OSCARS-publisher", "group",
                        "publish events to external services");

 -- add attribute to specify path elements on create and modify reservation
 INSERT INTO attributes VALUES (NULL, "OSCARS-may-specify-path", "privilege",
 						"an add-on attribute to allow specification of path elements");
 
-- USERATTRIBUTES TABLE					
-- add a unique index to userAttributes table
CREATE UNIQUE INDEX userAttr ON userAttributes(userId,attributeId);

-- ATUHORIZATIONS TABLE

-- convert constraintName to constraintId in authorizations table
-- the current text values are translated to numeric indexes which ALTER table will preserve

UPDATE authorizations SET constraintName=(select id from constraints where  name="my-site") where constraintName="my-site";
UPDATE authorizations SET constraintName=(select id from constraints where  name="all-users") where constraintName="all-users";
UPDATE authorizations SET constraintName=(select id from constraints where  name="specify-path-elements") where constraintName="specify-path-elements";
UPDATE authorizations SET constraintName=(select id from constraints where  name="specify-gri") where constraintName="specify-gri";
UPDATE authorizations SET constraintName=(select id from constraints where  name="max-bandwidth") where constraintName="max-bandwidth";
UPDATE authorizations SET constraintName=(select id from constraints where  name="max-duration") where constraintName="max-duration";

ALTER TABLE authorizations CHANGE COLUMN constraintName constraintId int;

INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-administrator"),
     (select id from resources where name="AAA"),
     (select id from permissions where name="list"),
     NULL,NULL); 
        
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-administrator"),
     (select id from resources where name="AAA"),
     (select id from permissions where name="modify"),
     NULL,NULL);
    
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-publisher"),
     (select id from resources where name="PublisherRegistrations"),
     (select id from permissions where name="create"),
     NULL,NULL);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-publisher"),
     (select id from resources where name="PublisherRegistrations"),
     (select id from permissions where name="modify"),
     NULL,NULL);
     
 INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="notifications"),
     (select id from permissions where name="query"),
     NULL, NULL); 
     
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="subscriptions"),
     (select id from permissions where name="create"),
     NULL, NULL); 
     
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="subscriptions"),
     (select id from permissions where name="modify"),
     NULL, NULL);
     
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="notifications"),
     (select id from permissions where name="query"),
     (select id from constraints where name="all-users"), 1);
     
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="subscriptions"),
     (select id from permissions where name="create"),
     NULL, NULL); 
     
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="subscriptions"),
     (select id from permissions where name="modify"),
     NULL, NULL);
     
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-administrator"),
     (select id from resources where name="Subscriptions"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="all-users"), 1);
     
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-administrator"),
     (select id from resources where name="PublisherRegistrations"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="all-users"), 1);
     
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="notifications"),
     (select id from permissions where name="query"),
     NULL, NULL); 
     
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="subscriptions"),
     (select id from permissions where name="create"),
     NULL, NULL); 
     
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="subscriptions"),
     (select id from permissions where name="modify"),
     NULL, NULL);
     
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-operator"),
     (select id from resources where name="notifications"),
     (select id from permissions where name="query"),
     (select id from constraints where name="all-users"), NULL); 
     
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-operator"),
     (select id from resources where name="subscriptions"),
     (select id from permissions where name="create"),
     NULL, NULL); 
     
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-operator"),
     (select id from resources where name="subscriptions"),
     (select id from permissions where name="modify"),
     NULL, NULL);
     
 INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-siteAdmin"),
     (select id from resources where name="notifications"),
     (select id from permissions where name="query"),
     (select id from constraints where name="my-site"),1);
     
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-siteAdmin"),
     (select id from resources where name="subscriptions"),
     (select id from permissions where name="create"),
     NULL, NULL);
     
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-siteAdmin"),
     (select id from resources where name="subscriptions"),
     (select id from permissions where name="modify"),
     NULL, NULL);
     
 -- OSCARS-may-specify-path
 INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-may-specify-path"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="specify-path-elements"), 1);  
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-may-specify-path"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="create"),
     (select id from constraints where name="specify-path-elements"), 1);  
  