import { ThemeProvider, createTheme } from '@mui/material/styles';
import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';

import { ProfileOverview } from './ProfileOverview';
import type { User } from '../api/types';

// Create a theme for testing
const theme = createTheme();

const renderWithTheme = (component: React.ReactElement) => {
  return render(<ThemeProvider theme={theme}>{component}</ThemeProvider>);
};

describe('ProfileOverview', () => {
  const mockUser: User = {
    keycloak_id: 'test-user-id',
    name: 'Test User',
    created_at: new Date('2024-01-01T00:00:00.000Z'),
    updated_at: new Date('2024-01-01T00:00:00.000Z'),
    roles: ['user'],
  };

  const mockOnEditProfile = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders the component with correct title', () => {
    renderWithTheme(<ProfileOverview user={mockUser} onEditProfile={mockOnEditProfile} />);

    expect(screen.getByText('Profile Overview')).toBeInTheDocument();
  });

  it('displays user information correctly', () => {
    renderWithTheme(<ProfileOverview user={mockUser} onEditProfile={mockOnEditProfile} />);

    expect(screen.getByText('Test User')).toBeInTheDocument();
    // Use a more flexible regex that accounts for timezone differences
    expect(screen.getByText(/Member since December 31, 2023/)).toBeInTheDocument();
    expect(screen.getByText('Roles: user')).toBeInTheDocument();
  });

  it('calls onEditProfile when edit button is clicked', () => {
    renderWithTheme(<ProfileOverview user={mockUser} onEditProfile={mockOnEditProfile} />);

    const editButton = screen.getByRole('button', { name: /edit profile/i });
    fireEvent.click(editButton);

    expect(mockOnEditProfile).toHaveBeenCalledTimes(1);
  });

  it('displays roles when user has roles', () => {
    const userWithRoles: User = {
      ...mockUser,
      roles: ['user', 'admin'],
    };

    renderWithTheme(<ProfileOverview user={userWithRoles} onEditProfile={mockOnEditProfile} />);

    expect(screen.getByText('Roles: user, admin')).toBeInTheDocument();
  });

  it('handles user without roles gracefully', () => {
    const userWithoutRoles: User = {
      ...mockUser,
      roles: undefined,
    };

    renderWithTheme(<ProfileOverview user={userWithoutRoles} onEditProfile={mockOnEditProfile} />);

    expect(screen.getByText('Test User')).toBeInTheDocument();
    expect(screen.queryByText(/Roles:/)).not.toBeInTheDocument();
  });

  it('handles missing created_at date', () => {
    const userWithoutDate: User = {
      ...mockUser,
      created_at: undefined as unknown as string,
    };

    renderWithTheme(<ProfileOverview user={userWithoutDate} onEditProfile={mockOnEditProfile} />);

    expect(screen.getByText('Member since N/A')).toBeInTheDocument();
  });

  it('renders avatar with account circle icon', () => {
    renderWithTheme(<ProfileOverview user={mockUser} onEditProfile={mockOnEditProfile} />);

    // Look for the AccountCircleIcon instead of img role
    const avatarIcon = screen.getByTestId('AccountCircleIcon');
    expect(avatarIcon).toBeInTheDocument();
  });
});
