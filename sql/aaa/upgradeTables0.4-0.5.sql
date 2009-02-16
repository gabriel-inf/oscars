-- upgrade aaa tables from  0.4.0 0.5

USE aaa;

-- This one got left out in 0.4 releases before 1/3/09 
INSERT IGNORE INTO rpcs VALUES (NULL,
    (select id from resources where name="users"),
    (select id from permissions where name="create"),
    (select id from constraints where name="all-users"));
  
  -- Table to look up an institution associated with a domain (for site admin
-- privileges)

CREATE TABLE IF NOT EXISTS sites (
    id                  INT NOT NULL AUTO_INCREMENT,
        -- topologyId for a domain -- matches topologyIdent in bss domains table
    domainTopologyId                TEXT NOT NULL,
        -- key of corresponding domain in domains table
    institution            INT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

CREATE UNIQUE INDEX row ON sites(domainTopologyId(7),institution); 

INSERT IGNORE INTO sites VALUES ( NULL,"es.net", 
     (select id from institutions where name="Energy Sciences Network"));
INSERT IGNORE INTO sites VALUES ( NULL,"dev.es.net", 
     (select id from institutions where name="Energy Sciences Network"));
INSERT IGNORE INTO sites VALUES (NULL, "dcn.internet2.edu",
      (select id from institutions where name="Internet2"));
INSERT IGNORE INTO sites VALUES (NULL, "bnl.gov",
      (select id from institutions where name="Brookhaven National Laboratory"));
INSERT IGNORE INTO sites VALUES (NULL, "fnal.gov",
      (select id from institutions where name="Fermilab")); 
INSERT IGNORE INTO sites VALUES (NULL, "geant2.net",
      (select id from institutions where name="DANTE"));   
      
   -- Doesn't make sense for a user not to be able to see and modify own profile
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-site-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="query"),
     (select id from constraints where name="none"),NULL);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-site-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="modify"),
     (select id from constraints where name="none"),NULL);