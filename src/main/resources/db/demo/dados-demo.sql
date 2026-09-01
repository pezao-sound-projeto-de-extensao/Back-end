-- Dados de demonstração (categorias e itens de exemplo do bd.sql antigo).
-- NÃO é uma migration: rode manualmente quando quiser popular um banco de teste.
--   mysql -u root -p stockflow < src/main/resources/db/demo/dados-demo.sql

INSERT INTO categorias (nome) VALUES
    ('Som Automotivo'),
    ('Baterias'),
    ('Cabos e Conectores'),
    ('Iluminação');

INSERT INTO itens (categoria_id, unidade_id, nome, quantidade_atual, quantidade_minima, preco_custo, preco_venda, ativo) VALUES
    (1, 1, 'Módulo Amplificador 400W', 8, 3, 180.00, 320.00, 1),
    (1, 2, 'Alto-falante 6x9 200W',    4, 2,  95.00, 170.00, 1),
    (1, 1, 'Subwoofer 12" 600W',       2, 2, 230.00, 420.00, 1),  -- estoque no limite
    (2, 1, 'Bateria 60Ah',             1, 3, 310.00, 520.00, 1),  -- abaixo do mínimo
    (3, 3, 'Cabo RCA 5m',             15, 5,  12.00,  25.00, 1),
    (4, 1, 'Strobo LED RGB',           0, 2,  45.00,  89.00, 1),  -- zerado
    (1, 1, 'Tweeters Triaxial 120W',   6, 2,  55.00,  95.00, 0);  -- inativo
