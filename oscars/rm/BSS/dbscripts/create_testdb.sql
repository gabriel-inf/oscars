CREATE DATABASE IF NOT EXISTS BSStest;
USE BSStest;

-- for example, ESnet
CREATE TABLE IF NOT EXISTS networks (
    network_id INT(3) NOT NULL AUTO_INCREMENT,
    network_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (network_id)
) type=MyISAM;

-- this is a router description
CREATE TABLE IF NOT EXISTS routers (
    router_id INT(5) NOT NULL AUTO_INCREMENT,
    router_valid BOOLEAN NOT NULL,
    router_name VARCHAR(50) NOT NULL,
    -- should be the loop back interface name
    router_loopback VARCHAR(50) NOT NULL,
    network_id INT(3) NOT NULL,
    PRIMARY KEY (router_id)
) type=MyISAM;

-- would need trigger updating paths if changed
CREATE TABLE IF NOT EXISTS interfaces (
    interface_id INT(5) NOT NULL AUTO_INCREMENT,
    interface_valid BOOLEAN NOT NULL,
    interface_speed INT(15) UNSIGNED NOT NULL,
    interface_descr VARCHAR(5),
    interface_alias VARCHAR(512),
    router_id INT(5) NOT NULL,
    PRIMARY KEY (interface_id)
) type=MyISAM;

-- table to hodl the router ip address
CREATE TABLE IF NOT EXISTS ipaddrs (
    ipaddrs_id INT(5) NOT NULL AUTO_INCREMENT,
    ipaddrs_ip VARCHAR(39) NOT NULL,
    interface_id INT(5) NOT NULL,
    PRIMARY KEY (ipaddrs_id)
) type=MyISAM;

-- table to hold the end host ip address
CREATE TABLE IF NOT EXISTS hostaddrs (
    hostaddrs_id INT(5) NOT NULL AUTO_INCREMENT,
    hostaddrs_ip VARCHAR(39) NOT NULL,
    PRIMARY KEY (hostaddrs_id)
) type=MyISAM;

-- this info is passed to the PSS
CREATE TABLE IF NOT EXISTS reservations (
    reservation_id INT(5) NOT NULL AUTO_INCREMENT,
    -- in time since epoch in seconds (UTC)
    reservation_start_time INT(9) NOT NULL,
    reservation_end_time INT(9) NOT NULL,
    reservation_created_time INT(9) NOT NULL,
    -- required for PSS module
    reservation_bandwidth VARCHAR(50) NOT NULL,
    reservation_class   VARCHAR(20) NOT NULL,
    reservation_burst_limit INT(9) NOT NULL,
    reservation_status VARCHAR(12) NOT NULL,
    ingress_interface_id INT(5) NOT NULL,
    egress_interface_id INT(5) NOT NULL,
    src_hostaddrs_id INT(5) NOT NULL, 
    dst_hostaddrs_id INT(5) NOT NULL, 
    user_dn VARCHAR(80) NOT NULL,
    -- this are applicaion ports, optional
    reservation_ingress_port INT(5),
    reservation_egress_port INT(5),
    -- more optional stuff
    reservation_dscp VARCHAR(8),
    reservation_description VARCHAR(30),
    PRIMARY KEY (reservation_id)
) type = MyISAM;
