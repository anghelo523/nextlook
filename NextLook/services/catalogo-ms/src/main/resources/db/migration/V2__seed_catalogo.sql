-- Datos de prueba de la tienda. Los id de variantes (1..N) se usan tambien en inventario-ms.
INSERT INTO categorias (nombre) VALUES ('Polos');
INSERT INTO categorias (nombre) VALUES ('Pantalones');
INSERT INTO categorias (nombre) VALUES ('Casacas');
INSERT INTO categorias (nombre) VALUES ('Vestidos');
INSERT INTO categorias (nombre) VALUES ('Zapatillas');

INSERT INTO prendas (nombre, descripcion, marca, genero, precio, activo, id_categoria) VALUES ('Polo basico algodon pima', 'Polo cuello redondo 100% algodon pima', 'NextLook Basics', 'UNISEX', 49.90, TRUE, 1);
INSERT INTO variantes (id_prenda, talla, color, sku, activo) VALUES (1, 'S', 'Blanco', 'NL-001-S-BLA', TRUE);  -- variante 1
INSERT INTO variantes (id_prenda, talla, color, sku, activo) VALUES (1, 'M', 'Blanco', 'NL-001-M-BLA', TRUE);  -- variante 2
INSERT INTO variantes (id_prenda, talla, color, sku, activo) VALUES (1, 'L', 'Blanco', 'NL-001-L-BLA', TRUE);  -- variante 3
INSERT INTO variantes (id_prenda, talla, color, sku, activo) VALUES (1, 'M', 'Negro', 'NL-001-M-NEG', TRUE);  -- variante 4
INSERT INTO variantes (id_prenda, talla, color, sku, activo) VALUES (1, 'L', 'Negro', 'NL-001-L-NEG', TRUE);  -- variante 5

INSERT INTO prendas (nombre, descripcion, marca, genero, precio, activo, id_categoria) VALUES ('Jean slim fit', 'Jean azul oscuro con elastano', 'Denim Co', 'HOMBRE', 129.90, TRUE, 2);
INSERT INTO variantes (id_prenda, talla, color, sku, activo) VALUES (2, '30', 'Azul', 'NL-002-30-AZU', TRUE);  -- variante 6
INSERT INTO variantes (id_prenda, talla, color, sku, activo) VALUES (2, '32', 'Azul', 'NL-002-32-AZU', TRUE);  -- variante 7
INSERT INTO variantes (id_prenda, talla, color, sku, activo) VALUES (2, '34', 'Azul', 'NL-002-34-AZU', TRUE);  -- variante 8

INSERT INTO prendas (nombre, descripcion, marca, genero, precio, activo, id_categoria) VALUES ('Casaca cortavientos', 'Casaca impermeable con capucha', 'Urban Wind', 'MUJER', 189.90, TRUE, 3);
INSERT INTO variantes (id_prenda, talla, color, sku, activo) VALUES (3, 'S', 'Verde oliva', 'NL-003-S-VER', TRUE);  -- variante 9
INSERT INTO variantes (id_prenda, talla, color, sku, activo) VALUES (3, 'M', 'Verde oliva', 'NL-003-M-VER', TRUE);  -- variante 10
INSERT INTO variantes (id_prenda, talla, color, sku, activo) VALUES (3, 'M', 'Negro', 'NL-003-M-NEG', TRUE);  -- variante 11

INSERT INTO prendas (nombre, descripcion, marca, genero, precio, activo, id_categoria) VALUES ('Vestido midi floral', 'Vestido de viscosa con estampado floral', 'Flora', 'MUJER', 159.90, TRUE, 4);
INSERT INTO variantes (id_prenda, talla, color, sku, activo) VALUES (4, 'S', 'Rosado', 'NL-004-S-ROS', TRUE);  -- variante 12
INSERT INTO variantes (id_prenda, talla, color, sku, activo) VALUES (4, 'M', 'Rosado', 'NL-004-M-ROS', TRUE);  -- variante 13

INSERT INTO prendas (nombre, descripcion, marca, genero, precio, activo, id_categoria) VALUES ('Zapatillas urbanas', 'Zapatillas de lona con suela de caucho', 'StreetStep', 'UNISEX', 219.90, TRUE, 5);
INSERT INTO variantes (id_prenda, talla, color, sku, activo) VALUES (5, '38', 'Blanco', 'NL-005-38-BLA', TRUE);  -- variante 14
INSERT INTO variantes (id_prenda, talla, color, sku, activo) VALUES (5, '40', 'Blanco', 'NL-005-40-BLA', TRUE);  -- variante 15
INSERT INTO variantes (id_prenda, talla, color, sku, activo) VALUES (5, '42', 'Negro', 'NL-005-42-NEG', TRUE);  -- variante 16
