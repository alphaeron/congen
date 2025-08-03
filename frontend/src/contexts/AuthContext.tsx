import React, { createContext, useContext, useEffect, useState } from 'react';

import { getCurrentUser, createUserProfile } from '../api/user';
import { User } from '../api/types';
import { useAuth as useOidcAuth } from 'react-oidc-context';
import { decodeToken } from '../common/authUtils';
import { setTokenGetter } from '../api/endpoint';

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

      try {
        // Get user profile from our backend
        const userProfile = await getCurrentUser();

        // Extract groups and roles from JWT token
        const tokenPayload = decodeToken(oidcAuth.user.access_token) as {
          groups?: string[];
          realm_access?: { roles?: string[] };
        };
        const groups = tokenPayload?.groups || [];
        const roles = tokenPayload?.realm_access?.roles || [];

        setUser({
          ...userProfile,
          groups,
          roles,
        });
        setError(null);
      } catch (profileError) {
        // Check if the error is due to authentication (401/403) vs missing profile
        const isAuthError = profileError && 
          typeof profileError === 'object' && 
          'response' in profileError && 
          profileError.response && 
          typeof profileError.response === 'object' && 
          'status' in profileError.response && 
          (profileError.response.status === 401 || profileError.response.status === 403);
        
        if (isAuthError) {
          // Authentication error - clear user state
          setUser(null);
          setError('Authentication failed. Please log in again.');
        } else {
          // Profile doesn't exist - set error for component to handle
          setUser(null);
          setError('Profile not found. Please create your profile.');
        }
      }
    };

    // Only sync if OIDC is not loading
    if (!oidcAuth.isLoading) {
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
        groups: user?.groups || [],
        roles: user?.roles || [],
      });
    } catch (err: unknown) {
      const errorMessage =
        err &&
        typeof err === 'object' &&
        'response' in err &&
        err.response &&
        typeof err.response === 'object' &&
        'data' in err.response &&
        err.response.data &&
        typeof err.response.data === 'object' &&
        'message' in err.response.data
          ? String(err.response.data.message)
          : 'Profile creation failed';
      setError(errorMessage);
    }
  };

  const clearError = () => {
    setError(null);
  };

  const value: AuthContextType = {
    user,
    isAuthenticated: oidcAuth.isAuthenticated && !!user,
    isLoading: oidcAuth.isLoading,
    error,
    login,
    logout,
    createProfile,
    clearError,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
