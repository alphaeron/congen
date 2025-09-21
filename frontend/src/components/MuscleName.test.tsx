import { render, screen, act, waitFor } from '@testing-library/react';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { MuscleName } from './MuscleName';
import type { Muscle } from '../api/types';
import { DataContext } from '../contexts/DataContext';

// Mock the DataContext
const mockGetMuscle = jest.fn();
const defaultMockDataContext = {
  getMuscle: mockGetMuscle,
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
  userConsent: null,
  userExercisePool: null,
  dashboardStats: null,
  isLoading: false,
  error: null,
  refreshData: jest.fn(),
  isDataStale: false,
  getExercise: jest.fn(),
  getExerciseMuscles: jest.fn(),
  getExerciseEquipmentData: jest.fn(),
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
  loadUserConsent: jest.fn(),
  loadUserExercisePool: jest.fn(),
  loadDashboardStats: jest.fn(),
  updateUserConsent: jest.fn(),
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

const renderWithProviders = (component: React.ReactElement) => {
  return render(
    <MemoryRouter>
      <DataContext.Provider value={defaultMockDataContext}>{component}</DataContext.Provider>
    </MemoryRouter>
  );
};

describe('MuscleName', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const mockMuscle: Muscle = {
    name: 'Pectoralis Major',
    description:
      'The pectoralis major is a thick, fan-shaped muscle, situated at the chest of the human body.',
  };

  it('should render muscle name with tooltip when data is loaded', async () => {
    mockGetMuscle.mockResolvedValue(mockMuscle);

    await act(async () => {
      renderWithProviders(<MuscleName muscleName="Pectoralis Major" />);
    });

    await waitFor(() => {
      expect(screen.getByText('Pectoralis Major')).toBeInTheDocument();
    });

    // Check that the element has cursor help style
    const muscleNameElement = screen.getByText('Pectoralis Major');
    expect(muscleNameElement).toHaveStyle('cursor: help');
  });

  it('should show loading tooltip when data is loading', async () => {
    // Mock a slow response
    mockGetMuscle.mockImplementation(() => new Promise(() => {}));

    await act(async () => {
      renderWithProviders(<MuscleName muscleName="Pectoralis Major" />);
    });

    expect(screen.getByText('Pectoralis Major')).toBeInTheDocument();
  });

  it('should show error tooltip when data fails to load', async () => {
    mockGetMuscle.mockRejectedValue(new Error('Failed to load'));

    await act(async () => {
      renderWithProviders(<MuscleName muscleName="Pectoralis Major" />);
    });

    await waitFor(() => {
      expect(screen.getByText('Pectoralis Major')).toBeInTheDocument();
    });
  });

  it('should render with custom variant and sx props', async () => {
    mockGetMuscle.mockResolvedValue(mockMuscle);

    await act(async () => {
      renderWithProviders(
        <MuscleName muscleName="Pectoralis Major" variant="h6" sx={{ fontWeight: 'bold' }} />
      );
    });

    await waitFor(() => {
      const muscleNameElement = screen.getByText('Pectoralis Major');
      expect(muscleNameElement).toHaveStyle('font-weight: 700');
    });
  });

  it('should render custom children when provided', async () => {
    mockGetMuscle.mockResolvedValue(mockMuscle);

    await act(async () => {
      renderWithProviders(
        <MuscleName muscleName="Pectoralis Major">Custom Muscle Name</MuscleName>
      );
    });

    await waitFor(() => {
      expect(screen.getByText('Custom Muscle Name')).toBeInTheDocument();
      expect(screen.queryByText('Pectoralis Major')).not.toBeInTheDocument();
    });
  });

  it('should capitalize muscle name when no custom children provided', async () => {
    mockGetMuscle.mockResolvedValue(mockMuscle);

    await act(async () => {
      renderWithProviders(<MuscleName muscleName="pectoralis major" />);
    });

    await waitFor(() => {
      expect(screen.getByText('Pectoralis Major')).toBeInTheDocument();
    });
  });

  it('should handle muscle without description', async () => {
    const muscleWithoutDescription: Muscle = {
      name: 'Pectoralis Major',
      description: '',
    };

    mockGetMuscle.mockResolvedValue(muscleWithoutDescription);

    await act(async () => {
      renderWithProviders(<MuscleName muscleName="Pectoralis Major" />);
    });

    await waitFor(() => {
      expect(screen.getByText('Pectoralis Major')).toBeInTheDocument();
    });
  });
});
