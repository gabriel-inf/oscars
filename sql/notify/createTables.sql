-- MySQL dump 10.9
--
-- Host: localhost    Database: notify
-- ------------------------------------------------------
-- Server version	4.1.20

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `subscriptionFilters`
--

DROP TABLE IF EXISTS `subscriptionFilters`;
CREATE TABLE `subscriptionFilters` (
  `id` int(11) NOT NULL auto_increment,
  `subscriptionId` int(11) NOT NULL default '0',
  `type` text NOT NULL,
  `value` text,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `subscriptionFilters`
--


/*!40000 ALTER TABLE `subscriptionFilters` DISABLE KEYS */;
LOCK TABLES `subscriptionFilters` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `subscriptionFilters` ENABLE KEYS */;

--
-- Table structure for table `subscriptions`
--

DROP TABLE IF EXISTS `subscriptions`;
CREATE TABLE `subscriptions` (
  `id` int(11) NOT NULL auto_increment,
  `referenceId` text NOT NULL,
  `userLogin` text NOT NULL,
  `url` text NOT NULL,
  `createdTime` bigint(20) NOT NULL default '0',
  `terminationTime` bigint(20) NOT NULL default '0',
  `status` int(11) NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `publishers`
--

DROP TABLE IF EXISTS `publishers`;
CREATE TABLE `publishers` (
  `id` int(11) NOT NULL auto_increment,
  `referenceId` text NOT NULL,
  `userLogin` text NOT NULL,
  `url` text NOT NULL,
  `createdTime` bigint(20) NOT NULL default '0',
  `terminationTime` bigint(20) NOT NULL default '0',
  `demand` tinyint(1) NOT NULL default '0',
  `status` int(11) NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `subscriptions`
--


/*!40000 ALTER TABLE `subscriptions` DISABLE KEYS */;
LOCK TABLES `subscriptions` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `subscriptions` ENABLE KEYS */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

