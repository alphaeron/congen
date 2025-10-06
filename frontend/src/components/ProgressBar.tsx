import { Box, useTheme, Tooltip } from '@mui/material';
import { Ranger } from '@tanstack/ranger';
import React, { useMemo, useRef, useEffect, useState } from 'react';

import { GameText } from './GameTheme';
import type { ProgressStatus } from '../utils/progressUtils';

interface ProgressBarProps {
  /** Current progress value (0-100) */
  value: number;
  /** Maximum value for the progress bar */
  max?: number;
  /** Status of the progress (affects color) */
  status?: ProgressStatus;
  /** Width of the progress bar */
  width?: number | string;
  /** Height of the progress bar */
  height?: number;
  /** Show percentage text */
  showPercentage?: boolean;
  /** Show fraction text (e.g., "3/5") */
  showFraction?: boolean;
  /** Current count for fraction display */
  current?: number;
  /** Total count for fraction display */
  total?: number;
  /** Custom label */
  label?: string;
  /** Animation duration in ms */
  animationDuration?: number;
  /** Custom color for the progress bar */
  color?: string;
  /** Show progress segments (for multi-step progress) */
  segments?: Array<{
    value: number;
    color: string;
    label?: string;
  }>;
  /** Enable smooth animations */
  smooth?: boolean;
  /** Custom className */
  className?: string;
  /** Custom steps for non-linear progress */
  steps?: number[];
  /** Custom ticks for visual markers */
  ticks?: number[];
  /** Show tick markers */
  showTicks?: boolean;
  /** Custom interpolator for non-linear scaling */
  interpolator?: {
    getPercentageForValue: (val: number, min: number, max: number) => number;
    getValueForClientX: (clientX: number, trackDims: object, min: number, max: number) => number;
  };
  /** Enable logarithmic scaling */
  logarithmic?: boolean;
  /** Show tooltip with progress details on hover */
  showTooltip?: boolean;
  /** ARIA label for accessibility */
  ariaLabel?: string;
  /** Custom style object */
  style?: React.CSSProperties;
}

export const ProgressBar: React.FC<ProgressBarProps> = ({
  value,
  max = 100,
  status = 'not-started',
  width = 120,
  height = 6,
  showPercentage = false,
  showFraction = false,
  current,
  total,
  label,
  animationDuration = 300,
  color,
  segments = [],
  smooth = true,
  className,
  steps,
  ticks,
  showTicks = false,
  interpolator,
  logarithmic = false,
  showTooltip = false,
  ariaLabel,
  style,
}) => {
  const theme = useTheme();
  const containerRef = useRef<HTMLDivElement>(null);

  // Calculate the actual progress percentage
  const progressPercentage = useMemo(() => {
    const clampedValue = Math.max(0, Math.min(value, max));
    return (clampedValue / max) * 100;
  }, [value, max]);

  // Get the appropriate color based on status
  const getProgressColor = () => {
    if (color) return color;

    switch (status) {
      case 'completed':
        return theme.palette.success.main;
      case 'in-progress':
        return theme.palette.warning.main;
      case 'not-started':
        return theme.palette.primary.main;
      default:
        return theme.palette.primary.main;
    }
  };

  // Create logarithmic interpolator if needed
  const logInterpolator = useMemo(() => {
    if (logarithmic) {
      return {
        getPercentageForValue: (val: number, min: number, max: number) => {
          if (val <= min) return 0;
          if (val >= max) return 100;
          const logMin = Math.log(min || 1);
          const logMax = Math.log(max);
          const logVal = Math.log(val);
          return ((logVal - logMin) / (logMax - logMin)) * 100;
        },
        getValueForClientX: (
          clientX: number,
          trackDims: { width: number },
          min: number,
          max: number
        ) => {
          const percentage = (clientX / trackDims.width) * 100;
          const logMin = Math.log(min || 1);
          const logMax = Math.log(max);
          const logVal = logMin + (percentage / 100) * (logMax - logMin);
          return Math.exp(logVal);
        },
      };
    }
    return undefined;
  }, [logarithmic, max]);

  // Use ranger for flexible progress tracking and range calculations
  const [rangerInstance, setRangerInstance] = useState<Ranger | null>(null);

  useEffect(() => {
    if (containerRef.current) {
      const rangerConfig = {
        getRangerElement: () => containerRef.current,
        values: [value],
        min: 0,
        max: max,
        stepSize: 1,
        onChange: () => {}, // Read-only progress bar
        disabled: true, // Make it read-only
      };

      // Add custom steps if provided
      if (steps && steps.length > 0) {
        rangerConfig.steps = steps;
      }

      // Add custom ticks if provided
      if (ticks && ticks.length > 0) {
        rangerConfig.ticks = ticks;
      }

      // Add custom interpolator if provided
      if (interpolator || logInterpolator) {
        rangerConfig.interpolator = interpolator || logInterpolator;
      }

      const ranger = new Ranger(rangerConfig);
      setRangerInstance(ranger);
    }
  }, [value, max, steps, ticks, interpolator, logInterpolator]);

  // Use ranger for flexible range calculations and positioning
  const getRangerPosition = () => {
    if (rangerInstance && (interpolator || logInterpolator)) {
      // Use custom interpolator for positioning
      const customInterpolator = interpolator || logInterpolator;
      return customInterpolator.getPercentageForValue(value, 0, max);
    }
    return progressPercentage;
  };

  // Get ranger segments for advanced styling
  const getRangerSegments = () => {
    if (rangerInstance && typeof rangerInstance.getSegments === 'function') {
      return rangerInstance.getSegments();
    }
    return [];
  };

  // Get ranger ticks for visual markers
  const getRangerTicks = () => {
    if (rangerInstance && typeof rangerInstance.getTicks === 'function') {
      return rangerInstance.getTicks();
    }
    return [];
  };

  // Render progress segments or single progress bar using ranger
  const renderProgressContent = () => {
    const rangerSegments = getRangerSegments();
    const rangerTicks = getRangerTicks();

    if (segments.length > 0 || rangerSegments.length > 0) {
      // Multi-segment progress bar using ranger for positioning
      return (
        <Box
          ref={containerRef}
          sx={{
            width,
            height,
            position: 'relative',
            overflow: 'hidden',
            borderRadius: height / 2,
            backgroundColor: theme.palette.action.hover,
          }}
        >
          {/* Render custom segments */}
          {segments.map((segment, index) => {
            const segmentWidth = (segment.value / max) * 100;
            const segmentLeft = segments
              .slice(0, index)
              .reduce((acc, seg) => acc + (seg.value / max) * 100, 0);

            return (
              <Box
                key={`custom-${index}`}
                sx={{
                  position: 'absolute',
                  left: `${segmentLeft}%`,
                  width: `${segmentWidth}%`,
                  height: '100%',
                  backgroundColor: segment.color,
                  transition: smooth ? `all ${animationDuration}ms ease` : 'none',
                  borderRadius: height / 2,
                }}
              />
            );
          })}

          {/* Render ranger segments */}
          {rangerSegments.map((segment, index) => (
            <Box
              key={`ranger-${index}`}
              sx={{
                position: 'absolute',
                left: `${segment.left}%`,
                width: `${segment.width}%`,
                height: '100%',
                backgroundColor: getProgressColor(),
                transition: smooth ? `all ${animationDuration}ms ease` : 'none',
                borderRadius: height / 2,
              }}
            />
          ))}

          {/* Render tick markers */}
          {showTicks &&
            rangerTicks.map(tick => (
              <Box
                key={tick.key}
                sx={{
                  position: 'absolute',
                  left: `${tick.percentage}%`,
                  top: '50%',
                  transform: 'translateX(-50%) translateY(-50%)',
                  width: 2,
                  height: height + 4,
                  backgroundColor: theme.palette.text.secondary,
                  opacity: 0.6,
                  borderRadius: 1,
                }}
              />
            ))}
        </Box>
      );
    } else {
      // Single progress bar using ranger for flexible positioning
      return (
        <Box
          ref={containerRef}
          sx={{
            width,
            height,
            backgroundColor: theme.palette.action.hover,
            borderRadius: height / 2,
            overflow: 'hidden',
            position: 'relative',
          }}
        >
          <Box
            sx={{
              position: 'absolute',
              left: 0,
              top: 0,
              width: `${getRangerPosition()}%`,
              height: '100%',
              backgroundColor: getProgressColor(),
              transition: smooth ? `width ${animationDuration}ms ease` : 'none',
              borderRadius: height / 2,
            }}
          />

          {/* Render tick markers for single progress bar */}
          {showTicks &&
            rangerTicks.map(tick => (
              <Box
                key={tick.key}
                sx={{
                  position: 'absolute',
                  left: `${tick.percentage}%`,
                  top: '50%',
                  transform: 'translateX(-50%) translateY(-50%)',
                  width: 2,
                  height: height + 4,
                  backgroundColor: theme.palette.text.secondary,
                  opacity: 0.6,
                  borderRadius: 1,
                }}
              />
            ))}
        </Box>
      );
    }
  };

  // Generate tooltip content
  const getTooltipContent = () => {
    if (!showTooltip) return '';

    let tooltipText = `${Math.round(progressPercentage)}%`;

    if (current !== undefined && total !== undefined) {
      tooltipText += ` (${current}/${total})`;
    }

    return tooltipText;
  };

  // Render text content (only when not using tooltip)
  const renderTextContent = () => {
    if (showTooltip) return null; // Don't show text when using tooltip

    const elements = [];

    if (showPercentage) {
      let percentageText = `${Math.round(progressPercentage)}%`;

      // Add count in parentheses if fraction is enabled
      if (showFraction && current !== undefined && total !== undefined) {
        percentageText += ` (${current}/${total})`;
      }

      elements.push(
        <GameText key="percentage" variant="caption" textVariant="secondary">
          {percentageText}
        </GameText>
      );
    }

    if (!showPercentage && showFraction && current !== undefined && total !== undefined) {
      // Show only fraction if percentage is disabled
      elements.push(
        <GameText key="fraction" variant="caption" textVariant="secondary">
          {current}/{total}
        </GameText>
      );
    }

    return elements.length > 0 ? <React.Fragment>{elements}</React.Fragment> : null;
  };

  const progressContent = (
    <Box
      data-testid="progress-bar"
      className={className}
      role="progressbar"
      aria-valuenow={value}
      aria-valuemin={0}
      aria-valuemax={max}
      aria-label={ariaLabel}
      style={style}
      sx={{ display: 'flex', width: '100%', alignItems: 'center', gap: 0.5 }}
    >
      {renderProgressContent()}
      {renderTextContent()}
      {label && (
        <GameText variant="caption" textVariant="secondary" style={{ marginLeft: '8px' }}>
          {label}
        </GameText>
      )}
    </Box>
  );

  return showTooltip ? (
    <Tooltip title={getTooltipContent()} arrow>
      {progressContent}
    </Tooltip>
  ) : (
    progressContent
  );
};

// Preset progress bar configurations
export const ProgressBarPresets = {
  // Standard progress bar
  standard: (props: Partial<ProgressBarProps>) => (
    <ProgressBar
      width={120}
      height={6}
      showFraction={true}
      smooth={true}
      animationDuration={300}
      {...props}
    />
  ),

  // Compact progress bar for breadcrumbs
  compact: (props: Partial<ProgressBarProps>) => (
    <ProgressBar
      width={100}
      height={4}
      showFraction={true}
      smooth={true}
      animationDuration={200}
      {...props}
    />
  ),

  // Large progress bar for main content
  large: (props: Partial<ProgressBarProps>) => (
    <ProgressBar
      width={200}
      height={8}
      showPercentage={true}
      showFraction={true}
      smooth={true}
      animationDuration={400}
      {...props}
    />
  ),

  // Multi-step progress bar
  multiStep: (props: Partial<ProgressBarProps>) => (
    <ProgressBar
      width={150}
      height={8}
      showPercentage={true}
      smooth={true}
      animationDuration={500}
      {...props}
    />
  ),

  // Logarithmic progress bar for exponential scales
  logarithmic: (props: Partial<ProgressBarProps>) => (
    <ProgressBar
      width={150}
      height={8}
      showPercentage={true}
      showTicks={true}
      logarithmic={true}
      smooth={true}
      animationDuration={400}
      {...props}
    />
  ),

  // Stepped progress bar with custom steps
  stepped: (props: Partial<ProgressBarProps>) => (
    <ProgressBar
      width={180}
      height={8}
      showPercentage={true}
      showTicks={true}
      steps={[0, 25, 50, 75, 100]}
      ticks={[0, 25, 50, 75, 100]}
      smooth={true}
      animationDuration={300}
      {...props}
    />
  ),

  // Multi-segment progress bar with custom segments
  multiSegment: (props: Partial<ProgressBarProps>) => (
    <ProgressBar
      width={200}
      height={10}
      showPercentage={true}
      segments={[
        { value: 30, color: '#ff4444', label: 'Phase 1' },
        { value: 40, color: '#ffaa44', label: 'Phase 2' },
        { value: 30, color: '#44ff44', label: 'Phase 3' },
      ]}
      smooth={true}
      animationDuration={500}
      {...props}
    />
  ),

  // Advanced progress bar with all features
  advanced: (props: Partial<ProgressBarProps>) => (
    <ProgressBar
      width={250}
      height={12}
      showPercentage={true}
      showFraction={true}
      showTicks={true}
      steps={[0, 20, 40, 60, 80, 100]}
      ticks={[0, 20, 40, 60, 80, 100]}
      logarithmic={false}
      smooth={true}
      animationDuration={600}
      {...props}
    />
  ),
};
