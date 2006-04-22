-- Database and tables associated with AAA, associated with users
-- and authentication.

CREATE DATABASE IF NOT EXISTS AAA;
USE AAA;

CREATE TABLE IF NOT EXISTS users (
    id			INT NOT NULL AUTO_INCREMENT,
    login		TEXT NOT NULL,
    certificate		TEXT,
    certSubject		TEXT,
    lastName	 	TEXT NOT NULL,
    firstName		TEXT NOT NULL,
    emailPrimary	TEXT NOT NULL,
    phonePrimary	TEXT NOT NULL,
    password		TEXT,
    description		TEXT,
    emailSecondary	TEXT,
    phoneSecondary	TEXT,
    status		TEXT,
    activationKey	TEXT,
    lastActiveTime	DATETIME,
    registerTime	DATETIME,
    institutionId	INT NOT NULL,	-- foreign key
    PRIMARY KEY (id)

) type=MyISAM;

CREATE TABLE IF NOT EXISTS institutions (
    id			INT NOT NULL AUTO_INCREMENT,
    name		TEXT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

CREATE TABLE IF NOT EXISTS resources (
    id			INT NOT NULL AUTO_INCREMENT,
    name		TEXT NOT NULL,
    description		TEXT,
    updateTime		DATETIME,
    PRIMARY KEY (id)
) type=MyISAM;

CREATE TABLE IF NOT EXISTS permissions (
    id			INT NOT NULL AUTO_INCREMENT,
    name		TEXT NOT NULL,
    description		TEXT,
    updateTime		DATETIME,
    PRIMARY KEY (id)
) type=MyISAM;

-- cross reference table
CREATE TABLE IF NOT EXISTS resourcePermissions (
    resourceId		INT NOT NULL,	-- foreign key
    permissionId	INT NOT NULL,	-- foreign key
    PRIMARY KEY (resourceId, permissionId)
) type=MyISAM;

CREATE TABLE IF NOT EXISTS authorizations (
    id			INT NOT NULL AUTO_INCREMENT,
    context		TEXT,
    updateTime		DATETIME,
    userId		INT NOT NULL,	-- foreign key
    resourceId 		INT NOT NULL,	-- foreign key
    permissionId	INT NOT NULL,	-- foreign key
    PRIMARY KEY (id)
) type=MyISAM;

