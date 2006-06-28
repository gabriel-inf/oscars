USE oscars;

-- The naming convention for tables and views:  tables start with a non-capital
-- letter, views start with a capital letter except those starting with
-- 'sql', which is intended to be used from within a MySQL client.

-- reservations-related views

CREATE OR REPLACE VIEW ReservationList AS
    SELECT 
        makeTag(r.startTime, r.login, r.id) AS tag,
        r.startTime, r.endTime, r.createdTime, r.status, r.origTimeZone,
        r.login,
        sh.name AS srcHost,
        dh.name AS destHost
    FROM reservations r
    INNER JOIN hosts sh ON sh.id = r.srcHostId
    INNER JOIN hosts dh ON dh.id = r.destHostId
    ORDER BY r.startTime DESC;

CREATE OR REPLACE VIEW ReservationAuthDetails AS
    SELECT r.id, r.startTime, r.endTime, r.createdTime, r.origTimeZone, 
        r.bandwidth, r.burstLimit, r.login, r.status,
        r.class, r.srcPort, r.destPort, r.dscp, r.protocol, r.description,
        makeTag(r.startTime, r.login, r.id) AS tag,
        p.pathList AS path,
        sh.name AS srcHost,
        dh.name AS destHost 
    FROM reservations r
    INNER JOIN hosts sh ON sh.id = r.srcHostId
    INNER JOIN hosts dh ON dh.id = r.destHostId
    INNER JOIN topology.paths p ON p.id = r.pathId;

CREATE OR REPLACE VIEW ReservationUserDetails AS
    SELECT r.id, r.startTime, r.endTime, r.createdTime, r.origTimeZone,
        r.bandwidth, r.burstLimit, r.login, r.status, r.class,
        r.srcPort, r.destPort, r.dscp, r.protocol, r.description,
        makeTag(r.startTime, r.login, r.id) AS tag,
        sh.name AS srcHost,
        dh.name AS destHost 
    FROM reservations r
    INNER JOIN hosts sh ON sh.id = r.srcHostId
    INNER JOIN hosts dh ON dh.id = r.destHostId;

CREATE OR REPLACE VIEW CheckOversubscribe AS
    SELECT r.name, i.id, i.valid, i.speed
    FROM topology.interfaces i
    INNER JOIN topology.routers r ON r.id = i.routerId;

--- reservation-related views intended to be run from a MySQL client

CREATE OR REPLACE VIEW sqlResvList AS
    SELECT
        makeTag(r.startTime, r.login, r.id) AS tag,
       (SELECT CONVERT_TZ(
           (SELECT from_unixtime(r.startTime)), '+00:00', origTimeZone)) 
        AS startTime, 
       (SELECT CONVERT_TZ(
           (SELECT from_unixtime(r.endTime)), '+00:00', origTimeZone)) 
        AS endTime, 
        r.status,
        sh.name AS srcHost,
        dh.name AS destHost
    FROM reservations r
    INNER JOIN hosts sh ON sh.id = r.srcHostId
    INNER JOIN hosts dh ON dh.id = r.destHostId
    ORDER BY r.startTime DESC;

CREATE OR REPLACE VIEW sqlResvDetails AS
    SELECT r.id,
       (SELECT CONVERT_TZ(
           (SELECT from_unixtime(r.startTime)), '+00:00', origTimeZone)) 
        AS startTime, 
       (SELECT CONVERT_TZ(
           (SELECT from_unixtime(r.endTime)), '+00:00', origTimeZone)) 
        AS endTime, 
       (SELECT CONVERT_TZ(
           (SELECT from_unixtime(r.createdTime)), '+00:00', origTimeZone)) 
        AS createdTime, 
        r.origTimeZone, r.bandwidth, r.burstLimit, r.login, r.status,
        r.class, r.srcPort, r.destPort, r.dscp, r.protocol,
        makeTag(r.startTime, r.login, r.id) AS tag,
        r.description,
        p.pathList AS path,
        sh.name AS srcHost,
        dh.name AS destHost 
    FROM reservations r
    INNER JOIN hosts sh ON sh.id = r.srcHostId
    INNER JOIN hosts dh ON dh.id = r.destHostId
    INNER JOIN topology.paths p ON p.id = r.pathId;

--- AAA related views

CREATE OR REPLACE VIEW UserList AS
    SELECT u.lastName, u.firstName, u.login, u.phonePrimary,
        i.name AS institutionName
    FROM users u
    INNER JOIN institutions i ON i.id = u.institutionId
    ORDER BY u.lastName;

CREATE OR REPLACE VIEW UserDetails AS
    SELECT u.login, u.lastName, u.firstName, u.emailPrimary, u.phonePrimary,
        u.description, u.emailSecondary, u.phoneSecondary,
        i.name AS institutionName 
    FROM users u
    INNER JOIN institutions i ON i.id = u.institutionId

