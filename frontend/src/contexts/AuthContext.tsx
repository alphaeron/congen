import React, { createContext, useContext, useEffect, useState } from 'react';
import { useAuth as useOidcAuth } from 'react-oidc-context';

import { setTokenGetter } from '../api/endpoint';
import type { User } from '../api/types';
import { getCurrentUser, createUserProfile } from '../api/user';
import { decodeToken } from '../common/authUtils';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: () => Promise<void>;
  logout: () => Promise<void>;
  createProfile: (userData: ProfileData) => Promise<void>;
  clearError: () => void;
}

interface ProfileData {
  name: string;
  age: number;
  height: number;
  weight: number;
  unit: string;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const oidcAuth = useOidcAuth();
  const [user, setUser] = useState<User | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Register token getter for API requests
  useEffect(() => {
    setTokenGetter(() => oidcAuth.user?.access_token || null);
  }, [oidcAuth.user?.access_token]);

  // Sync OIDC user with our custom user profile
  useEffect(() => {
    const syncUserProfile = async () => {
      // If OIDC says we're not authenticated, clear user state immediately
      if (!oidcAuth.user || !oidcAuth.isAuthenticated) {
        setUser(null);
        return;
      }

      // Ensure we have a valid access token before making API calls
      if (!oidcAuth.user.access_token) {
        return;
      }

      // Small delay to ensure token getter is properly set up
      await new Promise(resolve => setTimeout(resolve, 200));

      try {
        // Get user profile from our backend
        const userProfile = await getCurrentUser();

        // Extract roles from JWT token
        const tokenPayload = decodeToken(oidcAuth.user.access_token) as {
          realm_access?: { roles?: string[] };
        };
        const roles = tokenPayload?.realm_access?.roles || [];

        setUser({
          ...userProfile,
          roles,
        });
        setError(null);
      } catch (profileError) {
        // Check if the error is due to missing profile (404) vs other errors
        // The error structure depends on how it's transformed by the API layer
        let isProfileNotFound = false;

        // Check if it's a transformed error object with error message
        if (
          profileError &&
          typeof profileError === 'object' &&
          'error' in profileError &&
          typeof profileError.error === 'string'
        ) {
          // If the error message indicates "Resource not found", treat as 404
          isProfileNotFound =
            profileError.error.includes('Resource not found') ||
            profileError.error.includes('not found');
        }

        if (isProfileNotFound) {
          // Profile doesn't exist - set error for component to handle
          setUser(null);
          setError('Profile not found. Please create your profile.');
        } else {
          // Other error - set generic error
          setUser(null);
          setError('Error loading user profile. Please try again.');
        }
      }
    };

    // Only sync if OIDC is not loading and we have a user
    if (!oidcAuth.isLoading && oidcAuth.user && oidcAuth.isAuthenticated) {
      syncUserProfile();
    }
  }, [oidcAuth.user, oidcAuth.isAuthenticated, oidcAuth.isLoading]);

  // Handle OIDC errors
  useEffect(() => {
    if (oidcAuth.error) {
      setError(oidcAuth.error.message);
      setUser(null);
    }
  }, [oidcAuth.error]);

  const login = async () => {
    try {
      setError(null);
      await oidcAuth.signinRedirect();
    } catch (err: unknown) {
      const errorMessage =
        err && typeof err === 'object' && 'message' in err ? String(err.message) : 'Login failed';
      setError(errorMessage);
    }
  };

  const logout = async () => {
    try {
      await oidcAuth.removeUser();
      setUser(null);
      setError(null);
    } catch {
      // Logout error handled silently
    }
  };

  const createProfile = async (userData: ProfileData) => {
    try {
      setError(null);
      await createUserProfile(
        userData.name,
        userData.age,
        userData.height,
        userData.weight,
        userData.unit
      );

      // After successful profile creation, sync the user profile
      const userProfile = await getCurrentUser();
      setUser({
        ...userProfile,
        roles: user?.roles || [],
      });
    } catch (err: unknown) {
      const errorMessage =
        err && typeof err === 'object' && 'error' in err && typeof err.error === 'string'
          ? String(err.error)
          : 'Profile creation failed';
      setError(errorMessage);
    }
  };

  const clearError = () => {
    setError(null);
  };

  const value: AuthContextType = {
    user,
    isAuthenticated: oidcAuth.isAuthenticated,
    isLoading: oidcAuth.isLoading,
    error,
    login,
    logout,
    createProfile,
    clearError,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
