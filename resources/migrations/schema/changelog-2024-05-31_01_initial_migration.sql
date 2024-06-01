--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Initial changeset for browsing exercise data.
-- Fake rollback - the initial changelog does not make sense to roll back
--rollback SELECT 1
CREATE TABLE exercise (
  name VARCHAR NOT NULL,
  description VARCHAR NOT NULL,
  movement_type VARCHAR NOT NULL,
  is_unilateral BOOLEAN NOT NULL,
  is_upper BOOLEAN NOT NULL,
  is_accessory BOOLEAN NOT NULL,
  PRIMARY KEY (name)
);

CREATE TABLE muscle (
  name VARCHAR NOT NULL,
  description VARCHAR NOT NULL,
  PRIMARY KEY (name)
);

CREATE TABLE equipment (
  name VARCHAR NOT NULL,
  description VARCHAR NOT NULL,
  PRIMARY KEY (name)
);

CREATE TABLE exercise_muscle (
  exercise_name VARCHAR,
  muscle_name VARCHAR,
  PRIMARY KEY (exercise_name, muscle_name),
  CONSTRAINT fk_exercise FOREIGN KEY(exercise_name) REFERENCES exercise(name),
  CONSTRAINT fk_muscle FOREIGN KEY(muscle_name) REFERENCES muscle(name)
);

CREATE TABLE exercise_equipment (
  exercise_name VARCHAR,
  equipment_name VARCHAR,
  PRIMARY KEY (exercise_name, equipment_name),
  CONSTRAINT fk_exercise FOREIGN KEY(exercise_name) REFERENCES exercise(name),
  CONSTRAINT fk_equipment FOREIGN KEY(equipment_name) REFERENCES equipment(name)
);
