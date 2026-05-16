--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add equipment valslides
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('valslides', 'Smooth plastic discs that slide on carpet or other surfaces for resistance training.')
ON CONFLICT (name) DO NOTHING;
