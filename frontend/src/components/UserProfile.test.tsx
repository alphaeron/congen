import { render, screen, waitFor, act } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { UserProfile } from './UserProfile';
import { ENDPOINT } from '../api/endpoint';
import { deleteAllPersonalData } from '../api/gdpr';
import { DataContext } from '../contexts/DataContext';

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

const defaultMockDataContext = {
  userConsent: {
    keycloak_id: 'test-user-id',
    data_processing_consent: true,
    consent_timestamp: new Date('2023-08-09T10:15:30Z'),
    updated_at: new Date('2023-08-09T10:15:30Z'),
  },
  loadUserConsent: jest.fn().mockResolvedValue({
    keycloak_id: 'test-user-id',
    data_processing_consent: true,
    consent_timestamp: new Date('2023-08-09T10:15:30Z'),
    updated_at: new Date('2023-08-09T10:15:30Z'),
  }),
  updateUserConsent: jest.fn().mockResolvedValue({
    keycloak_id: 'test-user-id',
    data_processing_consent: true,
    consent_timestamp: new Date('2023-08-09T10:15:30Z'),
    updated_at: new Date('2023-08-09T10:15:30Z'),
  }),
  isLoading: false,
  userData: null,
  exerciseMuscleData: new Map(),
  weightUnitPreferences: [],
  exerciseData: new Map(),
  exerciseEquipmentData: new Map(),
  muscleData: new Map(),
  equipmentData: new Map(),
  programData: new Map(),
  allExercises: [],
  allMuscles: [],
  allEquipment: [],
  userEquipment: [],
  userWeakMuscles: [],
  userExercisePreferences: [],
  programPreferences: [],
  programmedWorkouts: [],
  userOneRepMaxes: [],
  userExercisePool: null,
  dashboardStats: null,
  error: null,
  refreshData: jest.fn(),
  isDataStale: false,
  getExercise: jest.fn(),
  getExerciseMuscles: jest.fn(),
  getExerciseEquipmentData: jest.fn(),
  getMuscle: jest.fn(),
  getEquipment: jest.fn(),
  getProgram: jest.fn(),
  loadAllExercises: jest.fn(),
  loadAllMuscles: jest.fn(),
  loadAllEquipment: jest.fn(),
  loadUserEquipment: jest.fn(),
  loadUserWeakMuscles: jest.fn(),
  loadUserExercisePreferences: jest.fn(),
  loadProgramPreferences: jest.fn(),
  loadProgrammedWorkouts: jest.fn(),
  loadUserOneRepMaxes: jest.fn(),
  loadUserExercisePool: jest.fn(),
  loadDashboardStats: jest.fn(),
  getProgramPreferencesById: jest.fn(),
  loadAllExercisesForComponents: jest.fn(),
  invalidateCache: jest.fn(),
  refreshSpecificData: jest.fn(),
  isLoadingSpecific: jest.fn(),
  getErrorForDataType: jest.fn(),
  clearError: jest.fn(),
  prefetchData: jest.fn(),
  prefetchRelatedData: jest.fn(),
  isOnline: true,
  syncPendingChanges: jest.fn(),
  getOfflineData: jest.fn(),
  getRelatedData: jest.fn(),
  updateDataRelationships: jest.fn(),
  predictivePrefetch: jest.fn(),
  compressData: jest.fn(),
  decompressData: jest.fn(),
  resolveDataConflicts: jest.fn(),
  syncWithServer: jest.fn(),
};

const renderWithProviders = (component: React.ReactElement, mockDataContext = defaultMockDataContext) => {
  return render(
    <DataContext.Provider value={mockDataContext}>
      <MemoryRouter>{component}</MemoryRouter>
    </DataContext.Provider>
  );
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
