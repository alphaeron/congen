import { ThemeProvider, createTheme } from '@mui/material/styles';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import React from 'react';

import { ExerciseHistory } from './ExerciseHistory';
import { ENDPOINT } from '../api/endpoint';
import type { User, UserOneRepMax } from '../api/types';

// Create axios mock adapter for the ENDPOINT instance
const mock = new MockAdapter(ENDPOINT);

// Create a theme for testing
const theme = createTheme();

// Mock useSearchParams
const mockSetSearchParams = jest.fn();
let mockSearchParams = new URLSearchParams();

jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  useSearchParams: () => [mockSearchParams, mockSetSearchParams],
}));

const renderWithTheme = (component: React.ReactElement) => {
  return render(<ThemeProvider theme={theme}>{component}</ThemeProvider>);
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



  beforeEach(() => {
    mock.reset();
    mockSetSearchParams.mockClear();
    mockSearchParams = new URLSearchParams();
  });

  afterAll(() => {
    mock.restore();
  });

  it('renders loading state initially', () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, []);
    mock.onGet('/exercise/').reply(200, []);

    renderWithTheme(<ExerciseHistory user={mockUser} />);

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('renders visualization page title and tabs', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, []);
    mock.onGet('/exercise/').reply(200, []);

    renderWithTheme(<ExerciseHistory user={mockUser} />);

    await waitFor(() => {
      expect(screen.getByText('Exercise History')).toBeInTheDocument();
      expect(screen.getByText('1RM Progress')).toBeInTheDocument();
      expect(screen.getByText('Exercise Information')).toBeInTheDocument();
    });
  });

  it('displays 1RM data when loaded successfully', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax]);
    mock.onGet('/exercise/').reply(200, []);

    renderWithTheme(<ExerciseHistory user={mockUser} />);

    await waitFor(() => {
      expect(screen.getByText('Bench Press')).toBeInTheDocument();
      expect(screen.getByText('225 lbs')).toBeInTheDocument();
    });
  });



  it('shows error message when API calls fail', async () => {
    mock
      .onGet('/user_one_rep_max/user/test-user-id')
      .reply(500, { message: 'Internal server error' });
    mock.onGet('/exercise/').reply(200, []);

    renderWithTheme(<ExerciseHistory user={mockUser} />);

    await waitFor(() => {
      expect(
        screen.getByText('Failed to load exercise history data. Please try again.')
      ).toBeInTheDocument();
    });
  });

  it('switches between tabs when clicked', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax]);
    mock.onGet('/exercise/').reply(200, []);

    renderWithTheme(<ExerciseHistory user={mockUser} />);

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

    renderWithTheme(<ExerciseHistory user={mockUser} />);

    await waitFor(() => {
      expect(screen.getByText('Bench Press')).toBeInTheDocument();
      expect(screen.getByText('Squat')).toBeInTheDocument();
    });
  });

  it('displays exercise information correctly', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax]);
    mock.onGet('/exercise/').reply(200, []);

    renderWithTheme(<ExerciseHistory user={mockUser} />);

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

    renderWithTheme(<ExerciseHistory user={mockUser} />);

    await waitFor(() => {
      expect(
        screen.getByText('No 1RM data available for the selected exercise.')
      ).toBeInTheDocument();
    });
  });

  it('displays tooltips for estimated data', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax]);
    mock.onGet('/exercise/').reply(200, []);

    renderWithTheme(<ExerciseHistory user={mockUser} />);

    await waitFor(() => {
      expect(screen.getByText('Estimated from performance')).toBeInTheDocument();
    });
  });

  it('handles exercise filter change', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax]);
    mock.onGet('/exercise/').reply(200, []);

    renderWithTheme(<ExerciseHistory user={mockUser} />);

    await waitFor(() => {
      const filterInput = screen.getByLabelText('Filter by Exercise');
      fireEvent.change(filterInput, { target: { value: 'Bench Press' } });
    });
  });

  it('verifies API calls are made with correct endpoints', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, []);
    mock.onGet('/exercise/').reply(200, []);

    renderWithTheme(<ExerciseHistory user={mockUser} />);

    await waitFor(() => {
      expect(mock.history.get).toHaveLength(2);
      expect(mock.history.get[0].url).toBe('/user_one_rep_max/user/test-user-id');
      expect(mock.history.get[1].url).toBe('/exercise/');
    });
  });

  it('maintains tab state when switching between tabs', async () => {
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax]);
    mock.onGet('/exercise/').reply(200, []);

    renderWithTheme(<ExerciseHistory user={mockUser} />);

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
