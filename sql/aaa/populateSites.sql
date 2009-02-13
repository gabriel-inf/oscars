USE aaa;

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
