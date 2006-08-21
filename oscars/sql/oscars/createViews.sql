USE oscars;

------------------------------------------------------------------------------
-- Copyright (c) 2006, The Regents of the University of California, through
-- Lawrence Berkeley National Laboratory (subject to receipt of any required
-- approvals from the U.S. Dept. of Energy). All rights reserved.
--
-- Redistribution and use in source and binary forms, with or without
-- modification, are permitted provided that the following conditions are met:
--
-- (1) Redistributions of source code must retain the above copyright notice,
--     this list of conditions and the following disclaimer.
--
-- (2) Redistributions in binary form must reproduce the above copyright
--     notice, this list of conditions and the following disclaimer in the
--     documentation and/or other materials provided with the distribution.
--
-- (3) Neither the name of the University of California, Lawrence Berkeley
--     National Laboratory, U.S. Dept. of Energy nor the names of its
--     contributors may be used to endorse or promote products derived from
--     this software without specific prior written permission.
--
-- THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
-- AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
-- IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
-- ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
-- LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
-- CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
-- SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
-- INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
-- CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
-- ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
-- POSSIBILITY OF SUCH DAMAGE.

-- You are under no obligation whatsoever to provide any bug fixes, patches,
-- or upgrades to the features, functionality or performance of the source
-- code ("Enhancements") to anyone; however, if you choose to make your
-- Enhancements available either publicly, or directly to Lawrence Berkeley
-- National Laboratory, without imposing a separate written license agreement
-- for such Enhancements, then you hereby grant the following license: a
-- non-exclusive, royalty-free perpetual license to install, use, modify,
-- prepare derivative works, incorporate into other computer software,
-- distribute, and sublicense such enhancements or derivative works thereof,
-- in binary and source code form.
------------------------------------------------------------------------------

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
        r.pathId,
        sh.name AS srcHost,
        dh.name AS destHost 
    FROM reservations r
    INNER JOIN hosts sh ON sh.id = r.srcHostId
    INNER JOIN hosts dh ON dh.id = r.destHostId;

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

