import { REQUEST } from './endpoint';
import { ExerciseMuscle } from './types';

/**
 * Get a list of all available exercises and the muscles they use.
 *
 * @return A list of all available exercises and the muscles they use.
 */
export const getExerciseMuscle = (): Promise<ExerciseMuscle[]> =>
  REQUEST({
    url: '/exercise_muscle/',
    method: 'GET',
  });
