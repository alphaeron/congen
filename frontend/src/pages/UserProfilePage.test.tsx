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
      logout: jest.fn(),
      clearError: jest.fn(),
    });

    renderWithProviders(<UserProfilePage />);

    expect(screen.getByText('User Profile')).toBeInTheDocument();
    expect(screen.getByText('John Doe')).toBeInTheDocument();
  });

  it('should show deactivate account button when user exists', () => {
    mockUseAuth.mockReturnValue({
      user: mockUser,
      logout: jest.fn(),
      clearError: jest.fn(),
    });

    renderWithProviders(<UserProfilePage />);

    expect(screen.getByText('Deactivate Account')).toBeInTheDocument();
  });

  it('should show edit profile button when user exists', () => {
    mockUseAuth.mockReturnValue({
      user: mockUser,
      logout: jest.fn(),
      clearError: jest.fn(),
    });

    renderWithProviders(<UserProfilePage />);

    expect(screen.getAllByText('Edit Profile')[0]).toBeInTheDocument();
  });

  it('should show error alert when user is not available', () => {
    mockUseAuth.mockReturnValue({
      user: null,
      error: 'User information not available. Please log in to view your profile.',
      logout: jest.fn(),
      clearError: jest.fn(),
    });

    renderWithProviders(<UserProfilePage />);

    expect(
      screen.getByText('User information not available. Please log in to view your profile.')
    ).toBeInTheDocument();
    expect(screen.queryByText('User Profile')).not.toBeInTheDocument();
  });
});
