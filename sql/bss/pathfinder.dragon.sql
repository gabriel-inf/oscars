CREATE DATABASE IF NOT EXISTS bss;
USE bss;

-- Table that maps hosts to DRAGON local ID values
CREATE TABLE IF NOT EXISTS dragonLocalIdMap (
    id                  INT NOT NULL AUTO_INCREMENT,
    vlsrIpId			INT NOT NULL,
    ip					TEXT NOT NULL,
    number				INT NOT NULL,
    type 				TEXT NOT NULL,
    PRIMARY KEY (id)
) type = MyISAM;

-- Table that associates outside hops with domains
CREATE TABLE IF NOT EXISTS peerIpaddrs (
    id		INT NOT NULL AUTO_INCREMENT,
    domainId	INT NOT NULL,
    ip		TEXT NOT NULL,
    PRIMARY KEY(id)
) type = MyISAM;
