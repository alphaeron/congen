import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';

import { App } from './App';

// Mock Keycloak
jest.mock('keycloak-js', () => {
  return jest.fn().mockImplementation(() => ({
    authenticated: false,
    token: null,
    tokenParsed: null,
    login: jest.fn(),
    logout: jest.fn(),
    updateToken: jest.fn().mockResolvedValue(true),
    accountManagement: jest.fn(),
  }));
});

// Mock AuthContext
jest.mock('./auth/AuthContext', () => ({
  useAuth: jest.fn(),
  AuthProvider: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

// Mock KeycloakConfig
jest.mock('./auth/KeycloakConfig', () => ({
  initKeycloak: jest.fn().mockResolvedValue({
    authenticated: false,
    token: null,
    tokenParsed: null,
    login: jest.fn(),
    logout: jest.fn(),
    updateToken: jest.fn().mockResolvedValue(true),
    accountManagement: jest.fn(),
  }),
}));

// Mock setKeycloakGetter
jest.mock('./api/endpoint', () => ({
  setKeycloakGetter: jest.fn(),
}));

describe('App', () => {
  const mockUseAuth = require('./auth/AuthContext').useAuth;

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should show loading state initially', () => {
    mockUseAuth.mockReturnValue({
      authenticated: false,
      loading: true,
      userId: null,
      keycloak: null,
    });

    render(<App />);
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('should render app with ConGen branding', async () => {
    mockUseAuth.mockReturnValue({
      authenticated: false,
      loading: false,
      userId: null,
      keycloak: null,
    });

    render(<App />);

    await waitFor(() => {
      expect(screen.getByText('ConGen')).toBeInTheDocument();
    });
  });

  it('should show navigation menu for authenticated users', async () => {
    mockUseAuth.mockReturnValue({
      authenticated: true,
      loading: false,
      userId: 'test-user-id',
      keycloak: {
        authenticated: true,
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
      },
    });

    render(<App />);

    await waitFor(() => {
      expect(screen.getByText('ConGen')).toBeInTheDocument();
      expect(screen.getByText('Exercises')).toBeInTheDocument();
    });
  });

  it('should show sign in/sign up buttons for unauthenticated users', async () => {
    mockUseAuth.mockReturnValue({
      authenticated: false,
      loading: false,
      userId: null,
      keycloak: null,
    });

    render(<App />);

    await waitFor(() => {
      expect(screen.getByText('Sign in')).toBeInTheDocument();
      expect(screen.getByText('Sign up')).toBeInTheDocument();
    });
  });

  it('should handle authentication initialization errors gracefully', async () => {
    mockUseAuth.mockReturnValue({
      authenticated: false,
      loading: false,
      userId: null,
      keycloak: null,
    });

    render(<App />);

    await waitFor(() => {
      expect(screen.getByText('ConGen')).toBeInTheDocument();
    });
  });
});
