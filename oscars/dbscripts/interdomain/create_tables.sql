CREATE DATABASE IF NOT EXISTS Interdomain;
USE Interdomain;

-- table for administrative domain, e.g. ESnet
CREATE TABLE IF NOT EXISTS domains (
    id		INT NOT NULL AUTO_INCREMENT,
    as_num	INT NOT NULL,
    name	TEXT NOT NULL,
        -- whether this is the local domain
    local	BOOLEAN NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;
