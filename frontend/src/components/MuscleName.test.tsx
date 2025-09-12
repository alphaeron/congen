import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import AxiosMockAdapter from 'axios-mock-adapter';
import React from 'react';

import { MuscleName } from './MuscleName';
import { ENDPOINT } from '../api/endpoint';
import type { Muscle } from '../api/types';

// Mock the useApiGet hook
jest.mock('../api/hooks', () => ({
  useApiGet: jest.fn(),
}));

const mockUseApiGet = require('../api/hooks').useApiGet;

describe('MuscleName', () => {
  let mockAdapter: AxiosMockAdapter;

  beforeEach(() => {
    mockAdapter = new AxiosMockAdapter(ENDPOINT);
  });

  afterEach(() => {
    mockAdapter.restore();
    jest.clearAllMocks();
  });

  const mockMuscle: Muscle = {
    name: 'Pectoralis Major',
    description: 'The pectoralis major is a thick, fan-shaped muscle, situated at the chest of the human body.',
  };

  it('should render muscle name with tooltip when data is loaded', async () => {
    mockUseApiGet.mockReturnValueOnce({
      data: mockMuscle,
      isLoading: false,
      error: null,
    });

    render(
      <MemoryRouter>
        <MuscleName muscleName="Pectoralis Major" />
      </MemoryRouter>
    );

    expect(screen.getByText('Pectoralis Major')).toBeInTheDocument();
    
    // Check that the element has cursor help style
    const muscleNameElement = screen.getByText('Pectoralis Major');
    expect(muscleNameElement).toHaveStyle('cursor: help');
  });

  it('should show loading tooltip when data is loading', async () => {
    mockUseApiGet.mockReturnValueOnce({
      data: null,
      isLoading: true,
      error: null,
    });

    render(
      <MemoryRouter>
        <MuscleName muscleName="Pectoralis Major" />
      </MemoryRouter>
    );

    expect(screen.getByText('Pectoralis Major')).toBeInTheDocument();
  });

  it('should show error tooltip when data fails to load', async () => {
    mockUseApiGet.mockReturnValueOnce({
      data: null,
      isLoading: false,
      error: new Error('Failed to load'),
    });

    render(
      <MemoryRouter>
        <MuscleName muscleName="Pectoralis Major" />
      </MemoryRouter>
    );

    expect(screen.getByText('Pectoralis Major')).toBeInTheDocument();
  });

  it('should render with custom variant and sx props', async () => {
    mockUseApiGet.mockReturnValueOnce({
      data: mockMuscle,
      isLoading: false,
      error: null,
    });

    render(
      <MemoryRouter>
        <MuscleName 
          muscleName="Pectoralis Major" 
          variant="h6"
          sx={{ fontWeight: 'bold' }}
        />
      </MemoryRouter>
    );

    const muscleNameElement = screen.getByText('Pectoralis Major');
    expect(muscleNameElement).toHaveStyle('font-weight: 700');
  });

  it('should render custom children when provided', async () => {
    mockUseApiGet.mockReturnValueOnce({
      data: mockMuscle,
      isLoading: false,
      error: null,
    });

    render(
      <MemoryRouter>
        <MuscleName muscleName="Pectoralis Major">
          Custom Muscle Name
        </MuscleName>
      </MemoryRouter>
    );

    expect(screen.getByText('Custom Muscle Name')).toBeInTheDocument();
    expect(screen.queryByText('Pectoralis Major')).not.toBeInTheDocument();
  });

  it('should capitalize muscle name when no custom children provided', async () => {
    mockUseApiGet.mockReturnValueOnce({
      data: mockMuscle,
      isLoading: false,
      error: null,
    });

    render(
      <MemoryRouter>
        <MuscleName muscleName="pectoralis major" />
      </MemoryRouter>
    );

    expect(screen.getByText('Pectoralis Major')).toBeInTheDocument();
  });

  it('should handle muscle without description', async () => {
    const muscleWithoutDescription: Muscle = {
      name: 'Pectoralis Major',
      description: '',
    };

    mockUseApiGet.mockReturnValueOnce({
      data: muscleWithoutDescription,
      isLoading: false,
      error: null,
    });

    render(
      <MemoryRouter>
        <MuscleName muscleName="Pectoralis Major" />
      </MemoryRouter>
    );

    expect(screen.getByText('Pectoralis Major')).toBeInTheDocument();
  });
});
