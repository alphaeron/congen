import { REQUEST } from './endpoint';
import type { ExerciseMuscle } from './types';

/**
 * Get a list of all available exercises and the muscles they use.
 *
 * @param options Optional configuration including forceRefresh flag
 * @return A list of all available exercises and the muscles they use.
 */
export const getExerciseMuscle = (options: { forceRefresh?: boolean } = {}): Promise<ExerciseMuscle[]> =>
  REQUEST({
    url: '/exercise_muscle/',
    method: 'GET',
    forceRefresh: options.forceRefresh,
  });
