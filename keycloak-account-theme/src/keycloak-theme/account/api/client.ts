/**
 * API client for Keycloak account management
 * Handles authentication and API calls to Keycloak endpoints
 */

import { API_ENDPOINTS, type ApiResponse, type UserProfile, type UpdateUserProfileRequest, type ChangePasswordRequest } from './types';
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
      
      console.log('Making API request to:', url);
      console.log('Headers:', headers);
      console.log('Method:', options.method || 'GET');
      
      const response = await fetch(url, {
        ...options,
        headers,
        credentials: 'omit', // Always omit credentials when using Bearer token
      });

      console.log('Response status:', response.status);
      console.log('Response headers:', Object.fromEntries(response.headers.entries()));

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Error response:', errorText);
        let errorData;
        try {
          errorData = JSON.parse(errorText);
        } catch {
          errorData = { errorMessage: errorText };
        }
        throw new Error(errorData.errorMessage || `HTTP ${response.status}: ${response.statusText}`);
      }

      const data = response.status === 204 ? null : await response.json();
      console.log('Response data:', data);
      
      return {
        success: true,
        data,
      };
    } catch (error) {
      console.error('API request error:', error);
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
      const userData: any = {};
      
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

      console.log('Updating user profile with data:', userData);

      // Use POST method with JSON data to update user via Keycloak Account Management API
      const response = await fetch(`${this.baseUrl.replace(/\/$/, '')}/realms/${this.realm}/account/`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${this.accessToken}`,
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        body: JSON.stringify(userData),
        credentials: 'include', // Include cookies for session-based auth
      });

      console.log('Profile update response status:', response.status);
      console.log('Profile update response headers:', Object.fromEntries(response.headers.entries()));

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Profile update error response:', errorText);
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      return {
        success: true,
        data: undefined,
      };
    } catch (error) {
      console.error('Error updating user profile:', error);
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error occurred',
      };
    }
  }

  /**
   * Change user password using Keycloak's account management form-based API
   * Based on Keycloak account UI implementation
   */
  async changePassword(passwordData: ChangePasswordRequest): Promise<ApiResponse<void>> {
    try {
      // Create form data for POST request - this is how Keycloak account UI does it
      const formData = new FormData();
      
      // Add password fields
      formData.append('password', passwordData.currentPassword);
      formData.append('password-new', passwordData.newPassword);
      formData.append('password-confirm', passwordData.confirmPassword);
      
      // Add submit action
      formData.append('submitAction', 'Save');
      
      // Get CSRF token from the page - try multiple selectors
      let csrfToken = document.querySelector('input[name="kc-csrf-token"]') as HTMLInputElement;
      if (!csrfToken) {
        csrfToken = document.querySelector('input[name="_token"]') as HTMLInputElement;
      }
      if (!csrfToken) {
        csrfToken = document.querySelector('meta[name="csrf-token"]') as HTMLInputElement;
      }
      
      if (csrfToken) {
        formData.append('kc-csrf-token', csrfToken.value);
        console.log('CSRF token found and added:', csrfToken.value);
      } else {
        console.warn('No CSRF token found on the page');
      }

      console.log('Changing password with form data:', Object.fromEntries(formData.entries()));

      // Make POST request with form data
      const response = await fetch(`${this.baseUrl.replace(/\/$/, '')}/realms/${this.realm}/account/credentials/password`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${this.accessToken}`,
        },
        body: formData,
        credentials: 'include', // Include cookies for session-based auth
      });

      console.log('Password change response status:', response.status);
      console.log('Password change response headers:', Object.fromEntries(response.headers.entries()));

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Password change error response:', errorText);
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      return {
        success: true,
        data: undefined,
      };
    } catch (error) {
      console.error('Error changing password:', error);
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
    // Use environment-based configuration like the frontend
    const baseUrl = kcContext?.authUrl || kcContext?.serverBaseUrl || KEYCLOAK_URL;
    
    // Extract realm name from kcContext.realm.name
    const realm = kcContext?.realm?.name || 'congen';
    
    console.log('Creating API client with:', { baseUrl, realm, kcContextRealm: kcContext?.realm });
    
    // Get access token from the token getter (set up by AuthContext)
    const accessToken = getToken ? getToken() : null;

    if (!accessToken) {
      console.warn('No access token available from token getter, using session-based authentication');
      // Return a client that will use session-based authentication
      return new KeycloakAccountApiClient(baseUrl, realm, '');
    }

    return new KeycloakAccountApiClient(baseUrl, realm, accessToken);
  } catch (error) {
    console.error('Failed to create API client:', error);
    return null;
  }
}
