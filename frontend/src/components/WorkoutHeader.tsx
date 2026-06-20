import { Settings, ArrowBack } from '@mui/icons-material';
import { Box, IconButton, Tooltip, Button } from '@mui/material';
import { motion } from 'framer-motion';
import React from 'react';

import { ExportButtons } from './ExportButtons';
import { GameCard, GameText, GAME_CLASSES } from './GameTheme';
import { ProgressBar } from './ProgressBar';

type HeaderContext = 'program' | 'week' | 'day';

interface WorkoutHeaderProps {
  context: HeaderContext;
  // Program context props
  currentWeek?: number;
  progressValue?: number;
  progressMax?: number;
  currentWeekWorkouts?: number;
  completedWorkouts?: number;
  // Week context props
  weekNumber?: number;
  totalWorkouts?: number;
  completedWeekWorkouts?: number;
  // Day context props
  dayNumber?: number;
  daysPerWeek?: number;
  workoutName?: string;
  totalExercises?: number;
  completedExercises?: number;
  // Common props
  onExportPDF: () => Promise<void>;
  onSettings?: () => void;
  onBack?: () => void;
  onAddExercise?: () => void;
  disabled?: boolean;
  saving?: boolean;
}

/**
 * Workout Header component with context-aware display for program/week/day views.
 *
 * @param context The context type (program, week, or day)
 * @param onExportPDF Function to call for PDF export
 * @param onSettings Optional function to call for settings
 * @param onBack Optional function to call for back navigation
 * @param disabled Whether export is disabled
 * @return Workout Header component
 */
export const WorkoutHeader: React.FC<WorkoutHeaderProps> = ({
  context,
  // Program context props
  currentWeek,
  progressValue,
  progressMax,
  currentWeekWorkouts,
  completedWorkouts,
  // Week context props
  weekNumber,
  totalWorkouts,
  completedWeekWorkouts,
  // Day context props
  dayNumber,
  daysPerWeek,
  workoutName,
  totalExercises,
  completedExercises,
  // Common props
  onExportPDF,
  onSettings,
  onBack,
  onAddExercise,
  disabled = false,
  saving = false,
}) => {
  // Calculate progress based on context
  const getProgressData = () => {
    switch (context) {
      case 'program':
        return {
          value: progressValue || 0,
          max: progressMax || 1,
          percentage:
            progressValue && progressMax ? Math.round((progressValue / progressMax) * 100) : 0,
        };
      case 'week':
        return {
          value: completedWeekWorkouts || 0,
          max: totalWorkouts || 1,
          percentage:
            completedWeekWorkouts && totalWorkouts
              ? Math.round((completedWeekWorkouts / totalWorkouts) * 100)
              : 0,
        };
      case 'day':
        return {
          value: completedExercises || 0,
          max: totalExercises || 1,
          percentage:
            completedExercises && totalExercises
              ? Math.round((completedExercises / totalExercises) * 100)
              : 0,
        };
      default:
        return { value: 0, max: 1, percentage: 0 };
    }
  };

  const progressData = getProgressData();

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, ease: 'easeOut' }}
    >
      <GameCard className="glassmorphism-card">
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            p: 3,
          }}
        >
          {/* Left Section: Context Badge */}
          <Box className={GAME_CLASSES.subCard} sx={{ p: 2, flexShrink: 0 }}>
            <GameText variant="h4" className={GAME_CLASSES.textBold}>
              {context === 'program' && `Week ${currentWeek}`}
              {context === 'week' && `Week ${weekNumber}`}
              {context === 'day' && `Day ${dayNumber}`}
            </GameText>
          </Box>

          {/* Middle Section: Progress Bar (takes all available space) */}
          <Box sx={{ display: 'flex', flexDirection: 'column', flex: 1, mx: 3 }}>
            <Box sx={{ mb: 1 }}>
              <GameText variant="body2" className={GAME_CLASSES.textMuted}>
                {context === 'program' &&
                  (currentWeekWorkouts && currentWeekWorkouts > 0
                    ? `${currentWeekWorkouts} workouts this week`
                    : 'Current training week')}
                {context === 'week' && `${totalWorkouts || 0} workouts in week`}
                {context === 'day' && (
                  <span>
                    {weekNumber != null && dayNumber != null && daysPerWeek != null
                      ? `Week ${weekNumber} - Day ${dayNumber} of ${daysPerWeek}${
                          workoutName ? ` (${workoutName})` : ''
                        }`
                      : workoutName}
                  </span>
                )}
              </GameText>
            </Box>

            {/* Progress Bar - takes full width */}
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <ProgressBar
                value={progressData.value}
                max={progressData.max}
                width="100%"
                height={6}
                showFraction={true}
                current={progressData.value}
                total={progressData.max}
                status={
                  progressData.percentage >= 80
                    ? 'completed'
                    : progressData.percentage >= 30
                      ? 'in-progress'
                      : 'not-started'
                }
                smooth={true}
                animationDuration={300}
              />
            </Box>

            <Box sx={{ mt: 0.5 }}>
              <GameText variant="body2" className={GAME_CLASSES.textMedium}>
                {context === 'program' &&
                  (completedWorkouts && completedWorkouts > 0
                    ? `${completedWorkouts} completed`
                    : `${progressValue}/${progressMax} weeks in program`)}
                {context === 'week' &&
                  `${completedWeekWorkouts || 0}/${totalWorkouts || 0} completed`}
                {context === 'day' && `${completedExercises || 0}/${totalExercises || 0} exercises`}
              </GameText>
            </Box>
          </Box>

          {/* Right Section: Quick Actions */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexShrink: 0 }}>
            {/* Back Button (for week and day contexts) */}
            {(context === 'week' || context === 'day') && onBack && (
              <Tooltip title="Go Back">
                <motion.div whileHover={{ scale: 1.1 }} whileTap={{ scale: 0.95 }}>
                  <IconButton onClick={onBack} className={GAME_CLASSES.button}>
                    <ArrowBack />
                  </IconButton>
                </motion.div>
              </Tooltip>
            )}

            {/* Add Exercise Button (day context only) */}
            {context === 'day' && onAddExercise && (
              <motion.div whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}>
                <Button
                  variant="contained"
                  onClick={onAddExercise}
                  disabled={saving}
                  sx={{
                    '&:hover:not(:disabled)': {
                      boxShadow: 'var(--game-cyan-shadow)',
                    },
                  }}
                >
                  Add Exercise
                </Button>
              </motion.div>
            )}

            {/* Export Button */}
            <motion.div whileHover={{ scale: 1.1 }} whileTap={{ scale: 0.95 }}>
              <ExportButtons onExportPDF={onExportPDF} disabled={disabled} />
            </motion.div>

            {/* Settings Button */}
            {onSettings && (
              <Tooltip title="Settings">
                <motion.div whileHover={{ scale: 1.1 }} whileTap={{ scale: 0.95 }}>
                  <IconButton onClick={onSettings} className={GAME_CLASSES.button}>
                    <Settings />
                  </IconButton>
                </motion.div>
              </Tooltip>
            )}
          </Box>
        </Box>
      </GameCard>
    </motion.div>
  );
};
