import { REQUEST } from './endpoint';
import type { Program } from './types';

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
  });
};
