-- Database and tables associated with AAAS, associated with users
-- and authentication.

CREATE DATABASE IF NOT EXISTS AAAS;
USE AAAS;

CREATE TABLE IF NOT EXISTS users (
    user_id			INT NOT NULL AUTO_INCREMENT,
    user_dn			TEXT NOT NULL,
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
    resource_hidden		BOOLEAN NOT NULL,
    resource_name		TEXT NOT NULL,
    resource_type               TEXT NOT NULL,
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


-- Table containing information necessary to start up a SOAP::Lite daemon
-- and for clients to access it.
CREATE TABLE IF NOT EXISTS daemons (
    daemon_id			INT NOT NULL AUTO_INCREMENT,
      --  currently only AAAS and BSS
    daemon_component_type	TEXT NOT NULL,
      --  defaults are AAAS and BSS, can be other ones set up for testing
    daemon_component_name	TEXT NOT NULL,
      -- used to start SOAP::Lite daemon
    daemon_host_name		TEXT NOT NULL,
    daemon_port			INT NOT NULL,
      -- information necessary for client of this server. Built up from
      -- host name, port, and class name.
    daemon_dispatcher_class	TEXT NOT NULL,
    daemon_server_class		TEXT NOT NULL,
      -- not currently used
    daemon_mail_list		TEXT NOT NULL,
    daemon_send_mail		BOOLEAN NOT NULL,
    PRIMARY KEY (daemon_id)
) type=MyISAM;
