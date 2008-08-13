-- Database and tables associated with AAA component, associated with users
-- and authentication.

CREATE DATABASE IF NOT EXISTS aaa;
USE aaa;

-- AAA tables ------------------------------------------------------

CREATE TABLE IF NOT EXISTS users (
    id                  INT NOT NULL AUTO_INCREMENT,
    login               TEXT NOT NULL,
    certIssuer          TEXT,
    certSubject         TEXT,
    lastName            TEXT NOT NULL,
    firstName           TEXT NOT NULL,
    emailPrimary        TEXT NOT NULL,
    phonePrimary        TEXT NOT NULL,
    password            TEXT,
    description         TEXT,
    emailSecondary      TEXT,
    phoneSecondary      TEXT,
    status              TEXT,
    activationKey       TEXT,
    loginTime           BIGINT,
    cookieHash          TEXT,
    institutionId       INT NOT NULL,    -- foreign key (when convert to InnoDB)
    PRIMARY KEY (id)

) type=MyISAM;

CREATE TABLE IF NOT EXISTS institutions (
    id                  INT NOT NULL AUTO_INCREMENT,
    name                TEXT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;
CREATE UNIQUE INDEX instName ON institutions(name(9));

CREATE TABLE IF NOT EXISTS resources (
    id                  INT NOT NULL AUTO_INCREMENT,
    name                TEXT NOT NULL,
    description         TEXT,
    updateTime          BIGINT,
    PRIMARY KEY (id)
) type=MyISAM;

CREATE TABLE IF NOT EXISTS permissions (
    id                  INT NOT NULL AUTO_INCREMENT,
    name                TEXT NOT NULL,
    description         TEXT,
    updateTime          BIGINT,
    PRIMARY KEY (id)
) type=MyISAM;

-- Create constraints table for use of the AAA web interface
CREATE TABLE IF NOT EXISTS constraints (
    id                  INT NOT NULL AUTO_INCREMENT,
    name                TEXT NOT NULL,
    type				TEXT NOT NULL,
    description			TEXT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;
CREATE UNIQUE INDEX constraintName ON constraints(name(9)); 

-- Create resource, permission, constraint (RPC) table which contains a list of the meaningful RPC tuples
CREATE TABLE IF NOT EXISTS rpcs (
   id              		INT NOT NULL AUTO_INCREMENT,
   resourceId			INT NOT NULL,
   permissionId			INT NOT NULL,
   constraintId			INT NOT NULL,
   PRIMARY KEY (id)
) type=MyISAM;
CREATE UNIQUE INDEX const ON rpcs(resourceId,permissionId,constraintId); 

CREATE TABLE IF NOT EXISTS authorizations (
    id                  INT NOT NULL AUTO_INCREMENT,
    context             TEXT,
    updateTime          BIGINT,
    attrId              INT NOT NULL,    -- foreign key
    resourceId          INT NOT NULL,    -- foreign key
    permissionId        INT NOT NULL,    -- foreign key
    constraintId        INT NOT NULL,    -- foreign key
    constraintValue     TEXT,
    PRIMARY KEY (id)
) type=MyISAM;
CREATE UNIQUE INDEX row ON authorizations (attrId,resourceId,permissionId,constraintId);

CREATE TABLE IF NOT EXISTS attributes (
    id                  INT NOT NULL AUTO_INCREMENT,
    name                TEXT NOT NULL,
    attrType            TEXT,
    description			TEXT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;
CREATE UNIQUE INDEX attrName ON attributes(name(9));

-- cross reference table
CREATE TABLE IF NOT EXISTS userAttributes (
    id		               INT NOT NULL AUTO_INCREMENT,
    userId              INT NOT NULL,    -- foreign key
    attributeId         INT NOT NULL,    -- foreign key
    PRIMARY KEY (id)
) type=MyISAM;
CREATE UNIQUE INDEX userAttr ON userAttributes(userId,attributeId);
