import { ThemeProvider, createTheme } from '@mui/material/styles';
import { render, screen } from '@testing-library/react';
import React from 'react';

import { UserStatusSystem } from './UserStatusSystem';
import type { User, UserDataExport, UserOneRepMax } from '../api/types';

// Mock the StatusCard components
jest.mock('./StatusCard', () => ({
  StatusCard: ({
    title,
    status,
    value,
    unit,
    trend,
    description,
  }: {
    title: string;
    status: string;
    value: number;
    unit: string;
    trend: string;
    description: string;
  }) => (
    <div data-testid={`status-card-${title.toLowerCase().replace(/\s+/g, '-')}`}>
      <div data-testid="status-title">{title}</div>
      <div data-testid="status-status">{status}</div>
      <div data-testid="status-value">{value}</div>
      <div data-testid="status-unit">{unit}</div>
      <div data-testid="status-trend">{trend}</div>
      <div data-testid="status-description">{description}</div>
    </div>
  ),
  StatusIndicator: ({ status }: { status: string }) => (
    <div data-testid={`status-indicator-${status}`}>Status: {status}</div>
  ),
  StatusProgress: ({ value, status }: { value: number; status: string }) => (
    <div data-testid={`status-progress-${status}`}>Progress: {value}%</div>
  ),
}));

// Create a theme for testing
const theme = createTheme();

const renderWithTheme = (component: React.ReactElement) => {
  return render(<ThemeProvider theme={theme}>{component}</ThemeProvider>);
};

describe('UserStatusSystem', () => {
  const mockUser: User = {
    keycloak_id: 'test-user-id',
    name: 'Test User',
    created_at: new Date('2024-01-01T00:00:00.000Z'),
    updated_at: new Date('2024-01-01T00:00:00.000Z'),
    roles: ['user'],
  };

  const mockOneRepMax: UserOneRepMax = {
    user_id: 'test-user-id',
    exercise_name: 'Bench Press',
    one_rep_max: 225,
    unit: 'LBS',
    created_at: new Date('2024-01-01T00:00:00Z'),
    updated_at: new Date('2024-01-15T00:00:00Z'),
  };

  const mockUserDataExport: UserDataExport = {
    training_programs: [
      {
        program: { id: 1, name: 'Test Program' },
        workouts: [
          {
            workout: {
              id: 1,
              name: 'Test Workout',
              created_at: new Date('2024-01-01T00:00:00Z'),
              updated_at: new Date('2024-01-01T00:00:00Z'),
              program_id: 1,
              day_number: 1,
            },
            stages: [],
          },
        ],
      },
    ],
    user_one_rep_max: [mockOneRepMax],
    data_retention_policies: [],
  };

  it('renders overall health and fitness status', () => {
    renderWithTheme(
      <UserStatusSystem
        user={mockUser}
        userData={mockUserDataExport}
        oneRepMaxes={[mockOneRepMax]}
      />
    );

    expect(screen.getByText('Overall Health & Fitness Status')).toBeInTheDocument();
  });

  it('renders all status metric cards', () => {
    renderWithTheme(
      <UserStatusSystem
        user={mockUser}
        userData={mockUserDataExport}
        oneRepMaxes={[mockOneRepMax]}
      />
    );

    // Check that all status cards are rendered
    expect(screen.getByTestId('status-card-strength-training')).toBeInTheDocument();
    expect(screen.getByTestId('status-card-cardio-fitness')).toBeInTheDocument();
    expect(screen.getByTestId('status-card-recovery')).toBeInTheDocument();
    expect(screen.getByTestId('status-card-nutrition')).toBeInTheDocument();
  });

  it('displays strength training metrics correctly', () => {
    renderWithTheme(
      <UserStatusSystem
        user={mockUser}
        userData={mockUserDataExport}
        oneRepMaxes={[mockOneRepMax]}
      />
    );

    const strengthCard = screen.getByTestId('status-card-strength-training');
    expect(strengthCard).toHaveTextContent('Strength Training');
    expect(strengthCard).toHaveTextContent('1'); // 1 1RM record
    expect(strengthCard).toHaveTextContent('1RMs');
    expect(strengthCard).toHaveTextContent('Based on recent 1RM improvements');
  });

  it('displays cardio fitness metrics correctly', () => {
    renderWithTheme(
      <UserStatusSystem
        user={mockUser}
        userData={mockUserDataExport}
        oneRepMaxes={[mockOneRepMax]}
      />
    );

    const cardioCard = screen.getByTestId('status-card-cardio-fitness');
    expect(cardioCard).toHaveTextContent('Cardio Fitness');
    expect(cardioCard).toHaveTextContent('0'); // Placeholder value
    expect(cardioCard).toHaveTextContent('VO2 Max');
    expect(cardioCard).toHaveTextContent('VO2 Max and cardiovascular health');
  });

  it('displays recovery metrics correctly', () => {
    renderWithTheme(
      <UserStatusSystem
        user={mockUser}
        userData={mockUserDataExport}
        oneRepMaxes={[mockOneRepMax]}
      />
    );

    const recoveryCard = screen.getByTestId('status-card-recovery');
    expect(recoveryCard).toHaveTextContent('Recovery');
    expect(recoveryCard).toHaveTextContent('85'); // Placeholder value
    expect(recoveryCard).toHaveTextContent('%');
    expect(recoveryCard).toHaveTextContent('Sleep quality and recovery metrics');
  });

  it('displays nutrition metrics correctly', () => {
    renderWithTheme(
      <UserStatusSystem
        user={mockUser}
        userData={mockUserDataExport}
        oneRepMaxes={[mockOneRepMax]}
      />
    );

    const nutritionCard = screen.getByTestId('status-card-nutrition');
    expect(nutritionCard).toHaveTextContent('Nutrition');
    expect(nutritionCard).toHaveTextContent('0'); // Placeholder value
    expect(nutritionCard).toHaveTextContent('Score');
    expect(nutritionCard).toHaveTextContent('Nutritional balance and hydration');
  });

  it('calculates strength status based on 1RM improvements', () => {
    const improvingOneRepMaxes = [
      { ...mockOneRepMax, one_rep_max: 200, updated_at: new Date('2024-01-01T00:00:00Z') },
      { ...mockOneRepMax, one_rep_max: 225, updated_at: new Date('2024-01-15T00:00:00Z') },
    ];

    renderWithTheme(
      <UserStatusSystem
        user={mockUser}
        userData={mockUserDataExport}
        oneRepMaxes={improvingOneRepMaxes}
      />
    );

    const strengthCard = screen.getByTestId('status-card-strength-training');
    expect(strengthCard).toHaveTextContent('2'); // 2 1RM records
  });

  it('handles empty user data gracefully', () => {
    const emptyUserData: UserDataExport = {
      training_programs: [],
      user_one_rep_max: [],
      data_retention_policies: [],
    };

    renderWithTheme(<UserStatusSystem user={mockUser} userData={emptyUserData} oneRepMaxes={[]} />);

    expect(screen.getByText('Overall Health & Fitness Status')).toBeInTheDocument();
    expect(screen.getByTestId('status-card-strength-training')).toBeInTheDocument();
    expect(screen.getByTestId('status-card-cardio-fitness')).toBeInTheDocument();
    expect(screen.getByTestId('status-card-recovery')).toBeInTheDocument();
    expect(screen.getByTestId('status-card-nutrition')).toBeInTheDocument();
  });

  it('handles null user data gracefully', () => {
    renderWithTheme(<UserStatusSystem user={mockUser} userData={null} oneRepMaxes={[]} />);

    expect(screen.getByText('Overall Health & Fitness Status')).toBeInTheDocument();
    expect(screen.getByTestId('status-card-strength-training')).toBeInTheDocument();
  });

  it('calculates training frequency status correctly', () => {
    const highFrequencyData: UserDataExport = {
      ...mockUserDataExport,
      training_programs: [
        {
          program: { id: 1, name: 'High Frequency Program' },
          workouts: Array.from({ length: 25 }, (_, i) => ({
            workout: {
              id: i + 1,
              name: `Workout ${i + 1}`,
              created_at: new Date('2024-01-01T00:00:00Z'),
              updated_at: new Date('2024-01-01T00:00:00Z'),
              program_id: 1,
              day_number: i + 1,
            },
            stages: [],
          })),
        },
      ],
    };

    renderWithTheme(
      <UserStatusSystem
        user={mockUser}
        userData={highFrequencyData}
        oneRepMaxes={[mockOneRepMax]}
      />
    );

    // Should still render all components
    expect(screen.getByText('Overall Health & Fitness Status')).toBeInTheDocument();
    expect(screen.getByTestId('status-card-strength-training')).toBeInTheDocument();
  });

  it('displays trend information correctly', () => {
    const multipleOneRepMaxes = [
      { ...mockOneRepMax, one_rep_max: 200, updated_at: new Date('2024-01-01T00:00:00Z') },
      { ...mockOneRepMax, one_rep_max: 225, updated_at: new Date('2024-01-15T00:00:00Z') },
    ];

    renderWithTheme(
      <UserStatusSystem
        user={mockUser}
        userData={mockUserDataExport}
        oneRepMaxes={multipleOneRepMaxes}
      />
    );

    const strengthCard = screen.getByTestId('status-card-strength-training');
    expect(strengthCard).toHaveTextContent('up'); // Should show improving trend
  });

  it('handles single 1RM record correctly', () => {
    renderWithTheme(
      <UserStatusSystem
        user={mockUser}
        userData={mockUserDataExport}
        oneRepMaxes={[mockOneRepMax]}
      />
    );

    const strengthCard = screen.getByTestId('status-card-strength-training');
    expect(strengthCard).toHaveTextContent('1'); // Single 1RM record
    expect(strengthCard).toHaveTextContent('stable'); // No trend with single record
  });
});
