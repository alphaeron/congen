import { Box } from '@mui/material';
import React, { useEffect, useMemo, useRef, useState } from 'react';

/**
 * Single bullet row datum matching the @nivo/bullet shape.
 */
export interface CongenBulletDatum {
  id: string | number;
  title?: React.ReactNode;
  ranges: number[];
  measures: number[];
  markers?: number[];
}

/**
 * Bottom axis configuration for exact tick placement.
 */
export interface CongenBulletAxisConfig {
  tickValues?: number[];
  format?: (value: number) => string;
  tickSize?: number;
  tickPadding?: number;
}

export interface CongenBulletMargin {
  top: number;
  right: number;
  bottom: number;
  left: number;
}

export interface CongenBulletTooltipProps {
  v0: number;
  v1?: number;
  color: string;
}

export interface CongenBulletProps {
  data: CongenBulletDatum[];
  margin?: Partial<CongenBulletMargin>;
  minValue?: number;
  maxValue?: number | 'auto';
  rangeColors?: string[];
  measureColors?: string[];
  markerColors?: string[];
  measureSize?: number;
  markerSize?: number;
  axisBottom?: CongenBulletAxisConfig | null;
  tooltip?: React.ComponentType<CongenBulletTooltipProps> | (() => React.ReactNode);
  role?: string;
}

const DEFAULT_MARGIN: CongenBulletMargin = {
  top: 10,
  right: 8,
  bottom: 40,
  left: 8,
};

type ComputedRange = {
  index: number;
  v0: number;
  v1: number;
  color: string;
};

/**
 * Builds contiguous range bands from cumulative end values.
 * Domain is exact — no d3.nice expansion and no phantom trailing band.
 *
 * @param rangeEnds Cumulative range end values
 * @param minValue Scale minimum
 * @param maxValue Scale maximum
 * @param colors Palette indexed by band order
 * @returns Computed bands clipped to the domain
 */
export function buildCongenBulletRanges(
  rangeEnds: number[],
  minValue: number,
  maxValue: number,
  colors: string[]
): ComputedRange[] {
  const ends = [...rangeEnds]
    .filter(value => Number.isFinite(value))
    .map(value => Math.min(Math.max(value, minValue), maxValue))
    .sort((a, b) => a - b);

  const uniqueEnds: number[] = [];
  ends.forEach(value => {
    if (!uniqueEnds.includes(value)) {
      uniqueEnds.push(value);
    }
  });

  if (uniqueEnds.length === 0) {
    return [
      {
        index: 0,
        v0: minValue,
        v1: maxValue,
        color: colors[0] ?? '#444444',
      },
    ];
  }

  const bands: ComputedRange[] = [];
  let previous = minValue;
  uniqueEnds.forEach((end, index) => {
    if (end <= previous) {
      return;
    }
    bands.push({
      index,
      v0: previous,
      v1: end,
      color: colors[index % colors.length] ?? '#444444',
    });
    previous = end;
  });

  return bands;
}

/**
 * Resolves the chart domain from explicit max or data extents.
 *
 * @param data Bullet rows
 * @param minValue Explicit minimum
 * @param maxValue Explicit maximum or auto
 * @returns Domain bounds
 */
export function resolveCongenBulletDomain(
  data: CongenBulletDatum[],
  minValue: number,
  maxValue: number | 'auto'
): { minValue: number; maxValue: number } {
  if (maxValue !== 'auto' && Number.isFinite(maxValue)) {
    return { minValue, maxValue: Math.max(minValue + 1, maxValue) };
  }

  let peak = minValue + 1;
  data.forEach(datum => {
    datum.ranges.forEach(value => {
      peak = Math.max(peak, value);
    });
    datum.measures.forEach(value => {
      peak = Math.max(peak, value);
    });
    (datum.markers ?? []).forEach(value => {
      peak = Math.max(peak, value);
    });
  });

  return { minValue, maxValue: peak };
}

function pickColor(colors: string[] | undefined, index: number, fallback: string): string {
  if (!colors?.length) {
    return fallback;
  }
  return colors[index % colors.length] ?? fallback;
}

/**
 * Congen bullet chart with a Nivo-compatible data API plus explicit axis ticks.
 * Uses an exact linear domain (no nice-rounding) so band boundaries stay honest.
 *
 * @param props Bullet configuration
 * @returns Responsive SVG bullet chart
 */
export const CongenBullet: React.FC<CongenBulletProps> = ({
  data,
  margin: marginProp,
  minValue = 0,
  maxValue = 'auto',
  rangeColors = ['#5c2b2b', '#5c4a1f', '#1f4d3a', '#6b2a2a'],
  measureColors = ['var(--game-cyan)'],
  markerColors = ['var(--game-cyan)'],
  measureSize = 0.45,
  markerSize = 0.75,
  axisBottom = null,
  tooltip,
  role = 'img',
}) => {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const [size, setSize] = useState({ width: 0, height: 0 });
  const [tooltipState, setTooltipState] = useState<{
    x: number;
    y: number;
    content: React.ReactNode;
  } | null>(null);

  const margin: CongenBulletMargin = {
    ...DEFAULT_MARGIN,
    ...marginProp,
  };

  useEffect(() => {
    const node = containerRef.current;
    if (!node) {
      return;
    }
    const updateSize = (width: number, height: number) => {
      setSize(previous =>
        previous.width === width && previous.height === height ? previous : { width, height }
      );
    };
    const rect = node.getBoundingClientRect();
    updateSize(rect.width, rect.height);
    const observer = new ResizeObserver(entries => {
      const entry = entries[0];
      if (entry) {
        updateSize(entry.contentRect.width, entry.contentRect.height);
      }
    });
    observer.observe(node);
    return () => {
      observer.disconnect();
    };
  }, []);

  const domain = useMemo(
    () => resolveCongenBulletDomain(data, minValue, maxValue),
    [data, minValue, maxValue]
  );

  const innerWidth = Math.max(0, size.width - margin.left - margin.right);
  const plotHeight = Math.max(0, size.height - margin.top - margin.bottom);

  const scaleX = (value: number): number => {
    if (domain.maxValue <= domain.minValue) {
      return 0;
    }
    const fraction = (value - domain.minValue) / (domain.maxValue - domain.minValue);
    return Math.min(innerWidth, Math.max(0, fraction * innerWidth));
  };

  const hideTooltip = () => setTooltipState(null);

  const showTooltip = (event: React.MouseEvent, content: React.ReactNode) => {
    const node = containerRef.current;
    if (!node) {
      return;
    }
    const rect = node.getBoundingClientRect();
    setTooltipState({
      x: event.clientX - rect.left + 12,
      y: event.clientY - rect.top - 12,
      content,
    });
  };

  const renderTooltipContent = (props: CongenBulletTooltipProps): React.ReactNode => {
    if (!tooltip) {
      return null;
    }
    if (typeof tooltip === 'function') {
      return React.createElement(tooltip as React.FC<CongenBulletTooltipProps>, props);
    }
    const TooltipComponent = tooltip;
    return <TooltipComponent {...props} />;
  };

  const tickValues = useMemo(() => {
    if (!axisBottom?.tickValues?.length) {
      return [domain.minValue, domain.maxValue];
    }
    const unique: number[] = [];
    axisBottom.tickValues.forEach(value => {
      const clamped = Math.min(Math.max(value, domain.minValue), domain.maxValue);
      if (!unique.includes(clamped)) {
        unique.push(clamped);
      }
    });
    return unique.sort((a, b) => a - b);
  }, [axisBottom?.tickValues, domain.minValue, domain.maxValue]);

  const formatTick = (value: number): string => {
    if (axisBottom?.format) {
      return axisBottom.format(value);
    }
    return String(Math.round(value));
  };

  return (
    <Box
      ref={containerRef}
      sx={{ width: '100%', height: '100%', position: 'relative', overflow: 'visible' }}
      data-testid="congen-bullet"
      role={role}
      onMouseLeave={hideTooltip}
    >
      {size.width > 0 && size.height > 0 ? (
        <svg width={size.width} height={size.height}>
          {data.map((datum, rowIndex) => {
            const rowY = margin.top + rowIndex * (plotHeight + 8);
            const bands = buildCongenBulletRanges(
              datum.ranges,
              domain.minValue,
              domain.maxValue,
              rangeColors
            );
            const measure = Math.min(
              domain.maxValue,
              Math.max(domain.minValue, datum.measures[0] ?? 0)
            );
            const measureHeight = Math.max(2, plotHeight * measureSize);
            const measureY = rowY + (plotHeight - measureHeight) / 2;
            const markerHeight = Math.max(4, plotHeight * markerSize);
            const markerY = rowY + (plotHeight - markerHeight) / 2;

            return (
              <g key={String(datum.id)} data-testid={`congen-bullet-row-${datum.id}`}>
                {bands.map(band => {
                  const x = scaleX(band.v0);
                  const width = Math.max(0, scaleX(band.v1) - x);
                  return (
                    <rect
                      key={`range-${band.index}`}
                      x={margin.left + x}
                      y={rowY}
                      width={width}
                      height={plotHeight}
                      fill={band.color}
                      data-testid={`congen-bullet-range-${datum.id}-${band.index}`}
                      onMouseEnter={event =>
                        showTooltip(
                          event,
                          renderTooltipContent({
                            v0: band.v0,
                            v1: band.v1,
                            color: band.color,
                          })
                        )
                      }
                      onMouseMove={event =>
                        showTooltip(
                          event,
                          renderTooltipContent({
                            v0: band.v0,
                            v1: band.v1,
                            color: band.color,
                          })
                        )
                      }
                    />
                  );
                })}

                <rect
                  x={margin.left + scaleX(domain.minValue)}
                  y={measureY}
                  width={Math.max(0, scaleX(measure) - scaleX(domain.minValue))}
                  height={measureHeight}
                  fill={pickColor(measureColors, 0, 'var(--game-cyan)')}
                  data-testid={`congen-bullet-measure-${datum.id}`}
                  onMouseEnter={event =>
                    showTooltip(
                      event,
                      renderTooltipContent({
                        v0: measure,
                        color: pickColor(measureColors, 0, 'var(--game-cyan)'),
                      })
                    )
                  }
                  onMouseMove={event =>
                    showTooltip(
                      event,
                      renderTooltipContent({
                        v0: measure,
                        color: pickColor(measureColors, 0, 'var(--game-cyan)'),
                      })
                    )
                  }
                />

                {(datum.markers ?? []).map((marker, markerIndex) => {
                  const clamped = Math.min(domain.maxValue, Math.max(domain.minValue, marker));
                  const x = margin.left + scaleX(clamped);
                  const color = pickColor(markerColors, markerIndex, 'var(--game-cyan)');
                  return (
                    <line
                      key={`marker-${markerIndex}`}
                      x1={x}
                      x2={x}
                      y1={markerY}
                      y2={markerY + markerHeight}
                      stroke={color}
                      strokeWidth={4}
                      data-testid={`congen-bullet-marker-${datum.id}-${markerIndex}`}
                      onMouseEnter={event =>
                        showTooltip(
                          event,
                          renderTooltipContent({
                            v0: clamped,
                            color,
                          })
                        )
                      }
                      onMouseMove={event =>
                        showTooltip(
                          event,
                          renderTooltipContent({
                            v0: clamped,
                            color,
                          })
                        )
                      }
                    />
                  );
                })}
              </g>
            );
          })}

          {axisBottom && (
            <g
              transform={`translate(0, ${margin.top + plotHeight})`}
              data-testid="congen-bullet-axis"
            >
              <line
                x1={margin.left}
                x2={margin.left + innerWidth}
                y1={0}
                y2={0}
                stroke="var(--game-white-muted)"
                strokeWidth={1}
              />
              {tickValues.map((value, index) => {
                const x = margin.left + scaleX(value);
                const isFirst = index === 0;
                const isLast = index === tickValues.length - 1;
                const textAnchor = isFirst ? 'start' : isLast ? 'end' : 'middle';
                const tickSize = axisBottom.tickSize ?? 14;
                const tickPadding = axisBottom.tickPadding ?? 6;
                const labelY = tickSize + tickPadding;

                return (
                  <g key={`tick-${value}`} transform={`translate(${x},0)`}>
                    <line
                      x1={0}
                      x2={0}
                      y1={0}
                      y2={tickSize}
                      stroke="var(--game-white-muted)"
                      strokeWidth={1.5}
                    />
                    <text
                      x={0}
                      y={labelY}
                      textAnchor={textAnchor}
                      dominantBaseline="hanging"
                      fill="var(--game-white-muted)"
                      fontSize="0.7rem"
                      data-testid={`congen-bullet-tick-${index}`}
                    >
                      {formatTick(value)}
                    </text>
                  </g>
                );
              })}
            </g>
          )}
        </svg>
      ) : null}

      {tooltipState ? (
        <Box
          sx={{
            position: 'absolute',
            left: tooltipState.x,
            top: tooltipState.y,
            zIndex: 2000,
            pointerEvents: 'none',
            transform: 'translateY(-100%)',
          }}
          data-testid="congen-bullet-tooltip"
        >
          {tooltipState.content}
        </Box>
      ) : null}
    </Box>
  );
};
