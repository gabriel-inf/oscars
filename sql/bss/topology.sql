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
