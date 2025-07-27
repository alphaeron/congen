import React, { createContext, useContext, useEffect, useState } from 'react';
import Keycloak from 'keycloak-js';

import { initKeycloak } from './KeycloakConfig';
import { setKeycloakGetter } from '../api/endpoint';

/**
 * Authentication context interface.
 */
interface AuthContextType {
  keycloak: Keycloak | null;
  authenticated: boolean;
  loading: boolean;
  userId: string | null;
  login: () => void;
  logout: () => void;
  updateToken: (minValidity: number) => Promise<boolean>;
}

/**
 * Authentication context with default values.
 */
const AuthContext = createContext<AuthContextType | undefined>(undefined);

/**
 * Hook to use the authentication context.
 *
 * @return Authentication context value
 * @throws Error when used outside AuthProvider
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

/**
 * Authentication provider component.
 *
 * Manages Keycloak authentication state and provides authentication methods
 * to child components.
 *
 * @param children Child components to wrap with authentication context
 * @return Authentication provider component
 */
export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [keycloak, setKeycloak] = useState<Keycloak | null>(null);
  const [authenticated, setAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);
  const [userId, setUserId] = useState<string | null>(null);

  useEffect(() => {
    const initializeAuth = async () => {
      try {
        const kc = await initKeycloak();
        setKeycloak(kc);
        setAuthenticated(kc.authenticated || false);

        // Extract user ID from token
        if (kc.authenticated && kc.tokenParsed) {
          const tokenParsed = kc.tokenParsed as any;
          setUserId(tokenParsed.sub || null);
        }

        // Set up token refresh
        kc.onTokenExpired = () => {
          kc.updateToken(70).catch(() => {
            console.error('Failed to refresh token');
            kc.logout();
          });
        };

        // Set up authentication state changes
        kc.onAuthSuccess = () => {
          setAuthenticated(true);
          if (kc.tokenParsed) {
            const tokenParsed = kc.tokenParsed as any;
            setUserId(tokenParsed.sub || null);
          }
        };

        kc.onAuthLogout = () => {
          setAuthenticated(false);
          setUserId(null);
        };

        kc.onAuthError = () => {
          setAuthenticated(false);
          setUserId(null);
        };

        kc.onAuthRefreshSuccess = () => {
          setAuthenticated(true);
          if (kc.tokenParsed) {
            const tokenParsed = kc.tokenParsed as any;
            setUserId(tokenParsed.sub || null);
          }
        };

        kc.onAuthRefreshError = () => {
          setAuthenticated(false);
          setUserId(null);
        };
      } catch (error) {
        console.error('Failed to initialize authentication:', error);
      } finally {
        setLoading(false);
      }
    };

    initializeAuth();
  }, []);

  // Register the Keycloak getter for endpoint.ts
  useEffect(() => {
    setKeycloakGetter(() => keycloak);
  }, [keycloak]);

  const login = () => {
    if (keycloak) {
      keycloak.login();
    }
  };

  const logout = () => {
    if (keycloak) {
      keycloak.logout();
    }
  };

  const updateToken = async (minValidity: number): Promise<boolean> => {
    if (keycloak) {
      try {
        return await keycloak.updateToken(minValidity);
      } catch (error) {
        console.error('Failed to update token:', error);
        return false;
      }
    }
    return false;
  };

  const value: AuthContextType = {
    keycloak,
    authenticated,
    loading,
    userId,
    login,
    logout,
    updateToken,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
