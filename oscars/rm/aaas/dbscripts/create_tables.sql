CREATE TABLE IF NOT EXISTS users (
    user_id INT(6) NOT NULL AUTO_INCREMENT,
    user_last_name VARCHAR(50) NOT NULL,
    user_first_name VARCHAR(50),
        -- for now
    user_login_name VARCHAR(25),
    user_password varchar(50),
    user_email_primary VARCHAR(100) NOT NULL,
    user_email_secondary VARCHAR(100),
    user_phone_primary VARCHAR(50),
    user_phone_secondary VARCHAR(50),
    user_description TEXT,
    user_level TINYINT UNSIGNED NOT NULL,
    user_register_time DATETIME,
    user_activation_key VARCHAR(40),
    user_pending_level TINYINT UNSIGNED,
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

-- for example, ESnet
CREATE TABLE IF NOT EXISTS networks (
    network_id INT(3) NOT NULL AUTO_INCREMENT,
    network_name VARCHAR(50) NOT NULL,
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
    interface_descr VARCHAR(5),
    interface_alias VARCHAR(512),
    router_id INT(5) NOT NULL,
    PRIMARY KEY (interface_id)
) type=MyISAM;

CREATE TABLE IF NOT EXISTS ipaddrs (
    ipaddrs_id INT(5) NOT NULL AUTO_INCREMENT,
    interface INT(5) NOT NULL,
    PRIMARY KEY (ipaddrs_id)
) type=MyISAM;

CREATE TABLE IF NOT EXISTS reservations (
    reservation_id INT(5) NOT NULL AUTO_INCREMENT,
      -- in GMT
    reservation_start_time DATETIME,
    reservation_end_time DATETIME,
    reservation_qos VARCHAR(50),
      -- need list of statuses to choose from
    reservation_status VARCHAR(12),
    reservation_description TEXT,
    reservation_created_time DATETIME NOT NULL,
    reservation_ingress_port INT(5) NOT NULL,
    reservation_egress_port INT(5) NOT NULL,
    ingress_interface_id INT(5) NOT NULL,
    egress_interface_id INT(5) NOT NULL,
    user_id INT(6) NOT NULL,
    PRIMARY KEY (reservation_id)
) type = MyISAM;

CREATE TABLE IF NOT EXISTS allocations (
    allocation_id INT(5) NOT NULL AUTO_INCREMENT,
        -- in seconds
    allocation_amount INT(9),
    allocation_used INT(9),
    user_id INT(6) NOT NULL,
    PRIMARY KEY (allocation_id)
) type = MyISAM;
