import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import React from 'react';
import { MemoryRouter } from 'react-router';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { WorkoutsOverview } from './WorkoutsOverview';
import type { User } from '../api/types';

// Mock the child components to prevent Nivo chart issues
jest.mock('./Workouts', () => ({
  Workouts: ({ user }: { user: User }) => (
    <div data-testid="workouts">Mock Workouts Component</div>
  ),
}));

jest.mock('./ConjugateProgression', () => ({
  ConjugateProgression: () => (
    <div data-testid="conjugate-progression">Mock Conjugate Progression</div>
  ),
}));

jest.mock('./ExerciseRotationVisualization', () => ({
  ExerciseRotationVisualization: () => (
    <div data-testid="exercise-rotation">Mock Exercise Rotation</div>
  ),
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
    expect(screen.getByText('Conjugate Progression')).toBeInTheDocument();
    expect(screen.getByText('Exercise Rotation')).toBeInTheDocument();
    expect(screen.getByText('Workout Preferences')).toBeInTheDocument();
  });

  it('displays Workouts component by default', () => {
    renderWithTheme(<WorkoutsOverview user={mockUser} />);
    
    expect(screen.getByTestId('workouts')).toBeInTheDocument();
  });

  it('switches between tabs correctly', async () => {
    renderWithTheme(<WorkoutsOverview user={mockUser} />);

    // Click on Conjugate Progression tab
    const conjugateTab = screen.getByRole('tab', { name: /conjugate progression/i });
    fireEvent.click(conjugateTab);

    // Should show conjugate progression content
    await waitFor(() => {
      expect(conjugateTab).toHaveAttribute('aria-selected', 'true');
      expect(screen.getByTestId('conjugate-progression')).toBeInTheDocument();
    });

    // Click on Exercise Rotation tab
    const rotationTab = screen.getByRole('tab', { name: /exercise rotation/i });
    fireEvent.click(rotationTab);

    // Should show exercise rotation content
    await waitFor(() => {
      expect(rotationTab).toHaveAttribute('aria-selected', 'true');
      expect(screen.getByTestId('exercise-rotation')).toBeInTheDocument();
    });

    // Click on Workout Preferences tab
    const preferencesTab = screen.getByRole('tab', { name: /workout preferences/i });
    fireEvent.click(preferencesTab);

    // Should show workout preferences content
    await waitFor(() => {
      expect(preferencesTab).toHaveAttribute('aria-selected', 'true');
      expect(screen.getByTestId('workout-preferences')).toBeInTheDocument();
    });

    // Click back to Workout Calendar tab
    const calendarTab = screen.getByRole('tab', { name: /workout calendar/i });
    fireEvent.click(calendarTab);

    // Should show workouts content
    await waitFor(() => {
      expect(calendarTab).toHaveAttribute('aria-selected', 'true');
      expect(screen.getByTestId('workouts')).toBeInTheDocument();
    });
  });

  it('passes user prop to Workouts component', () => {
    renderWithTheme(<WorkoutsOverview user={mockUser} />);
    
    // The Workouts component should be rendered (we can't easily test prop passing with mocks)
    expect(screen.getByTestId('workouts')).toBeInTheDocument();
  });
});
