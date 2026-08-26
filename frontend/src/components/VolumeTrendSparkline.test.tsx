import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';

import {
  getVolumeSparklineAriaLabel,
  VolumeTrendSparkline,
} from './VolumeTrendSparkline';
import { createCongenNivoTheme } from '../theme/nivoTheme';

jest.mock('@nivo/line', () => ({
  ResponsiveLine: () => <div data-testid="responsive-line">Mock Line</div>,
}));

describe('getVolumeSparklineAriaLabel', () => {
  it('describes expand action without a delta', () => {
    expect(getVolumeSparklineAriaLabel('Max Effort', null)).toContain('Click to expand');
  });

  it('includes week-over-week direction when delta is present', () => {
    expect(getVolumeSparklineAriaLabel('Max Effort', 12)).toContain('up 12 percent');
    expect(getVolumeSparklineAriaLabel('Max Effort', -8)).toContain('down 8 percent');
  });
});

describe('VolumeTrendSparkline', () => {
  it('invokes onClick when activated', () => {
    const onClick = jest.fn();
    render(
      <VolumeTrendSparkline
        data={[{ x: 'W1', y: 100 }]}
        nivoTheme={createCongenNivoTheme('dark')}
        ariaLabel="Max Effort weekly volume trend. Click to expand."
        onClick={onClick}
      />
    );

    fireEvent.click(screen.getByTestId('volume-trend-sparkline'));
    expect(onClick).toHaveBeenCalledTimes(1);
  });
});
