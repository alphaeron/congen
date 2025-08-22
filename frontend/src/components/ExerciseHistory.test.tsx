import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import MockAdapter from 'axios-mock-adapter';
import { ExerciseHistory } from './ExerciseHistory';
import { ENDPOINT } from '../api/endpoint';
import type { User, UserOneRepMax, ExerciseRotationHistory } from '../api/types';

// Create axios mock adapter for the ENDPOINT instance
const mock = new MockAdapter(ENDPOINT);

// Create a theme for testing
const theme = createTheme();

const renderWithTheme = (component: React.ReactElement) => {
  return render(
    <ThemeProvider theme={theme}>
      {component}
    </ThemeProvider>
  );
};

describe('ExerciseHistory', () => {
  const mockUser: User = {
    keycloak_id: 'test-user-id',
    name: 'Test User',
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
    roles: ['user'],
  };

  const mockOneRepMax: UserOneRepMax = {
    user_id: 'test-user-id',
    exercise_name: 'Bench Press',
    one_rep_max: 225,
    unit: 'lbs',
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
  };

  const mockExerciseHistory: ExerciseRotationHistory = {
    id: 1,
    user_id: 'test-user-id',
    exercise_name: 'Bench Press',
    is_accessory: false,
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
  };

  beforeEach(() => {
    mock.reset();
  });

  afterAll(() => {
    mock.restore();
  });

  it('renders loading state initially', () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, []);
    mock.onGet('/exercise_rotation_history/').reply(200, []);

    renderWithTheme(<ExerciseHistory user={mockUser} />);

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('renders visualization page title and tabs', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, []);
    mock.onGet('/exercise_rotation_history/').reply(200, []);

    await waitFor(() => {
      expect(screen.getByText('Exercise Visualization')).toBeInTheDocument();
      expect(screen.getByText('1RM Trends')).toBeInTheDocument();
      expect(screen.getByText('Exercise History')).toBeInTheDocument();
    });
  });

  it('displays 1RM data when loaded successfully', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax]);
    mock.onGet('/exercise_rotation_history/').reply(200, []);

    await waitFor(() => {
      expect(screen.getByText('Bench Press')).toBeInTheDocument();
      expect(screen.getByText('225 lbs')).toBeInTheDocument();
    });
  });

  it('displays exercise history when loaded successfully', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, []);
    mock.onGet('/exercise_rotation_history/').reply(200, [mockExerciseHistory]);

    await waitFor(() => {
      expect(screen.getByText('Bench Press')).toBeInTheDocument();
      expect(screen.getByText('Primary')).toBeInTheDocument();
    });
  });

  it('shows error message when API calls fail', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(500, { message: 'Internal server error' });
    mock.onGet('/exercise_rotation_history/').reply(200, []);

    await waitFor(() => {
      expect(screen.getByText('Failed to load visualization data. Please try again.')).toBeInTheDocument();
    });
  });

  it('switches between tabs when clicked', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax]);
    mock.onGet('/exercise_rotation_history/').reply(200, [mockExerciseHistory]);

    await waitFor(() => {
      const exerciseHistoryTab = screen.getByText('Exercise History');
      fireEvent.click(exerciseHistoryTab);
    });

    // Should show exercise history content
    expect(screen.getByText('Exercise History')).toBeInTheDocument();
  });

  it('filters data by exercise selection', async () => {
    const mockOneRepMax2 = { ...mockOneRepMax, exercise_name: 'Squat' };
    const mockExerciseHistory2 = { ...mockExerciseHistory, id: 2, exercise_name: 'Squat' };
    
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax, mockOneRepMax2]);
    mock.onGet('/exercise_rotation_history/').reply(200, [mockExerciseHistory, mockExerciseHistory2]);

    await waitFor(() => {
      const exerciseSelect = screen.getByLabelText(/exercise/i);
      fireEvent.mouseDown(exerciseSelect);
    });

    await waitFor(() => {
      const squatOption = screen.getByText('Squat');
      fireEvent.click(squatOption);
    });

    // Should show only Squat data
    expect(screen.getByText('Squat')).toBeInTheDocument();
    expect(screen.queryByText('Bench Press')).not.toBeInTheDocument();
  });

  it('displays exercise statistics correctly', async () => {
    const mockExerciseHistory2 = { ...mockExerciseHistory, id: 2, is_accessory: true };
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax]);
    mock.onGet('/exercise_rotation_history/').reply(200, [mockExerciseHistory, mockExerciseHistory2]);

    await waitFor(() => {
      expect(screen.getByText('Bench Press')).toBeInTheDocument();
      expect(screen.getByText('Primary: 1')).toBeInTheDocument();
      expect(screen.getByText('Accessory: 1')).toBeInTheDocument();
    });
  });

  it('shows no data message when no data is available', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, []);
    mock.onGet('/exercise_rotation_history/').reply(200, []);

    await waitFor(() => {
      expect(screen.getByText(/No 1RM data available/)).toBeInTheDocument();
      expect(screen.getByText(/No exercise history available/)).toBeInTheDocument();
    });
  });

  it('displays tooltips for estimated data', async () => {
    const estimatedOneRepMax = { ...mockOneRepMax, is_estimated: true };
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [estimatedOneRepMax]);
    mock.onGet('/exercise_rotation_history/').reply(200, []);

    await waitFor(() => {
      const infoIcon = screen.getByTestId('InfoIcon');
      expect(infoIcon).toBeInTheDocument();
    });
  });

  it('handles exercise filter change', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax]);
    mock.onGet('/exercise_rotation_history/').reply(200, [mockExerciseHistory]);

    await waitFor(() => {
      const exerciseSelect = screen.getByLabelText(/exercise/i);
      fireEvent.mouseDown(exerciseSelect);
    });

    await waitFor(() => {
      const allOption = screen.getByText('All Exercises');
      fireEvent.click(allOption);
    });

    // Should show all data
    expect(screen.getByText('Bench Press')).toBeInTheDocument();
  });

  it('verifies API calls are made with correct endpoints', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, []);
    mock.onGet('/exercise_rotation_history/').reply(200, []);

    await waitFor(() => {
      expect(mock.history.get).toHaveLength(2);
      expect(mock.history.get[0].url).toBe('/user_one_rep_max/user/test-user-id');
      expect(mock.history.get[1].url).toBe('/exercise_rotation_history/');
    });
  });

  it('maintains tab state when switching between tabs', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax]);
    mock.onGet('/exercise_rotation_history/').reply(200, [mockExerciseHistory]);

    await waitFor(() => {
      const exerciseHistoryTab = screen.getByText('Exercise History');
      fireEvent.click(exerciseHistoryTab);
    });

    await waitFor(() => {
      const oneRMTab = screen.getByText('1RM Trends');
      fireEvent.click(oneRMTab);
    });

    // Should be back on 1RM tab
    expect(screen.getByText('1RM Trends')).toBeInTheDocument();
  });
});
