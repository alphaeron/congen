--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add equipment bodyblade
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('bodyblade', 'A BodyBlade is a lightweight, handheld rehabilitation and fitness tool that uses inertia and vibration to challenge muscle endurance and stability.')
ON CONFLICT (name) DO NOTHING;
