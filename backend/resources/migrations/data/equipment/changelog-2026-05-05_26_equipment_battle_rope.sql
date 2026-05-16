--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add equipment battle rope
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('battle rope', 'Heavy ropes used for dynamic upper body and conditioning exercises.')
ON CONFLICT (name) DO NOTHING;
