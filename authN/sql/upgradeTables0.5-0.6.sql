-- Script to create the authN database and tables and populate them with
-- data from the 0.5 aaa database.
-- If you do not have 0.5 data, use createTable.sql instead.
-- NOTE:  This needs to be run as mysql -u root -p < upgradeTables0.5-0.6.sql.  Can only be run once

CREATE DATABASE IF NOT EXISTS authn;
CREATE DATABASE IF NOT EXISTS testauthn;

GRANT select, insert, update, delete ON `authn`.* TO 'oscars'@'localhost';
GRANT select, insert, update, delete, create, drop, alter on `testauthn`.* TO 'oscars'@'localhost';

USE authn;

CREATE TABLE users LIKE aaa.users;
INSERT INTO users SELECT * from aaa.users;

CREATE TABLE userAttributes LIKE aaa.userAttributes;
INSERT INTO userAttributes SELECT * from aaa.userAttributes;

CREATE TABLE institutions LIKE aaa.institutions;
INSERT INTO institutions SELECT * from aaa.institutions;

CREATE TABLE attributes LIKE aaa.attributes;
INSERT INTO attributes SELECT * from aaa.attributes;

ALTER TABLE attributes CHANGE attrType attrId text not null after id;
ALTER TABLE attributes CHANGE name value text not null;

