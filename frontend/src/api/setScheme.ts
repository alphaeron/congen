import { REQUEST } from './endpoint';
import type { SetScheme } from './types';

/**
 * Get all set schemes for a specific programmed exercise.
 *
 * @param programmedExerciseId The ID of the programmed exercise
 * @returns Promise containing a list of set schemes
 */
export const getSetSchemesByExercise = (programmedExerciseId: number): Promise<SetScheme[]> => {
  return REQUEST({
    method: 'GET',
    url: `/set_scheme/exercise/${programmedExerciseId}`,
  });
};

/**
 * Get a specific set scheme by ID.
 *
 * @param id The set scheme ID
 * @returns Promise containing the set scheme details
 */
export const getSetScheme = (id: number): Promise<SetScheme> => {
  return REQUEST({
    method: 'GET',
    url: `/set_scheme/${id}`,
  });
};
