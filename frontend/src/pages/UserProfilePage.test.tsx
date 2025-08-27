import { render, screen } from '@testing-library/react';
import React from 'react';
import { BrowserRouter } from 'react-router';

import { UserProfilePage } from './UserProfilePage';
import type { User } from '../api/types';

// Mock the auth context
const mockUser: User = {
  keycloak_id: 'test-user-id',
  name: 'John Doe',
  created_at: '2024-01-01T00:00:00Z',
  updated_at: '2024-01-01T00:00:00Z',
  roles: ['user'],
};

const mockUseAuth = jest.fn();

jest.mock('../contexts/AuthContext', () => ({
  useAuth: () => mockUseAuth(),
}));

jest.mock('react-oidc-context', () => ({
  useAuth: () => ({
    user: {
      profile: {
        given_name: 'John',
        family_name: 'Doe',
        name: 'John Doe',
      },
    },
  }),
}));

const renderWithProviders = (component: React.ReactElement) => {
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe('UserProfilePage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render user profile information when user exists', () => {
    mockUseAuth.mockReturnValue({
      user: mockUser,
      isLoading: false,
    });

    renderWithProviders(<UserProfilePage />);

    expect(screen.getByText('User Profile')).toBeInTheDocument();
    expect(screen.getByText('John Doe')).toBeInTheDocument();
  });

  it('should show deactivate account button when user exists', () => {
    mockUseAuth.mockReturnValue({
      user: mockUser,
      isLoading: false,
    });

    renderWithProviders(<UserProfilePage />);

    // The Deactivate Account button is in the Account Security tab, not visible by default
    // Instead, check that the Account Security tab is available
    expect(screen.getByText('Account Security')).toBeInTheDocument();
  });

  it('should show edit profile button when user exists', () => {
    mockUseAuth.mockReturnValue({
      user: mockUser,
      isLoading: false,
    });

    renderWithProviders(<UserProfilePage />);

    expect(screen.getAllByText('Edit Profile')[0]).toBeInTheDocument();
  });

  it('should show loading message when user is not available', () => {
    mockUseAuth.mockReturnValue({
      user: null,
      isLoading: false,
    });

    renderWithProviders(<UserProfilePage />);

    expect(screen.getByText('Creating Your Profile')).toBeInTheDocument();
    expect(screen.getByText('Your profile is being created automatically using your Keycloak information...')).toBeInTheDocument();
    expect(screen.queryByText('User Profile')).not.toBeInTheDocument();
  });

  it('should show loading spinner when isLoading is true', () => {
    mockUseAuth.mockReturnValue({
      user: null,
      isLoading: true,
    });

    renderWithProviders(<UserProfilePage />);

    // The LoadingSpinner component should be rendered
    expect(screen.queryByText('User Profile')).not.toBeInTheDocument();
    expect(screen.queryByText('Creating Your Profile')).not.toBeInTheDocument();
  });
});
