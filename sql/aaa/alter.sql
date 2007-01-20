USE oscars;

-- Hibernate functionality is used now instead of views
DROP VIEW CheckOversubscribe;
DROP VIEW ReservationAuthDetails;
DROP VIEW ReservationList;
DROP VIEW UserDetails;
DROP VIEW UserList;
DROP VIEW sqlResvList;
DROP VIEW sqlResvDetails;

-- these were never used
DROP TABLE interdomainPathDomains;
DROP TABLE interdomainPaths;

-- temporary table for transforming reservations table data
CREATE TABLE IF NOT EXISTS tmpReservations (
    id                  INT NOT NULL AUTO_INCREMENT,
    startTime           DATETIME NOT NULL,
    endTime             DATETIME NOT NULL,
        -- time this entry was created
    createdTime         DATETIME NOT NULL,
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
    pathId              INT NOT NULL,   -- foreign key
    PRIMARY KEY (id)
) type = MyISAM;

-- transform data as it is copied

INSERT INTO tmpReservations (startTime, endTime, createdTime,
	                     bandwidth, burstLimit, login, status, lspClass,
			     srcHost, destHost, srcPort, destPort, dscp,
			     protocol, description, pathId)
    SELECT
        r.startTime, r.endTime, r.createdTime,
	r.bandwidth, r.burstLimit, r.login, r.status, r.class,
	sh.IP as srcHost,
	dh.IP as destHost,
        r.srcPort, r.destPort, r.dscp, r.protocol, r.description, r.pathId
    FROM reservations r
    INNER JOIN hosts sh ON sh.id = r.srcHostId
    INNER JOIN hosts dh ON dh.id = r.destHostId;

DROP TABLE reservations;

ALTER TABLE tmpReservations RENAME reservations;

DROP TABLE hosts;

