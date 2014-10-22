
#用户表
DROP TABLE IF EXISTS `merchant_appuser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `merchant_appuser` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `auth_value` int(11) NOT NULL,
  `username` varchar(200) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `password` varchar(64) DEFAULT NULL,
  `gender` varchar(10) DEFAULT NULL,
  `description` varchar(200) DEFAULT NULL,
  `createtime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=61 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;


#允许的设备表,存储网关 MAC
DROP TABLE IF EXISTS `merchant_device`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `merchant_device` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mac` varchar(50) NOT NULL,
  `status` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=24 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;


#日志表
DROP TABLE IF EXISTS `merchant_wifiuserlog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `merchant_wifiuserlog` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `phonemac` varchar(50) DEFAULT NULL,
  `appuser` varchar(50) DEFAULT NULL,
  `device` varchar(64) DEFAULT NULL,
  `ip` varchar(64) DEFAULT NULL,
  `incoming` int(11) DEFAULT NULL,
  `outgoing` int(11) DEFAULT NULL,
  `login_time` datetime DEFAULT NULL,
  `online_time` int(11) DEFAULT NULL,
  `isonline` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=285 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
