--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add muscle glutes
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('glutes', 'Gluteus maximus is the primary hip extensor muscle, and also the largest of the three gluteals. Their biggest job is in keeping us upright and pushing our bodies forward.');
