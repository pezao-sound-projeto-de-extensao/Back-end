-- Tabela dos refresh tokens. Até aqui ela só existia porque o ddl-auto=update
-- criava na marra em cada máquina — não estava em nenhum script versionado.

CREATE TABLE IF NOT EXISTS `refresh_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID do refresh token',
  `usuario_id` int NOT NULL COMMENT 'FK para usuarios',
  `token_hash` varchar(100) NOT NULL COMMENT 'Hash BCrypt do segredo do token',
  `expira_em` datetime(6) NOT NULL COMMENT 'Momento em que o token expira',
  `revogado_em` datetime(6) DEFAULT NULL COMMENT 'Momento da revogação — nulo enquanto válido',
  PRIMARY KEY (`id`),
  KEY `idx_refresh_token_usuario` (`usuario_id`),
  KEY `idx_refresh_token_expira_em` (`expira_em`),
  CONSTRAINT `fk_refresh_tokens_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Refresh tokens emitidos no login';
