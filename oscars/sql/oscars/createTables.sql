-- Database and tables associated with oscars, associated with users
-- and authentication.

CREATE DATABASE IF NOT EXISTS oscars;
USE oscars;

-- AAA tables ------------------------------------------------------

CREATE TABLE IF NOT EXISTS users (
    id			INT NOT NULL AUTO_INCREMENT,
    login		TEXT NOT NULL,
    certificate		TEXT,
    certSubject		TEXT,
    lastName	 	TEXT NOT NULL,
    firstName		TEXT NOT NULL,
    emailPrimary	TEXT NOT NULL,
    phonePrimary	TEXT NOT NULL,
    password		TEXT,
    description		TEXT,
    emailSecondary	TEXT,
    phoneSecondary	TEXT,
    status		TEXT,
    activationKey	TEXT,
    lastActiveTime	INT,
    registerTime	INT,
    institutionId	INT NOT NULL,	-- foreign key
    PRIMARY KEY (id)

) type=MyISAM;

CREATE TABLE IF NOT EXISTS institutions (
    id			INT NOT NULL AUTO_INCREMENT,
    name		TEXT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

CREATE TABLE IF NOT EXISTS resources (
    id			INT NOT NULL AUTO_INCREMENT,
    name		TEXT NOT NULL,
    description		TEXT,
    updateTime		INT,
    PRIMARY KEY (id)
) type=MyISAM;

CREATE TABLE IF NOT EXISTS permissions (
    id			INT NOT NULL AUTO_INCREMENT,
    name		TEXT NOT NULL,
    description		TEXT,
    updateTime		INT,
    PRIMARY KEY (id)
) type=MyISAM;

-- cross reference table
CREATE TABLE IF NOT EXISTS resourcePermissions (
    resourceId		INT NOT NULL,	-- foreign key
    permissionId	INT NOT NULL,	-- foreign key
    PRIMARY KEY (resourceId, permissionId)
) type=MyISAM;

CREATE TABLE IF NOT EXISTS authorizations (
    id			INT NOT NULL AUTO_INCREMENT,
    context		TEXT,
    updateTime		INT,
    userId		INT NOT NULL,	-- foreign key
    resourceId 		INT NOT NULL,	-- foreign key
    permissionId	INT NOT NULL,	-- foreign key
    PRIMARY KEY (id)
) type=MyISAM;

-- Reservations tables -------------------------------------------

-- table for administrative domain, e.g. ESnet
CREATE TABLE IF NOT EXISTS domains (
    id		INT NOT NULL AUTO_INCREMENT,
    as_num	INT NOT NULL,
    name	TEXT NOT NULL,
        -- used in creating unique reservation tags
    abbrev      TEXT NOT NULL,
        -- whether this is the local domain
    local	BOOLEAN NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

-- table for interdomain paths of reservations
CREATE TABLE IF NOT EXISTS interdomainPaths (
    id				INT NOT NULL AUTO_INCREMENT,
        -- if path still valid
    valid                       BOOLEAN NOT NULL,
        -- used for quick comparison to see if path already exists
    domainList			TEXT,
        -- number of times this path is referenced (inactive if 0)
    refCtr                      INT,
    PRIMARY KEY (id)
) type=MyISAM;

-- cross reference table
CREATE TABLE IF NOT EXISTS interdomainPathDomains (
    interdomainPathId		INT NOT NULL,	-- foreign key
    domainId			INT NOT NULL,	-- foreign key
       -- unique reservation tag used in that domain
    reservationTag              TEXT NOT NULL,  -- foreign key
       -- used to order path
    sequenceNumber              INT NOT NULL,
    PRIMARY KEY (interdomainPathId, domainId)
) type=MyISAM;


-- table for source and destination IP addresses
CREATE TABLE IF NOT EXISTS hosts (
    id			INT NOT NULL AUTO_INCREMENT,
    IP			TEXT NOT NULL,
        -- cached host name (or IP address if DNS couldn't find it)
    name                TEXT,
    PRIMARY KEY (id)
) type=MyISAM;

-- table holding reservation information
-- this information is passed to the PSS
CREATE TABLE IF NOT EXISTS reservations (
    id			INT NOT NULL AUTO_INCREMENT,
      -- times will be in seconds since epoch (UTC)
    startTime		INT NOT NULL,
    endTime		INT NOT NULL,
        -- time this entry was created
    createdTime		INT NOT NULL,
        -- client's time zone
    origTimeZone	TEXT NOT NULL,
        -- bandwidth requested (Mbps)
    bandwidth		BIGINT UNSIGNED NOT NULL,
        -- in Mbps
    burstLimit		BIGINT UNSIGNED NOT NULL,
        -- user making the reservation
    login		TEXT NOT NULL,
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
    description		TEXT,
      -- foreign keys (not optional)
      -- local path
    pathId		INT NOT NULL,   -- foreign key
      -- interdomain path
    interdomainPathId	INT NOT NULL,   -- foreign key
        -- keys of source and destination addresses in hosts table
    srcHostId		INT NOT NULL,   -- foreign key 
    destHostId		INT NOT NULL,	-- foreign key 
    PRIMARY KEY (id)
) type = MyISAM;


-- Table holding archived reservation information.  Foreign keys are replaced
-- by what they reference, since router information may change over time.
CREATE TABLE IF NOT EXISTS archivedReservations (
    -- Note that this is the id of the row in the original reservations
    -- table.
    id			INT NOT NULL,
      -- times will be in seconds since epoch (UTC)
    startTime		INT NOT NULL,
    endTime		INT NOT NULL,
        -- time this entry was created
    createdTime		INT NOT NULL,
        -- client's time zone
    origTimeZone	TEXT NOT NULL,
        -- bandwidth requested (Mbps)
    bandwidth		BIGINT UNSIGNED NOT NULL,
        -- in Mbps
    burstLimit		BIGINT UNSIGNED NOT NULL,
        -- user making the reservation
    login		TEXT NOT NULL,
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
        -- space separated list of names or addresses of routers in path
    path		TEXT,
    description		TEXT,
    srcHost		TEXT NOT NULL, 
    destHost		TEXT NOT NULL,
    PRIMARY KEY (id)
) type = MyISAM;

-- Tests tables ---------------------------------------------------

-- Test configuration.
CREATE TABLE IF NOT EXISTS configTests (
    id			INT NOT NULL AUTO_INCREMENT,
    testName		TEXT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;


-- Test address.
CREATE TABLE IF NOT EXISTS configAddresses (
    id			INT NOT NULL AUTO_INCREMENT,
    address		TEXT NOT NULL,
    description		TEXT,
    testConfId		INT,	-- foreign key
    PRIMARY KEY (id)
) type=MyISAM;

