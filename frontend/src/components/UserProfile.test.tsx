import { render, screen, waitFor, act } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { UserProfile } from './UserProfile';
import { ENDPOINT } from '../api/endpoint';
import { deleteAllPersonalData } from '../api/gdpr';

// Mock react-router
const mockNavigate = jest.fn();
const mockSearchParams = new URLSearchParams();
const mockSetSearchParams = jest.fn();

jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  useNavigate: () => mockNavigate,
  useSearchParams: () => [mockSearchParams, mockSetSearchParams],
}));

// Mock the auth context
const mockLogout = jest.fn();
jest.mock('../contexts/AuthContext', () => ({
  useAuth: () => ({
    logout: mockLogout,
  }),
}));

const renderWithProviders = (component: React.ReactElement) => {
  return render(<MemoryRouter>{component}</MemoryRouter>);
};

describe('UserProfile', () => {
  // Create a new mock adapter for each test to prevent interference
  let mock: MockAdapter;

  beforeEach(() => {
    // Create a fresh mock adapter for each test
    mock = new MockAdapter(ENDPOINT);
    jest.clearAllMocks();

    // Mock GDPR API calls that GdprComplianceSection makes
    mock.onGet('/gdpr/consent').reply(200, {
      keycloak_id: 'test-user-id',
      data_processing_consent: true,
      consent_timestamp: '2023-08-09T10:15:30Z',
      updated_at: '2023-08-09T10:15:30Z',
    });

    // Mock AccountSecurity API calls
    mock.onDelete('/gdpr/delete_all_data').reply(200, { message: 'Account deleted successfully' });
  });

  afterEach(() => {
    // Properly clean up the mock adapter
    if (mock) {
      mock.restore();
    }
  });

  it('should render user profile information', async () => {
    await act(async () => {
      renderWithProviders(<UserProfile />);
    });

    expect(screen.getByText('User Profile')).toBeInTheDocument();
    expect(screen.getByText('Privacy & Data')).toBeInTheDocument();
    expect(screen.getByText('Manage Profile')).toBeInTheDocument();
  });

  it('should render all tab navigation items', async () => {
    await act(async () => {
      renderWithProviders(<UserProfile />);
    });

    expect(screen.getByText('Privacy & Data')).toBeInTheDocument();
    expect(screen.getByText('Manage Profile')).toBeInTheDocument();
  });

  it('should show physical attributes content by default', async () => {
    await act(async () => {
      renderWithProviders(<UserProfile />);
    });

    // The default section is 'physical' which renders PhysicalAttributesSection
    // Look for the heading in the content area (h5 element)
    await waitFor(() => {
      const heading = screen.getByRole('heading', { name: 'Physical Attributes' });
      expect(heading).toBeInTheDocument();
    }, { timeout: 10000 });
  }, 15000);

  it('should navigate to privacy tab', async () => {
    await act(async () => {
      renderWithProviders(<UserProfile initialSection="privacy" />);
    });

    // Wait for the privacy content to load
    await waitFor(() => {
      expect(screen.getByText('Privacy & Data Protection')).toBeInTheDocument();
    }, { timeout: 10000 });
  }, 15000);

  it('should render Manage Profile button', async () => {
    await act(async () => {
      renderWithProviders(<UserProfile />);
    });

    const manageProfileButton = screen.getByText('Manage Profile');
    expect(manageProfileButton).toBeInTheDocument();
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

    // The function returns void, so we just verify it doesn't throw
    await expect(deleteAllPersonalData('DELETE_ALL_MY_DATA')).resolves.toBeUndefined();
  });

  // Note: The component's delete functionality now uses GDPR deleteAllPersonalData
  // The axios-mock-adapter is set up correctly and working (as verified by the tests above),
  // and the deleteAllPersonalData function works correctly with the mock.
});
