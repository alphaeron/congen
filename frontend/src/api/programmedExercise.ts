import { REQUEST } from './endpoint';
import type { ProgrammedExercise } from './types';

/**
 * Get all programmed exercises for a specific workout stage.
 *
 * @param workoutStageId The ID of the workout stage
 * @returns Promise containing a list of programmed exercises
 */
export const getProgrammedExercisesByStage = (
  workoutStageId: number
): Promise<ProgrammedExercise[]> => {
  return REQUEST({
    method: 'GET',
    url: `/programmed_exercise/stage/${workoutStageId}`,
  });
};

/**
 * Get a specific programmed exercise by ID.
 *
 * @param id The programmed exercise ID
 * @returns Promise containing the programmed exercise details
 */
export const getProgrammedExercise = (id: number): Promise<ProgrammedExercise> => {
  return REQUEST({
    method: 'GET',
    url: `/programmed_exercise/${id}`,
  });
};
