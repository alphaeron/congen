import React from 'react';
import { render, screen, waitFor, act } from '@testing-library/react';
import { fireEvent } from '@testing-library/react';

import { AuthProvider, useAuth } from './AuthContext';

// Mock Keycloak
jest.mock('keycloak-js', () => {
  return jest.fn().mockImplementation(() => ({
    authenticated: false,
    token: 'mock-token',
    tokenParsed: {
      sub: 'test-user-id',
      preferred_username: 'testuser',
      email: 'test@example.com',
      name: 'Test User',
    },
    login: jest.fn(),
    logout: jest.fn(),
    updateToken: jest.fn().mockResolvedValue(true),
    accountManagement: jest.fn(),
  }));
});

// Mock initKeycloak
jest.mock('./KeycloakConfig', () => ({
  initKeycloak: jest.fn(),
}));

// Mock setKeycloakGetter
jest.mock('../api/endpoint', () => ({
  setKeycloakGetter: jest.fn(),
}));

// Test component to use the auth context
const TestComponent: React.FC = () => {
  const { keycloak, authenticated, loading, userId, login, logout, updateToken } = useAuth();

  return (
    <div>
      <div data-testid="loading">{loading.toString()}</div>
      <div data-testid="authenticated">{authenticated.toString()}</div>
      <div data-testid="userId">{userId || 'null'}</div>
      <div data-testid="keycloak">{keycloak ? 'present' : 'null'}</div>
      <button data-testid="login" onClick={login}>
        Login
      </button>
      <button data-testid="logout" onClick={logout}>
        Logout
      </button>
      <button data-testid="update-token" onClick={() => updateToken(70)}>
        Update Token
      </button>
    </div>
  );
};

describe('AuthContext', () => {
  const mockInitKeycloak = require('./KeycloakConfig').initKeycloak;

  beforeEach(() => {
    jest.clearAllMocks();

    // Default mock implementation
    mockInitKeycloak.mockResolvedValue({
      authenticated: false,
      token: 'mock-token',
      tokenParsed: {
        sub: 'test-user-id',
        preferred_username: 'testuser',
        email: 'test@example.com',
        name: 'Test User',
      },
      login: jest.fn(),
      logout: jest.fn(),
      updateToken: jest.fn().mockResolvedValue(true),
      accountManagement: jest.fn(),
    });
  });

  describe('AuthProvider', () => {
    it('should initialize with loading state', () => {
      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );

      expect(screen.getByTestId('loading')).toHaveTextContent('true');
      expect(screen.getByTestId('authenticated')).toHaveTextContent('false');
      expect(screen.getByTestId('userId')).toHaveTextContent('null');
      expect(screen.getByTestId('keycloak')).toHaveTextContent('null');
    });

    it('should initialize Keycloak and complete loading', async () => {
      await act(async () => {
        render(
          <AuthProvider>
            <TestComponent />
          </AuthProvider>
        );
      });

      await waitFor(() => {
        expect(screen.getByTestId('loading')).toHaveTextContent('false');
      });

      expect(screen.getByTestId('keycloak')).toHaveTextContent('present');
      // When not authenticated, userId should be null
      expect(screen.getByTestId('userId')).toHaveTextContent('null');
    });

    it('should handle authentication success', async () => {
      mockInitKeycloak.mockResolvedValue({
        authenticated: true,
        token: 'mock-token',
        tokenParsed: {
          sub: 'authenticated-user-id',
          preferred_username: 'testuser',
          email: 'test@example.com',
          name: 'Test User',
        },
        login: jest.fn(),
        logout: jest.fn(),
        updateToken: jest.fn().mockResolvedValue(true),
        accountManagement: jest.fn(),
      });

      await act(async () => {
        render(
          <AuthProvider>
            <TestComponent />
          </AuthProvider>
        );
      });

      await waitFor(() => {
        expect(screen.getByTestId('loading')).toHaveTextContent('false');
      });

      expect(screen.getByTestId('authenticated')).toHaveTextContent('true');
      expect(screen.getByTestId('userId')).toHaveTextContent('authenticated-user-id');
    });

    it('should handle authentication failure', async () => {
      mockInitKeycloak.mockRejectedValue(new Error('Authentication failed'));

      await act(async () => {
        render(
          <AuthProvider>
            <TestComponent />
          </AuthProvider>
        );
      });

      await waitFor(() => {
        expect(screen.getByTestId('loading')).toHaveTextContent('false');
      });

      expect(screen.getByTestId('authenticated')).toHaveTextContent('false');
      expect(screen.getByTestId('userId')).toHaveTextContent('null');
    });

    it('should handle token without sub field', async () => {
      mockInitKeycloak.mockResolvedValue({
        authenticated: true,
        token: 'mock-token',
        tokenParsed: {
          preferred_username: 'testuser',
          email: 'test@example.com',
          name: 'Test User',
        },
        login: jest.fn(),
        logout: jest.fn(),
        updateToken: jest.fn().mockResolvedValue(true),
        accountManagement: jest.fn(),
      });

      await act(async () => {
        render(
          <AuthProvider>
            <TestComponent />
          </AuthProvider>
        );
      });

      await waitFor(() => {
        expect(screen.getByTestId('loading')).toHaveTextContent('false');
      });

      expect(screen.getByTestId('authenticated')).toHaveTextContent('true');
      expect(screen.getByTestId('userId')).toHaveTextContent('null');
    });
  });

  describe('useAuth hook', () => {
    it('should throw error when used outside AuthProvider', () => {
      // Suppress console.error for this test
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

      expect(() => {
        render(<TestComponent />);
      }).toThrow('useAuth must be used within an AuthProvider');

      consoleSpy.mockRestore();
    });
  });
});
