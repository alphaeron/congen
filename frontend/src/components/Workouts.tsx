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
import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, useSearchParams } from 'react-router';

import { WorkoutAnalytics } from './WorkoutAnalytics';
import { WorkoutDetail } from './WorkoutDetail';
import { generateNextWeek } from '../api/conjugateWorkoutGenerator';
import { getPrograms } from '../api/program';
import { getProgrammedWorkouts } from '../api/programmedWorkout';
import type { Program, ProgrammedWorkout, User } from '../api/types';

interface WorkoutsProps {
  user: User;
  selectedWorkout?: string | null;
}

/**
 * Workouts component for managing and viewing workout programs.
 *
 * Features:
 * - Display active program and its workouts
 * - Generate new workouts for programs
 * - View workout details with slide-left animation
 * - Auto-refresh functionality after workout generation
 * - URL query parameters for workout selection
 *
 * @param user The current user object
 * @param selectedWorkout The selected workout ID (from URL)
 * @returns Workouts component
 */
export const Workouts: React.FC<WorkoutsProps> = ({ user, selectedWorkout }) => {
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
  const programWorkouts = workouts.filter(
    workout => activeProgram && workout.program_id === activeProgram.id
  );

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
      // Use the same pattern as handleBackToWorkouts
      handleBackToWorkouts();
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
                {/* Program Overview and Generation */}
                <Grid item xs={12}>
                  <Card>
                    <CardContent>
                      <Box display="flex" justifyContent="space-between" alignItems="center">
                        <Box>
                          <Typography variant="h6" gutterBottom>
                            {activeProgram.name}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            Week {activeProgram.current_week_number} • {programWorkouts.length}{' '}
                            workouts
                          </Typography>
                        </Box>
                        <Box display="flex" gap={1}>
                          <Button
                            variant="contained"
                            startIcon={isGenerating ? <CircularProgress size={16} /> : <AddIcon />}
                            onClick={() => openGenerateDialog(activeProgram)}
                            disabled={isGenerating}
                          >
                            {isGenerating ? 'Generating...' : 'Generate Workouts'}
                          </Button>
                        </Box>
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>

                {/* Workout List */}
                <Grid item xs={12}>
                  <Card>
                    <CardContent>
                      <Typography variant="h6" gutterBottom>
                        Workouts
                      </Typography>
                      {isLoading ? (
                        <Box display="flex" justifyContent="center" p={3}>
                          <CircularProgress />
                        </Box>
                      ) : programWorkouts.length === 0 ? (
                        <Typography variant="body2" color="text.secondary">
                          No workouts generated yet. Click &quot;Generate Workouts&quot; to create
                          your first workout.
                        </Typography>
                      ) : (
                        <List>
                          {programWorkouts.map((workout, index) => (
                            <ListItem
                              key={workout.id}
                              disablePadding
                              sx={{
                                cursor: 'pointer',
                                '&:hover': { backgroundColor: 'action.hover' },
                              }}
                              onClick={() => handleWorkoutClick(workout.id)}
                            >
                              <ListItemText
                                primary={`Day ${index + 1}`}
                                secondary={`${workout.name || `Workout ${index + 1}`}`}
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
                  onBack={handleBackToWorkouts}
                  onWorkoutDetailsUpdate={handleWorkoutDetailsUpdate}
                />
              </Box>
            </Slide>
          )}
        </Box>
      )}

      {/* Workout Analytics Section */}
      <WorkoutAnalytics user={user} />

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
