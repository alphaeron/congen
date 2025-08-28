import { ThemeProvider, createTheme } from '@mui/material/styles';
import { render, screen, act } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import React from 'react';

import { WorkoutDetail } from './WorkoutDetail';
import { ENDPOINT } from '../api/endpoint';

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
  user_weight_unit_preferences: [],
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
  return render(<ThemeProvider theme={theme}>{component}</ThemeProvider>);
};

describe('WorkoutDetail', () => {
  const mockOnBack = jest.fn();

  beforeEach(() => {
    mock.reset();
    jest.clearAllMocks();
  });

  describe('Success State', () => {
    beforeEach(() => {
      mock.onGet('/gdpr/export').reply(200, mockUserDataExport);
    });

    it('should display workout details when data loads successfully', async () => {
      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

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

      // Check that the notes icon is present (notes are now in tooltip)
      expect(screen.getByTestId('NotesIcon')).toBeInTheDocument();
    });

    it('should display set scheme information correctly', async () => {
      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={1} onBack={mockOnBack} />);
      });

      expect(screen.getByText('135 lbs')).toBeInTheDocument();
      expect(screen.getByText('8')).toBeInTheDocument();
      expect(screen.getAllByText('-')).toHaveLength(2); // Rest and Notes show as "-" when null
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
                        ...mockUserDataExport.training_programs[0].workouts[0].stages[0].exercises[0],
                        exercise: {
                          ...mockUserDataExport.training_programs[0].workouts[0].stages[0].exercises[0].exercise,
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

      mock.onGet('/gdpr/export').reply(200, dataWithoutNotes);

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

      mock.onGet('/gdpr/export').reply(200, dataWithMultipleStages);

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

      mock.onGet('/gdpr/export').reply(200, dataWithMultipleExercises);

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
                        ...mockUserDataExport.training_programs[0].workouts[0].stages[0].exercises[0],
                        set_schemes: [
                          mockUserDataExport.training_programs[0].workouts[0].stages[0].exercises[0].set_schemes[0],
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

      mock.onGet('/gdpr/export').reply(200, dataWithMultipleSets);

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

      mock.onGet('/gdpr/export').reply(200, dataWithEmptyStages);

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

      mock.onGet('/gdpr/export').reply(200, dataWithEmptyExercises);

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
                        ...mockUserDataExport.training_programs[0].workouts[0].stages[0].exercises[0],
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

      mock.onGet('/gdpr/export').reply(200, dataWithEmptySets);

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
      mock.onGet('/gdpr/export').reply(200, mockUserDataExport);

      await act(async () => {
        renderWithTheme(<WorkoutDetail workoutId={999} onBack={mockOnBack} />);
      });

      expect(screen.getByText('Workout not found.')).toBeInTheDocument();
    });

    it('should handle API failure', async () => {
      mock.onGet('/gdpr/export').reply(500);

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
      mock.onGet('/gdpr/export').reply(200, mockUserDataExport);

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
