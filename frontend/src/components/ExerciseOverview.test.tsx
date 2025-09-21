import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor, act } from '@testing-library/react';
import AxiosMockAdapter from 'axios-mock-adapter';
import * as React from 'react';
import { MemoryRouter } from 'react-router';
import { DataContext } from '../contexts/DataContext';

// Mock TanStack Virtual to render all items in tests
jest.mock('@tanstack/react-virtual', () => ({
  useVirtualizer: () => ({
    getVirtualItems: () => [
      { index: 0, key: '0', start: 0, size: 300 },
      { index: 1, key: '1', start: 300, size: 300 },
    ],
    getTotalSize: () => 600,
  }),
}));

// Mock the auth context
jest.mock('../contexts/AuthContext', () => ({
  useAuth: () => ({
    user: {
      keycloak_id: 'test-user-id',
      name: 'Test User',
    },
  }),
}));

// Mock DataContext
const mockUseData = jest.fn();
jest.mock('../contexts/DataContext', () => ({
  useData: () => mockUseData(),
  DataProvider: ({ children }: { children: React.ReactNode }) => children,
  DataContext: {
    Provider: ({ children }: { children: React.ReactNode }) => children,
  },
}));

import { ExerciseOverview } from './ExerciseOverview';
import {
  EXERCISE,
  EXERCISE_MUSCLE,
  EXERCISE_EQUIPMENT,
  MUSCLE,
  EQUIPMENT,
} from '../__mocks__/data';
import { ENDPOINT } from '../api/endpoint';

describe('ExerciseOverview component', () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

  const mockAdapter = new AxiosMockAdapter(ENDPOINT);

  beforeEach(async () => {
    mockAdapter.onGet('/equipment/').reply(200, [EQUIPMENT]);
    mockAdapter.onGet('/exercise/').reply(200, [EXERCISE]);
    mockAdapter.onGet('/exercise_equipment/').reply(200, [EXERCISE_EQUIPMENT]);
    mockAdapter.onGet('/exercise_muscle/').reply(200, [EXERCISE_MUSCLE]);
    mockAdapter.onGet('/muscle/').reply(200, [MUSCLE]);

    // Set up default mock data for DataContext
    const defaultMockDataContext = {
      userData: null,
      exerciseMuscleData: new Map([['Bench Press', ['chest', 'triceps']]]),
      weightUnitPreferences: [],
      exerciseData: new Map(),
      exerciseEquipmentData: new Map([['Bench Press', [{ equipment_name: 'barbell' }]]]),
      muscleData: new Map(),
      equipmentData: new Map(),
      programData: new Map(),
      allExercises: [EXERCISE],
      allMuscles: [MUSCLE],
      allEquipment: [EQUIPMENT],
      loadAllExercises: jest.fn().mockResolvedValue([EXERCISE]),
      loadAllEquipment: jest.fn().mockResolvedValue([EQUIPMENT]),
      loadAllMuscles: jest.fn().mockResolvedValue([MUSCLE]),
      isLoading: false,
      error: null,
      refreshData: jest.fn(),
      isDataStale: false,
      getExercise: jest.fn(),
      getExerciseMuscles: jest.fn(),
      getExerciseEquipmentData: jest.fn(),
      getMuscle: jest.fn(),
      getEquipment: jest.fn(),
      getProgram: jest.fn(),
    };
    
    mockUseData.mockReturnValue(defaultMockDataContext);

    await act(async () => {
      render(
        <MemoryRouter>
          <QueryClientProvider client={queryClient}>
            <DataContext.Provider value={defaultMockDataContext}>
              <ExerciseOverview />
            </DataContext.Provider>
          </QueryClientProvider>
        </MemoryRouter>
      );
    });
  });

  afterEach(() => {
    mockAdapter.reset();
  });

  it('Renders the exercise header', async () => {
    // Wait for the data to load and the header to be rendered
    await waitFor(
      () => {
        const exerciseElement = screen.getByTestId('exerciseHeader');
        expect(exerciseElement).toBeInTheDocument();
        expect(exerciseElement).toHaveTextContent('Exercise Library');
      },
      { timeout: 10000 }
    );

    // The component now uses DataContext, so it doesn't make direct API calls
    expect(mockAdapter.history.get.length).toBe(0);
  }, 15000);
});

