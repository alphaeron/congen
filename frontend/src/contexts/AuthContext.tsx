import React, { createContext, useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth as useOidcAuth } from 'react-oidc-context';
import { registerUser, getCurrentUser } from '../api/user';
import { User } from '../api/types';
import { setTokenGetter } from '../api/endpoint';

/**
 * Decode JWT token and extract user information
 */
const decodeToken = (token: string): any => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
  } catch (error) {
    console.error('Error decoding token:', error);
    return null;
  }
};

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
          
          // Extract groups from JWT token
          const tokenPayload = decodeToken(oidcAuth.user.access_token);
          if (tokenPayload) {
            const groups = tokenPayload.groups || [];
            const roles = tokenPayload.realm_access?.roles || [];
            setUser({
              ...userProfile,
              groups,
              roles,
            });
          } else {
            setUser(userProfile);
          }
        } catch (err) {
          console.error('Failed to sync user profile:', err);
          // If we can't get the profile, still set basic user info from OIDC
          setUser({
            id: 0,
            name: oidcAuth.user.profile.name || '',
            age: 0,
            height: 0,
            weight: 0,
            groups: [],
            roles: [],
            created_at: new Date().toISOString(),
            updated_at: new Date().toISOString(),
          });
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
    } catch (err: any) {
      setError(err.message || 'Login failed');
      throw err;
    }
  };

  const logout = async () => {
    try {
      await oidcAuth.removeUser();
      setUser(null);
    } catch (err) {
      console.error('Logout error:', err);
    }
  };

  const register = async (userData: RegisterData) => {
    try {
      // Register user through backend
      const newUser = await registerUser(
        userData.name,
        userData.age,
        userData.height,
        userData.weight,
        userData.email,
        userData.password,
        userData.unit
      );
      
      // After successful registration, automatically log the user in
      try {
        await oidcAuth.signinRedirect({
          extraQueryParams: {
            login_hint: userData.email,
          },
        });
      } catch (loginErr: any) {
        // If automatic login fails, fall back to manual login
        console.warn('Automatic login after registration failed:', loginErr);
        navigate('/login', { 
          state: { 
            message: 'Registration successful! Please sign in with your new account.',
            email: userData.email 
          },
          replace: true 
        });
      }
      
    } catch (err: any) {
      setError(err.response?.data?.message || 'Registration failed');
      throw err;
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