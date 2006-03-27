-- Database and tables associated with AAAS, associated with users
-- and authentication.

CREATE DATABASE IF NOT EXISTS AAAS;
USE AAAS;

CREATE TABLE IF NOT EXISTS users (
    user_id			INT NOT NULL AUTO_INCREMENT,
    user_login			TEXT NOT NULL,
    user_certificate		TEXT,
    user_cert_subject           TEXT,
    user_last_name	 	TEXT NOT NULL,
    user_first_name		TEXT NOT NULL,
    user_email_primary		TEXT NOT NULL,
    user_phone_primary		TEXT NOT NULL,
    user_password		TEXT,
    user_description		TEXT,
    user_email_secondary	TEXT,
    user_phone_secondary	TEXT,
    user_status			TEXT,
    user_activation_key		TEXT,
    user_last_active_time	DATETIME,
    user_register_time		DATETIME,
    institution_id		INT NOT NULL,	-- foreign key
    PRIMARY KEY (user_id)

) type=MyISAM;

CREATE TABLE IF NOT EXISTS institutions (
    institution_id		INT NOT NULL AUTO_INCREMENT,
    institution_name		TEXT NOT NULL,
    PRIMARY KEY (institution_id)
) type=MyISAM;

CREATE TABLE IF NOT EXISTS resources (
    resource_id			INT NOT NULL AUTO_INCREMENT,
    resource_name		TEXT NOT NULL,
    resource_description	TEXT,
    resource_update_time	DATETIME NOT NULL,
    PRIMARY KEY (resource_id)
) type=MyISAM;

CREATE TABLE IF NOT EXISTS permissions (
    permission_id		INT NOT NULL AUTO_INCREMENT,
    permission_name		TEXT NOT NULL,
    permission_description	TEXT,
    permission_update_time	DATETIME NOT NULL,
    PRIMARY KEY (permission_id)
) type=MyISAM;

-- cross reference table
CREATE TABLE IF NOT EXISTS resourcepermissions (
    resource_id			INT NOT NULL,	-- foreign key
    permission_id		INT NOT NULL,	-- foreign key
    PRIMARY KEY (resource_id, permission_id)
) type=MyISAM;

CREATE TABLE IF NOT EXISTS authorizations (
    authorization_id		INT NOT NULL AUTO_INCREMENT,
    authorization_context	TEXT NOT NULL,
    authorization_update_time	DATETIME NOT NULL,
    user_id			INT NOT NULL,	-- foreign key
    resource_id 		INT NOT NULL,	-- foreign key
    permission_id		INT NOT NULL,	-- foreign key
    PRIMARY KEY (authorization_id)
) type=MyISAM;


-- Table for default clients.
CREATE TABLE IF NOT EXISTS clients (
    client_id			INT NOT NULL AUTO_INCREMENT,
    client_uri			TEXT NOT NULL,
    client_proxy		TEXT NOT NULL,
    as_num			TEXT,   -- autonomous system number
    PRIMARY KEY (client_id)
    ) type=MyISAM;
