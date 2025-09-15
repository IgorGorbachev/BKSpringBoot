-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: localhost    Database: test
-- ------------------------------------------------------
-- Server version	8.0.41

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
-- Table structure for table `task`
--

DROP TABLE IF EXISTS `task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `text` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=294 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task`
--

LOCK TABLES `task` WRITE;
/*!40000 ALTER TABLE `task` DISABLE KEYS */;
INSERT INTO `task` VALUES (32,'Было бы замечательно, если бы был поиск по артикулу)'),(170,'С Мамаева недобрал 630р (нужно будет раскидать по другим заказам)(Возможно + ещё 830 за наконечник 770.5800800 от которого не вернул пакет)'),(181,'Руслан Отдать Ролик на пробу ТОПКОВЕР и Ремень или гейтс или СК премиум (отдали Ремень SK-7730014-01PL и ролик T0955-4006)'),(198,'толстик, бензонасос в баке  1J0919087J - (SFP 1818 - 1J0919051D) И сетка-фильтр для насоса! и посмотреть замок зажигания 4B0 905 851B'),(220,'Интернет - 750р в месяц , 28 числа каждого месяца!'),(228,'Лена соседка оплатила за интернет 250р 29.08.2025'),(256,'ООО «Озбэттериз» Предложение по Аккумуляторам на WHATSAPP'),(258,'Михаил Мерс Стоянка - Оплатил 500р ( 80р ещё должен)'),(276,'Евгений, Памятники оплатил 250р 03.09.2025'),(277,'Рябцов пожаловался на фильтр маршалл - который сепаратор - Резиа на фильтре тонкая и пропускает воздух'),(282,'Санек Горбунов Сальник ступицы на прицеп!, еще фото в ватсапп'),(285,'Рябцов попросил привезти креатек'),(291,'рено 8787, левое зеркло крышка'),(292,'иванов, на  ман передние колодки');
/*!40000 ALTER TABLE `task` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-07 11:57:28
