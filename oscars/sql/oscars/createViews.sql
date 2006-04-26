USE oscars;

CREATE VIEW userReservations AS
    SELECT startTime, endTime, createdTime, bandwidth, burstLimit, status,
           class, srcPort, destPort, dscp, protocol, tag, description,
           srcHostId, destHostId, origTimeZone FROM reservations;
