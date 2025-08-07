import { REQUEST } from './endpoint';
import type { Muscle } from './types';

/**
 * Get a list of all available muscles.
 *
 * @return A list of all available muscles.
 */
export const getMuscles = (): Promise<Muscle[]> =>
  REQUEST({
    url: '/muscle/',
    method: 'GET',
  });

/**
 * Get an individual muscle.
 *
 * @param muscleName The name of the muscle to get.
 *
 * @return The muscle obtained.
 */
export const getIndividualMuscle = (muscleName: string): Promise<Muscle> =>
  REQUEST({
    url: `/muscle/${muscleName}`,
    method: 'GET',
  });
