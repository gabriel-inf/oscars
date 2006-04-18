CREATE DATABASE IF NOT EXISTS Interdomain;
USE Interdomain;

-- table for administrative domain, e.g. ESnet
CREATE TABLE IF NOT EXISTS domains (
    domain_id			INT NOT NULL AUTO_INCREMENT,
    domain_as_num               INT NOT NULL,
    domain_name			TEXT NOT NULL,
        -- whether this is the local domain
    local_domain		BOOLEAN NOT NULL,
    PRIMARY KEY (domain_id)
) type=MyISAM;
