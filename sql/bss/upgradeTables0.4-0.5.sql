-- update bss tables from release 0.4 to 0.5

USE bss;

-- temp
DELETE FROM mplsData where id not in (select mplsDataId from paths where mplsDataId is not null);

--
-- table to store additional parameters relating to a pathElem
--
CREATE TABLE IF NOT EXISTS pathElemParams (
    id                  INT NOT NULL AUTO_INCREMENT,
    pathElemId          INT NOT NULL, -- foreign key
    swcap               TEXT NOT NULL,
    type                TEXT NOT NULL,
    value               TEXT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

ALTER TABLE paths ADD reservationId INT AFTER id;
ALTER TABLE paths ADD pathType TEXT AFTER nextDomainId;
ALTER TABLE layer2Data ADD pathId INT AFTER id;
ALTER TABLE layer3Data ADD pathId INT AFTER id;
ALTER TABLE mplsData ADD pathId INT AFTER id;
ALTER TABLE pathElems ADD pathId INT AFTER id;
ALTER TABLE pathElems ADD seqNumber INT AFTER pathId; 
ALTER TABLE pathElems ADD urn TEXT NOT NULL AFTER seqNumber;
ALTER TABLE pathElems ADD userName TEXT after urn;

UPDATE paths p set p.reservationId =
    (SELECT r.id from reservations r where r.pathId = p.id);

-- create intradomain paths table for copy (can't figure out how to make
-- unique field non-unique, alter doesn't work)
CREATE TABLE intraPaths (
    id                  INT NOT NULL AUTO_INCREMENT,
    reservationId       INT NOT NULL,
    explicit            BOOLEAN NOT NULL,
    pathSetupMode       TEXT,
    nextDomainId        INT,
    pathType            TEXT,
    pathElemId          INT NOT NULL,
    layer2DataId        INT,
    layer3DataId        INT,
    mplsDataId          INT,
    PRIMARY KEY (id)
) type=MyISAM;

INSERT INTO intraPaths (reservationId, explicit, pathSetupMode, nextDomainId,
       	pathType, pathElemId, layer2DataId, layer3DataId, mplsDataId)
    SELECT p.reservationId, p.explicit, p.pathSetupMode, p.nextDomainId,
        p.pathType, p.pathElemId, p.layer2DataId, p.layer3DataId,
	p.mplsDataId
    FROM paths p;

-- create interdomain paths table for copy
CREATE TABLE interPaths (
    id                  INT NOT NULL AUTO_INCREMENT,
    reservationId       INT NOT NULL,
    explicit            BOOLEAN NOT NULL,
    pathSetupMode       TEXT,
    nextDomainId        INT,
    pathType            TEXT,
    pathElemId          INT NOT NULL,
    layer2DataId        INT,
    layer3DataId        INT,
    mplsDataId          INT,
    PRIMARY KEY (id)
) type=MyISAM;

INSERT INTO interPaths (reservationId, explicit, pathSetupMode, nextDomainId,
       	pathType, pathElemId, layer2DataId, layer3DataId, mplsDataId)
    SELECT p.reservationId, p.explicit, p.pathSetupMode, p.nextDomainId,
        p.pathType, p.interPathElemId, p.layer2DataId, p.layer3DataId,
	p.mplsDataId
    FROM paths p;

UPDATE intraPaths set pathType = 'local';
UPDATE interPaths set pathType = 'interdomain';
DROP TABLE paths;

-- create paths table with some fields that are temporary
CREATE TABLE paths (
    id                  INT NOT NULL AUTO_INCREMENT,
    reservationId       INT NOT NULL,
    explicit            BOOLEAN NOT NULL,
    pathSetupMode       TEXT,
    nextDomainId        INT,
    pathType            TEXT NOT NULL,
    pathElemId          INT NOT NULL,
    layer2DataId        INT,
    layer3DataId        INT,
    mplsDataId          INT,
    PRIMARY KEY (id)
) type=MyISAM;

INSERT INTO paths (reservationId, explicit, pathSetupMode, nextDomainId,
       	pathType, pathElemId, layer2DataId, layer3DataId, mplsDataId)
    SELECT p.reservationId, p.explicit, p.pathSetupMode, p.nextDomainId,
        p.pathType, p.pathElemId, p.layer2DataId, p.layer3DataId,
	p.mplsDataId
    FROM intraPaths p;

INSERT INTO paths (reservationId, explicit, pathSetupMode, nextDomainId,
       	pathType, pathElemId, layer2DataId, layer3DataId, mplsDataId)
    SELECT p.reservationId, p.explicit, p.pathSetupMode, p.nextDomainId,
        p.pathType, p.pathElemId, p.layer2DataId, p.layer3DataId,
	p.mplsDataId
    FROM interPaths p;

DROP TABLE intraPaths;
DROP TABLE interPaths;

ALTER TABLE paths ADD direction TEXT AFTER pathType;
ALTER TABLE paths ADD priority INT AFTER pathType;
ALTER TABLE paths ADD grouping TEXT AFTER priority;

-- stored procedure section

DROP PROCEDURE IF EXISTS updatePathElems;
DROP PROCEDURE IF EXISTS alterLayer2Data;
DROP PROCEDURE IF EXISTS alterLayer3Data;
DROP PROCEDURE IF EXISTS alterMplsData;
DROP PROCEDURE IF EXISTS alterPathElems;

DELIMITER //
CREATE PROCEDURE updatePathElems()
BEGIN
    DECLARE pId INT;
    DECLARE l2DataId INT;
    DECLARE l3DataId INT;
    DECLARE mDataId INT;
    DECLARE pElemId INT;
    DECLARE pType TEXT;
    DECLARE finished INT DEFAULT 0;
    DECLARE pathElemCur CURSOR FOR
        SELECT id, layer2DataId, layer3DataId, mplsDataId, pathElemId, pathType
       	from paths;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;

    OPEN pathElemCur;
    alterPath: LOOP
        FETCH pathElemCur INTO pId, l2DataId, l3DataId, mDataId, pElemId, pType;
	IF finished THEN
	    LEAVE alterPath;
        END IF;
	IF l2DataId IS NOT NULL THEN
	    CALL alterLayer2Data(pId, l2DataId, pType);
        END IF;
	IF l3DataId IS NOT NULL THEN
	    CALL alterLayer3Data(pId, l3DataId, pType);
        END IF;
	IF mDataId IS NOT NULL THEN
	    CALL alterMplsData(pId, mDataId, pType);
        END IF;
	CALL alterPathElems(pId, pElemId);
    END LOOP alterPath;
END
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE alterLayer2Data(IN pId INT, IN l2DataId INT, IN pType TEXT)
BEGIN
    DECLARE srcPoint TEXT;
    DECLARE destPoint TEXT;

    IF pType = 'intra' THEN
	UPDATE layer2Data SET pathId = pId where id = l2DataId;
    ELSE
	SELECT srcEndpoint, destEndpoint INTO srcPoint, destPoint
	    FROM layer2Data where id = l2DataId;
	INSERT INTO layer2Data VALUES(NULL, pId, srcPoint, destPoint);
    END IF;
END
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE alterLayer3Data(IN pId INT, IN l3DataId INT, IN pType TEXT)
BEGIN
    DECLARE sHost TEXT;
    DECLARE dHost TEXT;
    DECLARE sIpPort SMALLINT UNSIGNED;
    DECLARE dIpPort SMALLINT UNSIGNED;
    DECLARE prot TEXT;
    DECLARE d TEXT;

    IF pType = 'intra' THEN
	UPDATE layer3Data SET pathId = pId where id = l3DataId;
    ELSE
	SELECT srcHost, destHost, srcIpPort, destIpPort, protocol, dscp
       	INTO sHost, dHost, sIpPort, dIpPort, prot, d
        FROM layer3Data where id = l3DataId;
	INSERT INTO layer3Data VALUES(NULL, pId, sHost, dHost, sIpPort, dIpPort,
	                              prot, d);
    END IF;
END
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE alterMplsData(IN pId INT, IN mDataId INT, IN pType TEXT)
BEGIN
    DECLARE bLimit BIGINT UNSIGNED;
    DECLARE lsp TEXT;

    IF pType = 'intra' THEN
	UPDATE mplsData SET pathId = pId where id = mDataId;
    ELSE
	SELECT burstLimit, lspClass INTO bLimit, lsp
	    FROM mplsData where id = mDataId;
	INSERT INTO mplsData VALUES(NULL, pId, bLimit, lsp);
    END IF;
END
//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE alterPathElems(IN pId INT, IN pElemId INT)
BEGIN
    DECLARE peId INT;
    DECLARE nId INT;
    DECLARE seqNum INT DEFAULT 0;

    UPDATE pathElems SET pathId = pId, seqNumber = seqNum where id = pElemId;
    SELECT nextId INTO nId FROM pathElems where id = pElemId;
    WHILE nId IS NOT NULL DO
	SELECT id, nextId INTO peId, nId FROM pathElems where id=nId;
	SET seqNum = seqNum + 1;
	UPDATE pathElems set pathId = pId, seqNumber = seqNum
	WHERE id=peId;
    END WHILE;
END
//
DELIMITER ;

CALL updatePathElems();

-- end stored procedure section

ALTER TABLE reservations DROP pathId;
ALTER TABLE paths DROP layer2DataId;
ALTER TABLE paths DROP layer3DataId;
ALTER TABLE paths DROP mplsDataId;
ALTER TABLE paths DROP pathElemId;
ALTER TABLE pathElems DROP nextId;
ALTER TABLE pathElems DROP description;
-- ALTER TABLE pathElems DROP linkDescr;

ALTER TABLE pathElems CHANGE linkId linkId INT;
ALTER TABLE layer2Data CHANGE pathId pathId INT NOT NULL UNIQUE;
ALTER TABLE layer3Data CHANGE pathId pathId INT NOT NULL UNIQUE;
ALTER TABLE mplsData CHANGE pathId pathId INT NOT NULL UNIQUE;

-- delete any left over orphaned path elements

DELETE FROM pathElems where seqNumber IS NULL;

