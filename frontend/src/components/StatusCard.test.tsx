import { ThemeProvider, createTheme } from '@mui/material/styles';
import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';

import { StatusCard, StatusIndicator, StatusProgress, StatusLevel } from './StatusCard';

// Create a theme for testing
const theme = createTheme();

const renderWithTheme = (component: React.ReactElement) => {
  return render(
    <ThemeProvider theme={theme}>{component}</ThemeProvider>
  );
};

describe('StatusCard', () => {
  const mockOnClick = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders with excellent status', () => {
    renderWithTheme(
      <StatusCard
        title="Test Status"
        status="excellent"
        value={95}
        unit="%"
        description="Test description"
      />
    );

    expect(screen.getByText('Test Status')).toBeInTheDocument();
    expect(screen.getByText('Excellent')).toBeInTheDocument();
    expect(screen.getByText('95')).toBeInTheDocument();
    expect(screen.getByText('%')).toBeInTheDocument();
    expect(screen.getByText('Test description')).toBeInTheDocument();
  });

  it('renders with good status', () => {
    renderWithTheme(
      <StatusCard
        title="Good Status"
        status="good"
        value={85}
        unit="score"
      />
    );

    expect(screen.getByText('Good Status')).toBeInTheDocument();
    expect(screen.getByText('Good')).toBeInTheDocument();
    expect(screen.getByText('85')).toBeInTheDocument();
    expect(screen.getByText('score')).toBeInTheDocument();
  });

  it('renders with fair status', () => {
    renderWithTheme(
      <StatusCard
        title="Fair Status"
        status="fair"
        value={65}
        unit="%"
      />
    );

    expect(screen.getByText('Fair Status')).toBeInTheDocument();
    expect(screen.getByText('Fair')).toBeInTheDocument();
    expect(screen.getByText('65')).toBeInTheDocument();
  });

  it('renders with needs_attention status', () => {
    renderWithTheme(
      <StatusCard
        title="Needs Attention"
        status="needs_attention"
        value={30}
        unit="%"
      />
    );

    expect(screen.getAllByText('Needs Attention')).toHaveLength(2); // Title and chip
    expect(screen.getByText('30')).toBeInTheDocument();
  });

  it('renders with trend indicators', () => {
    renderWithTheme(
      <StatusCard
        title="Trending Status"
        status="good"
        value={80}
        unit="%"
        trend="up"
      />
    );

    expect(screen.getByText('↗ Improving')).toBeInTheDocument();
  });

  it('renders with down trend', () => {
    renderWithTheme(
      <StatusCard
        title="Declining Status"
        status="fair"
        value={60}
        unit="%"
        trend="down"
      />
    );

    expect(screen.getByText('↘ Declining')).toBeInTheDocument();
  });

  it('renders with stable trend', () => {
    renderWithTheme(
      <StatusCard
        title="Stable Status"
        status="good"
        value={75}
        unit="%"
        trend="stable"
      />
    );

    expect(screen.getByText('→ Stable')).toBeInTheDocument();
  });

  it('renders with last updated timestamp', () => {
    const lastUpdated = new Date('2024-01-15T10:30:00Z');
    renderWithTheme(
      <StatusCard
        title="Timestamped Status"
        status="good"
        value={80}
        unit="%"
        lastUpdated={lastUpdated}
      />
    );

    expect(screen.getByText('Updated: 1/15/2024')).toBeInTheDocument();
  });

  it('handles click events when onClick is provided', () => {
    renderWithTheme(
      <StatusCard
        title="Clickable Status"
        status="good"
        value={80}
        unit="%"
        onClick={mockOnClick}
      />
    );

    const card = screen.getByText('Clickable Status').closest('.MuiCard-root');
    expect(card).toBeInTheDocument();
    
    fireEvent.click(card!);
    expect(mockOnClick).toHaveBeenCalledTimes(1);
  });

  it('renders without click cursor when onClick is not provided', () => {
    renderWithTheme(
      <StatusCard
        title="Non-clickable Status"
        status="good"
        value={80}
        unit="%"
      />
    );

    const card = screen.getByText('Non-clickable Status').closest('.MuiCard-root');
    expect(card).toHaveStyle('cursor: default');
  });

  it('renders with children content', () => {
    renderWithTheme(
      <StatusCard
        title="Status with Children"
        status="good"
        value={80}
        unit="%"
      >
        <div data-testid="child-content">Child content</div>
      </StatusCard>
    );

    expect(screen.getByTestId('child-content')).toBeInTheDocument();
    expect(screen.getByText('Child content')).toBeInTheDocument();
  });

  it('renders without value when not provided', () => {
    renderWithTheme(
      <StatusCard
        title="Status without Value"
        status="good"
        description="No value provided"
      />
    );

    expect(screen.getByText('Status without Value')).toBeInTheDocument();
    expect(screen.getByText('Good')).toBeInTheDocument();
    expect(screen.getByText('No value provided')).toBeInTheDocument();
    // Should not render value or unit
    expect(screen.queryByText('80')).not.toBeInTheDocument();
  });
});

describe('StatusIndicator', () => {
  it('renders with small size', () => {
    renderWithTheme(
      <StatusIndicator status="excellent" size="small" />
    );

    const indicator = screen.getByTestId('CheckCircleIcon');
    expect(indicator).toBeInTheDocument();
  });

  it('renders with medium size by default', () => {
    renderWithTheme(
      <StatusIndicator status="good" />
    );

    const indicator = screen.getByTestId('CheckCircleIcon');
    expect(indicator).toBeInTheDocument();
  });

  it('renders with large size', () => {
    renderWithTheme(
      <StatusIndicator status="fair" size="large" />
    );

    const indicator = screen.getByTestId('WarningIcon');
    expect(indicator).toBeInTheDocument();
  });

  it('renders with label when showLabel is true', () => {
    renderWithTheme(
      <StatusIndicator status="needs_attention" showLabel={true} />
    );

    expect(screen.getByText('Needs Attention')).toBeInTheDocument();
  });

  it('renders without label when showLabel is false', () => {
    renderWithTheme(
      <StatusIndicator status="excellent" showLabel={false} />
    );

    expect(screen.queryByText('Excellent')).not.toBeInTheDocument();
  });

  it('renders correct icons for each status', () => {
    const { rerender } = renderWithTheme(
      <StatusIndicator status="excellent" />
    );
    expect(screen.getByTestId('CheckCircleIcon')).toBeInTheDocument();

    rerender(
      <ThemeProvider theme={theme}>
        <StatusIndicator status="good" />
      </ThemeProvider>
    );
    expect(screen.getByTestId('CheckCircleIcon')).toBeInTheDocument();

    rerender(
      <ThemeProvider theme={theme}>
        <StatusIndicator status="fair" />
      </ThemeProvider>
    );
    expect(screen.getByTestId('WarningIcon')).toBeInTheDocument();

    rerender(
      <ThemeProvider theme={theme}>
        <StatusIndicator status="needs_attention" />
      </ThemeProvider>
    );
    expect(screen.getByTestId('ErrorIcon')).toBeInTheDocument();
  });
});

describe('StatusProgress', () => {
  it('renders progress bar with value', () => {
    renderWithTheme(
      <StatusProgress
        value={75}
        status="good"
        label="Test Progress"
        showValue={true}
      />
    );

    expect(screen.getByText('Test Progress')).toBeInTheDocument();
    expect(screen.getByText('75%')).toBeInTheDocument();
  });

  it('renders progress bar without value when showValue is false', () => {
    renderWithTheme(
      <StatusProgress
        value={60}
        status="fair"
        label="Hidden Value Progress"
        showValue={false}
      />
    );

    expect(screen.getByText('Hidden Value Progress')).toBeInTheDocument();
    expect(screen.queryByText('60%')).not.toBeInTheDocument();
  });

  it('renders progress bar without label when not provided', () => {
    renderWithTheme(
      <StatusProgress
        value={90}
        status="excellent"
        showValue={true}
      />
    );

    expect(screen.getByText('90%')).toBeInTheDocument();
    expect(screen.queryByText('Test Progress')).not.toBeInTheDocument();
  });

  it('clamps value between 0 and 100', () => {
    const { rerender } = renderWithTheme(
      <StatusProgress
        value={-10}
        status="needs_attention"
        showValue={true}
      />
    );

    expect(screen.getByText('0%')).toBeInTheDocument();

    rerender(
      <ThemeProvider theme={theme}>
        <StatusProgress
          value={150}
          status="excellent"
          showValue={true}
        />
      </ThemeProvider>
    );

    expect(screen.getByText('100%')).toBeInTheDocument();
  });

  it('renders with correct status colors', () => {
    const { rerender } = renderWithTheme(
      <StatusProgress
        value={95}
        status="excellent"
        showValue={true}
      />
    );

    // Check that the percentage is displayed correctly
    expect(screen.getByText('95%')).toBeInTheDocument();

    rerender(
      <ThemeProvider theme={theme}>
        <StatusProgress
          value={30}
          status="needs_attention"
          showValue={true}
        />
      </ThemeProvider>
    );

    expect(screen.getByText('30%')).toBeInTheDocument();
  });
});
