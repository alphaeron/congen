/**
 * React hooks for Keycloak account API operations
 * Provides easy-to-use hooks for common user operations
 */

import { useState, useEffect, useCallback } from 'react';
import { createApiClient, KeycloakAccountApiClient } from './client';
import { type UserProfile, type UpdateUserProfileRequest, type ChangePasswordRequest } from './types';

export interface UseUserProfileResult {
  user: UserProfile | null;
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
  updateProfile: (data: UpdateUserProfileRequest) => Promise<boolean>;
}

export interface UsePasswordChangeResult {
  changePassword: (data: ChangePasswordRequest) => Promise<boolean>;
  loading: boolean;
  error: string | null;
}

/**
 * Hook for managing user profile data
 * In Keycloak account themes, user data comes from the context, not API calls
 */
export function useUserProfile(kcContext: any): Omit<UseUserProfileResult, 'user' | 'refetch'> {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [apiClient, setApiClient] = useState<KeycloakAccountApiClient | null>(null);

  // Initialize API client when kcContext changes
  useEffect(() => {
    const client = createApiClient(kcContext);
    setApiClient(client);
  }, [kcContext]);

  const updateProfile = useCallback(async (data: UpdateUserProfileRequest): Promise<boolean> => {
    if (!apiClient) {
      setError('API client not available');
      return false;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await apiClient.updateUserProfile(data);
      
      if (response.success) {
        // In a real implementation, you might want to refresh the page or update the context
        // For now, we'll just return success
        return true;
      } else {
        setError(response.error || 'Failed to update profile');
        return false;
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
      return false;
    } finally {
      setLoading(false);
    }
  }, [apiClient]);

  return {
    loading,
    error,
    updateProfile,
  };
}

/**
 * Hook for password change operations
 */
export function usePasswordChange(kcContext: any): UsePasswordChangeResult {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [apiClient, setApiClient] = useState<KeycloakAccountApiClient | null>(null);

  // Initialize API client when kcContext changes
  useEffect(() => {
    const client = createApiClient(kcContext);
    setApiClient(client);
  }, [kcContext]);

  const changePassword = useCallback(async (data: ChangePasswordRequest): Promise<boolean> => {
    if (!apiClient) {
      setError('API client not available');
      return false;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await apiClient.changePassword(data);
      
      if (response.success) {
        return true;
      } else {
        setError(response.error || 'Failed to change password');
        return false;
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
      return false;
    } finally {
      setLoading(false);
    }
  }, [apiClient]);

  return {
    changePassword,
    loading,
    error,
  };
}

/**
 * Hook for account deletion
 */
export function useAccountDeletion(kcContext: any) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [apiClient, setApiClient] = useState<KeycloakAccountApiClient | null>(null);

  useEffect(() => {
    const client = createApiClient(kcContext);
    setApiClient(client);
  }, [kcContext]);

  const deleteAccount = useCallback(async (): Promise<boolean> => {
    if (!apiClient) {
      setError('API client not available');
      return false;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await apiClient.deleteAccount();
      
      if (response.success) {
        return true;
      } else {
        setError(response.error || 'Failed to delete account');
        return false;
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
      return false;
    } finally {
      setLoading(false);
    }
  }, [apiClient]);

  return {
    deleteAccount,
    loading,
    error,
  };
}
