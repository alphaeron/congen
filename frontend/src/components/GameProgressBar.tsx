import { Box, Tooltip } from '@mui/material';
import { styled, alpha } from '@mui/material/styles';
import { motion, useMotionValue, useAnimation } from 'framer-motion';
import React, { useEffect, useRef, useState } from 'react';

import { GameText } from './GameTheme';

/**
 * Shared progress bar components for consistent styling across the game UI.
 */
const ProgressBarContainer = styled(Box)(() => ({
  position: 'relative',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  height: 12,
  minWidth: 200,
  borderRadius: 6,
  backgroundColor: alpha('#ffffff', 0.3),
  overflow: 'hidden',
}));

const ProgressBarFill = styled(Box)<{ percentage: number; color: string }>(
  ({ percentage, color }) => ({
    position: 'absolute',
    left: 0,
    top: 0,
    height: '100%',
    width: `${Math.max(2, percentage)}%`, // Show available portion in blue
    backgroundColor: color,
    borderRadius: 4,
    transition: 'width 0.3s cubic-bezier(0.4, 0, 0.2, 1)', // Matches Congen's transition
  })
);

const ProgressBarText = styled(GameText)(() => ({
  position: 'relative',
  zIndex: 1,
  fontSize: '0.75rem',
  fontWeight: 600, // Matches Congen's font weight
  color: 'white',
  textShadow: '1px 1px 2px rgba(0, 0, 0, 0.7)',
  textAlign: 'center',
  lineHeight: 1,
  fontFamily: '"Inter", "system-ui", "sans-serif"', // Matches Congen's font
}));

const LabelText = styled(GameText)(() => ({
  fontSize: '0.75rem',
  color: '#ffffff',
  textShadow: '0 0 3px #00bcd4',
  textAlign: 'center',
  marginTop: 4,
  fontWeight: 600, // Matches Congen's font weight
  fontFamily: '"Inter", "system-ui", "sans-serif"', // Matches Congen's font
  textTransform: 'uppercase',
}));

interface GameProgressBarProps {
  icon?: React.ReactNode;
  label: string;
  current: number;
  max: number;
  color: string;
  tooltip: string;
  animated?: boolean;
  delay?: number;
}

/**
 * Linear progress bar with centered text overlay
 */
export const GameProgressBar: React.FC<GameProgressBarProps> = ({
  icon,
  label,
  current,
  max,
  color,
  tooltip,
  animated = false,
  delay = 0,
}) => {
  const percentage = max === 0 ? 0 : Math.max(0, Math.min(100, (current / max) * 100));
  const progressValue = useMotionValue(0);
  const controls = useAnimation();
  const ref = useRef<HTMLDivElement>(null);
  const [isInView, setIsInView] = useState(false);

  useEffect(() => {
    if (!animated) return;

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting && !isInView) {
          setIsInView(true);
          const timer = setTimeout(() => {
            // Check if component is still mounted before starting animation
            if (ref.current) {
              controls.start({
                x: 0,
                opacity: 1,
                transition: { duration: 0.25 },
              });
              progressValue.set(1);
            }
          }, delay);

          return () => clearTimeout(timer);
        }
      },
      { threshold: 0.3 }
    );

    if (ref.current) {
      observer.observe(ref.current);
    }

    return () => {
      if (ref.current) {
        observer.unobserve(ref.current);
      }
    };
  }, [animated, controls, progressValue, delay, isInView]);

  const ProgressBarContent = (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: 0.25,
          minWidth: 'auto',
        }}
      >
        {icon}
        <LabelText>{label}</LabelText>
      </Box>
      <Box sx={{ flexGrow: 1 }}>
        <ProgressBarContainer>
          {animated ? (
            <motion.div
              style={{
                position: 'absolute',
                left: 0,
                top: 0,
                height: '100%',
                backgroundColor: color,
                borderRadius: 4,
              }}
              initial={{ width: '0%' }}
              animate={
                isInView
                  ? {
                      width: `${Math.max(2, percentage)}%`,
                    }
                  : {
                      width: '0%',
                    }
              }
              transition={{
                duration: 1.2,
                delay: delay / 1000,
                ease: [0.25, 0.46, 0.45, 0.94],
              }}
            />
          ) : (
            <ProgressBarFill percentage={percentage} color={color} />
          )}
          {animated ? (
            <motion.div
              style={{
                position: 'relative',
                zIndex: 1,
                fontSize: '0.75rem',
                fontWeight: 600,
                color: 'white',
                textShadow: '1px 1px 2px rgba(0, 0, 0, 0.7)',
                textAlign: 'center',
                lineHeight: 1,
                fontFamily: '"Inter", "system-ui", "sans-serif"',
              }}
            >
              <motion.span
                initial={{ opacity: 0 }}
                animate={
                  isInView
                    ? {
                        opacity: 1,
                      }
                    : {
                        opacity: 0,
                      }
                }
                transition={{
                  duration: 0.3,
                  delay: delay / 1000 + 0.8,
                }}
              >
                {Math.round(current)}/{Math.round(max)}
              </motion.span>
            </motion.div>
          ) : (
            <ProgressBarText>
              {current.toFixed(0)}/{max.toFixed(0)}
            </ProgressBarText>
          )}
        </ProgressBarContainer>
      </Box>
    </Box>
  );

  if (animated) {
    return (
      <motion.div
        ref={ref}
        initial={{ opacity: 0, x: -15 }}
        animate={controls}
        style={{ display: 'flex', alignItems: 'center', gap: 8 }}
      >
        <Tooltip title={tooltip}>{ProgressBarContent}</Tooltip>
      </motion.div>
    );
  }

  return <Tooltip title={tooltip}>{ProgressBarContent}</Tooltip>;
};

const CircularProgressContainer = styled(Box)(() => ({
  position: 'relative',
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'center',
  width: 90,
  height: 90,
}));

const CircularProgressWrapper = styled(Box)(() => ({
  position: 'relative',
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  width: 75,
  height: 75,
}));

const CircularProgressText = styled(GameText)(() => ({
  position: 'absolute',
  fontSize: '0.6rem',
  fontWeight: 600, // Matches Congen's font weight
  color: 'white',
  textAlign: 'center',
  lineHeight: 1,
  fontFamily: '"Inter", "system-ui", "sans-serif"', // Matches Congen's font
}));

/**
 * Circular progress bar with centered values and label
 */
export const GameCircularProgressBar: React.FC<GameProgressBarProps> = ({
  icon,
  label,
  current,
  max,
  color,
  tooltip,
  animated = false,
  delay = 0,
}) => {
  const percentage = max === 0 ? 0 : Math.max(0, Math.min(100, (current / max) * 100));
  const progressValue = useMotionValue(0);
  const controls = useAnimation();
  const ref = useRef<HTMLDivElement>(null);
  const [isInView, setIsInView] = useState(false);

  useEffect(() => {
    if (!animated) return;

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting && !isInView) {
          setIsInView(true);
          const timer = setTimeout(() => {
            // Check if component is still mounted before starting animation
            if (ref.current) {
              controls.start({
                x: 0,
                opacity: 1,
                transition: { duration: 0.25 },
              });
              progressValue.set(1);
            }
          }, delay);

          return () => clearTimeout(timer);
        }
      },
      { threshold: 0.3 }
    );

    if (ref.current) {
      observer.observe(ref.current);
    }

    return () => {
      if (ref.current) {
        observer.unobserve(ref.current);
      }
    };
  }, [animated, controls, progressValue, delay, isInView]);

  const circumference = 2 * Math.PI * 30;

  const CircularProgressContent = (
    <Box>
      <CircularProgressContainer>
        <CircularProgressWrapper>
          <Box
            sx={{
              position: 'relative',
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              width: 75,
              height: 75,
            }}
          >
            <svg width={75} height={75} viewBox="0 0 75 75" style={{ transform: 'rotate(90deg)' }}>
              <circle
                cx="37.5"
                cy="37.5"
                r="30"
                fill="none"
                stroke={alpha('#ffffff', 0.2)}
                strokeWidth="5"
                strokeDasharray="1.5,1.5"
              />
              {animated ? (
                <motion.circle
                  cx="37.5"
                  cy="37.5"
                  r="30"
                  fill="none"
                  stroke={color}
                  strokeWidth="5"
                  strokeLinecap="round"
                  strokeDasharray={circumference}
                  initial={{ strokeDashoffset: -circumference }}
                  animate={
                    isInView
                      ? {
                          strokeDashoffset: -circumference * (1 - percentage / 100),
                        }
                      : {
                          strokeDashoffset: -circumference,
                        }
                  }
                  transition={{
                    duration: 1.2,
                    delay: delay / 1000,
                    ease: [0.25, 0.46, 0.45, 0.94],
                  }}
                />
              ) : (
                <circle
                  cx="37.5"
                  cy="37.5"
                  r="30"
                  fill="none"
                  stroke={color}
                  strokeWidth="5"
                  strokeLinecap="round"
                  strokeDasharray={`${2 * Math.PI * 30}`}
                  strokeDashoffset={`-${2 * Math.PI * 30 * (1 - percentage / 100)}`}
                  style={{
                    transition: 'stroke-dashoffset 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                  }}
                />
              )}
            </svg>
            {animated ? (
              <motion.div
                style={{
                  position: 'absolute',
                  fontSize: '0.6rem',
                  fontWeight: 600,
                  color: 'white',
                  textAlign: 'center',
                  lineHeight: 1,
                  fontFamily: '"Inter", "system-ui", "sans-serif"',
                }}
                initial={{ opacity: 0 }}
                animate={
                  isInView
                    ? {
                        opacity: 1,
                      }
                    : {
                        opacity: 0,
                      }
                }
                transition={{
                  duration: 0.3,
                  delay: delay / 1000 + 0.8,
                }}
              >
                {Math.round(current)}/{Math.round(max)}
              </motion.div>
            ) : (
              <CircularProgressText>
                {current.toFixed(0)}/{max.toFixed(0)}
              </CircularProgressText>
            )}
          </Box>
        </CircularProgressWrapper>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 0.5 }}>
          {icon && icon}
          <LabelText>{label}</LabelText>
        </Box>
      </CircularProgressContainer>
    </Box>
  );

  if (animated) {
    return (
      <motion.div ref={ref} initial={{ opacity: 0, x: 15 }} animate={controls}>
        <Tooltip title={tooltip}>{CircularProgressContent}</Tooltip>
      </motion.div>
    );
  }

  return <Tooltip title={tooltip}>{CircularProgressContent}</Tooltip>;
};
