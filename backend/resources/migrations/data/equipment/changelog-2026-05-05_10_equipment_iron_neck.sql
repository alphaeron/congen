--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add equipment iron neck
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('iron neck', 'A specialized device used to improve strength and mobility in the head, neck, and spine.');
