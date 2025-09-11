/**
 * React hooks for Keycloak account API operations
 * Provides easy-to-use hooks for common user operations
 */

import { useState, useEffect } from 'react';
import { createApiClient, KeycloakAccountApiClient } from './client';
import {
  type UpdateUserProfileRequest,
  type ChangePasswordRequest,
  type UseUserProfileResult,
  type UsePasswordChangeResult,
} from './types';

/**
 * Hook that provides an initialized API client
 */
export function useApiClient(kcContext: Record<string, unknown>): {
  apiClient: KeycloakAccountApiClient | null;
  loading: boolean;
  error: string | null;
} {
  const [apiClient, setApiClient] = useState<KeycloakAccountApiClient | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    try {
      const client = createApiClient(kcContext);
      setApiClient(client);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to initialize API client');
    } finally {
      setLoading(false);
    }
  }, [kcContext]);

  return { apiClient, loading, error };
}

/**
 * Helper function to create API operation handlers with consistent error handling
 */
function createApiOperationHandler<T>(
  apiClient: KeycloakAccountApiClient | null,
  operation: (
    client: KeycloakAccountApiClient,
    data: T
  ) => Promise<{ success: boolean; error?: string }>,
  setLoading: (loading: boolean) => void,
  setError: (error: string | null) => void
) {
  return async (data: T): Promise<boolean> => {
    if (!apiClient) {
      setError('API client not available');
      return false;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await operation(apiClient, data);

      if (response.success) {
        return true;
      } else {
        setError(response.error || 'Operation failed');
        return false;
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error';
      setError(errorMessage);
      return false;
    } finally {
      setLoading(false);
    }
  };
}

/**
 * Hook for managing user profile data
 * In Keycloak account themes, user data comes from the context, not API calls
 */
export function useUserProfile(
  kcContext: Record<string, unknown>
): Omit<UseUserProfileResult, 'user' | 'refetch'> {
  const { apiClient } = useApiClient(kcContext);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const updateProfile = createApiOperationHandler<UpdateUserProfileRequest>(
    apiClient,
    (client, data) => client.updateUserProfile(data),
    setLoading,
    setError
  );

  return {
    loading,
    error,
    updateProfile,
  };
}

/**
 * Hook for password change operations
 */
export function usePasswordChange(kcContext: Record<string, unknown>): UsePasswordChangeResult {
  const { apiClient } = useApiClient(kcContext);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const changePassword = createApiOperationHandler<ChangePasswordRequest>(
    apiClient,
    (client, data) => client.changePassword(data),
    setLoading,
    setError
  );

  return {
    changePassword,
    loading,
    error,
  };
}
