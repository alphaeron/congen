/**
 * Hook to fetch user data from Keycloak account API
 * Uses session-based authentication
 */

import { useState, useEffect, useCallback } from 'react';
import { updateUserProfile, changeUserPassword } from './keycloakAccountApi';
import { type UserProfile } from './types';

export interface UseKeycloakUserResult {
  user: UserProfile | null;
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
  updateUser: (data: Partial<UserProfile>) => Promise<boolean>;
  changePassword: (data: { currentPassword: string; newPassword: string }) => Promise<boolean>;
}


export function useKeycloakUser(kcContext: any): UseKeycloakUserResult {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Debug logging - check for authentication-related properties
  console.log('useKeycloakUser - Available keys:', Object.keys(kcContext || {}));
  
  // Check for any authentication-related properties
  const authKeys = Object.keys(kcContext || {}).filter(key => 
    key.toLowerCase().includes('token') || 
    key.toLowerCase().includes('auth') || 
    key.toLowerCase().includes('session') ||
    key.toLowerCase().includes('credential') ||
    key.toLowerCase().includes('bearer')
  );
  console.log('useKeycloakUser - Auth-related keys:', authKeys);
  
  // Check for user-related properties
  const userKeys = Object.keys(kcContext || {}).filter(key => 
    key.toLowerCase().includes('user') || 
    key.toLowerCase().includes('profile') || 
    key.toLowerCase().includes('account') ||
    key.toLowerCase().includes('person') ||
    key.toLowerCase().includes('name') ||
    key.toLowerCase().includes('email')
  );
  console.log('useKeycloakUser - User-related keys:', userKeys);
  
  // Log specific user-related values
  if (kcContext) {
    console.log('useKeycloakUser - kcContext.user:', kcContext.user);
    console.log('useKeycloakUser - kcContext.profile:', kcContext.profile);
    console.log('useKeycloakUser - kcContext.account:', kcContext.account);
    console.log('useKeycloakUser - kcContext.userProfile:', kcContext.userProfile);
    console.log('useKeycloakUser - kcContext.token:', kcContext.token);
    console.log('useKeycloakUser - kcContext.accessToken:', kcContext.accessToken);
    console.log('useKeycloakUser - kcContext.authToken:', kcContext.authToken);
  }
  
  // Log specific authentication properties
  if (kcContext) {
    console.log('useKeycloakUser - kcContext.realm:', kcContext.realm);
    console.log('useKeycloakUser - kcContext.clientId:', kcContext.clientId);
    console.log('useKeycloakUser - kcContext.serverBaseUrl:', kcContext.serverBaseUrl);
    
    // Check for Keycloak JavaScript adapter
    console.log('useKeycloakUser - window.Keycloak available:', typeof window !== 'undefined' && 'Keycloak' in window);
    console.log('useKeycloakUser - window.kc available:', typeof window !== 'undefined' && 'kc' in window);
    
    // Check for any global Keycloak instance
    if (typeof window !== 'undefined') {
      const globalKeys = Object.keys(window).filter(key => 
        key.toLowerCase().includes('keycloak') || 
        key.toLowerCase().includes('kc') ||
        key.toLowerCase().includes('oidc')
      );
      console.log('useKeycloakUser - Global Keycloak-related keys:', globalKeys);
    }
  }

  const fetchUser = useCallback(async () => {
    // Extract realm name from the baseUrl path
    // The baseUrl.path contains "/realms/congen/account/" so we can extract "congen" from it
    let realmName = 'congen'; // default fallback
    
    if (kcContext?.baseUrl?.path) {
      const pathMatch = kcContext.baseUrl.path.match(/\/realms\/([^\/]+)\//);
      if (pathMatch) {
        realmName = pathMatch[1];
      }
    }
    
    if (!kcContext?.authUrl || !realmName) {
      setError('Missing Keycloak context information');
      setLoading(false);
      return;
    }

    console.log('useKeycloakUser - authUrl:', kcContext.authUrl);
    console.log('useKeycloakUser - baseUrl.path:', kcContext.baseUrl?.path);
    console.log('useKeycloakUser - extracted realm name:', realmName);

    setLoading(true);
    setError(null);

    try {
      // In Keycloak account theme context, user data should be available in KcContext
      // Let's check what user-related data is available in the context
      console.log('useKeycloakUser - Checking KcContext for user data');
      console.log('useKeycloakUser - Available cookies:', document.cookie);
      
      // Check if user data is available in KcContext
      if (kcContext?.user) {
        console.log('useKeycloakUser - User data found in KcContext:', kcContext.user);
        setUser(kcContext.user);
      } else if (kcContext?.profile) {
        console.log('useKeycloakUser - Profile data found in KcContext:', kcContext.profile);
        setUser(kcContext.profile);
      } else if (kcContext?.account) {
        console.log('useKeycloakUser - Account data found in KcContext:', kcContext.account);
        setUser(kcContext.account);
      } else if (kcContext?.userProfile) {
        console.log('useKeycloakUser - UserProfile data found in KcContext:', kcContext.userProfile);
        setUser(kcContext.userProfile);
      } else {
        console.log('useKeycloakUser - No user data found in KcContext');
        // In a Keycloak account theme, if no user data is in context,
        // the user is likely not authenticated. Don't set an error here -
        // let the OIDC authentication handle the authentication state.
        console.log('useKeycloakUser - No user data in KcContext, relying on OIDC authentication');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
    } finally {
      setLoading(false);
    }
  }, [kcContext?.authUrl, kcContext?.baseUrl?.path]);

  const updateUser = useCallback(async (data: Partial<UserProfile>): Promise<boolean> => {
    // Extract realm name from the baseUrl path
    let realmName = 'congen'; // default fallback
    
    if (kcContext?.baseUrl?.path) {
      const pathMatch = kcContext.baseUrl.path.match(/\/realms\/([^\/]+)\//);
      if (pathMatch) {
        realmName = pathMatch[1];
      }
    }
    
    if (!kcContext?.authUrl || !realmName) {
      setError('Missing Keycloak context information');
      return false;
    }

    setLoading(true);
    setError(null);

    try {
      const success = await updateUserProfile(kcContext.authUrl, realmName, data);
      
      if (success) {
        // Refetch user data after successful update
        await fetchUser();
        return true;
      } else {
        setError('Failed to update user profile');
        return false;
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
      return false;
    } finally {
      setLoading(false);
    }
  }, [kcContext?.authUrl, kcContext?.baseUrl?.path, fetchUser]);

  const changePassword = useCallback(async (data: { currentPassword: string; newPassword: string }): Promise<boolean> => {
    // Extract realm name from the baseUrl path
    let realmName = 'congen'; // default fallback
    
    if (kcContext?.baseUrl?.path) {
      const pathMatch = kcContext.baseUrl.path.match(/\/realms\/([^\/]+)\//);
      if (pathMatch) {
        realmName = pathMatch[1];
      }
    }
    
    if (!kcContext?.authUrl || !realmName) {
      setError('Missing Keycloak context information');
      return false;
    }

    setLoading(true);
    setError(null);

    try {
      const success = await changeUserPassword(kcContext.authUrl, realmName, data);
      
      if (!success) {
        setError('Failed to change password');
      }
      
      return success;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
      return false;
    } finally {
      setLoading(false);
    }
  }, [kcContext?.authUrl, kcContext?.baseUrl?.path]);

  // Fetch user data on mount
  useEffect(() => {
    fetchUser();
  }, [fetchUser]);

  return {
    user,
    loading,
    error,
    refetch: fetchUser,
    updateUser,
    changePassword,
  };
}
