--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add equipment adjustable bench
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('adjustable bench', 'A bench whose back pad (and sometimes seat) allows you to change the angle of elevation.');
