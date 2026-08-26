import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import React from 'react';

import { VolumeTrendChartDialog } from './VolumeTrendChartDialog';
import { createCongenNivoTheme } from '../theme/nivoTheme';

jest.mock('@nivo/line', () => ({
  ResponsiveLine: ({
    tooltip,
    data,
  }: {
    tooltip?: (props: {
      point: { data: { x: string | number; y: number }; seriesColor: string };
    }) => React.ReactNode;
    data?: Array<{ id: string; data: Array<{ x: string; y: number }> }>;
  }) => (
    <div data-testid="responsive-line">
      {tooltip && data?.[0]?.data?.[0] ? (
        <div data-testid="line-tooltip-slot">
          {tooltip({
            point: {
              data: data[0].data[0],
              seriesColor: 'var(--game-cyan)',
            },
          })}
        </div>
      ) : null}
    </div>
  ),
}));

describe('VolumeTrendChartDialog', () => {
  const nivoTheme = createCongenNivoTheme('dark');
  const weekVolumes = [
    {
      weekNumber: 1,
      maxEffortVolume: 1000,
      dynamicEffortVolume: 0,
      accessoryVolume: 0,
      totalVolume: 1000,
      maxEffortProgrammedVolume: 0,
      dynamicEffortProgrammedVolume: 0,
      accessoryProgrammedVolume: 0,
      totalProgrammedVolume: 0,
      performedSets: 1,
      targetSets: 1,
      completedWorkouts: 1,
      plannedWorkouts: 1,
      maxEffortPeakWeightLbs: 300,
      maxEffortPeakReps: 3,
      maxEffortPeakExerciseName: 'Bench Press',
    },
    {
      weekNumber: 2,
      maxEffortVolume: 800,
      dynamicEffortVolume: 0,
      accessoryVolume: 0,
      totalVolume: 800,
      maxEffortProgrammedVolume: 0,
      dynamicEffortProgrammedVolume: 0,
      accessoryProgrammedVolume: 0,
      totalProgrammedVolume: 0,
      performedSets: 1,
      targetSets: 1,
      completedWorkouts: 1,
      plannedWorkouts: 1,
      maxEffortPeakWeightLbs: 280,
      maxEffortPeakReps: 2,
      maxEffortPeakExerciseName: 'Bench Press',
    },
  ];

  it('renders legend and tooltip content when open', () => {
    render(
      <VolumeTrendChartDialog
        open={true}
        category="Max Effort"
        volumeData={[
          { x: 'W1', y: 1000 },
          { x: 'W2', y: 800 },
        ]}
        acwrData={[
          { x: 'W1', y: 0 },
          { x: 'W2', y: 0.8 },
        ]}
        intensityData={[{ x: 'W1', y: 85 }]}
        weekVolumes={weekVolumes}
        nivoTheme={nivoTheme}
        onClose={jest.fn()}
      />
    );

    expect(screen.getByTestId('volume-trend-dialog-Max Effort')).toBeInTheDocument();
    expect(screen.getByTestId('volume-trend-dialog-legend-Max Effort')).toHaveTextContent('Volume');
    const tooltip = screen.getByTestId('volume-trend-dialog-tooltip');
    expect(tooltip).toHaveTextContent(/Week:/);
    expect(tooltip).toHaveTextContent(/Volume:/);
    expect(tooltip).toHaveTextContent(/ACWR:/);
    expect(tooltip).toHaveTextContent(/no data/);
    expect(tooltip).toHaveTextContent(/Intensity:/);
    expect(tooltip).toHaveTextContent(/Peak lift:/);
    expect(tooltip).toHaveTextContent(/Bench Press/);
  });

  it('calls onClose when the close button is clicked', async () => {
    const onClose = jest.fn();
    render(
      <VolumeTrendChartDialog
        open={true}
        category="Max Effort"
        volumeData={[{ x: 'W1', y: 1000 }]}
        acwrData={[{ x: 'W1', y: 0 }]}
        intensityData={[]}
        weekVolumes={weekVolumes.slice(0, 1)}
        nivoTheme={nivoTheme}
        onClose={onClose}
      />
    );

    fireEvent.click(screen.getByTestId('volume-trend-dialog-close-Max Effort'));
    await waitFor(() => {
      expect(onClose).toHaveBeenCalled();
    });
  });
});
