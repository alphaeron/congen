import { REQUEST } from './endpoint';
import { User } from './types';

/**
 * Create user profile after Keycloak registration.
 *
 * Creates a user profile in the application database after successful Keycloak registration.
 * The user must be authenticated and the profile will be linked to their Keycloak user ID.
 *
 * @param name The user's full name
 * @param age The user's age in years
 * @param height The user's height in centimeters
 * @param weight The user's weight in kilograms
 * @param unit The weight unit (optional, defaults to KG)
 * @return The created user profile
 */
export const createUserProfile = (
  name: string,
  age: number,
  height: number,
  weight: number,
  unit?: string
): Promise<User> => {
  const params = new URLSearchParams({
    name,
    age: age.toString(),
    height: height.toString(),
    weight: weight.toString(),
  });

  if (unit) {
    params.append('unit', unit);
  }

  return REQUEST({
    method: 'POST',
    url: `/user/?${params.toString()}`,
  });
};

/**
 * Get user profile by ID.
 *
 * @param userId The user ID
 * @return The user profile
 */
export const getUserById = (userId: number): Promise<User> =>
  REQUEST({
    method: 'GET',
    url: `/user/${userId}`,
  });

/**
 * Get current user profile.
 *
 * @return The current user's profile
 */
export const getCurrentUser = (): Promise<User> =>
  REQUEST({
    method: 'GET',
    url: '/user/me',
  });

/**
 * Delete a user account.
 *
 * Permanently removes a user from the system. This action cannot be undone.
 *
 * @param userId The user ID to delete
 * @return The deleted user profile
 */
export const deleteUser = (userId: number): Promise<User> =>
  REQUEST({
    method: 'DELETE',
    url: `/user/${userId}`,
  });
