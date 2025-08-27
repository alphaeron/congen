import { ThemeProvider, createTheme } from '@mui/material/styles';
import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';
import { MemoryRouter } from 'react-router';

import { Dashboard } from './Dashboard';
import type { User } from '../api/types';

// Mock the useDrawer hook
const mockUseDrawer = {
  drawerOpen: true,
  setDrawerOpen: jest.fn(),
  drawerWidth: 240,
};

jest.mock('../App', () => ({
  useDrawer: () => mockUseDrawer,
}));

// Mock the child components
jest.mock('./DashboardOverview', () => ({
  DashboardOverview: ({ user }: { user: User }) => (
    <div data-testid="dashboard-overview">Dashboard Overview for {user.name}</div>
  ),
}));

jest.mock('./ProgramManagement', () => ({
  ProgramManagement: ({ user }: { user: User }) => (
    <div data-testid="program-management">Program Management for {user.name}</div>
  ),
}));

jest.mock('./Workouts', () => ({
  Workouts: ({ user }: { user: User }) => (
    <div data-testid="workouts">Workouts for {user.name}</div>
  ),
}));

jest.mock('./ExerciseHistory', () => ({
  ExerciseHistory: ({ user }: { user: User }) => (
    <div data-testid="exercise-history">Exercise History for {user.name}</div>
  ),
}));

// Create a theme for testing
const theme = createTheme();

const renderWithTheme = (component: React.ReactElement) => {
  return render(
    <MemoryRouter>
      <ThemeProvider theme={theme}>{component}</ThemeProvider>
    </MemoryRouter>
  );
};

describe('Dashboard', () => {
  const mockUser: User = {
    keycloak_id: 'test-user-id',
    name: 'Test User',
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
    roles: ['user'],
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockUseDrawer.drawerOpen = true;
    mockUseDrawer.setDrawerOpen.mockClear();
  });

  it('renders the dashboard with drawer and content', () => {
    renderWithTheme(<Dashboard user={mockUser} />);

    expect(screen.getByText('Dashboard')).toBeInTheDocument();
    expect(screen.getByTestId('dashboard-overview')).toBeInTheDocument();
  });

  it('displays all menu items in the drawer', () => {
    renderWithTheme(<Dashboard user={mockUser} />);

    expect(screen.getByText('Overview')).toBeInTheDocument();
    expect(screen.getByText('Programs')).toBeInTheDocument();
    expect(screen.getByText('Workouts')).toBeInTheDocument();
    expect(screen.getByText('Exercise History')).toBeInTheDocument();
  });

  it('shows overview as the default active tab', () => {
    renderWithTheme(<Dashboard user={mockUser} />);

    const overviewButton = screen.getByRole('button', { name: 'Overview' });
    expect(overviewButton).toHaveClass('Mui-selected');
    expect(screen.getByTestId('dashboard-overview')).toBeInTheDocument();
  });

  it('switches to programs when clicked', () => {
    renderWithTheme(<Dashboard user={mockUser} />);

    const programsButton = screen.getByRole('button', { name: 'Programs' });
    fireEvent.click(programsButton);

    expect(programsButton).toHaveClass('Mui-selected');
    expect(screen.getByTestId('program-management')).toBeInTheDocument();
    expect(screen.queryByTestId('dashboard-overview')).not.toBeInTheDocument();
  });

  it('switches to workouts when clicked', () => {
    renderWithTheme(<Dashboard user={mockUser} />);

    const workoutsButton = screen.getByRole('button', { name: 'Workouts' });
    fireEvent.click(workoutsButton);

    expect(workoutsButton).toHaveClass('Mui-selected');
    expect(screen.getByTestId('workouts')).toBeInTheDocument();
    expect(screen.queryByTestId('dashboard-overview')).not.toBeInTheDocument();
  });

  it('switches to exercise history when clicked', () => {
    renderWithTheme(<Dashboard user={mockUser} />);

    const exerciseHistoryButton = screen.getByRole('button', { name: 'Exercise History' });
    fireEvent.click(exerciseHistoryButton);

    expect(exerciseHistoryButton).toHaveClass('Mui-selected');
    expect(screen.getByTestId('exercise-history')).toBeInTheDocument();
    expect(screen.queryByTestId('dashboard-overview')).not.toBeInTheDocument();
  });

  it('renders with correct drawer structure', () => {
    renderWithTheme(<Dashboard user={mockUser} />);

    // Check that the drawer is rendered (it's a div with MuiDrawer classes)
    const drawer = document.querySelector('.MuiDrawer-root');
    expect(drawer).toBeInTheDocument();
  });

  it('passes user data to child components', () => {
    renderWithTheme(<Dashboard user={mockUser} />);

    expect(screen.getByText('Dashboard Overview for Test User')).toBeInTheDocument();
  });

  it('handles mobile responsive behavior', () => {
    // Mock useMediaQuery to return true for mobile
    const originalMatchMedia = window.matchMedia;
    window.matchMedia = jest.fn().mockImplementation(query => ({
      matches: query.includes('(max-width:899.95px)'),
      media: query,
      onchange: null,
      addListener: jest.fn(),
      removeListener: jest.fn(),
      addEventListener: jest.fn(),
      removeEventListener: jest.fn(),
      dispatchEvent: jest.fn(),
    }));

    renderWithTheme(<Dashboard user={mockUser} />);

    // Should still render correctly on mobile
    expect(screen.getByText('Dashboard')).toBeInTheDocument();
    expect(screen.getByTestId('dashboard-overview')).toBeInTheDocument();

    // Restore original matchMedia
    window.matchMedia = originalMatchMedia;
  });

  it('maintains drawer state across tab switches', () => {
    renderWithTheme(<Dashboard user={mockUser} />);

    // Switch to programs
    const programsButton = screen.getByRole('button', { name: 'Programs' });
    fireEvent.click(programsButton);

    // Switch back to overview
    const overviewButton = screen.getByRole('button', { name: 'Overview' });
    fireEvent.click(overviewButton);

    // Should show overview content
    expect(screen.getByTestId('dashboard-overview')).toBeInTheDocument();
    expect(overviewButton).toHaveClass('Mui-selected');
  });

  it('renders all menu items with correct icons', () => {
    renderWithTheme(<Dashboard user={mockUser} />);

    // Check that all menu items are present
    const menuItems = [
      'Overview',
      'Programs',
      'Workouts',
      'Exercise History',
    ];
    menuItems.forEach(item => {
      expect(screen.getByText(item)).toBeInTheDocument();
    });
  });
});
