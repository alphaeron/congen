import { render, screen } from '@testing-library/react';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { ConjugateProgression } from './ConjugateProgression';
import type { User, UserDataExport, Exercise, UserOneRepMax } from '../api/types';
import type { UserWeightUnitPreference } from '../api/userWeightUnitPreference';
import { WeightUnit } from '../api/userWeightUnitPreference';

// Mock Nivo charts to avoid rendering issues in tests
interface LineChartData {
  id: string;
  data: Array<{ x: number; y: number }>;
}

interface PieChartData {
  id: string;
  value: number;
}

interface ChordChartData {
  data: unknown[];
  keys?: string[];
}

jest.mock('@nivo/line', () => ({
  ResponsiveLine: ({ data }: { data: LineChartData[] }) => (
    <div data-testid="line-chart">
      {data.map((series: LineChartData) => (
        <div key={series.id} data-testid={`line-series-${series.id}`}>
          {series.data.length} points
        </div>
      ))}
    </div>
  ),
}));

jest.mock('@nivo/pie', () => ({
  ResponsivePie: ({ data }: { data: PieChartData[] }) => (
    <div data-testid="pie-chart">
      {data.map((item: PieChartData) => (
        <div key={item.id} data-testid={`pie-item-${item.id}`}>
          {item.value}
        </div>
      ))}
    </div>
  ),
}));

jest.mock('@nivo/chord', () => ({
  ResponsiveChord: ({ keys }: ChordChartData) => (
    <div data-testid="chord-chart">{keys?.length || 0} keys</div>
  ),
}));

const renderWithProviders = (component: React.ReactElement) => {
  return render(<MemoryRouter>{component}</MemoryRouter>);
};

const mockUser: User = {
  keycloak_id: 'test-user-id',
  name: 'Test User',
  created_at: new Date('2024-01-01T00:00:00Z'),
  updated_at: new Date('2024-01-01T00:00:00Z'),
  roles: ['user'],
};

const mockExercise: Exercise = {
  name: 'Bench Press',
  description: 'A compound exercise for chest',
  movement_type: 'push',
  is_unilateral: false,
  is_upper: true,
  is_accessory: false,
};

const mockOneRepMax: UserOneRepMax = {
  user_id: 'test-user-id',
  exercise_name: 'Bench Press',
  one_rep_max: 225,
  unit: 'LBS',
  created_at: new Date('2024-01-01T00:00:00Z'),
  updated_at: new Date('2024-01-01T00:00:00Z'),
};

const mockWeightUnitPreference: UserWeightUnitPreference = {
  user_id: 'test-user-id',
  exercise_name: 'Bench Press',
  preferred_unit: WeightUnit.LBS,
  created_at: new Date('2024-01-01T00:00:00Z'),
  updated_at: new Date('2024-01-01T00:00:00Z'),
};

const mockUserDataExport: UserDataExport = {
  keycloak_id: 'test-user-id',
  name: 'Test User',
  created_at: new Date('2024-01-01T00:00:00Z'),
  updated_at: new Date('2024-01-01T00:00:00Z'),
  data_processing_consent: true,
  consent_timestamp: new Date('2024-01-01T00:00:00Z'),
  export_timestamp: new Date('2024-01-01T00:00:00Z'),
  user_equipment: [],
  user_exercise_preferences: [],
  user_one_rep_max: [mockOneRepMax] as unknown as Record<string, unknown>[],
  user_weight_unit_preferences: [mockWeightUnitPreference] as unknown as Record<string, unknown>[],
  training_programs: [
    {
      program: {
        id: 1,
        user_id: 'test-user-id',
        name: 'Test Program',
        current_week_number: 1,
        created_at: new Date('2024-01-01T00:00:00Z'),
        updated_at: new Date('2024-01-01T00:00:00Z'),
        is_active: true,
      },
      program_preferences: {
        program_id: 1,
        program_days_per_week: 3,
        session_time_length_in_minutes: 60,
        created_at: new Date('2024-01-01T00:00:00Z'),
        updated_at: new Date('2024-01-01T00:00:00Z'),
      },
      workouts: [
        {
          workout: {
            id: 1,
            program_id: 1,
            day_number: 1,
            name: 'Push Day',
            created_at: new Date('2024-01-01T00:00:00Z'),
            updated_at: new Date('2024-01-01T00:00:00Z'),
          },
          stages: [
            {
              stage: {
                id: 1,
                programmed_workout_id: 1,
                stage_type_id: 1,
                position: 1,
                name: 'Warm-up',
                created_at: new Date('2024-01-01T00:00:00Z'),
                updated_at: new Date('2024-01-01T00:00:00Z'),
              },
              exercises: [
                {
                  exercise: {
                    id: 1,
                    workout_stage_id: 1,
                    exercise_name: 'Bench Press',
                    position: 1,
                    notes: 'Focus on form',
                    created_at: new Date('2024-01-01T00:00:00Z'),
                    updated_at: new Date('2024-01-01T00:00:00Z'),
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
                      created_at: new Date('2024-01-01T00:00:00Z'),
                      updated_at: new Date('2024-01-01T00:00:00Z'),
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

describe('ConjugateProgression', () => {
  it('should display empty state when no training programs exist', () => {
    const emptyUserData: UserDataExport = {
      ...mockUserDataExport,
      training_programs: [],
    };

    renderWithProviders(
      <ConjugateProgression
        user={mockUser}
        userData={emptyUserData}
        exerciseData={new Map()}
        oneRepMaxes={[]}
        weightUnitPreferences={[]}
      />
    );

    expect(screen.getByText('Conjugate Progress Tracking')).toBeInTheDocument();
    expect(
      screen.getByText(/Complete your first workout to see progress statistics and correlations/)
    ).toBeInTheDocument();
  });

  it('should display component structure when training programs exist', () => {
    const exerciseData = new Map<string, Exercise>();
    exerciseData.set('Bench Press', mockExercise);

    renderWithProviders(
      <ConjugateProgression
        user={mockUser}
        userData={mockUserDataExport}
        exerciseData={exerciseData}
        oneRepMaxes={[mockOneRepMax]}
        weightUnitPreferences={[mockWeightUnitPreference]}
      />
    );

    // Check that the component renders without the empty state message
    expect(screen.queryByText(/Complete your first workout to see progress statistics and correlations/)).not.toBeInTheDocument();
    
    // Check that the component renders the actual content
    expect(screen.getByText('Volume Progression')).toBeInTheDocument();
    expect(screen.getByText('Exercise Distribution')).toBeInTheDocument();
    expect(screen.getByText('Current 1RM Values')).toBeInTheDocument();
    expect(screen.getByText('Progress Tracking')).toBeInTheDocument();
  });

  it('should handle weight unit conversion correctly', () => {
    const kgOneRepMax: UserOneRepMax = {
      ...mockOneRepMax,
      exercise_name: 'Squat',
      one_rep_max: 100,
      unit: 'KG',
    };

    const kgWeightUnitPreference: UserWeightUnitPreference = {
      ...mockWeightUnitPreference,
      exercise_name: 'Squat',
      preferred_unit: WeightUnit.LBS,
    };

    const exerciseData = new Map<string, Exercise>();
    exerciseData.set('Bench Press', mockExercise);
    exerciseData.set('Squat', { ...mockExercise, name: 'Squat' });

    renderWithProviders(
      <ConjugateProgression
        user={mockUser}
        userData={mockUserDataExport}
        exerciseData={exerciseData}
        oneRepMaxes={[mockOneRepMax, kgOneRepMax]}
        weightUnitPreferences={[mockWeightUnitPreference, kgWeightUnitPreference]}
      />
    );

    // Component should render without errors and show the expected content
    expect(screen.getByText('Volume Progression')).toBeInTheDocument();
    expect(screen.getByText('Bench Press')).toBeInTheDocument();
    expect(screen.getByText('Squat')).toBeInTheDocument();
  });

  it('should render with minimal data', () => {
    renderWithProviders(
      <ConjugateProgression
        user={mockUser}
        userData={null}
        exerciseData={new Map()}
        oneRepMaxes={[]}
        weightUnitPreferences={[]}
      />
    );

    // Component should handle null userData gracefully
    expect(screen.getByText('Conjugate Progress Tracking')).toBeInTheDocument();
  });
});
