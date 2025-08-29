import { Add as AddIcon } from '@mui/icons-material';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Grid,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  List,
  ListItem,
  ListItemText,
  Breadcrumbs,
  Link,
  Slide,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useNavigate, useSearchParams } from 'react-router';

import { WorkoutDetail } from './WorkoutDetail';
import { generateNextWeek } from '../api/conjugateWorkoutGenerator';
import { getPrograms } from '../api/program';
import { getProgrammedWorkouts } from '../api/programmedWorkout';
import type { Program, ProgrammedWorkout, User } from '../api/types';

interface WorkoutWeekDetailsProps {
  user: User;
  selectedWorkout?: string | null;
  weekNumber: number;
}

/**
 * WorkoutWeekDetails component for viewing workouts grouped by week.
 *
 * Features:
 * - Display workouts for a specific week
 * - Generate new workouts for the week
 * - View workout details with slide-left animation
 * - Auto-refresh functionality after workout generation
 * - URL query parameters for workout selection
 * - Breadcrumb navigation with week number
 *
 * @param user The current user object
 * @param selectedWorkout The selected workout ID (from URL)
 * @param weekNumber The week number to display
 * @returns WorkoutWeekDetails component
 */
export const WorkoutWeekDetails: React.FC<WorkoutWeekDetailsProps> = ({ 
  user, 
  selectedWorkout, 
  weekNumber 
}) => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { enqueueSnackbar } = useSnackbar();

  const [programs, setPrograms] = useState<Program[]>([]);
  const [workouts, setWorkouts] = useState<ProgrammedWorkout[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isGenerating, setIsGenerating] = useState(false);
  const [generateDialogOpen, setGenerateDialogOpen] = useState(false);
  const [selectedProgram, setSelectedProgram] = useState<Program | null>(null);
  const [currentWorkoutDetails, setCurrentWorkoutDetails] = useState<{
    name: string;
    day_number: number;
    stages: number;
  } | null>(null);

  // URL query parameters
  const selectedWorkoutId = searchParams.get('workout') || selectedWorkout;

  // Reset workout details when workout selection changes
  useEffect(() => {
    if (!selectedWorkoutId) {
      setCurrentWorkoutDetails(null);
    }
  }, [selectedWorkoutId]);

  // Load workout data
  useEffect(() => {
    const loadWorkoutData = async () => {
      setIsLoading(true);
      try {
        const [programsData, workoutsData] = await Promise.all([
          getPrograms(),
          getProgrammedWorkouts(),
        ]);

        setPrograms(programsData);
        setWorkouts(workoutsData);
      } catch {
        enqueueSnackbar('Failed to load workout data. Please try again.', { variant: 'error' });
      } finally {
        setIsLoading(false);
      }
    };

    loadWorkoutData();
  }, []); // Only load once on mount

  const activeProgram = programs.find(program => program.is_active);
  
  // Group workouts by week and filter for the current week
  const weekWorkouts = useMemo(() => {
    if (!activeProgram) return [];
    
    const programWorkouts = workouts.filter(
      workout => workout.program_id === activeProgram.id
    );
    
    // Calculate which week each workout belongs to based on day_number
    // Assuming workouts are generated in weekly cycles (e.g., 3-4 workouts per week)
    const workoutsPerWeek = activeProgram.current_week_number > 1 ? 
      Math.ceil(programWorkouts.length / activeProgram.current_week_number) : 
      programWorkouts.length;
    
    return programWorkouts
      .map(workout => {
        const weekNum = Math.ceil(workout.day_number / workoutsPerWeek);
        const dayInWeek = ((workout.day_number - 1) % workoutsPerWeek) + 1;
        return { workout, weekNumber: weekNum, dayInWeek };
      })
      .filter(weekWorkout => weekWorkout.weekNumber === weekNumber)
      .sort((a, b) => a.dayInWeek - b.dayInWeek);
  }, [workouts, activeProgram, weekNumber]);

  const openGenerateDialog = (program: Program) => {
    setSelectedProgram(program);
    setGenerateDialogOpen(true);
  };

  const handleWorkoutClick = (workoutId: number) => {
    const newSearchParams = new URLSearchParams(searchParams);
    newSearchParams.set('workout', workoutId.toString());
    navigate(`?${newSearchParams.toString()}`);
  };

  const handleBackToWorkouts = () => {
    const newSearchParams = new URLSearchParams(searchParams);
    newSearchParams.delete('workout');
    newSearchParams.delete('week');
    navigate(`?${newSearchParams.toString()}`);
  };

  const handleBackToWeekList = () => {
    const newSearchParams = new URLSearchParams(searchParams);
    newSearchParams.delete('workout');
    navigate(`?${newSearchParams.toString()}`);
  };

  const handleGenerateWorkouts = async () => {
    if (!selectedProgram) return;

    setIsGenerating(true);
    try {
      await generateNextWeek(selectedProgram.id);

      // Refresh data after generation
      const [programsData, workoutsData] = await Promise.all([
        getPrograms(),
        getProgrammedWorkouts(),
      ]);
      setPrograms(programsData);
      setWorkouts(workoutsData);

      setGenerateDialogOpen(false);
      setSelectedProgram(null);
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
        pb: 3,
      }}
    >
      <Breadcrumbs sx={{ mb: 2 }}>
        <Link
          component="button"
          variant="body1"
          onClick={() => handleBreadcrumbClick('workouts')}
          sx={{ color: 'text.secondary' }}
        >
          Workouts
        </Link>
        <Link
          component="button"
          variant="body1"
          onClick={() => handleBreadcrumbClick('week')}
          sx={{ color: 'text.secondary' }}
        >
          Week {weekNumber}
        </Link>
        {selectedWorkoutId && currentWorkoutDetails && (
          <Typography variant="body1" color="text.primary">
            {currentWorkoutDetails.name} (Day {currentWorkoutDetails.day_number}) •{' '}
            {currentWorkoutDetails.stages} stages
          </Typography>
        )}
        {selectedWorkoutId && !currentWorkoutDetails && (
          <Typography variant="body1" color="text.primary">
            Workout Details
          </Typography>
        )}
      </Breadcrumbs>
    </Box>
  );

  const handleBreadcrumbClick = (path: string) => {
    if (path === 'workouts') {
      handleBackToWorkouts();
    } else if (path === 'week') {
      handleBackToWeekList();
    }
  };

  const handleWorkoutDetailsUpdate = useCallback(
    (workoutDetails: { name: string; day_number: number; stages: number }) => {
      setCurrentWorkoutDetails(workoutDetails);
    },
    []
  );

  return (
    <React.Fragment>
      {renderBreadcrumbs()}
      {!activeProgram ? (
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              No Active Program
            </Typography>
            <Typography variant="body2" color="text.secondary" paragraph>
              You need to create a program first before you can generate and view workouts. Please
              go to the Programs section to create a program.
            </Typography>
          </CardContent>
        </Card>
      ) : (
        <Box sx={{ display: 'flex', gap: 3 }}>
          {/* Workout List - Slides right when workout is selected */}
          <Slide direction="right" in={!selectedWorkoutId} mountOnEnter unmountOnExit>
            <Box sx={{ flex: 1, minWidth: 0 }}>
              <Grid container spacing={3}>
                {/* Week Overview and Generation */}
                <Grid item xs={12}>
                  <Card>
                    <CardContent>
                      <Box display="flex" justifyContent="space-between" alignItems="center">
                        <Box>
                          <Typography variant="h6" gutterBottom>
                            {activeProgram.name} - Week {weekNumber}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            {weekWorkouts.length} workouts • Week {weekNumber} of {activeProgram.current_week_number}
                          </Typography>
                        </Box>
                        <Box display="flex" gap={1}>
                          <Button
                            variant="contained"
                            startIcon={isGenerating ? <CircularProgress size={16} /> : <AddIcon />}
                            onClick={() => openGenerateDialog(activeProgram)}
                            disabled={isGenerating}
                          >
                            {isGenerating ? 'Generating...' : 'Generate Next Week'}
                          </Button>
                        </Box>
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>

                {/* Week Workout List */}
                <Grid item xs={12}>
                  <Card>
                    <CardContent>
                      <Typography variant="h6" gutterBottom>
                        Week {weekNumber} Workouts
                      </Typography>
                      {isLoading ? (
                        <Box display="flex" justifyContent="center" p={3}>
                          <CircularProgress />
                        </Box>
                      ) : weekWorkouts.length === 0 ? (
                        <Typography variant="body2" color="text.secondary">
                          No workouts found for Week {weekNumber}. 
                          {weekNumber === activeProgram.current_week_number && 
                            ' Click "Generate Next Week" to create new workouts.'}
                        </Typography>
                      ) : (
                        <List>
                          {weekWorkouts.map((weekWorkout) => (
                            <ListItem
                              key={weekWorkout.workout.id}
                              disablePadding
                              sx={{
                                cursor: 'pointer',
                                '&:hover': { backgroundColor: 'action.hover' },
                              }}
                              onClick={() => handleWorkoutClick(weekWorkout.workout.id)}
                            >
                              <ListItemText
                                primary={`Day ${weekWorkout.dayInWeek}`}
                                secondary={`${weekWorkout.workout.name || `Workout ${weekWorkout.workout.day_number}`}`}
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

          {/* Workout Details - Slides in from left when workout is selected */}
          {selectedWorkoutId && (
            <Slide direction="left" in={!!selectedWorkoutId} mountOnEnter unmountOnExit>
              <Box sx={{ flex: 1, minWidth: 0 }}>
                <WorkoutDetail
                  workoutId={parseInt(selectedWorkoutId)}
                  onBack={handleBackToWeekList}
                  onWorkoutDetailsUpdate={handleWorkoutDetailsUpdate}
                />
              </Box>
            </Slide>
          )}
        </Box>
      )}

      {/* Generate Workouts Dialog */}
      <Dialog open={generateDialogOpen} onClose={() => setGenerateDialogOpen(false)}>
        <DialogTitle>Generate Workouts</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Generate next week&apos;s workouts for {selectedProgram?.name}?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setGenerateDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleGenerateWorkouts} variant="contained">
            Generate
          </Button>
        </DialogActions>
      </Dialog>
    </React.Fragment>
  );
};
