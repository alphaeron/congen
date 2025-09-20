import { ThemeProvider, createTheme } from '@mui/material/styles';
import { Ranger } from '@tanstack/ranger';
import { render, screen, act } from '@testing-library/react';
import React from 'react';

import { ProgressBar } from './ProgressBar';

// Mock TanStack Ranger with a simple mock that doesn't require constructor
jest.mock('@tanstack/ranger', () => ({
  Ranger: jest.fn().mockImplementation(() => ({
    getValues: () => [0],
  })),
}));

const theme = createTheme();

const renderWithTheme = (component: React.ReactElement) => {
  return act(() => {
    return render(<ThemeProvider theme={theme}>{component}</ThemeProvider>);
  });
};

describe('ProgressBar', () => {
  describe('Basic Rendering', () => {
    it('should render with default props', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={50} status="in-progress" />);
      });

      const progressBar = screen.getByRole('progressbar');
      expect(progressBar).toBeInTheDocument();
      expect(progressBar).toHaveAttribute('aria-valuenow', '50');
      expect(progressBar).toHaveAttribute('aria-valuemin', '0');
      expect(progressBar).toHaveAttribute('aria-valuemax', '100');
    });

    it('should render with custom max value', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={75} status="completed" max={200} />);
      });

      const progressBar = screen.getByRole('progressbar');
      expect(progressBar).toHaveAttribute('aria-valuenow', '75');
      expect(progressBar).toHaveAttribute('aria-valuemax', '200');
    });

    it('should render with custom status', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={25} status="error" />);
      });

      const progressBar = screen.getByRole('progressbar');
      expect(progressBar).toBeInTheDocument();
    });

    it('should render with label', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={60} status="in-progress" label="Loading..." />);
      });

      expect(screen.getByText('Loading...')).toBeInTheDocument();
    });

    it('should render with percentage display', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={33} status="in-progress" showPercentage={true} />);
      });

      expect(screen.getByText('33%')).toBeInTheDocument();
    });
  });

  describe('Status Variants', () => {
    it('should render with in-progress status', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={30} status="in-progress" />);
      });

      const progressBar = screen.getByRole('progressbar');
      expect(progressBar).toBeInTheDocument();
    });

    it('should render with completed status', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={100} status="completed" />);
      });

      const progressBar = screen.getByRole('progressbar');
      expect(progressBar).toBeInTheDocument();
    });

    it('should render with error status', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={50} status="error" />);
      });

      const progressBar = screen.getByRole('progressbar');
      expect(progressBar).toBeInTheDocument();
    });

    it('should render with warning status', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={75} status="warning" />);
      });

      const progressBar = screen.getByRole('progressbar');
      expect(progressBar).toBeInTheDocument();
    });
  });

  describe('Size Variants', () => {
    it('should render with small size', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={50} status="in-progress" size="small" />);
      });

      const progressBar = screen.getByRole('progressbar');
      expect(progressBar).toBeInTheDocument();
    });

    it('should render with medium size', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={50} status="in-progress" size="medium" />);
      });

      const progressBar = screen.getByRole('progressbar');
      expect(progressBar).toBeInTheDocument();
    });

    it('should render with large size', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={50} status="in-progress" size="large" />);
      });

      const progressBar = screen.getByRole('progressbar');
      expect(progressBar).toBeInTheDocument();
    });
  });

  describe('Animation and Smooth Transitions', () => {
    it('should render with smooth transitions enabled', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={60} status="in-progress" smooth={true} />);
      });

      const progressBar = screen.getByRole('progressbar');
      expect(progressBar).toBeInTheDocument();
    });

    it('should render without smooth transitions', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={60} status="in-progress" smooth={false} />);
      });

      const progressBar = screen.getByRole('progressbar');
      expect(progressBar).toBeInTheDocument();
    });
  });

  describe('Edge Cases', () => {
    it('should handle value of 0', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={0} status="in-progress" />);
      });

      const progressBar = screen.getByRole('progressbar');
      expect(progressBar).toHaveAttribute('aria-valuenow', '0');
    });

    it('should handle value of 100', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={100} status="completed" />);
      });

      const progressBar = screen.getByRole('progressbar');
      expect(progressBar).toHaveAttribute('aria-valuenow', '100');
    });

    it('should handle value greater than max', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={150} status="completed" max={100} />);
      });

      const progressBar = screen.getByRole('progressbar');
      expect(progressBar).toHaveAttribute('aria-valuenow', '150');
    });

    it('should handle negative value', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={-10} status="error" />);
      });

      const progressBar = screen.getByRole('progressbar');
      expect(progressBar).toHaveAttribute('aria-valuenow', '-10');
    });

    it('should handle custom max value', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={50} status="in-progress" max={200} />);
      });

      const progressBar = screen.getByRole('progressbar');
      expect(progressBar).toHaveAttribute('aria-valuemax', '200');
    });
  });

  describe('Accessibility', () => {
    it('should have proper ARIA attributes', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={75} status="in-progress" />);
      });

      const progressBar = screen.getByRole('progressbar');
      expect(progressBar).toHaveAttribute('aria-valuenow', '75');
      expect(progressBar).toHaveAttribute('aria-valuemin', '0');
      expect(progressBar).toHaveAttribute('aria-valuemax', '100');
    });

    it('should have proper ARIA label when provided', async () => {
      await act(async () => {
        renderWithTheme(
          <ProgressBar value={50} status="in-progress" ariaLabel="Custom progress" />
        );
      });

      const progressBar = screen.getByRole('progressbar');
      expect(progressBar).toHaveAttribute('aria-label', 'Custom progress');
    });
  });

  describe('Custom Styling', () => {
    it('should render with custom className', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={50} status="in-progress" className="custom-class" />);
      });

      const progressBar = screen.getByRole('progressbar');
      expect(progressBar).toHaveClass('custom-class');
    });

    it('should render with custom style', async () => {
      await act(async () => {
        renderWithTheme(
          <ProgressBar value={50} status="in-progress" style={{ backgroundColor: 'red' }} />
        );
      });

      const progressBar = screen.getByRole('progressbar');
      expect(progressBar).toHaveStyle('background-color: red');
    });
  });

  describe('TanStack Ranger Integration', () => {
    it('should create Ranger instance with correct parameters', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={60} status="in-progress" max={100} />);
      });

      expect(Ranger).toHaveBeenCalledWith({
        values: [60],
        min: 0,
        max: 100,
        stepSize: 1,
        disabled: true,
        getRangerElement: expect.any(Function),
        onChange: expect.any(Function),
      });
    });

    it('should handle Ranger instance creation with custom max', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={150} status="completed" max={200} />);
      });

      expect(Ranger).toHaveBeenCalledWith({
        values: [150],
        min: 0,
        max: 200,
        stepSize: 1,
        disabled: true,
        getRangerElement: expect.any(Function),
        onChange: expect.any(Function),
      });
    });

    it('should create Ranger instance with custom steps', async () => {
      await act(async () => {
        renderWithTheme(
          <ProgressBar value={50} status="in-progress" steps={[0, 25, 50, 75, 100]} />
        );
      });

      expect(Ranger).toHaveBeenCalledWith({
        values: [50],
        min: 0,
        max: 100,
        stepSize: 1,
        steps: [0, 25, 50, 75, 100],
        disabled: true,
        getRangerElement: expect.any(Function),
        onChange: expect.any(Function),
      });
    });

    it('should create Ranger instance with custom ticks', async () => {
      await act(async () => {
        renderWithTheme(
          <ProgressBar
            value={75}
            status="completed"
            ticks={[0, 25, 50, 75, 100]}
            showTicks={true}
          />
        );
      });

      expect(Ranger).toHaveBeenCalledWith({
        values: [75],
        min: 0,
        max: 100,
        stepSize: 1,
        ticks: [0, 25, 50, 75, 100],
        disabled: true,
        getRangerElement: expect.any(Function),
        onChange: expect.any(Function),
      });
    });

    it('should create Ranger instance with logarithmic interpolator', async () => {
      await act(async () => {
        renderWithTheme(<ProgressBar value={50} status="in-progress" logarithmic={true} />);
      });

      expect(Ranger).toHaveBeenCalledWith({
        values: [50],
        min: 0,
        max: 100,
        stepSize: 1,
        interpolator: expect.any(Object),
        disabled: true,
        getRangerElement: expect.any(Function),
        onChange: expect.any(Function),
      });
    });

    it('should create Ranger instance with custom interpolator', async () => {
      const customInterpolator = {
        getPercentageForValue: (val: number) => val * 2,
        getValueForClientX: (clientX: number) => clientX / 2,
      };

      await act(async () => {
        renderWithTheme(
          <ProgressBar value={30} status="in-progress" interpolator={customInterpolator} />
        );
      });

      expect(Ranger).toHaveBeenCalledWith({
        values: [30],
        min: 0,
        max: 100,
        stepSize: 1,
        interpolator: customInterpolator,
        disabled: true,
        getRangerElement: expect.any(Function),
        onChange: expect.any(Function),
      });
    });
  });
});
