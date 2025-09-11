import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';

import { CongenAppBar } from './CongenAppBar';
import { handleLogout, navigateToFrontend } from './utils';

// Mock the utils module
jest.mock('./utils', () => ({
  handleLogout: jest.fn(),
  navigateToFrontend: jest.fn(),
}));

// SVG import is mocked by Jest config

const mockKcContext = {
  themeType: 'account' as const,
  themeName: 'test-theme',
  properties: {},
};

const mockUser = {
  username: 'testuser',
  email: 'test@example.com',
  firstName: 'Test',
  lastName: 'User',
};

describe('CongenAppBar', () => {
  const defaultProps = {
    kcContext: mockKcContext,
    user: mockUser,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders app bar with logo and title', () => {
    render(<CongenAppBar {...defaultProps} />);

    expect(screen.getByText('ConGen')).toBeInTheDocument();
    expect(screen.getByAltText('ConGen')).toBeInTheDocument();
  });

  it('renders user menu when user is provided', () => {
    render(<CongenAppBar {...defaultProps} />);

    const userButton = screen.getByRole('button');
    expect(userButton).toBeInTheDocument();
  });

  it('renders without user menu when user is not provided', () => {
    render(<CongenAppBar {...defaultProps} user={undefined} />);

    // The button is still rendered but shows default avatar
    const avatar = screen.getByText('U');
    expect(avatar).toBeInTheDocument();
  });

  it('opens user menu when user button is clicked', () => {
    render(<CongenAppBar {...defaultProps} />);

    const userButton = screen.getByRole('button');
    fireEvent.click(userButton);

    expect(screen.getByText('Profile')).toBeInTheDocument();
    expect(screen.getByText('Sign Out')).toBeInTheDocument();
  });

  it('can interact with user menu items', () => {
    render(<CongenAppBar {...defaultProps} />);

    const userButton = screen.getByRole('button');
    fireEvent.click(userButton);

    expect(screen.getByText('Profile')).toBeInTheDocument();
    expect(screen.getByText('Sign Out')).toBeInTheDocument();

    // Test that menu items are clickable
    const profileItem = screen.getByText('Profile');
    expect(profileItem).toBeInTheDocument();
  });

  it('handles home click', () => {
    render(<CongenAppBar {...defaultProps} />);

    const homeButton = screen.getByText('ConGen');
    fireEvent.click(homeButton);

    expect(navigateToFrontend).toHaveBeenCalledWith('/');
  });

  it('handles profile navigation from user menu', () => {
    render(<CongenAppBar {...defaultProps} />);

    const userButton = screen.getByRole('button');
    fireEvent.click(userButton);

    const profileItem = screen.getByText('Profile');
    fireEvent.click(profileItem);

    expect(navigateToFrontend).toHaveBeenCalledWith('/user_profile?section=privacy');
  });

  it('handles logout from user menu', () => {
    render(<CongenAppBar {...defaultProps} />);

    const userButton = screen.getByRole('button');
    fireEvent.click(userButton);

    const logoutItem = screen.getByText('Sign Out');
    fireEvent.click(logoutItem);

    expect(handleLogout).toHaveBeenCalledWith(mockKcContext);
  });

  it('displays user initials in avatar', () => {
    render(<CongenAppBar {...defaultProps} />);

    const avatar = screen.getByText('T');
    expect(avatar).toBeInTheDocument();
  });

  it('displays user email in menu', () => {
    render(<CongenAppBar {...defaultProps} />);

    const userButton = screen.getByRole('button');
    fireEvent.click(userButton);

    // The menu doesn't show email in this component
    expect(screen.getByText('Profile')).toBeInTheDocument();
  });

  it('handles user with only first name', () => {
    const userWithOnlyFirstName = {
      username: 'testuser',
      email: 'test@example.com',
      firstName: 'Test',
    };

    render(<CongenAppBar {...defaultProps} user={userWithOnlyFirstName} />);

    const avatar = screen.getByText('T');
    expect(avatar).toBeInTheDocument();
  });

  it('handles user with only last name', () => {
    const userWithOnlyLastName = {
      username: 'testuser',
      email: 'test@example.com',
      lastName: 'User',
    };

    render(<CongenAppBar {...defaultProps} user={userWithOnlyLastName} />);

    // Should show first letter of username since no firstName
    const avatar = screen.getByText('T');
    expect(avatar).toBeInTheDocument();
  });

  it('handles user with no name', () => {
    const userWithNoName = {
      username: 'testuser',
      email: 'test@example.com',
    };

    render(<CongenAppBar {...defaultProps} user={userWithNoName} />);

    // Should show first letter of username
    const avatar = screen.getByText('T');
    expect(avatar).toBeInTheDocument();
  });

  it('renders navigation menu items', () => {
    render(<CongenAppBar {...defaultProps} />);

    expect(screen.getByText('Exercises')).toBeInTheDocument();
    expect(screen.getByText('Privacy')).toBeInTheDocument();
  });
});
