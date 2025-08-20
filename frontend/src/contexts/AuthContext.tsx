import React, { createContext, useContext, useEffect, useState } from 'react';
import { useAuth as useOidcAuth } from 'react-oidc-context';

import { setTokenGetter } from '../api/endpoint';
import type { User } from '../api/types';
import { createUserProfile, getCurrentUser } from '../api/user';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: () => Promise<void>;
  logout: () => Promise<void>;
  clearError: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: React.ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const oidcAuth = useOidcAuth();
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const login = async (): Promise<void> => {
    try {
      await oidcAuth.signinRedirect();
    } catch (error) {
      console.error('Login error:', error);
      setError('Login failed. Please try again.');
    }
  };

  const logout = async (): Promise<void> => {
    try {
      // Use signoutRedirect to properly logout and redirect to the post_logout_redirect_uri
      await oidcAuth.signoutRedirect();
      setUser(null);
    } catch (error) {
      console.error('Logout error:', error);
      setError('Logout failed. Please try again.');
    }
  };

  const clearError = (): void => {
    setError(null);
  };

  // Set up token getter for API requests
  useEffect(() => {
    setTokenGetter(() => {
      if (oidcAuth.user?.access_token) {
        return oidcAuth.user.access_token;
      }
      return null;
    });
  }, [oidcAuth.user]);

  // Sync user profile when authentication state changes
  useEffect(() => {
    const syncUserProfile = async (): Promise<void> => {
      if (oidcAuth.isAuthenticated && oidcAuth.user) {
        try {
          const userProfile = await getCurrentUser();
          setUser(userProfile);
        } catch (error) {
          console.error('Error syncing user profile:', error);

          // If user doesn't have a profile, create one automatically
          // This handles both 404 status codes and "Resource not found" error messages
          if (
            (error &&
              typeof error === 'object' &&
              'response' in error &&
              error.response &&
              typeof error.response === 'object' &&
              'status' in error.response &&
              error.response.status === 404) ||
            (error &&
              typeof error === 'object' &&
              'error' in error &&
              typeof error.error === 'string' &&
              error.error.includes('Resource not found'))
          ) {
            try {
              setIsLoading(true);
              setError(null);
              const newUserProfile = await createUserProfile();
              setUser(newUserProfile);
            } catch (createError) {
              console.error('Error creating profile:', createError);
              setError('Failed to create profile. Please try again.');
            } finally {
              setIsLoading(false);
            }
          } else {
            // Don't set error here as it might be expected (user doesn't have profile yet)
          }
        }
      } else {
        setUser(null);
      }
    };

    syncUserProfile();
  }, [oidcAuth.isAuthenticated, oidcAuth.user]);

  // Handle OIDC errors
  useEffect(() => {
    if (oidcAuth.error) {
      setError(oidcAuth.error.message);
      setUser(null);
    }
  }, [oidcAuth.error]);

  const value: AuthContextType = {
    user,
    isAuthenticated: oidcAuth.isAuthenticated,
    isLoading: oidcAuth.isLoading || isLoading,
    error,
    login,
    logout,
    clearError,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
