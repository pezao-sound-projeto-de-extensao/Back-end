
-- OBSOLETO — substituído por src/main/resources/db/migration/V4__view_alertas_estoque.sql

CREATE OR REPLACE VIEW `vw_alertas_estoque` AS
SELECT
    `i`.`id` AS `item_id`,
    `i`.`atualizado_em` AS `data_ocorrencia`,
    `i`.`nome` AS `item_nome`,
    `c`.`nome` AS `categoria_nome`,
    `u`.`abreviacao` AS `unidade_medida`,
    `i`.`quantidade_atual` AS `quantidade_atual`,
    `i`.`quantidade_minima` AS `quantidade_minima`,
    CASE
        WHEN `i`.`quantidade_atual` = 0 THEN 'zerado'
        ELSE 'estoque_baixo'
    END AS `tipo_alerta`
FROM
    `itens` `i`
INNER JOIN
    `categorias` `c` ON `i`.`categoria_id` = `c`.`id`
INNER JOIN
    `unidades` `u` ON `i`.`unidade_id` = `u`.`id`
WHERE
    `i`.`ativo` = 1
    AND `i`.`quantidade_atual` <= `i`.`quantidade_minima`;
