-- Unifica a vw_alertas_estoque: no bd.sql ela existia como TABELA, sem as colunas
-- categoria_nome e unidade_medida que a entidade Alerta mapeia. Aqui ela passa a
-- ser sempre uma VIEW, com as colunas que o back-end espera.

-- A ordem importa: se já for uma view, o DROP VIEW resolve e o DROP TABLE não
-- encontra nada; se for a tabela antiga do dump, acontece o inverso.
DROP VIEW IF EXISTS `vw_alertas_estoque`;
DROP TABLE IF EXISTS `vw_alertas_estoque`;

CREATE VIEW `vw_alertas_estoque` AS
SELECT
    `i`.`id`                AS `item_id`,
    `i`.`atualizado_em`     AS `data_ocorrencia`,
    `i`.`nome`              AS `item_nome`,
    `c`.`nome`              AS `categoria_nome`,
    `u`.`abreviacao`        AS `unidade_medida`,
    `i`.`quantidade_atual`  AS `quantidade_atual`,
    `i`.`quantidade_minima` AS `quantidade_minima`,
    CASE
        WHEN `i`.`quantidade_atual` = 0 THEN 'zerado'
        ELSE 'estoque_baixo'
    END AS `tipo_alerta`
FROM `itens` `i`
INNER JOIN `categorias` `c` ON `i`.`categoria_id` = `c`.`id`
INNER JOIN `unidades`   `u` ON `i`.`unidade_id`   = `u`.`id`
WHERE `i`.`ativo` = 1
  AND `i`.`quantidade_atual` <= `i`.`quantidade_minima`;
