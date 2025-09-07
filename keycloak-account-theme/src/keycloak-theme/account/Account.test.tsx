import React from 'react';
import { screen } from '@testing-library/react';
import { render, createMockKcContext, createMockUser } from '../../test-utils';
import Account from './Account';

describe('Account', () => {
  const defaultKcContext = createMockKcContext();

  it('renders user profile information correctly', () => {
    render(<Account kcContext={defaultKcContext} i18n={{}} />);

    expect(screen.getByText('Personal Information')).toBeInTheDocument();
    expect(screen.getByText('Test User')).toBeInTheDocument();
    expect(screen.getByText('test@example.com')).toBeInTheDocument();
    expect(screen.getByText('testuser')).toBeInTheDocument();
  });

  it('renders security settings card', () => {
    render(<Account kcContext={defaultKcContext} i18n={{}} />);

    expect(screen.getByText('Security Settings')).toBeInTheDocument();
    expect(screen.getByText('Password')).toBeInTheDocument();
    expect(screen.getByText('Two-Factor Authentication')).toBeInTheDocument();
  });

  it('renders quick actions grid', () => {
    render(<Account kcContext={defaultKcContext} i18n={{}} />);

    expect(screen.getByText('Quick Actions')).toBeInTheDocument();
    expect(screen.getAllByText('Edit Profile')).toHaveLength(2); // One in personal info, one in quick actions
    expect(screen.getByText('Change Password')).toBeInTheDocument();
    expect(screen.getByText('Security Settings')).toBeInTheDocument();
    expect(screen.getByText('Notifications')).toBeInTheDocument();
    expect(screen.getByText('Support')).toBeInTheDocument();
  });

  it('handles missing user information gracefully', () => {
    const kcContextWithoutUser = createMockKcContext({ user: undefined });
    render(<Account kcContext={kcContextWithoutUser} i18n={{}} />);

    expect(screen.getByText('Personal Information')).toBeInTheDocument();
    expect(screen.getByText('Security Settings')).toBeInTheDocument();
    expect(screen.getByText('Quick Actions')).toBeInTheDocument();
  });

  it('handles partial user information', () => {
    const kcContextWithPartialUser = createMockKcContext({
      user: createMockUser({
        firstName: 'John',
        lastName: undefined,
        email: undefined,
      }),
    });
    render(<Account kcContext={kcContextWithPartialUser} i18n={{}} />);

    expect(screen.getByText('John')).toBeInTheDocument();
    expect(screen.getByText('testuser')).toBeInTheDocument();
  });

  it('applies correct Material-UI styling', () => {
    render(<Account kcContext={defaultKcContext} i18n={{}} />);

    // Check for Material-UI components
    const cards = document.querySelectorAll('.MuiCard-root');
    expect(cards).toHaveLength(3); // Personal Info, Security, Quick Actions

    // Check for Grid layout
    const gridContainers = document.querySelectorAll('.MuiGrid-container');
    expect(gridContainers.length).toBeGreaterThan(0);
  });

  it('renders with dark theme', () => {
    render(<Account kcContext={defaultKcContext} i18n={{}} />, {
      theme: 'dark',
    });

    expect(screen.getByText('Personal Information')).toBeInTheDocument();
    expect(screen.getByText('Security Settings')).toBeInTheDocument();
    expect(screen.getByText('Quick Actions')).toBeInTheDocument();
  });

  it('displays correct user avatar with icon', () => {
    render(<Account kcContext={defaultKcContext} i18n={{}} />);

    // Check for avatar with AccountCircle icon
    const avatar = document.querySelector('.MuiAvatar-root');
    expect(avatar).toBeInTheDocument();

    // Check for AccountCircle icon
    const accountIcon = document.querySelector('[data-testid="AccountCircleIcon"]');
    expect(accountIcon).toBeInTheDocument();
  });

  it('handles long user names correctly', () => {
    const kcContextWithLongName = createMockKcContext({
      user: createMockUser({
        firstName: 'VeryLongFirstName',
        lastName: 'VeryLongLastName',
      }),
    });
    render(<Account kcContext={kcContextWithLongName} i18n={{}} />);

    expect(screen.getByText('VeryLongFirstName VeryLongLastName')).toBeInTheDocument();

    // Check for avatar with AccountCircle icon (not initials)
    const accountIcon = document.querySelector('[data-testid="AccountCircleIcon"]');
    expect(accountIcon).toBeInTheDocument();
  });

  it('renders all quick action buttons', () => {
    render(<Account kcContext={defaultKcContext} i18n={{}} />);

    const buttons = screen.getAllByRole('button');
    expect(buttons).toHaveLength(6); // Edit Profile (2), Security Settings, Edit Profile, Change Password, Notifications, Support
  });

  it('maintains responsive design structure', () => {
    render(<Account kcContext={defaultKcContext} i18n={{}} />);

    // Check that Grid components have proper size props
    const gridContainers = document.querySelectorAll('.MuiGrid-container');
    expect(gridContainers.length).toBeGreaterThan(0);

    // Check for Grid items with size props
    const gridItems = document.querySelectorAll('[class*="MuiGrid-grid-"]');
    expect(gridItems.length).toBeGreaterThan(0);
  });
});
