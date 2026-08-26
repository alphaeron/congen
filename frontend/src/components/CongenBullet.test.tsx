import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';

import {
  CongenBullet,
  buildCongenBulletRanges,
  resolveCongenBulletDomain,
} from './CongenBullet';
import { setupResizeObserverMock } from '../testUtils/setupResizeObserverMock';

beforeAll(() => {
  setupResizeObserverMock();
});

describe('CongenBullet helpers', () => {
  it('builds exactly four bands from four cumulative ends', () => {
    const bands = buildCongenBulletRanges(
      [3100, 3500, 4025, 4700],
      0,
      4700,
      ['#a', '#b', '#c', '#d']
    );

    expect(bands).toHaveLength(4);
    expect(bands[0]).toMatchObject({ v0: 0, v1: 3100, color: '#a' });
    expect(bands[1]).toMatchObject({ v0: 3100, v1: 3500, color: '#b' });
    expect(bands[2]).toMatchObject({ v0: 3500, v1: 4025, color: '#c' });
    expect(bands[3]).toMatchObject({ v0: 4025, v1: 4700, color: '#d' });
  });

  it('does not invent a trailing band when ends already reach max', () => {
    const bands = buildCongenBulletRanges([100, 200, 300, 400], 0, 400, [
      '#1',
      '#2',
      '#3',
      '#4',
      '#5',
    ]);
    expect(bands).toHaveLength(4);
    expect(bands[3].v1).toBe(400);
  });

  it('resolves an explicit domain without expansion', () => {
    expect(
      resolveCongenBulletDomain(
        [{ id: 'x', ranges: [1, 2, 3], measures: [1] }],
        0,
        4700
      )
    ).toEqual({ minValue: 0, maxValue: 4700 });
  });
});

describe('CongenBullet', () => {
  it('renders four ranges, measure, markers, and explicit axis ticks', () => {
    render(
      <div style={{ width: 320, height: 88 }}>
        <CongenBullet
          data={[
            {
              id: 'Max Effort',
              ranges: [3100, 3500, 4025, 4700],
              measures: [1200],
              markers: [3500],
            },
          ]}
          minValue={0}
          maxValue={4700}
          rangeColors={['#5c2b2b', '#5c4a1f', '#1f4d3a', '#6b2a2a']}
          axisBottom={{
            tickValues: [0, 3100, 3500, 4025, 4700],
            format: value => `${value}`,
          }}
        />
      </div>
    );

    expect(screen.getByTestId('congen-bullet')).toBeInTheDocument();
    expect(screen.getByTestId('congen-bullet-range-Max Effort-0')).toBeInTheDocument();
    expect(screen.getByTestId('congen-bullet-range-Max Effort-1')).toBeInTheDocument();
    expect(screen.getByTestId('congen-bullet-range-Max Effort-2')).toBeInTheDocument();
    expect(screen.getByTestId('congen-bullet-range-Max Effort-3')).toBeInTheDocument();
    expect(screen.queryByTestId('congen-bullet-range-Max Effort-4')).not.toBeInTheDocument();
    expect(screen.getByTestId('congen-bullet-measure-Max Effort')).toBeInTheDocument();
    expect(screen.getByTestId('congen-bullet-marker-Max Effort-0')).toBeInTheDocument();
    expect(screen.getByTestId('congen-bullet-axis')).toBeInTheDocument();
    expect(screen.getAllByTestId(/congen-bullet-tick-/)).toHaveLength(5);
    const axis = screen.getByTestId('congen-bullet-axis');
    const tickLines = axis.querySelectorAll('line');
    const verticalTicks = Array.from(tickLines).filter(line => line.getAttribute('x1') === '0');
    expect(verticalTicks).toHaveLength(5);
    const lengths = verticalTicks.map(line => Number(line.getAttribute('y2')));
    expect(new Set(lengths).size).toBe(1);
    expect(lengths[0]).toBeGreaterThanOrEqual(12);
    const labelYs = Array.from(screen.getAllByTestId(/congen-bullet-tick-/)).map(node =>
      Number(node.getAttribute('y'))
    );
    expect(new Set(labelYs).size).toBe(1);
  });

  it('shows tooltip content on range hover', () => {
    render(
      <div style={{ width: 320, height: 88 }}>
        <CongenBullet
          data={[
            {
              id: 'row',
              ranges: [100, 200, 300, 400],
              measures: [50],
            },
          ]}
          minValue={0}
          maxValue={400}
          tooltip={() => <div data-testid="custom-tip">tip</div>}
        />
      </div>
    );

    fireEvent.mouseEnter(screen.getByTestId('congen-bullet-range-row-0'));
    expect(screen.getByTestId('congen-bullet-tooltip')).toBeInTheDocument();
    expect(screen.getByTestId('custom-tip')).toBeInTheDocument();
  });
});
