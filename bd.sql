-- MySQL dump 10.13  Distrib 8.0.44, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: stockflow
-- ------------------------------------------------------
-- Server version	8.0.45

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
-- Table structure for table `alertas`
--

DROP TABLE IF EXISTS `alertas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `alertas` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID do alerta',
  `item_id` int NOT NULL COMMENT 'FK para itens',
  `tipo` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'estoque_baixo ou zerado',
  `visualizado` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Se já foi visto no dashboard',
  `criado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Quando o alerta foi gerado',
  PRIMARY KEY (`id`),
  KEY `fk_alertas_item` (`item_id`),
  CONSTRAINT `fk_alertas_item` FOREIGN KEY (`item_id`) REFERENCES `itens` (`id`),
  CONSTRAINT `chk_alertas_tipo` CHECK ((`tipo` in (_utf8mb4'estoque_baixo',_utf8mb4'zerado')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Alertas gerados quando estoque cai abaixo do mínimo';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `alertas`
--

LOCK TABLES `alertas` WRITE;
/*!40000 ALTER TABLE `alertas` DISABLE KEYS */;
/*!40000 ALTER TABLE `alertas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cargo_permissoes`
--

DROP TABLE IF EXISTS `cargo_permissoes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cargo_permissoes` (
  `cargo_id` int NOT NULL COMMENT 'FK para cargos',
  `permissao_id` int NOT NULL COMMENT 'FK para permissoes',
  PRIMARY KEY (`cargo_id`,`permissao_id`),
  KEY `fk_cargo_permissoes_permissao` (`permissao_id`),
  CONSTRAINT `fk_cargo_permissoes_cargo` FOREIGN KEY (`cargo_id`) REFERENCES `cargos` (`id`),
  CONSTRAINT `fk_cargo_permissoes_permissao` FOREIGN KEY (`permissao_id`) REFERENCES `permissoes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Relação entre cargos e suas permissões';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cargo_permissoes`
--

LOCK TABLES `cargo_permissoes` WRITE;
/*!40000 ALTER TABLE `cargo_permissoes` DISABLE KEYS */;
INSERT INTO `cargo_permissoes` VALUES (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8);
/*!40000 ALTER TABLE `cargo_permissoes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cargos`
--

DROP TABLE IF EXISTS `cargos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cargos` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID do cargo',
  `nome` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Denominação do cargo — ex: Administrador, Operador',
  `descricao` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Descrição do cargo',
  `criado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Data de criação',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Cargos dos usuários do sistema';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cargos`
--

LOCK TABLES `cargos` WRITE;
/*!40000 ALTER TABLE `cargos` DISABLE KEYS */;
INSERT INTO `cargos` VALUES (1,'Administrador','Cargo com acesso total ao sistema','2026-04-14 00:51:33');
/*!40000 ALTER TABLE `cargos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categorias`
--

DROP TABLE IF EXISTS `categorias`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categorias` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID da categoria',
  `nome` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Nome da categoria',
  `criado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Data de criação',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Categorias de produto — ex: Som automotivo, Baterias';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categorias`
--

LOCK TABLES `categorias` WRITE;
/*!40000 ALTER TABLE `categorias` DISABLE KEYS */;
/*!40000 ALTER TABLE `categorias` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `itens`
--

DROP TABLE IF EXISTS `itens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `itens` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID do item',
  `categoria_id` int NOT NULL COMMENT 'FK para categorias',
  `unidade_id` int NOT NULL COMMENT 'FK para unidades',
  `nome` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Nome do item',
  `quantidade_atual` int NOT NULL DEFAULT '0' COMMENT 'Estoque atual',
  `quantidade_minima` int NOT NULL DEFAULT '0' COMMENT 'Abaixo disso gera alerta',
  `preco_custo` double DEFAULT NULL,
  `preco_venda` double DEFAULT NULL,
  `ativo` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Inativar sem deletar',
  `criado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Data de cadastro',
  `atualizado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Última atualização',
  PRIMARY KEY (`id`),
  KEY `fk_itens_categoria` (`categoria_id`),
  KEY `fk_itens_unidade` (`unidade_id`),
  CONSTRAINT `fk_itens_categoria` FOREIGN KEY (`categoria_id`) REFERENCES `categorias` (`id`),
  CONSTRAINT `fk_itens_unidade` FOREIGN KEY (`unidade_id`) REFERENCES `unidades` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Itens do estoque do Pezão Sound';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `itens`
--

LOCK TABLES `itens` WRITE;
/*!40000 ALTER TABLE `itens` DISABLE KEYS */;
/*!40000 ALTER TABLE `itens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `itens_seq`
--

DROP TABLE IF EXISTS `itens_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `itens_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `itens_seq`
--

LOCK TABLES `itens_seq` WRITE;
/*!40000 ALTER TABLE `itens_seq` DISABLE KEYS */;
INSERT INTO `itens_seq` VALUES (1);
/*!40000 ALTER TABLE `itens_seq` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movimentacoes`
--

DROP TABLE IF EXISTS `movimentacoes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movimentacoes` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID da movimentação',
  `item_id` int NOT NULL COMMENT 'FK para itens',
  `usuario_id` int NOT NULL COMMENT 'FK para usuários — quem registrou',
  `tipo` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'entrada ou saida',
  `quantidade` int NOT NULL COMMENT 'Quantidade movimentada',
  `estoque_antes` int NOT NULL COMMENT 'Estoque antes da movimentação',
  `estoque_depois` int NOT NULL COMMENT 'Estoque após a movimentação',
  `data` date NOT NULL COMMENT 'Data da movimentação',
  `observacao` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Anotação opcional — ex: NF, fornecedor',
  `criado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Data do registro',
  PRIMARY KEY (`id`),
  KEY `fk_movimentacoes_item` (`item_id`),
  KEY `fk_movimentacoes_usuario` (`usuario_id`),
  CONSTRAINT `fk_movimentacoes_item` FOREIGN KEY (`item_id`) REFERENCES `itens` (`id`),
  CONSTRAINT `fk_movimentacoes_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `chk_movimentacoes_tipo` CHECK ((`tipo` in (_utf8mb4'entrada',_utf8mb4'saida')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Histórico de todas as entradas e saídas';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movimentacoes`
--

LOCK TABLES `movimentacoes` WRITE;
/*!40000 ALTER TABLE `movimentacoes` DISABLE KEYS */;
/*!40000 ALTER TABLE `movimentacoes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permissoes`
--

DROP TABLE IF EXISTS `permissoes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permissoes` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID da permissão',
  `nome` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Identificador da permissão — ex: cadastrar_produto',
  `descricao` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'O que essa permissão permite fazer',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Permissões disponíveis no sistema';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permissoes`
--

LOCK TABLES `permissoes` WRITE;
/*!40000 ALTER TABLE `permissoes` DISABLE KEYS */;
INSERT INTO `permissoes` VALUES (1,'GERENCIAR_USUARIOS','Permite gerenciar usuários do sistema'),(2,'GERENCIAR_CARGOS','Permite gerenciar cargos do sistema'),(3,'CADASTRAR_ITENS','Permite cadastrar novos itens no estoque'),(4,'EDITAR_ITENS','Permite editar itens do estoque'),(5,'EXCLUIR_ITENS','Permite excluir ou inativar itens do estoque'),(6,'REGISTRAR_ENTRADA','Permite registrar entradas de estoque'),(7,'REGISTRAR_SAIDA','Permite registrar saídas de estoque'),(8,'VER_RELATORIOS','Permite visualizar relatórios do sistema');
/*!40000 ALTER TABLE `permissoes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `unidades`
--

DROP TABLE IF EXISTS `unidades`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `unidades` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID da unidade',
  `nome` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Nome completo — ex: Unidade',
  `abreviacao` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Unidades de medida dos produtos';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `unidades`
--

LOCK TABLES `unidades` WRITE;
/*!40000 ALTER TABLE `unidades` DISABLE KEYS */;
/*!40000 ALTER TABLE `unidades` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID do usuário',
  `cargo_id` int NOT NULL COMMENT 'FK para cargo',
  `nome` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Nome completo',
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Email de login',
  `senha_hash` varchar(2000) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Senha criptografada',
  `ativo` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Usuário ativo ou inativo',
  `ultimo_acesso` datetime DEFAULT NULL COMMENT 'Último login registrado',
  `criado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Data de cadastro',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `fk_usuarios_cargo` (`cargo_id`),
  CONSTRAINT `fk_usuarios_cargo` FOREIGN KEY (`cargo_id`) REFERENCES `cargos` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Usuários com acesso ao sistema';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (1,1,'ADM','adm@email.com','$2a$10$FsYqdmjvhmmXTShZNqhGt.g8HHniASRM49aw5FP0GKKD1T0T4Rlkq',1,'2026-04-14 00:58:47','2026-04-14 00:51:41');
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vw_alertas_estoque`
--

DROP TABLE IF EXISTS `vw_alertas_estoque`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vw_alertas_estoque` (
  `item_id` int NOT NULL,
  `data_ocorrencia` datetime(6) DEFAULT NULL,
  `item_nome` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `quantidade_atual` int DEFAULT NULL,
  `quantidade_minima` int DEFAULT NULL,
  `tipo_alerta` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vw_alertas_estoque`
--

LOCK TABLES `vw_alertas_estoque` WRITE;
/*!40000 ALTER TABLE `vw_alertas_estoque` DISABLE KEYS */;
/*!40000 ALTER TABLE `vw_alertas_estoque` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-13 22:04:02
