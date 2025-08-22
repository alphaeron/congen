import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { ProfileOverview } from './ProfileOverview';
import type { User } from '../api/types';

// Create a theme for testing
const theme = createTheme();

const renderWithTheme = (component: React.ReactElement) => {
  return render(
    <ThemeProvider theme={theme}>
      {component}
    </ThemeProvider>
  );
};

describe('ProfileOverview', () => {
  const mockUser: User = {
    keycloak_id: 'test-user-id',
    name: 'Test User',
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
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
    expect(screen.getByText(/Member since January 1, 2024/)).toBeInTheDocument();
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
      created_at: undefined as any,
    };
    
    renderWithTheme(<ProfileOverview user={userWithoutDate} onEditProfile={mockOnEditProfile} />);
    
    expect(screen.getByText('Member since N/A')).toBeInTheDocument();
  });

  it('renders avatar with account circle icon', () => {
    renderWithTheme(<ProfileOverview user={mockUser} onEditProfile={mockOnEditProfile} />);
    
    const avatar = screen.getByRole('img', { hidden: true });
    expect(avatar).toBeInTheDocument();
  });
});
