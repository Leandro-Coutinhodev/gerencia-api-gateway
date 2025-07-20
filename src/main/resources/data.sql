INSERT INTO tb_role (role_id, name) VALUES (1, 'ADMIN') ON CONFLICT (role_id) DO NOTHING;
INSERT INTO tb_role (role_id, name) VALUES (2, 'SECRETARY') ON CONFLICT (role_id) DO NOTHING;
INSERT INTO tb_role (role_id, name) VALUES (3, 'PROFESSIONAL') ON CONFLICT (role_id) DO NOTHING;
INSERT INTO tb_role (role_id, name) VALUES (4, 'ASSISTANT') ON CONFLICT (role_id) DO NOTHING;
