-- Baseline do esquema do StockFlow.
-- Reproduz o estado do bd.sql. Bancos que já existem recebem esta versão como
-- baseline (spring.flyway.baseline-on-migrate=true) e pulam direto para a V2.

CREATE TABLE IF NOT EXISTS `permissoes` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID da permissão',
  `nome` varchar(255) NOT NULL COMMENT 'Identificador da permissão',
  `descricao` varchar(255) NOT NULL COMMENT 'O que essa permissão permite fazer',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permissoes_nome` (`nome`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Permissões disponíveis no sistema';

CREATE TABLE IF NOT EXISTS `cargos` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID do cargo',
  `nome` varchar(255) NOT NULL COMMENT 'Denominação do cargo',
  `descricao` varchar(255) DEFAULT NULL COMMENT 'Descrição do cargo',
  `criado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Data de criação',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Cargos dos usuários do sistema';

CREATE TABLE IF NOT EXISTS `cargo_permissoes` (
  `cargo_id` int NOT NULL COMMENT 'FK para cargos',
  `permissao_id` int NOT NULL COMMENT 'FK para permissoes',
  PRIMARY KEY (`cargo_id`,`permissao_id`),
  KEY `fk_cargo_permissoes_permissao` (`permissao_id`),
  CONSTRAINT `fk_cargo_permissoes_cargo` FOREIGN KEY (`cargo_id`) REFERENCES `cargos` (`id`),
  CONSTRAINT `fk_cargo_permissoes_permissao` FOREIGN KEY (`permissao_id`) REFERENCES `permissoes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Relação entre cargos e suas permissões';

CREATE TABLE IF NOT EXISTS `usuarios` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID do usuário',
  `cargo_id` int NOT NULL COMMENT 'FK para cargo',
  `nome` varchar(255) NOT NULL COMMENT 'Nome completo',
  `email` varchar(255) NOT NULL COMMENT 'Email de login',
  `senha_hash` varchar(2000) NOT NULL COMMENT 'Senha criptografada',
  `ativo` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Usuário ativo ou inativo',
  `ultimo_acesso` datetime DEFAULT NULL COMMENT 'Último login registrado',
  `criado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Data de cadastro',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `fk_usuarios_cargo` (`cargo_id`),
  CONSTRAINT `fk_usuarios_cargo` FOREIGN KEY (`cargo_id`) REFERENCES `cargos` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Usuários com acesso ao sistema';

CREATE TABLE IF NOT EXISTS `categorias` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID da categoria',
  `nome` varchar(255) NOT NULL COMMENT 'Nome da categoria',
  `criado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Data de criação',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Categorias de produto';

CREATE TABLE IF NOT EXISTS `unidades` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID da unidade',
  `nome` varchar(255) NOT NULL COMMENT 'Nome completo da unidade',
  `abreviacao` varchar(255) DEFAULT NULL COMMENT 'Abreviação — ex: UN, PR, M',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Unidades de medida dos produtos';

CREATE TABLE IF NOT EXISTS `itens` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID do item',
  `categoria_id` int NOT NULL COMMENT 'FK para categorias',
  `unidade_id` int NOT NULL COMMENT 'FK para unidades',
  `nome` varchar(255) NOT NULL COMMENT 'Nome do item',
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

CREATE TABLE IF NOT EXISTS `movimentacoes` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID da movimentação',
  `item_id` int NOT NULL COMMENT 'FK para itens',
  `usuario_id` int NOT NULL COMMENT 'FK para usuários — quem registrou',
  `tipo` varchar(20) NOT NULL COMMENT 'entrada ou saida',
  `quantidade` int NOT NULL COMMENT 'Quantidade movimentada',
  `estoque_antes` int NOT NULL COMMENT 'Estoque antes da movimentação',
  `estoque_depois` int NOT NULL COMMENT 'Estoque após a movimentação',
  `data` date NOT NULL COMMENT 'Data da movimentação',
  `observacao` varchar(255) DEFAULT NULL COMMENT 'Anotação opcional',
  `criado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Data do registro',
  PRIMARY KEY (`id`),
  KEY `fk_movimentacoes_item` (`item_id`),
  KEY `fk_movimentacoes_usuario` (`usuario_id`),
  CONSTRAINT `fk_movimentacoes_item` FOREIGN KEY (`item_id`) REFERENCES `itens` (`id`),
  CONSTRAINT `fk_movimentacoes_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `chk_movimentacoes_tipo` CHECK ((`tipo` in ('entrada','saida')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Histórico de todas as entradas e saídas';

CREATE TABLE IF NOT EXISTS `imagem_produto` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID da imagem do produto',
  `item_id` int NOT NULL COMMENT 'FK para itens',
  `nome_arquivo` varchar(255) NOT NULL COMMENT 'Nome original do arquivo enviado',
  `caminho` varchar(500) NOT NULL COMMENT 'Caminho relativo do arquivo no storage',
  `mime_type` varchar(100) NOT NULL COMMENT 'Tipo MIME do arquivo',
  `tamanho_bytes` int DEFAULT NULL COMMENT 'Tamanho do arquivo em bytes',
  `criado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Data do upload',
  PRIMARY KEY (`id`),
  KEY `fk_imagem_produto_item` (`item_id`),
  CONSTRAINT `fk_imagem_produto_item` FOREIGN KEY (`item_id`) REFERENCES `itens` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Imagens vinculadas aos itens';

CREATE TABLE IF NOT EXISTS `nota_entrada` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID do arquivo da nota',
  `movimentacao_id` int NOT NULL COMMENT 'FK para movimentacoes',
  `tipo` varchar(20) NOT NULL COMMENT 'imagem ou nota_fiscal',
  `nome_arquivo` varchar(255) NOT NULL COMMENT 'Nome original do arquivo enviado',
  `caminho` varchar(500) NOT NULL COMMENT 'Caminho relativo do arquivo no storage',
  `mime_type` varchar(100) DEFAULT NULL COMMENT 'Tipo MIME do arquivo',
  `tamanho_bytes` int DEFAULT NULL COMMENT 'Tamanho do arquivo em bytes',
  `criado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Data do upload',
  PRIMARY KEY (`id`),
  KEY `fk_nota_entrada_movimentacao` (`movimentacao_id`),
  CONSTRAINT `fk_nota_entrada_movimentacao` FOREIGN KEY (`movimentacao_id`) REFERENCES `movimentacoes` (`id`),
  CONSTRAINT `chk_nota_entrada_tipo` CHECK ((`tipo` in ('imagem','nota_fiscal')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Arquivos vinculados a movimentação';

CREATE TABLE IF NOT EXISTS `alertas` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID do alerta',
  `item_id` int NOT NULL COMMENT 'FK para itens',
  `tipo` varchar(30) NOT NULL COMMENT 'estoque_baixo ou zerado',
  `visualizado` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Se já foi visto no dashboard',
  `criado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Quando o alerta foi gerado',
  PRIMARY KEY (`id`),
  KEY `fk_alertas_item` (`item_id`),
  CONSTRAINT `fk_alertas_item` FOREIGN KEY (`item_id`) REFERENCES `itens` (`id`),
  CONSTRAINT `chk_alertas_tipo` CHECK ((`tipo` in ('estoque_baixo','zerado')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Alertas gerados quando estoque cai abaixo do mínimo';
