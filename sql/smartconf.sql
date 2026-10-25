-- MySQL dump 10.13  Distrib 5.1.73, for redhat-linux-gnu (i386)
--
-- Host: localhost    Database: smartconf
-- ------------------------------------------------------
-- Server version	5.1.73

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cc_core_domain`
--

DROP TABLE IF EXISTS `cc_core_domain`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc_core_domain` (
  `rid` int(11) NOT NULL AUTO_INCREMENT,
  `domain_id` varchar(8) NOT NULL,
  `domain_name` varchar(16) NOT NULL,
  `par_domain_id` varchar(8) DEFAULT NULL,
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc_core_domain`
--

LOCK TABLES `cc_core_domain` WRITE;
/*!40000 ALTER TABLE `cc_core_domain` DISABLE KEYS */;
INSERT INTO `cc_core_domain` VALUES (1,'1','顶级域','0'),(2,'2','呼叫中心一部','1'),(3,'3','监控组','1'),(4,'4','班组1','2'),(5,'5','班组2','2'),(6,'6','北京中心组','2'),(7,'7','班组3','2'),(8,'8','班组4','2');
/*!40000 ALTER TABLE `cc_core_domain` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cc_core_extension`
--

DROP TABLE IF EXISTS `cc_core_extension`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc_core_extension` (
  `rid` int(11) NOT NULL AUTO_INCREMENT,
  `extn_dn` varchar(32) DEFAULT NULL,
  `extn_type` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc_core_extension`
--

LOCK TABLES `cc_core_extension` WRITE;
/*!40000 ALTER TABLE `cc_core_extension` DISABLE KEYS */;
INSERT INTO `cc_core_extension` VALUES (4,'3001-3060','2'),(5,'7001-7120','7');
/*!40000 ALTER TABLE `cc_core_extension` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cc_core_fileinfo`
--

DROP TABLE IF EXISTS `cc_core_fileinfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc_core_fileinfo` (
  `file_id` int(8) NOT NULL AUTO_INCREMENT,
  `file_path` varchar(300) DEFAULT NULL,
  `file_name` varchar(300) DEFAULT NULL,
  `file_type` varchar(30) DEFAULT NULL,
  `file_desc` varchar(300) DEFAULT NULL,
  `create_date` varchar(30) DEFAULT NULL,
  `modify_date` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`file_id`)
) ENGINE=InnoDB AUTO_INCREMENT=240 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc_core_fileinfo`
--

LOCK TABLES `cc_core_fileinfo` WRITE;
/*!40000 ALTER TABLE `cc_core_fileinfo` DISABLE KEYS */;
INSERT INTO `cc_core_fileinfo` VALUES (1,'/usr/src/smartcc/resources/wav/','SystemBusy.vox','VOICE','','2014-10-25 09:54:53',NULL),(3,'/usr/src/smartcc/resources/wav/','hy_busy.wav','VOICE','','2014-10-25 09:54:53',NULL),(5,'/usr/src/smartcc/resources/wav/','hy_busy.vox','VOICE','','2014-10-25 09:54:53',NULL),(6,'/usr/src/smartcc/resources/wav/','hy_music_1_min.wav','VOICE','','2014-10-25 09:54:53',NULL),(7,'/usr/src/smartcc/resources/wav/','hy_music_1_min.vox','VOICE','','2014-10-25 09:54:53',NULL),(8,'/usr/src/smartcc/resources/wav/','SystemBusy.wav','VOICE','','2014-10-25 09:54:53',NULL),(11,'/usr/src/smartcc/resources/vxml/','App.vxml','FLOW','','2014-10-25 09:54:53',NULL),(14,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.01.23.11.25.13.bak','FLOW','','2015-01-24 15:29:18',NULL),(15,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.01.23.10.50.36.bak','FLOW','','2015-01-24 15:29:18',NULL),(16,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.01.23.11.25.49.bak','FLOW','','2015-01-24 15:29:18',NULL),(17,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.01.23.11.22.45.bak','FLOW','','2015-01-24 15:29:18',NULL),(18,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.01.23.10.29.50.bak','FLOW','','2015-01-24 15:29:18',NULL),(19,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.01.26.11.16.00.bak','FLOW','','2015-01-26 12:02:57',NULL),(40,'/usr/src/smartcc/resources/vxml/','01234567890.vxml','FLOW',NULL,'2015-02-04 19:15:35',NULL),(41,'/usr/src/smartcc/resources/wav/','welcome.wav','VOICE','welcome.wav','2015-02-06 11:24:31',NULL),(42,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.02.10.13.16.27.bak','FLOW','','2015-02-10 13:18:08',NULL),(44,'/usr/src/smartcc/resources/wav/','service.wav','VOICE','','2015-02-10 13:18:27',NULL),(48,'/usr/src/smartcc/resources/wav/','otherConsule.wav','VOICE',NULL,'2015-02-10 13:19:38',NULL),(49,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.03.18.14.55.52.bak','FLOW','','2015-03-18 15:03:57',NULL),(50,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.03.18.15.05.29.bak','FLOW','','2015-03-18 15:08:38',NULL),(51,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.03.18.15.09.25.bak','FLOW','','2015-03-18 15:29:55',NULL),(52,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.03.18.15.30.13.bak','FLOW','','2015-03-18 15:47:07',NULL),(69,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.03.25.15.37.43.bak','FLOW','','2015-03-25 15:58:28',NULL),(75,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.03.30.11.35.06.bak','FLOW','','2015-03-30 11:35:31',NULL),(76,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.03.30.11.50.03.bak','FLOW','','2015-03-30 11:50:25',NULL),(77,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.03.30.11.55.34.bak','FLOW','','2015-03-30 11:56:11',NULL),(78,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.03.30.11.58.33.bak','FLOW','','2015-03-30 11:58:56',NULL),(79,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.03.30.11.59.46.bak','FLOW','','2015-03-30 12:00:16',NULL),(80,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.03.30.12.01.02.bak','FLOW','','2015-03-30 12:13:47',NULL),(81,'/usr/src/smartcc/resources/vxml/','1234567890.vxml.2015.04.03.09.54.49.bak','FLOW','','2015-04-03 09:58:42',NULL),(83,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.04.13.15.55.02.bak','FLOW','','2015-04-13 15:56:59',NULL),(89,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.04.13.18.39.02.bak','FLOW','','2015-04-13 18:41:38',NULL),(90,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.04.13.18.42.08.bak','FLOW','','2015-04-14 09:09:56',NULL),(92,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.04.14.09.11.52.bak','FLOW','','2015-04-14 09:15:33',NULL),(93,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.04.14.09.12.03.bak','FLOW','','2015-04-14 09:15:33',NULL),(98,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.04.16.11.11.17.bak','FLOW','','2015-04-16 11:11:33',NULL),(99,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.04.17.13.25.45.bak','FLOW','','2015-04-17 13:26:51',NULL),(101,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.04.17.14.10.21.bak','FLOW','','2015-04-17 14:11:05',NULL),(103,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.04.17.14.27.16.bak','FLOW','','2015-04-17 14:52:31',NULL),(105,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.04.17.14.53.15.bak','FLOW','','2015-04-17 15:02:49',NULL),(106,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.04.17.15.04.34.bak','FLOW','','2015-04-17 15:05:07',NULL),(107,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.04.17.15.08.58.bak','FLOW','','2015-04-17 15:09:02',NULL),(108,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.04.21.13.12.18.bak','FLOW','','2015-04-21 13:12:42',NULL),(111,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.04.28.14.56.53.bak','FLOW','','2015-04-28 14:57:24',NULL),(112,'/usr/src/smartcc/resources/wav/','wait.wav','VOICE',NULL,'2015-04-28 14:57:44',NULL),(114,'/usr/src/smartcc/resources/wav/','waitnew.wav','VOICE',NULL,'2015-04-28 14:58:00',NULL),(116,'/usr/src/smartcc/resources/vxml/','ipcc.vxml','FLOW',NULL,'2015-04-28 15:02:54',NULL),(119,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.09.43.26.bak','FLOW','','2015-05-06 09:44:06',NULL),(120,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.09.57.06.bak','FLOW','','2015-05-06 09:58:27',NULL),(121,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.09.58.47.bak','FLOW','','2015-05-06 09:59:12',NULL),(122,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.10.00.39.bak','FLOW','','2015-05-06 10:01:04',NULL),(123,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.10.01.32.bak','FLOW','','2015-05-06 10:02:35',NULL),(124,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.10.12.59.bak','FLOW','','2015-05-06 10:13:51',NULL),(125,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.06.10.31.21.bak','FLOW','','2015-05-06 10:41:51',NULL),(126,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.06.10.42.51.bak','FLOW','','2015-05-06 10:44:40',NULL),(127,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.06.10.47.03.bak','FLOW','','2015-05-06 10:50:12',NULL),(128,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.06.10.51.25.bak','FLOW','','2015-05-06 10:53:01',NULL),(129,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.06.10.54.05.bak','FLOW','','2015-05-06 10:55:41',NULL),(130,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.06.11.00.12.bak','FLOW','','2015-05-06 11:00:37',NULL),(131,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.06.11.01.41.bak','FLOW','','2015-05-06 11:02:03',NULL),(132,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.06.11.03.03.bak','FLOW','','2015-05-06 11:04:02',NULL),(133,'/usr/src/smartcc/resources/wav/','goodbye.wav','VOICE',NULL,'2015-05-06 11:04:16',NULL),(134,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.06.11.10.36.bak','FLOW','','2015-05-06 11:11:54',NULL),(135,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.11.27.08.bak','FLOW','','2015-05-06 11:27:55',NULL),(136,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.11.28.31.bak','FLOW','','2015-05-06 11:35:14',NULL),(137,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.15.52.57.bak','FLOW','','2015-05-06 15:54:32',NULL),(138,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.16.04.41.bak','FLOW','','2015-05-06 16:05:33',NULL),(139,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.16.06.21.bak','FLOW','','2015-05-06 16:08:53',NULL),(140,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.16.10.49.bak','FLOW','','2015-05-06 16:11:28',NULL),(141,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.16.12.38.bak','FLOW','','2015-05-06 16:13:32',NULL),(142,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.16.19.55.bak','FLOW','','2015-05-06 16:20:23',NULL),(143,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.16.22.29.bak','FLOW','','2015-05-06 16:23:07',NULL),(144,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.16.26.04.bak','FLOW','','2015-05-06 16:26:45',NULL),(145,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.16.27.04.bak','FLOW','','2015-05-06 16:27:28',NULL),(146,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.16.27.45.bak','FLOW','','2015-05-06 16:28:16',NULL),(147,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.16.28.49.bak','FLOW','','2015-05-06 16:29:14',NULL),(148,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.16.31.06.bak','FLOW','','2015-05-06 16:31:29',NULL),(149,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.16.37.47.bak','FLOW','','2015-05-06 16:40:07',NULL),(150,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.06.16.45.59.bak','FLOW','','2015-05-06 16:46:59',NULL),(151,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.16.47.30.bak','FLOW','','2015-05-06 16:48:09',NULL),(152,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.16.49.10.bak','FLOW','','2015-05-06 16:49:46',NULL),(153,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.16.50.29.bak','FLOW','','2015-05-06 16:51:00',NULL),(154,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.16.52.46.bak','FLOW','','2015-05-06 16:53:02',NULL),(155,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.16.54.39.bak','FLOW','','2015-05-06 16:55:07',NULL),(156,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.16.55.23.bak','FLOW','','2015-05-06 16:56:22',NULL),(157,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.17.00.09.bak','FLOW','','2015-05-06 17:01:09',NULL),(158,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.17.05.08.bak','FLOW','','2015-05-06 17:06:36',NULL),(159,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.17.07.30.bak','FLOW','','2015-05-06 17:08:07',NULL),(160,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.17.09.14.bak','FLOW','','2015-05-06 17:09:48',NULL),(161,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.17.10.38.bak','FLOW','','2015-05-06 17:15:57',NULL),(162,'/usr/src/smartcc/resources/wav/','satisfaction.wav','VOICE',NULL,'2015-05-06 17:16:22',NULL),(163,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.17.18.16.bak','FLOW','','2015-05-06 17:20:40',NULL),(164,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.06.17.21.19.bak','FLOW','','2015-05-07 10:25:06',NULL),(165,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.12.11.05.11.bak','FLOW','','2015-05-12 11:06:47',NULL),(166,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.12.11.05.00.bak','FLOW','','2015-05-12 11:06:47',NULL),(167,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.12.11.11.32.bak','FLOW','','2015-05-12 11:12:08',NULL),(168,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.12.11.16.53.bak','FLOW','','2015-05-12 11:17:24',NULL),(169,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.12.11.18.22.bak','FLOW','','2015-05-12 11:19:02',NULL),(170,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.12.11.21.13.bak','FLOW','','2015-05-12 11:21:52',NULL),(171,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.12.11.25.52.bak','FLOW','','2015-05-12 11:28:00',NULL),(172,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.12.11.28.41.bak','FLOW','','2015-05-12 11:29:45',NULL),(173,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.12.11.34.49.bak','FLOW','','2015-05-12 11:36:07',NULL),(174,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.12.11.48.53.bak','FLOW','','2015-05-12 11:49:35',NULL),(175,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.12.11.50.08.bak','FLOW','','2015-05-12 11:50:37',NULL),(176,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.12.11.51.31.bak','FLOW','','2015-05-12 11:52:23',NULL),(177,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.12.11.54.23.bak','FLOW','','2015-05-12 11:55:08',NULL),(178,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.12.11.56.07.bak','FLOW','','2015-05-12 11:56:39',NULL),(179,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.12.11.55.50.bak','FLOW','','2015-05-12 11:56:39',NULL),(180,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.12.11.57.30.bak','FLOW','','2015-05-12 11:58:10',NULL),(181,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.12.12.00.04.bak','FLOW','','2015-05-12 12:01:10',NULL),(182,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.12.12.12.24.bak','FLOW','','2015-05-12 12:12:42',NULL),(183,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.12.12.13.06.bak','FLOW','','2015-05-12 12:13:37',NULL),(184,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.12.12.14.32.bak','FLOW','','2015-05-12 12:15:26',NULL),(185,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.12.14.10.12.bak','FLOW','','2015-05-12 14:24:35',NULL),(186,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.15.16.44.47.bak','FLOW','','2015-05-18 08:54:36',NULL),(187,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.18.12.20.36.bak','FLOW','','2015-05-18 14:12:57',NULL),(188,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.18.12.21.23.bak','FLOW','','2015-05-18 14:12:57',NULL),(189,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.18.14.25.35.bak','FLOW','','2015-05-18 14:29:03',NULL),(190,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.18.14.30.11.bak','FLOW','','2015-05-18 14:31:31',NULL),(191,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.05.18.14.30.15.bak','FLOW','','2015-05-18 14:31:31',NULL),(192,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.18.14.31.52.bak','FLOW','','2015-05-18 14:34:40',NULL),(193,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.18.14.36.07.bak','FLOW','','2015-05-18 14:36:31',NULL),(194,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.18.14.36.43.bak','FLOW','','2015-05-18 14:41:28',NULL),(195,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.21.17.31.40.bak','FLOW','','2015-05-21 17:38:42',NULL),(196,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.26.14.00.31.bak','FLOW','','2015-05-26 14:09:24',NULL),(197,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.26.14.00.52.bak','FLOW','','2015-05-26 14:09:24',NULL),(198,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.26.14.08.38.bak','FLOW','','2015-05-26 14:09:24',NULL),(199,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.26.14.10.53.bak','FLOW','','2015-05-26 14:11:01',NULL),(200,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.26.14.13.52.bak','FLOW','','2015-05-26 14:14:18',NULL),(201,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.26.14.40.09.bak','FLOW','','2015-05-26 14:45:36',NULL),(202,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.26.14.52.23.bak','FLOW','','2015-05-26 16:11:57',NULL),(204,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.28.14.06.12.bak','FLOW','','2015-05-28 14:22:23',NULL),(205,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.28.14.24.53.bak','FLOW','','2015-05-28 14:26:09',NULL),(206,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.28.14.25.52.bak','FLOW','','2015-05-28 14:26:09',NULL),(207,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.28.14.39.37.bak','FLOW','','2015-05-28 14:42:12',NULL),(208,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.28.14.43.05.bak','FLOW','','2015-05-28 14:46:36',NULL),(209,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.28.15.11.40.bak','FLOW','','2015-05-28 15:11:48',NULL),(210,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.28.15.19.15.bak','FLOW','','2015-05-28 15:20:15',NULL),(211,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.28.15.21.12.bak','FLOW','','2015-05-28 15:23:53',NULL),(212,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.28.15.22.58.bak','FLOW','','2015-05-28 15:23:53',NULL),(213,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.28.15.28.59.bak','FLOW','','2015-05-28 15:31:36',NULL),(214,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.28.15.52.29.bak','FLOW','','2015-05-28 18:27:52',NULL),(215,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.28.18.49.12.bak','FLOW','','2015-05-28 18:51:06',NULL),(216,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.28.18.52.17.bak','FLOW','','2015-05-28 18:52:24',NULL),(217,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.05.28.18.53.35.bak','FLOW','','2015-05-28 18:54:28',NULL),(218,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.06.01.16.54.53.bak','FLOW','','2015-06-01 16:56:05',NULL),(219,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.06.01.16.56.37.bak','FLOW','','2015-06-01 17:00:13',NULL),(220,'/usr/src/smartcc/resources/vxml/','ipcc-testmeiyongde1.vxml.2015.06.01.18.54.03.bak','FLOW','','2015-06-01 18:56:26',NULL),(221,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.07.23.15.12.08.bak','FLOW','','2015-07-23 15:13:57',NULL),(222,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.07.23.15.16.14.bak','FLOW','','2015-07-23 15:22:55',NULL),(223,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.07.23.15.23.46.bak','FLOW','','2015-07-23 15:25:30',NULL),(224,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.07.23.15.26.13.bak','FLOW','','2015-07-23 15:27:25',NULL),(225,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.07.23.15.28.30.bak','FLOW','','2015-07-23 15:29:47',NULL),(226,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.07.23.15.37.03.bak','FLOW','','2015-07-23 15:39:28',NULL),(227,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.07.23.15.40.22.bak','FLOW','','2015-07-23 15:40:36',NULL),(228,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.07.23.15.50.06.bak','FLOW','','2015-07-23 15:50:12',NULL),(229,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.07.23.15.52.08.bak','FLOW','','2015-07-23 15:52:16',NULL),(230,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.07.23.16.02.42.bak','FLOW','','2015-07-23 16:05:29',NULL),(231,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.07.23.16.06.57.bak','FLOW','','2015-07-23 16:08:19',NULL),(232,'/usr/src/smartcc/resources/vxml/','ipcc.vxml.2015.07.23.16.11.08.bak','FLOW','','2015-07-23 16:11:29',NULL),(233,'/usr/src/smartcc/resources/wav/','service1.wav','VOICE','','2019-10-19 12:27:31',NULL),(234,'/usr/src/smartcc/resources/wav/','LeaveMessage.wav','VOICE','','2019-10-19 12:27:31',NULL),(235,'/usr/src/smartcc/resources/wav/','service2.wav','VOICE','','2019-10-19 12:27:31',NULL),(236,'/usr/src/smartcc/resources/wav/','welcome1.wav','VOICE','','2019-10-19 12:27:31',NULL),(237,'/usr/src/smartcc/resources/vxml/','App-old.vxml','FLOW','','2019-10-19 12:27:31',NULL),(238,'/usr/src/smartcc/resources/vxml/','ipcc-simple.vxml','FLOW','','2019-10-19 12:27:31',NULL),(239,'/usr/src/smartcc/resources/vxml/','ipcc-lt.vxml','FLOW','','2019-10-19 12:27:31',NULL);
/*!40000 ALTER TABLE `cc_core_fileinfo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cc_core_generalcode`
--

DROP TABLE IF EXISTS `cc_core_generalcode`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc_core_generalcode` (
  `rid` int(11) NOT NULL AUTO_INCREMENT,
  `gesection` varchar(16) DEFAULT NULL,
  `gekey` varchar(32) DEFAULT NULL,
  `gevalue` varchar(64) DEFAULT NULL,
  `genote` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB AUTO_INCREMENT=185 DEFAULT CHARSET=utf8 COMMENT='常用代码表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc_core_generalcode`
--

LOCK TABLES `cc_core_generalcode` WRITE;
/*!40000 ALTER TABLE `cc_core_generalcode` DISABLE KEYS */;
INSERT INTO `cc_core_generalcode` VALUES (1,'skillgrouptype','1','坐席组',NULL),(2,'skillgrouptype','7','中继组',NULL),(3,'channelType','1','电话',NULL),(4,'callRouteTactic','1','最长排队时间优先',NULL),(5,'callRouteTactic','2','最高客户等级优先',NULL),(6,'callRouteTactic','3','PBX路由',NULL),(7,'agentRouteTactic','1','最长空闲时间优先',NULL),(8,'agentRouteTactic','2','通话次数最少优先',NULL),(11,'agentRouteTactic','5','最少通话时间优先',NULL),(12,'agentRouteTactic','6','最高技能等级优先',NULL),(13,'agentRouteTactic','7','最少并发处理优先',NULL),(14,'agentRouteTactic','8','ACD(PBX路由)',NULL),(15,'overflowType','1','技能组',NULL),(16,'overflowType','2','设备号码',NULL),(17,'skillgrouptype','2','寻线组',NULL),(18,'ivrServiceType','BSP','BSP',NULL),(19,'ivrServiceType','VPS','VPS',NULL),(20,'ivrServiceType','MCS','MCS',NULL),(21,'ivrloadcfgType','0','(默认)全的',NULL),(22,'ivrloadcfgType','1','服务信息修改',NULL),(23,'ivrloadcfgType','2','系统参数修改',NULL),(24,'ivrloadcfgType','3','核心信息修改',NULL),(25,'ivrloadcfgType','4','分机修改',NULL),(26,'ivrloadcfgType','5','全部修改',NULL),(27,'trunkType','5','SIP',NULL),(28,'trunkType','6','DAHDI',NULL),(29,'extenDnType','1','坐席分机',NULL),(30,'extenDnType','2','IVR分机',NULL),(31,'extenDnType','7','中继分机',NULL),(32,'calldirection','1','呼入',NULL),(33,'calldirection','2','呼出',NULL),(34,'calldirection','3','双向呼叫',NULL),(35,'channelType','2','消息',NULL),(36,'outboundType','DOWN','不通',NULL),(37,'outboundType','EARLY','用户拒接',NULL),(38,'outboundType','RING_WAIT','拒接后，未转到CC前挂机',NULL),(39,'outboundType','ANSWER','接通并成功转到座席',NULL),(40,'taskType','0','初始化',NULL),(41,'taskType','1','预备呼',NULL),(42,'taskType','2','已呼出',NULL),(43,'customType','1','微信',NULL),(44,'customType','2','支付宝',NULL),(46,'orderStatus','1','等待审核',NULL),(47,'orderStatus','2','等待到款',NULL),(48,'orderStatus','3','正在配货',NULL),(49,'orderStatus','4','已发货',NULL),(50,'orderStatus','5','已送达',NULL),(51,'orderStatus','6','交易成功',NULL),(52,'orderStatus','7','交易失败',NULL),(53,'orderStatus','8','取消',NULL),(60,'workStatus','1','办理中',NULL),(61,'workStatus','2','转办中',NULL),(62,'workStatus','3','办结',NULL),(63,'workStatus','4','作废',NULL),(65,'general_tags_kno','314','手机',NULL),(66,'general_tags_kno','833','衣服',NULL),(67,'workType','1','通话小结',NULL),(68,'workType','2','业务咨询',NULL),(69,'workType','3','售后服务',NULL),(71,'workType','4','业务办理',NULL),(72,'workType','5','投诉建议',NULL),(74,'workBusinessType','2','呼出',NULL),(75,'workBusinessType','3','微信消息',NULL),(76,'workBusinessType','4','支付宝消息',NULL),(77,'verify_status','0','待审',NULL),(78,'verify_status','1','通过',NULL),(79,'verify_status','2','废弃',NULL),(81,'general_tags_kno','456','在线教程',NULL),(82,'procedures','1','精准回复',NULL),(83,'procedures','2','模糊回复',NULL),(84,'procedures','3','人工技能',NULL),(85,'procedures','4','机器人',NULL),(86,'procedures','5','其他',NULL),(93,'general_tags_kno','349','aa',NULL),(100,'quality_grade','1','不合格',NULL),(101,'quality_grade','2','合格',NULL),(102,'quality_grade','3','良好',NULL),(103,'quality_grade','4','优秀',NULL),(113,'general_tags_cty','0','建筑工程',NULL),(169,'customClassType','0','特别重要',NULL),(170,'customClassType','1','重要',NULL),(171,'customClassType','2','次要',NULL),(172,'customClassType','3','一般',NULL),(175,'general_tags_cty','1','一级建造师',NULL),(176,'general_tags_cty','2','二级建造师',NULL),(177,'general_tags_cty','3','专科学历',NULL),(178,'general_tags_cty','4','本科学历',NULL),(179,'general_tags_cty','5','证书挂靠',NULL),(180,'general_tags_cty','6','医药其他',NULL),(181,'general_tags_cty','7','建筑其他',NULL),(182,'outboundType','ACTIVE','呼叫成功',NULL),(183,'outboundType','NO_ANSWER','用户接通，座席拒接',NULL),(184,'outboundType','USER_REJECT','用户接通后，在座席接通之前挂机',NULL);
/*!40000 ALTER TABLE `cc_core_generalcode` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cc_core_ivrroute`
--

DROP TABLE IF EXISTS `cc_core_ivrroute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc_core_ivrroute` (
  `trunksn` int(11) NOT NULL AUTO_INCREMENT,
  `trunk_servicename` varchar(16) DEFAULT 'mcsuser',
  `flow_servicename` varchar(16) DEFAULT 'vpsuser',
  `trunkgroupid` varchar(2) NOT NULL,
  `calldirection` varchar(1) DEFAULT '3',
  `channelCount` int(11) DEFAULT '10',
  `proxyname` varchar(16) DEFAULT 'bpuser',
  `routenum` varchar(32) DEFAULT '',
  `flowname` varchar(64) DEFAULT '',
  `displayname` varchar(64) DEFAULT '',
  PRIMARY KEY (`trunksn`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc_core_ivrroute`
--

LOCK TABLES `cc_core_ivrroute` WRITE;
/*!40000 ALTER TABLE `cc_core_ivrroute` DISABLE KEYS */;
INSERT INTO `cc_core_ivrroute` VALUES (1,'mcsuser','vpsuser','1','1',10,'bpuser','1234567890','../resources/vxml/App.vxml','123'),(3,'mcsuser','vpsuser','1','3',20,'bpuser','10010','../resources/vxml/ipcc.vxml','');
/*!40000 ALTER TABLE `cc_core_ivrroute` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cc_core_linkinfo`
--

DROP TABLE IF EXISTS `cc_core_linkinfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc_core_linkinfo` (
  `rid` int(11) NOT NULL AUTO_INCREMENT,
  `link_servername` varchar(32) DEFAULT NULL,
  `link_username` varchar(32) DEFAULT NULL,
  `link_password` varchar(32) DEFAULT NULL,
  `link_version` varchar(32) DEFAULT NULL,
  `link_apitype` varchar(16) DEFAULT NULL,
  `pbx_type` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc_core_linkinfo`
--

LOCK TABLES `cc_core_linkinfo` WRITE;
/*!40000 ALTER TABLE `cc_core_linkinfo` DISABLE KEYS */;
INSERT INTO `cc_core_linkinfo` VALUES (1,'ASTERISK',NULL,NULL,'1','CTC','ASTERISK');
/*!40000 ALTER TABLE `cc_core_linkinfo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cc_core_numanalysis`
--

DROP TABLE IF EXISTS `cc_core_numanalysis`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc_core_numanalysis` (
  `rid` int(11) NOT NULL AUTO_INCREMENT,
  `dnistitle` varchar(16) DEFAULT NULL,
  `trunkgroupid` varchar(1) DEFAULT NULL,
  `dnisminlen` varchar(2) DEFAULT '1',
  `dnismaxlen` varchar(2) DEFAULT '32',
  `numconvid` varchar(1) DEFAULT '0',
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc_core_numanalysis`
--

LOCK TABLES `cc_core_numanalysis` WRITE;
/*!40000 ALTER TABLE `cc_core_numanalysis` DISABLE KEYS */;
INSERT INTO `cc_core_numanalysis` VALUES (1,'10','2','1','32','0'),(2,'DEFAULT','1','1','32','0');
/*!40000 ALTER TABLE `cc_core_numanalysis` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cc_core_numconvert`
--

DROP TABLE IF EXISTS `cc_core_numconvert`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc_core_numconvert` (
  `rid` int(11) NOT NULL AUTO_INCREMENT,
  `numconvid` varchar(2) DEFAULT NULL,
  `aniconvtype` varchar(2) DEFAULT NULL,
  `aniconvbegin` varchar(2) DEFAULT NULL,
  `aniconvlen` varchar(4) DEFAULT NULL,
  `aniconvdest` varchar(16) DEFAULT NULL,
  `dnisconvtype` varchar(1) DEFAULT '0',
  `dnisconvbegin` varchar(1) DEFAULT '0',
  `dnisconvlen` varchar(1) DEFAULT '0',
  `dnisconvdest` varchar(8) DEFAULT '3000',
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc_core_numconvert`
--

LOCK TABLES `cc_core_numconvert` WRITE;
/*!40000 ALTER TABLE `cc_core_numconvert` DISABLE KEYS */;
INSERT INTO `cc_core_numconvert` VALUES (1,'1','1','0','20','10.130.16.36','0','0','0','3000');
/*!40000 ALTER TABLE `cc_core_numconvert` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cc_core_pbxtrunk`
--

DROP TABLE IF EXISTS `cc_core_pbxtrunk`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc_core_pbxtrunk` (
  `rid` int(11) NOT NULL AUTO_INCREMENT,
  `d_exten` varchar(5) DEFAULT NULL,
  `d_type` varchar(8) DEFAULT 'peer',
  `d_username` varchar(16) DEFAULT '',
  `d_secret` varchar(16) DEFAULT '',
  `d_host` varchar(16) DEFAULT NULL,
  `d_context` varchar(16) DEFAULT 'from-pstn',
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc_core_pbxtrunk`
--

LOCK TABLES `cc_core_pbxtrunk` WRITE;
/*!40000 ALTER TABLE `cc_core_pbxtrunk` DISABLE KEYS */;
/*!40000 ALTER TABLE `cc_core_pbxtrunk` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cc_core_profile`
--

DROP TABLE IF EXISTS `cc_core_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc_core_profile` (
  `rid` int(11) NOT NULL AUTO_INCREMENT,
  `con_section` varchar(32) NOT NULL,
  `con_key` varchar(32) NOT NULL,
  `con_value` varchar(64) DEFAULT NULL,
  `con_desc` varchar(256) DEFAULT '',
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc_core_profile`
--

LOCK TABLES `cc_core_profile` WRITE;
/*!40000 ALTER TABLE `cc_core_profile` DISABLE KEYS */;
INSERT INTO `cc_core_profile` VALUES (1,'CONTACT_TEL','MAXTELCONTACT','2048',''),(2,'CONTACT_TEL','MAXAGENTCALLCOUNT','1',''),(3,'CONTACT_IM','MAXIMCONTACT','8',''),(4,'CONTACT_IM','MAXAGENTCALLCOUNT','4',''),(5,'CONTACT_STAT','UPDATENUM','5',''),(6,'CONTACT_STAT','PUSHTIME','1',''),(7,'CONTACT_STAT','STATISTICTIME','15',''),(8,'CONTACT_STAT','MAXSAVETIME_ST1','2',''),(9,'CONTACT_STAT','MAXSAVETIME_ST2','8',''),(10,'CONTACT_STAT','MAXSAVETIME_ST2_MINUTE','1','测试'),(11,'CONTACT_STAT','CDRTIME','1',''),(12,'CONTACT_STAT','CDRSIZE','3',''),(13,'CONTACT_STAT','NTIME_CONN','20','');
/*!40000 ALTER TABLE `cc_core_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cc_core_projinfo`
--

DROP TABLE IF EXISTS `cc_core_projinfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc_core_projinfo` (
  `rid` int(11) NOT NULL AUTO_INCREMENT,
  `proj_name` varchar(16) DEFAULT NULL,
  `update_time` varchar(32) DEFAULT NULL,
  `type` varchar(8) DEFAULT NULL,
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc_core_projinfo`
--

LOCK TABLES `cc_core_projinfo` WRITE;
/*!40000 ALTER TABLE `cc_core_projinfo` DISABLE KEYS */;
INSERT INTO `cc_core_projinfo` VALUES (1,'smartcc 1.0','2019-10-17 17:16:52','IVR'),(2,'smatcc 1.0','2019-10-17 17:16:52','CTI'),(3,'smartcc 1.0','2019-10-17 17:16:52','PBX');
/*!40000 ALTER TABLE `cc_core_projinfo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cc_core_recordmember`
--

DROP TABLE IF EXISTS `cc_core_recordmember`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc_core_recordmember` (
  `rid` int(11) NOT NULL AUTO_INCREMENT,
  `extn_dn` varchar(32) DEFAULT NULL,
  `record_path` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc_core_recordmember`
--

LOCK TABLES `cc_core_recordmember` WRITE;
/*!40000 ALTER TABLE `cc_core_recordmember` DISABLE KEYS */;
INSERT INTO `cc_core_recordmember` VALUES (1,'7001-7120','/var/spool/uc/RecordShare1/');
/*!40000 ALTER TABLE `cc_core_recordmember` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cc_core_serviceinfo`
--

DROP TABLE IF EXISTS `cc_core_serviceinfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc_core_serviceinfo` (
  `rid` int(11) NOT NULL AUTO_INCREMENT,
  `servicename` varchar(16) DEFAULT NULL,
  `servicetype` varchar(16) DEFAULT NULL,
  `serviceip` varchar(16) DEFAULT NULL,
  `serviceport` varchar(8) DEFAULT NULL,
  `mcsdevice` varchar(16) DEFAULT '',
  `iscluster` varchar(1) DEFAULT '0',
  `loadcfgtype` varchar(1) DEFAULT '0',
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc_core_serviceinfo`
--

LOCK TABLES `cc_core_serviceinfo` WRITE;
/*!40000 ALTER TABLE `cc_core_serviceinfo` DISABLE KEYS */;
INSERT INTO `cc_core_serviceinfo` VALUES (1,'mcsuser','MCS','127.0.0.1','13001','Asterisk','0','0'),(2,'vpsuser','VPS','127.0.0.1','14001',NULL,'0','0'),(3,'bpuser','BSP','127.0.0.1','15001',NULL,'0','0');
/*!40000 ALTER TABLE `cc_core_serviceinfo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cc_core_skill`
--

DROP TABLE IF EXISTS `cc_core_skill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc_core_skill` (
  `rid` int(11) NOT NULL AUTO_INCREMENT,
  `skill_id` varchar(2) NOT NULL,
  `skill_name` varchar(16) DEFAULT NULL,
  `skill_group_type` varchar(1) DEFAULT '2',
  `domain_id` varchar(1) DEFAULT NULL,
  `skill_weight` varchar(2) DEFAULT NULL,
  `channel_type` varchar(1) DEFAULT '1',
  `queue_dn` varchar(8) DEFAULT '',
  `agent_route_tactic` varchar(1) DEFAULT NULL,
  `call_route_tactic` varchar(1) DEFAULT NULL,
  `queue_over_timer` varchar(8) DEFAULT '60000',
  `queue_max_count` varchar(4) DEFAULT '30',
  `overflow_type1` varchar(1) DEFAULT '',
  `overflow_desc1` varchar(8) DEFAULT '',
  `overflow_type2` varchar(1) DEFAULT '',
  `overflow_desc2` varchar(8) DEFAULT '',
  `overflow_type3` varchar(1) DEFAULT '',
  `overflow_desc3` varchar(8) DEFAULT '',
  `pop3_user` varchar(16) DEFAULT '',
  `ftp_user` varchar(16) DEFAULT '',
  `is_valid` varchar(1) DEFAULT '1',
  `skill_desc` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc_core_skill`
--

LOCK TABLES `cc_core_skill` WRITE;
/*!40000 ALTER TABLE `cc_core_skill` DISABLE KEYS */;
INSERT INTO `cc_core_skill` VALUES (1,'1','1001','2','4','4','1',NULL,'1','1','60000','30',NULL,NULL,NULL,NULL,NULL,NULL,'','','1','测试1'),(2,'2','1002','2','5','1','1','','1','1','60000','30','','','','','','','','','1','测试2'),(3,'3','1003','2','6','5','1','','6','1','60000','30','','','','','','','','','1','测试3'),(4,'4','1004','2','7','5','1','','6','1','60000','30','','','','','','','','','1','测试4'),(5,'5','1005','2','8','1','1','','1','1','60000','30','','','','','','','','','1','测试5'),(6,'6','monitor','2','3','5','1',NULL,'1','1','60000','60',NULL,NULL,NULL,NULL,NULL,NULL,'','','1','质检');
/*!40000 ALTER TABLE `cc_core_skill` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cc_core_sys_menu`
--

DROP TABLE IF EXISTS `cc_core_sys_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc_core_sys_menu` (
  `menu_id` int(8) NOT NULL,
  `menu_name` varchar(30) NOT NULL COMMENT '菜单名称',
  `menu_url` varchar(300) DEFAULT NULL COMMENT '菜单URL',
  `menu_level` int(8) NOT NULL COMMENT '菜单级别',
  `menu_order` int(8) DEFAULT NULL COMMENT '顺序',
  `parent_id` int(8) DEFAULT NULL COMMENT '上级菜单',
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='系统菜单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc_core_sys_menu`
--

LOCK TABLES `cc_core_sys_menu` WRITE;
/*!40000 ALTER TABLE `cc_core_sys_menu` DISABLE KEYS */;
INSERT INTO `cc_core_sys_menu` VALUES (0,'',NULL,0,NULL,706),(1,'账号管理','/',1,1,NULL),(2,'PBX管理','/',1,2,NULL),(3,'CTI 管理','/',1,3,NULL),(4,'IVR 管理','/',1,4,NULL),(5,'实时监控','/',1,5,NULL),(6,'服务记录','/',1,6,NULL),(7,'交互查询','/',1,7,NULL),(8,'客户管理','/',1,8,NULL),(9,'订单管理','/',1,9,NULL),(10,'智能消息','/',1,10,NULL),(11,'质检管理','/',1,11,NULL),(15,'外呼任务','/',1,15,NULL),(16,'统计分析','/',1,16,NULL),(101,'用户管理','/usermanager',2,1,1),(102,'角色管理','/userrole',2,2,1),(201,'SIP分机','/sipExtenController',2,1,2),(202,'PBX出局','/TrunkController',2,2,2),(203,'PBX局向','/ExtenDisinfo',2,3,2),(204,'PBX路由','/RouteController',2,4,2),(205,'服务配置','/serviceconf',2,5,2),(206,'配置加载','/configdata',2,6,2),(301,'链路配置','/linkController',2,1,3),(302,'号码配置','/ExtenController',2,2,3),(303,'CTI 参数','/ctisyspController',2,3,3),(304,'录音配置','/recordController',2,4,3),(305,'统计域配置','/domainController',2,5,3),(306,'技能组配置','/skillController',2,6,3),(401,'服务配置','/serviceinfo',2,1,4),(402,'流程配置','/flowinfo',2,6,4),(403,'中继群配置','/trunkgroup',2,3,4),(404,'中继配置','/trunkinfo',2,4,4),(405,'通道配置','/channelinfo',2,5,4),(407,'文件管理','/filemanager',2,7,4),(408,'IVR 参数','/ivrsyspController',2,8,4),(409,'IVR路由','/ivrRouteController',2,9,4),(501,'话务监控','/callinfo',2,1,5),(502,'座席状态','/agentinfo',2,2,5),(503,'技能统计','/skillinfo',2,3,5),(504,'IVR通道','/ivrchannelinfo',2,4,5),(505,'坐席整体','/allstatus',2,5,5),(601,'工单信息','/worktaskinfo',2,1,6),(602,'我的工单','/worktaskinfo/belongMe',2,2,6),(603,'我的受理','/worktaskinfo/myWorkDone',2,3,6),(701,'通话详单','/record',2,1,7),(702,'未接记录','/missCall',2,2,7),(704,'我的未接','/missCall/myRecord',2,4,7),(705,'我的通话','/record/myCall',2,5,7),(707,'消息记录','/msgRecordNew',2,7,7),(801,'客户信息','/custominfo',2,1,8),(802,'我的客户','/custominfo/myCustom',2,2,8),(901,'订单信息','/orderinfo',2,1,9),(902,'产品信息','/productsinfo',2,2,9),(903,'我的订单','/orderinfo/myOrder',2,3,9),(1001,'精准回复','/topicinfo',2,1,10),(1002,'自动回复','/knowinfo',2,2,10),(1004,'接入信息','/interfController',2,4,10),(1101,'质检','/inspectinfo',2,1,11),(1102,'质检模板','/qualitytempinfo',2,2,11),(1103,'质检成绩','/qualityresult',2,3,11),(1104,'我的成绩','/qualityresult/myResult',2,4,11),(1501,'任务信息','/callTaskController',2,1,15),(1502,'任务详单','/detailController',2,2,15),(1503,'我的任务','/callTaskController/myTask',2,3,15),(1504,'我的详单','/detailController/myTaskInfo',2,4,15),(1601,'话务统计','/report/talk',2,1,16),(1602,'满意度统计','/report/satisfaction',2,2,16);
/*!40000 ALTER TABLE `cc_core_sys_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cc_core_sys_role`
--

DROP TABLE IF EXISTS `cc_core_sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc_core_sys_role` (
  `role_id` int(7) NOT NULL AUTO_INCREMENT,
  `role_name` varchar(19) NOT NULL,
  `description` varchar(511) DEFAULT NULL,
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc_core_sys_role`
--

LOCK TABLES `cc_core_sys_role` WRITE;
/*!40000 ALTER TABLE `cc_core_sys_role` DISABLE KEYS */;
INSERT INTO `cc_core_sys_role` VALUES (4,'运营者','拥有所有功能'),(5,'管理员','普通管理员'),(6,'坐席','普通坐席'),(8,'质检专员',NULL),(17,'A1','');
/*!40000 ALTER TABLE `cc_core_sys_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cc_core_sys_rolemenu`
--

DROP TABLE IF EXISTS `cc_core_sys_rolemenu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc_core_sys_rolemenu` (
  `role_id` int(8) NOT NULL,
  `menu_id` int(8) NOT NULL,
  PRIMARY KEY (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc_core_sys_rolemenu`
--

LOCK TABLES `cc_core_sys_rolemenu` WRITE;
/*!40000 ALTER TABLE `cc_core_sys_rolemenu` DISABLE KEYS */;
INSERT INTO `cc_core_sys_rolemenu` VALUES (4,1),(4,2),(4,3),(4,4),(4,5),(4,6),(4,7),(4,8),(4,9),(4,10),(4,11),(4,15),(4,16),(4,101),(4,102),(4,201),(4,202),(4,204),(4,205),(4,206),(4,302),(4,304),(4,306),(4,407),(4,409),(4,501),(4,502),(4,503),(4,504),(4,505),(4,601),(4,602),(4,603),(4,701),(4,702),(4,704),(4,705),(4,707),(4,801),(4,802),(4,901),(4,902),(4,903),(4,1001),(4,1002),(4,1004),(4,1101),(4,1102),(4,1103),(4,1104),(4,1501),(4,1502),(4,1503),(4,1504),(4,1601),(4,1602),(5,1),(5,3),(5,4),(5,5),(5,6),(5,7),(5,8),(5,9),(5,10),(5,11),(5,15),(5,16),(5,101),(5,306),(5,407),(5,501),(5,502),(5,503),(5,504),(5,505),(5,601),(5,602),(5,603),(5,701),(5,702),(5,703),(5,704),(5,705),(5,706),(5,707),(5,801),(5,802),(5,901),(5,902),(5,903),(5,1001),(5,1002),(5,1004),(5,1101),(5,1102),(5,1103),(5,1501),(5,1502),(5,1503),(5,1504),(5,1601),(5,1602),(6,5),(6,6),(6,7),(6,8),(6,9),(6,11),(6,15),(6,16),(6,501),(6,502),(6,505),(6,602),(6,603),(6,704),(6,705),(6,802),(6,902),(6,903),(6,1102),(6,1104),(6,1501),(6,1502),(6,1503),(6,1504),(6,1601),(6,1602),(8,1),(8,2),(8,3),(8,4),(8,5),(8,6),(8,7),(8,8),(8,9),(8,10),(8,15),(8,101),(8,102),(8,104),(8,201),(8,202),(8,204),(8,205),(8,206),(8,301),(8,302),(8,303),(8,304),(8,306),(8,401),(8,402),(8,403),(8,404),(8,405),(8,407),(8,408),(8,501),(8,502),(8,503),(8,504),(8,505),(8,601),(8,701),(8,702),(8,703),(8,801),(8,901),(8,902),(8,1001),(8,1002),(8,1004),(8,1501),(8,1502),(17,1),(17,7),(17,9),(17,15),(17,101),(17,102),(17,701),(17,702),(17,901),(17,902),(17,1501),(17,1502);
/*!40000 ALTER TABLE `cc_core_sys_rolemenu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cc_core_sys_user`
--

DROP TABLE IF EXISTS `cc_core_sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc_core_sys_user` (
  `user_id` int(8) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(30) NOT NULL COMMENT 'ÓÃ»§Ãû',
  `login_name` varchar(30) NOT NULL COMMENT 'µÇÂ¼Ãû',
  `password` varchar(60) DEFAULT NULL COMMENT 'ÃÜÂë',
  `telephone` varchar(60) DEFAULT NULL COMMENT 'µç»°ºÅÂë',
  `email` varchar(60) DEFAULT NULL COMMENT 'µç×ÓÓÊÏä',
  `qualityAgCode` varchar(64) DEFAULT '',
  `qualityAgDN` varchar(64) DEFAULT '',
  `skillist` varchar(256) DEFAULT NULL,
  `tech` varchar(128) DEFAULT NULL,
  `role_type` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=87 DEFAULT CHARSET=utf8 COMMENT='ÏµÍ³ÓÃ»§';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc_core_sys_user`
--

LOCK TABLES `cc_core_sys_user` WRITE;
/*!40000 ALTER TABLE `cc_core_sys_user` DISABLE KEYS */;
INSERT INTO `cc_core_sys_user` VALUES (1,'admin','admin','18c6d818ae35a3e8279b5330eda01498','13510018001','admin@oms.cn','3002','3002','1001=1;1002=1;1003=1;1004=1;1005=1;monitor=1;',NULL,0),(20,'asd','asd','7815696ecbf1c96e6894b779456d330e','123','12312','3003','3003','1001=1;1002=1;1003=1;1004=1;1005=1;',NULL,1),(24,'3007','3007','a724b9124acc7b5058ed75a31a9c2919','3007','3007v@163.com','3007','3007','1001=1;1002=1;1003=1;1004=1;1005=1;',NULL,1),(26,'3008','3008','c02f9de3c2f3040751818aacc7f60b74','3008','sadas@163.com','3008','3008','1001=1;monitor=1;',NULL,1),(69,'3004','3004','e10adc3949ba59abbe56e057f20f883e','777777777','1111111111111','3004','3004','1001=1;1002=1;1003=1;1004=1;1005=1;monitor=1;',NULL,1),(74,'3012','3012','5a01f0597ac4bdf35c24846734ee9a76',NULL,NULL,'3012','3012','1001=1;',NULL,1),(81,'raojl','raojl','25a9a4de25a95a59fda0cf80ecae1953',NULL,NULL,'3006','3006','1001=1;1002=1;1003=1;1004=1;1005=1;',NULL,1),(85,'3010','3010','22722a343513ed45f14905eb07621686','30103010','3010','3010','3010','1001=1;1002=1;',NULL,1),(86,'4001','4001','ffc58105bf6f8a91aba0fa2d99e6f106',NULL,NULL,'4001','4001','1001=1;1002=1;1003=1;1004=1;monitor=1;',NULL,1);
/*!40000 ALTER TABLE `cc_core_sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cc_core_sys_userrole`
--

DROP TABLE IF EXISTS `cc_core_sys_userrole`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc_core_sys_userrole` (
  `user_id` int(8) NOT NULL,
  `role_id` int(8) NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc_core_sys_userrole`
--

LOCK TABLES `cc_core_sys_userrole` WRITE;
/*!40000 ALTER TABLE `cc_core_sys_userrole` DISABLE KEYS */;
INSERT INTO `cc_core_sys_userrole` VALUES (1,4),(10,6),(11,6),(14,4),(15,4),(16,6),(18,6),(19,6),(20,6),(21,6),(22,6),(23,6),(24,6),(25,5),(26,6),(27,6),(28,17),(29,8),(30,6),(31,6),(32,4),(33,5),(34,6),(36,6),(42,6),(43,6),(46,6),(47,6),(49,6),(50,6),(51,8),(52,6),(53,6),(54,6),(55,8),(56,8),(57,6),(58,6),(63,6),(64,6),(65,6),(66,6),(67,6),(69,6),(70,6),(71,6),(72,4),(73,6),(74,6),(75,6),(76,6),(77,6),(78,6),(79,6),(80,5),(81,6),(82,5),(83,4),(84,8),(85,6),(86,6);
/*!40000 ALTER TABLE `cc_core_sys_userrole` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cc_core_systemparam`
--

DROP TABLE IF EXISTS `cc_core_systemparam`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc_core_systemparam` (
  `rid` int(11) NOT NULL AUTO_INCREMENT,
  `servicename` varchar(16) DEFAULT NULL,
  `paramname` varchar(32) DEFAULT NULL,
  `paramvalue` varchar(64) DEFAULT NULL,
  `paramnote` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc_core_systemparam`
--

LOCK TABLES `cc_core_systemparam` WRITE;
/*!40000 ALTER TABLE `cc_core_systemparam` DISABLE KEYS */;
INSERT INTO `cc_core_systemparam` VALUES (1,'mcsuser','CallTimeOut','30','呼叫超时'),(2,'mcsuser','MaxOnlineTime','3600',''),(3,'mcsuser','B2BUA','1','语音流转模式'),(4,'mcsuser','SystemError','SystemBusy','系统忙音'),(5,'mcsuser','DefaultWavPath','/usr/src/smartcc/resources/wav',''),(6,'mcsuser','WavLibPath','/usr/src/smartcc/resources/wavlib',''),(7,'mcsuser','SendFaxPath','/usr/src/smartcc/resources/sendfax',''),(8,'mcsuser','RecvFaxPath','/usr/src/smartcc/resources/recvfax',''),(9,'mcsuser','RecordPath','/usr/src/smartcc/resources/recfile','录音文件路径'),(10,'default','LogLevel','5','默认日志等级'),(11,'grsuser','CallInRouteType','1',''),(12,'bpuser','CTIMidWare','libmwcti.so','cti调用接口'),(13,'mcsuser','SipHeaderKey','X-Test','sip header标识');
/*!40000 ALTER TABLE `cc_core_systemparam` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cc_core_topic`
--

DROP TABLE IF EXISTS `cc_core_topic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc_core_topic` (
  `rid` int(11) NOT NULL AUTO_INCREMENT,
  `topic_id` varchar(8) DEFAULT NULL,
  `topic_name` varchar(32) DEFAULT NULL,
  `topic_sql` varchar(2048) DEFAULT NULL,
  `notify_type` varchar(1) DEFAULT '1',
  `is_sysuse` varchar(1) DEFAULT '1',
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc_core_topic`
--

LOCK TABLES `cc_core_topic` WRITE;
/*!40000 ALTER TABLE `cc_core_topic` DISABLE KEYS */;
INSERT INTO `cc_core_topic` VALUES (1,'101','联络实时数据','SELECT Distinct t1.ContactID 联络ID, t1.SkillName 技能组描述, \r\n			          t2.StatusName 联络状态,t1.RequestLevel 呼叫级别,t1.StartTime 状态开始时间,\r\n			          t1.ANI 原始主叫, t1.DNIS 原始被叫, t3.CallDirectionName 呼叫方向, t4.CallTypeName 呼叫类型, \r\n			          t5.ChannelTypeName 通道类型, t1.WorkUnitCount 服务单元数, \r\n			          t1.DeviceDN 分机号码, t6.DeviceTypeName 设备类型,t1.LastContactID 最近联络ID,t1.OriContactID 原始联络ID,t1.UpdateFlag\r\n	       FROM RT_Contact t1, Desc_ContactStatus t2, Desc_CallDirection t3, \r\n              Desc_CallType t4, Desc_ChannelType t5, Desc_DeviceType t6\r\n         WHERE t1.ContactStatus = t2.ContactStatus AND t1.CallDirection = t3.CallDirection AND\r\n               t1.CallType = t4.CallType AND t1.ChannelType = t5.ChannelType AND \r\n               t1.DeviceType = t6.DeviceType AND UpdateNum > 0','1','1'),(2,'102','座席实时数据','SELECT Distinct t1.AgentLoginID 座席登录ID, t1.AgentID 座席工号, t1.AgentDN 座席分机,t2.StatusName 座席状态, t1.ContactID 联络ID, t1.StartTime 开始时间, \r\n                LOCKED_NUM 分配次数,(IN_IB_OFNG_NUM+EX_IB_OFNG_NUM+IN_IB_BECNSOFNG_NUM+EX_IB_BECNSOFNG_NUM) 振铃次数,\r\n                (IN_IB_CONN_NUM+EX_IB_CONN_NUM+IN_OB_CONN_NUM+EX_OB_CONN_NUM+IN_IB_BECNSCONN_NUM+EX_IB_BECNSCONN_NUM) 通话次数,\r\n                t1.SkillDesc 技能组描述, t1.AgentName 座席名称, t3.AgentTypeName 座席类型, \r\n                t1.LoginTime 登录时间,t1.AgentIP IP地址,t1.ANI 主叫, t1.DNIS 被叫,t4.CallDirectionName 呼叫方向, t5.CallTypeName 呼叫类型, \r\n                t6.ChannelTypeName 通道类型, t1.TotalMailNum 邮件个数,round((IN_IB_OFNG_TIME+EX_IB_OFNG_TIME+IN_IB_BECNSOFNG_TIME+EX_IB_BECNSOFNG_TIME)*1.000/1000, 2) 振铃时长,\r\n         				round((IN_IB_CONN_TIME+EX_IB_CONN_TIME+IN_OB_CONN_TIME+EX_OB_CONN_TIME+IN_IB_HOD_TIME+EX_IB_HOD_TIME+IN_OB_HOD_TIME+EX_OB_HOD_TIME+IN_IB_CNSCONN_TIME+EX_IB_CNSCONN_TIME+IN_OB_CNSCONN_TIME+EX_OB_CNSCONN_TIME+IN_IB_BECNSCONN_TIME+EX_IB_BECNSCONN_TIME+IN_OB_BECNSCONN_TIME+EX_OB_BECNSCONN_TIME+IN_IB_CNF_TIME+EX_IB_CNF_TIME+IN_OB_CNF_TIME+EX_OB_CNF_TIME+IN_IB_BECNF_TIME+EX_IB_BECNF_TIME+IN_OB_BECNF_TIME+EX_OB_BECNF_TIME)*1.000/1000, 2) 通话时长,\r\n                IDLE_NUM 示闲次数, BUSY_NUM 示忙次数, OW_NUM 其他工作次数, ACW_NUM 事后整理次数,round(IDLE_TIME*1.000/1000,2) 示闲时长,round(BUSY_TIME*1.000/1000,2) 示忙时长,\r\n         				round(OW_TIME*1.000/1000,2) 其他工作时长, round(ACW_TIME*1.000/1000,2) 事后整理时长,t1.UpdateFlag\r\n         FROM RT_Agent t1, Desc_DeviceStatus t2, Desc_AgentType t3, Desc_CallDirection t4, Desc_CallType t5, Desc_ChannelType t6 WHERE t1.CurrStatus = t2.DeviceStatus AND t1.AgentType = t3.AgentType AND  t1.CallDirection = t4.CallDirection AND t1.CallType = t5.CallType AND \r\n               t1.ChannelType = t6.ChannelType AND UpdateNum > 0','1','1'),(3,'103','统计域实时数据','SELECT DomainID 统计域ID,DomainName 域名称,SkillID 技能组ID,\r\n        			  AgentTotal 坐席总数, AgentReady 空闲人数,\r\n        			  ContactInQueue 当前排队数, AgentWorking 当前通话数,\r\n        			  ContactInbound 当前呼入数,ContactOutbound 当前呼出数,\r\n        			  AgentNotReady 置忙人数, AgentOtherWork 其他工作人数, AgentAfterCallWork 事后整理人数\r\n         FROM RT_Domain\r\n         WHERE UpdateNum > 0 ORDER BY DomainID','1','1'),(4,'111','SkillGroupInfo','SELECT t1.SkillID SkillID,t2.SkillName SkillName,t1.AgentTotal AgentTotal,t1.AgentReady AgentReady,t1.ContactInQueue ContactInQueue,t1.MaxRequestLevel MaxRequestLevel FROM RT_Domain t1, RT_SkillGroup t2 WHERE t1.SkillID=t2.SkillID AND UpdateNum>0','1','1'),(5,'112','IVRInfo','SELECT Count(*) ChannelTotalNum, ( SELECT Count(*) FROM RT_IVR_Channel_Info WHERE (ChannelState=\'\'Idle\'\' OR ChannelState=\'\'Disconnected\'\') AND TrunkID=1 AND UpdateFlag=1 ) ChannelIdleNum FROM RT_IVR_Channel_Info WHERE  TrunkID=1 AND UpdateFlag=1','1','1'),(6,'204','IVR服务信息','SELECT ServiceName 服务名称,ServiceState 服务状态,StateDesc 状态描述,ServiceIP 服务IP地址,UpdateFlag FROM RT_IVR_Service_Info WHERE UpdateNum > 0 ORDER BY ServiceName ;','1','1'),(7,'205','IVR流程信息','SELECT FlowName 流程名称,FlowInitNum 解析流程,FlowTryNum 试呼流程,FlowIdleNum 空闲流程,FlowUsedNum 已用流程,FlowNum 流程总数,UpdateFlag FROM RT_IVR_Flow_Info WHERE UpdateNum > 0 ORDER BY FlowName ;','1','1'),(8,'206','IVR通道信息','SELECT ChannelName 通道名称,CallID 呼叫ID,ChannelState 通道状态,MediaState 媒体状态,ChannelDirection 呼叫方向,ANI 主叫号码,DNIS 被叫号码,FlowName 流程,TrunkID 中继,ExtenNum 分机号码 ,ChannelDir 通道方向,UpdateFlag FROM RT_IVR_Channel_Info WHERE UpdateNum > 0 ORDER BY ChannelName ;','1','1'),(9,'301','服务信息','select * from rt_serviceinfo','1','1'),(10,'302','IVR通道信息','select ChannelName,ChannelState,MediaState,ANI,DNIS,ExtenNum,FlowName from RT_IVR_Channel_Info where ChannelName !=\"NoUse\"','1','1'),(11,'303','åå¸­ä¿¡æ¯','select Distinct AgentID,AgentName,AgentDN,CurrStatus,StartTime,LOCKED_NUM,IN_IB_OFNG_NUM+EX_IB_OFNG_NUM+IN_IB_BECNSOFNG_NUM+EX_IB_BECNSOFNG_NUM,IN_IB_CONN_NUM+EX_IB_CONN_NUM+IN_IB_BECNSCONN_NUM+EX_IB_BECNSCONN_NUM,IN_OB_CONN_NUM+EX_OB_CONN_NUM,ANI,DNIS,SkillDesc  from RT_Agent where AgentLoginID != \"NoUse\";','1','1'),(12,'304','技能组信息','SELECT t1.DomainID,t2.SkillName,t1.AgentTotal,t1.AgentReady,t1.ContactInQueue,t1.AgentWorking,t1.ContactInbound,t1.ContactOutbound,t1.AgentNotReady,t1.AgentOtherWork,t1.AgentAfterCallWork FROM RT_Domain t1, RT_SkillGroup t2 WHERE t1.SkillID=t2.SkillID;','1','1'),(13,'305','会话信息','SELECT t1.ContactID,t1.SkillName,t1.ContactStatus,t1.StartTime,t1.ANI, t1.DNIS,t1.DeviceDN,t1.DeviceType FROM RT_Contact t1 where t1.ContactID != \"NoUse\";','1','1'),(14,'306','大屏整体监控','SELECT t1.AgentTotal,t1.AgentWorking,t1.ContactInQueue,t1.AgentNotReady,t1.AgentReady,t1.AgentAfterCallWork,t1.AgentOtherWork FROM RT_Domain t1 where DomainID  in (select  DomainID from RT_DomainInfo where PreDomainID = \"0\")','1','1');
/*!40000 ALTER TABLE `cc_core_topic` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cc_core_trunk`
--

DROP TABLE IF EXISTS `cc_core_trunk`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc_core_trunk` (
  `rid` int(11) NOT NULL AUTO_INCREMENT,
  `trunk_id` varchar(2) NOT NULL,
  `trunk_name` varchar(32) NOT NULL,
  `trunk_type` varchar(1) NOT NULL,
  `tac_code` varchar(8) DEFAULT NULL,
  `circuit_code` varchar(16) DEFAULT NULL,
  `extn_dn` varchar(32) DEFAULT NULL,
  `trunk_direction` varchar(1) NOT NULL DEFAULT '' COMMENT '中继方向',
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc_core_trunk`
--

LOCK TABLES `cc_core_trunk` WRITE;
/*!40000 ALTER TABLE `cc_core_trunk` DISABLE KEYS */;
INSERT INTO `cc_core_trunk` VALUES (15,'1','.*\\..*\\..*\\..*','5',NULL,NULL,'7001-7060',''),(17,'3','9541','5',NULL,NULL,'7110-7120',''),(18,'4','SIPTo','5',NULL,NULL,'7060-7109','');
/*!40000 ALTER TABLE `cc_core_trunk` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cc_core_trunkgroup`
--

DROP TABLE IF EXISTS `cc_core_trunkgroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc_core_trunkgroup` (
  `rid` int(11) NOT NULL AUTO_INCREMENT,
  `trunkgroupid` varchar(2) DEFAULT NULL,
  `trunkgroupname` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc_core_trunkgroup`
--

LOCK TABLES `cc_core_trunkgroup` WRITE;
/*!40000 ALTER TABLE `cc_core_trunkgroup` DISABLE KEYS */;
INSERT INTO `cc_core_trunkgroup` VALUES (1,'1','TG1');
/*!40000 ALTER TABLE `cc_core_trunkgroup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pbx_device`
--

DROP TABLE IF EXISTS `pbx_device`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pbx_device` (
  `id` varchar(30) NOT NULL COMMENT '分机号码',
  `cid` varchar(30) DEFAULT '' COMMENT 'CID/DID号码',
  `secret` varchar(30) DEFAULT '' COMMENT '分机密码',
  `type` varchar(16) NOT NULL DEFAULT 'friend',
  `port` varchar(30) DEFAULT '' COMMENT '分机端口',
  `dtmfmode` varchar(30) DEFAULT '' COMMENT 'DTMF模式',
  `disallow` varchar(30) DEFAULT '' COMMENT '禁用编解码',
  `allow` varchar(30) DEFAULT '' COMMENT '允许编解码',
  `nat` varchar(30) DEFAULT '' COMMENT 'NAT穿透',
  `canreinvite` varchar(30) DEFAULT '' COMMENT '媒体流透传',
  `context` varchar(30) DEFAULT '' COMMENT '拨号方案',
  `bind_ipaddr` varchar(64) DEFAULT NULL,
  `callerid` varchar(30) DEFAULT NULL,
  `username` char(20) DEFAULT NULL,
  `host` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='PBX分机表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pbx_device`
--

LOCK TABLES `pbx_device` WRITE;
/*!40000 ALTER TABLE `pbx_device` DISABLE KEYS */;
INSERT INTO `pbx_device` VALUES ('11111','','123456','peer','5060','rfc2833',NULL,'','yes','no','from-internal',NULL,NULL,'123456','123456'),('3001','','rht1LinkData','peer','5080','rfc2833',NULL,'','yes','no','from-internal',NULL,'3001','13810452123','192.168.1.123'),('3002','','rht1LinkData','friend','5060','rfc2833','','','yes','no','from-internal',NULL,'3002',NULL,NULL),('3003','','rht1LinkData','friend','5060','rfc2833','','','yes','no','from-internal',NULL,'3003',NULL,NULL),('3004','','rht1LinkData','friend','5060','rfc2833','','','yes','no','from-internal',NULL,'3004',NULL,NULL),('3005','','rht1LinkData','friend','5060','rfc2833','','','yes','no','from-internal',NULL,'3005',NULL,NULL),('3006','','rht1LinkData','friend','5060','rfc2833','','','yes','no','from-internal',NULL,'3006',NULL,NULL),('3007','','rht1LinkData','friend','5060','rfc2833','','','yes','no','from-internal',NULL,'3007',NULL,NULL),('3008','','rht1LinkData','friend','5060','rfc2833','','','yes','no','from-internal',NULL,'3008',NULL,NULL),('3009','','rht1LinkData','friend','5060','rfc2833','','','yes','no','from-internal',NULL,'3009',NULL,NULL),('3010','','rht1LinkData','peer','5080','rfc2833','','','yes','no','from-internal',NULL,'3010','013810452123','183.247.163.233'),('3011','','rht1LinkData','friend','5060','rfc2833','','','yes','no','from-internal',NULL,'3011',NULL,NULL),('3012','','rht1LinkData','friend','5060','rfc2833','','','yes','no','from-internal',NULL,'3012',NULL,NULL),('3013','','rht1LinkData','friend','5060','rfc2833',NULL,'','yes','no','from-internal',NULL,'3013',NULL,NULL),('3014','','rht1LinkData','friend','5060','rfc2833','','','yes','no','from-internal',NULL,'3014',NULL,NULL),('3015','','rht1LinkData','friend','5060','rfc2833','','','yes','no','from-internal',NULL,'3015',NULL,NULL),('3016','','rht1LinkData','friend','5060','rfc2833','','','yes','no','from-internal',NULL,'3016',NULL,NULL),('3017','','rht1LinkData','friend','5060','rfc2833','','','yes','no','from-internal',NULL,'3017',NULL,NULL),('3018','','rht1LinkData','friend','5060','rfc2833','','','yes','no','from-internal',NULL,'3018',NULL,NULL),('3019','','rht1LinkData','friend','5060','rfc2833','','','yes','no','from-internal',NULL,'3',NULL,NULL),('4001','','4001','peer','5080','rfc2833',NULL,'','yes','no','from-internal',NULL,'013810452123','013810452123','183.247.163.233');
/*!40000 ALTER TABLE `pbx_device` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pbx_route`
--

DROP TABLE IF EXISTS `pbx_route`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pbx_route` (
  `route_id` int(8) NOT NULL AUTO_INCREMENT COMMENT '路由Id',
  `route_dial` varchar(32) NOT NULL COMMENT '路由字冠',
  `route_name` varchar(32) NOT NULL COMMENT '路由名字',
  `route_demo` varchar(32) NOT NULL COMMENT '路由备注',
  PRIMARY KEY (`route_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='PBX中继路由表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pbx_route`
--

LOCK TABLES `pbx_route` WRITE;
/*!40000 ALTER TABLE `pbx_route` DISABLE KEYS */;
INSERT INTO `pbx_route` VALUES (1,'_9.','OutTo160','外拨到160出中继');
/*!40000 ALTER TABLE `pbx_route` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pbx_route_trunk`
--

DROP TABLE IF EXISTS `pbx_route_trunk`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pbx_route_trunk` (
  `route_id` int(8) NOT NULL COMMENT '路由Id',
  `trunk_id` int(8) NOT NULL COMMENT '中继Id',
  `orderno` int(8) NOT NULL DEFAULT '0',
  PRIMARY KEY (`route_id`,`trunk_id`),
  KEY `fk_route_trunk_route_id` (`route_id`),
  KEY `fk_route_trunk_trunk_id` (`trunk_id`),
  CONSTRAINT `fk_route_trunk_route_id` FOREIGN KEY (`route_id`) REFERENCES `pbx_route` (`route_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_route_trunk_trunk_id` FOREIGN KEY (`trunk_id`) REFERENCES `pbx_trunk` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='PBX路由的中继顺序表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pbx_route_trunk`
--

LOCK TABLES `pbx_route_trunk` WRITE;
/*!40000 ALTER TABLE `pbx_route_trunk` DISABLE KEYS */;
INSERT INTO `pbx_route_trunk` VALUES (1,1,1);
/*!40000 ALTER TABLE `pbx_route_trunk` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pbx_trunk`
--

DROP TABLE IF EXISTS `pbx_trunk`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pbx_trunk` (
  `id` int(8) NOT NULL AUTO_INCREMENT COMMENT '中继Id',
  `name` varchar(30) DEFAULT NULL COMMENT '中继名称',
  `type` varchar(30) DEFAULT NULL COMMENT '中继类型(SIP, PRA)',
  `cid` varchar(30) DEFAULT NULL COMMENT '发送主叫',
  `prefix` varchar(30) DEFAULT NULL,
  `keepcid` varchar(30) DEFAULT NULL COMMENT '许主叫变换',
  `maxchans` varchar(30) DEFAULT NULL COMMENT '最大通道数',
  `fail` varchar(30) DEFAULT NULL,
  `disable` varchar(30) DEFAULT NULL COMMENT '是否有效',
  `sip_account` varchar(30) DEFAULT NULL COMMENT '中继名称',
  `sip_host` varchar(30) DEFAULT NULL COMMENT '对端地址',
  `sip_port` varchar(30) DEFAULT NULL COMMENT '对端端口',
  `sip_username` varchar(30) DEFAULT NULL COMMENT '用户名',
  `sip_secret` varchar(30) DEFAULT NULL COMMENT '密码',
  `sip_type` varchar(30) DEFAULT NULL COMMENT '中继类型(peer, user)',
  `sip_context` varchar(30) DEFAULT NULL COMMENT '拨号方案',
  `sip_nat` varchar(30) DEFAULT NULL COMMENT 'NAT穿透',
  `callerchange` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='PBX中继表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pbx_trunk`
--

LOCK TABLES `pbx_trunk` WRITE;
/*!40000 ALTER TABLE `pbx_trunk` DISABLE KEYS */;
INSERT INTO `pbx_trunk` VALUES (1,'SIPTo150','SIP','999999999',NULL,'on','5',NULL,'off',NULL,'183.247.163.233','5080',NULL,NULL,'peer','from-internal','yes',NULL);
/*!40000 ALTER TABLE `pbx_trunk` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-12-11 20:41:58
