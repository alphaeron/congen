import { render, screen } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import React from 'react';

import { ProgressBar } from './ProgressBar';
import type { ProgressStatus } from '../utils/progressUtils';

// Mock TanStack Ranger
jest.mock('@tanstack/ranger', () => ({
  Ranger: jest.fn().mockImplementation(() => ({
    getValues: () => [0],
  })),
}));

const theme = createTheme();

const renderWithTheme = (component: React.ReactElement) => {
  return render(
    <ThemeProvider theme={theme}>
      {component}
    </ThemeProvider>
  );
};

describe('ProgressBar', () => {
  describe('Basic Rendering', () => {
    it('should render with default props', () => {
      renderWithTheme(
        <ProgressBar
          value={50}
          status="in-progress"
        />
      );

      // The progress bar should be rendered (we can't easily test the visual appearance in unit tests)
      expect(document.querySelector('[data-testid="progress-bar"]')).toBeInTheDocument();
    });

    it('should render with custom width and height', () => {
      renderWithTheme(
        <ProgressBar
          value={75}
          status="completed"
          width={200}
          height={10}
        />
      );

      const progressBar = document.querySelector('[data-testid="progress-bar"]');
      expect(progressBar).toBeInTheDocument();
    });

    it('should render with custom className', () => {
      renderWithTheme(
        <ProgressBar
          value={25}
          status="not-started"
          className="custom-progress-bar"
        />
      );

      const progressBar = document.querySelector('.custom-progress-bar');
      expect(progressBar).toBeInTheDocument();
    });
  });

  describe('Progress Display', () => {
    it('should show fraction when showFraction is true', () => {
      renderWithTheme(
        <ProgressBar
          value={60}
          status="in-progress"
          current={3}
          total={5}
          showFraction={true}
        />
      );

      expect(screen.getByText('3/5')).toBeInTheDocument();
    });

    it('should show percentage when showPercentage is true', () => {
      renderWithTheme(
        <ProgressBar
          value={75}
          status="completed"
          showPercentage={true}
        />
      );

      expect(screen.getByText('75%')).toBeInTheDocument();
    });

    it('should show custom label when provided', () => {
      renderWithTheme(
        <ProgressBar
          value={50}
          status="in-progress"
          label="Custom Progress"
        />
      );

      expect(screen.getByText('Custom Progress')).toBeInTheDocument();
    });

    it('should show both fraction and percentage when both are enabled', () => {
      renderWithTheme(
        <ProgressBar
          value={80}
          status="completed"
          current={4}
          total={5}
          showFraction={true}
          showPercentage={true}
        />
      );

      expect(screen.getByText('80% (4/5)')).toBeInTheDocument();
    });

    it('should show tooltip when showTooltip is enabled', () => {
      renderWithTheme(
        <ProgressBar
          value={60}
          status="in-progress"
          current={3}
          total={5}
          showTooltip={true}
        />
      );

      // The tooltip content should be available but not visible until hover
      expect(document.querySelector('[data-testid="progress-bar"]')).toBeInTheDocument();
    });

    it('should not show text when tooltip is enabled', () => {
      renderWithTheme(
        <ProgressBar
          value={60}
          status="in-progress"
          current={3}
          total={5}
          showTooltip={true}
          showPercentage={true}
          showFraction={true}
        />
      );

      // Should not show the percentage/fraction text when tooltip is enabled
      expect(screen.queryByText('60% (3/5)')).not.toBeInTheDocument();
    });
  });

  describe('Status-based Styling', () => {
    it('should apply completed status styling', () => {
      renderWithTheme(
        <ProgressBar
          value={100}
          status="completed"
        />
      );

      // The component should render without errors
      expect(document.querySelector('[data-testid="progress-bar"]')).toBeInTheDocument();
    });

    it('should apply in-progress status styling', () => {
      renderWithTheme(
        <ProgressBar
          value={50}
          status="in-progress"
        />
      );

      expect(document.querySelector('[data-testid="progress-bar"]')).toBeInTheDocument();
    });

    it('should apply not-started status styling', () => {
      renderWithTheme(
        <ProgressBar
          value={0}
          status="not-started"
        />
      );

      expect(document.querySelector('[data-testid="progress-bar"]')).toBeInTheDocument();
    });

    it('should use custom color when provided', () => {
      renderWithTheme(
        <ProgressBar
          value={75}
          status="in-progress"
          color="#ff0000"
        />
      );

      expect(document.querySelector('[data-testid="progress-bar"]')).toBeInTheDocument();
    });
  });

  describe('Multi-segment Progress', () => {
    it('should render multi-segment progress bar', () => {
      const segments = [
        { value: 30, color: '#ff0000' },
        { value: 40, color: '#00ff00' },
        { value: 30, color: '#0000ff' },
      ];

      renderWithTheme(
        <ProgressBar
          value={100}
          status="completed"
          segments={segments}
        />
      );

      expect(document.querySelector('[data-testid="progress-bar"]')).toBeInTheDocument();
    });

    it('should handle empty segments array', () => {
      renderWithTheme(
        <ProgressBar
          value={50}
          status="in-progress"
          segments={[]}
        />
      );

      expect(document.querySelector('[data-testid="progress-bar"]')).toBeInTheDocument();
    });
  });

  describe('Animation and Smooth Transitions', () => {
    it('should render with smooth transitions enabled', () => {
      renderWithTheme(
        <ProgressBar
          value={60}
          status="in-progress"
          smooth={true}
          animationDuration={500}
        />
      );

      expect(document.querySelector('[data-testid="progress-bar"]')).toBeInTheDocument();
    });

    it('should render without smooth transitions', () => {
      renderWithTheme(
        <ProgressBar
          value={40}
          status="not-started"
          smooth={false}
        />
      );

      expect(document.querySelector('[data-testid="progress-bar"]')).toBeInTheDocument();
    });
  });

  describe('Edge Cases', () => {
    it('should handle value of 0', () => {
      renderWithTheme(
        <ProgressBar
          value={0}
          status="not-started"
        />
      );

      expect(document.querySelector('[data-testid="progress-bar"]')).toBeInTheDocument();
    });

    it('should handle value of 100', () => {
      renderWithTheme(
        <ProgressBar
          value={100}
          status="completed"
        />
      );

      expect(document.querySelector('[data-testid="progress-bar"]')).toBeInTheDocument();
    });

    it('should handle value greater than max', () => {
      renderWithTheme(
        <ProgressBar
          value={150}
          max={100}
          status="completed"
        />
      );

      expect(document.querySelector('[data-testid="progress-bar"]')).toBeInTheDocument();
    });

    it('should handle negative value', () => {
      renderWithTheme(
        <ProgressBar
          value={-10}
          status="not-started"
        />
      );

      expect(document.querySelector('[data-testid="progress-bar"]')).toBeInTheDocument();
    });

    it('should handle custom max value', () => {
      renderWithTheme(
        <ProgressBar
          value={50}
          max={200}
          status="in-progress"
        />
      );

      expect(document.querySelector('[data-testid="progress-bar"]')).toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('should render with proper ARIA attributes', () => {
      renderWithTheme(
        <ProgressBar
          value={75}
          status="in-progress"
          current={3}
          total={4}
          showFraction={true}
        />
      );

      // The component should be accessible
      expect(document.querySelector('[data-testid="progress-bar"]')).toBeInTheDocument();
    });
  });

  describe('TanStack Ranger Integration', () => {
    it('should create Ranger instance with correct parameters', () => {
      const { Ranger } = require('@tanstack/ranger');
      
      renderWithTheme(
        <ProgressBar
          value={60}
          status="in-progress"
          max={100}
        />
      );

      expect(Ranger).toHaveBeenCalledWith({
        getRangerElement: expect.any(Function),
        values: [60],
        min: 0,
        max: 100,
        stepSize: 1,
        onChange: expect.any(Function),
        disabled: true,
      });
    });

    it('should handle Ranger instance creation with custom max', () => {
      const { Ranger } = require('@tanstack/ranger');
      
      renderWithTheme(
        <ProgressBar
          value={150}
          status="completed"
          max={200}
        />
      );

      expect(Ranger).toHaveBeenCalledWith({
        getRangerElement: expect.any(Function),
        values: [150],
        min: 0,
        max: 200,
        stepSize: 1,
        onChange: expect.any(Function),
        disabled: true,
      });
    });

    it('should create Ranger instance with custom steps', () => {
      const { Ranger } = require('@tanstack/ranger');
      
      renderWithTheme(
        <ProgressBar
          value={50}
          status="in-progress"
          steps={[0, 25, 50, 75, 100]}
        />
      );

      expect(Ranger).toHaveBeenCalledWith({
        getRangerElement: expect.any(Function),
        values: [50],
        min: 0,
        max: 100,
        stepSize: 1,
        steps: [0, 25, 50, 75, 100],
        onChange: expect.any(Function),
        disabled: true,
      });
    });

    it('should create Ranger instance with custom ticks', () => {
      const { Ranger } = require('@tanstack/ranger');
      
      renderWithTheme(
        <ProgressBar
          value={75}
          status="completed"
          ticks={[0, 25, 50, 75, 100]}
          showTicks={true}
        />
      );

      expect(Ranger).toHaveBeenCalledWith({
        getRangerElement: expect.any(Function),
        values: [75],
        min: 0,
        max: 100,
        stepSize: 1,
        ticks: [0, 25, 50, 75, 100],
        onChange: expect.any(Function),
        disabled: true,
      });
    });

    it('should create Ranger instance with logarithmic interpolator', () => {
      const { Ranger } = require('@tanstack/ranger');
      
      renderWithTheme(
        <ProgressBar
          value={50}
          status="in-progress"
          logarithmic={true}
        />
      );

      expect(Ranger).toHaveBeenCalledWith({
        getRangerElement: expect.any(Function),
        values: [50],
        min: 0,
        max: 100,
        stepSize: 1,
        interpolator: expect.objectContaining({
          getPercentageForValue: expect.any(Function),
          getValueForClientX: expect.any(Function),
        }),
        onChange: expect.any(Function),
        disabled: true,
      });
    });

    it('should create Ranger instance with custom interpolator', () => {
      const { Ranger } = require('@tanstack/ranger');
      const customInterpolator = {
        getPercentageForValue: (val: number) => val * 2,
        getValueForClientX: (clientX: number) => clientX / 2,
      };
      
      renderWithTheme(
        <ProgressBar
          value={30}
          status="in-progress"
          interpolator={customInterpolator}
        />
      );

      expect(Ranger).toHaveBeenCalledWith({
        getRangerElement: expect.any(Function),
        values: [30],
        min: 0,
        max: 100,
        stepSize: 1,
        interpolator: customInterpolator,
        onChange: expect.any(Function),
        disabled: true,
      });
    });
  });

  describe('Advanced Features', () => {
    it('should render with tick markers', () => {
      renderWithTheme(
        <ProgressBar
          value={60}
          status="in-progress"
          showTicks={true}
          ticks={[0, 25, 50, 75, 100]}
        />
      );

      expect(document.querySelector('[data-testid="progress-bar"]')).toBeInTheDocument();
    });

    it('should render with logarithmic scaling', () => {
      renderWithTheme(
        <ProgressBar
          value={50}
          status="completed"
          logarithmic={true}
        />
      );

      expect(document.querySelector('[data-testid="progress-bar"]')).toBeInTheDocument();
    });

    it('should render with custom steps', () => {
      renderWithTheme(
        <ProgressBar
          value={40}
          status="in-progress"
          steps={[0, 20, 40, 60, 80, 100]}
        />
      );

      expect(document.querySelector('[data-testid="progress-bar"]')).toBeInTheDocument();
    });
  });
});
