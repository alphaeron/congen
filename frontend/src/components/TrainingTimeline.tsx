import { Box, Typography, Chip, IconButton } from '@mui/material';
import { ChevronLeft, ChevronRight } from '@mui/icons-material';
import { motion, AnimatePresence } from 'framer-motion';
import React, { useState, useMemo } from 'react';

import { GameCard, GameText, GAME_CLASSES } from './GameTheme';

import type { ProgrammedWorkout } from '../api/types';
import { replaceUnderscoresWithSpaces } from '../common/utils';

interface TrainingTimelineProps {
  weeks: Array<{
    weekNumber: number;
    workouts: ProgrammedWorkout[];
    isCompleted: boolean;
    completedWorkouts: number;
  }>;
  onWeekClick: (weekNumber: number) => void;
  currentWeek: number;
}

/**
 * Training Timeline component for displaying workout weeks in a horizontal scrollable format.
 *
 * @param weeks Array of week data with workouts and completion status
 * @param onWeekClick Function to call when a week is clicked
 * @param currentWeek Current active week number
 * @return Training Timeline component
 */
export const TrainingTimeline: React.FC<TrainingTimelineProps> = ({
  weeks,
  onWeekClick,
  currentWeek,
}) => {
  const [scrollPosition, setScrollPosition] = useState(0);
  const [isScrolling, setIsScrolling] = useState(false);

  const sortedWeeks = useMemo(() => {
    return [...weeks].sort((a, b) => b.weekNumber - a.weekNumber);
  }, [weeks]);

  const handleScroll = (direction: 'left' | 'right') => {
    setIsScrolling(true);
    const scrollAmount = 200;
    const newPosition = direction === 'left' 
      ? Math.max(0, scrollPosition - scrollAmount)
      : scrollPosition + scrollAmount;
    
    setScrollPosition(newPosition);
    
    setTimeout(() => setIsScrolling(false), 300);
  };

  const getWeekStatus = (week: typeof weeks[0]) => {
    if (week.weekNumber === currentWeek) return 'current';
    if (week.isCompleted) return 'completed';
    if (week.weekNumber < currentWeek) return 'past';
    return 'future';
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'current':
        return 'var(--game-info)'; // Blue
      case 'completed':
        return 'var(--game-success)'; // Green
      case 'past':
        return 'var(--game-gray-light)'; // Gray
      default:
        return 'var(--game-warning)'; // Amber
    }
  };

  const getCompletionDots = (week: typeof weeks[0]) => {
    const totalWorkouts = week.workouts.length;
    const completedWorkouts = week.completedWorkouts || 0;
    const dots = [];
    
    for (let i = 0; i < totalWorkouts; i++) {
      dots.push(
        <motion.div
          key={i}
          initial={{ scale: 0 }}
          animate={{ scale: 1 }}
          transition={{ duration: 0.3, delay: i * 0.1 }}
          whileHover={{ scale: 1.2 }}
        >
          <Box
            sx={{
              width: 6,
              height: 6,
              borderRadius: '50%',
              backgroundColor: i < completedWorkouts ? 'var(--game-cyan)' : 'var(--game-gray)',
            }}
          />
        </motion.div>
      );
    }
    
    return dots;
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
      transition={{ duration: 0.5, ease: "easeOut" }}
    >
      <GameCard className="glassmorphism-card game-overflow-visible">
      {/* Header */}
      <Box sx={{ p: 2, borderBottom: '1px solid var(--game-cyan-border)' }}>
        <GameText variant="h6" className={GAME_CLASSES.textMedium}>
          Training Timeline
        </GameText>
      </Box>

      {/* Timeline Container */}
      <Box sx={{ position: 'relative', p: 2, overflow: 'visible' }}>
        {/* Scroll Buttons */}
        <AnimatePresence>
          {scrollPosition > 0 && (
            <motion.div
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              transition={{ duration: 0.2 }}
              style={{
                position: 'absolute',
                left: 8,
                top: '50%',
                y: '-50%',
                zIndex: 2,
              }}
            >
              <motion.div
                whileHover={{ scale: 1.1 }}
                whileTap={{ scale: 0.95 }}
              >
                <IconButton
                  onClick={() => handleScroll('left')}
                  sx={{
                    backgroundColor: 'var(--game-gray-dark)',
                    color: 'var(--game-white)',
                  }}
                >
                  <ChevronLeft />
                </IconButton>
              </motion.div>
            </motion.div>
          )}

          {scrollPosition < (sortedWeeks.length - 3) * 200 && (
            <motion.div
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 20 }}
              transition={{ duration: 0.2 }}
              style={{
                position: 'absolute',
                right: 8,
                top: '50%',
                y: '-50%',
                zIndex: 2,
              }}
            >
              <motion.div
                whileHover={{ scale: 1.1 }}
                whileTap={{ scale: 0.95 }}
              >
                <IconButton
                  onClick={() => handleScroll('right')}
                  sx={{
                    backgroundColor: 'var(--game-gray-dark)',
                    color: 'var(--game-white)',
                  }}
                >
                  <ChevronRight />
                </IconButton>
              </motion.div>
            </motion.div>
          )}
        </AnimatePresence>

        {/* Weeks Container */}
        <motion.div
          animate={{ x: -scrollPosition }}
          transition={{ duration: 0.3, ease: "easeOut" }}
          style={{
            display: 'flex',
            gap: 16,
            overflow: 'visible',
            scrollbarWidth: 'none',
            msOverflowStyle: 'none',
          }}
        >
          {sortedWeeks.map((week) => {
            const status = getWeekStatus(week);
            const isCurrentWeek = week.weekNumber === currentWeek;
            
            return (
              <motion.div
                key={week.weekNumber}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.3, delay: week.weekNumber * 0.1 }}
                whileHover={{ y: -8 }}
                whileTap={{ scale: 0.98 }}
                onClick={() => onWeekClick(week.weekNumber)}
                style={{
                  minWidth: 180,
                  padding: 16,
                  background: isCurrentWeek
                    ? 'linear-gradient(135deg, var(--game-info) 0%, var(--game-success) 100%)'
                    : 'var(--game-gray)',
                  border: `2px solid ${getStatusColor(status)}`,
                  borderRadius: 8,
                  cursor: 'pointer',
                }}
              >
                {/* Week Header */}
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                  <Typography variant="h6" sx={{ color: 'white', fontWeight: 600 }}>
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
                    }}
                  />
                </Box>

                {/* Completion Dots */}
                <Box sx={{ display: 'flex', gap: 0.5, mb: 1, flexWrap: 'wrap' }}>
                  {getCompletionDots(week)}
                </Box>

                {/* Workout Types */}
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                  {week.workouts.slice(0, 3).map((workout, index) => (
                    <Typography
                      key={index}
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
                  ))}
                  {week.workouts.length > 3 && (
                    <Typography variant="caption" sx={{ color: 'var(--game-gray-light)', fontSize: '0.7rem' }}>
                      +{week.workouts.length - 3} more
                    </Typography>
                  )}
                </Box>
              </motion.div>
            );
          })}
        </motion.div>
      </Box>
      </GameCard>
    </motion.div>
  );
};
