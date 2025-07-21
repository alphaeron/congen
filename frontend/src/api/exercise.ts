import { REQUEST } from "./endpoint";
import { Exercise, ExerciseEquipment, ExerciseMuscle } from "./types";

/**
 * Get a list of all available exercises.
 *
 * @return A list of all available exercises.
 */
export const getExercises = (): Promise<Exercise[]> =>
  REQUEST({
    url: "/exercise/",
    method: "GET",
  });

/**
 * Get an individual exercise.
 *
 * @param exerciseName The name of the exercise to get.
 *
 * @return The exercise obtained.
 */
export const getIndividualExercise = (
  exerciseName: string,
): Promise<Exercise> =>
  REQUEST({
    url: `/exercise/${exerciseName}`,
    method: "GET",
  });

/**
 * Get a list of muscles used for the exercise.
 *
 * @param exerciseName The name of the exercise to get the muscles for.
 *
 * @return A list muscles used for the exercise.
 */
export const getExerciseMuscles = (
  exerciseName: string,
): Promise<ExerciseMuscle[]> =>
  REQUEST({
    url: `/exercise/${exerciseName}/muscle`,
    method: "GET",
  });

/**
 * Get a list of equipment options that can be used to perform an exercise.
 *
 * @param exerciseName The name of the exercise to get the equipment options for.
 *
 * @return A list equipment options that can be used to perform the exercise.
 */
export const getExerciseEquipment = (
  exerciseName: string,
): Promise<ExerciseEquipment[]> =>
  REQUEST({
    url: `/exercise/${exerciseName}/equipment`,
    method: "GET",
  });
