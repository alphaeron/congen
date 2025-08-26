import { render, screen, waitFor, act } from '@testing-library/react';
import MockAdapter from 'axios-mock-adapter';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { DashboardOverview } from './DashboardOverview';
import { ENDPOINT } from '../api/endpoint';
import type { User } from '../api/types';

// Create axios mock adapter for the ENDPOINT instance
const mock = new MockAdapter(ENDPOINT);

const renderWithProviders = (component: React.ReactElement) => {
  return render(<MemoryRouter>{component}</MemoryRouter>);
};

const mockUser: User = {
  keycloak_id: 'test-user-id',
  name: 'Test User',
  created_at: '2024-01-01T00:00:00Z',
  updated_at: '2024-01-01T00:00:00Z',
  roles: ['user'],
};

const mockProgram = {
  id: 1,
  user_id: 'test-user-id',
  name: 'Test Program',
  current_week_number: 2,
  created_at: '2024-01-01T00:00:00Z',
  updated_at: '2024-01-01T00:00:00Z',
  is_active: true,
};

const mockOneRepMax = {
  user_id: 'test-user-id',
  exercise_name: 'Bench Press',
  one_rep_max: 225,
  unit: 'KG',
  created_at: '2024-01-01T00:00:00Z',
  updated_at: '2024-01-01T00:00:00Z',
};

describe('DashboardOverview', () => {
  beforeEach(() => {
    mock.reset();
    jest.clearAllMocks();
  });

  afterAll(() => {
    mock.restore();
  });

  it('should render loading state initially', () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, []);

    renderWithProviders(<DashboardOverview user={mockUser} />);

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('should render dashboard overview when data loads successfully', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax]);
    mock.onGet('/exercise/').reply(200, []);

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Dashboard Overview')).toBeInTheDocument();
    });

    expect(screen.getByText('Total Workouts')).toBeInTheDocument();
    expect(screen.getByText('1RM Records')).toBeInTheDocument();
    expect(screen.getByText('Unique Exercises')).toBeInTheDocument();
    expect(screen.getByText('Current Week')).toBeInTheDocument();
  });

  it('should render error message when API calls fail', async () => {
    mock.onGet('/program/').reply(500, { message: 'Internal server error' });
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, []);

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    await waitFor(() => {
      expect(
        screen.getByText('Failed to load dashboard data. Please try again.')
      ).toBeInTheDocument();
    });
  });

  it('should display active program when available', async () => {
    mock.onGet('/program/').reply(200, [mockProgram]);
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, []);
    mock.onGet('/exercise/').reply(200, []);

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Active Program')).toBeInTheDocument();
      expect(screen.getByText('Test Program')).toBeInTheDocument();
      expect(screen.getByText('Week 2')).toBeInTheDocument();
      expect(screen.getByText('Active')).toBeInTheDocument();
    });
  });

  it('should display recent 1RM records when available', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, [mockOneRepMax]);
    mock.onGet('/exercise/').reply(200, []);

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Recent 1RM Records')).toBeInTheDocument();
      // Check for Bench Press in the Recent 1RM Records section specifically
      const recentOneRepMaxSection = screen.getByText('Recent 1RM Records').closest('.MuiCard-root');
      expect(recentOneRepMaxSection).toHaveTextContent('Bench Press');
      expect(recentOneRepMaxSection).toHaveTextContent('225 KG');
    });
  }, 10000);

  it('should display welcome message when no data is available', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, []);

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    await waitFor(() => {
      expect(screen.getByText('Welcome to Your Dashboard!')).toBeInTheDocument();
      expect(screen.getByText(/Start by creating your first program/)).toBeInTheDocument();
    });
  });

  it('should calculate and display correct statistics', async () => {
    const multiplePrograms = [
      { ...mockProgram, current_week_number: 3 },
      { ...mockProgram, id: 2, current_week_number: 2, is_active: false },
    ];
    const multipleOneRepMaxes = [
      mockOneRepMax,
      { ...mockOneRepMax, exercise_name: 'Squat', one_rep_max: 315 },
    ];

    mock.onGet('/program/').reply(200, multiplePrograms);
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, multipleOneRepMaxes);
    mock.onGet('/exercise/').reply(200, []);

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    await waitFor(
      () => {
        // Total workouts should be sum of current_week_number (3 + 2 = 5)
        expect(screen.getByText('5')).toBeInTheDocument();
        // 1RM records count - check the card with ShowChartIcon (first one in dashboard)
        const showChartIcons = screen.getAllByTestId('ShowChartIcon');
        const oneRepMaxCard = showChartIcons[0].closest('.MuiCard-root');
        expect(oneRepMaxCard).toHaveTextContent('2');
        // Unique exercises count - check the card with TrendingUpIcon
        const uniqueExercisesCard = screen.getByTestId('TrendingUpIcon').closest('.MuiCard-root');
        expect(uniqueExercisesCard).toHaveTextContent('2');
        // Current week from active program
        expect(screen.getByText('3')).toBeInTheDocument();
      },
      { timeout: 10000 }
    );
  }, 15000);

  it('should verify API calls are made with correct endpoints', async () => {
    mock.onGet('/program/').reply(200, []);
    mock.onGet('/user_one_rep_max/user/test-user-id').reply(200, []);
    mock.onGet('/exercise/').reply(200, []);

    await act(async () => {
      renderWithProviders(<DashboardOverview user={mockUser} />);
    });

    await waitFor(() => {
      expect(mock.history.get).toHaveLength(4);
      expect(mock.history.get[0].url).toBe('/program/');
      expect(mock.history.get[1].url).toBe('/user_one_rep_max/user/test-user-id');
      expect(mock.history.get[2].url).toBe('/user_one_rep_max/user/test-user-id');
      expect(mock.history.get[3].url).toBe('/exercise/');
    });
  }, 10000);
});
