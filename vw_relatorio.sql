DROP TABLE IF EXISTS vw_relatorio;

CREATE OR REPLACE VIEW vw_relatorio AS
SELECT
    (SELECT COUNT(*) FROM itens WHERE ativo = true) AS total_itens,
    (SELECT COUNT(*) FROM itens WHERE quantidade_atual > quantidade_minima AND ativo = true) AS itens_ok,
    (SELECT COUNT(*) FROM itens WHERE quantidade_atual <= quantidade_minima AND quantidade_atual > 0 AND ativo = true) AS itens_alerta,
    (SELECT COUNT(*) FROM itens WHERE quantidade_atual = 0 AND ativo = true) AS itens_zerados,
    -- Dados da última movimentação
    (SELECT i.nome FROM movimentacoes m JOIN itens i ON m.item_id = i.id ORDER BY m.criado_em DESC LIMIT 1) AS ultima_mov_item,
    (SELECT tipo FROM movimentacoes ORDER BY criado_em DESC LIMIT 1) AS ultima_mov_tipo,
    (SELECT criado_em FROM movimentacoes ORDER BY criado_em DESC LIMIT 1) AS ultima_mov_data;