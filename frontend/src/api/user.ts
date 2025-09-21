import { REQUEST } from './endpoint';
import type { User } from './types';

/**
 * Create user profile after Keycloak registration.
 *
 * Creates a user profile in the application database after successful Keycloak registration.
 * The user must be authenticated and the profile will be linked to their Keycloak user ID.
 * User information (name) is automatically extracted from the JWT token.
 *
 * @return The created user profile
 */
export const createUserProfile = (): Promise<User> => {
  return REQUEST({
    method: 'POST',
    url: '/user/',
  });
};

/**
 * Get current user profile.
 *
 * Retrieves the current authenticated user's profile.
 *
 * @return The current user profile
 */
export const getCurrentUser = (): Promise<User> =>
  REQUEST({
    method: 'GET',
    url: '/user/me',
  });

/**
 * Update current user profile.
 *
 * Updates the current authenticated user's profile information.
 * The user must be authenticated and can only update their own profile.
 *
 * @param profileData The profile data to update
 * @return The updated user profile
 */
export const updateUserProfile = (profileData: { 
  name: string; 
  age?: number; 
  weight?: number; 
  height?: number; 
  gender?: string; 
}): Promise<User> =>
  REQUEST({
    method: 'PATCH',
    url: '/user/me',
    params: profileData,
  });
