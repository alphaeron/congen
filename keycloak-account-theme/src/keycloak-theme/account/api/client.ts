/**
 * API client for Keycloak account management
 * Handles authentication and API calls to Keycloak endpoints
 */

import {
  type ApiResponse,
  type UserProfile,
  type UpdateUserProfileRequest,
  type ChangePasswordRequest,
} from './types';
import { KEYCLOAK_URL } from '../../../globals';

// Token getter registration
let getToken: (() => string | null) | null = null;
export const setTokenGetter = (getter: () => string | null) => {
  getToken = getter;
};

export class KeycloakAccountApiClient {
  private baseUrl: string;
  private realm: string;
  private accessToken: string;

  constructor(baseUrl: string, realm: string, accessToken: string) {
    this.baseUrl = baseUrl;
    this.realm = realm;
    this.accessToken = accessToken;
  }

  getAccessToken(): string {
    return this.accessToken;
  }

  private async makeRequest<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<ApiResponse<T>> {
    try {
      const url = `${this.baseUrl.replace(/\/$/, '')}${endpoint.replace('{realm}', this.realm)}`;

      // Prepare headers - match the exact format from the Keycloakify example
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        ...(options.headers as Record<string, string>),
      };

      // Add authorization header if we have a token
      if (this.accessToken) {
        headers['Authorization'] = `Bearer ${this.accessToken}`;
      }

      const response = await fetch(url, {
        ...options,
        headers,
        credentials: 'omit', // Always omit credentials when using Bearer token
      });

      if (!response.ok) {
        const errorText = await response.text();
        let errorData;
        try {
          errorData = JSON.parse(errorText);
        } catch {
          errorData = { errorMessage: errorText };
        }
        throw new Error(
          errorData.errorMessage || `HTTP ${response.status}: ${response.statusText}`
        );
      }

      const data = response.status === 204 ? null : await response.json();

      return {
        success: true,
        data,
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error occurred',
      };
    }
  }

  /**
   * Update user profile using Keycloak's Account Management REST API
   * Uses PUT /realms/{realm}/account with JSON data
   */
  async updateUserProfile(profile: UpdateUserProfileRequest): Promise<ApiResponse<void>> {
    try {
      // Prepare user data for the PUT request
      const userData: Record<string, unknown> = {};

      // Map profile fields to the correct Keycloak user representation
      if (profile.firstName !== undefined) {
        userData.firstName = profile.firstName;
      }
      if (profile.lastName !== undefined) {
        userData.lastName = profile.lastName;
      }
      if (profile.email !== undefined) {
        userData.email = profile.email;
      }

      // Use POST method with JSON data to update user via Keycloak Account Management API
      const response = await fetch(
        `${this.baseUrl.replace(/\/$/, '')}/realms/${this.realm}/account/`,
        {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${this.accessToken}`,
            'Content-Type': 'application/json',
            Accept: 'application/json',
          },
          body: JSON.stringify(userData),
          credentials: 'include', // Include cookies for session-based auth
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      return {
        success: true,
        data: undefined,
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error occurred',
      };
    }
  }

  /**
   * Change user password using Keycloak's account management REST API
   * Uses the same JSON approach as updateUserProfile
   */
  async changePassword(passwordData: ChangePasswordRequest): Promise<ApiResponse<void>> {
    try {
      // Use POST method with JSON data to change password via Keycloak Account Management API
      const response = await fetch(
        `${this.baseUrl.replace(/\/$/, '')}/realms/${this.realm}/account/credentials/password/`,
        {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${this.accessToken}`,
            'Content-Type': 'application/json',
            Accept: 'application/json',
          },
          body: JSON.stringify({
            currentPassword: passwordData.currentPassword,
            newPassword: passwordData.newPassword,
            confirmPassword: passwordData.confirmPassword,
          }),
          credentials: 'include', // Include cookies for session-based auth
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      return {
        success: true,
        data: undefined,
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error occurred',
      };
    }
  }

  /**
   * Get current user profile
   */
  async getUserProfile(): Promise<ApiResponse<UserProfile>> {
    return this.makeRequest<UserProfile>('/realms/{realm}/account/?userProfileMetadata=true', {
      method: 'GET',
    });
  }
}

/**
 * Factory function to create API client from Keycloak context
 */
export function createApiClient(
  kcContext: Record<string, unknown>
): KeycloakAccountApiClient | null {
  try {
    // Use environment-based configuration like the frontend
    const baseUrl = kcContext?.authUrl || kcContext?.serverBaseUrl || KEYCLOAK_URL;

    // Extract realm name from kcContext.realm.name
    const realm = kcContext?.realm?.name || 'congen';

    // Get access token from the token getter (set up by AuthContext)
    const accessToken = getToken ? getToken() : null;

    if (!accessToken) {
      // Return a client that will use session-based authentication
      return new KeycloakAccountApiClient(baseUrl, realm, '');
    }

    return new KeycloakAccountApiClient(baseUrl, realm, accessToken);
  } catch {
    return null;
  }
}
