import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';

// Mock react-oidc-context
jest.mock('react-oidc-context', () => ({
  useAuth: () => ({
    isAuthenticated: true,
    user: {
      profile: {
        sub: 'test-user-id',
        preferred_username: 'testuser',
        email: 'test@example.com',
        name: 'Test User',
      },
    },
    signoutRedirect: jest.fn(),
  }),
  AuthProvider: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

// Mock AuthorizedElement component
jest.mock('./AuthorizedElement', () => ({
  AuthorizedElement: ({ children }: { children: any }) => children,
}));

import { UserProfile } from './UserProfile';

describe('UserProfile', () => {
  it('should render user profile button when authenticated', () => {
    render(<UserProfile />);

    const profileButton = screen.getByRole('button');
    expect(profileButton).toBeInTheDocument();
    expect(profileButton).toHaveAttribute('aria-label', 'Account settings');
  });

  it('should display user information when menu is opened', async () => {
    render(<UserProfile />);

    const profileButton = screen.getByRole('button');
    fireEvent.click(profileButton);

    // Wait for the menu to open and check that the menu items are present
    await waitFor(() => {
      expect(screen.getByText('Test User')).toBeInTheDocument();
    });
    expect(screen.getByText('test@example.com')).toBeInTheDocument();
    expect(screen.getByText('Logout')).toBeInTheDocument();
  });

  it('should render menu items correctly', async () => {
    render(<UserProfile />);

    const profileButton = screen.getByRole('button');
    fireEvent.click(profileButton);

    await waitFor(() => {
      expect(screen.getByText('Test User')).toBeInTheDocument();
    });

    // Check that all expected menu items are present
    expect(screen.getByText('test@example.com')).toBeInTheDocument();
    expect(screen.getByText('Logout')).toBeInTheDocument();
  });
});
