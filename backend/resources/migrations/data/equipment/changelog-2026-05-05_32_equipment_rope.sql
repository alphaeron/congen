--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add equipment rope
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('rope', 'Heavy rope or chain used for dragging exercises in strongman training.')
ON CONFLICT (name) DO NOTHING;
