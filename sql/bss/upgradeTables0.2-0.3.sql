-- update bss tables from release 0.2 to 0.3
-- add sites table

USE bss;

--
-- Table to look up an institution associated with a domain (for site admin
-- privileges)
CREATE TABLE IF NOT EXISTS sites (
    id                  INT NOT NULL AUTO_INCREMENT,
        -- site name, matches a name in the aaa institutions table
    name                TEXT NOT NULL,
        -- key of corresponding domain in domains table
    domainId            INT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;


INSERT IGNORE INTO sites VALUES ( 1,"Energy Sciences Network", 
     (select id from domains where topologyIdent="es.net"));
INSERT IGNORE INTO sites VALUES (2, "Internet2",
      (select id from domains where topologyIdent="dcn.internet2.edu"));
INSERT IGNORE INTO sites VALUES (3, "Brookhaven National Laboratory",
      (select id from domains where topologyIdent="bnl.gov"));
INSERT IGNORE INTO sites VALUES (4, "Fermilab",
      (select id from domains where topologyIdent="fnal.gov")); 
INSERT IGNORE INTO sites VALUES (5, "DANTE",
      (select id from domains where topologyIdent="geant2.net")); 

