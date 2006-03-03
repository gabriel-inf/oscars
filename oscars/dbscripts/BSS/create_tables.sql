CREATE DATABASE IF NOT EXISTS BSS;
USE BSS;

-- table for administrative domain, e.g. ESnet
CREATE TABLE IF NOT EXISTS domains (
    domain_id			INT NOT NULL AUTO_INCREMENT,
    domain_as_num               INT NOT NULL,
    domain_name			TEXT NOT NULL,
        -- whether OSCARS server handles this domain
    local_domain		BOOLEAN NOT NULL,
    PRIMARY KEY (domain_id)
) type=MyISAM;

-- table for router description
CREATE TABLE IF NOT EXISTS routers (
    router_id			INT NOT NULL AUTO_INCREMENT,
    router_valid		BOOLEAN NOT NULL,
    router_name			TEXT NOT NULL,
        -- loopback interface IP, if present
    router_loopback		TEXT,
        -- key of corresponding AS in domains table
    domain_id			INT NOT NULL,	-- foreign key
    PRIMARY KEY (router_id)
) type=MyISAM;

-- table for interface description
-- would need trigger updating paths if changed
CREATE TABLE IF NOT EXISTS interfaces (
    interface_id		INT NOT NULL AUTO_INCREMENT,
    interface_valid		BOOLEAN NOT NULL,
        -- SNMP index
    interface_snmp_id           INT NOT NULL,
        -- bandwidth in bps
    interface_speed		BIGINT UNSIGNED,
        -- description
    interface_descr		TEXT,
    interface_alias		TEXT,
        -- key of corresponding router in routers table
    router_id			INT NOT NULL,	-- foreign key
    PRIMARY KEY (interface_id)
) type=MyISAM;

-- table for router interface ip addresses
CREATE TABLE IF NOT EXISTS ipaddrs (
    ipaddr_id			INT NOT NULL AUTO_INCREMENT,
        -- IP address
    ipaddr_ip			TEXT NOT NULL,
        -- key of corresponding interface in interfaces table
    interface_id		INT NOT NULL,	-- foreign key
    PRIMARY KEY (ipaddr_id)
) type=MyISAM;

-- table for source and destination IP addresses
CREATE TABLE IF NOT EXISTS hosts (
    host_id			INT NOT NULL AUTO_INCREMENT,
    host_ip			TEXT NOT NULL,
    PRIMARY KEY (host_id)
) type=MyISAM;

-- table holding reservation information
-- this information is passed to the PSS
CREATE TABLE IF NOT EXISTS reservations (
    reservation_id	INT NOT NULL AUTO_INCREMENT,
      -- times will be in seconds since epoch (UTC)
    reservation_start_time	DATETIME NOT NULL,
    reservation_end_time	DATETIME NOT NULL,
        -- time this entry was created
    reservation_created_time	DATETIME NOT NULL,
        -- client's time zone
    reservation_time_zone	TEXT NOT NULL,
        -- bandwidth requested (Mbps)
    reservation_bandwidth	BIGINT UNSIGNED NOT NULL,
        -- in Mbps
    reservation_burst_limit	BIGINT UNSIGNED NOT NULL,
        -- user making the reservation
    user_dn			TEXT NOT NULL,
        -- pending, active, failed, precancel, or cancelled
    reservation_status		TEXT NOT NULL,
    reservation_class		TEXT NOT NULL,
      -- the following are optional fields
        -- source and destination ports
    reservation_src_port	SMALLINT UNSIGNED,
    reservation_dst_port	SMALLINT UNSIGNED,
        -- differentiated services code point
    reservation_dscp		TEXT,
        -- protocol used (0-255, or a protocol string, such as udp)
    reservation_protocol	TEXT,
        -- human readable identifier
    reservation_tag		TEXT,
        -- space separated list of addresses of routers in path
        -- This will be normalized by adding a paths table
    reservation_path		TEXT,
    reservation_description	TEXT,
      -- foreign keys (not optional)
        -- keys of ingress and egress interfaces in interfaces table
    ingress_interface_id	INT NOT NULL,	-- foreign key
    egress_interface_id		INT NOT NULL,	-- foreign key
        -- keys of source and destination addresses in hosts table
    src_host_id			INT NOT NULL,   -- foreign key 
    dst_host_id			INT NOT NULL,	-- foreign key 
    PRIMARY KEY (reservation_id)
) type = MyISAM;


-- Following three tbbles are for configuration variables that can exist on a per
-- reservation basis (one row is for the defaults).  The defaults are used
-- if the foreign key is null in the reservation.  The defaults can
-- only be overriden by a user with engineer privileges.

-- Configuration for scheduler
CREATE TABLE IF NOT EXISTS scheduler_confs (
    scheduler_conf_id		INT NOT NULL AUTO_INCREMENT,
        -- Time (in seconds) between polling the reservation db
    scheduler_db_poll_time	INT NOT NULL,
        -- Time interval (in seconds) to search for reservations 
        -- to schedule must be larger then db_poll time
    scheduler_time_interval	INT NOT NULL,
    PRIMARY KEY (scheduler_conf_id)
) type=MyISAM;

-- Configuration for route discovery
CREATE TABLE IF NOT EXISTS trace_confs (
    trace_conf_id		INT NOT NULL AUTO_INCREMENT,
      -- traceroute information section
        -- source of traceroutes
    trace_conf_jnx_source	TEXT NOT NULL,
        -- SSH variables to use for Juniper login
    trace_conf_jnx_user		TEXT NOT NULL,
    trace_conf_jnx_key 		TEXT NOT NULL,
        -- traceroute configuration variables
    trace_conf_ttl		INT NOT NULL,
    trace_conf_timeout		INT NOT NULL,
        -- Whether to run traceroute from the routers
        -- (requires ssh keys setup)
    trace_conf_run_trace	BOOLEAN NOT NULL,
    PRIMARY KEY (trace_conf_id)
) type=MyISAM;

-- Configuration for SNMP queries
CREATE TABLE IF NOT EXISTS snmp_confs (
    snmp_conf_id		INT NOT NULL AUTO_INCREMENT,
        -- community string (TODO:  secure enough here?
    snmp_conf_community		TEXT NOT NULL,
    snmp_conf_version		TEXT NOT NULL,
    snmp_conf_port		INT NOT NULL,
    snmp_conf_timeout		INT NOT NULL,
    snmp_conf_retries		INT NOT NULL,
    PRIMARY KEY (snmp_conf_id)
) type=MyISAM;

-- Configuration for LSP setup and teardown
CREATE TABLE IF NOT EXISTS pss_confs (
    pss_conf_id			INT NOT NULL AUTO_INCREMENT,
        -- Router access
    pss_conf_access		TEXT NOT NULL,
    pss_conf_login		TEXT NOT NULL,
    pss_conf_passwd		TEXT NOT NULL,
        -- XML related variables.
    pss_conf_firewall_marker	TEXT NOT NULL,
    pss_conf_setup_file		TEXT NOT NULL,
    pss_conf_teardown_file	TEXT NOT NULL,
    pss_conf_ext_if_filter	TEXT NOT NULL,
        -- LSP values.
    pss_conf_CoS		INT NOT NULL,
        -- in bps
    pss_conf_burst_limit	INT NOT NULL,
    pss_conf_setup_priority	INT NOT NULL,
    pss_conf_resv_priority	INT NOT NULL,
        -- allow LSP configuration
    pss_conf_allow_lsp		BOOLEAN NOT NULL,
    PRIMARY KEY (pss_conf_id)
) type=MyISAM;


-- Table holding archived reservation information.  Foreign keys are replaced
-- by what they reference, since router information may change over time.
CREATE TABLE IF NOT EXISTS archived_reservations (
    -- Note that this is the id of the row in the original reservations
    -- table.
    reservation_id	        INT NOT NULL,
      -- times will be in seconds since epoch (UTC)
    reservation_start_time	DATETIME NOT NULL,
    reservation_end_time	DATETIME NOT NULL,
        -- time this entry was created
    reservation_created_time	DATETIME NOT NULL,
        -- client's time zone
    reservation_time_zone	TEXT NOT NULL,
        -- bandwidth requested (Mbps)
    reservation_bandwidth	BIGINT UNSIGNED NOT NULL,
        -- in Mbps
    reservation_burst_limit	BIGINT UNSIGNED NOT NULL,
        -- user making the reservation
    user_dn			TEXT NOT NULL,
        -- pending, active, failed, precancel, or cancelled
    reservation_status		TEXT NOT NULL,
    reservation_class		TEXT NOT NULL,
      -- the following are optional fields
        -- source and destination ports
    reservation_src_port	SMALLINT UNSIGNED,
    reservation_dst_port	SMALLINT UNSIGNED,
        -- differentiated services code point
    reservation_dscp		TEXT,
        -- protocol used (0-255, or a protocol string, such as udp)
    reservation_protocol	TEXT,
        -- human readable identifier
    reservation_tag		TEXT,
        -- space separated list of addresses of routers in path
    reservation_path		TEXT,
    reservation_description	TEXT,
    archived_ingress_router	TEXT NOT NULL,
    archived_egress_router	TEXT NOT NULL,
    archived_source_host	TEXT NOT NULL, 
    archived_destination_host	TEXT NOT NULL,
    PRIMARY KEY (reservation_id)
) type = MyISAM;


