-- Drop and then repopulate the institutions, resources, attributes,
-- userAttributes, permissions, and authorizations tables.  Note that the
-- users table is not handled by this script.  Attributes and authorizations
-- for sample ESnet users are given.

DROP TABLE institutions;
DROP TABLE resources;
DROP TABLE attributes;
DROP TABLE userAttributes;
DROP TABLE permissions;
DROP TABLE authorizations;

-- populate institutions table

CREATE TABLE IF NOT EXISTS institutions (
    id                  INT NOT NULL AUTO_INCREMENT,
    name                TEXT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

INSERT INTO institutions VALUES(NULL, "Energy Sciences Network");
INSERT INTO institutions VALUES(NULL, "Lawrence Berkeley National Laboratory");
INSERT INTO institutions VALUES(NULL, "Internet2");
INSERT INTO institutions VALUES(NULL, "Brookhaven National Laboratory");
INSERT INTO institutions VALUES(NULL, "Fermilab");
INSERT INTO institutions VALUES(NULL, "General Atomics");
INSERT INTO institutions VALUES(NULL, "SLAC");
INSERT INTO institutions VALUES(NULL, "NERSC");
INSERT INTO institutions VALUES(NULL, "DANTE");
INSERT INTO institutions VALUES(NULL, "Oak Ridge National Laboratory");
INSERT INTO institutions VALUES(NULL, "University of Delaware");


-- populate resources table

CREATE TABLE IF NOT EXISTS resources (
    id                  INT NOT NULL AUTO_INCREMENT,
    name                TEXT NOT NULL,
    description         TEXT,
    updateTime          BIGINT,
    PRIMARY KEY (id)
) type=MyISAM;

INSERT INTO resources VALUES(NULL, "Users",
                        "Information about all users", NULL);
INSERT INTO resources VALUES(NULL, "Reservations",
                        "Information about all reservations", NULL);
INSERT INTO resources VALUES(NULL, "Domains",
                        "Information about OSCARS-realm domain controllers",
                        NULL);
INSERT INTO resources VALUES(NULL, "chi-sl-sdn1", "a single router", NULL);

-- populate attributes table

CREATE TABLE IF NOT EXISTS attributes (
    id                  INT NOT NULL AUTO_INCREMENT,
    name                TEXT NOT NULL,
    attrType            TEXT,
    PRIMARY KEY (id)
) type=MyISAM;

INSERT INTO attributes VALUES(NULL, "OSCARS-user", "group");
INSERT INTO attributes VALUES(NULL, "OSCARS-engineer", "group");
INSERT INTO attributes VALUES(NULL, "OSCARS-developer", "group");
INSERT INTO attributes VALUES(NULL, "OSCARS-administrator", "group");
-- INSERT INTO attributes VALUES(NULL, "user-mary ", "user");


-- populate userAttributes table

CREATE TABLE IF NOT EXISTS userAttributes (
    id                  INT NOT NULL AUTO_INCREMENT,
    userId              INT NOT NULL,    -- foreign key
    attributeId         INT NOT NULL,    -- foreign key
    PRIMARY KEY (id)
) type=MyISAM;

INSERT INTO userAttributes VALUES(NULL,
        (select id from users where login = "mrthompson@lbl.gov"), 
        (select id from attributes where name="OSCARS-user"));
INSERT INTO userAttributes VALUES(NULL,
        (select id from users where login = "mrthompson@lbl.gov"), 
        (select id from attributes where name="OSCARS-administrator"));
-- INSERT INTO userAttributes VALUES(NULL,
        -- (select id from users where login = "mrthompson@lbl.gov"), 
        -- (select id from attributes where name="user-mary"));
INSERT INTO userAttributes VALUES(NULL, 
        (select id from users where login = "dwrobertson@lbl.gov"), 
        (select id from attributes where name="OSCARS-engineer"));
INSERT INTO userAttributes VALUES(NULL,
        (select id from users where login = "dwrobertson@lbl.gov"), 
        (select id from attributes where name="OSCARS-administrator"));
INSERT INTO userAttributes VALUES(NULL,
        (select id from users where login = "chin@es.net"), 
        (select id from attributes where name="OSCARS-engineer"));
INSERT INTO userAttributes VALUES(NULL,
        (select id from users where login = "chin@es.net"), 
        (select id from attributes where name="OSCARS-administrator"));

        
-- populate permissions table

CREATE TABLE IF NOT EXISTS permissions (
    id                  INT NOT NULL AUTO_INCREMENT,
    name                TEXT NOT NULL,
    description         TEXT,
    updateTime          BIGINT,
    PRIMARY KEY (id)
) type=MyISAM;

INSERT INTO permissions VALUES(NULL, "list",
            "view minimum infomation about a user or reservation", NULL);
INSERT INTO permissions VALUES(NULL, "query",
            "view complete infomation about a user or reservation", NULL);
INSERT INTO permissions VALUES(NULL, "modify",
            "change or delete a user or reservation", NULL);
INSERT INTO permissions VALUES(NULL, "create",
            "create a user or reservation", NULL);


-- populate authorizations table
        
CREATE TABLE IF NOT EXISTS authorizations (
    id                  INT NOT NULL AUTO_INCREMENT,
    context             TEXT,
    updateTime          BIGINT,
    attrId              INT NOT NULL,    -- foreign key
    resourceId          INT NOT NULL,    -- foreign key
    permissionId        INT NOT NULL,    -- foreign key
    constraintName      TEXT,
    constraintValue     INT,
    PRIMARY KEY (id)
) type=MyISAM;

-- generic user authorizations
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="users"),
     (select id from permissions where name="query"),
    "all-users", 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="users"),
     (select id from permissions where name="modify"),
    "all-users", 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="list"),
     "all-users", 0);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="query"),
     "all-users", 0);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="modify"),
     "all-users", 0);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="create"),
     "max-bandwidth", 1000);   
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="create"),
     "max-duration", 60);    
-- super-user authorizations for BSS operations
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="list"),
     "all-users", 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="query"),
     "all-users", 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="modify"),
     "all-users", 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="create"),
     "specify-path-elements", 1);   
-- developer authorizations add the ability to list and
-- query all reservations
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-developer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="list"),
     "all-users", 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-developer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="query"),
     "all-users", 1);
-- super-user authorizations for AAA operations
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="list"),
     "all-users", 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="query"),
     "all-users", 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="create"),
     "all-users", 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="modify"),
     "all-users", 1);
-- INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     -- (select id from attributes where name="user-mary"),
     -- (select id from resources where name="reservations"),
     -- (select id from permissions where name="create"),
     -- "specify-path-elements", 1);
