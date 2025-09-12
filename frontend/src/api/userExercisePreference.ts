import { REQUEST } from './endpoint';
import type { UserExercisePreference } from './types';

/**
 * Get all exercise preferences for a specific user.
 *
 * @param userId The Keycloak identifier of the user
 * @returns Promise containing the user's exercise preferences list
 */
export const getUserExercisePreferences = async (
  userId: string
): Promise<UserExercisePreference[]> => {
  return REQUEST({
    method: 'GET',
    url: `/user_exercise_preference/${encodeURIComponent(userId)}`,
  });
};

/**
 * Add or update an exercise preference for a user.
 *
 * @param userId The Keycloak identifier of the user
 * @param exerciseName The name of the exercise
 * @param shouldAvoid Whether the user should avoid this exercise
 * @returns Promise containing the created or updated user exercise preference
 */
export const upsertUserExercisePreference = async (
  userId: string,
  exerciseName: string,
  shouldAvoid: boolean
): Promise<UserExercisePreference> => {
  return REQUEST({
    method: 'POST',
    url: '/user_exercise_preference/',
    params: {
      user_id: userId,
      exercise_name: exerciseName,
      should_avoid: shouldAvoid,
    },
  });
};

/**
 * Update an existing exercise preference for a user.
 *
 * @param userId The Keycloak identifier of the user
 * @param exerciseName The name of the exercise
 * @param shouldAvoid Whether the user should avoid this exercise
 * @returns Promise containing the updated user exercise preference
 */
export const updateUserExercisePreference = async (
  userId: string,
  exerciseName: string,
  shouldAvoid: boolean
): Promise<UserExercisePreference> => {
  return REQUEST({
    method: 'PATCH',
    url: '/user_exercise_preference/',
    params: {
      user_id: userId,
      exercise_name: exerciseName,
      should_avoid: shouldAvoid,
    },
  });
};

/**
 * Remove an exercise preference for a user.
 *
 * @param userId The Keycloak identifier of the user
 * @param exerciseName The name of the exercise to remove preference for
 * @returns Promise containing the deletion result
 */
export const removeUserExercisePreference = async (
  userId: string,
  exerciseName: string
): Promise<void> => {
  return REQUEST({
    method: 'DELETE',
    url: '/user_exercise_preference/',
    params: {
      user_id: userId,
      exercise_name: exerciseName,
    },
  });
};
