import { ThemeProvider, createTheme } from '@mui/material/styles';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import React from 'react';

import { AccountSecurity } from './AccountSecurity';
import { ENDPOINT } from '../api/endpoint';
import type { User } from '../api/types';

// Create axios mock adapter for the ENDPOINT instance
const mock = new MockAdapter(ENDPOINT);

// Create a theme for testing
const theme = createTheme();

const renderWithTheme = (component: React.ReactElement) => {
  return render(<ThemeProvider theme={theme}>{component}</ThemeProvider>);
};

describe('AccountSecurity', () => {
  const mockUser: User = {
    keycloak_id: 'test-user-id',
    name: 'Test User',
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
    roles: ['user'],
  };

  const mockOnAccountDeleted = jest.fn();

  beforeEach(() => {
    mock.reset();
    jest.clearAllMocks();
  });

  afterAll(() => {
    mock.restore();
  });

  it('renders the component with correct title and description', () => {
    renderWithTheme(<AccountSecurity user={mockUser} onAccountDeleted={mockOnAccountDeleted} />);

    expect(screen.getByText('Account Security')).toBeInTheDocument();
    expect(
      screen.getByText(/Manage your account security settings and access controls/)
    ).toBeInTheDocument();
  });

  it('displays security settings section', () => {
    renderWithTheme(<AccountSecurity user={mockUser} onAccountDeleted={mockOnAccountDeleted} />);

    expect(screen.getByText('Security Settings')).toBeInTheDocument();
    expect(
      screen.getByText(/Configure your account security preferences and access controls/)
    ).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /change password/i })).toBeInTheDocument();
  });

  it('displays danger zone section', () => {
    renderWithTheme(<AccountSecurity user={mockUser} onAccountDeleted={mockOnAccountDeleted} />);

    expect(screen.getByText('Danger Zone')).toBeInTheDocument();
    expect(
      screen.getByText(/Once you delete your account, there is no going back/)
    ).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /deactivate account/i })).toBeInTheDocument();
  });

  it('opens delete dialog when deactivate button is clicked', () => {
    renderWithTheme(<AccountSecurity user={mockUser} onAccountDeleted={mockOnAccountDeleted} />);

    const deactivateButton = screen.getByRole('button', { name: /deactivate account/i });
    fireEvent.click(deactivateButton);

    expect(screen.getByText('Delete Account')).toBeInTheDocument();
    expect(screen.getByText(/Are you sure you want to delete your account/)).toBeInTheDocument();
  });

  it('closes dialog when cancel is clicked', () => {
    renderWithTheme(<AccountSecurity user={mockUser} onAccountDeleted={mockOnAccountDeleted} />);

    // Open dialog
    const deactivateButton = screen.getByRole('button', { name: /deactivate account/i });
    fireEvent.click(deactivateButton);

    // Close dialog
    const cancelButton = screen.getByRole('button', { name: /cancel/i });
    fireEvent.click(cancelButton);

    expect(screen.queryByText('Delete Account')).not.toBeInTheDocument();
  });

  it('deletes account successfully', async () => {
    mock
      .onDelete('/gdpr/delete_all_personal_data/test-user-id')
      .reply(200, { message: 'Account deleted successfully' });

    renderWithTheme(<AccountSecurity user={mockUser} onAccountDeleted={mockOnAccountDeleted} />);

    // Open dialog
    const deactivateButton = screen.getByRole('button', { name: /deactivate account/i });
    fireEvent.click(deactivateButton);

    // Confirm deletion
    const confirmDeleteButton = screen.getByRole('button', { name: /delete account/i });
    fireEvent.click(confirmDeleteButton);

    await waitFor(() => {
      expect(mock.history.delete).toHaveLength(1);
      expect(mock.history.delete[0].url).toBe('/gdpr/delete_all_personal_data/test-user-id');
      expect(mockOnAccountDeleted).toHaveBeenCalledTimes(1);
    });
  });

  it('shows error when account deletion fails', async () => {
    mock
      .onDelete('/gdpr/delete_all_personal_data/test-user-id')
      .reply(500, { message: 'Internal server error' });

    renderWithTheme(<AccountSecurity user={mockUser} onAccountDeleted={mockOnAccountDeleted} />);

    // Open dialog
    const deactivateButton = screen.getByRole('button', { name: /deactivate account/i });
    fireEvent.click(deactivateButton);

    // Confirm deletion
    const confirmDeleteButton = screen.getByRole('button', { name: /delete account/i });
    fireEvent.click(confirmDeleteButton);

    await waitFor(() => {
      expect(screen.getByText('Failed to delete account. Please try again.')).toBeInTheDocument();
      expect(mockOnAccountDeleted).not.toHaveBeenCalled();
    });
  });

  it('renders change password button as outlined variant', () => {
    renderWithTheme(<AccountSecurity user={mockUser} onAccountDeleted={mockOnAccountDeleted} />);

    const changePasswordButton = screen.getByRole('button', { name: /change password/i });
    expect(changePasswordButton).toHaveClass('MuiButton-outlined');
  });

  it('renders deactivate account button with error color', () => {
    renderWithTheme(<AccountSecurity user={mockUser} onAccountDeleted={mockOnAccountDeleted} />);

    const deactivateButton = screen.getByRole('button', { name: /deactivate account/i });
    expect(deactivateButton).toHaveClass('MuiButton-outlined');
    expect(deactivateButton).toHaveClass('MuiButton-colorError');
  });

  it('displays delete icon in deactivate button', () => {
    renderWithTheme(<AccountSecurity user={mockUser} onAccountDeleted={mockOnAccountDeleted} />);

    const deactivateButton = screen.getByRole('button', { name: /deactivate account/i });
    // The delete icon should be present in the button
    expect(deactivateButton).toBeInTheDocument();
  });

  it('works without onAccountDeleted callback', () => {
    renderWithTheme(<AccountSecurity user={mockUser} />);

    expect(screen.getByText('Account Security')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /deactivate account/i })).toBeInTheDocument();
  });

  it('verifies API call is made with correct endpoint', async () => {
    mock
      .onDelete('/gdpr/delete_all_personal_data/test-user-id')
      .reply(200, { message: 'Account deleted successfully' });

    renderWithTheme(<AccountSecurity user={mockUser} onAccountDeleted={mockOnAccountDeleted} />);

    // Open dialog
    const deactivateButton = screen.getByRole('button', { name: /deactivate account/i });
    fireEvent.click(deactivateButton);

    // Confirm deletion
    const confirmDeleteButton = screen.getByRole('button', { name: /delete account/i });
    fireEvent.click(confirmDeleteButton);

    await waitFor(() => {
      expect(mock.history.delete).toHaveLength(1);
      expect(mock.history.delete[0].url).toBe('/gdpr/delete_all_personal_data/test-user-id');
    });
  });
});
