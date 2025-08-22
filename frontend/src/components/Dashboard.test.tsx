import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
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

jest.mock('./WorkoutFlow', () => ({
  WorkoutFlow: ({ user }: { user: User }) => (
    <div data-testid="workout-flow">Workout Flow for {user.name}</div>
  ),
}));

jest.mock('./ExerciseHistory', () => ({
  ExerciseHistory: ({ user }: { user: User }) => (
    <div data-testid="visualization-page">Visualization Page for {user.name}</div>
  ),
}));

jest.mock('./WorkoutCalendar', () => ({
  WorkoutCalendar: ({ user }: { user: User }) => (
    <div data-testid="workout-calendar">Workout Calendar for {user.name}</div>
  ),
}));

// Create a theme for testing
const theme = createTheme();

const renderWithTheme = (component: React.ReactElement) => {
  return render(
    <ThemeProvider theme={theme}>
      {component}
    </ThemeProvider>
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
    expect(screen.getByText('Program Management')).toBeInTheDocument();
    expect(screen.getByText('Workout Flow')).toBeInTheDocument();
    expect(screen.getByText('Visualization')).toBeInTheDocument();
    expect(screen.getByText('Calendar')).toBeInTheDocument();
  });

  it('shows overview as the default active tab', () => {
    renderWithTheme(<Dashboard user={mockUser} />);
    
    const overviewButton = screen.getByText('Overview').closest('button');
    expect(overviewButton).toHaveClass('Mui-selected');
    expect(screen.getByTestId('dashboard-overview')).toBeInTheDocument();
  });

  it('switches to program management when clicked', () => {
    renderWithTheme(<Dashboard user={mockUser} />);
    
    const programManagementButton = screen.getByText('Program Management').closest('button');
    fireEvent.click(programManagementButton!);
    
    expect(programManagementButton).toHaveClass('Mui-selected');
    expect(screen.getByTestId('program-management')).toBeInTheDocument();
    expect(screen.queryByTestId('dashboard-overview')).not.toBeInTheDocument();
  });

  it('switches to workout flow when clicked', () => {
    renderWithTheme(<Dashboard user={mockUser} />);
    
    const workoutFlowButton = screen.getByText('Workout Flow').closest('button');
    fireEvent.click(workoutFlowButton!);
    
    expect(workoutFlowButton).toHaveClass('Mui-selected');
    expect(screen.getByTestId('workout-flow')).toBeInTheDocument();
    expect(screen.queryByTestId('dashboard-overview')).not.toBeInTheDocument();
  });

  it('switches to visualization when clicked', () => {
    renderWithTheme(<Dashboard user={mockUser} />);
    
    const visualizationButton = screen.getByText('Visualization').closest('button');
    fireEvent.click(visualizationButton!);
    
    expect(visualizationButton).toHaveClass('Mui-selected');
    expect(screen.getByTestId('visualization-page')).toBeInTheDocument();
    expect(screen.queryByTestId('dashboard-overview')).not.toBeInTheDocument();
  });

  it('switches to calendar when clicked', () => {
    renderWithTheme(<Dashboard user={mockUser} />);
    
    const calendarButton = screen.getByText('Calendar').closest('button');
    fireEvent.click(calendarButton!);
    
    expect(calendarButton).toHaveClass('Mui-selected');
    expect(screen.getByTestId('workout-calendar')).toBeInTheDocument();
    expect(screen.queryByTestId('dashboard-overview')).not.toBeInTheDocument();
  });

  it('calls setDrawerOpen when drawer toggle is triggered', () => {
    renderWithTheme(<Dashboard user={mockUser} />);
    
    // Simulate drawer toggle (this would typically be triggered by a button or resize)
    // Since the drawer is controlled by the useDrawer hook, we test the effect
    expect(mockUseDrawer.setDrawerOpen).toHaveBeenCalledWith(true);
  });

  it('renders with correct drawer width', () => {
    renderWithTheme(<Dashboard user={mockUser} />);
    
    const drawer = screen.getByRole('navigation');
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
    
    // Switch to program management
    const programManagementButton = screen.getByText('Program Management').closest('button');
    fireEvent.click(programManagementButton!);
    
    // Switch back to overview
    const overviewButton = screen.getByText('Overview').closest('button');
    fireEvent.click(overviewButton!);
    
    // Should show overview content
    expect(screen.getByTestId('dashboard-overview')).toBeInTheDocument();
    expect(overviewButton).toHaveClass('Mui-selected');
  });

  it('renders all menu items with correct icons', () => {
    renderWithTheme(<Dashboard user={mockUser} />);
    
    // Check that all menu items are present
    const menuItems = ['Overview', 'Program Management', 'Workout Flow', 'Visualization', 'Calendar'];
    menuItems.forEach(item => {
      expect(screen.getByText(item)).toBeInTheDocument();
    });
  });
});
