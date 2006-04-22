CREATE DATABASE IF NOT EXISTS Intradomain;
USE Intradomain;

-- Table for default clients.
CREATE TABLE IF NOT EXISTS clients (
    id			INT NOT NULL AUTO_INCREMENT,
    uri			TEXT NOT NULL,
    proxy		TEXT NOT NULL,
    asNum		TEXT,   -- autonomous system number
    PRIMARY KEY (id)
) type=MyISAM;

-- table for router description
CREATE TABLE IF NOT EXISTS routers (
    id			INT NOT NULL AUTO_INCREMENT,
    valid		BOOLEAN NOT NULL,
    name		TEXT NOT NULL,
        -- loopback interface IP, if present
    loopback		TEXT,
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
    descr		TEXT,
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

-- table for source and destination IP addresses
CREATE TABLE IF NOT EXISTS hosts (
    id			INT NOT NULL AUTO_INCREMENT,
    IP			TEXT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

-- table holding reservation information
-- this information is passed to the PSS
CREATE TABLE IF NOT EXISTS reservations (
    id			INT NOT NULL AUTO_INCREMENT,
      -- times will be in seconds since epoch (UTC)
    startTime		DATETIME NOT NULL,
    endTime		DATETIME NOT NULL,
        -- time this entry was created
    createdTime		DATETIME NOT NULL,
        -- client's time zone
    origTimeZone	TEXT NOT NULL,
        -- bandwidth requested (Mbps)
    bandwidth		BIGINT UNSIGNED NOT NULL,
        -- in Mbps
    burstLimit		BIGINT UNSIGNED NOT NULL,
        -- user making the reservation
    user_login		TEXT NOT NULL,
        -- pending, active, failed, precancel, or cancelled
    status		TEXT NOT NULL,
    class		TEXT NOT NULL,
      -- the following are optional fields
        -- source and destination ports
    srcPort		SMALLINT UNSIGNED,
    destPort		SMALLINT UNSIGNED,
        -- differentiated services code point
    dscp		TEXT,
        -- protocol used (0-255, or a protocol string, such as udp)
    protocol		TEXT,
        -- human readable identifier
    tag			TEXT,
        -- space separated list of addresses of routers in path
        -- This will be normalized by adding a paths table
    path		TEXT,
    description		TEXT,
      -- foreign keys (not optional)
        -- keys of ingress and egress interfaces in interfaces table
    ingressInterfaceId	INT NOT NULL,	-- foreign key
    egressInterfaceId	INT NOT NULL,	-- foreign key
        -- keys of source and destination addresses in hosts table
    srcHostId		INT NOT NULL,   -- foreign key 
    destHostId		INT NOT NULL,	-- foreign key 
    PRIMARY KEY (id)
) type = MyISAM;


-- Following three tables are for configuration variables that can exist on a per
-- reservation basis (one row is for the defaults).  The defaults are used
-- if the foreign key is null in the reservation.  The defaults can
-- only be overriden by a user with engineer privileges.

-- Configuration for scheduler
CREATE TABLE IF NOT EXISTS schedulerConfs (
    id			INT NOT NULL AUTO_INCREMENT,
        -- Time (in seconds) between polling the reservation db
    pollInterval	INT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

-- Configuration for route discovery
CREATE TABLE IF NOT EXISTS traceConfs (
    id			INT NOT NULL AUTO_INCREMENT,
      -- traceroute information section
        -- source of traceroutes
    jnx_source		TEXT NOT NULL,
        -- SSH variables to use for Juniper login
    jnx_user		TEXT NOT NULL,
    jnx_key 		TEXT NOT NULL,
        -- traceroute configuration variables
    ttl			INT NOT NULL,
    timeout		INT NOT NULL,
        -- Whether to run traceroute from the routers
        -- (requires ssh keys setup)
    run_trace		BOOLEAN NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

-- Configuration for SNMP queries
CREATE TABLE IF NOT EXISTS snmpConfs (
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
CREATE TABLE IF NOT EXISTS pssConfs (
    id			INT NOT NULL AUTO_INCREMENT,
        -- Router access
    access		TEXT NOT NULL,
    login		TEXT NOT NULL,
    passwd		TEXT NOT NULL,
        -- XML related variables.
    firewall_marker	TEXT NOT NULL,
    setup_file		TEXT NOT NULL,
    teardown_file	TEXT NOT NULL,
    ext_if_filter	TEXT NOT NULL,
        -- LSP values.
    CoS			INT NOT NULL,
        -- in bps
    burstLimit		INT NOT NULL,
    setup_priority	INT NOT NULL,
    resv_priority	INT NOT NULL,
        -- allow LSP configuration
    allow_lsp		BOOLEAN NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;


-- Table holding archived reservation information.  Foreign keys are replaced
-- by what they reference, since router information may change over time.
CREATE TABLE IF NOT EXISTS archivedReservations (
    -- Note that this is the id of the row in the original reservations
    -- table.
    id			INT NOT NULL,
      -- times will be in seconds since epoch (UTC)
    startTime		DATETIME NOT NULL,
    endTime		DATETIME NOT NULL,
        -- time this entry was created
    createdTime		DATETIME NOT NULL,
        -- client's time zone
    origTimeZone	TEXT NOT NULL,
        -- bandwidth requested (Mbps)
    bandwidth		BIGINT UNSIGNED NOT NULL,
        -- in Mbps
    burstLimit		BIGINT UNSIGNED NOT NULL,
        -- user making the reservation
    user_dn		TEXT NOT NULL,
        -- pending, active, failed, precancel, or cancelled
    status		TEXT NOT NULL,
    class		TEXT NOT NULL,
      -- the following are optional fields
        -- source and destination ports
    srcPort		SMALLINT UNSIGNED,
    destPort		SMALLINT UNSIGNED,
        -- differentiated services code point
    dscp		TEXT,
        -- protocol used (0-255, or a protocol string, such as udp)
    protocol		TEXT,
        -- human readable identifier
    tag			TEXT,
        -- space separated list of addresses of routers in path
    path		TEXT,
    description		TEXT,
    ingressRouter	TEXT NOT NULL,
    egressRouter	TEXT NOT NULL,
    srcHost		TEXT NOT NULL, 
    destHost		TEXT NOT NULL,
    PRIMARY KEY (id)
) type = MyISAM;


-- Test configuration.
CREATE TABLE IF NOT EXISTS testConfs (
    id			INT NOT NULL AUTO_INCREMENT,
    testName		TEXT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;


-- Test address.
CREATE TABLE IF NOT EXISTS testAddresses (
    id			INT NOT NULL AUTO_INCREMENT,
    address		TEXT NOT NULL,
    description		TEXT,
    testConfId		INT,	-- foreign key
    PRIMARY KEY (id)
) type=MyISAM;

