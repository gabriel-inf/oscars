CREATE DATABASE IF NOT EXISTS bss;
USE bss;

-- table for router description
CREATE TABLE IF NOT EXISTS routers (
    id                  INT NOT NULL AUTO_INCREMENT,
    valid               BOOLEAN NOT NULL,
    name                TEXT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

-- table for interface description
-- would need trigger updating paths if changed
CREATE TABLE IF NOT EXISTS interfaces (
    id                  INT NOT NULL AUTO_INCREMENT,
    valid               BOOLEAN NOT NULL,
        -- SNMP index
    snmpId              INT NOT NULL,
        -- bandwidth in bps
    speed               BIGINT UNSIGNED,
        -- description
    description         TEXT,
    alias               TEXT,
        -- key of corresponding router in routers table
    routerId            INT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

-- table for router interface ip addresses
CREATE TABLE IF NOT EXISTS ipaddrs (
    id                  INT NOT NULL AUTO_INCREMENT,
        -- IP address
    IP                  TEXT NOT NULL,
        -- description (currently loopback, traceAddress, or NULL)
    description         TEXT,
        -- key of corresponding interface in interfaces table
    interfaceId         INT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

-- table for path of IP addresses
CREATE TABLE IF NOT EXISTS paths (
    id                  INT NOT NULL AUTO_INCREMENT,
        -- currently ingress, egress, or null
    addressType         TEXT,
        -- next element in path
    nextId              INT,           -- maps back to this table
        -- what this path is made up of
    ipaddrId            INT NOT NULL,   -- foreign key
    PRIMARY KEY (id)
) type=MyISAM;


-- Reservations tables -------------------------------------------

-- table for administrative domain, e.g. ESnet
CREATE TABLE IF NOT EXISTS domains (
    id                  INT NOT NULL AUTO_INCREMENT,
    as_num              INT NOT NULL,
    name                TEXT NOT NULL,
        -- used in creating unique reservation tags
    abbrev              TEXT NOT NULL,
        -- whether this is the local domain
    local               BOOLEAN NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

-- table holding reservation information
-- this information is passed to the PSS
CREATE TABLE IF NOT EXISTS reservations (
    id                  INT NOT NULL AUTO_INCREMENT,
    startTime           BIGINT NOT NULL,
    endTime             BIGINT NOT NULL,
        -- time this entry was created
    createdTime         BIGINT NOT NULL,
        -- bandwidth requested (Mbps)
    bandwidth           BIGINT UNSIGNED NOT NULL,
        -- in bps
    burstLimit          BIGINT UNSIGNED NOT NULL,
        -- user making the reservation
    login               TEXT NOT NULL,
        -- pending, active, failed, precancel, or cancelled
    status              TEXT NOT NULL,
    lspClass            TEXT NOT NULL,
    srcHost             TEXT NOT NULL, 
    destHost            TEXT NOT NULL,
      -- the following are optional fields
        -- source and destination ports
    srcPort             SMALLINT UNSIGNED,
    destPort            SMALLINT UNSIGNED,
        -- differentiated services code point
    dscp                TEXT,
        -- protocol used (0-255, or a protocol string, such as udp)
    protocol            TEXT,
    description         TEXT,
    pathId              INT NOT NULL,   -- foreign key
    PRIMARY KEY (id)
) type = MyISAM;


-- Table holding archived reservation information.  Foreign keys are replaced
-- by what they reference, since router information may change over time.
CREATE TABLE IF NOT EXISTS archivedReservations (
    -- Note that this is the id of the row in the original reservations
    -- table.
    id                  INT NOT NULL,
    startTime           BIGINT NOT NULL,
    endTime             BIGINT NOT NULL,
        -- time this entry was created
    createdTime         BIGINT NOT NULL,
        -- bandwidth requested (Mbps)
    bandwidth           BIGINT UNSIGNED NOT NULL,
        -- in Mbps
    burstLimit          BIGINT UNSIGNED NOT NULL,
        -- user making the reservation
    login               TEXT NOT NULL,
        -- pending, active, failed, precancel, or cancelled
    status              TEXT NOT NULL,
    lspClass            TEXT NOT NULL,
    srcHost             TEXT NOT NULL, 
    destHost            TEXT NOT NULL,
      -- the following are optional fields
        -- source and destination ports
    srcPort             SMALLINT UNSIGNED,
    destPort            SMALLINT UNSIGNED,
        -- differentiated services code point
    dscp                TEXT,
        -- protocol used (0-255, or a protocol string, such as udp)
    protocol            TEXT,
    description         TEXT,
        -- was a foreign key before archiving
        -- space separated list of names or addresses of routers in path
    path                TEXT,
    PRIMARY KEY (id)
) type = MyISAM;
