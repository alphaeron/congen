--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add equipment safety squat bar
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('safety squat bar', 'A steel-cambered bar with padding in the middle and handles on both sides. The padding makes it comfortable to rest the bar on your neck and shoulders during squats.');
