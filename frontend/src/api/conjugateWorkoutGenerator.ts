import { REQUEST } from './endpoint';
import type { Program, UserExercisePoolResponse } from './types';

/**
 * Generates the next week of workouts for an existing conjugate powerlifting program.
 *
 * @param programId The ID of the existing program to generate workouts for
 * @returns Promise containing the updated program with new workouts
 */
export const generateNextWeek = (programId: number): Promise<Program> => {
  return REQUEST({
    method: 'POST',
    url: `/conjugate_workout_generator/${programId}`,
    timeout: 30000, // 30 seconds timeout for workout generation
  });
};

/**
 * Retrieves the user's exercise pool with available exercises and metadata.
 *
 * @returns Promise containing the user's exercise pool data
 */
export const getUserExercisePool = (): Promise<UserExercisePoolResponse> => {
  return REQUEST({
    method: 'GET',
    url: '/conjugate_workout_generator/exercise_pool',
  });
};

/**
 * Updates a generated workout with user's 1RM data to tailor weights appropriately.
 *
 * @param programId The ID of the program to update
 * @returns Promise containing the updated program
 */
export const updateWorkoutWithOneRepMax = (programId: number): Promise<Program> => {
  return REQUEST({
    method: 'PATCH',
    url: `/conjugate_workout_generator/${programId}/update_with_1rm`,
  });
};
