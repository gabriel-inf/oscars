

-- Database and tables associated with AAA component, associated with users
-- and authentication.

CREATE DATABASE IF NOT EXISTS aaa;
USE aaa;

-- create empty users table 
-- use the tools/utils/idc-useradd script to add a first administrative user
-- after that use the WBUI

CREATE TABLE IF NOT EXISTS users (
    id                  INT NOT NULL AUTO_INCREMENT,
    login               TEXT NOT NULL,
    certIssuer          TEXT,
    certSubject         TEXT,
    lastName            TEXT NOT NULL,
    firstName           TEXT NOT NULL,
    emailPrimary        TEXT NOT NULL,
    phonePrimary        TEXT NOT NULL,
    password            TEXT,
    description         TEXT,
    emailSecondary      TEXT,
    phoneSecondary      TEXT,
    status              TEXT,
    activationKey       TEXT,
    loginTime           BIGINT,
    cookieHash          TEXT,
    institutionId       INT NOT NULL,    -- foreign key (when convert to InnoDB)
    PRIMARY KEY (id)
) type=MyISAM;

-- populate institutions table     

CREATE TABLE IF NOT EXISTS institutions (
    id                  INT NOT NULL AUTO_INCREMENT,
    name                TEXT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

CREATE UNIQUE INDEX instName ON institutions(name(9));
INSERT INTO institutions VALUES(1, "Energy Sciences Network");
INSERT INTO institutions VALUES(2, "Internet2");

-- populate resources table

CREATE TABLE IF NOT EXISTS resources (
    id                  INT NOT NULL AUTO_INCREMENT,
    name                TEXT NOT NULL,
    description         TEXT,
    updateTime          BIGINT,
    PRIMARY KEY (id)
) type=MyISAM;

CREATE UNIQUE INDEX resourceName ON resources(name(6));
INSERT INTO resources VALUES(NULL, "Users",
                        "Information about all users", NULL);
INSERT INTO resources VALUES(NULL, "Reservations",
                        "Information about all reservations", NULL);
INSERT INTO resources VALUES(NULL, "Domains",
                        "Information about OSCARS-realm domain controllers",
                        NULL);
INSERT INTO resources VALUES(NULL, "AAA",
                        "Information about Institutions, Attributes and Authorizations",
                        NULL);
INSERT INTO resources VALUES(NULL, "Notifications",
                        "Information an entity wishes to communicate to another entity",
                        NULL);
INSERT INTO resources VALUES(NULL, "Subscriptions",
                        "Information about the relationship between the producer and consumer of notifications",
                        NULL);
INSERT INTO resources VALUES(NULL, "PublisherRegistrations",
                        "Information about the relationship between a Publisher and a NotificationBroker",
                        NULL);
                        
-- populate attributes table

CREATE TABLE IF NOT EXISTS attributes (
    id                  INT NOT NULL AUTO_INCREMENT,
    name                TEXT NOT NULL,
    attrType            TEXT,
    description			TEXT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

CREATE UNIQUE INDEX attrName ON attributes(name(9));
-- ordinary OSCARS user
INSERT INTO attributes VALUES(NULL, "OSCARS-user", "group" , "make reservations");

-- member of the  network engineering group. Has complete control over
-- all reservations
INSERT INTO attributes VALUES(NULL, "OSCARS-engineer", "group", "manage all reservations, view and update topology");

-- Has complete control over all user accounts, including granting permissions
INSERT INTO attributes VALUES(NULL, "OSCARS-administrator", "group", "manage all users");

-- attribute for an IDC in an adjacent network domain. It's attributes implement
-- an SLA between domains.  Currently set to all permissions on reservations and 
-- query permissions for domains, no permissions on users
INSERT INTO attributes VALUES(NULL, "OSCARS-service", "group", "make reservations and view topology");

-- for use by NOC operators. Can see all reservations.
INSERT INTO attributes VALUES(NULL, "OSCARS-operator", "group", "view all reservations");

-- Site Administrator - Can manage all reservations starting or terminating at a site
INSERT INTO attributes VALUES(NULL, "OSCARS-siteAdmin", "group", "manage all reservations starting or ending at site");

-- Publisher - for use by IDCs and other services that want to publish notifications
INSERT INTO attributes VALUES(NULL, "OSCARS-publisher", "group",
                        "publish events to external services");
                        
-- OSCARS-may-specify-path - an attribute that can be given to any user in addition to
--    a normal OSCARS-user role that allows specification of path elements on create reservation
INSERT INTO attributes VALUES (NULL, "OSCARS-may-specify-path", "privilege",
 						"an add-on attribute to allow specification of path elements");
 						                        

CREATE TABLE IF NOT EXISTS userAttributes (
    id                  INT NOT NULL AUTO_INCREMENT,
    userId              INT NOT NULL,    -- foreign key
    attributeId         INT NOT NULL,    -- foreign key
    PRIMARY KEY (id)
) type=MyISAM;
CREATE UNIQUE INDEX userAttr ON userAttributes(userId,attributeId);    
 -- populate userAttributes table by selecting attributes in tool/utils/idc-adduser
       
-- populate permissions table

CREATE TABLE IF NOT EXISTS permissions (
    id                  INT NOT NULL AUTO_INCREMENT,
    name                TEXT NOT NULL,
    description         TEXT,
    updateTime          BIGINT,
    PRIMARY KEY (id)
) type=MyISAM;
CREATE UNIQUE INDEX permName on permissions (name(6));

INSERT INTO permissions VALUES(NULL, "list",
            "view minimum information about a user or reservation", NULL);
INSERT INTO permissions VALUES(NULL, "query",
            "view complete information about a user or reservation", NULL);
INSERT INTO permissions VALUES(NULL, "modify",
            "change or delete a user or reservation", NULL);
INSERT INTO permissions VALUES(NULL, "create",
            "create a user or reservation", NULL);
INSERT INTO permissions VALUES(NULL, "signal",
            "signal a previously placed reservation", NULL);
            
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

-- Create resource, permission, constraint (rpcs) table which contains a list of the meaningful RPC tuples
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
INSERT INTO rpcs VALUES (NULL,
	(select id from resources where name="notifications"),
	(select id from permissions where name="query"),
	(select id from constraints where name="all-users"));
INSERT INTO rpcs VALUES (NULL,
	(select id from resources where name="subscriptions"),
	(select id from permissions where name="modify"),
	(select id from constraints where name="all-users"));
INSERT INTO rpcs VALUES (NULL,
	(select id from resources where name="publisherregistrations"),
	(select id from permissions where name="modify"),
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
 
-- populate authorizations table
        
CREATE TABLE IF NOT EXISTS authorizations (
    id                  INT NOT NULL AUTO_INCREMENT,
    context             TEXT,
    updateTime          BIGINT,
    attrId              INT NOT NULL,    -- foreign key
    resourceId          INT NOT NULL,    -- foreign key
    permissionId        INT NOT NULL,    -- foreign key
    constraintId        INT,             -- foreign key
    constraintValue     INT,
    PRIMARY KEY (id)
) type=MyISAM;
-- allows duplicate rows if constraint is null
CREATE UNIQUE INDEX row ON authorizations (attrId,resourceId,permissionId,constraintId);

-- authorizations for standard attributes

-- authorizations for OSCARS-user
-- query and modify own profile
-- list, query, modify, create and signal own reservations
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="users"),
     (select id from permissions where name="query"),
     NULL, NULL);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="users"),
     (select id from permissions where name="modify"),
     NULL, NULL);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="list"),
     NULL, NULL);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="query"),
     NULL, NULL);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="modify"),
     NULL, NULL);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="create"),
     NULL, NULL);   
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="signal"),
     NULL, NULL);
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
     
-- authorizations for OSCARS-engineer
-- query and modify own profile
-- list,query,modify, create and signal all reservations
-- when creating or modifying reservations may set path elements
-- query and modify topology information
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="users"),
     (select id from permissions where name="query"),
     NULL, NULL);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="users"),
     (select id from permissions where name="modify"),
     NULL, NULL);
-- super-user authorizations for BSS operations
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="list"),
     (select id from constraints where name="all-users"), 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="query"),
     (select id from constraints where name="all-users"), 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="all-users"), 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="specify-path-elements"), 1);  
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="create"),
     (select id from constraints where name="specify-path-elements"), 1);  
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="signal"),
     (select id from constraints where name="all-users"), 1); 
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="domains"),
     (select id from permissions where name="query"),
     NULL, NULL);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="domains"),
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
     
--  Authorizations for OSCARS-administrator
-- list, query, create and modify all user information
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="list"),
     (select id from constraints where name="all-users"), 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="query"),
     (select id from constraints where name="all-users"), 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="create"),
     NULL,NULL);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="all-users"), 1);
 
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
     (select id from attributes where name="OSCARS-administrator"),
     (select id from resources where name="Subscriptions"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="all-users"), 1);

INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-administrator"),
     (select id from resources where name="PublisherRegistrations"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="all-users"), 1);
     
-- authorizations for an IDC forwarding a request to an adjacent domain 
-- note that all the reservations fowarded by a IDC are owned by the IDC
-- Query, modify, list, signal reservations that it owns
-- Create reservations specifying GRI and path elements
-- Fetch topology, modify local topology
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
     (select id from constraints where name="specify-path-elements"), 1); 
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="create"),
     (select id from constraints where name="specify-gri"), 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="specify-path-elements"), 1);  
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
     
-- NOC operators
-- List and query all reservations
-- List all users
-- See and modify own profile
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-operator"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="list"),
     (select id from constraints where name="all-users"), 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-operator"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="query"),
     (select id from constraints where name="all-users"), 1); 
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-operator"),
     (select id from resources where name="users"),
     (select id from permissions where name="list"),
     (select id from constraints where name="all-users"), 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-operator"),
     (select id from resources where name="users"),
     (select id from permissions where name="query"),
     NULL, NULL);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-operator"),
     (select id from resources where name="users"),
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
     
    -- Site Administrator
    -- List, query, modify, create and signal any reservation
    --   that starts or terminates at his site
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-siteAdmin"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="query"),
     (select id from constraints where name="my-site"),1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-siteAdmin"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="my-site"),1); 
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-siteAdmin"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="list"),
     (select id from constraints where name="my-site"),1); 
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-siteAdmin"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="create"),
     (select id from constraints where name="my-site"),1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-siteAdmin"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="signal"),
     (select id from constraints where name="my-site"),1);
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

    -- Publisher
    -- Publish notifications    
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