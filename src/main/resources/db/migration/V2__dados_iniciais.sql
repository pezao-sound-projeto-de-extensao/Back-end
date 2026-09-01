-- Dados de referência mínimos para a aplicação funcionar.
-- INSERT IGNORE porque esta migration também roda em bancos que já existem
-- (eles entram pelo baseline na V1 e recebem da V2 em diante).

INSERT IGNORE INTO `permissoes` (`id`, `nome`, `descricao`) VALUES
  (1, 'GERENCIAR_USUARIOS', 'Permite gerenciar usuários do sistema'),
  (2, 'GERENCIAR_CARGOS',   'Permite gerenciar cargos do sistema'),
  (3, 'CADASTRAR_ITENS',    'Permite cadastrar novos itens no estoque'),
  (4, 'EDITAR_ITENS',       'Permite editar itens do estoque'),
  (5, 'EXCLUIR_ITENS',      'Permite excluir ou inativar itens do estoque'),
  (6, 'REGISTRAR_ENTRADA',  'Permite registrar entradas de estoque'),
  (7, 'REGISTRAR_SAIDA',    'Permite registrar saídas de estoque'),
  (8, 'VER_RELATORIOS',     'Permite visualizar relatórios do sistema');

INSERT IGNORE INTO `cargos` (`id`, `nome`, `descricao`) VALUES
  (1, 'Administrador', 'Cargo com acesso total ao sistema');

INSERT IGNORE INTO `cargo_permissoes` (`cargo_id`, `permissao_id`) VALUES
  (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8);

-- Usuário inicial: adm@email.com com a senha padrão (PezaoSenha).
-- O primeiro login é bloqueado até a troca de senha em /api/auth/trocar-senha.
INSERT IGNORE INTO `usuarios` (`id`, `cargo_id`, `nome`, `email`, `senha_hash`, `ativo`) VALUES
  (1, 1, 'ADM', 'adm@email.com', '$2a$10$FsYqdmjvhmmXTShZNqhGt.g8HHniASRM49aw5FP0GKKD1T0T4Rlkq', 1);

INSERT IGNORE INTO `unidades` (`id`, `nome`, `abreviacao`) VALUES
  (1, 'Unidade', 'UN'),
  (2, 'Par',     'PR'),
  (3, 'Metro',   'M');
