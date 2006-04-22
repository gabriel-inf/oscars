CREATE DATABASE IF NOT EXISTS topology;
USE topology;

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
