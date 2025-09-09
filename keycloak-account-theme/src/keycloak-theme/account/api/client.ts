/**
 * API client for Keycloak account management
 * Handles authentication and API calls to Keycloak endpoints
 */

import { API_ENDPOINTS, type ApiResponse, type UserProfile, type UpdateUserProfileRequest, type ChangePasswordRequest } from './types';

export class KeycloakAccountApiClient {
  private baseUrl: string;
  private realm: string;
  private accessToken: string;

  constructor(baseUrl: string, realm: string, accessToken: string) {
    this.baseUrl = baseUrl;
    this.realm = realm;
    this.accessToken = accessToken;
  }

  private async makeRequest<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<ApiResponse<T>> {
    try {
      const url = `${this.baseUrl}${endpoint.replace('{realm}', this.realm)}`;
      
      const response = await fetch(url, {
        ...options,
        headers: {
          'Authorization': `Bearer ${this.accessToken}`,
          'Content-Type': 'application/json',
          ...options.headers,
        },
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.errorMessage || `HTTP ${response.status}: ${response.statusText}`);
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
   * Get current user profile
   */
  async getUserProfile(): Promise<ApiResponse<UserProfile>> {
    return this.makeRequest<UserProfile>(API_ENDPOINTS.USER_PROFILE, {
      method: 'GET',
    });
  }

  /**
   * Update user profile
   */
  async updateUserProfile(profile: UpdateUserProfileRequest): Promise<ApiResponse<UserProfile>> {
    return this.makeRequest<UserProfile>(API_ENDPOINTS.UPDATE_PROFILE, {
      method: 'PUT',
      body: JSON.stringify(profile),
    });
  }

  /**
   * Change user password
   */
  async changePassword(passwordData: ChangePasswordRequest): Promise<ApiResponse<void>> {
    return this.makeRequest<void>(API_ENDPOINTS.CHANGE_PASSWORD, {
      method: 'PUT',
      body: JSON.stringify(passwordData),
    });
  }

  /**
   * Delete user account
   */
  async deleteAccount(): Promise<ApiResponse<void>> {
    return this.makeRequest<void>(API_ENDPOINTS.DELETE_ACCOUNT, {
      method: 'DELETE',
    });
  }

  /**
   * Get user sessions
   */
  async getUserSessions(): Promise<ApiResponse<any[]>> {
    return this.makeRequest<any[]>(API_ENDPOINTS.SESSIONS, {
      method: 'GET',
    });
  }

  /**
   * Get user applications
   */
  async getUserApplications(): Promise<ApiResponse<any[]>> {
    return this.makeRequest<any[]>(API_ENDPOINTS.APPLICATIONS, {
      method: 'GET',
    });
  }

  /**
   * Get user credentials
   */
  async getUserCredentials(): Promise<ApiResponse<any[]>> {
    return this.makeRequest<any[]>(API_ENDPOINTS.CREDENTIALS, {
      method: 'GET',
    });
  }
}

/**
 * Factory function to create API client from Keycloak context
 */
export function createApiClient(kcContext: any): KeycloakAccountApiClient | null {
  try {
    // Extract necessary information from Keycloak context
    const baseUrl = kcContext?.authUrl || kcContext?.serverBaseUrl;
    const realm = kcContext?.realm || 'congen';
    const accessToken = kcContext?.accessToken || kcContext?.token;

    if (!baseUrl || !accessToken) {
      console.warn('Missing required Keycloak context data for API client');
      return null;
    }

    return new KeycloakAccountApiClient(baseUrl, realm, accessToken);
  } catch (error) {
    console.error('Failed to create API client:', error);
    return null;
  }
}
