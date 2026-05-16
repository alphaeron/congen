--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add equipment hurdle
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('hurdle', 'An upright frame to jump over.');
