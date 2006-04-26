USE oscars;

CREATE OR REPLACE VIEW userReservations AS
    SELECT login, id, startTime, endTime, createdTime, bandwidth, burstLimit, 
           status, class, srcPort, destPort, dscp, protocol, tag, description,
           srcHostId, destHostId, origTimeZone FROM reservations;

CREATE OR REPLACE VIEW userList AS
    SELECT u.lastName, u.firstName, u.login,
           i.name AS institutionName, u.phonePrimary
    FROM users u, institutions i
    WHERE i.id = u.institutionId
    ORDER BY u.lastName;
