CREATE TABLE IF NOT EXISTS users (
    user_id INT(6) NOT NULL AUTO_INCREMENT,
    user_surname VARCHAR(50) NOT NULL,
    user_first_name VARCHAR(50),
    user_token varchar(2048),
    authorization_id INT(6),    -- foreign key
    institution_id INT(6),      -- foreign key
    PRIMARY KEY (user_id)
) type=MyISAM;

CREATE TABLE IF NOT EXISTS institutions (
    institution_id INT(5) NOT NULL AUTO_INCREMENT,
    institution_name VARCHAR(50),
    PRIMARY KEY (institution_id)
) type=MyISAM;

CREATE TABLE IF NOT EXISTS authorizations (
    authorization_id INT(5) NOT NULL AUTO_INCREMENT,
    authorization_data VARCHAR(512) NOT NULL,
    auth_type_id INT(3) NOT NULL,
    PRIMARY KEY (authorization_id)
) type=MyISAM;

CREATE TABLE IF NOT EXISTS auth_types (
    auth_type_id INT(3) NOT NULL AUTO_INCREMENT,
    auth_name VARCHAR(50),
    PRIMARY KEY (auth_type_id)
) type=MyISAM;

CREATE TABLE IF NOT EXISTS isps (
    isp_id INT(3) NOT NULL AUTO_INCREMENT,
    isp_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (isp_id)
) type=MyISAM;

CREATE TABLE IF NOT EXISTS networks (
    network_id INT(3) NOT NULL AUTO_INCREMENT,
    network_name VARCHAR(50) NOT NULL,
    isp_id INT(3) NOT NULL,
    PRIMARY KEY (network_id)
) type=MyISAM;

CREATE TABLE IF NOT EXISTS routers (
    router_id INT(5) NOT NULL AUTO_INCREMENT,
    router_valid BOOLEAN NOT NULL,
    router_name VARCHAR(50) NOT NULL,
    router_loopback VARCHAR(50) NOT NULL,
    network_id INT(3) NOT NULL,
    PRIMARY KEY (router_id)
) type=MyISAM;

-- would need trigger updating paths if changed

CREATE TABLE IF NOT EXISTS interfaces (
    interface_id INT(5) NOT NULL AUTO_INCREMENT,
    interface_valid BOOLEAN NOT NULL,
    interface_speed INT(15) NOT NULL,
    interface_ip INT(4) NOT NULL,
    interface_descr VARCHAR(5),
    interface_alias VARCHAR(512),
    router_id INT(5) NOT NULL,
    PRIMARY KEY (interface_id)
) type=MyISAM;

CREATE TABLE IF NOT EXISTS paths (
    path_id INT(5) NOT NULL AUTO_INCREMENT,
    path_index INT(5) NOT NULL,
    interface_id INT(5) NOT NULL,
    reservation_id INT(5) NOT NULL,
    PRIMARY KEY (path_id)
) type=MyISAM;

CREATE TABLE IF NOT EXISTS reservations (
    reservation_id INT(5) NOT NULL AUTO_INCREMENT,
    reservation_start DATETIME,
    reservation_end DATETIME,
    user_id INT(6) NOT NULL,
    PRIMARY KEY (reservation_id)
) type = MyISAM;

CREATE TABLE IF NOT EXISTS reservation_requests (
    reservation_request_id INT(5) NOT NULL AUTO_INCREMENT,
    reservation_request_start DATETIME,
    reservation_request_end DATETIME,
    reservation_request_qos VARCHAR(50),
    user_id INT(6) NOT NULL,
    PRIMARY KEY (reservation_request_id)
) type = MyISAM;

CREATE TABLE IF NOT EXISTS allocations (
    allocation_id INT(5) NOT NULL AUTO_INCREMENT,
        -- in seconds
    allocation_amount INT(9),
    allocation_used INT(9),
    user_id INT(6) NOT NULL,
    PRIMARY KEY (allocation_id)
) type = MyISAM;
