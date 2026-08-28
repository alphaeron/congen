--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add muscle hamstrings
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('hamstrings', 'Your hamstring muscles serve a variety of functions, including: Bending the knee joint. Extending the hip joint. Rotating the hip joint.');
