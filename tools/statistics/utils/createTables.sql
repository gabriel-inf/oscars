use bss;

CREATE TABLE resvPathReport (
    id INT NOT NULL AUTO_INCREMENT,
    reservationId INT NOT NULL,
    pathId INT NOT NULL,
    createdTime BIGINT UNSIGNED NOT NULL,
    duration BIGINT UNSIGNED NOT NULL,
    bandwidth BIGINT UNSIGNED NOT NULL,
    login               TEXT NOT NULL,
    payloadSender       TEXT,
    hopCount INT NOT NULL,
    ingress TEXT NOT NULL,
    egress TEXT NOT NULL,
    INDEX edgeIndex (ingress(125), egress(125)),
    INDEX userIndex (login(50), payloadSender(50)),
    INDEX timeIndex (createdTime),
    INDEX pathIndex (pathId),
    PRIMARY KEY (id)
);