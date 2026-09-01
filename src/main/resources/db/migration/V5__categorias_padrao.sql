-- Categorias previstas no escopo das telas de Produtos.
-- Insere apenas as que ainda não existem, comparando sem diferenciar maiúsculas
-- de minúsculas: bancos que já têm "Som Automotivo" não ganham uma duplicata
-- "Som automotivo". Categorias criadas pelo time fora dessa lista são mantidas.

INSERT INTO `categorias` (`nome`)
SELECT * FROM (
    SELECT 'Som automotivo'      AS nome
    UNION ALL SELECT 'Materiais de capa'
    UNION ALL SELECT 'Acessórios elétricos'
    UNION ALL SELECT 'Iluminação'
    UNION ALL SELECT 'Baterias'
    UNION ALL SELECT 'Outros'
) AS padrao
WHERE NOT EXISTS (
    SELECT 1 FROM `categorias` c WHERE LOWER(c.`nome`) = LOWER(padrao.nome)
);
