import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { useAuth as useOidcAuth } from 'react-oidc-context';

import type { UserProfile } from './api/types';

interface AuthContextType {
  user: UserProfile | null;
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
  const [user, setUser] = useState<UserProfile | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  // Function to clear all authentication state
  const clearAuthState = useCallback(() => {
    setUser(null);
  }, []);

  const login = async (): Promise<void> => {
    try {
      await oidcAuth.signinRedirect();
    } catch (error) {
      console.error('Login failed:', error);
    }
  };

  const logout = async (): Promise<void> => {
    try {
      // Use signoutRedirect to properly logout and redirect to the post_logout_redirect_uri
      await oidcAuth.signoutRedirect();
      clearAuthState();
    } catch (error) {
      console.error('Logout failed:', error);
      // Even if logout fails, clear local state
      clearAuthState();
    }
  };

  // Sync user profile when authentication state changes
  useEffect(() => {
    const syncUserProfile = async (): Promise<void> => {
      if (oidcAuth.isAuthenticated && oidcAuth.user) {
        try {
          // Transform OIDC user to UserProfile format
          const userProfile: UserProfile = {
            id: oidcAuth.user.profile.sub,
            username: oidcAuth.user.profile.preferred_username || '',
            email: oidcAuth.user.profile.email || '',
            firstName: oidcAuth.user.profile.given_name || '',
            lastName: oidcAuth.user.profile.family_name || '',
            emailVerified: oidcAuth.user.profile.email_verified || false,
            enabled: true,
            createdTimestamp: Date.now(),
          };
          setUser(userProfile);
        } catch (error) {
          console.error('Failed to sync user profile:', error);
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
      console.error('OIDC error:', oidcAuth.error);
      clearAuthState();
    }
  }, [oidcAuth.error, clearAuthState]);

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
