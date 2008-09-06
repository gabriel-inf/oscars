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
