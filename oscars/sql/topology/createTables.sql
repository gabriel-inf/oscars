------------------------------------------------------------------------------
-- Copyright (c) 2006, The Regents of the University of California, through
-- Lawrence Berkeley National Laboratory (subject to receipt of any required
-- approvals from the U.S. Dept. of Energy). All rights reserved.
--
-- Redistribution and use in source and binary forms, with or without
-- modification, are permitted provided that the following conditions are met:
--
-- (1) Redistributions of source code must retain the above copyright notice,
--     this list of conditions and the following disclaimer.
--
-- (2) Redistributions in binary form must reproduce the above copyright
--     notice, this list of conditions and the following disclaimer in the
--     documentation and/or other materials provided with the distribution.
--
-- (3) Neither the name of the University of California, Lawrence Berkeley
--     National Laboratory, U.S. Dept. of Energy nor the names of its
--     contributors may be used to endorse or promote products derived from
--     this software without specific prior written permission.
--
-- THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
-- AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
-- IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
-- ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
-- LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
-- CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
-- SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
-- INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
-- CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
-- ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
-- POSSIBILITY OF SUCH DAMAGE.

-- You are under no obligation whatsoever to provide any bug fixes, patches,
-- or upgrades to the features, functionality or performance of the source
-- code ("Enhancements") to anyone; however, if you choose to make your
-- Enhancements available either publicly, or directly to Lawrence Berkeley
-- National Laboratory, without imposing a separate written license agreement
-- for such Enhancements, then you hereby grant the following license: a
-- non-exclusive, royalty-free perpetual license to install, use, modify,
-- prepare derivative works, incorporate into other computer software,
-- distribute, and sublicense such enhancements or derivative works thereof,
-- in binary and source code form.
------------------------------------------------------------------------------

CREATE DATABASE IF NOT EXISTS topology;
USE topology;

-- table for router description
CREATE TABLE IF NOT EXISTS routers (
    id			INT NOT NULL AUTO_INCREMENT,
    valid		BOOLEAN NOT NULL,
    name		TEXT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

-- table for interface description
-- would need trigger updating paths if changed
CREATE TABLE IF NOT EXISTS interfaces (
    id			INT NOT NULL AUTO_INCREMENT,
    valid		BOOLEAN NOT NULL,
        -- SNMP index
    snmpId		INT NOT NULL,
        -- bandwidth in bps
    speed		BIGINT UNSIGNED,
        -- description
    description		TEXT,
    alias		TEXT,
        -- key of corresponding router in routers table
    routerId		INT NOT NULL,	-- foreign key
    PRIMARY KEY (id)
) type=MyISAM;

-- table for router interface ip addresses
CREATE TABLE IF NOT EXISTS ipaddrs (
    id			INT NOT NULL AUTO_INCREMENT,
        -- IP address
    IP			TEXT NOT NULL,
        -- description (currently loopback, traceAddress, or NULL)
    description         TEXT,
        -- key of corresponding interface in interfaces table
    interfaceId		INT NOT NULL,	-- foreign key
    PRIMARY KEY (id)
) type=MyISAM;


-- table for intradomain paths of reservations
CREATE TABLE IF NOT EXISTS paths (
    id				INT NOT NULL AUTO_INCREMENT,
        -- if path still valid
    valid                       BOOLEAN NOT NULL,
        -- used for quick comparison to see if path already exists
    pathList			TEXT,
        -- number of times this path is referenced (inactive if 0)
    refCtr                      INT,
    PRIMARY KEY (id)
) type=MyISAM;

-- cross reference table
CREATE TABLE IF NOT EXISTS pathIpaddrs (
    pathId			INT NOT NULL,	-- foreign key
    ipaddrId			INT NOT NULL,	-- foreign key
       -- used to order path
    sequenceNumber              INT NOT NULL,
    PRIMARY KEY (pathId, ipaddrId)
) type=MyISAM;


-- Configuration tables ------------------------------------------

-- Configuration for route discovery
CREATE TABLE IF NOT EXISTS configTrace (
    id			INT NOT NULL AUTO_INCREMENT,
      -- traceroute information section
        -- source of traceroutes
    jnxSource		TEXT NOT NULL,
        -- SSH variables to use for Juniper login
    jnxUser		TEXT NOT NULL,
    jnxKey 		TEXT NOT NULL,
        -- traceroute configuration variables
    ttl			INT NOT NULL,
    timeout		INT NOT NULL,
        -- Whether to run traceroute from the routers
        -- (requires ssh keys setup)
    runTrace		BOOLEAN NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

-- Configuration for SNMP queries
CREATE TABLE IF NOT EXISTS configSNMP (
    id			INT NOT NULL AUTO_INCREMENT,
        -- community string (TODO:  secure enough here?
    community		TEXT NOT NULL,
    version		TEXT NOT NULL,
    domainSuffix	TEXT NOT NULL,
    port		INT NOT NULL,
    timeout		INT NOT NULL,
    retries		INT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

-- Configuration for LSP setup and teardown
CREATE TABLE IF NOT EXISTS configPSS (
    id			INT NOT NULL AUTO_INCREMENT,
    access		TEXT NOT NULL,
    login		TEXT NOT NULL,
    password		TEXT NOT NULL,
    firewallMarker	TEXT NOT NULL,
    setupFile		TEXT NOT NULL,
    teardownFile	TEXT NOT NULL,
    externalInterfaceFilter	TEXT NOT NULL,
    CoS			INT NOT NULL,
    dscp		TEXT NOT NULL,
        -- in bps
    burstLimit		INT NOT NULL,
    setupPriority	INT NOT NULL,
    reservationPriority	INT NOT NULL,
        -- allow LSP configuration
    allowLSP		BOOLEAN NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;
