--liquibase formatted sql

--changeset John Matty:3 labels:prod,test
--comment: Add user, user_equipment, user_program_preferences, and user_exercise_preference tables for user-specific workout generation.

CREATE TABLE "user" (
  id SERIAL PRIMARY KEY,
  name VARCHAR NOT NULL,
  age INTEGER NOT NULL,
  height NUMERIC NOT NULL,
  weight NUMERIC NOT NULL
);

CREATE TABLE user_equipment (
  user_id INTEGER NOT NULL,
  equipment_name VARCHAR NOT NULL,
  PRIMARY KEY (user_id, equipment_name),
  CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES "user"(id),
  CONSTRAINT fk_equipment FOREIGN KEY(equipment_name) REFERENCES equipment(name)
);

CREATE TABLE user_program_preferences (
  user_id INTEGER PRIMARY KEY,
  program_days_per_week INTEGER NOT NULL,
  session_time_length_in_minutes INTEGER NOT NULL,
  CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES "user"(id)
);

CREATE TABLE user_exercise_preference (
  user_id INTEGER NOT NULL,
  exercise_name VARCHAR NOT NULL,
  should_avoid BOOLEAN NOT NULL,
  PRIMARY KEY (user_id, exercise_name),
  CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES "user"(id),
  CONSTRAINT fk_exercise FOREIGN KEY(exercise_name) REFERENCES exercise(name)
); 