import { useSnackbar } from 'notistack';
import React, { createContext, useContext, useEffect, useState } from 'react';
import { useAuth as useOidcAuth } from 'react-oidc-context';

import { setTokenGetter } from '../api/endpoint';
import type { User } from '../api/types';
import { createUserProfile, getCurrentUser } from '../api/user';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: () => Promise<void>;
  logout: () => Promise<void>;
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
      setUser(null);
    } catch {
      enqueueSnackbar('Logout failed. Please try again.', { variant: 'error' });
    }
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
      setUser(null);
    }
  }, [oidcAuth.error, enqueueSnackbar]);

  const value: AuthContextType = {
    user,
    isAuthenticated: oidcAuth.isAuthenticated,
    isLoading: oidcAuth.isLoading || isLoading,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
