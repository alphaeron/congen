import { REQUEST } from './endpoint';
import type { UserOneRepMax } from './types';

/**
 * Get all one rep max records for a user.
 *
 * @param userId The Keycloak user ID
 * @param unit Optional unit to convert weights to (kg or lbs)
 * @return List of one rep max records for the user
 */
export const getUserOneRepMaxes = (userId: string, unit?: string): Promise<UserOneRepMax[]> => {
  return REQUEST({
    method: 'GET',
    url: `/user_one_rep_max/user/${userId}`,
    params: unit ? { unit } : undefined,
  });
};

/**
 * Get a specific one rep max record by user and exercise name.
 *
 * @param userId The Keycloak user ID
 * @param exerciseName The exercise name
 * @param unit Optional unit to convert weight to (kg or lbs)
 * @return The one rep max record for the exercise
 */
export const getUserOneRepMax = (
  userId: string,
  exerciseName: string,
  unit?: string
): Promise<UserOneRepMax> => {
  return REQUEST({
    method: 'GET',
    url: `/user_one_rep_max/user/${userId}/exercise/${encodeURIComponent(exerciseName)}`,
    params: unit ? { unit } : undefined,
  });
};

/**
 * Create or update a one rep max record.
 *
 * @param userId The Keycloak user ID
 * @param exerciseName The exercise name
 * @param oneRepMax The one rep max weight value
 * @param unit The weight unit (kg or lbs)
 * @return The created or updated one rep max record
 */
export const upsertUserOneRepMax = (
  userId: string,
  exerciseName: string,
  oneRepMax: number,
  unit: string
): Promise<UserOneRepMax> => {
  return REQUEST({
    method: 'PUT',
    url: '/user_one_rep_max/',
    params: {
      user_id: userId,
      exercise_name: exerciseName,
      one_rep_max: oneRepMax,
      unit,
    },
  });
};

/**
 * Delete a one rep max record.
 *
 * @param userId The Keycloak user ID
 * @param exerciseName The exercise name
 * @return The deleted one rep max record
 */
export const deleteUserOneRepMax = (
  userId: string,
  exerciseName: string
): Promise<UserOneRepMax> => {
  return REQUEST({
    method: 'DELETE',
    url: `/user_one_rep_max/user/${userId}/exercise/${encodeURIComponent(exerciseName)}`,
  });
};
