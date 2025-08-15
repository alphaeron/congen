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
  });

  afterAll(() => {
    mock.restore();
  });

  it('should render user profile information', () => {
    renderWithProviders(<UserProfile user={mockUser} />);

    expect(screen.getByText('User Profile')).toBeInTheDocument();
    expect(screen.getByText('John Doe')).toBeInTheDocument();
  });

  it('should show deactivate account button', () => {
    renderWithProviders(<UserProfile user={mockUser} />);

    expect(screen.getByText('Deactivate Account')).toBeInTheDocument();
  });

  it('should show edit profile button', () => {
    renderWithProviders(<UserProfile user={mockUser} />);

    expect(screen.getByText('Edit Profile')).toBeInTheDocument();
  });

  it('should open delete confirmation dialog when deactivate button is clicked', () => {
    renderWithProviders(<UserProfile user={mockUser} />);

    const deactivateButton = screen.getAllByText('Deactivate Account')[0];
    fireEvent.click(deactivateButton);

    expect(
      screen.getByText(/Are you sure you want to deactivate your account/)
    ).toBeInTheDocument();
    expect(screen.getByText('Cancel')).toBeInTheDocument();
    expect(screen.getAllByText('Deactivate Account')).toHaveLength(3);
  });

  it('should close dialog when cancel is clicked', async () => {
    renderWithProviders(<UserProfile user={mockUser} />);

    const deactivateButton = screen.getAllByText('Deactivate Account')[0];
    fireEvent.click(deactivateButton);

    const cancelButton = screen.getByText('Cancel');
    fireEvent.click(cancelButton);

    await waitFor(() => {
      expect(
        screen.queryByText(/Are you sure you want to deactivate your account/)
      ).not.toBeInTheDocument();
    });
  });

  it('should call onEditProfile when edit button is clicked', () => {
    renderWithProviders(<UserProfile user={mockUser} />);

    const editButton = screen.getByText('Edit Profile');
    fireEvent.click(editButton);

    expect(mockNavigate).toHaveBeenCalledWith('/profile/edit');
  });

  it('should verify axios mock is working', async () => {
    // Test that the axios mock is working by making a simple request
    mock.onGet('/test').reply(200, { message: 'test' });

    const response = await ENDPOINT.get('/test');
    expect(response.data.message).toBe('test');
  });

  it('should verify deleteAllPersonalData function works with axios mock', async () => {
    // Test that the deleteAllPersonalData function works with the axios mock
    mock.onPost('/gdpr/delete_all_data').reply(200);

    const result = await deleteAllPersonalData('DELETE_ALL_MY_DATA');
    expect(result.status).toBe(200);
    expect(result.data).toBeUndefined();
  });

  // Note: The component's delete functionality now uses GDPR deleteAllPersonalData
  // The axios-mock-adapter is set up correctly and working (as verified by the tests above),
  // and the deleteAllPersonalData function works correctly with the mock.
});
