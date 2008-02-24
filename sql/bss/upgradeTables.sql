USE bss;

-- add interPathElem to path so interdomain paths can be stored
ALTER TABLE paths ADD interPathElemId INT UNIQUE AFTER pathElemId;

--
-- Table for static LIDP entries
--
CREATE TABLE IF NOT EXISTS staticLIDP (
  id INT NOT NULL AUTO_INCREMENT,
  localLinkId INT NOT NULL,
  destDomainId INT,
  destNodeId INT,
  destPortId INT,
  destLinkId INT,
  defaultRoute TINYINT(1) DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=MyISAM;
