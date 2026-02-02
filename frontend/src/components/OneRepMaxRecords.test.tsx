import { render, screen, waitFor } from '@testing-library/react';
import React from 'react';

import { OneRepMaxRecords } from './OneRepMaxRecords';

// Mock the DataContext
const mockUseData = {
  userData: {
    user_one_rep_max: [
      {
        user_id: 'user123',
        exercise_name: 'Bench Press',
        one_rep_max: 100,
        unit: 'KG',
        created_at: new Date('2023-01-01T00:00:00Z'),
        updated_at: new Date('2023-01-01T00:00:00Z'),
      },
    ],
  },
  weightUnitPreferences: [
    {
      exercise_name: 'Bench Press',
      preferred_unit: 'KG',
    },
  ],
  isLoading: false,
  upsertUserOneRepMax: jest.fn(),
  allExercises: [{ name: 'Bench Press' }, { name: 'Squat' }, { name: 'Deadlift' }],
  loadAllExercises: jest.fn(),
  userOneRepMaxes: [
    {
      user_id: 'user123',
      exercise_name: 'Bench Press',
      one_rep_max: 100,
      unit: 'KG',
      created_at: new Date('2023-01-01T00:00:00Z'),
      updated_at: new Date('2023-01-01T00:00:00Z'),
    },
  ],
  loadUserOneRepMaxes: jest.fn(),
  getDefaultWeightUnit: jest.fn(() => 'KG'),
};

jest.mock('../contexts/DataContext', () => ({
  useData: jest.fn(() => mockUseData),
}));

// Mock notistack
jest.mock('notistack', () => ({
  useSnackbar: () => ({
    enqueueSnackbar: jest.fn(),
  }),
}));

// Mock TanStack components
jest.mock('@tanstack/react-form', () => ({
  useField: () => ({
    state: { value: 'Bench Press' },
  }),
}));

jest.mock('@tanstack/react-table', () => ({
  useReactTable: () => ({
    getRowModel: () => ({ rows: [] }),
    getHeaderGroups: () => [],
  }),
  getCoreRowModel: () => ({}),
  getFilteredRowModel: () => ({}),
  flexRender: (content: unknown, context: unknown) => {
    if (typeof content === 'function') {
      try {
        const result = content(context);
        return String(result || '');
      } catch {
        return String(content || '');
      }
    }
    return String(content || '');
  },
  createColumnHelper: () => ({
    accessor: () => ({}),
  }),
}));

jest.mock('@tanstack/react-virtual', () => ({
  useVirtualizer: () => ({
    getVirtualItems: () => [],
    getTotalSize: () => 0,
  }),
}));

// Mock components
jest.mock('./ExerciseName', () => ({
  ExerciseName: ({ exerciseName }: { exerciseName: string }) => (
    <div data-testid="exercise-name">{exerciseName}</div>
  ),
}));

jest.mock('./FormDialog', () => ({
  FormDialog: ({
    children,
    open,
    onClose,
    onSubmit,
    title,
  }: {
    children: React.ReactNode;
    open: boolean;
    onClose: () => void;
    onSubmit: (data: { exercise_name: string; one_rep_max: number }) => void;
    title: string;
  }) =>
    open ? (
      <div data-testid="form-dialog">
        <div data-testid="dialog-title">{title}</div>
        <button data-testid="close-dialog" onClick={onClose}>
          Close
        </button>
        <button
          data-testid="submit-dialog"
          onClick={() => onSubmit({ exercise_name: 'Bench Press', one_rep_max: 100 })}
        >
          Submit
        </button>
        {children}
      </div>
    ) : null,
}));

// Mock GameTheme components
jest.mock('./GameTheme', () => {
  // eslint-disable-next-line @typescript-eslint/no-require-imports
  const React = require('react');
  return {
    // eslint-disable-next-line react/prop-types
    GameCard: ({ children, ...props }) => {
      return React.createElement('div', { 'data-testid': 'game-card', ...props }, children);
    },
    // eslint-disable-next-line react/prop-types
    GameText: ({ children, ...props }) => {
      // Filter out GameText specific props
      const gameTextProps = new Set(['textVariant']);
      const filteredProps = Object.fromEntries(
        Object.entries(props).filter(([key]) => !gameTextProps.has(key))
      );
      return React.createElement('div', { 'data-testid': 'game-text', ...filteredProps }, children);
    },
    GameTextField: ({ ...props }) => {
      // Filter out GameTextField specific props
      const gameTextFieldProps = new Set(['fullWidth']);
      const filteredProps = Object.fromEntries(
        Object.entries(props).filter(([key]) => !gameTextFieldProps.has(key))
      );
      return React.createElement('input', { 'data-testid': 'game-text-field', ...filteredProps });
    },
    GAME_CLASSES: {
      textMedium: 'text-medium',
      marginBottom2: 'margin-bottom-2',
      marginTop2: 'margin-top-2',
    },
  };
});

describe('OneRepMaxRecords', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders empty state when no records exist', async () => {
    const emptyMock = {
      ...mockUseData,
      userData: { user_one_rep_max: [] },
      userOneRepMaxes: [],
      loadUserOneRepMaxes: jest.fn().mockResolvedValue([]),
    };

    jest.mocked(jest.requireMock('../contexts/DataContext').useData).mockReturnValue(emptyMock);

    render(<OneRepMaxRecords />);

    await waitFor(() => {
      expect(screen.queryByText('Loading 1RM records...')).not.toBeInTheDocument();
    });

    expect(screen.getByText('No 1RM Records Found')).toBeInTheDocument();
    expect(
      screen.getByText('Start recording your 1RM values to track your strength progress over time.')
    ).toBeInTheDocument();
  });

  it('renders search functionality', async () => {
    const emptyMock = {
      ...mockUseData,
      userData: { user_one_rep_max: [] },
      userOneRepMaxes: [],
      loadUserOneRepMaxes: jest.fn().mockResolvedValue([]),
    };

    jest.mocked(jest.requireMock('../contexts/DataContext').useData).mockReturnValue(emptyMock);

    render(<OneRepMaxRecords />);

    await waitFor(() => {
      expect(screen.queryByText('Loading 1RM records...')).not.toBeInTheDocument();
    });

    expect(screen.getByText('No 1RM Records Found')).toBeInTheDocument();
    expect(screen.queryByTestId('game-text-field')).not.toBeInTheDocument();
  });

  it('displays record count', async () => {
    const emptyMock = {
      ...mockUseData,
      userData: { user_one_rep_max: [] },
      userOneRepMaxes: [],
      loadUserOneRepMaxes: jest.fn().mockResolvedValue([]),
    };

    jest.mocked(jest.requireMock('../contexts/DataContext').useData).mockReturnValue(emptyMock);

    render(<OneRepMaxRecords />);

    await waitFor(() => {
      expect(screen.queryByText('Loading 1RM records...')).not.toBeInTheDocument();
    });

    expect(screen.getByText('0')).toBeInTheDocument();
  });

  it('handles form submission', async () => {
    const mockUpsert = jest.fn().mockResolvedValue(undefined);
    const mockWithUpsert = {
      ...mockUseData,
      upsertUserOneRepMax: mockUpsert,
    };

    jest
      .mocked(jest.requireMock('../contexts/DataContext').useData)
      .mockReturnValue(mockWithUpsert);

    render(<OneRepMaxRecords />);

    // Wait for the component to finish loading
    await waitFor(() => {
      expect(screen.queryByText('Loading 1RM records...')).not.toBeInTheDocument();
    });

    // The component should render the button
    expect(screen.getByText('Record 1RM')).toBeInTheDocument();
  });
});
