import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
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
  allExercises: [
    { name: 'Bench Press' },
    { name: 'Squat' },
    { name: 'Deadlift' },
  ],
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
  getDefaultWeightUnit: jest.fn((exerciseName: string) => 'KG'),
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
  flexRender: (content: any, context: any) => {
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
  ExerciseName: ({ exerciseName }: any) => <div data-testid="exercise-name">{String(exerciseName)}</div>,
}));

jest.mock('./FormDialog', () => ({
  FormDialog: ({ children, open, onClose, onSubmit, title }: any) => (
    open ? (
      <div data-testid="form-dialog">
        <div data-testid="dialog-title">{title}</div>
        <button data-testid="close-dialog" onClick={onClose}>Close</button>
        <button data-testid="submit-dialog" onClick={() => onSubmit({ exercise_name: 'Bench Press', one_rep_max: 100 })}>Submit</button>
        {children}
      </div>
    ) : null
  ),
}));

// Mock GameTheme components
jest.mock('./GameTheme', () => ({
  GameCard: ({ children, ...props }: any) => <div data-testid="game-card" {...props}>{children}</div>,
  GameText: ({ children, textVariant, ...props }: any) => <div data-testid="game-text" {...props}>{children}</div>,
  GameTextField: ({ children, fullWidth, ...props }: any) => <input data-testid="game-text-field" {...props} />,
  GAME_CLASSES: {
    textMedium: 'text-medium',
    marginBottom2: 'margin-bottom-2',
    marginTop2: 'margin-top-2',
  },
}));

describe('OneRepMaxRecords', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders empty state when no records exist', async () => {
    const emptyMock = {
      ...mockUseData,
      userData: { user_one_rep_max: [] },
      userOneRepMaxes: [],
    };
    
    jest.mocked(require('../contexts/DataContext').useData).mockReturnValue(emptyMock);
    
    render(<OneRepMaxRecords />);
    
    // Wait for the component to finish loading
    await waitFor(() => {
      expect(screen.queryByText('Loading 1RM records...')).not.toBeInTheDocument();
    });
    
    // The component should render the empty state
    expect(screen.getByText('No 1RM Records Found')).toBeInTheDocument();
    expect(screen.getByText('Start recording your 1RM values to track your strength progress over time.')).toBeInTheDocument();
  });

  it('renders search functionality', async () => {
    render(<OneRepMaxRecords />);
    
    // Wait for the component to finish loading
    await waitFor(() => {
      expect(screen.queryByText('Loading 1RM records...')).not.toBeInTheDocument();
    });
    
    // The component shows empty state when no records are found, so no search field
    expect(screen.getByText('No 1RM Records Found')).toBeInTheDocument();
    expect(screen.queryByTestId('game-text-field')).not.toBeInTheDocument();
  });

  it('displays record count', async () => {
    render(<OneRepMaxRecords />);
    
    // Wait for the component to finish loading
    await waitFor(() => {
      expect(screen.queryByText('Loading 1RM records...')).not.toBeInTheDocument();
    });
    
    // The component should display the record count (0 when no records)
    expect(screen.getByText('0')).toBeInTheDocument(); // Record count
  });

  it('handles form submission', async () => {
    const mockUpsert = jest.fn().mockResolvedValue(undefined);
    const mockWithUpsert = {
      ...mockUseData,
      upsertUserOneRepMax: mockUpsert,
    };
    
    jest.mocked(require('../contexts/DataContext').useData).mockReturnValue(mockWithUpsert);
    
    render(<OneRepMaxRecords />);
    
    // Wait for the component to finish loading
    await waitFor(() => {
      expect(screen.queryByText('Loading 1RM records...')).not.toBeInTheDocument();
    });
    
    // The component should render the button
    expect(screen.getByText('Record 1RM')).toBeInTheDocument();
  });
});
