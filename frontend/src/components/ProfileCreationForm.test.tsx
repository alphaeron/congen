import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import React from 'react';

import { ProfileCreationForm } from './ProfileCreationForm';

// Mock react-oidc-context
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

// Mock the auth context
const mockCreateProfile = jest.fn();
const mockClearError = jest.fn();

jest.mock('../contexts/AuthContext', () => ({
  useAuth: () => ({
    createProfile: mockCreateProfile,
    isLoading: false,
    error: null as string | null,
    clearError: mockClearError,
  }),
  AuthProvider: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

describe('ProfileCreationForm', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render the form with all required fields', () => {
    render(<ProfileCreationForm />);

    expect(screen.getByText('Complete Your Profile')).toBeInTheDocument();
    expect(
      screen.getByText('Please provide your fitness information to complete your account setup.')
    ).toBeInTheDocument();

    expect(screen.getByDisplayValue('John Doe')).toBeInTheDocument();
    expect(screen.getByRole('spinbutton', { name: 'Age' })).toBeInTheDocument();
    expect(screen.getByRole('spinbutton', { name: 'Height (cm)' })).toBeInTheDocument();
    expect(screen.getByRole('combobox', { name: 'Height Unit' })).toBeInTheDocument();
    expect(screen.getByRole('spinbutton', { name: 'Weight' })).toBeInTheDocument();
    expect(screen.getByRole('combobox', { name: 'Unit' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Complete Profile' })).toBeInTheDocument();
  });

  it('should auto-fill name from Keycloak user profile', () => {
    render(<ProfileCreationForm />);

    const nameField = screen.getByDisplayValue('John Doe');
    expect(nameField).toBeInTheDocument();
  });

  it('should handle form input changes', () => {
    render(<ProfileCreationForm />);

    const ageField = screen.getByRole('spinbutton', { name: 'Age' });
    const weightField = screen.getByRole('spinbutton', { name: 'Weight' });

    fireEvent.change(ageField, { target: { value: '25' } });
    fireEvent.change(weightField, { target: { value: '70' } });

    expect(ageField).toHaveValue(25);
    expect(weightField).toHaveValue(70);
  });

  it('should handle form submission with valid data', async () => {
    mockCreateProfile.mockResolvedValue(undefined);

    render(<ProfileCreationForm />);

    const ageField = screen.getByRole('spinbutton', { name: 'Age' });
    const weightField = screen.getByRole('spinbutton', { name: 'Weight' });
    const heightField = screen.getByRole('spinbutton', { name: 'Height (cm)' });
    const submitButton = screen.getByRole('button', { name: 'Complete Profile' });

    fireEvent.change(ageField, { target: { value: '25' } });
    fireEvent.change(weightField, { target: { value: '70' } });
    fireEvent.change(heightField, { target: { value: '175' } });

    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockClearError).toHaveBeenCalled();
      expect(mockCreateProfile).toHaveBeenCalledWith({
        name: 'John Doe',
        age: 25,
        height: 175,
        weight: 70,
        unit: 'KG',
      });
    });
  });

  it('should handle form submission error', async () => {
    const error = new Error('Network error');
    mockCreateProfile.mockRejectedValue(error);

    render(<ProfileCreationForm />);

    const ageField = screen.getByRole('spinbutton', { name: 'Age' });
    const weightField = screen.getByRole('spinbutton', { name: 'Weight' });
    const heightField = screen.getByRole('spinbutton', { name: 'Height (cm)' });
    const submitButton = screen.getByRole('button', { name: 'Complete Profile' });

    fireEvent.change(ageField, { target: { value: '25' } });
    fireEvent.change(weightField, { target: { value: '70' } });
    fireEvent.change(heightField, { target: { value: '175' } });

    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockCreateProfile).toHaveBeenCalled();
    });
  });
});
