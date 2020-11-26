DROP TABLE IF EXISTS `cqc_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cqc_audit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `subscriber_id` bigint(20) DEFAULT NULL,
  `text` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3214 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cqc_id_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cqc_id_map` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cqc_id` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `guid` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=167600 DEFAULT CHARSET=latin1 COLLATE=latin1_bin;