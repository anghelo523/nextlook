-- Fotos de las prendas y catalogo ampliado.
-- La foto vive en su propia tabla (bytea) para que listar el catalogo no cargue las imagenes:
-- prendas.imagen_version solo dice si hay foto y sirve para versionar su URL (cache del navegador).
ALTER TABLE prendas ADD COLUMN IF NOT EXISTS imagen_version BIGINT;

CREATE TABLE IF NOT EXISTS prenda_imagenes (
    id_prenda BIGINT NOT NULL REFERENCES prendas(id),
    tipo VARCHAR(40) NOT NULL,
    contenido BYTEA NOT NULL,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (id_prenda)
);

-- El calzado ya no es solo zapatillas; y la casaca cortavientos se vende en el color de su foto
UPDATE categorias SET nombre = 'Calzado' WHERE id = 5;
UPDATE variantes SET color = 'Terracota' WHERE id IN (9, 10);

INSERT INTO categorias (id, nombre) VALUES (6, 'Polerones');
INSERT INTO categorias (id, nombre) VALUES (7, 'Camisas');
INSERT INTO categorias (id, nombre) VALUES (8, 'Accesorios');

-- Poleron hoodie oversize
INSERT INTO prendas (id, nombre, descripcion, marca, genero, precio, activo, id_categoria) VALUES (6, 'Poleron hoodie oversize', 'Hoodie de french terry con capucha y bolsillo canguro, calce amplio', 'NextLook Street', 'UNISEX', 139.90, TRUE, 6);
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (17, 6, 'S', 'Gris jaspeado', 'NL-006-S-GRI', TRUE);  -- variante 17
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (18, 6, 'M', 'Gris jaspeado', 'NL-006-M-GRI', TRUE);  -- variante 18
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (19, 6, 'L', 'Gris jaspeado', 'NL-006-L-GRI', TRUE);  -- variante 19

-- Jogger chino
INSERT INTO prendas (id, nombre, descripcion, marca, genero, precio, activo, id_categoria) VALUES (7, 'Jogger chino', 'Pantalon chino con punos elasticos y bolsillos laterales', 'Urban Wind', 'HOMBRE', 109.90, TRUE, 2);
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (20, 7, '30', 'Beige', 'NL-007-30-BEI', TRUE);  -- variante 20
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (21, 7, '32', 'Beige', 'NL-007-32-BEI', TRUE);  -- variante 21
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (22, 7, '34', 'Beige', 'NL-007-34-BEI', TRUE);  -- variante 22

-- Casaca denim clasica
INSERT INTO prendas (id, nombre, descripcion, marca, genero, precio, activo, id_categoria) VALUES (8, 'Casaca denim clasica', 'Casaca de jean lavado claro con botones metalicos', 'Denim Co', 'UNISEX', 199.90, TRUE, 3);
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (23, 8, 'S', 'Azul claro', 'NL-008-S-AZU', TRUE);  -- variante 23
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (24, 8, 'M', 'Azul claro', 'NL-008-M-AZU', TRUE);  -- variante 24
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (25, 8, 'L', 'Azul claro', 'NL-008-L-AZU', TRUE);  -- variante 25

-- Casaca biker
INSERT INTO prendas (id, nombre, descripcion, marca, genero, precio, activo, id_categoria) VALUES (9, 'Casaca biker', 'Casaca estilo motociclista de cuero sintetico con cierres asimetricos', 'Noir', 'UNISEX', 289.90, TRUE, 3);
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (26, 9, 'S', 'Negro', 'NL-009-S-NEG', TRUE);  -- variante 26
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (27, 9, 'M', 'Negro', 'NL-009-M-NEG', TRUE);  -- variante 27
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (28, 9, 'L', 'Negro', 'NL-009-L-NEG', TRUE);  -- variante 28

-- Gorra lavada
INSERT INTO prendas (id, nombre, descripcion, marca, genero, precio, activo, id_categoria) VALUES (10, 'Gorra lavada', 'Gorra de algodon con efecto lavado y correa regulable', 'NextLook Street', 'UNISEX', 59.90, TRUE, 8);
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (29, 10, 'UNICA', 'Gris oscuro', 'NL-010-UNI-GRI', TRUE);  -- variante 29

-- Camisa chambray
INSERT INTO prendas (id, nombre, descripcion, marca, genero, precio, activo, id_categoria) VALUES (11, 'Camisa chambray', 'Camisa de chambray con microestampado y manga larga', 'NextLook Basics', 'HOMBRE', 119.90, TRUE, 7);
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (30, 11, 'S', 'Azul', 'NL-011-S-AZU', TRUE);  -- variante 30
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (31, 11, 'M', 'Azul', 'NL-011-M-AZU', TRUE);  -- variante 31
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (32, 11, 'L', 'Azul', 'NL-011-L-AZU', TRUE);  -- variante 32

-- Camisa franela a cuadros
INSERT INTO prendas (id, nombre, descripcion, marca, genero, precio, activo, id_categoria) VALUES (12, 'Camisa franela a cuadros', 'Camisa de franela suave con estampado escoces', 'Urban Wind', 'HOMBRE', 99.90, TRUE, 7);
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (33, 12, 'M', 'Verde', 'NL-012-M-VER', TRUE);  -- variante 33
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (34, 12, 'L', 'Verde', 'NL-012-L-VER', TRUE);  -- variante 34

-- Blazer slim azul noche
INSERT INTO prendas (id, nombre, descripcion, marca, genero, precio, activo, id_categoria) VALUES (13, 'Blazer slim azul noche', 'Blazer entallado de lana fria con solapa de muesca', 'Noir', 'HOMBRE', 349.90, TRUE, 3);
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (35, 13, 'S', 'Azul noche', 'NL-013-S-AZU', TRUE);  -- variante 35
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (36, 13, 'M', 'Azul noche', 'NL-013-M-AZU', TRUE);  -- variante 36
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (37, 13, 'L', 'Azul noche', 'NL-013-L-AZU', TRUE);  -- variante 37

-- Vestido negro cuello alto
INSERT INTO prendas (id, nombre, descripcion, marca, genero, precio, activo, id_categoria) VALUES (14, 'Vestido negro cuello alto', 'Vestido midi de punto con cuello alto y manga larga', 'Noir', 'MUJER', 179.90, TRUE, 4);
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (38, 14, 'S', 'Negro', 'NL-014-S-NEG', TRUE);  -- variante 38
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (39, 14, 'M', 'Negro', 'NL-014-M-NEG', TRUE);  -- variante 39

-- Vestido denim
INSERT INTO prendas (id, nombre, descripcion, marca, genero, precio, activo, id_categoria) VALUES (15, 'Vestido denim', 'Vestido corto de denim con botones al frente y manga corta', 'Denim Co', 'MUJER', 149.90, TRUE, 4);
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (40, 15, 'S', 'Azul', 'NL-015-S-AZU', TRUE);  -- variante 40
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (41, 15, 'M', 'Azul', 'NL-015-M-AZU', TRUE);  -- variante 41
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (42, 15, 'L', 'Azul', 'NL-015-L-AZU', TRUE);  -- variante 42

-- Botines de cuero
INSERT INTO prendas (id, nombre, descripcion, marca, genero, precio, activo, id_categoria) VALUES (16, 'Botines de cuero', 'Botines de cuero con pasadores y suela de goma', 'StreetStep', 'HOMBRE', 259.90, TRUE, 5);
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (43, 16, '40', 'Marron', 'NL-016-40-MAR', TRUE);  -- variante 43
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (44, 16, '41', 'Marron', 'NL-016-41-MAR', TRUE);  -- variante 44
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (45, 16, '42', 'Marron', 'NL-016-42-MAR', TRUE);  -- variante 45

-- Polo negro estampado
INSERT INTO prendas (id, nombre, descripcion, marca, genero, precio, activo, id_categoria) VALUES (17, 'Polo negro estampado', 'Polo de algodon peinado con estampado pequeno en el pecho', 'NextLook Street', 'UNISEX', 69.90, TRUE, 1);
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (46, 17, 'S', 'Negro', 'NL-017-S-NEG', TRUE);  -- variante 46
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (47, 17, 'M', 'Negro', 'NL-017-M-NEG', TRUE);  -- variante 47
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (48, 17, 'L', 'Negro', 'NL-017-L-NEG', TRUE);  -- variante 48

-- Poleron crewneck
INSERT INTO prendas (id, nombre, descripcion, marca, genero, precio, activo, id_categoria) VALUES (18, 'Poleron crewneck', 'Poleron cuello redondo de algodon perchado', 'NextLook Basics', 'UNISEX', 119.90, TRUE, 6);
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (49, 18, 'S', 'Blanco', 'NL-018-S-BLA', TRUE);  -- variante 49
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (50, 18, 'M', 'Blanco', 'NL-018-M-BLA', TRUE);  -- variante 50
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (51, 18, 'L', 'Blanco', 'NL-018-L-BLA', TRUE);  -- variante 51

-- Jogger satinado
INSERT INTO prendas (id, nombre, descripcion, marca, genero, precio, activo, id_categoria) VALUES (19, 'Jogger satinado', 'Pantalon jogger de tela satinada con pretina elastica', 'Flora', 'MUJER', 129.90, TRUE, 2);
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (52, 19, 'S', 'Rosa palo', 'NL-019-S-ROS', TRUE);  -- variante 52
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (53, 19, 'M', 'Rosa palo', 'NL-019-M-ROS', TRUE);  -- variante 53

-- Mochila urbana
INSERT INTO prendas (id, nombre, descripcion, marca, genero, precio, activo, id_categoria) VALUES (20, 'Mochila urbana', 'Mochila de lona resistente al agua con compartimento para laptop', 'StreetStep', 'UNISEX', 149.90, TRUE, 8);
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (54, 20, 'UNICA', 'Negro', 'NL-020-UNI-NEG', TRUE);  -- variante 54

-- Lentes redondos
INSERT INTO prendas (id, nombre, descripcion, marca, genero, precio, activo, id_categoria) VALUES (21, 'Lentes redondos', 'Lentes de sol con montura metalica dorada y proteccion UV400', 'Noir', 'UNISEX', 89.90, TRUE, 8);
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (55, 21, 'UNICA', 'Dorado', 'NL-021-UNI-DOR', TRUE);  -- variante 55

-- Conjunto buzo oversize
INSERT INTO prendas (id, nombre, descripcion, marca, genero, precio, activo, id_categoria) VALUES (22, 'Conjunto buzo oversize', 'Hoodie corto y jogger a juego de algodon afelpado', 'NextLook Street', 'MUJER', 189.90, TRUE, 6);
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (56, 22, 'S', 'Mostaza', 'NL-022-S-MOS', TRUE);  -- variante 56
INSERT INTO variantes (id, id_prenda, talla, color, sku, activo) VALUES (57, 22, 'M', 'Mostaza', 'NL-022-M-MOS', TRUE);  -- variante 57

-- Los id se pusieron a mano (deben coincidir con inventario-ms): las secuencias siguen desde el maximo
SELECT setval(pg_get_serial_sequence('categorias', 'id'), (SELECT MAX(id) FROM categorias));
SELECT setval(pg_get_serial_sequence('prendas', 'id'), (SELECT MAX(id) FROM prendas));
SELECT setval(pg_get_serial_sequence('variantes', 'id'), (SELECT MAX(id) FROM variantes));
