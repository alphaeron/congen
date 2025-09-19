import { ThemeProvider, createTheme } from '@mui/material/styles';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, act, waitFor } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import React from 'react';
import { MemoryRouter } from 'react-router';

// Mock react-oidc-context
jest.mock('react-oidc-context', () => ({
  useAuth: () => ({
    user: {
      keycloak_id: 'test-user-id',
      email: 'test@example.com',
      name: 'Test User',
    },
    isAuthenticated: true,
    isLoading: false,
    signinRedirect: jest.fn(),
    signoutRedirect: jest.fn(),
    removeUser: jest.fn(),
  }),
}));

// Mock DataContext
jest.mock('../contexts/DataContext', () => ({
  useData: jest.fn(),
  DataProvider: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}));

// Mock other components
jest.mock('./ExerciseName', () => ({
  ExerciseName: ({ exerciseName }: { exerciseName: string }) => (
    <span>{exerciseName}</span>
  ),
}));

jest.mock('./ExportButtons', () => ({
  ExportButtons: () => <div data-testid="export-buttons">Export Buttons</div>,
}));

jest.mock('./LoadingSpinner', () => ({
  LoadingSpinner: ({ message }: { message: string }) => (
    <div data-testid="loading-spinner">{message}</div>
  ),
}));


jest.mock('./SetSchemeEditor', () => ({
  SetSchemeEditor: () => <div data-testid="set-scheme-editor">Set Scheme Editor</div>,
}));

jest.mock('./RichTextEditor', () => ({
  RichTextEditor: ({ value, onChange }: { value: string; onChange: (value: string) => void }) => (
    <textarea data-testid="rich-text-editor" value={value} onChange={(e) => onChange(e.target.value)} />
  ),
}));

jest.mock('./RichTextDisplay', () => ({
  RichTextDisplay: ({ content }: { content: string }) => (
    <div data-testid="rich-text-display">{content}</div>
  ),
}));

// Remove the module mock - we'll use axios mock adapter instead

jest.mock('../utils/exportUtils', () => ({
  exportWorkoutToPDF: jest.fn(),
}));

// Mock chart components to avoid rendering issues in tests
jest.mock('./ChordChart', () => ({
  ChordChart: ({ data }: { data: unknown }) => (
    <div data-testid="chord-chart">{(data as any)?.length || 0} items</div>
  ),
}));

jest.mock('./SunburstChart', () => ({
  SunburstChart: ({ data }: { data: unknown }) => (
    <div data-testid="sunburst-chart">{(data as any)?.length || 0} items</div>
  ),
}));

// Mock TanStack Virtual to render all items in tests
jest.mock('@tanstack/react-virtual', () => ({
  useVirtualizer: () => ({
    getVirtualItems: () => [
      { index: 0, key: '0', start: 0, size: 60 },
      { index: 1, key: '1', start: 60, size: 60 },
      { index: 2, key: '2', start: 120, size: 60 },
    ],
    getTotalSize: () => 180,
  }),
}));

import { WorkoutDetail } from './WorkoutDetail';
import { ENDPOINT } from '../api/endpoint';
import { AuthProvider } from '../contexts/AuthContext';
import { DataProvider } from '../contexts/DataContext';

const mock = new MockAdapter(ENDPOINT);

const theme = createTheme();

const mockUserDataExport = {
  keycloak_id: 'test-user-id',
  name: 'Test User',
  created_at: '2024-01-01T00:00:00Z',
  updated_at: '2024-01-01T00:00:00Z',
  data_processing_consent: true,
  export_timestamp: '2024-01-01T00:00:00Z',
  user_equipment: [],
  user_exercise_preferences: [],
  user_one_rep_max: [],
  user_weight_unit_preferences: [
    {
      user_id: 'test-user-id',
      exercise_name: 'Bench Press',
      preferred_unit: 'LBS',
    },
  ],
  training_programs: [
    {
      program: {
        id: 1,
        user_id: 'test-user-id',
        name: 'Test Program',
        current_week_number: 1,
        created_at: '2024-01-01T00:00:00Z',
        updated_at: '2024-01-01T00:00:00Z',
        is_active: true,
      },
      program_preferences: {
        program_days_per_week: 3,
      },
      workouts: [
        {
          workout: {
            id: 1,
            program_id: 1,
            day_number: 1,
            name: 'Push Day',
            created_at: '2024-01-01T00:00:00Z',
            updated_at: '2024-01-01T00:00:00Z',
          },
          stages: [
            {
              stage: {
                id: 1,
                programmed_workout_id: 1,
                stage_type_id: 1,
                position: 1,
                name: 'Warm-up',
                created_at: '2024-01-01T00:00:00Z',
                updated_at: '2024-01-01T00:00:00Z',
              },
              exercises: [
                {
                  exercise: {
                    id: 1,
                    workout_stage_id: 1,
                    exercise_name: 'Bench Press',
                    position: 1,
                    notes: 'Focus on form',
                    created_at: '2024-01-01T00:00:00Z',
                    updated_at: '2024-01-01T00:00:00Z',
                  },
                  set_schemes: [
                    {
                      id: 1,
                      programmed_exercise_id: 1,
                      set_number: 1,
                      target_rep_count: 8,
                      target_weight: 135,
                      rest_seconds: 90,
                      is_amrap: false,
                      is_emom: false,
                      use_tempo: false,
                      eccentric_tempo: undefined,
                      isometric_tempo: undefined,
                      concentric_tempo: undefined,
                      created_at: '2024-01-01T00:00:00Z',
                      updated_at: '2024-01-01T00:00:00Z',
                    },
                  ],
                },
              ],
            },
          ],
        },
      ],
    },
  ],
  audit_logs: [],
  data_retention_policies: [],
};

const renderWithTheme = (component: React.ReactElement) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

  return render(
    <MemoryRouter>
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <DataProvider>
            <ThemeProvider theme={theme}>{component}</ThemeProvider>
          </DataProvider>
        </AuthProvider>
      </QueryClientProvider>
    </MemoryRouter>
  );
};

describe('WorkoutDetail', () => {
  const mockOnBack = jest.fn();
  const mockUseData = require('../contexts/DataContext').useData;

  beforeEach(() => {
    mock.reset();
    jest.clearAllMocks();

    // Mock the exercise API calls that ExerciseName component makes
    mock.onGet('/exercise/').reply(200, [
      { id: 1, name: 'Bench Press' },
      { id: 2, name: 'Incline Press' },
    ]);

    // Mock the updateProgrammedExercise API call
    mock.onPatch(/\/programmed_exercise\/\d+/).reply(200, {
      id: 1,
      workout_stage_id: 1,
      exercise_name: 'Bench Press',
      position: 1,
      notes: 'Updated notes',
      created_at: '2024-01-01T00:00:00Z',
      updated_at: '2024-01-01T00:00:00Z',
    });
  });

  describe('Success State', () => {
    beforeEach(() => {
      // Mock the useData hook to return the expected data
      mockUseData.mockReturnValue({
        userData: mockUserDataExport,
        exerciseMuscleData: new Map(),
        weightUnitPreferences: [
          {
            user_id: 'test-user-id',
            exercise_name: 'Bench Press',
            preferred_unit: 'LBS',
          },
        ],
        isLoading: false,
        error: null,
        refreshData: jest.fn(),
        isDataStale: false,
      });

      mock.onGet('/gdpr/export').reply(200, mockUserDataExport);
      mock.onGet('/exercise/').reply(200, []);
      mock.onGet('/exercise_muscle/').reply(200, []);
      mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, [
        {
          user_id: 'test-user-id',
          exercise_name: 'Bench Press',
          preferred_unit: 'LBS',
        },
      ]);
    });

    it('should display workout details when data loads successfully', async () => {
      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      // Since we're mocking useData with isLoading: false, the data should be immediately available
      expect(screen.queryByText('Loading workout details...')).not.toBeInTheDocument();

      expect(screen.getByText('Warm-up')).toBeInTheDocument();
      expect(screen.getByText('Bench Press')).toBeInTheDocument();
      expect(screen.getByText('1')).toBeInTheDocument(); // Number of sets
    });

    it('should display workout information correctly', async () => {
      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      expect(screen.getByText('Warm-up')).toBeInTheDocument();
      expect(screen.getByText('Exercise')).toBeInTheDocument();
      expect(screen.getByText('Sets')).toBeInTheDocument();
    });

    it('should display stage information correctly', async () => {
      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      expect(screen.getByText('Exercise')).toBeInTheDocument();
      expect(screen.getByText('Sets')).toBeInTheDocument();
    });

    it('should display exercise information correctly', async () => {
      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      // Check that the exercise name is displayed
      expect(screen.getByText('Bench Press')).toBeInTheDocument();
      // Check that the notes content is displayed (since we have notes in mock data)
      expect(screen.getByText('Focus on form')).toBeInTheDocument();
    });

    it('should display set scheme information correctly', async () => {
      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      // Weight 135 KG converted to LBS: 135 * 2.20462 = 297.62 lbs
      expect(screen.getByText('297.62 lbs')).toBeInTheDocument();
      expect(screen.getByText('8')).toBeInTheDocument();
      expect(screen.getByText('90s')).toBeInTheDocument(); // Rest shows as "90s"
      expect(screen.getAllByText('-')).toHaveLength(1); // Only tempo shows "-" when null (notes are displayed)
      expect(screen.getByText('1')).toBeInTheDocument(); // Number of sets
    });

    it('should handle exercise without notes', async () => {
      const dataWithoutNotes = {
        ...mockUserDataExport,
        training_programs: [
          {
            ...mockUserDataExport.training_programs[0],
            workouts: [
              {
                ...mockUserDataExport.training_programs[0].workouts[0],
                stages: [
                  {
                    ...mockUserDataExport.training_programs[0].workouts[0].stages[0],
                    exercises: [
                      {
                        ...mockUserDataExport.training_programs[0].workouts[0].stages[0]
                          .exercises[0],
                        exercise: {
                          ...mockUserDataExport.training_programs[0].workouts[0].stages[0]
                            .exercises[0].exercise,
                          notes: undefined,
                        },
                      },
                    ],
                  },
                ],
              },
            ],
          },
        ],
      };

      // Update the mock to return data without notes
      mockUseData.mockReturnValue({
        userData: dataWithoutNotes,
        exerciseMuscleData: new Map(),
        weightUnitPreferences: [
          {
            user_id: 'test-user-id',
            exercise_name: 'Bench Press',
            preferred_unit: 'LBS',
          },
        ],
        isLoading: false,
        error: null,
        refreshData: jest.fn(),
        isDataStale: false,
      });

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      expect(screen.queryByTestId('NotesIcon')).not.toBeInTheDocument();
    });

    it('should handle multiple stages', async () => {
      const dataWithMultipleStages = {
        ...mockUserDataExport,
        training_programs: [
          {
            ...mockUserDataExport.training_programs[0],
            workouts: [
              {
                ...mockUserDataExport.training_programs[0].workouts[0],
                stages: [
                  mockUserDataExport.training_programs[0].workouts[0].stages[0],
                  {
                    stage: {
                      id: 2,
                      programmed_workout_id: 1,
                      stage_type_id: 2,
                      position: 2,
                      name: 'Main Work',
                      created_at: '2024-01-01T00:00:00Z',
                      updated_at: '2024-01-01T00:00:00Z',
                    },
                    exercises: [],
                  },
                ],
              },
            ],
          },
        ],
      };

      // Update the mock to return data with multiple stages
      mockUseData.mockReturnValue({
        userData: dataWithMultipleStages,
        exerciseMuscleData: new Map(),
        weightUnitPreferences: [
          {
            user_id: 'test-user-id',
            exercise_name: 'Bench Press',
            preferred_unit: 'LBS',
          },
        ],
        isLoading: false,
        error: null,
        refreshData: jest.fn(),
        isDataStale: false,
      });

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      expect(screen.getByText('Warm-up')).toBeInTheDocument();
      expect(screen.getByText('Main Work')).toBeInTheDocument();
    });

    it('should handle multiple exercises', async () => {
      const dataWithMultipleExercises = {
        ...mockUserDataExport,
        training_programs: [
          {
            ...mockUserDataExport.training_programs[0],
            workouts: [
              {
                ...mockUserDataExport.training_programs[0].workouts[0],
                stages: [
                  {
                    ...mockUserDataExport.training_programs[0].workouts[0].stages[0],
                    exercises: [
                      mockUserDataExport.training_programs[0].workouts[0].stages[0].exercises[0],
                      {
                        exercise: {
                          id: 2,
                          workout_stage_id: 1,
                          exercise_name: 'Incline Press',
                          position: 2,
                          notes: undefined,
                          created_at: '2024-01-01T00:00:00Z',
                          updated_at: '2024-01-01T00:00:00Z',
                        },
                        set_schemes: [
                          {
                            id: 2,
                            programmed_exercise_id: 2,
                            set_number: 1,
                            target_rep_count: 6,
                            target_weight: 145,
                            rest_seconds: 90,
                            is_amrap: false,
                            is_emom: false,
                            use_tempo: false,
                            eccentric_tempo: undefined,
                            isometric_tempo: undefined,
                            concentric_tempo: undefined,
                            created_at: '2024-01-01T00:00:00Z',
                            updated_at: '2024-01-01T00:00:00Z',
                          },
                        ],
                      },
                    ],
                  },
                ],
              },
            ],
          },
        ],
      };

      // Update the mock to return data with multiple exercises
      mockUseData.mockReturnValue({
        userData: dataWithMultipleExercises,
        exerciseMuscleData: new Map(),
        weightUnitPreferences: [
          {
            user_id: 'test-user-id',
            exercise_name: 'Bench Press',
            preferred_unit: 'LBS',
          },
        ],
        isLoading: false,
        error: null,
        refreshData: jest.fn(),
        isDataStale: false,
      });

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      expect(screen.getByText('Bench Press')).toBeInTheDocument();
      expect(screen.getByText('Incline Press')).toBeInTheDocument();
    });

    it('should handle multiple set schemes', async () => {
      const dataWithMultipleSets = {
        ...mockUserDataExport,
        training_programs: [
          {
            ...mockUserDataExport.training_programs[0],
            workouts: [
              {
                ...mockUserDataExport.training_programs[0].workouts[0],
                stages: [
                  {
                    ...mockUserDataExport.training_programs[0].workouts[0].stages[0],
                    exercises: [
                      {
                        ...mockUserDataExport.training_programs[0].workouts[0].stages[0]
                          .exercises[0],
                        set_schemes: [
                          mockUserDataExport.training_programs[0].workouts[0].stages[0].exercises[0]
                            .set_schemes[0],
                          {
                            id: 2,
                            programmed_exercise_id: 1,
                            set_number: 2,
                            target_rep_count: 6,
                            target_weight: 145,
                            rest_seconds: 90,
                            is_amrap: false,
                            is_emom: false,
                            use_tempo: false,
                            eccentric_tempo: undefined,
                            isometric_tempo: undefined,
                            concentric_tempo: undefined,
                            created_at: '2024-01-01T00:00:00Z',
                            updated_at: '2024-01-01T00:00:00Z',
                          },
                        ],
                      },
                    ],
                  },
                ],
              },
            ],
          },
        ],
      };

      // Update the mock to return data with multiple set schemes
      mockUseData.mockReturnValue({
        userData: dataWithMultipleSets,
        exerciseMuscleData: new Map(),
        weightUnitPreferences: [
          {
            user_id: 'test-user-id',
            exercise_name: 'Bench Press',
            preferred_unit: 'LBS',
          },
        ],
        isLoading: false,
        error: null,
        refreshData: jest.fn(),
        isDataStale: false,
      });

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      // Should show the number of sets (2) instead of individual set numbers
      expect(screen.getByText('2')).toBeInTheDocument();
    });
  });

  describe('Empty State', () => {
    it('should handle empty stages', async () => {
      const dataWithEmptyStages = {
        ...mockUserDataExport,
        training_programs: [
          {
            ...mockUserDataExport.training_programs[0],
            workouts: [
              {
                ...mockUserDataExport.training_programs[0].workouts[0],
                stages: [],
              },
            ],
          },
        ],
      };

      // Update the mock to return data with empty stages
      mockUseData.mockReturnValue({
        userData: dataWithEmptyStages,
        exerciseMuscleData: new Map(),
        weightUnitPreferences: [],
        isLoading: false,
        error: null,
        refreshData: jest.fn(),
        isDataStale: false,
      });

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      // When there are no stages, only the table header should be visible
      expect(screen.getByText('Exercise')).toBeInTheDocument();
      expect(screen.getByText('Sets')).toBeInTheDocument();
    });

    it('should handle empty exercises', async () => {
      const dataWithEmptyExercises = {
        ...mockUserDataExport,
        training_programs: [
          {
            ...mockUserDataExport.training_programs[0],
            workouts: [
              {
                ...mockUserDataExport.training_programs[0].workouts[0],
                stages: [
                  {
                    ...mockUserDataExport.training_programs[0].workouts[0].stages[0],
                    exercises: [],
                  },
                ],
              },
            ],
          },
        ],
      };

      // Update the mock to return data with empty exercises
      mockUseData.mockReturnValue({
        userData: dataWithEmptyExercises,
        exerciseMuscleData: new Map(),
        weightUnitPreferences: [],
        isLoading: false,
        error: null,
        refreshData: jest.fn(),
        isDataStale: false,
      });

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      // Should show the stage header but no exercise rows
      expect(screen.getByText('Warm-up')).toBeInTheDocument();
      expect(screen.getByText('Exercise')).toBeInTheDocument();
    });

    it('should handle empty set schemes', async () => {
      const dataWithEmptySets = {
        ...mockUserDataExport,
        training_programs: [
          {
            ...mockUserDataExport.training_programs[0],
            workouts: [
              {
                ...mockUserDataExport.training_programs[0].workouts[0],
                stages: [
                  {
                    ...mockUserDataExport.training_programs[0].workouts[0].stages[0],
                    exercises: [
                      {
                        ...mockUserDataExport.training_programs[0].workouts[0].stages[0]
                          .exercises[0],
                        set_schemes: [],
                      },
                    ],
                  },
                ],
              },
            ],
          },
        ],
      };

      // Update the mock to return data with empty set schemes
      mockUseData.mockReturnValue({
        userData: dataWithEmptySets,
        exerciseMuscleData: new Map(),
        weightUnitPreferences: [],
        isLoading: false,
        error: null,
        refreshData: jest.fn(),
        isDataStale: false,
      });

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      // Check that the table headers are present but no data rows
      expect(screen.getByText('Sets')).toBeInTheDocument();
      expect(screen.getByText('Reps')).toBeInTheDocument();
      expect(screen.getByText('Tempo')).toBeInTheDocument();
      expect(screen.getByText('Weight')).toBeInTheDocument();
      expect(screen.getByText('Rest')).toBeInTheDocument();
      expect(screen.getByText('Notes')).toBeInTheDocument();
    });
  });

  describe('Error State', () => {
    it('should handle workout not found', async () => {
      // Mock useData to return data but no matching workout
      mockUseData.mockReturnValue({
        userData: mockUserDataExport,
        exerciseMuscleData: new Map(),
        weightUnitPreferences: [],
        isLoading: false,
        error: null,
        refreshData: jest.fn(),
        isDataStale: false,
      });

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={999} onBack={mockOnBack} />);
      });

      expect(screen.getByText('Workout not found.')).toBeInTheDocument();
    });

    it('should handle API failure', async () => {
      // Mock useData to return null userData (simulating API failure)
      mockUseData.mockReturnValue({
        userData: null,
        exerciseMuscleData: new Map(),
        weightUnitPreferences: [],
        isLoading: false,
        error: 'API Error',
        refreshData: jest.fn(),
        isDataStale: false,
      });

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      // Component should handle error gracefully
      expect(screen.getByText('Workout not found.')).toBeInTheDocument();
    });
  });

  describe('Component Props', () => {
    it('should call onWorkoutDetailsUpdate when workout data is loaded', async () => {
      const mockOnWorkoutDetailsUpdate = jest.fn();
      
      // Mock useData to return the expected data
      mockUseData.mockReturnValue({
        userData: mockUserDataExport,
        exerciseMuscleData: new Map(),
        weightUnitPreferences: [],
        isLoading: false,
        error: null,
        refreshData: jest.fn(),
        isDataStale: false,
      });

      await act(async () => {
        renderWithTheme(
          <WorkoutDetail
            workoutId={1}
            onBack={mockOnBack}
            onWorkoutDetailsUpdate={mockOnWorkoutDetailsUpdate}
          />
        );
      });

      expect(mockOnWorkoutDetailsUpdate).toHaveBeenCalledWith({
        name: 'Push Day',
        day_number: 1,
        stages: 1,
      });
    });
  });
});
