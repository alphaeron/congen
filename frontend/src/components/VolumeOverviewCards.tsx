import { Box, CardContent, LinearProgress } from '@mui/material';
import { ResponsiveLine } from '@nivo/line';
import { motion } from 'framer-motion';
import React, { useMemo } from 'react';

import { GameCard, GameText, GAME_CLASSES } from './GameTheme';
import type {
  UserDataExport,
  UserWeightUnitPreference,
  ProgramWithWorkouts,
  Exercise,
} from '../api/types';
import {
  categorizeExerciseVolume,
  convertWeightToPounds,
  replaceUnderscoresWithSpaces,
  formatDate,
} from '../common/utils';

interface VolumeOverviewCardsProps {
  userDataExport: UserDataExport | null;
  exerciseData: Map<string, Exercise>;
  weightUnitPreferences: UserWeightUnitPreference[];
  height?: number;
}

interface VolumeData {
  date: string;
  maxEffortVolume: number;
  dynamicEffortVolume: number;
  accessoryVolume: number;
}

interface VolumeCardData {
  type: 'Max Effort' | 'Dynamic Effort' | 'Accessory';
  current: number;
  target: number;
  trend: number; // percentage change
  trendData: Array<{ x: string; y: number }>;
  color: string;
}

/**
 * Volume Overview Cards component for displaying volume trends and progress.
 *
 * This component shows three cards with bullet-style progress bars and mini line charts
 * for each volume type (Max Effort, Dynamic Effort, Accessory).
 *
 * @param userDataExport The raw user data export containing all workout information
 * @param exerciseData Map of exercise data for categorization
 * @param weightUnitPreferences User's weight unit preferences for conversion
 * @return Volume Overview Cards component
 */
export const VolumeOverviewCards: React.FC<VolumeOverviewCardsProps> = ({
  userDataExport,
  exerciseData,
  weightUnitPreferences,
}) => {
  // Extract workouts from the raw data
  const workouts = useMemo(() => {
    if (!userDataExport?.training_programs?.length) return [];

    return userDataExport.training_programs.flatMap((program: ProgramWithWorkouts) =>
      program.workouts.map(workoutWithStages => ({
        workout: workoutWithStages.workout,
        stages: workoutWithStages.stages.map(stageWithExercises => ({
          stage: stageWithExercises.stage,
          exercises: stageWithExercises.exercises.map(exerciseWithSetSchemes => ({
            exercise: exerciseWithSetSchemes.exercise,
            set_schemes: exerciseWithSetSchemes.set_schemes,
          })),
        })),
      }))
    );
  }, [userDataExport]);

  // Calculate workout volume data
  const volumeData = useMemo((): VolumeData[] => {
    if (!workouts.length) return [];

    return workouts
      .map(workoutData => {
        let maxEffortVolume = 0;
        let dynamicEffortVolume = 0;
        let accessoryVolume = 0;

        workoutData.stages.forEach(stage => {
          stage.exercises.forEach(exerciseWithSchemes => {
            exerciseWithSchemes.set_schemes.forEach(setScheme => {
              const weight = setScheme.performed_weight || setScheme.target_weight || 0;
              const reps = setScheme.performed_rep_count || setScheme.target_rep_count || 0;
              const bandWeight = setScheme.band_weight_lbs
                ? (setScheme.band_weight_lbs as { weight_lbs?: number })?.weight_lbs || 0
                : 0;

              // Get user's preferred weight unit for this exercise
              const exerciseName = exerciseWithSchemes.exercise.exercise_name;
              const weightUnitPreference = weightUnitPreferences.find(
                pref => pref.exercise_name === exerciseName
              );

              // Convert weight to pounds for consistent calculations
              const convertedWeight = convertWeightToPounds(
                weight,
                weightUnitPreference?.preferred_unit as 'KG' | 'LBS' | undefined
              );
              const totalWeight = convertedWeight + bandWeight; // bandWeight is already in lbs
              const setVolume = totalWeight * reps;

              // Get exercise data and categorize volume using shared helper
              const exerciseInfo = exerciseData.get(exerciseName);
              const categorizedVolume = categorizeExerciseVolume(
                exerciseInfo,
                replaceUnderscoresWithSpaces(workoutData.workout.name),
                setVolume
              );

              maxEffortVolume += categorizedVolume.maxEffortVolume;
              dynamicEffortVolume += categorizedVolume.dynamicEffortVolume;
              accessoryVolume += categorizedVolume.accessoryVolume;
            });
          });
        });

        return {
          date: formatDate(workoutData.workout.created_at),
          maxEffortVolume: Math.round(maxEffortVolume),
          dynamicEffortVolume: Math.round(dynamicEffortVolume),
          accessoryVolume: Math.round(accessoryVolume),
        };
      })
      .slice(-10); // Last 10 workouts
  }, [workouts, exerciseData, weightUnitPreferences]);

  // Prepare card data with trends and targets
  const cardData = useMemo((): VolumeCardData[] => {
    if (!volumeData.length) return [];

    const currentWeek = volumeData[volumeData.length - 1];
    const previousWeek = volumeData[volumeData.length - 2];

    // Calculate targets based on historical averages (simplified)
    const avgMaxEffort =
      volumeData.reduce((sum, v) => sum + v.maxEffortVolume, 0) / volumeData.length;
    const avgDynamicEffort =
      volumeData.reduce((sum, v) => sum + v.dynamicEffortVolume, 0) / volumeData.length;
    const avgAccessory =
      volumeData.reduce((sum, v) => sum + v.accessoryVolume, 0) / volumeData.length;

    const calculateTrend = (current: number, previous: number): number => {
      if (!previous || previous === 0) return 0;
      return Math.round(((current - previous) / previous) * 100);
    };

    const prepareTrendData = (volumeType: keyof VolumeData) => {
      return volumeData.map(v => ({
        x: v.date.split('/')[0], // Just show month/day
        y: v[volumeType] as number,
      }));
    };

    return [
      {
        type: 'Max Effort',
        current: currentWeek.maxEffortVolume,
        target: Math.round(avgMaxEffort * 1.1), // 10% above average as target
        trend: calculateTrend(currentWeek.maxEffortVolume, previousWeek?.maxEffortVolume || 0),
        trendData: prepareTrendData('maxEffortVolume'),
        color: 'var(--game-cyan)',
      },
      {
        type: 'Dynamic Effort',
        current: currentWeek.dynamicEffortVolume,
        target: Math.round(avgDynamicEffort * 1.1),
        trend: calculateTrend(
          currentWeek.dynamicEffortVolume,
          previousWeek?.dynamicEffortVolume || 0
        ),
        trendData: prepareTrendData('dynamicEffortVolume'),
        color: 'var(--game-cyan)',
      },
      {
        type: 'Accessory',
        current: currentWeek.accessoryVolume,
        target: Math.round(avgAccessory * 1.1),
        trend: calculateTrend(currentWeek.accessoryVolume, previousWeek?.accessoryVolume || 0),
        trendData: prepareTrendData('accessoryVolume'),
        color: 'var(--game-cyan)',
      },
    ];
  }, [volumeData]);

  // Don't render if no data
  if (!cardData.length) {
    return null;
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, ease: 'easeOut' }}
      style={{ display: 'flex', gap: 16, flexWrap: 'wrap' }}
    >
      {cardData.map((card, index) => (
        <motion.div
          key={card.type}
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4, delay: index * 0.1 }}
          whileHover={{ y: -8 }}
          style={{
            flex: '1 1 300px',
            minWidth: 280,
          }}
        >
          <GameCard
            className="glassmorphism-card"
            sx={{
              height: '100%',
            }}
          >
            <CardContent sx={{ p: 2 }}>
              {/* Header */}
              <Box
                sx={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  mb: 2,
                }}
              >
                <GameText variant="h6" className={GAME_CLASSES.textMedium}>
                  {card.type}
                </GameText>
                <GameText
                  variant="body2"
                  className={GAME_CLASSES.textMedium}
                  sx={{
                    color: card.trend >= 0 ? 'var(--game-cyan)' : 'var(--game-error)',
                  }}
                >
                  {card.trend >= 0 ? '↗' : '↘'} {Math.abs(card.trend)}%
                </GameText>
              </Box>

              {/* Current vs Target */}
              <Box sx={{ mb: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                  <GameText variant="h4" className={GAME_CLASSES.textBold}>
                    {card.current.toLocaleString()}
                  </GameText>
                  <GameText
                    variant="body2"
                    className={GAME_CLASSES.textMuted}
                    sx={{ alignSelf: 'flex-end' }}
                  >
                    / {card.target.toLocaleString()} lbs
                  </GameText>
                </Box>

                {/* Progress Bar */}
                <LinearProgress
                  variant="determinate"
                  value={(card.current / card.target) * 100}
                  sx={{
                    height: 8,
                    borderRadius: 4,
                    backgroundColor: 'var(--game-cyan-light)',
                    '& .MuiLinearProgress-bar': {
                      backgroundColor: card.color,
                      borderRadius: 4,
                    },
                  }}
                />
              </Box>

              {/* Mini Line Chart */}
              <Box sx={{ height: 60, width: '100%' }}>
                <ResponsiveLine
                  data={[
                    {
                      id: card.type,
                      data: card.trendData,
                    },
                  ]}
                  margin={{ top: 5, right: 5, bottom: 5, left: 5 }}
                  xScale={{ type: 'point' }}
                  yScale={{
                    type: 'linear',
                    min: 'auto',
                    max: 'auto',
                  }}
                  curve="monotoneX"
                  axisTop={null}
                  axisRight={null}
                  axisBottom={null}
                  axisLeft={null}
                  enableGridX={false}
                  enableGridY={false}
                  enablePoints={false}
                  enableArea={true}
                  areaOpacity={0.3}
                  colors={[card.color]}
                  lineWidth={2}
                  theme={{
                    background: 'transparent',
                    text: {
                      fontSize: 10,
                      fill: 'var(--game-white-muted)',
                    },
                    grid: {
                      line: {
                        stroke: 'var(--game-cyan-light)',
                      },
                    },
                  }}
                  animate={true}
                  motionConfig="gentle"
                />
              </Box>
            </CardContent>
          </GameCard>
        </motion.div>
      ))}
    </motion.div>
  );
};
