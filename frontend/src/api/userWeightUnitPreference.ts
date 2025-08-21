import { ENDPOINT } from './endpoint';

/**
 * Weight unit enum.
 */
export enum WeightUnit {
  KG = 'KG',
  LBS = 'LBS',
}

/**
 * User weight unit preference interface.
 */
export interface UserWeightUnitPreference {
  user_id: string;
  exercise_name: string;
  preferred_unit: WeightUnit;
  created_at: string;
  updated_at: string;
}

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
): Promise<{ data: UserWeightUnitPreference }> => {
  const response = await ENDPOINT.put('/user_weight_unit_preference/', null, {
    params: {
      user_id: userId,
      exercise_name: exerciseName,
      preferred_unit: preferredUnit,
    },
  });
  return response;
};

/**
 * Get all weight unit preferences for a user.
 *
 * @param userId The Keycloak identifier of the user
 * @returns Promise containing a list of user weight unit preferences
 */
export const getUserWeightUnitPreferences = async (
  userId: string
): Promise<{ data: UserWeightUnitPreference[] }> => {
  const response = await ENDPOINT.get(`/user_weight_unit_preference/${userId}`);
  return response;
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
): Promise<{ data: UserWeightUnitPreference }> => {
  const response = await ENDPOINT.get(`/user_weight_unit_preference/${userId}/${encodeURIComponent(exerciseName)}`);
  return response;
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
): Promise<{ data: UserWeightUnitPreference }> => {
  const response = await ENDPOINT.delete(`/user_weight_unit_preference/${userId}/${encodeURIComponent(exerciseName)}`);
  return response;
};
