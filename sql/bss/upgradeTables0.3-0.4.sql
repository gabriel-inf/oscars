-- update bss tables from release 0.2 to 0.3
-- add sites table

USE bss;

--
-- Table to lookup external servcies associated with a domain
-- Used as a backup to the perfSONAR LS
CREATE TABLE IF NOT EXISTS domainServices (
    id                  INT NOT NULL AUTO_INCREMENT,
    domainId            INT NOT NULL,
    type                TEXT NOT NULL,
    url                 TEXT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;


ALTER TABLE reservations ADD localStatus TINYINT(1) DEFAULT 0 AFTER status;