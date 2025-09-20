import { Add as AddIcon } from '@mui/icons-material';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Grid,
  List,
  ListItem,
  ListItemText,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate, useSearchParams } from 'react-router';

import { ConfirmationDialog } from './ConfirmationDialog';
import { ExportButtons } from './ExportButtons';
import { LoadingBackdrop } from './LoadingBackdrop';
import { LoadingSpinner } from './LoadingSpinner';
import { StreamChart } from './StreamChart';
import { WorkoutGenerationWizard } from './WorkoutGenerationWizard';
import { ProgressBar } from './ProgressBar';
import { generateNextWeek } from '../api/conjugateWorkoutGenerator';
import { calculateProgramProgress } from '../utils/progressUtils';

import { getIndividualExercise } from '../api/exercise';
import { getUserDataExport } from '../api/gdpr';
import { getProgramsWithPreferences } from '../api/program';
import { getProgrammedWorkouts } from '../api/programmedWorkout';
import type {
  Program,
  ProgrammedWorkout,
  User,
  ProgramWithPreferences,
  Exercise,
  UserDataExport,
  ProgramWithWorkouts,
  UserWeightUnitPreference,
} from '../api/types';
import { getUserWeightUnitPreferences } from '../api/userWeightUnitPreference';
import { replaceUnderscoresWithSpaces } from '../common/utils';
import { exportProgramToPDF } from '../utils/exportUtils';

interface WorkoutsProps {
  user: User;
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
 * @param user The current user object
 * @param selectedWorkout The selected workout ID (from URL)
 * @returns Workouts component
 */
export const Workouts: React.FC<WorkoutsProps> = ({ user, selectedWorkout }) => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { enqueueSnackbar } = useSnackbar();

  const [programsWithPreferences, setProgramsWithPreferences] = useState<
    Array<ProgramWithPreferences>
  >([]);
  const [workouts, setWorkouts] = useState<ProgrammedWorkout[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isGenerating, setIsGenerating] = useState(false);
  const [wizardOpen, setWizardOpen] = useState(false);
  const [selectedProgram, setSelectedProgram] = useState<Program | null>(null);
  const [exerciseData, setExerciseData] = useState<Map<string, Exercise>>(new Map());
  const [weightUnitPreferences, setWeightUnitPreferences] = useState<UserWeightUnitPreference[]>(
    []
  );
  const [userDataExport, setUserDataExport] = useState<UserDataExport | null>(null);

  // Load workout data
  useEffect(() => {
    const loadWorkoutData = async () => {
      setIsLoading(true);
      try {
        const [programsData, workoutsData, userData, weightUnitData] = await Promise.all([
          getProgramsWithPreferences(),
          getProgrammedWorkouts(),
          getUserDataExport(),
          getUserWeightUnitPreferences(user.keycloak_id),
        ]);

        setProgramsWithPreferences(programsData);
        setWorkouts(workoutsData);
        setUserDataExport(userData);
        setWeightUnitPreferences(weightUnitData || []);

        // Extract unique exercises from the export data and fetch exercise details
        const uniqueExercises = new Set<string>();
        (userData.training_programs as ProgramWithWorkouts[])?.forEach(program => {
          program.workouts.forEach(workoutWithStages => {
            workoutWithStages.stages.forEach(stageWithExercises => {
              stageWithExercises.exercises.forEach(exerciseWithSetSchemes => {
                uniqueExercises.add(exerciseWithSetSchemes.exercise.exercise_name);
              });
            });
          });
        });

        // Fetch exercise data for all unique exercises
        const exerciseMap = new Map<string, Exercise>();
        for (const exerciseName of Array.from(uniqueExercises)) {
          try {
            const exercise = await getIndividualExercise(exerciseName);
            exerciseMap.set(exerciseName, exercise);
          } catch {
            enqueueSnackbar(`Error fetching exercise data for ${exerciseName}`, {
              variant: 'error',
            });
          }
        }

        setExerciseData(exerciseMap);
      } catch {
        enqueueSnackbar('Failed to load workout data. Please try again.', { variant: 'error' });
      } finally {
        setIsLoading(false);
      }
    };

    loadWorkoutData();
  }, [user.keycloak_id]); // Reload when user changes

  const activeProgram = programsWithPreferences.find(program => program.program.is_active);

  // Group workouts by week
  const weeks = useMemo(() => {
    if (!activeProgram) return [];

    const programWorkouts = workouts.filter(
      workout => workout.program_id === activeProgram.program.id
    );

    if (programWorkouts.length === 0) return [];

    // Use program preferences
    const workoutsPerWeek = activeProgram.program_preferences.program_days_per_week;

    const weekMap = new Map<number, ProgrammedWorkout[]>();

    programWorkouts.forEach(workout => {
      const weekNum = Math.ceil(workout.day_number / workoutsPerWeek);
      if (!weekMap.has(weekNum)) {
        weekMap.set(weekNum, []);
      }
      weekMap.get(weekNum)!.push(workout);
    });

    return Array.from(weekMap.entries())
      .map(([weekNumber, weekWorkouts]) => ({
        weekNumber,
        workoutCount: weekWorkouts.length,
        workouts: weekWorkouts.sort((a, b) => a.day_number - b.day_number),
      }))
      .sort((a, b) => a.weekNumber - b.weekNumber);
  }, [workouts, activeProgram]);

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

  const handleBackToWorkouts = () => {
    const newSearchParams = new URLSearchParams(searchParams);
    newSearchParams.set('section', 'workouts');
    newSearchParams.delete('week');
    newSearchParams.delete('workout');
    navigate(`/dashboard?${newSearchParams.toString()}`);
  };

  const handleWizardComplete = async (updatedProgram: Program) => {
    // Refresh data after generation
    const [programsData, workoutsData] = await Promise.all([
      getProgramsWithPreferences(),
      getProgrammedWorkouts(),
    ]);
    setProgramsWithPreferences(programsData);
    setWorkouts(workoutsData);
    
    enqueueSnackbar('Workouts generated successfully!', { variant: 'success' });
    setWizardOpen(false);
    setSelectedProgram(null);
  };

  const handleWizardClose = () => {
    setWizardOpen(false);
    setSelectedProgram(null);
  };

  // Calculate progress metrics
  const getProgressMetrics = () => {
    if (!activeProgram || !userDataExport) return null;
    
    const programData = userDataExport.training_programs.find(p => p.program.id === activeProgram.program.id);
    if (!programData) return null;
    
    const workoutsPerWeek = activeProgram.program_preferences?.program_days_per_week || 3;
    const programProgress = calculateProgramProgress(programData.workouts, workoutsPerWeek);
    const currentWeek = Math.max(activeProgram.program.current_week_number, 1);
    
    return {
      ...programProgress,
      currentWeek,
    };
  };

  // Get progress metrics for the component
  const progressMetrics = getProgressMetrics();

  // Export handlers
  const handleExportPDF = async () => {
    if (!activeProgram || !userDataExport) return;
    const programData = userDataExport.training_programs.find(p => p.program.id === activeProgram.program.id);
    if (!programData) return;
    
    await exportProgramToPDF(programData, weightUnitPreferences, {
      title: activeProgram.program.name,
      filename: `program-${activeProgram.program.name.replace(/\s+/g, '-').toLowerCase()}`,
    });
  };

  // Show loading state while data is being fetched
  if (isLoading) {
    return (
      <React.Fragment>
        <LoadingSpinner message="Loading workout data..." fullHeight={false} />
      </React.Fragment>
    );
  }

  // Show workout calendar
  return (
    <React.Fragment>
      <Box sx={{ p: 3 }}>

        {!activeProgram ? (
          <Card sx={{ mb: 3, mt: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom={true}>
                No Active Program
              </Typography>
              <Typography variant="body2" color="text.secondary" paragraph>
                You need to create a program first before you can generate and view workouts. Please
                go to the Programs section to create a program.
              </Typography>
            </CardContent>
          </Card>
        ) : (
          <Box>
            {/* Top Section: Progress Bar and Export Buttons */}
            <Box sx={{ mb: 3 }}>
              <Box display="flex" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
                {/* Progress Bar on the left */}
                <Box sx={{ flex: 1, mr: 2 }}>
                  {progressMetrics && (
                    <ProgressBar
                      value={progressMetrics.completionRate}
                      status={progressMetrics.status}
                      current={progressMetrics.completedWeeks}
                      total={progressMetrics.totalWeeks}
                      showTooltip={true}
                      showTicks={true}
                      steps={Array.from({ length: progressMetrics.totalWeeks + 1 }, (_, i) => (i / progressMetrics.totalWeeks) * 100)}
                      ticks={Array.from({ length: progressMetrics.totalWeeks + 1 }, (_, i) => (i / progressMetrics.totalWeeks) * 100)}
                      width="100%"
                      height={8}
                      smooth={true}
                      animationDuration={400}
                    />
                  )}
                </Box>
                
                {/* Export Buttons on the right */}
                <ExportButtons
                  onExportPDF={handleExportPDF}
                  disabled={weeks.length === 0}
                />
              </Box>
            </Box>

            {/* Current Week Section */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Box display="flex" justifyContent="space-between" alignItems="flex-start" sx={{ mb: 2 }}>
                  <Box>
                    <Typography variant="h6" gutterBottom>
                      {`Current Week: Week ${Math.max(activeProgram.program.current_week_number, 1)}`}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Track your progress and generate new workout weeks
                    </Typography>
                  </Box>
                  <Box display="flex" gap={1}>
                    <Button
                      variant="contained"
                      startIcon={<AddIcon />}
                      onClick={() => openWizard(activeProgram.program)}
                      disabled={isGenerating}
                      size="large"
                    >
                      {isGenerating ? 'Generating...' : 'Generate Next Week'}
                    </Button>
                  </Box>
                </Box>

                {/* Volume Flow Chart */}
                {userDataExport?.training_programs &&
                  userDataExport.training_programs.length > 0 &&
                  userDataExport.training_programs.some(program => program.workouts.length > 0) && (
                  <Box sx={{ mt: 2 }}>
                    <StreamChart
                      userDataExport={userDataExport}
                      exerciseData={exerciseData}
                      weightUnitPreferences={weightUnitPreferences}
                      title="Volume Flow Over Time"
                      description="Training volume distribution across workout types"
                      height={300}
                    />
                  </Box>
                )}
              </CardContent>
            </Card>

            {/* Training Weeks Section */}
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Training Weeks
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Click on any week to view detailed workout information
                </Typography>
                {isLoading ? (
                  <Box display="flex" justifyContent="center" p={3}>
                    <LoadingSpinner message="Loading weeks..." size={40} />
                  </Box>
                ) : weeks.length === 0 ? (
                  <Box sx={{ textAlign: 'center', py: 4 }}>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                      No workouts generated yet. Click &quot;Generate Next Week&quot; to create
                      your first workout week.
                    </Typography>
                  </Box>
                ) : (
                  <List>
                    {weeks.map(week => (
                      <ListItem
                        key={week.weekNumber}
                        disablePadding
                        sx={{
                          cursor: 'pointer',
                          borderRadius: 1,
                          mb: 1,
                          '&:hover': {
                            backgroundColor: 'action.hover',
                            transform: 'translateX(4px)',
                            transition: 'all 0.2s ease'
                          },
                        }}
                        onClick={() => handleWeekClick(week.weekNumber)}
                      >
                        <ListItemText
                          primary={
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                              <Typography variant="subtitle1" fontWeight="medium">
                                Week {week.weekNumber}
                              </Typography>
                              {week.workoutCount > 0 && (
                                <Box
                                  sx={{
                                    backgroundColor: 'primary.main',
                                    color: 'primary.contrastText',
                                    borderRadius: '50%',
                                    width: 20,
                                    height: 20,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    fontSize: '0.75rem',
                                    fontWeight: 'bold'
                                  }}
                                >
                                  {week.workoutCount}
                                </Box>
                              )}
                            </Box>
                          }
                          secondary={
                            <Typography variant="body2" color="text.secondary">
                              {week.workouts.map(w => replaceUnderscoresWithSpaces(w.name || `Workout ${w.day_number}`)).join(' • ')}
                            </Typography>
                          }
                        />
                      </ListItem>
                    ))}
                  </List>
                )}
              </CardContent>
            </Card>
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
