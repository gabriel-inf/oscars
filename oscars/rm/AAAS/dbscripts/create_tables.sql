-- Database and tables associated with AAAS, associated with users
-- and authentication.

CREATE DATABASE IF NOT EXISTS AAAS;
USE AAAS;

CREATE TABLE IF NOT EXISTS users (
    user_id INT(6) NOT NULL AUTO_INCREMENT,
    user_last_name VARCHAR(30) NOT NULL,
    user_first_name VARCHAR(20),
    user_dn VARCHAR(30) NOT NULL,
    user_password varchar(30),
    user_email_primary VARCHAR(30) NOT NULL,
    user_level INT(2) NOT NULL,
    user_email_secondary VARCHAR(30),
    user_phone_primary VARCHAR(20),
    user_phone_secondary VARCHAR(20),
    user_description TEXT,
    user_status VARCHAR(20),
    user_last_active_time DATETIME,
    user_register_time DATETIME,
    user_activation_key VARCHAR(40),
    institution_id INT(6) NOT NULL, -- foreign key
    PRIMARY KEY (user_id)

) type=MyISAM;

CREATE TABLE IF NOT EXISTS institutions (
    institution_id INT(5) NOT NULL AUTO_INCREMENT,
    institution_name VARCHAR(50),
    PRIMARY KEY (institution_id)
) type=MyISAM;


CREATE TABLE IF NOT EXISTS user_levels (
    user_level_id INT(5) NOT NULL AUTO_INCREMENT,
    user_level_bit INT(3) NOT NULL,
    user_level_description VARCHAR(16) NOT NULL,
    auth_type_id INT(3) NOT NULL,
    PRIMARY KEY (user_level_id)
) type=MyISAM;


CREATE TABLE IF NOT EXISTS auth_types (
    auth_type_id INT(3) NOT NULL AUTO_INCREMENT,
    auth_name VARCHAR(30),
    PRIMARY KEY (auth_type_id)
) type=MyISAM;


CREATE TABLE IF NOT EXISTS allocations (
    allocation_id INT(5) NOT NULL AUTO_INCREMENT,
        -- in seconds
    allocation_amount INT(9),
    allocation_used INT(9),
    user_id INT(6) NOT NULL,
    PRIMARY KEY (allocation_id)
) type = MyISAM;


-- Table containing default AAAS server info, and info about each running 
-- server.  When a server is started, it registers itself by creating
-- an entry in this table.
CREATE TABLE IF NOT EXISTS aaas_servers (
    aaas_server_id               INT NOT NULL AUTO_INCREMENT,
        -- This has a default value in the db, but can be overriden on 
        -- the command line.
    aaas_server_port             INT NOT NULL,

        -- debug level
    aaas_server_debug            INT NOT NULL,
    aaas_server_start_time       DATETIME,
    aaas_server_end_time         DATETIME,

    aaas_server_mail_list        VARCHAR(36) NOT NULL,
    aaas_server_send_mail        BOOLEAN NOT NULL,
    PRIMARY KEY (aaas_server_id)
) type=MyISAM;


-- Table containing default BSS server info, and info about each running server.
-- When a server is started, it registers itself by creating an entry in
-- this table.
CREATE TABLE IF NOT EXISTS bss_servers (
    bss_server_id               INT NOT NULL AUTO_INCREMENT,
        -- This has a default value in the db, but can be overriden on the 
        -- command line.
    bss_server_port             INT NOT NULL,

        -- debug level
    bss_server_debug            INT NOT NULL,
        -- Time (in seconds) between polling the reservation db
    bss_server_db_poll_time     INT NOT NULL,
        -- Time interval (in seconds) to search for reservations 
        -- to schedule must be larger then db_poll time
    bss_server_time_interval    INT NOT NULL,

    bss_server_start_time       DATETIME,
    bss_server_end_time         DATETIME,

    bss_server_mail_list        VARCHAR(36) NOT NULL,
    bss_server_send_mail        BOOLEAN NOT NULL,
    PRIMARY KEY (bss_server_id)
) type=MyISAM;
