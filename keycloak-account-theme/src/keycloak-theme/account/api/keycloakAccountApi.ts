/**
 * Keycloak Account API utilities
 * Provides methods to interact with Keycloak's account management API
 */

import { type UserProfile } from './types';

/**
 * Parse Keycloak issuer URI to extract realm and HTTP relative path
 */
function parseKeycloakIssuerUri(issuerUri: string): { kcHttpRelativePath?: string; realm: string } | null {
  try {
    const url = new URL(issuerUri);
    const pathParts = url.pathname.split('/');
    const realmsIndex = pathParts.indexOf('realms');

    if (realmsIndex === -1 || realmsIndex === pathParts.length - 1) {
      return null;
    }

    const realm = pathParts[realmsIndex + 1];
    const kcHttpRelativePath = pathParts.slice(0, realmsIndex).join('/') || undefined;

    return { kcHttpRelativePath, realm };
  } catch (error) {
    console.error('Failed to parse Keycloak issuer URI:', error);
    return null;
  }
}


/**
 * Transform Keycloak account API response to UserProfile format
 */
function transformUserProfile(userProfile: any): UserProfile {
  return {
    id: userProfile.id,
    username: userProfile.username,
    email: userProfile.email,
    firstName: userProfile.firstName,
    lastName: userProfile.lastName,
    emailVerified: userProfile.emailVerified || false,
    enabled: userProfile.enabled !== false,
    createdTimestamp: userProfile.createdTimestamp || Date.now(),
    // Add any other fields that might be needed
  } as UserProfile;
}

/**
 * Update user profile via Keycloak account API
 */
export async function updateUserProfile(
  baseUrl: string, 
  realm: string, 
  profileData: Partial<UserProfile>
): Promise<boolean> {
  try {
    // Clean up the base URL to avoid double slashes
    const cleanBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
    const accountUrl = `${cleanBaseUrl}/realms/${realm}/account`;
    
    const response = await fetch(accountUrl, {
      method: 'PUT',
      credentials: 'include',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(profileData),
    });

    if (!response.ok) {
      console.error('Failed to update user profile:', response.status, response.statusText);
      return false;
    }

    return true;
  } catch (error) {
    console.error('Error updating user profile:', error);
    return false;
  }
}

/**
 * Change password via Keycloak account API
 */
export async function changeUserPassword(
  baseUrl: string,
  realm: string,
  passwordData: {
    currentPassword: string;
    newPassword: string;
  }
): Promise<boolean> {
  try {
    // Clean up the base URL to avoid double slashes
    const cleanBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
    const passwordUrl = `${cleanBaseUrl}/realms/${realm}/account/credentials/password`;
    
    const response = await fetch(passwordUrl, {
      method: 'PUT',
      credentials: 'include',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(passwordData),
    });

    if (!response.ok) {
      console.error('Failed to change password:', response.status, response.statusText);
      return false;
    }

    return true;
  } catch (error) {
    console.error('Error changing password:', error);
    return false;
  }
}

