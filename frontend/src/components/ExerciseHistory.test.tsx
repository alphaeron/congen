import { ThemeProvider, createTheme } from '@mui/material/styles';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import { SnackbarProvider } from 'notistack';
import React from 'react';

import { ExerciseHistory } from './ExerciseHistory';
import { ENDPOINT } from '../api/endpoint';
import type { User, UserOneRepMax } from '../api/types';

// Create a theme for testing
const theme = createTheme();

// Mock useSearchParams
const mockSetSearchParams = jest.fn();
let mockSearchParams = new URLSearchParams();

jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  useSearchParams: () => [mockSearchParams, mockSetSearchParams],
}));

const renderWithProviders = (component: React.ReactElement) => {
  return render(
    <SnackbarProvider>
      <ThemeProvider theme={theme}>{component}</ThemeProvider>
    </SnackbarProvider>
  );
};

describe('ExerciseHistory', () => {
  // Create a new mock adapter for each test to prevent interference
  let mock: MockAdapter;

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

  beforeEach(() => {
    // Create a fresh mock adapter for each test
    mock = new MockAdapter(ENDPOINT);
    jest.clearAllMocks();
    mockSetSearchParams.mockClear();
    mockSearchParams = new URLSearchParams();
  });

  afterEach(() => {
    // Properly clean up the mock adapter
    if (mock) {
      mock.restore();
    }
  });

  it('renders visualization page title and tabs', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, []);
    mock.onGet('/exercise/').reply(200, []);

    await act(async () => {
      renderWithProviders(<ExerciseHistory user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Exercise History')).toBeInTheDocument();
      expect(screen.getByText('1RM Progress')).toBeInTheDocument();
      expect(screen.getByText('Exercise Information')).toBeInTheDocument();
    });
  });

  it('displays 1RM data when loaded successfully', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax]);
    mock.onGet('/exercise/').reply(200, []);

    await act(async () => {
      renderWithProviders(<ExerciseHistory user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Bench Press')).toBeInTheDocument();
      expect(screen.getByText('225 lbs')).toBeInTheDocument();
    });
  });

  it('switches between tabs when clicked', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax]);
    mock.onGet('/exercise/').reply(200, []);

    await act(async () => {
      renderWithProviders(<ExerciseHistory user={mockUser} />);
    });

    // Mock the URL parameter update
    mockSetSearchParams.mockImplementation(newParams => {
      mockSearchParams = newParams;
    });

    await waitFor(() => {
      const exerciseInfoTab = screen.getByText('Exercise Information');
      fireEvent.click(exerciseInfoTab);
    });

    await waitFor(() => {
      expect(screen.getByText('Exercise Information')).toBeInTheDocument();
    });
  });

  it('filters data by exercise selection', async () => {
    const mockOneRepMax2 = { ...mockOneRepMax, exercise_name: 'Squat' };

    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax, mockOneRepMax2]);
    mock.onGet('/exercise/').reply(200, []);

    await act(async () => {
      renderWithProviders(<ExerciseHistory user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Bench Press')).toBeInTheDocument();
      expect(screen.getByText('Squat')).toBeInTheDocument();
    });
  });

  it('displays exercise information correctly', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax]);
    mock.onGet('/exercise/').reply(200, []);

    await act(async () => {
      renderWithProviders(<ExerciseHistory user={mockUser} />);
    });

    // Mock the URL parameter update
    mockSetSearchParams.mockImplementation(newParams => {
      mockSearchParams = newParams;
    });

    // Switch to Exercise Information tab
    await waitFor(() => {
      const exerciseInfoTab = screen.getByText('Exercise Information');
      fireEvent.click(exerciseInfoTab);
    });

    await waitFor(() => {
      expect(screen.getByText('Exercise Information')).toBeInTheDocument();
      expect(screen.getByText('Bench Press')).toBeInTheDocument();
    });
  });

  it('shows no data message when no data is available', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, []);
    mock.onGet('/exercise/').reply(200, []);

    await act(async () => {
      renderWithProviders(<ExerciseHistory user={mockUser} />);
    });

    await waitFor(() => {
      expect(
        screen.getByText('No 1RM data available for the selected exercise.')
      ).toBeInTheDocument();
    });
  });

  it('displays tooltips for estimated data', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax]);
    mock.onGet('/exercise/').reply(200, []);

    await act(async () => {
      renderWithProviders(<ExerciseHistory user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Estimated from performance')).toBeInTheDocument();
    });
  });

  it('handles exercise filter change', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax]);
    mock.onGet('/exercise/').reply(200, []);

    await act(async () => {
      renderWithProviders(<ExerciseHistory user={mockUser} />);
    });

    await waitFor(() => {
      const filterInput = screen.getByLabelText('Filter by Exercise');
      fireEvent.change(filterInput, { target: { value: 'Bench Press' } });
    });
  });

  it('verifies API calls are made with correct endpoints', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, []);
    mock.onGet('/exercise/').reply(200, []);

    await act(async () => {
      renderWithProviders(<ExerciseHistory user={mockUser} />);
    });

    await waitFor(() => {
      expect(mock.history.get.length).toBeGreaterThanOrEqual(2);
      expect(mock.history.get[0].url).toBe('/user_one_rep_max/user/test-user-id');
      expect(mock.history.get[1].url).toBe('/exercise/');
    });
  });

  it('maintains tab state when switching between tabs', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax]);
    mock.onGet('/exercise/').reply(200, []);

    await act(async () => {
      renderWithProviders(<ExerciseHistory user={mockUser} />);
    });

    // Mock the URL parameter update
    mockSetSearchParams.mockImplementation(newParams => {
      mockSearchParams = newParams;
    });

    await waitFor(() => {
      const exerciseInfoTab = screen.getByText('Exercise Information');
      fireEvent.click(exerciseInfoTab);
    });

    await waitFor(() => {
      expect(screen.getByText('Exercise Information')).toBeInTheDocument();
    });

    // Switch back to 1RM Progress tab
    await waitFor(() => {
      const oneRmTab = screen.getByText('1RM Progress');
      fireEvent.click(oneRmTab);
    });

    await waitFor(() => {
      expect(screen.getByText('1RM Progress Tracking')).toBeInTheDocument();
    });
  });
});
