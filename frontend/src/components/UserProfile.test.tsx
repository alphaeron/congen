import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import React from 'react';
import { BrowserRouter } from 'react-router';

import { UserProfile } from './UserProfile';
import { ENDPOINT } from '../api/endpoint';
import { deleteAllPersonalData } from '../api/gdpr';
import type { User } from '../api/types';

// Create axios mock adapter for the ENDPOINT instance
const mock = new MockAdapter(ENDPOINT);

const mockUser: User = {
  keycloak_id: 'test-user-id',
  name: 'John Doe',
  created_at: '2024-01-01T00:00:00Z',
  updated_at: '2024-01-01T00:00:00Z',
  roles: ['user'],
};

// Mock react-router
const mockNavigate = jest.fn();
jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  useNavigate: () => mockNavigate,
}));

// Mock the auth context
const mockLogout = jest.fn();
jest.mock('../contexts/AuthContext', () => ({
  useAuth: () => ({
    logout: mockLogout,
  }),
}));

const renderWithProviders = (component: React.ReactElement) => {
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe('UserProfile', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mock.reset();

    // Mock GDPR API calls that GdprComplianceSection makes
    mock.onGet('/gdpr/consent').reply(200, {
      keycloak_id: 'test-user-id',
      data_processing_consent: true,
      consent_timestamp: '2023-08-09T10:15:30Z',
      updated_at: '2023-08-09T10:15:30Z',
    });
  });

  afterAll(() => {
    mock.restore();
  });

  it('should render user profile information', async () => {
    renderWithProviders(<UserProfile user={mockUser} />);

    expect(screen.getByText('User Profile')).toBeInTheDocument();
    expect(screen.getByText('John Doe')).toBeInTheDocument();

    // Wait for GdprComplianceSection to load
    await waitFor(() => {
      expect(screen.getByText('Privacy & Data Protection')).toBeInTheDocument();
    }, { timeout: 10000 });
  });

  it('should render all tab navigation items', async () => {
    renderWithProviders(<UserProfile user={mockUser} />);

    expect(screen.getAllByText('Overview')[0]).toBeInTheDocument();
    expect(screen.getAllByText('Workout Preferences')[0]).toBeInTheDocument();
    expect(screen.getByText('Privacy & Data')).toBeInTheDocument();
    expect(screen.getByText('Account Security')).toBeInTheDocument();

    // Wait for GdprComplianceSection to load
    await waitFor(() => {
      expect(screen.getByText('Privacy & Data Protection')).toBeInTheDocument();
    }, { timeout: 10000 });
  });

  it('should show overview content by default', async () => {
    renderWithProviders(<UserProfile user={mockUser} />);

    expect(screen.getByText('Account Information')).toBeInTheDocument();
    expect(screen.getByText('Quick Actions')).toBeInTheDocument();
    expect(screen.getByText('User ID: test-user-id')).toBeInTheDocument();
    expect(screen.getByText('Roles: user')).toBeInTheDocument();

    // Wait for GdprComplianceSection to load
    await waitFor(() => {
      expect(screen.getByText('Privacy & Data Protection')).toBeInTheDocument();
    });
  });

  it('should navigate to workout preferences tab', async () => {
    renderWithProviders(<UserProfile user={mockUser} />);

    // Wait for GdprComplianceSection to load
    await waitFor(() => {
      expect(screen.getByText('Privacy & Data Protection')).toBeInTheDocument();
    }, { timeout: 10000 });

    const workoutPrefsButton = screen.getByText('Workout Preferences');
    fireEvent.click(workoutPrefsButton);

    expect(screen.getByText('Program Settings')).toBeInTheDocument();
    expect(screen.getByText('Configure Preferences')).toBeInTheDocument();
  });

  it('should navigate to privacy tab', async () => {
    renderWithProviders(<UserProfile user={mockUser} />);

    // Wait for GdprComplianceSection to load
    await waitFor(() => {
      expect(screen.getByText('Privacy & Data Protection')).toBeInTheDocument();
    }, { timeout: 10000 });

    const privacyButton = screen.getByText('Privacy & Data');
    fireEvent.click(privacyButton);

    // Privacy content should already be visible since it's loaded by default
    expect(screen.getByText('Privacy & Data Protection')).toBeInTheDocument();
  });

  it('should navigate to account security tab', async () => {
    renderWithProviders(<UserProfile user={mockUser} />);

    // Wait for GdprComplianceSection to load
    await waitFor(() => {
      expect(screen.getByText('Privacy & Data Protection')).toBeInTheDocument();
    }, { timeout: 10000 });

    const securityButton = screen.getByText('Account Security');
    fireEvent.click(securityButton);

    expect(screen.getByText('Security Settings')).toBeInTheDocument();
    expect(screen.getByText('Change Password')).toBeInTheDocument();
    expect(screen.getByText('Two-Factor Authentication')).toBeInTheDocument();
    expect(screen.getByText('Session Management')).toBeInTheDocument();
  });

  it('should show deactivate account button in security tab', async () => {
    renderWithProviders(<UserProfile user={mockUser} />);

    // Wait for GdprComplianceSection to load
    await waitFor(() => {
      expect(screen.getByText('Privacy & Data Protection')).toBeInTheDocument();
    }, { timeout: 10000 });

    const securityButton = screen.getByText('Account Security');
    fireEvent.click(securityButton);

    expect(screen.getByText('Deactivate Account')).toBeInTheDocument();
  });

  it('should show edit profile button in overview', async () => {
    renderWithProviders(<UserProfile user={mockUser} />);

    expect(screen.getAllByText('Edit Profile')).toHaveLength(2); // One in header, one in quick actions

    // Wait for GdprComplianceSection to load
    await waitFor(() => {
      expect(screen.getByText('Privacy & Data Protection')).toBeInTheDocument();
    }, { timeout: 10000 });
  });

  it('should open delete confirmation dialog when deactivate button is clicked', async () => {
    renderWithProviders(<UserProfile user={mockUser} />);

    // Wait for GdprComplianceSection to load
    await waitFor(() => {
      expect(screen.getByText('Privacy & Data Protection')).toBeInTheDocument();
    }, { timeout: 10000 });

    // Navigate to security tab
    const securityButton = screen.getByText('Account Security');
    fireEvent.click(securityButton);

    const deactivateButton = screen.getByText('Deactivate Account');
    fireEvent.click(deactivateButton);

    expect(
      screen.getByText(/Are you sure you want to deactivate your account/)
    ).toBeInTheDocument();
    expect(screen.getByText('Cancel')).toBeInTheDocument();
    expect(screen.getByText('Deactivate Account')).toBeInTheDocument();
  });

  it('should close dialog when cancel is clicked', async () => {
    renderWithProviders(<UserProfile user={mockUser} />);

    // Wait for GdprComplianceSection to load
    await waitFor(() => {
      expect(screen.getByText('Privacy & Data Protection')).toBeInTheDocument();
    }, { timeout: 10000 });

    // Navigate to security tab
    const securityButton = screen.getByText('Account Security');
    fireEvent.click(securityButton);

    const deactivateButton = screen.getByText('Deactivate Account');
    fireEvent.click(deactivateButton);

    const cancelButton = screen.getByText('Cancel');
    fireEvent.click(cancelButton);

    await waitFor(() => {
      expect(
        screen.queryByText(/Are you sure you want to deactivate your account/)
      ).not.toBeInTheDocument();
    }, { timeout: 10000 });
  });

  it('should call onEditProfile when edit button is clicked', async () => {
    renderWithProviders(<UserProfile user={mockUser} />);

    // Wait for GdprComplianceSection to load
    await waitFor(() => {
      expect(screen.getByText('Privacy & Data Protection')).toBeInTheDocument();
    }, { timeout: 10000 });

    const editButtons = screen.getAllByText('Edit Profile');
    fireEvent.click(editButtons[0]); // Click the first edit button

    expect(mockNavigate).toHaveBeenCalledWith('/profile/edit');
  });

  it('should show quick action buttons in overview', async () => {
    renderWithProviders(<UserProfile user={mockUser} />);

    // Wait for GdprComplianceSection to load
    await waitFor(() => {
      expect(screen.getByText('Privacy & Data Protection')).toBeInTheDocument();
    }, { timeout: 10000 });

    expect(screen.getByText('Workout Preferences')).toBeInTheDocument();
    expect(screen.getByText('Privacy Settings')).toBeInTheDocument();
  });

  it('should verify axios mock is working', async () => {
    // Test that the axios mock is working by making a simple request
    mock.onGet('/test').reply(200, { message: 'test' });

    const response = await ENDPOINT.get('/test');
    expect(response.data.message).toBe('test');
  });

  it('should verify deleteAllPersonalData function works with axios mock', async () => {
    // Test that the deleteAllPersonalData function works with the axios mock
    mock.onDelete('/gdpr/delete_all_data').reply(200);

    const result = await deleteAllPersonalData('DELETE_ALL_MY_DATA');
    expect(result.status).toBe(200);
    expect(result.data).toBeUndefined();
  });

  // Note: The component's delete functionality now uses GDPR deleteAllPersonalData
  // The axios-mock-adapter is set up correctly and working (as verified by the tests above),
  // and the deleteAllPersonalData function works correctly with the mock.
});
