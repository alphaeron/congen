import React from 'react';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { UserProfilePage } from './UserProfilePage';

// Mock the auth context
const mockUser = {
  id: 1,
  name: 'John Doe',
  age: 30,
  height: 175,
  weight: 80,
  created_at: '2024-01-01T00:00:00Z',
  updated_at: '2024-01-01T00:00:00Z',
  keycloak_user_id: 'test-user-id',
  groups: ['fitness-enthusiasts'],
  roles: ['user'],
};

const mockUseAuth = jest.fn();

jest.mock('../contexts/AuthContext', () => ({
  useAuth: () => mockUseAuth(),
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
    });

    renderWithProviders(<UserProfilePage />);

    expect(screen.getByText('User Profile')).toBeInTheDocument();
    expect(screen.getByText('John Doe')).toBeInTheDocument();
    expect(screen.getByText('30 years old')).toBeInTheDocument();
    expect(screen.getByText('175 cm')).toBeInTheDocument();
    expect(screen.getByText('80 kg')).toBeInTheDocument();
  });

  it('should display roles and groups when user exists', () => {
    mockUseAuth.mockReturnValue({
      user: mockUser,
      logout: jest.fn(),
    });

    renderWithProviders(<UserProfilePage />);

    expect(screen.getByText('Roles & Groups')).toBeInTheDocument();
    expect(screen.getByText('user')).toBeInTheDocument();
    expect(screen.getByText('fitness-enthusiasts')).toBeInTheDocument();
  });

  it('should show deactivate account button when user exists', () => {
    mockUseAuth.mockReturnValue({
      user: mockUser,
      logout: jest.fn(),
    });

    renderWithProviders(<UserProfilePage />);

    expect(screen.getByText('Deactivate Account')).toBeInTheDocument();
  });

  it('should show edit profile button when user exists', () => {
    mockUseAuth.mockReturnValue({
      user: mockUser,
      logout: jest.fn(),
    });

    renderWithProviders(<UserProfilePage />);

    expect(screen.getByText('Edit Profile')).toBeInTheDocument();
  });

  it('should show error alert when user is not available', () => {
    mockUseAuth.mockReturnValue({
      user: null,
      logout: jest.fn(),
    });

    renderWithProviders(<UserProfilePage />);

    expect(
      screen.getByText('User information not available. Please log in to view your profile.')
    ).toBeInTheDocument();
    expect(screen.queryByText('User Profile')).not.toBeInTheDocument();
  });
});
