-- Database and tables associated with AAAS, associated with users
-- and authentication.

CREATE DATABASE IF NOT EXISTS AAAS;
USE AAAS;

CREATE TABLE IF NOT EXISTS users (
    user_id INT(6) NOT NULL AUTO_INCREMENT,
    user_last_name VARCHAR(50) NOT NULL,
    user_first_name VARCHAR(50),
        -- for now
    user_dn VARCHAR(25) NOT NULL,
    user_password varchar(50),
    user_email_primary VARCHAR(100) NOT NULL,
    user_level INT(2) NOT NULL,
    user_email_secondary VARCHAR(100),
    user_phone_primary VARCHAR(50),
    user_phone_secondary VARCHAR(50),
    user_description TEXT,
    user_register_time DATETIME,
    user_activation_key VARCHAR(40),
    institution_id INT(6),      -- foreign key
    PRIMARY KEY (user_id)

) type=MyISAM;

CREATE TABLE IF NOT EXISTS institutions (
    institution_id INT(5) NOT NULL AUTO_INCREMENT,
    institution_name VARCHAR(50),
    PRIMARY KEY (institution_id)
) type=MyISAM;


CREATE TABLE IF NOT EXISTS user_levels (
    user_level_id INT(5) NOT NULL AUTO_INCREMENT,
    user_level_enum INT(2) NOT NULL,
    user_level_description VARCHAR(30) NOT NULL,
    auth_type_id INT(3) NOT NULL,
    PRIMARY KEY (user_level_id)
) type=MyISAM;


CREATE TABLE IF NOT EXISTS auth_types (
    auth_type_id INT(3) NOT NULL AUTO_INCREMENT,
    auth_name VARCHAR(50),
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
