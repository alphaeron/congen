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
  Breadcrumbs,
  Link,
  Slide,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate, useSearchParams } from 'react-router';

import { ConfirmationDialog } from './ConfirmationDialog';
import { ExportButtons } from './ExportButtons';
import { LoadingBackdrop } from './LoadingBackdrop';
import { LoadingSpinner } from './LoadingSpinner';
import { StreamChart } from './StreamChart';
import { WorkoutWeekDetails } from './WorkoutWeekDetails';
import { generateNextWeek } from '../api/conjugateWorkoutGenerator';
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
  const [generateDialogOpen, setGenerateDialogOpen] = useState(false);
  const [selectedProgram, setSelectedProgram] = useState<Program | null>(null);
  const [exerciseData, setExerciseData] = useState<Map<string, Exercise>>(new Map());
  const [weightUnitPreferences, setWeightUnitPreferences] = useState<UserWeightUnitPreference[]>(
    []
  );
  const [userDataExport, setUserDataExport] = useState<UserDataExport | null>(null);

  // URL query parameters
  const selectedWeek = searchParams.get('week');
  const selectedWorkoutId = searchParams.get('workout') || selectedWorkout;

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

  const openGenerateDialog = (program: Program) => {
    setSelectedProgram(program);
    setGenerateDialogOpen(true);
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

  const handleGenerateWorkouts = async () => {
    if (!selectedProgram) return;

    // Close dialog immediately and show loading state
    setGenerateDialogOpen(false);
    setSelectedProgram(null);
    setIsGenerating(true);

    try {
      await generateNextWeek(selectedProgram.id);

      // Refresh data after generation
      const [programsData, workoutsData] = await Promise.all([
        getProgramsWithPreferences(),
        getProgrammedWorkouts(),
      ]);
      setProgramsWithPreferences(programsData);
      setWorkouts(workoutsData);
    } catch {
      enqueueSnackbar('Failed to generate workouts. Please try again.', { variant: 'error' });
    } finally {
      setIsGenerating(false);
    }
  };

  // Render breadcrumbs
  const renderBreadcrumbs = () => (
    <Box
      position="sticky"
      top={0}
      zIndex={1001}
      sx={{
        backgroundColor: 'background.default',
        pt: 2,
        pb: 2,
        borderBottom: 1,
        borderColor: 'divider',
      }}
    >
      <Box sx={{ mb: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        {/* Show program name instead of "Workouts" */}
        {selectedWeek ? (
          <Link
            component="button"
            variant="body1"
            onClick={() => handleBreadcrumbClick('workouts')}
            sx={{ color: 'text.secondary' }}
          >
            {activeProgram?.program.name || 'Workouts'}
          </Link>
        ) : (
          <Typography variant="body1" color="text.primary">
            {activeProgram?.program.name || 'Workouts'}
          </Typography>
        )}
        {selectedWeek && (
          <>
            <Typography variant="body1" color="text.primary">
              /
            </Typography>
            <Typography variant="body1" color="text.primary">
              Week {selectedWeek}
            </Typography>
          </>
        )}
        
        {/* Export buttons */}
        <Box sx={{ flexGrow: 1 }} />
        <ExportButtons
          onExportPDF={handleExportPDF}
          disabled={weeks.length === 0}
        />
        </Box>
      </Box>
    </Box>
  );

  const handleBreadcrumbClick = (path: string) => {
    if (path === 'workouts') {
      handleBackToWorkouts();
    }
  };

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


  // If a week is selected, show the WorkoutWeekDetails component
  if (selectedWeek) {
    return (
      <WorkoutWeekDetails selectedWorkout={selectedWorkoutId} weekNumber={parseInt(selectedWeek)} />
    );
  }

  // Show loading state while data is being fetched
  if (isLoading) {
    return (
      <React.Fragment>
        {renderBreadcrumbs()}
        <LoadingSpinner message="Loading workout data..." fullHeight={false} />
      </React.Fragment>
    );
  }

  return (
    <React.Fragment>
      {renderBreadcrumbs()}
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
          <Box id="workouts-overview-content">
            <Box sx={{ mt: 3, display: 'flex', gap: 3 }}>
          {/* Week List - Slides right when week is selected */}
          <Slide direction="right" in={!selectedWeek} mountOnEnter unmountOnExit>
            <Box sx={{ flex: 1, minWidth: 0 }}>
              <Grid container spacing={3}>
                {/* Program Overview and Generation */}
                <Grid size={{ xs: 12 }}>
                  <Card>
                    <CardContent>
                      <Box display="flex" justifyContent="space-between" alignItems="center">
                        <Box>
                          <Typography variant="h6" gutterBottom>
                            {`Current Week: Week ${Math.max(activeProgram.program.current_week_number, 1)}`}
                          </Typography>
                        </Box>
                        <Box display="flex" gap={1}>
                          <Button
                            variant="contained"
                            startIcon={<AddIcon />}
                            onClick={() => openGenerateDialog(activeProgram.program)}
                            disabled={isGenerating}
                          >
                            {isGenerating ? 'Generating...' : 'Generate Next Week'}
                          </Button>
                        </Box>
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>

                {/* Week List */}
                <Grid size={{ xs: 12 }}>
                  <Card sx={{ mb: 3 }}>
                    <CardContent>
                      <Typography variant="h6" gutterBottom>
                        Training Weeks
                      </Typography>
                      {isLoading ? (
                        <Box display="flex" justifyContent="center" p={3}>
                          <LoadingSpinner message="Loading weeks..." size={40} />
                        </Box>
                      ) : weeks.length === 0 ? (
                        <Typography variant="body2" color="text.secondary">
                          No workouts generated yet. Click &quot;Generate Next Week&quot; to create
                          your first workout week.
                        </Typography>
                      ) : (
                        <List>
                          {weeks.map(week => (
                            <ListItem
                              key={week.weekNumber}
                              disablePadding
                              sx={{
                                cursor: 'pointer',
                                '&:hover': { backgroundColor: 'action.hover' },
                              }}
                              onClick={() => handleWeekClick(week.weekNumber)}
                            >
                              <ListItemText
                                primary={`Week ${week.weekNumber}`}
                                secondary={`${week.workoutCount} workouts • ${week.workouts.map(w => replaceUnderscoresWithSpaces(w.name || `Workout ${w.day_number}`)).join(', ')}`}
                              />
                            </ListItem>
                          ))}
                        </List>
                      )}
                    </CardContent>
                  </Card>
                </Grid>
              </Grid>
            </Box>
          </Slide>
            </Box>
          </Box>
        </Box>
      )}

      {/* Stream Chart Section */}
      {userDataExport?.training_programs &&
        userDataExport.training_programs.length > 0 &&
        userDataExport.training_programs.some(program => program.workouts.length > 0) && (
          <StreamChart
            userDataExport={userDataExport}
            exerciseData={exerciseData}
            weightUnitPreferences={weightUnitPreferences}
            title="Volume Flow Over Time"
            description="Training volume distribution across workout types"
            height={400}
          />
        )}

      {/* Generate Workouts Dialog */}
      <ConfirmationDialog
        open={generateDialogOpen}
        onClose={() => setGenerateDialogOpen(false)}
        onConfirm={handleGenerateWorkouts}
        title="Generate Workouts"
        message={`Generate next week's workouts for ${selectedProgram?.name}?`}
        confirmText="Generate"
        confirmColor="primary"
      />

      {/* Full-screen loading overlay during workout generation */}
      <LoadingBackdrop
        open={isGenerating}
        message="Generating workouts..."
        subMessage="This may take a few moments"
      />
    </React.Fragment>
  );
};
