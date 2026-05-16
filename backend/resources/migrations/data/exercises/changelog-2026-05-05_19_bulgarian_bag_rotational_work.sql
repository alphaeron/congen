--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Bulgarian Bag Rotational Work
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Bulgarian Bag Rotational Work',
  'Dynamic rotational swinging patterns using a Bulgarian bag.',
  'plyometric',
  false,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Bulgarian Bag Rotational Work', 'anterior deltoid'),
  ('Bulgarian Bag Rotational Work', 'lateral deltoid'),
  ('Bulgarian Bag Rotational Work', 'biceps'),
  ('Bulgarian Bag Rotational Work', 'obliques'),
  ('Bulgarian Bag Rotational Work', 'lats');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Bulgarian Bag Rotational Work', 'bands');
