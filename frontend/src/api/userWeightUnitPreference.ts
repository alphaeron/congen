import { REQUEST, encodeExerciseName } from './endpoint';
import type { UserWeightUnitPreference, WeightUnit } from './types';

/**
 * Create or update a user weight unit preference (upsert).
 *
 * @param userId The Keycloak identifier of the user
 * @param exerciseName The name of the exercise
 * @param preferredUnit The user's preferred weight unit for this exercise
 * @returns Promise containing the created or updated user weight unit preference
 */
export const upsertUserWeightUnitPreference = async (
  userId: string,
  exerciseName: string,
  preferredUnit: WeightUnit
): Promise<UserWeightUnitPreference> => {
  return REQUEST({
    method: 'PUT',
    url: '/user_weight_unit_preference/',
    params: {
      user_id: userId,
      exercise_name: exerciseName,
      preferred_unit: preferredUnit,
    },
  });
};

/**
 * Get all weight unit preferences for a user.
 *
 * @param userId The Keycloak identifier of the user
 * @param options Optional configuration including forceRefresh flag
 * @returns Promise containing a list of user weight unit preferences
 */
export const getUserWeightUnitPreferences = async (
  userId: string,
  options: { forceRefresh?: boolean } = {}
): Promise<UserWeightUnitPreference[]> => {
  return REQUEST({
    method: 'GET',
    url: `/user_weight_unit_preference/${userId}`,
    forceRefresh: options.forceRefresh,
  });
};

/**
 * Get a specific weight unit preference for a user and exercise.
 *
 * @param userId The Keycloak identifier of the user
 * @param exerciseName The name of the exercise
 * @returns Promise containing the user weight unit preference
 */
export const getUserWeightUnitPreference = async (
  userId: string,
  exerciseName: string
): Promise<UserWeightUnitPreference> => {
  return REQUEST({
    method: 'GET',
    url: `/user_weight_unit_preference/${userId}/${encodeExerciseName(exerciseName)}`,
  });
};

/**
 * Delete a user weight unit preference.
 *
 * @param userId The Keycloak identifier of the user
 * @param exerciseName The name of the exercise
 * @returns Promise containing the deleted user weight unit preference
 */
export const deleteUserWeightUnitPreference = async (
  userId: string,
  exerciseName: string
): Promise<UserWeightUnitPreference> => {
  return REQUEST({
    method: 'DELETE',
    url: `/user_weight_unit_preference/${userId}/${encodeExerciseName(exerciseName)}`,
  });
};
