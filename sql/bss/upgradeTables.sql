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

--
-- Table to look up an institution associated with a domain (for site admin
-- privileges)
CREATE TABLE IF NOT EXISTS sites (
    id                  INT NOT NULL AUTO_INCREMENT,
        -- site name, matches a name in the aaa institutions table
    name                TEXT NOT NULL,
        -- key of corresponding domain in domains table
    domainId            INT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;
