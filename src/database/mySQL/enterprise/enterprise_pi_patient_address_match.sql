-- MySQL dump 10.13  Distrib 8.0.17, for Win64 (x86_64)
--
-- Host: localhost    Database: subscriber_pi
-- ------------------------------------------------------
-- Server version	5.7.23-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `patient_address_match`
--

DROP TABLE IF EXISTS `patient_address_match`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `patient_address_match` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `patient_id` bigint(20) NOT NULL,
  `person_id` bigint(20) NOT NULL,
  `uprn` bigint(20) NOT NULL,
  `status` tinyint(1) DEFAULT NULL,
  `classification` varchar(45) CHARACTER SET utf8 DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `xcoordinate` double DEFAULT NULL,
  `ycoordinate` double DEFAULT NULL,
  `qualifier` varchar(50) CHARACTER SET utf8 DEFAULT NULL,
  `algorithm` varchar(50) CHARACTER SET utf8 DEFAULT NULL,
  `match_date` datetime DEFAULT NULL,
  `abp_address_number` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `abp_address_street` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `abp_address_locality` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `abp_address_town` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `abp_address_postcode` varchar(10) CHARACTER SET utf8 DEFAULT NULL,
  `abp_address_organization` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `match_pattern_postcode` varchar(20) CHARACTER SET utf8 DEFAULT NULL,
  `match_pattern_street` varchar(20) CHARACTER SET utf8 DEFAULT NULL,
  `match_pattern_number` varchar(20) CHARACTER SET utf8 DEFAULT NULL,
  `match_pattern_building` varchar(20) CHARACTER SET utf8 DEFAULT NULL,
  `match_pattern_flat` varchar(20) CHARACTER SET utf8 DEFAULT NULL,
  `algorithm_version` varchar(20) CHARACTER SET utf8 DEFAULT NULL,
  `epoc` varchar(20) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `patient_address_uprn_index` (`uprn`),
  KEY `patient_patient_id` (`patient_id`),
  CONSTRAINT `patient_uprn_patient_id_fk` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='stores uprn details for addresses';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-01-24 11:19:27
