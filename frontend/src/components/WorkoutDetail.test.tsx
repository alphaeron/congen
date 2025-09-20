import { ThemeProvider, createTheme } from '@mui/material/styles';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, act, waitFor } from '@testing-library/react';
import React from 'react';
import { MemoryRouter } from 'react-router';

// Mock react-oidc-context
jest.mock('react-oidc-context', () => ({
  useAuth: () => ({
    user: { sub: 'test-user' },
    isAuthenticated: true,
    signinRedirect: jest.fn(),
    signoutRedirect: jest.fn(),
    removeUser: jest.fn(),
  }),
}));

// Mock DataContext
jest.mock('../contexts/DataContext', () => ({
  useData: jest.fn(),
  DataProvider: ({ children }: { children: React.ReactNode }) => children,
}));

// Mock other components
jest.mock('./ExerciseName', () => ({
  ExerciseName: ({ exerciseId }: { exerciseId: string }) => (
    <div data-testid={`exercise-name-${exerciseId}`}>Exercise {exerciseId}</div>
  ),
}));

jest.mock('./SetSchemeEditor', () => ({
  SetSchemeEditor: ({ setSchemeId }: { setSchemeId: string }) => (
    <div data-testid={`set-scheme-editor-${setSchemeId}`}>Set Scheme {setSchemeId}</div>
  ),
}));

// Mock API functions
jest.mock('../api/programmedWorkout', () => ({
  getProgrammedWorkout: jest.fn(),
  updateProgrammedWorkout: jest.fn(),
}));

jest.mock('../api/programmedExercise', () => ({
  createProgrammedExercise: jest.fn(),
  updateProgrammedExercise: jest.fn(),
  deleteProgrammedExercise: jest.fn(),
}));

jest.mock('../api/setScheme', () => ({
  createSetScheme: jest.fn(),
  updateSetScheme: jest.fn(),
  deleteSetScheme: jest.fn(),
}));

import { WorkoutDetail } from './WorkoutDetail';
import { useData } from '../contexts/DataContext';

const mockUseData = useData as jest.MockedFunction<typeof useData>;

const theme = createTheme();
const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false },
    mutations: { retry: false },
  },
});

const renderWithTheme = (component: React.ReactElement) => {
  return act(() => {
    return render(
      <MemoryRouter>
        <QueryClientProvider client={queryClient}>
          <ThemeProvider theme={theme}>{component}</ThemeProvider>
        </QueryClientProvider>
      </MemoryRouter>
    );
  });
};

describe('WorkoutDetail', () => {
  const mockOnBack = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    mockUseData.mockReset();
  });

  it('should render loading state initially', async () => {
    // Mock the useData hook to return loading state
    mockUseData.mockReturnValue({
      isLoading: true,
      data: null,
      error: null,
    });

    await act(async () => {
      renderWithTheme(<WorkoutDetail workoutId="1" onBack={mockOnBack} />);
    });

    expect(screen.getByText('Loading workout details...')).toBeInTheDocument();
  });

  it('should render workout not found when no data', async () => {
    // Mock the useData hook to return no data
    mockUseData.mockReturnValue({
      isLoading: false,
      data: null,
      error: null,
    });

    await act(async () => {
      renderWithTheme(<WorkoutDetail workoutId="1" onBack={mockOnBack} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText('Workout not found.')).toBeInTheDocument();
      },
      { timeout: 2000 }
    );
  }, 5000);

  it('should render error state when data fails to load', async () => {
    // Mock the useData hook to return error state
    mockUseData.mockReturnValue({
      isLoading: false,
      data: null,
      error: new Error('Failed to load workout'),
    });

    await act(async () => {
      renderWithTheme(<WorkoutDetail workoutId="1" onBack={mockOnBack} />);
    });

    await waitFor(
      () => {
        expect(screen.getByText('Workout not found.')).toBeInTheDocument();
      },
      { timeout: 2000 }
    );
  }, 5000);

  it('should render alert when workout not found', async () => {
    // Mock the useData hook to return no data
    mockUseData.mockReturnValue({
      isLoading: false,
      data: null,
      error: null,
    });

    await act(async () => {
      renderWithTheme(<WorkoutDetail workoutId="1" onBack={mockOnBack} />);
    });

    await waitFor(
      () => {
        expect(screen.getByRole('alert')).toBeInTheDocument();
      },
      { timeout: 2000 }
    );
  }, 5000);
});
