import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import AxiosMockAdapter from 'axios-mock-adapter';
import { SnackbarProvider } from 'notistack';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { PhysicalAttributesSection } from './PhysicalAttributesSection';
import { ENDPOINT } from '../api/endpoint';
import type { User } from '../api/types';

// Mock framer-motion
jest.mock('framer-motion', () => ({
  motion: {
    div: 'div',
  },
}));

// Mock the auth context
const mockUser: User = {
  keycloak_id: 'test-user-id',
  name: 'Test User',
  age: 30,
  weight: 180,
  height: 72,
  gender: 'male',
  created_at: new Date('2023-01-01T00:00:00.000Z'),
  updated_at: new Date('2023-01-01T00:00:00.000Z'),
};

jest.mock('../contexts/AuthContext', () => ({
  useAuth: () => ({
    user: mockUser,
  }),
}));

jest.mock('../contexts/DataContext', () => ({
  useData: () => ({
    refreshData: jest.fn(),
    isReady: true,
  }),
}));

const renderWithProviders = (component: React.ReactElement) => {
  return render(
    <MemoryRouter>
      <SnackbarProvider>{component}</SnackbarProvider>
    </MemoryRouter>
  );
};

describe('PhysicalAttributesSection', () => {
  let mockAdapter: AxiosMockAdapter;

  beforeEach(() => {
    mockAdapter = new AxiosMockAdapter(ENDPOINT);
    jest.clearAllMocks();
  });

  afterEach(() => {
    mockAdapter.restore();
  });

  it('should render the component title and description', () => {
    renderWithProviders(<PhysicalAttributesSection />);

    expect(screen.getByText('Physical Attributes')).toBeInTheDocument();
    expect(
      screen.getByText(/Manage your physical attributes for personalized workout recommendations/)
    ).toBeInTheDocument();
  });

  it('should render form fields with user data', () => {
    renderWithProviders(<PhysicalAttributesSection />);

    // Check that form fields are rendered with user data (excluding name field)
    expect(screen.getByDisplayValue('30')).toBeInTheDocument();
    expect(screen.getByDisplayValue('180')).toBeInTheDocument();
    expect(screen.getByDisplayValue('72')).toBeInTheDocument();
    expect(screen.getByText('Male')).toBeInTheDocument();
  });

  it('should show save button as enabled when form is valid', () => {
    renderWithProviders(<PhysicalAttributesSection />);

    const saveButton = screen.getByRole('button', { name: /save changes/i });
    expect(saveButton).toBeEnabled();
  });

  it('should enable save button when changes are made', async () => {
    renderWithProviders(<PhysicalAttributesSection />);

    const ageInput = screen.getByDisplayValue('30');

    await act(async () => {
      fireEvent.change(ageInput, { target: { value: '31' } });
    });

    const saveButton = screen.getByRole('button', { name: /save changes/i });
    expect(saveButton).toBeEnabled();
  });

  it('should save changes successfully', async () => {
    const updatedUser = { ...mockUser, age: 31 };
    mockAdapter.onPatch('/user/me').reply(200, updatedUser);
    mockAdapter.onGet('/user/me').reply(200, updatedUser);

    renderWithProviders(<PhysicalAttributesSection />);

    const ageInput = screen.getByDisplayValue('30');

    await act(async () => {
      fireEvent.change(ageInput, { target: { value: '31' } });
    });

    const saveButton = screen.getByRole('button', { name: /save changes/i });

    await act(async () => {
      fireEvent.click(saveButton);
    });

    await waitFor(
      () => {
        expect(screen.getByText('Profile updated successfully')).toBeInTheDocument();
      },
      { timeout: 15000 }
    );

    expect(mockAdapter.history.patch[0].url).toBe('/user/me');
    expect(mockAdapter.history.patch[0].params).toEqual({
      name: 'Test User', // Name is kept unchanged
      age: 31,
      weight: 180,
      height: 72,
      gender: 'male',
    });

    // Verify that the PATCH request was made successfully
    expect(mockAdapter.history.patch).toHaveLength(1);
  }, 20000);

  it('should handle save error gracefully', async () => {
    mockAdapter.onPatch('/user/me').reply(500, { error: 'Internal server error' });

    renderWithProviders(<PhysicalAttributesSection />);

    const ageInput = screen.getByDisplayValue('30');

    await act(async () => {
      fireEvent.change(ageInput, { target: { value: '31' } });
    });

    const saveButton = screen.getByRole('button', { name: /save changes/i });

    await act(async () => {
      fireEvent.click(saveButton);
    });

    await waitFor(() => {
      expect(screen.getByText('Failed to update profile')).toBeInTheDocument();
    });
  });

  it('should show success message after successful save', async () => {
    const updatedUser = { ...mockUser, age: 31 };
    mockAdapter.onPatch('/user/me').reply(200, updatedUser);
    mockAdapter.onGet('/user/me').reply(200, updatedUser);

    renderWithProviders(<PhysicalAttributesSection />);

    const ageInput = screen.getByDisplayValue('30');

    await act(async () => {
      fireEvent.change(ageInput, { target: { value: '31' } });
    });

    const saveButton = screen.getByRole('button', { name: /save changes/i });

    await act(async () => {
      fireEvent.click(saveButton);
    });

    await waitFor(() => {
      expect(screen.getByText('Profile updated successfully')).toBeInTheDocument();
    });
  });

  it('should handle empty string values correctly', async () => {
    const updatedUser = { ...mockUser, age: undefined, weight: undefined };
    mockAdapter.onPatch('/user/me').reply(200, updatedUser);
    mockAdapter.onGet('/user/me').reply(200, updatedUser);

    renderWithProviders(<PhysicalAttributesSection />);

    const ageInput = screen.getByDisplayValue('30');
    const weightInput = screen.getByDisplayValue('180');

    await act(async () => {
      fireEvent.change(ageInput, { target: { value: '' } });
      fireEvent.change(weightInput, { target: { value: '' } });
    });

    const saveButton = screen.getByRole('button', { name: /save changes/i });

    await act(async () => {
      fireEvent.click(saveButton);
    });

    expect(mockAdapter.history.patch[0].params).toEqual({
      name: 'Test User', // Name is kept unchanged
      age: undefined,
      weight: undefined,
      height: 72,
      gender: 'male',
    });
  });
});
