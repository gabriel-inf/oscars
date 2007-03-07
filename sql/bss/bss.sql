CREATE DATABASE IF NOT EXISTS bss;
USE bss;

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
        -- VLAN id
    vlanId              INT,
    description         TEXT,
    pathId              INT NOT NULL,   -- foreign key
    nextDomainId        INT NOT NULL,   -- foreign key
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
        -- VLAN id
    vlanId              INT,
        -- was a foreign key before archiving
        -- space separated list of names or addresses of routers in path
    path                TEXT,
        -- was a foreign key before archiving
    nextDomain          TEXT,
    PRIMARY KEY (id)
) type = MyISAM;
