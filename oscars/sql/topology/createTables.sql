CREATE DATABASE IF NOT EXISTS topology;
USE topology;

-- table for router description
CREATE TABLE IF NOT EXISTS routers (
    id			INT NOT NULL AUTO_INCREMENT,
    valid		BOOLEAN NOT NULL,
    name		TEXT NOT NULL,
        -- loopback interface IP, if present
    loopback		TEXT,
    traceAddress	TEXT,
    PRIMARY KEY (id)
) type=MyISAM;

-- table for interface description
-- would need trigger updating paths if changed
CREATE TABLE IF NOT EXISTS interfaces (
    id			INT NOT NULL AUTO_INCREMENT,
    valid		BOOLEAN NOT NULL,
        -- SNMP index
    snmpId		INT NOT NULL,
        -- bandwidth in bps
    speed		BIGINT UNSIGNED,
        -- description
    description		TEXT,
    alias		TEXT,
        -- key of corresponding router in routers table
    routerId		INT NOT NULL,	-- foreign key
    PRIMARY KEY (id)
) type=MyISAM;

-- table for router interface ip addresses
CREATE TABLE IF NOT EXISTS ipaddrs (
    id			INT NOT NULL AUTO_INCREMENT,
        -- IP address
    IP			TEXT NOT NULL,
        -- key of corresponding interface in interfaces table
    interfaceId		INT NOT NULL,	-- foreign key
    PRIMARY KEY (id)
) type=MyISAM;


-- table for intradomain paths of reservations
CREATE TABLE IF NOT EXISTS paths (
    pathId			INT NOT NULL AUTO_INCREMENT,
       -- total number of hops, including ingress and egress
    numHops			INT,
    PRIMARY KEY (pathId)
) type=MyISAM;

-- cross reference table
CREATE TABLE IF NOT EXISTS pathInterfaces (
    pathId			INT NOT NULL,	-- foreign key
    interfaceId			INT NOT NULL,	-- foreign key
       -- used to order path
    sequenceNumber              INT NOT NULL,
    PRIMARY KEY (pathId, interfaceId)
) type=MyISAM;


-- Configuration tables ------------------------------------------

-- Configuration for route discovery
CREATE TABLE IF NOT EXISTS configTrace (
    id			INT NOT NULL AUTO_INCREMENT,
      -- traceroute information section
        -- source of traceroutes
    jnxSource		TEXT NOT NULL,
        -- SSH variables to use for Juniper login
    jnxUser		TEXT NOT NULL,
    jnxKey 		TEXT NOT NULL,
        -- traceroute configuration variables
    ttl			INT NOT NULL,
    timeout		INT NOT NULL,
        -- Whether to run traceroute from the routers
        -- (requires ssh keys setup)
    runTrace		BOOLEAN NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

-- Configuration for SNMP queries
CREATE TABLE IF NOT EXISTS configSNMP (
    id			INT NOT NULL AUTO_INCREMENT,
        -- community string (TODO:  secure enough here?
    community		TEXT NOT NULL,
    version		TEXT NOT NULL,
    domainSuffix	TEXT NOT NULL,
    port		INT NOT NULL,
    timeout		INT NOT NULL,
    retries		INT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

-- Configuration for LSP setup and teardown
CREATE TABLE IF NOT EXISTS configPSS (
    id			INT NOT NULL AUTO_INCREMENT,
    access		TEXT NOT NULL,
    login		TEXT NOT NULL,
    password		TEXT NOT NULL,
    firewallMarker	TEXT NOT NULL,
    setupFile		TEXT NOT NULL,
    teardownFile	TEXT NOT NULL,
    externalInterfaceFilter	TEXT NOT NULL,
    CoS			INT NOT NULL,
    dscp		TEXT NOT NULL,
        -- in bps
    burstLimit		INT NOT NULL,
    setupPriority	INT NOT NULL,
    reservationPriority	INT NOT NULL,
        -- allow LSP configuration
    allowLSP		BOOLEAN NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;
