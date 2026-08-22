import { Box, CardContent } from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate, useSearchParams } from 'react-router';

import { GameCard, GameText, GAME_CLASSES } from './GameTheme';
import { LoadingBackdrop } from './LoadingBackdrop';
import { LoadingSpinner } from './LoadingSpinner';
import { TrainingTimeline } from './TrainingTimeline';
import { VolumeOverviewCards } from './VolumeOverviewCards';
import { WorkoutGenerationWizard } from './WorkoutGenerationWizard';
import { WorkoutHeader } from './WorkoutHeader';
import type { Program, Exercise } from '../api/types';
import { useData } from '../contexts/DataContext';
import { exportProgramToPDF } from '../utils/exportUtils';
import {
  calculateProgramProgress,
  calculateWorkoutProgress,
  buildWeekProgressSummaries,
  getCurrentWeekFromProgress,
} from '../utils/progressUtils';
import { buildWeekVolumeTotals } from '../utils/volumeOverviewUtils';

interface WorkoutsProps {
  selectedWorkout?: string | null;
}

/**
 * Workouts component for managing and viewing workout programs.
 *
 * Features:
 * - Display active program and its weeks
 * - Generate new workouts for programs
 * - View week details with slide-left animation
 * - Auto-refresh functionality after workout generation
 * - URL query parameters for week and workout selection
 *
 * @param selectedWorkout The selected workout ID (from URL)
 * @returns Workouts component
 */
export const Workouts: React.FC<WorkoutsProps> = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { enqueueSnackbar } = useSnackbar();
  const {
    userData,
    weightUnitPreferences,
    refreshData,
    refreshSpecificData,
    isLoading: isDataLoading,
    loadProgramPreferences,
    programPreferences = [],
    getExercise,
  } = useData();

  const [isLoading, setIsLoading] = useState(true);
  const [wizardOpen, setWizardOpen] = useState(false);
  const [selectedProgram, setSelectedProgram] = useState<Program | null>(null);
  const [exerciseData, setExerciseData] = useState<Map<string, Exercise>>(new Map());
  const [isGenerating, setIsGenerating] = useState(false);

  // Load additional data that's not in DataContext
  useEffect(() => {
    const loadAdditionalData = async () => {
      if (!userData) return;

      setIsLoading(true);
      try {
        if (programPreferences.length === 0) {
          await loadProgramPreferences();
        }

        // Extract unique exercises from the userData and fetch exercise details
        const uniqueExercises = new Set<string>();
        userData.training_programs?.forEach(program => {
          program.workouts.forEach(workoutWithStages => {
            workoutWithStages.stages.forEach(stageWithExercises => {
              stageWithExercises.exercises.forEach(exerciseWithSetSchemes => {
                uniqueExercises.add(exerciseWithSetSchemes.exercise.exercise_name);
              });
            });
          });
        });

        // Fetch exercise data for all unique exercises using DataContext
        const exerciseMap = new Map<string, Exercise>();
        for (const exerciseName of Array.from(uniqueExercises)) {
          try {
            const exercise = await getExercise(exerciseName);
            if (exercise) {
              exerciseMap.set(exerciseName, exercise);
            }
          } catch {
            enqueueSnackbar(`Error fetching exercise data for ${exerciseName}`, {
              variant: 'error',
            });
          }
        }

        setExerciseData(exerciseMap);
      } catch {
        enqueueSnackbar('Failed to load additional workout data. Please try again.', {
          variant: 'error',
        });
      } finally {
        setIsLoading(false);
      }
    };

    loadAdditionalData();
  }, [userData, programPreferences.length, enqueueSnackbar, loadProgramPreferences, getExercise]);

  // Get active program data consistently from userData
  const activeProgramData = useMemo(() => {
    if (!userData?.training_programs) return null;
    return userData.training_programs.find(p => p.program.is_active) || null;
  }, [userData]);

  // Get active program preferences
  const activeProgramPreferences = useMemo(() => {
    if (!activeProgramData) return null;
    return programPreferences.find(p => p.program.id === activeProgramData.program.id) || null;
  }, [activeProgramData, programPreferences]);

  // Legacy activeProgram for backward compatibility in render
  const activeProgram = activeProgramPreferences;

  // Group workouts by week
  const weeks = useMemo(() => {
    if (
      !activeProgramData ||
      !activeProgramPreferences ||
      activeProgramData.workouts.length === 0
    ) {
      return [];
    }

    // Use program preferences
    const workoutsPerWeek = activeProgramPreferences.program_preferences.program_days_per_week;
    const weekSummaries = buildWeekProgressSummaries(activeProgramData.workouts, workoutsPerWeek);

    return weekSummaries.map(summary => {
      const weekWorkoutsWithStages = activeProgramData.workouts.filter(
        workoutWithStages =>
          Math.ceil(workoutWithStages.workout.day_number / workoutsPerWeek) === summary.weekNumber
      );

      const weekWorkoutsSorted = [...weekWorkoutsWithStages].sort(
        (a, b) => a.workout.day_number - b.workout.day_number
      );

      const workoutProgresses = weekWorkoutsSorted.map(workoutWithStages =>
        calculateWorkoutProgress(workoutWithStages)
      );

      const completedWorkouts = workoutProgresses.filter(
        progress => progress.status === 'completed'
      ).length;

      return {
        weekNumber: summary.weekNumber,
        workoutCount: weekWorkoutsWithStages.length,
        workouts: weekWorkoutsSorted.map((workoutWithStages, index) => ({
          workout: workoutWithStages.workout,
          isCompleted: workoutProgresses[index].status === 'completed',
        })),
        isCompleted: summary.isCompleted,
        completedWorkouts,
      };
    });
  }, [activeProgramData, activeProgramPreferences]);

  const currentWeek = useMemo(() => getCurrentWeekFromProgress(weeks), [weeks]);

  const preferredUnit = useMemo((): 'KG' | 'LBS' => {
    const preference = weightUnitPreferences.find(item => item.preferred_unit);
    return preference?.preferred_unit === 'KG' ? 'KG' : 'LBS';
  }, [weightUnitPreferences]);

  const timelineWeekSummaries = useMemo(() => {
    if (!activeProgramData || !activeProgramPreferences) {
      return [];
    }

    const weekVolumes = buildWeekVolumeTotals(
      activeProgramData.workouts,
      exerciseData,
      activeProgramPreferences.program_preferences.program_days_per_week,
      preferredUnit
    );
    const volumeByWeek = new Map(weekVolumes.map(week => [week.weekNumber, week]));

    return weeks.map(week => {
      const volume = volumeByWeek.get(week.weekNumber);
      const meUnderHint = volume && volume.maxEffortVolume <= 0 ? ' · No ME volume' : '';

      return {
        weekNumber: week.weekNumber,
        workouts: week.workouts,
        isCompleted: week.isCompleted,
        completedWorkouts: week.completedWorkouts,
        plannedWorkouts: week.workoutCount,
        totalVolume: volume?.totalVolume || 0,
        statusHint: `${week.completedWorkouts} of ${week.workoutCount} sessions${meUnderHint}`,
      };
    });
  }, [activeProgramData, activeProgramPreferences, weeks, exerciseData, preferredUnit]);

  const openWizard = (program: Program) => {
    setSelectedProgram(program);
    setWizardOpen(true);
  };

  const handleWeekClick = (weekNumber: number) => {
    const newSearchParams = new URLSearchParams(searchParams);
    newSearchParams.set('section', 'workouts');
    newSearchParams.set('week', weekNumber.toString());
    navigate(`/dashboard?${newSearchParams.toString()}`);
  };

  const handleWizardComplete = async () => {
    // Close the wizard first
    setWizardOpen(false);
    setSelectedProgram(null);

    setIsGenerating(true);
    try {
      await refreshData();
      await refreshSpecificData('programs');
      enqueueSnackbar('Workouts generated successfully!', { variant: 'success' });
    } catch {
      enqueueSnackbar('Failed to refresh workout data', { variant: 'error' });
    } finally {
      setIsGenerating(false);
    }
  };

  const handleWizardClose = () => {
    setWizardOpen(false);
    setSelectedProgram(null);
  };

  const progressMetrics = useMemo(() => {
    if (!activeProgramData || !activeProgramPreferences) return null;

    const workoutsPerWeek =
      activeProgramPreferences.program_preferences?.program_days_per_week || 3;
    const programProgress = calculateProgramProgress(activeProgramData.workouts, workoutsPerWeek);

    return {
      ...programProgress,
      currentWeek,
    };
  }, [activeProgramData, activeProgramPreferences, currentWeek]);

  // Export handlers
  const handleExportPDF = async () => {
    if (!activeProgramData) return;

    await exportProgramToPDF(activeProgramData, weightUnitPreferences, {
      title: activeProgramData.program.name,
      filename: `program-${activeProgramData.program.name.replace(/\s+/g, '-').toLowerCase()}`,
    });
  };

  // Show loading state while data is being fetched
  if (isDataLoading || isLoading) {
    return <LoadingSpinner message="Loading workout data..." fullHeight={false} />;
  }

  // Show workout calendar
  return (
    <React.Fragment>
      <Box sx={{ px: 3, pt: 3 }}>
        {!activeProgram ? (
          <GameCard className={`${GAME_CLASSES.marginBottom3} ${GAME_CLASSES.marginTop3}`}>
            <CardContent>
              <GameText variant="h6" gutterBottom={true}>
                No Active Program
              </GameText>
              <GameText variant="body2" className={GAME_CLASSES.opacity80} paragraph>
                You need to create a program first before you can generate and view workouts. Please
                go to the Programs section to create a program.
              </GameText>
            </CardContent>
          </GameCard>
        ) : (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3, overflow: 'visible' }}>
            {/* Header Section */}
            {progressMetrics && (
              <WorkoutHeader
                context="program"
                currentWeek={currentWeek}
                progressValue={progressMetrics.completedWeeks}
                progressMax={Math.max(activeProgram.program.current_week_number, 1)}
                onExportPDF={handleExportPDF}
                disabled={weeks.length === 0}
                currentWeekWorkouts={
                  weeks.find(w => w.weekNumber === currentWeek)?.workoutCount || 0
                }
                completedWorkouts={
                  weeks.find(w => w.weekNumber === currentWeek)?.completedWorkouts || 0
                }
                onGenerateWeek={() => openWizard(activeProgram.program)}
                generateDisabled={isGenerating}
                generateLoading={isGenerating}
              />
            )}

            {userData?.training_programs &&
              userData.training_programs.length > 0 &&
              userData.training_programs.some(program => program.workouts.length > 0) && (
                <VolumeOverviewCards
                  userDataExport={userData}
                  exerciseData={exerciseData}
                  workoutsPerWeek={
                    activeProgramPreferences?.program_preferences.program_days_per_week || 3
                  }
                  currentWeek={currentWeek}
                  preferredUnit={preferredUnit}
                />
              )}

            <TrainingTimeline
              weeks={timelineWeekSummaries}
              onWeekClick={handleWeekClick}
              currentWeek={currentWeek}
            />
          </Box>
        )}
      </Box>

      {/* Workout Generation Wizard */}
      {selectedProgram && (
        <WorkoutGenerationWizard
          open={wizardOpen}
          onClose={handleWizardClose}
          onComplete={handleWizardComplete}
          program={selectedProgram}
        />
      )}

      {/* Full-screen loading overlay during workout generation */}
      <LoadingBackdrop
        open={isGenerating}
        message="Generating workouts..."
        subMessage="This may take a few moments"
      />
    </React.Fragment>
  );
};
