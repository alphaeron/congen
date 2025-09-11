import React, { createContext, useContext, useEffect, useCallback } from 'react';
import { useAuth as useOidcAuth } from 'react-oidc-context';

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

  // Function to clear all authentication state
  const clearAuthState = useCallback(() => {
    // Clear any local state if needed
  }, []);

  // Set up token getter for API requests - exactly like the frontend
  useEffect(() => {
    if (oidcAuth) {
      setTokenGetter(() => {
        if (oidcAuth.user?.access_token) {
          return oidcAuth.user.access_token;
        }
        return null;
      });
    }
  }, [oidcAuth]);

  const login = async (): Promise<void> => {
    try {
      if (oidcAuth) {
        await oidcAuth.signinRedirect();
      }
    } catch {
      // Login failed - error will be handled by OIDC context
    }
  };

  const logout = async (): Promise<void> => {
    try {
      if (oidcAuth) {
        await oidcAuth.signoutRedirect();
      }
      clearAuthState();
    } catch {
      // Logout failed - still clear auth state
      clearAuthState();
    }
  };

  // Use the actual OIDC authentication state
  const value: AuthContextType = {
    isAuthenticated: oidcAuth?.isAuthenticated || false,
    isLoading: oidcAuth?.isLoading || false,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
