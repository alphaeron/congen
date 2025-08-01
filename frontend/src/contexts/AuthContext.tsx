import React, { createContext, useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth as useOidcAuth } from 'react-oidc-context';
import { registerUser, getCurrentUser } from '../api/user';
import { User } from '../api/types';
import { setTokenGetter } from '../api/endpoint';
import { decodeToken } from '../common/authUtils';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: () => Promise<void>;
  logout: () => Promise<void>;
  register: (userData: RegisterData) => Promise<void>;
  clearError: () => void;
}

interface RegisterData {
  name: string;
  age: number;
  height: number;
  weight: number;
  email: string;
  password: string;
  unit?: string;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
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
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  // Register token getter for API requests
  useEffect(() => {
    setTokenGetter(() => oidcAuth.user?.access_token || null);
  }, [oidcAuth.user?.access_token]);

  // Sync OIDC user with our custom user profile
  useEffect(() => {
    const syncUserProfile = async () => {
      if (oidcAuth.user && oidcAuth.isAuthenticated) {
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
        } catch (error) {
          // If we can't get the profile, log the error and don't set user
          console.error('Failed to get user profile:', error);
          setUser(null);
        }
      } else {
        setUser(null);
      }
    };

    syncUserProfile();
  }, [oidcAuth.user, oidcAuth.isAuthenticated]);

  // Handle OIDC errors
  useEffect(() => {
    if (oidcAuth.error) {
      setError(oidcAuth.error.message);
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
    } catch {
      // Logout error handled silently
    }
  };

  const register = async (userData: RegisterData) => {
    try {
      // Register user through backend
      await registerUser(
        userData.name,
        userData.age,
        userData.height,
        userData.weight,
        userData.email,
        userData.password,
        userData.unit
      );

      // After successful registration, redirect to login page
      navigate('/login', {
        state: {
          message: 'Registration successful! Please sign in with your new account.',
          email: userData.email,
        },
        replace: true,
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
          : 'Registration failed';
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
    register,
    clearError,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
