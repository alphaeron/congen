import { REQUEST } from './endpoint';
import type { UserWeakMuscle } from './types';

/**
 * Get all weak muscles for a specific user.
 *
 * @param userId The Keycloak identifier of the user
 * @returns Promise containing the user's weak muscles list
 */
export const getUserWeakMuscles = async (userId: string): Promise<UserWeakMuscle[]> => {
  return REQUEST({
    method: 'GET',
    url: `/user_weak_muscle/${encodeURIComponent(userId)}`,
  });
};

/**
 * Add a weak muscle for a user.
 *
 * @param userId The Keycloak identifier of the user
 * @param muscleName The name of the muscle to add as weak
 * @returns Promise containing the created user weak muscle
 */
export const addUserWeakMuscle = async (
  userId: string,
  muscleName: string
): Promise<UserWeakMuscle> => {
  return REQUEST({
    method: 'POST',
    url: '/user_weak_muscle/',
    params: {
      user_id: userId,
      muscle_name: muscleName,
    },
  });
};

/**
 * Remove a weak muscle for a user.
 *
 * @param userId The Keycloak identifier of the user
 * @param muscleName The name of the muscle to remove from weak muscles
 * @returns Promise containing the deletion result
 */
export const removeUserWeakMuscle = async (userId: string, muscleName: string): Promise<void> => {
  return REQUEST({
    method: 'DELETE',
    url: '/user_weak_muscle/',
    params: {
      user_id: userId,
      muscle_name: muscleName,
    },
  });
};
