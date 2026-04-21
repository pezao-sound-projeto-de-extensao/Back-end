DROP TABLE IF EXISTS vw_alertas_estoque;

CREATE OR REPLACE VIEW vw_alertas_estoque AS
SELECT
    id AS item_id,
    nome AS item_nome,
    quantidade_atual,
    quantidade_minima,
    CASE
        WHEN quantidade_atual = 0 THEN 'estoque_zerado'
        ELSE 'estoque_baixo'
        END AS tipo_alerta,
    atualizado_em AS data_ocorrencia
FROM itens
WHERE quantidade_atual <= quantidade_minima
  AND ativo = true;