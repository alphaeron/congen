import { render, screen, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import MockAdapter from 'axios-mock-adapter';
import { SnackbarProvider } from 'notistack';
import * as React from 'react';
import { MemoryRouter } from 'react-router';

import { GdprComplianceSection } from './GdprComplianceSection';
import { ENDPOINT } from '../api/endpoint';
import type { UserConsent } from '../api/types';
import { DataContext } from '../contexts/DataContext';

describe('GdprComplianceSection', () => {
  // Create a new mock adapter for each test to prevent interference
  let mock: MockAdapter;

  const mockConsentStatus: UserConsent = {
    keycloak_id: 'test-user-123',
    data_processing_consent: true,
    consent_timestamp: new Date('2023-08-09T10:15:30.000Z'),
    updated_at: new Date('2023-08-09T10:15:30.000Z'),
  };

  const defaultMockDataContext = {
    userConsent: mockConsentStatus,
    loadUserConsent: jest.fn().mockResolvedValue(mockConsentStatus),
    updateUserConsent: jest.fn().mockResolvedValue(mockConsentStatus),
    exportUserData: jest.fn().mockResolvedValue({}),
    deleteAllPersonalData: jest.fn().mockResolvedValue(undefined),
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
        <SnackbarProvider>
          <MemoryRouter>{component}</MemoryRouter>
        </SnackbarProvider>
      </DataContext.Provider>
    );
  };

  beforeEach(() => {
    // Create a fresh mock adapter for each test
    mock = new MockAdapter(ENDPOINT);
    jest.clearAllMocks();
  });

  afterEach(() => {
    // Properly clean up the mock adapter
    if (mock) {
      mock.restore();
    }
  });

  it('should render GDPR compliance section with consent status', async () => {
    await act(async () => {
      renderWithProviders(<GdprComplianceSection />);
    });

    // Wait for content to load
    await waitFor(() => {
      expect(screen.getByText('Privacy & Data Protection')).toBeInTheDocument();
    });

    // Check sections
    expect(screen.getByText('Data Processing Consent')).toBeInTheDocument();
    expect(screen.getByText('Your Data Rights')).toBeInTheDocument();
    expect(screen.getByText('Consent Given')).toBeInTheDocument();
    expect(screen.getByText('Withdraw Consent')).toBeInTheDocument();

    // Check GDPR actions
    expect(screen.getByText('Export Your Data')).toBeInTheDocument();
    expect(screen.getByText('Privacy Policy')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Delete All Data' })).toBeInTheDocument();
  });

  it('should handle consent withdrawal', async () => {
    const user = userEvent.setup();

    await act(async () => {
      renderWithProviders(<GdprComplianceSection />);
    });

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Withdraw Consent' })).toBeInTheDocument();
    });

    // Click withdraw consent
    await user.click(screen.getByRole('button', { name: 'Withdraw Consent' }));

    // Check dialog opens
    expect(screen.getByRole('heading', { name: 'Withdraw Consent' })).toBeInTheDocument();
    expect(screen.getByText('I withdraw consent for data processing')).toBeInTheDocument();

    // Select withdraw option and confirm
    await user.click(screen.getByLabelText('I withdraw consent for data processing'));
    await user.click(screen.getByText('Confirm'));

    await waitFor(() => {
      expect(screen.getByText('Consent withdrawn successfully')).toBeInTheDocument();
    });
  });

  it('should handle data deletion with confirmation', async () => {
    const mockDeleteAllPersonalData = jest.fn().mockResolvedValue(undefined);
    const mockDataContext = {
      ...defaultMockDataContext,
      deleteAllPersonalData: mockDeleteAllPersonalData,
    };

    const user = userEvent.setup();

    await act(async () => {
      renderWithProviders(<GdprComplianceSection />, mockDataContext);
    });

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Delete All Data' })).toBeInTheDocument();
    });

    // Click delete all data
    await user.click(screen.getByRole('button', { name: 'Delete All Data' }));

    // Check dialog opens
    expect(screen.getByText('Delete Account and All Data')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('DELETE_ALL_MY_DATA')).toBeInTheDocument();

    // Type confirmation
    await user.type(screen.getByPlaceholderText('DELETE_ALL_MY_DATA'), 'DELETE_ALL_MY_DATA');

    // Confirm deletion
    await user.click(screen.getByRole('button', { name: 'Delete Account' }));

    await waitFor(() => {
      expect(mockDeleteAllPersonalData).toHaveBeenCalledWith('DELETE_ALL_MY_DATA');
    });
  });

  it('should handle consent withdrawn status', async () => {
    const withdrawnConsentStatus: UserConsent = {
      keycloak_id: 'test-user-123',
      data_processing_consent: false,
      consent_timestamp: undefined,
      updated_at: new Date('2023-08-09T10:15:30.000Z'),
    };

    const withdrawnMockDataContext = {
      ...defaultMockDataContext,
      userConsent: withdrawnConsentStatus,
    };

    await act(async () => {
      renderWithProviders(<GdprComplianceSection />, withdrawnMockDataContext);
    });

    await waitFor(() => {
      expect(screen.getByText('Consent Withdrawn')).toBeInTheDocument();
      expect(screen.getByText('Give Consent')).toBeInTheDocument();
    });
  });

  it('should validate delete confirmation text', async () => {

    const user = userEvent.setup();

    await act(async () => {
      renderWithProviders(<GdprComplianceSection />);
    });

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Delete All Data' })).toBeInTheDocument();
    });

    // Click delete all data
    await user.click(screen.getByRole('button', { name: 'Delete All Data' }));

    // Type incorrect confirmation
    await user.type(screen.getByPlaceholderText('DELETE_ALL_MY_DATA'), 'WRONG_TEXT');

    // Delete button should be disabled
    expect(screen.getByRole('button', { name: 'Delete Account' })).toBeDisabled();
    expect(screen.getByText('Please type exactly "DELETE_ALL_MY_DATA"')).toBeInTheDocument();
  });

  it('should have privacy policy link', async () => {

    await act(async () => {
      renderWithProviders(<GdprComplianceSection />);
    });

    await waitFor(() => {
      const privacyPolicyLink = screen.getByText('View Policy').closest('a');
      expect(privacyPolicyLink).toHaveAttribute('href', '/privacy_policy');
    });
  });
});
