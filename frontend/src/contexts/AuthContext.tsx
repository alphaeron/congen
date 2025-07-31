import React, { createContext, useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { initiateAuth, exchangeCodeForTokens, refreshToken, logoutUser } from '../api/auth';
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
  const [user, setUser] = useState<User | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const navigate = useNavigate();

  // Register token getter for API requests
  useEffect(() => {
    setTokenGetter(() => accessToken);
  }, [accessToken]);

  // Check for existing tokens on mount
  useEffect(() => {
    const checkAuth = async () => {
      const token = localStorage.getItem('access_token');
      const refreshTokenValue = localStorage.getItem('refresh_token');
      
      if (token && refreshTokenValue) {
        try {
          setAccessToken(token);
          setIsAuthenticated(true);
          
          // Try to get current user profile
          const userProfile = await getCurrentUser();
          
          // Extract groups from JWT token
          const tokenPayload = decodeToken(token);
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
          // Token might be expired, try to refresh
          try {
            const newTokens = await refreshToken(refreshTokenValue);
            localStorage.setItem('access_token', newTokens.access_token);
            localStorage.setItem('refresh_token', newTokens.refresh_token);
            setAccessToken(newTokens.access_token);
            setIsAuthenticated(true);
            
            // Get user profile with new token
            const userProfile = await getCurrentUser();
            
            // Extract groups from new JWT token
            const tokenPayload = decodeToken(newTokens.access_token);
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
          } catch (refreshErr) {
            // Both token and refresh failed, clear storage
            localStorage.removeItem('access_token');
            localStorage.removeItem('refresh_token');
            setIsAuthenticated(false);
            setAccessToken(null);
            setUser(null);
          }
        }
      }
      setIsLoading(false);
    };

    checkAuth();
  }, []);

  const login = async () => {
    try {
      setIsLoading(true);
      setError(null);
      
      // Initiate the authorization code flow
      await initiateAuth();
      // Note: The actual token exchange happens in the callback component
      
    } catch (err: any) {
      setError(err.message || 'Login failed');
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async () => {
    try {
      const refreshTokenValue = localStorage.getItem('refresh_token');
      if (refreshTokenValue) {
        await logoutUser(refreshTokenValue);
      }
    } catch (err) {
      // Even if logout fails, clear local storage
      console.error('Logout error:', err);
    } finally {
      // Clear local storage
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
      setIsAuthenticated(false);
      setUser(null);
      setAccessToken(null);
    }
  };

  const register = async (userData: RegisterData) => {
    try {
      setIsLoading(true);
      setError(null);
      
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
      
      // After successful registration, redirect back to our application to complete the authorization code flow.
      if (newUser.keycloak_user_id) {
        // Registration successful, redirect to the root page
        // TODO: redirect to the user's profile page
        navigate('/');
      } else {
        // Fallback: just set the user data without authentication
        setUser(newUser);
      }
      
    } catch (err: any) {
      setError(err.response?.data?.message || 'Registration failed');
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const clearError = () => {
    setError(null);
  };

  // Function to handle token exchange (called from callback component)
  const handleTokenExchange = async (code: string, state: string) => {
    try {
      const tokens = await exchangeCodeForTokens(code, state);
      
      // Store tokens
      localStorage.setItem('access_token', tokens.access_token);
      localStorage.setItem('refresh_token', tokens.refresh_token);
      setAccessToken(tokens.access_token);
      setIsAuthenticated(true);
      
      // Get user profile
      const userProfile = await getCurrentUser();
      
      // Extract groups from JWT token
      const tokenPayload = decodeToken(tokens.access_token);
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
      
    } catch (err: any) {
      setError(err.message || 'Authentication failed');
      throw err;
    }
  };

  const value: AuthContextType = {
    user,
    isAuthenticated,
    isLoading,
    error,
    login,
    logout,
    register,
    clearError,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}; 