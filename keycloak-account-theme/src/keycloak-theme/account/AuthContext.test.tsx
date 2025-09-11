import { render, screen, act } from '@testing-library/react';
import React from 'react';

import { AuthProvider, useAuth } from './AuthContext';

// Mock react-oidc-context
const mockOidcAuth = {
  isAuthenticated: false,
  isLoading: false,
  user: null,
  signinRedirect: jest.fn(),
  signoutRedirect: jest.fn(),
};

jest.mock('react-oidc-context', () => ({
  useAuth: jest.fn(() => mockOidcAuth),
}));

// Mock the API client
jest.mock('./api/client', () => ({
  setTokenGetter: jest.fn(),
}));

const TestComponent: React.FC = () => {
  const auth = useAuth();
  return (
    <div>
      <div data-testid="is-authenticated">{auth.isAuthenticated.toString()}</div>
      <div data-testid="is-loading">{auth.isLoading.toString()}</div>
      <button data-testid="login-btn" onClick={auth.login}>
        Login
      </button>
      <button data-testid="logout-btn" onClick={auth.logout}>
        Logout
      </button>
    </div>
  );
};

describe('AuthContext', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('AuthProvider', () => {
    it('should provide auth context to children', () => {
      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );

      expect(screen.getByTestId('is-authenticated')).toHaveTextContent('false');
      expect(screen.getByTestId('is-loading')).toHaveTextContent('false');
    });

    it('should reflect authentication state from OIDC', () => {
      mockOidcAuth.isAuthenticated = true;
      mockOidcAuth.isLoading = true;

      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );

      expect(screen.getByTestId('is-authenticated')).toHaveTextContent('true');
      expect(screen.getByTestId('is-loading')).toHaveTextContent('true');
    });

    it('should handle login action', async () => {
      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );

      const loginBtn = screen.getByTestId('login-btn');
      await act(async () => {
        loginBtn.click();
      });

      expect(mockOidcAuth.signinRedirect).toHaveBeenCalled();
    });

    it('should handle logout action', async () => {
      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );

      const logoutBtn = screen.getByTestId('logout-btn');
      await act(async () => {
        logoutBtn.click();
      });

      expect(mockOidcAuth.signoutRedirect).toHaveBeenCalled();
    });

    it('should handle login error gracefully', async () => {
      mockOidcAuth.signinRedirect.mockRejectedValue(new Error('Login failed'));

      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );

      const loginBtn = screen.getByTestId('login-btn');
      await act(async () => {
        loginBtn.click();
      });

      expect(mockOidcAuth.signinRedirect).toHaveBeenCalled();
    });

    it('should handle logout error gracefully', async () => {
      mockOidcAuth.signoutRedirect.mockRejectedValue(new Error('Logout failed'));

      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );

      const logoutBtn = screen.getByTestId('logout-btn');
      await act(async () => {
        logoutBtn.click();
      });

      expect(mockOidcAuth.signoutRedirect).toHaveBeenCalled();
    });
  });

  describe('useAuth', () => {
    it('should throw error when used outside AuthProvider', () => {
      expect(() => {
        render(<TestComponent />);
      }).toThrow('useAuth must be used within an AuthProvider');
    });

    it('should return auth context when used within AuthProvider', () => {
      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );

      expect(screen.getByTestId('is-authenticated')).toBeInTheDocument();
      expect(screen.getByTestId('is-loading')).toBeInTheDocument();
    });
  });
});
