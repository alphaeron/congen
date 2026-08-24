import { ChevronLeft, ChevronRight } from '@mui/icons-material';
import { Box, Typography, Chip, IconButton } from '@mui/material';
import { motion } from 'framer-motion';
import React, { useState, useMemo, useRef, useEffect, useCallback } from 'react';

import { GameCard, GameText, GAME_CLASSES } from './GameTheme';
import type { ProgrammedWorkout } from '../api/types';
import { replaceUnderscoresWithSpaces } from '../common/utils';
import { getWeekTimelineStatus } from '../utils/progressUtils';

interface TrainingTimelineWeekWorkout {
  workout: ProgrammedWorkout;
  isCompleted: boolean;
}

interface TrainingTimelineProps {
  weeks: Array<{
    weekNumber: number;
    workouts: TrainingTimelineWeekWorkout[];
    isCompleted: boolean;
    completedWorkouts: number;
    plannedWorkouts?: number;
    completedExercises?: number;
    totalExercises?: number;
  }>;
  onWeekClick: (weekNumber: number) => void;
  currentWeek: number;
}

const WEEK_CARD_MIN_WIDTH = 180;
const WEEK_CARD_GAP = 16;

/**
 * Training Timeline component for displaying workout weeks in a horizontal scrollable format.
 *
 * @param weeks Array of week data with workouts, completion, and exercise progress
 * @param onWeekClick Function to call when a week is clicked
 * @param currentWeek Current active week number
 * @return Training Timeline component
 */
export const TrainingTimeline: React.FC<TrainingTimelineProps> = ({
  weeks,
  onWeekClick,
  currentWeek,
}) => {
  const scrollContainerRef = useRef<HTMLDivElement>(null);
  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(false);

  const sortedWeeks = useMemo(() => {
    return [...weeks].sort((a, b) => b.weekNumber - a.weekNumber);
  }, [weeks]);

  const updateScrollButtons = useCallback(() => {
    const container = scrollContainerRef.current;
    if (!container) {
      setCanScrollLeft(false);
      setCanScrollRight(false);
      return;
    }

    const maxScrollLeft = container.scrollWidth - container.clientWidth;
    setCanScrollLeft(container.scrollLeft > 0);
    setCanScrollRight(container.scrollLeft < maxScrollLeft - 1);
  }, []);

  const handleScroll = (direction: 'left' | 'right') => {
    const container = scrollContainerRef.current;
    if (!container) {
      return;
    }

    const scrollAmount = Math.max(
      container.clientWidth * 0.75,
      WEEK_CARD_MIN_WIDTH + WEEK_CARD_GAP
    );

    container.scrollBy({
      left: direction === 'left' ? -scrollAmount : scrollAmount,
      behavior: 'smooth',
    });
  };

  useEffect(() => {
    const container = scrollContainerRef.current;
    if (!container) {
      return;
    }

    updateScrollButtons();

    const handleScrollEvent = () => {
      updateScrollButtons();
    };

    container.addEventListener('scroll', handleScrollEvent, { passive: true });

    const resizeObserver = new ResizeObserver(() => {
      updateScrollButtons();
    });

    resizeObserver.observe(container);

    return () => {
      container.removeEventListener('scroll', handleScrollEvent);
      resizeObserver.disconnect();
    };
  }, [sortedWeeks, updateScrollButtons]);

  useEffect(() => {
    const container = scrollContainerRef.current;
    if (!container) {
      return;
    }

    const currentWeekElement = container.querySelector(
      `[data-week-number="${currentWeek}"]`
    ) as HTMLElement | null;

    if (currentWeekElement?.scrollIntoView) {
      currentWeekElement.scrollIntoView({
        behavior: 'smooth',
        block: 'nearest',
        inline: 'center',
      });
    }
  }, [currentWeek, sortedWeeks]);

  const getWeekStatus = (week: (typeof weeks)[0]) => {
    return getWeekTimelineStatus(week, currentWeek);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'current':
        return 'var(--game-info)';
      case 'completed':
        return 'var(--game-success)';
      case 'past':
        return 'var(--game-gray-light)';
      default:
        return 'var(--game-warning)';
    }
  };

  if (weeks.length === 0) {
    return (
      <Box
        sx={{
          p: 4,
          textAlign: 'center',
          background: 'linear-gradient(135deg, var(--game-gray-dark) 0%, var(--game-gray) 100%)',
          border: '1px solid var(--game-cyan-border)',
          borderRadius: 2,
          backdropFilter: 'blur(10px)',
        }}
      >
        <Typography variant="body1" sx={{ color: 'var(--game-white-muted)' }}>
          No workouts generated yet. Start your training journey!
        </Typography>
      </Box>
    );
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, ease: 'easeOut' }}
    >
      <GameCard className="glassmorphism-card">
        <Box sx={{ p: 2, borderBottom: '1px solid var(--game-cyan-border)' }}>
          <GameText variant="h6" className={GAME_CLASSES.textMedium}>
            Training Timeline
          </GameText>
        </Box>

        <Box sx={{ position: 'relative', p: 2 }}>
          {canScrollLeft && (
            <IconButton
              aria-label="Scroll timeline left"
              data-testid="timeline-scroll-left"
              onClick={() => handleScroll('left')}
              sx={{
                position: 'absolute',
                left: 8,
                top: '50%',
                transform: 'translateY(-50%)',
                zIndex: 2,
                backgroundColor: 'var(--game-gray-dark)',
                color: 'var(--game-white)',
                boxShadow: 2,
                '&:hover': {
                  backgroundColor: 'var(--game-gray)',
                },
              }}
            >
              <ChevronLeft />
            </IconButton>
          )}

          {canScrollRight && (
            <IconButton
              aria-label="Scroll timeline right"
              data-testid="timeline-scroll-right"
              onClick={() => handleScroll('right')}
              sx={{
                position: 'absolute',
                right: 8,
                top: '50%',
                transform: 'translateY(-50%)',
                zIndex: 2,
                backgroundColor: 'var(--game-gray-dark)',
                color: 'var(--game-white)',
                boxShadow: 2,
                '&:hover': {
                  backgroundColor: 'var(--game-gray)',
                },
              }}
            >
              <ChevronRight />
            </IconButton>
          )}

          <Box
            ref={scrollContainerRef}
            data-testid="timeline-scroll-container"
            sx={{
              overflowX: 'auto',
              overflowY: 'hidden',
              scrollBehavior: 'smooth',
              scrollbarWidth: 'none',
              msOverflowStyle: 'none',
              px: canScrollLeft || canScrollRight ? 5 : 0,
              '&::-webkit-scrollbar': {
                display: 'none',
              },
            }}
          >
            <Box
              sx={{
                display: 'flex',
                gap: `${WEEK_CARD_GAP}px`,
                width: 'max-content',
              }}
            >
              {sortedWeeks.map(week => {
                const status = getWeekStatus(week);
                const isCurrentWeek = week.weekNumber === currentWeek;
                const totalExercises = week.totalExercises ?? 0;
                const completedExercises = week.completedExercises ?? 0;
                const exerciseCompletionPercent =
                  totalExercises > 0
                    ? Math.min(100, Math.round((completedExercises / totalExercises) * 100))
                    : 0;

                return (
                  <motion.div
                    key={week.weekNumber}
                    data-week-number={week.weekNumber}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.3, delay: week.weekNumber * 0.1 }}
                    whileHover={{ filter: 'brightness(1.1)', zIndex: 2 }}
                    whileTap={{ scale: 0.98 }}
                    onClick={() => onWeekClick(week.weekNumber)}
                    style={{
                      minWidth: WEEK_CARD_MIN_WIDTH,
                      flexShrink: 0,
                      padding: 16,
                      background: isCurrentWeek
                        ? 'linear-gradient(135deg, var(--game-info) 0%, var(--game-success) 100%)'
                        : 'var(--game-gray)',
                      border: `2px solid ${getStatusColor(status)}`,
                      borderRadius: 8,
                      cursor: 'pointer',
                    }}
                  >
                    <Box
                      sx={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        gap: 1,
                        mb: 1,
                      }}
                    >
                      <Typography
                        variant="h6"
                        sx={{ color: 'white', fontWeight: 600, flexShrink: 0 }}
                      >
                        Week {week.weekNumber}
                      </Typography>
                      <Chip
                        label={status}
                        size="small"
                        sx={{
                          backgroundColor: getStatusColor(status),
                          color: 'white',
                          fontSize: '0.7rem',
                          height: 20,
                          flexShrink: 0,
                        }}
                      />
                    </Box>

                    <Box sx={{ mb: 1 }}>
                      <Box
                        data-testid={`week-exercise-progress-${week.weekNumber}`}
                        aria-label={`${completedExercises} of ${totalExercises} exercises recorded`}
                        sx={{
                          height: 6,
                          borderRadius: 3,
                          backgroundColor: 'rgba(255,255,255,0.2)',
                          overflow: 'hidden',
                        }}
                      >
                        <Box
                          sx={{
                            width: `${exerciseCompletionPercent}%`,
                            height: '100%',
                            backgroundColor: 'var(--game-cyan)',
                          }}
                        />
                      </Box>
                    </Box>

                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                      {week.workouts.map(({ workout, isCompleted }, index) => (
                        <Box
                          key={workout.id}
                          sx={{ display: 'flex', alignItems: 'center', gap: 1 }}
                        >
                          <motion.div
                            initial={{ scale: 0 }}
                            animate={{ scale: 1 }}
                            transition={{ duration: 0.3, delay: index * 0.1 }}
                          >
                            <Box
                              data-testid={`workout-completion-dot-${workout.id}`}
                              data-completed={isCompleted ? 'true' : 'false'}
                              sx={{
                                width: 6,
                                height: 6,
                                borderRadius: '50%',
                                flexShrink: 0,
                                backgroundColor: isCompleted
                                  ? 'var(--game-cyan)'
                                  : 'var(--game-gray)',
                              }}
                            />
                          </motion.div>
                          <Typography
                            variant="caption"
                            sx={{
                              color: 'var(--game-white-muted)',
                              fontSize: '0.75rem',
                              overflow: 'hidden',
                              textOverflow: 'ellipsis',
                              whiteSpace: 'nowrap',
                            }}
                          >
                            {replaceUnderscoresWithSpaces(workout.name)}
                          </Typography>
                        </Box>
                      ))}
                    </Box>
                  </motion.div>
                );
              })}
            </Box>
          </Box>
        </Box>
      </GameCard>
    </motion.div>
  );
};
