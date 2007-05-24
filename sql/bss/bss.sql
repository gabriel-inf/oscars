CREATE DATABASE IF NOT EXISTS bss;
USE bss;

-- table holding reservation information
-- this information is passed to the PSS
CREATE TABLE IF NOT EXISTS reservations (
    id                  INT NOT NULL AUTO_INCREMENT,
    startTime           BIGINT UNSIGNED NOT NULL,
    endTime             BIGINT UNSIGNED NOT NULL,
        -- time this entry was created
    createdTime         BIGINT UNSIGNED NOT NULL,
        -- bandwidth requested (bps)
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


-- topology section

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
    valid               BOOLEAN NOT NULL,
        -- IP address
    IP                  TEXT NOT NULL,
        -- description (currently loopback, traceAddress, or NULL)
    description         TEXT,
        -- key of corresponding interface in interfaces table
    interfaceId         INT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

-- table for elements in paths associated with pending or active reservations
CREATE TABLE IF NOT EXISTS pathElems (
    id                  INT NOT NULL AUTO_INCREMENT,
        -- whether loose or strict
    loose               BOOLEAN NOT NULL,
        -- currently ingress, egress, or null
    description         TEXT,
        -- what this path is made up of
    ipaddrId            INT NOT NULL,  -- foreign key
        -- next element in path
    nextId              INT,           -- maps back to this table
    PRIMARY KEY (id)
) type=MyISAM;

-- table for paths associated with pending or active reservations
CREATE TABLE IF NOT EXISTS paths (
    id                  INT NOT NULL AUTO_INCREMENT,
        -- whether path was explicitly given by user
    explicit            BOOLEAN NOT NULL,
        -- first element in path
    pathElemId          INT NOT NULL,  -- foreign key
    vlanId              INT,           -- optional foreign key
    nextDomainId        INT,           -- optional foreign key
    PRIMARY KEY (id)
) type=MyISAM;

-- table for administrative domain, e.g. ESnet
CREATE TABLE IF NOT EXISTS domains (
    id                  INT NOT NULL AUTO_INCREMENT,
    name                TEXT NOT NULL,
    url                 TEXT NOT NULL,
    abbrev              TEXT NOT NULL,
    asNum               INT NOT NULL,
        -- used in creating unique reservation tags
        -- whether this is the local domain
    local               BOOLEAN NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

-- Table that associates outside hops with domains
CREATE TABLE IF NOT EXISTS peerIpaddrs (
    id		INT NOT NULL AUTO_INCREMENT,
    domainId	INT NOT NULL,
    ip		TEXT NOT NULL,
    PRIMARY KEY(id)
) type = MyISAM;

-- Table holding associations between vlans and a specific router.
CREATE TABLE IF NOT EXISTS vlans (
    id                  INT NOT NULL AUTO_INCREMENT,
    vlanTag             INT NOT NULL,
    routerId            INT NOT NULL,    -- foreign key
PRIMARY KEY (id)
) type = MyISAM;


-- DRAGON section

-- Table that maps hosts to DRAGON local ID values
CREATE TABLE IF NOT EXISTS dragonLocalIdMap (
    id                  INT NOT NULL AUTO_INCREMENT,
    vlsrIpId            INT NOT NULL,
    ip                  TEXT NOT NULL,
    number              INT NOT NULL,
    type                TEXT NOT NULL,
    PRIMARY KEY (id)
) type = MyISAM;
