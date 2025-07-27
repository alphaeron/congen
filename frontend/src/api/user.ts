import { REQUEST } from './endpoint';
import { User } from './types';

/**
 * Register a new user.
 *
 * Creates both a Keycloak user account and a corresponding user profile
 * in the application database.
 *
 * @param name The user's full name
 * @param age The user's age in years
 * @param height The user's height in centimeters
 * @param weight The user's weight in kilograms
 * @param email The user's email address (required for Keycloak integration)
 * @param password The user's password (required for Keycloak integration)
 * @param unit The weight unit (optional, defaults to KG)
 * @return The created user with assigned ID and timestamps
 */
export const registerUser = (
  name: string,
  age: number,
  height: number,
  weight: number,
  email: string,
  password: string,
  unit?: string
): Promise<User> => {
  const params = new URLSearchParams({
    name,
    age: age.toString(),
    height: height.toString(),
    weight: weight.toString(),
    email,
    password,
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
