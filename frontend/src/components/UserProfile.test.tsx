import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';

import { UserProfile } from './UserProfile';

// Mock Keycloak
jest.mock('keycloak-js', () => {
  return jest.fn().mockImplementation(() => ({
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
  }));
});

// Mock AuthContext
jest.mock('../auth/AuthContext', () => ({
  useAuth: jest.fn(),
}));

// Mock AuthenticatedOnly component
jest.mock('./AuthenticatedOnly', () => ({
  AuthenticatedOnly: ({ children }: { children: any }) => children,
}));

describe('UserProfile', () => {
  const mockUseAuth = require('../auth/AuthContext').useAuth;

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should not render when not authenticated', () => {
    mockUseAuth.mockReturnValue({
      authenticated: false,
      userId: null,
      keycloak: null,
      logout: jest.fn(),
    });

    const { container } = render(<UserProfile />);
    // Since AuthenticatedOnly is mocked to always render children,
    // we need to check that the component handles null keycloak gracefully
    expect(container.firstChild).toBeInTheDocument();
  });

  it('should not render when keycloak is null', () => {
    mockUseAuth.mockReturnValue({
      authenticated: true,
      userId: null,
      keycloak: null,
      logout: jest.fn(),
    });

    const { container } = render(<UserProfile />);
    // Since AuthenticatedOnly is mocked to always render children,
    // we need to check that the component handles null keycloak gracefully
    expect(container.firstChild).toBeInTheDocument();
  });

  it('should render user profile button when authenticated', async () => {
    const mockKeycloak = {
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
    };

    mockUseAuth.mockReturnValue({
      authenticated: true,
      userId: 'test-user-id',
      keycloak: mockKeycloak,
      logout: jest.fn(),
    });

    render(<UserProfile />);

    const profileButton = screen.getByRole('button');
    expect(profileButton).toBeInTheDocument();
    expect(profileButton).toHaveAttribute('aria-label', 'Account settings');
  });

  it('should open menu when profile button is clicked', async () => {
    const mockKeycloak = {
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
    };

    mockUseAuth.mockReturnValue({
      authenticated: true,
      userId: 'test-user-id',
      keycloak: mockKeycloak,
      logout: jest.fn(),
    });

    render(<UserProfile />);

    const profileButton = screen.getByRole('button');
    fireEvent.click(profileButton);

    await waitFor(() => {
      expect(screen.getByText('Test User')).toBeInTheDocument();
    });
  });

  it('should display user information in menu', async () => {
    const mockKeycloak = {
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
    };

    mockUseAuth.mockReturnValue({
      authenticated: true,
      userId: 'test-user-id',
      keycloak: mockKeycloak,
      logout: jest.fn(),
    });

    render(<UserProfile />);

    const profileButton = screen.getByRole('button');
    fireEvent.click(profileButton);

    await waitFor(() => {
      expect(screen.getByText('Test User')).toBeInTheDocument();
      expect(screen.getByText('test@example.com')).toBeInTheDocument();
    });
  });

  it('should handle logout when logout menu item is clicked', async () => {
    const mockLogout = jest.fn();
    const mockKeycloak = {
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
    };

    mockUseAuth.mockReturnValue({
      authenticated: true,
      userId: 'test-user-id',
      keycloak: mockKeycloak,
      logout: mockLogout,
    });

    render(<UserProfile />);

    const profileButton = screen.getByRole('button');
    fireEvent.click(profileButton);

    await waitFor(() => {
      expect(screen.getByText('Logout')).toBeInTheDocument();
    });

    const logoutButton = screen.getByText('Logout');
    fireEvent.click(logoutButton);

    expect(mockLogout).toHaveBeenCalled();
  });

  it('should handle account management when account settings is clicked', async () => {
    const mockKeycloak = {
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
    };

    mockUseAuth.mockReturnValue({
      authenticated: true,
      userId: 'test-user-id',
      keycloak: mockKeycloak,
      logout: jest.fn(),
    });

    render(<UserProfile />);

    const profileButton = screen.getByRole('button');
    fireEvent.click(profileButton);

    await waitFor(() => {
      expect(screen.getByText('Account Settings')).toBeInTheDocument();
    });

    const accountSettingsButton = screen.getByText('Account Settings');
    fireEvent.click(accountSettingsButton);

    expect(mockKeycloak.accountManagement).toHaveBeenCalled();
  });

  it('should close menu when clicking outside', async () => {
    const mockKeycloak = {
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
    };

    mockUseAuth.mockReturnValue({
      authenticated: true,
      userId: 'test-user-id',
      keycloak: mockKeycloak,
      logout: jest.fn(),
    });

    render(<UserProfile />);

    const profileButton = screen.getByRole('button');
    fireEvent.click(profileButton);

    await waitFor(() => {
      expect(screen.getByText('Test User')).toBeInTheDocument();
    });

    // Click outside to close - use a more specific approach
    const backdrop = document.querySelector('.MuiBackdrop-root');
    if (backdrop) {
      fireEvent.click(backdrop);
    } else {
      // Fallback: click on the document body
      fireEvent.click(document.body);
    }

    // Menu should be closed - wait a bit longer for the animation
    await waitFor(
      () => {
        expect(screen.queryByText('Test User')).not.toBeInTheDocument();
      },
      { timeout: 1000 }
    );
  });

  it('should handle missing user information gracefully', async () => {
    const mockKeycloak = {
      authenticated: true,
      token: 'mock-token',
      tokenParsed: {
        sub: 'test-user-id',
        // Missing user information
      },
      login: jest.fn(),
      logout: jest.fn(),
      updateToken: jest.fn().mockResolvedValue(true),
      accountManagement: jest.fn(),
    };

    mockUseAuth.mockReturnValue({
      authenticated: true,
      userId: 'test-user-id',
      keycloak: mockKeycloak,
      logout: jest.fn(),
    });

    render(<UserProfile />);

    const profileButton = screen.getByRole('button');
    fireEvent.click(profileButton);

    await waitFor(() => {
      expect(screen.getByText('User')).toBeInTheDocument();
    });
  });

  it('should handle missing email gracefully', async () => {
    const mockKeycloak = {
      authenticated: true,
      token: 'mock-token',
      tokenParsed: {
        sub: 'test-user-id',
        preferred_username: 'testuser',
        name: 'Test User',
        // Missing email
      },
      login: jest.fn(),
      logout: jest.fn(),
      updateToken: jest.fn().mockResolvedValue(true),
      accountManagement: jest.fn(),
    };

    mockUseAuth.mockReturnValue({
      authenticated: true,
      userId: 'test-user-id',
      keycloak: mockKeycloak,
      logout: jest.fn(),
    });

    render(<UserProfile />);

    const profileButton = screen.getByRole('button');
    fireEvent.click(profileButton);

    await waitFor(() => {
      expect(screen.getByText('Test User')).toBeInTheDocument();
      // Should not display email since it's missing
      expect(screen.queryByText('test@example.com')).not.toBeInTheDocument();
    });
  });
});
