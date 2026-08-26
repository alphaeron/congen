import { ThemeProvider, createTheme } from '@mui/material/styles';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { WorkoutsOverview } from './WorkoutsOverview';
import type { User } from '../api/types';

jest.mock('./Workouts', () => ({
  Workouts: () => <div data-testid="workouts">Mock Workouts Component</div>,
}));

jest.mock('./ExerciseRotationVisualization', () => ({
  ExerciseRotationVisualization: () => (
    <div data-testid="exercise-rotation">Mock Exercise Rotation</div>
  ),
}));

jest.mock('./OneRepMaxRecords', () => ({
  OneRepMaxRecords: () => <div data-testid="one-rep-max-records">Mock 1RM Records</div>,
}));

jest.mock('./WorkoutPreferencesSection', () => ({
  WorkoutPreferencesSection: () => (
    <div data-testid="workout-preferences">Mock Workout Preferences</div>
  ),
}));

const theme = createTheme();

const renderWithTheme = (component: React.ReactElement) => {
  return render(
    <MemoryRouter>
      <ThemeProvider theme={theme}>{component}</ThemeProvider>
    </MemoryRouter>
  );
};

describe('WorkoutsOverview', () => {
  const mockUser: User = {
    keycloak_id: 'test-user-id',
    name: 'Test User',
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
    roles: ['user'],
  };

  it('renders component without errors', () => {
    renderWithTheme(<WorkoutsOverview user={mockUser} />);

    expect(screen.getByText('Workout Calendar')).toBeInTheDocument();
    expect(screen.getByText('Exercise Rotation')).toBeInTheDocument();
    expect(screen.getByText('1RM Records')).toBeInTheDocument();
    expect(screen.getByText('Workout Preferences')).toBeInTheDocument();
  });

  it('displays Workouts component by default', () => {
    renderWithTheme(<WorkoutsOverview user={mockUser} />);

    expect(screen.getByTestId('workouts')).toBeInTheDocument();
  });

  it('switches between tabs correctly', async () => {
    renderWithTheme(<WorkoutsOverview user={mockUser} />);

    const rotationTab = screen.getByRole('tab', { name: /exercise rotation/i });
    fireEvent.click(rotationTab);

    await waitFor(() => {
      expect(rotationTab).toHaveAttribute('aria-selected', 'true');
      expect(screen.getByTestId('exercise-rotation')).toBeInTheDocument();
    });

    const recordsTab = screen.getByRole('tab', { name: /1rm records/i });
    fireEvent.click(recordsTab);

    await waitFor(() => {
      expect(recordsTab).toHaveAttribute('aria-selected', 'true');
      expect(screen.getByTestId('one-rep-max-records')).toBeInTheDocument();
    });

    const preferencesTab = screen.getByRole('tab', { name: /workout preferences/i });
    fireEvent.click(preferencesTab);

    await waitFor(() => {
      expect(preferencesTab).toHaveAttribute('aria-selected', 'true');
      expect(screen.getByTestId('workout-preferences')).toBeInTheDocument();
    });

    const calendarTab = screen.getByRole('tab', { name: /workout calendar/i });
    fireEvent.click(calendarTab);

    await waitFor(() => {
      expect(calendarTab).toHaveAttribute('aria-selected', 'true');
      expect(screen.getByTestId('workouts')).toBeInTheDocument();
    });
  });

  it('passes user prop to Workouts component', () => {
    renderWithTheme(<WorkoutsOverview user={mockUser} />);

    expect(screen.getByTestId('workouts')).toBeInTheDocument();
  });
});
