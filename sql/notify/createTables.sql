CREATE DATABASE IF NOT EXISTS notify;
USE notify;

-- Table to hold filters for each subscription
--
CREATE TABLE IF NOT EXISTS subscriptionFilters (
  id int(11) NOT NULL auto_increment,
  subscriptionId int(11) NOT NULL default '0',
  type text NOT NULL,
  value text,
  PRIMARY KEY  (id)
) type=MyISAM;

--
-- Table of subscriptions to notifications
--
CREATE TABLE IF NOT EXISTS subscriptions (
  id int(11) NOT NULL auto_increment,
  referenceId text NOT NULL,
  userLogin text NOT NULL,
  url text NOT NULL,
  createdTime bigint(20) NOT NULL default '0',
  terminationTime bigint(20) NOT NULL default '0',
  status int(11) NOT NULL default '0',
  PRIMARY KEY  (id)
) type=MyISAM;

--
-- Table of registered publishers that send notifications 
-- to broker for forwarding
--
CREATE TABLE IF NOT EXISTS publishers (
  id int(11) NOT NULL auto_increment,
  referenceId text NOT NULL,
  userLogin text NOT NULL,
  url text NOT NULL,
  createdTime bigint(20) NOT NULL default '0',
  terminationTime bigint(20) NOT NULL default '0',
  demand tinyint(1) NOT NULL default '0',
  status int(11) NOT NULL default '0',
  PRIMARY KEY  (id)
) type=MyISAM;

--
-- Table with info about external services
--
CREATE TABLE IF NOT EXISTS externalServices (
  id int(11) NOT NULL auto_increment,
  type                TEXT NOT NULL,
  url                 TEXT NOT NULL,
  serviceKey          TEXT,
  PRIMARY KEY  (id)
) type=MyISAM;
