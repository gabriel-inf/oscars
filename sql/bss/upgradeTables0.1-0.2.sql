-- upgrade bss tables from release 0.1 to 0.2

USE bss;

-- add interPathElem to path so interdomain paths can be stored
ALTER TABLE paths ADD interPathElemId INT UNIQUE AFTER pathElemId;

--
-- Table for interdomain routes
--
CREATE TABLE IF NOT EXISTS interdomainRoutes (
  id INT NOT NULL AUTO_INCREMENT,
  srcNodeId INT,
  srcPortId INT,
  srcLinkId INT,
  destDomainId INT,
  destNodeId INT,
  destPortId INT,
  destLinkId INT,
  routeElemId INT NOT NULL,
  preference INT NOT NULL DEFAULT 100,
  defaultRoute BOOLEAN NOT NULL,
  PRIMARY KEY (id)
) ENGINE=MyISAM;

--
-- Table for route elements referenced by a routing table
--
CREATE TABLE IF NOT EXISTS routeElems (
  id INT NOT NULL AUTO_INCREMENT,
  -- one of the following 4 must be populated
  domainId INT,
  nodeId INT,
  portId INT,
  linkId INT,
  nextHopId INT UNIQUE,
  description TEXT,
  strict BOOLEAN NOT NULL,
  PRIMARY KEY (id)
) ENGINE=MyISAM;


