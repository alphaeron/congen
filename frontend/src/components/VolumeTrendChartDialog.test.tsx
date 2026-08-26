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

  it('renders legend and tooltip content when open', () => {
    render(
      <VolumeTrendChartDialog
        open={true}
        category="Max Effort"
        volumeData={[{ x: 'W1', y: 1000 }]}
        acwrData={[{ x: 'W1', y: 1.1 }]}
        intensityData={[{ x: 'W1', y: 85 }]}
        nivoTheme={nivoTheme}
        onClose={jest.fn()}
      />
    );

    expect(screen.getByTestId('volume-trend-dialog-Max Effort')).toBeInTheDocument();
    expect(screen.getByTestId('volume-trend-dialog-legend-Max Effort')).toHaveTextContent(
      'Volume'
    );
    expect(screen.getByTestId('volume-trend-dialog-tooltip')).toHaveTextContent(/Week:/);
    expect(screen.getByTestId('volume-trend-dialog-tooltip')).toHaveTextContent(/Volume:/);
    expect(screen.getByTestId('volume-trend-dialog-tooltip')).toHaveTextContent(/ACWR:/);
    expect(screen.getByTestId('volume-trend-dialog-tooltip')).toHaveTextContent(/Intensity:/);
  });

  it('calls onClose when the close button is clicked', async () => {
    const onClose = jest.fn();
    render(
      <VolumeTrendChartDialog
        open={true}
        category="Max Effort"
        volumeData={[{ x: 'W1', y: 1000 }]}
        acwrData={[]}
        intensityData={[]}
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
