import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import { SnackbarProvider } from 'notistack';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { DashboardOverview } from './DashboardOverview';
import type { User } from '../api/types';

const mockUseData = jest.fn();
jest.mock('../contexts/DataContext', () => ({
  useData: () => mockUseData(),
  DataProvider: ({ children }: { children: React.ReactNode }) => children,
}));

jest.mock('./AdventurerStatusCard', () => ({
  AdventurerStatusCard: ({ userName }: { userName: string }) => (
    <div data-testid="adventurer-status-card">
      <div>Adventurer Status Card</div>
      <div>Status for {userName}</div>
    </div>
  ),
}));

jest.mock('./CompactQuestCard', () => ({
  CompactQuestCard: ({ type }: { type: string }) => (
    <div data-testid={`compact-quest-card-${type}`}>Mock {type} quest card</div>
  ),
}));

describe('DashboardOverview', () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  const renderWithProviders = (component: React.ReactElement) => {
    return render(
      <QueryClientProvider client={queryClient}>
        <SnackbarProvider>
          <MemoryRouter>{component}</MemoryRouter>
        </SnackbarProvider>
      </QueryClientProvider>
    );
  };

  const mockUser: User = {
    keycloak_id: 'test-user-id',
    name: 'Test User',
    created_at: new Date('2024-01-01T00:00:00.000Z'),
    updated_at: new Date('2024-01-01T00:00:00.000Z'),
    roles: ['user'],
  };

  const baseDataContext = {
    isLoading: false,
    isReady: true,
    performanceScores: {
      strength: 50,
      endurance: 60,
      power: 40,
      overall: 50,
    },
    performanceMetrics: [],
    weeklyTests: [],
    refreshPerformanceData: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockUseData.mockReturnValue(baseDataContext);
  });

  it('renders loading state when data is not ready', () => {
    mockUseData.mockReturnValue({
      ...baseDataContext,
      isLoading: true,
      isReady: false,
    });

    renderWithProviders(<DashboardOverview user={mockUser} />);

    expect(screen.getByText('Loading dashboard...')).toBeInTheDocument();
  });

  it('renders dashboard content when performance scores are available', () => {
    renderWithProviders(<DashboardOverview user={mockUser} />);

    expect(screen.getByTestId('adventurer-status-card')).toBeInTheDocument();
    expect(screen.getByText('Status for Test User')).toBeInTheDocument();
    expect(screen.getByTestId('compact-quest-card-daily')).toBeInTheDocument();
    expect(screen.getByTestId('compact-quest-card-weekly')).toBeInTheDocument();
  });

  it('renders nothing when performance scores are unavailable', () => {
    mockUseData.mockReturnValue({
      ...baseDataContext,
      performanceScores: null,
    });

    renderWithProviders(<DashboardOverview user={mockUser} />);

    expect(screen.queryByTestId('adventurer-status-card')).not.toBeInTheDocument();
    expect(screen.queryByTestId('compact-quest-card-daily')).not.toBeInTheDocument();
  });
});
