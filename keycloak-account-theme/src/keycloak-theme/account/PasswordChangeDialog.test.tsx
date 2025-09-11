import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import React from 'react';

import PasswordChangeDialog from './PasswordChangeDialog';

// Mock notistack
const mockEnqueueSnackbar = jest.fn();
jest.mock('notistack', () => ({
  useSnackbar: () => ({ enqueueSnackbar: mockEnqueueSnackbar }),
}));

// Mock the API client
const mockChangePassword = jest.fn();
jest.mock('./api/client', () => ({
  createApiClient: jest.fn(() => ({
    changePassword: mockChangePassword,
  })),
}));

// Mock LoadingSpinner
jest.mock('../../components/LoadingSpinner', () => ({
  LoadingSpinner: ({ message }: { message: string }) => <div data-testid="loading-spinner">{message}</div>,
}));

const mockKcContext = {
  themeType: 'account' as const,
  themeName: 'test-theme',
  properties: {},
};

describe('PasswordChangeDialog', () => {
  const defaultProps = {
    open: true,
    onClose: jest.fn(),
    kcContext: mockKcContext,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders dialog when open', () => {
    render(<PasswordChangeDialog {...defaultProps} />);

    expect(screen.getByRole('heading', { name: 'Change Password' })).toBeInTheDocument();
    expect(screen.getAllByDisplayValue('')).toHaveLength(3); // Three password fields are empty by default
  });

  it('does not render when closed', () => {
    render(<PasswordChangeDialog {...defaultProps} open={false} />);

    expect(screen.queryByText('Change Password')).not.toBeInTheDocument();
  });

  it('handles input changes', () => {
    render(<PasswordChangeDialog {...defaultProps} />);

    const passwordInputs = screen.getAllByDisplayValue('');
    const currentPasswordInput = passwordInputs[0];
    const newPasswordInput = passwordInputs[1];
    const confirmPasswordInput = passwordInputs[2];

    fireEvent.change(currentPasswordInput, { target: { value: 'current123' } });
    fireEvent.change(newPasswordInput, { target: { value: 'newpassword123' } });
    fireEvent.change(confirmPasswordInput, { target: { value: 'newpassword123' } });

    expect(currentPasswordInput).toHaveValue('current123');
    expect(newPasswordInput).toHaveValue('newpassword123');
    expect(confirmPasswordInput).toHaveValue('newpassword123');
  });

  it('validates required fields', async () => {
    render(<PasswordChangeDialog {...defaultProps} />);

    const changeButton = screen.getByRole('button', { name: 'Change Password' });
    fireEvent.click(changeButton);

    await waitFor(() => {
      expect(mockEnqueueSnackbar).toHaveBeenCalledWith('Current password is required', { variant: 'error' });
    });
  });

  it('validates password length', async () => {
    render(<PasswordChangeDialog {...defaultProps} />);

    const passwordInputs = screen.getAllByDisplayValue('');
    const currentPasswordInput = passwordInputs[0];
    const newPasswordInput = passwordInputs[1];
    const confirmPasswordInput = passwordInputs[2];

    fireEvent.change(currentPasswordInput, { target: { value: 'current123' } });
    fireEvent.change(newPasswordInput, { target: { value: 'short' } });
    fireEvent.change(confirmPasswordInput, { target: { value: 'short' } });

    const changeButton = screen.getByRole('button', { name: 'Change Password' });
    fireEvent.click(changeButton);

    await waitFor(() => {
      expect(mockEnqueueSnackbar).toHaveBeenCalledWith('New password must be at least 8 characters long', { variant: 'error' });
    });
  });

  it('validates password confirmation', async () => {
    render(<PasswordChangeDialog {...defaultProps} />);

    const passwordInputs = screen.getAllByDisplayValue('');
    const currentPasswordInput = passwordInputs[0];
    const newPasswordInput = passwordInputs[1];
    const confirmPasswordInput = passwordInputs[2];

    fireEvent.change(currentPasswordInput, { target: { value: 'current123' } });
    fireEvent.change(newPasswordInput, { target: { value: 'newpassword123' } });
    fireEvent.change(confirmPasswordInput, { target: { value: 'different123' } });

    const changeButton = screen.getByRole('button', { name: 'Change Password' });
    fireEvent.click(changeButton);

    await waitFor(() => {
      expect(mockEnqueueSnackbar).toHaveBeenCalledWith('New passwords do not match', { variant: 'error' });
    });
  });

  it('handles successful password change', async () => {
    mockChangePassword.mockResolvedValue({ success: true });

    render(<PasswordChangeDialog {...defaultProps} />);

    const passwordInputs = screen.getAllByDisplayValue('');
    const currentPasswordInput = passwordInputs[0];
    const newPasswordInput = passwordInputs[1];
    const confirmPasswordInput = passwordInputs[2];

    fireEvent.change(currentPasswordInput, { target: { value: 'current123' } });
    fireEvent.change(newPasswordInput, { target: { value: 'newpassword123' } });
    fireEvent.change(confirmPasswordInput, { target: { value: 'newpassword123' } });

    const changeButton = screen.getByRole('button', { name: 'Change Password' });
    fireEvent.click(changeButton);

    await waitFor(() => {
      expect(mockChangePassword).toHaveBeenCalledWith({
        currentPassword: 'current123',
        newPassword: 'newpassword123',
        confirmPassword: 'newpassword123',
      });
    });

    await waitFor(() => {
      expect(mockEnqueueSnackbar).toHaveBeenCalledWith('Password changed successfully!', { variant: 'success' });
    });

    expect(defaultProps.onClose).toHaveBeenCalled();
  });

  it('handles password change error', async () => {
    mockChangePassword.mockResolvedValue({ success: false, error: 'Invalid current password' });

    render(<PasswordChangeDialog {...defaultProps} />);

    const passwordInputs = screen.getAllByDisplayValue('');
    const currentPasswordInput = passwordInputs[0];
    const newPasswordInput = passwordInputs[1];
    const confirmPasswordInput = passwordInputs[2];

    fireEvent.change(currentPasswordInput, { target: { value: 'current123' } });
    fireEvent.change(newPasswordInput, { target: { value: 'newpassword123' } });
    fireEvent.change(confirmPasswordInput, { target: { value: 'newpassword123' } });

    const changeButton = screen.getByRole('button', { name: 'Change Password' });
    fireEvent.click(changeButton);

    await waitFor(() => {
      expect(mockEnqueueSnackbar).toHaveBeenCalledWith('Failed to change password', { variant: 'error' });
    });
  });

  it('handles API exceptions', async () => {
    mockChangePassword.mockRejectedValue(new Error('Network error'));

    render(<PasswordChangeDialog {...defaultProps} />);

    const passwordInputs = screen.getAllByDisplayValue('');
    const currentPasswordInput = passwordInputs[0];
    const newPasswordInput = passwordInputs[1];
    const confirmPasswordInput = passwordInputs[2];

    fireEvent.change(currentPasswordInput, { target: { value: 'current123' } });
    fireEvent.change(newPasswordInput, { target: { value: 'newpassword123' } });
    fireEvent.change(confirmPasswordInput, { target: { value: 'newpassword123' } });

    const changeButton = screen.getByRole('button', { name: 'Change Password' });
    fireEvent.click(changeButton);

    await waitFor(() => {
      expect(mockEnqueueSnackbar).toHaveBeenCalledWith('Failed to change password', { variant: 'error' });
    });
  });

  it('calls onClose when cancel button is clicked', () => {
    render(<PasswordChangeDialog {...defaultProps} />);

    const cancelButton = screen.getByText('Cancel');
    fireEvent.click(cancelButton);

    expect(defaultProps.onClose).toHaveBeenCalled();
  });

  it('shows loading state during password change', async () => {
    mockChangePassword.mockImplementation(() => new Promise(resolve => setTimeout(() => resolve({ success: true }), 100)));

    render(<PasswordChangeDialog {...defaultProps} />);

    const passwordInputs = screen.getAllByDisplayValue('');
    const currentPasswordInput = passwordInputs[0];
    const newPasswordInput = passwordInputs[1];
    const confirmPasswordInput = passwordInputs[2];

    fireEvent.change(currentPasswordInput, { target: { value: 'current123' } });
    fireEvent.change(newPasswordInput, { target: { value: 'newpassword123' } });
    fireEvent.change(confirmPasswordInput, { target: { value: 'newpassword123' } });

    const changeButton = screen.getByRole('button', { name: 'Change Password' });
    fireEvent.click(changeButton);

    expect(screen.getByTestId('loading-spinner')).toBeInTheDocument();
    expect(screen.getByText('Changing...')).toBeInTheDocument();
  });
});
