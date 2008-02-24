CREATE DATABASE IF NOT EXISTS bss;
USE bss;

-- table holding reservation information
-- this information is used for scheduling
CREATE TABLE IF NOT EXISTS reservations (
    id                  INT NOT NULL AUTO_INCREMENT,
    startTime           BIGINT UNSIGNED NOT NULL,
    endTime             BIGINT UNSIGNED NOT NULL,
        -- time this entry was created
    createdTime         BIGINT UNSIGNED NOT NULL,
        -- bandwidth requested (bps)
    bandwidth           BIGINT UNSIGNED NOT NULL,
        -- user making the reservation
    login               TEXT NOT NULL,
        -- pending, active, failed, precancel, or cancelled
    status              TEXT NOT NULL,
    description         TEXT,
    globalReservationId VARCHAR(63) UNIQUE,
    pathId              INT NOT NULL UNIQUE,   -- foreign key
PRIMARY KEY (id)
) type = MyISAM;

-- table used as a sequence generator for part of the GRI
CREATE TABLE IF NOT EXISTS idSequence (
    id                  INT NOT NULL AUTO_INCREMENT,
PRIMARY KEY (id)
) type = MyISAM;

-- topology section

-- table for administrative domain, e.g. ESnet
CREATE TABLE IF NOT EXISTS domains (
    id                  INT NOT NULL AUTO_INCREMENT,
        -- topology exchange id; in ESnet, currently AS number
    topologyIdent       TEXT NOT NULL,
    name                TEXT NOT NULL,
    url                 TEXT NOT NULL,
    abbrev              TEXT NOT NULL,
        -- used in creating unique reservation tags
        -- whether this is the local domain
    local               BOOLEAN NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

-- table for node description
CREATE TABLE IF NOT EXISTS nodes (
    id                  INT NOT NULL AUTO_INCREMENT,
    valid               BOOLEAN NOT NULL,
      -- topology exchange id; in ESnet, router name
    topologyIdent       TEXT NOT NULL,
        -- key of corresponding domain in domains table
    domainId            INT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

-- table for port description
CREATE TABLE IF NOT EXISTS ports (
    id                  INT NOT NULL AUTO_INCREMENT,
    valid               BOOLEAN NOT NULL,
    snmpIndex           INT NOT NULL,
        -- topology exchange id; in ESnet, physical interface name
    topologyIdent       TEXT NOT NULL,
        -- total capacity of port in bps
    capacity            BIGINT UNSIGNED NOT NULL,
        -- maximum reservable bandwidth in bps
    maximumReservableCapacity  BIGINT UNSIGNED NOT NULL,
        -- minimum requestable bandwidth
    minimumReservableCapacity  BIGINT UNSIGNED NOT NULL,
        -- granularity of requestable bandwidth
    granularity         BIGINT UNSIGNED NOT NULL,
        -- currently free bandwidth
    unreservedCapacity  BIGINT UNSIGNED NOT NULL,
    alias               TEXT,
        -- key of corresponding node in nodes table
    nodeId              INT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

-- table for link description
CREATE TABLE IF NOT EXISTS links (
    id                  INT NOT NULL AUTO_INCREMENT,
    valid               BOOLEAN NOT NULL,
        -- SNMP index
    snmpIndex           INT,
        -- topology exchange id; in ESnet, logical interface name
    topologyIdent       TEXT NOT NULL,
        -- probably just latency at first
    trafficEngineeringMetric   TEXT,
        -- these may not always have values, so can be null
        -- total capacity of link in bps
    capacity            BIGINT UNSIGNED,
        -- maximum reservable bandwidth in bps
    maximumReservableCapacity  BIGINT UNSIGNED,
        -- minimum requestable bandwidth
    minimumReservableCapacity  BIGINT UNSIGNED,
        -- granularity of requestable bandwidth
    granularity         BIGINT UNSIGNED,
        -- currently free bandwidth
    unreservedCapacity  BIGINT UNSIGNED,
    alias               TEXT,
        -- will eventually be not null, may be many-to-many in the future
    remoteLinkId        INT,          -- foreign key
        -- key of corresponding port in ports table
    portId              INT NOT NULL, -- foreign key
    PRIMARY KEY (id)
) type=MyISAM;

-- table for ip addresses
CREATE TABLE IF NOT EXISTS ipaddrs (
    id                  INT NOT NULL AUTO_INCREMENT,
    valid               BOOLEAN NOT NULL,
        -- IP address
    IP                  TEXT NOT NULL,
        -- key of corresponding link in links table
    linkId              INT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

-- table for paths associated with pending or active reservations
CREATE TABLE IF NOT EXISTS paths (
    id                  INT NOT NULL AUTO_INCREMENT,
        -- whether path was explicitly given by user
    explicit            BOOLEAN NOT NULL,
    pathSetupMode       TEXT,
    nextDomainId        INT,           -- optional foreign key
        -- first element in path
    pathElemId          INT NOT NULL UNIQUE,  -- foreign key
        -- couldn't get Hibernate optional one-to-one associations
        -- working correctly
    interPathElemId     INT UNIQUE,    -- optional foreign key
    layer2DataId        INT UNIQUE,    -- optional foreign key
    layer3DataId        INT UNIQUE,    -- optional foreign key
    mplsDataId          INT UNIQUE,    -- optional foreign key
    PRIMARY KEY (id)
) type=MyISAM;

-- table for elements in paths associated with pending or active reservations
CREATE TABLE IF NOT EXISTS pathElems (
    id                  INT NOT NULL AUTO_INCREMENT,
        -- currently ingress, egress, or null
    description         TEXT,
        -- what this path is made up of
    linkId              INT NOT NULL,  -- foreign key
        -- optional description of link (for things like VLAN id's)
    linkDescr           TEXT,
        -- next element in path
    nextId              INT UNIQUE,    -- maps back to this table
    PRIMARY KEY (id)
) type=MyISAM;

-- table for layer 2 information
CREATE TABLE IF NOT EXISTS layer2Data (
    id                  INT NOT NULL AUTO_INCREMENT,
    srcEndpoint         TEXT NOT NULL,
    destEndpoint        TEXT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

-- table for layer 3 information
CREATE TABLE IF NOT EXISTS layer3Data (
    id                  INT NOT NULL AUTO_INCREMENT,
    srcHost             TEXT NOT NULL, 
    destHost            TEXT NOT NULL,
      -- the following are optional fields
        -- source and destination ports
    srcIpPort           SMALLINT UNSIGNED,
    destIpPort          SMALLINT UNSIGNED,
        -- protocol used (0-255, or a protocol string, such as udp)
    protocol            TEXT,
        -- differentiated services code point
    dscp                TEXT,
    PRIMARY KEY (id)
) type=MyISAM;

-- table for MPLS information
CREATE TABLE IF NOT EXISTS mplsData (
    id                 INT NOT NULL AUTO_INCREMENT,
        -- in bps
    burstLimit         BIGINT UNSIGNED NOT NULL,
    lspClass           TEXT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

-- table for node addresses (layer 2 only)
CREATE TABLE IF NOT EXISTS nodeAddresses (
    id                  INT NOT NULL AUTO_INCREMENT,
    address             TEXT NOT NULL,
        -- key of corresponding node in nodes table
    nodeId              INT NOT NULL UNIQUE,
    PRIMARY KEY (id)
) type=MyISAM;

-- Layer 2 edge table
CREATE TABLE IF NOT EXISTS l2SwitchingCapabilityData (
    id			INT NOT NULL AUTO_INCREMENT,
    linkId		INT NOT NULL UNIQUE,
    vlanRangeAvailability TEXT NOT NULL,
    interfaceMTU	INT NOT NULL,
    PRIMARY KEY (id)
) type = MyISAM;

-- Table that associates outside hops with domains and stores
-- info only needed at edges (layer 3 only)
CREATE TABLE IF NOT EXISTS edgeInfos (
    id                 INT NOT NULL AUTO_INCREMENT,
    externalIP         TEXT NOT NULL,
    -- needs to be populated with bgpinfo tool
    ipaddrId           INT NOT NULL,   -- foreign key
    domainId           INT NOT NULL,   -- foreign key
    PRIMARY KEY(id)
) type = MyISAM;

--
-- Table for signaling tokens
--
CREATE TABLE IF NOT EXISTS tokens (
  id                 INT NOT NULL AUTO_INCREMENT,
  reservationId      INT NOT NULL,
  value              TEXT NOT NULL,
  PRIMARY KEY  (id)
) type = MyISAM;

--
-- Table for history entries
--
CREATE TABLE IF NOT EXISTS `history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `reservationId` int(11) NOT NULL,
  `traceId` text NOT NULL,
  `description` text NOT NULL,
  `operationType` text NOT NULL,
  `operationTime` bigint(20) unsigned NOT NULL,
  `result` text NOT NULL,
  `receivedFrom` text,
  `forwardedTo` text,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM;

--
-- Table for scheduler job entries
--
CREATE TABLE IF NOT EXISTS `jobs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `reservationId` int(11) NOT NULL,
  `operation` text NOT NULL,
  `scheduledTime` bigint(20) unsigned NOT NULL,
  `actualTime` bigint(20) unsigned DEFAULT NULL,
  `result` text NOT NULL,
  `done` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM;

--
-- Table for static LIDP entries
--
CREATE TABLE IF NOT EXISTS staticLIDP (
  id INT NOT NULL AUTO_INCREMENT,
  localLinkId INT NOT NULL,
  destDomainId INT,
  destNodeId INT,
  destPortId INT,
  destLinkId INT,
  defaultRoute TINYINT(1) DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=MyISAM;

--
-- Table for static SIDP entries
--
CREATE TABLE IF NOT EXISTS staticSIDP (
  id INT NOT NULL AUTO_INCREMENT,
  linkId INT NOT NULL,
  nextHopId INT UNIQUE,
  description TEXT,
  PRIMARY KEY (id)
) ENGINE=MyISAM;
