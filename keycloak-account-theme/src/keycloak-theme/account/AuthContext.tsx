import React, { createContext, useContext, useEffect, useCallback } from 'react';
import { useAuth as useOidcAuth } from 'react-oidc-context';
import { useSnackbar } from 'notistack';

import { setTokenGetter } from './api/client';

interface AuthContextType {
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

  // Function to clear all authentication state
  const clearAuthState = useCallback(() => {
    // Clear any local state if needed
  }, []);

  // Set up token getter for API requests - exactly like the frontend
  useEffect(() => {
    setTokenGetter(() => {
      if (oidcAuth.user?.access_token) {
        return oidcAuth.user.access_token;
      }
      return null;
    });
  }, [oidcAuth.user]);


  const login = async (): Promise<void> => {
    try {
      await oidcAuth.signinRedirect();
    } catch (error) {
      console.error('Login failed:', error);
    }
  };

  const logout = async (): Promise<void> => {
    try {
      await oidcAuth.signoutRedirect();
      clearAuthState();
    } catch (error) {
      console.error('Logout failed:', error);
      clearAuthState();
    }
  };

  const value: AuthContextType = {
    isAuthenticated: oidcAuth.isAuthenticated,
    isLoading: oidcAuth.isLoading,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};