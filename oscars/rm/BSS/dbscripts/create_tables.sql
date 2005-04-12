CREATE DATABASE IF NOT EXISTS BSS;
USE BSS;

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
    ipaddrs_ip VARCHAR(30) NOT NULL,
    interface_id INT(5) NOT NULL,
    PRIMARY KEY (ipaddrs_id)
) type=MyISAM;

CREATE TABLE IF NOT EXISTS reservations (
    reservation_id INT(5) NOT NULL AUTO_INCREMENT,
      -- in time since epoch in seconds (UTC)
    reservation_start_time INT(9),
    reservation_end_time INT(9),
    reservation_qos VARCHAR(50),
      -- need list of statuses to choose from
    reservation_status VARCHAR(12),
    reservation_description TEXT,
    reservation_created_time INT(9) NOT NULL,
    reservation_ingress_port INT(5) NOT NULL,
    reservation_egress_port INT(5) NOT NULL,
    ingress_interface_id INT(5) NOT NULL,
    egress_interface_id INT(5) NOT NULL,
    user_dn VARCHAR(60) NOT NULL,
    PRIMARY KEY (reservation_id)
) type = MyISAM;
