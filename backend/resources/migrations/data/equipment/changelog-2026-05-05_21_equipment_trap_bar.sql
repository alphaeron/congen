--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add equipment trap bar
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('trap bar', 'A hexagonally-shaped bar with handles inside the hexagon.');
