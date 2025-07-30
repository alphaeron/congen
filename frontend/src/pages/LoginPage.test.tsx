import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

import { LoginPage } from './LoginPage';
import { AuthProvider, useAuth } from 'react-oidc-context';

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

// Mock react-oidc-context
const mockUseAuth = jest.fn();
jest.mock('react-oidc-context', () => ({
  useAuth: mockUseAuth,
  AuthProvider: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

// Test wrapper component
const TestWrapper = () => {
  return (
    <AuthProvider>
              <MemoryRouter>
          <LoginPage />
        </MemoryRouter>
    </AuthProvider>
  );
};

describe('LoginPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render login page with welcome message', async () => {
    render(<TestWrapper />);

    await waitFor(() => {
      expect(screen.getByText('Welcome to ConGen')).toBeInTheDocument();
    });
    expect(screen.getByText('Your AI-powered workout companion')).toBeInTheDocument();
  });

  it('should render sign in and sign up cards', async () => {
    render(<TestWrapper />);

    await waitFor(() => {
      expect(screen.getByText('Create Account')).toBeInTheDocument();
    });
    expect(screen.getByText('Sign Up')).toBeInTheDocument();
  });

  it('should display feature list', async () => {
    render(<TestWrapper />);

    await waitFor(() => {
      expect(
        screen.getByText("By signing in or creating an account, you'll be able to:")
      ).toBeInTheDocument();
    });
    expect(screen.getByText('Generate personalized workout programs')).toBeInTheDocument();
    expect(screen.getByText('Track your exercise preferences and equipment')).toBeInTheDocument();
    expect(screen.getByText('Monitor your progress and one-rep maxes')).toBeInTheDocument();
    expect(screen.getByText('Access a comprehensive exercise database')).toBeInTheDocument();
  });

  it('should show loading state when authentication is loading', () => {
    // Mock loading state
    const { initKeycloak } = require('../auth/KeycloakConfig');
    initKeycloak.mockImplementation(() => new Promise(() => {})); // Never resolves

    render(<TestWrapper />);

    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('should call login function when sign in button is clicked', async () => {
    const mockLogin = jest.fn();
    const { initKeycloak } = require('../auth/KeycloakConfig');
    initKeycloak.mockResolvedValue({
      authenticated: false,
      token: null,
      tokenParsed: null,
      login: mockLogin,
      logout: jest.fn(),
      updateToken: jest.fn().mockResolvedValue(true),
      accountManagement: jest.fn(),
    });

    render(<TestWrapper />);

    await waitFor(() => {
      expect(screen.getByText('Sign Up')).toBeInTheDocument();
    });

    const signInButton = screen.getByRole('button', { name: 'Sign In' });
    fireEvent.click(signInButton);
    expect(mockLogin).toHaveBeenCalled();
  });

  it('should call login function when sign up button is clicked', async () => {
    const mockLogin = jest.fn();
    const { initKeycloak } = require('../auth/KeycloakConfig');
    initKeycloak.mockResolvedValue({
      authenticated: false,
      token: null,
      tokenParsed: null,
      login: mockLogin,
      logout: jest.fn(),
      updateToken: jest.fn().mockResolvedValue(true),
      accountManagement: jest.fn(),
    });

    render(<TestWrapper />);

    await waitFor(() => {
      expect(screen.getByText('Sign Up')).toBeInTheDocument();
    });

    const signUpButton = screen.getByText('Sign Up');
    fireEvent.click(signUpButton);

    expect(mockLogin).toHaveBeenCalled();
  });

  it('should display sign in card content correctly', async () => {
    render(<TestWrapper />);

    await waitFor(() => {
      expect(screen.getByText('Create Account')).toBeInTheDocument();
    });

    const signInCard = screen.getByText('Sign In', { selector: 'h3' }).closest('.MuiCard-root');
    expect(signInCard).toBeInTheDocument();

    expect(
      screen.getByText('Access your personalized workout programs and track your progress.')
    ).toBeInTheDocument();
  });

  it('should display sign up card content correctly', async () => {
    render(<TestWrapper />);

    await waitFor(() => {
      expect(screen.getByText('Create Account')).toBeInTheDocument();
    });

    const signUpCard = screen.getByText('Create Account').closest('.MuiCard-root');
    expect(signUpCard).toBeInTheDocument();

    expect(
      screen.getByText(
        'New to ConGen? Create an account to get started with personalized workout programs.'
      )
    ).toBeInTheDocument();
  });

  it('should have proper button styling', async () => {
    render(<TestWrapper />);

    await waitFor(() => {
      expect(screen.getByText('Create Account')).toBeInTheDocument();
    });

    const signInButton = screen.getByRole('button', { name: 'Sign In' });
    const signUpButton = screen.getByRole('button', { name: 'Sign Up' });

    expect(signInButton).toHaveClass('MuiButton-contained');
    expect(signUpButton).toHaveClass('MuiButton-outlined');
  });

  it('should handle authentication failure gracefully', async () => {
    const { initKeycloak } = require('../auth/KeycloakConfig');
    initKeycloak.mockRejectedValue(new Error('Auth failed'));

    const consoleSpy = jest.spyOn(console, 'error').mockImplementation();

    render(<TestWrapper />);

    await waitFor(() => {
      expect(screen.getByText('Create Account')).toBeInTheDocument();
    });

    // Should still show the login page even if auth initialization fails
    expect(screen.getByText('Welcome to ConGen')).toBeInTheDocument();

    consoleSpy.mockRestore();
  });
});
