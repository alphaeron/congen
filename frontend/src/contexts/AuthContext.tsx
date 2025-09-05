import { useSnackbar } from 'notistack';
import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { useAuth as useOidcAuth } from 'react-oidc-context';

import { setTokenGetter } from '../api/endpoint';
import type { User } from '../api/types';
import { createUserProfile, getCurrentUser } from '../api/user';
import { clearAuthenticationState, isTokenExpired, sanitizeToken, preventTokenExposure } from '../common/authUtils';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: () => Promise<void>;
  logout: () => Promise<void>;
  clearAuthState: () => void;
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
  const { enqueueSnackbar } = useSnackbar();
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  // Function to clear all authentication state
  const clearAuthState = useCallback(() => {
    setUser(null);
    clearAuthenticationState();
  }, []);

  const login = async (): Promise<void> => {
    try {
      await oidcAuth.signinRedirect();
    } catch {
      enqueueSnackbar('Login failed. Please try again.', { variant: 'error' });
    }
  };

  const logout = async (): Promise<void> => {
    try {
      // Use signoutRedirect to properly logout and redirect to the post_logout_redirect_uri
      await oidcAuth.signoutRedirect();
      clearAuthState();
    } catch {
      enqueueSnackbar('Logout failed. Please try again.', { variant: 'error' });
      // Even if logout fails, clear local state
      clearAuthState();
    }
  };

  // Set up token getter for API requests
  useEffect(() => {
    setTokenGetter(() => {
      if (oidcAuth.user?.access_token) {
        // Sanitize token to prevent XSS
        const sanitizedToken = sanitizeToken(oidcAuth.user.access_token);
        if (!sanitizedToken) {
          clearAuthState();
          return null;
        }
        
        // Check if token is expired
        if (isTokenExpired(sanitizedToken)) {
          // Token is expired, clear state and return null
          clearAuthState();
          return null;
        }
        
        return sanitizedToken;
      }
      return null;
    });
  }, [oidcAuth.user, clearAuthState]);

  // Initialize token exposure prevention
  useEffect(() => {
    preventTokenExposure();
  }, []);

  // Sync user profile when authentication state changes
  useEffect(() => {
    const syncUserProfile = async (): Promise<void> => {
      if (oidcAuth.isAuthenticated && oidcAuth.user) {
        try {
          const userProfile = await getCurrentUser();
          setUser(userProfile);
        } catch (error) {
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
              const newUserProfile = await createUserProfile();
              setUser(newUserProfile);
            } catch {
              enqueueSnackbar('Failed to create profile. Please try again.', { variant: 'error' });
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
      enqueueSnackbar(oidcAuth.error.message, { variant: 'error' });
      clearAuthState();
    }
  }, [oidcAuth.error, enqueueSnackbar, clearAuthState]);

  // Handle authentication state changes
  useEffect(() => {
    // If OIDC is no longer authenticated but we still have user state, clear it
    if (!oidcAuth.isAuthenticated && user) {
      clearAuthState();
    }
  }, [oidcAuth.isAuthenticated, user, clearAuthState]);

  // Handle user object changes
  useEffect(() => {
    // If OIDC user is null but we still have user state, clear it
    if (!oidcAuth.user && user) {
      clearAuthState();
    }
  }, [oidcAuth.user, user, clearAuthState]);

  const value: AuthContextType = {
    user,
    isAuthenticated: oidcAuth.isAuthenticated,
    isLoading: oidcAuth.isLoading || isLoading,
    login,
    logout,
    clearAuthState,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
