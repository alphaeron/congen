import { default as PlayArrowIcon } from '@mui/icons-material/PlayArrow';
import { default as PauseIcon } from '@mui/icons-material/Pause';
import { default as CheckCircleIcon } from '@mui/icons-material/CheckCircle';
import { default as AddIcon } from '@mui/icons-material/Add';
import { default as RefreshIcon } from '@mui/icons-material/Refresh';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Grid,
  Alert,
  CircularProgress,
  Chip,
  List,
  ListItem,
  ListItemText,
  Divider,
  LinearProgress,
  IconButton,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  DialogContentText,
} from '@mui/material';
import React, { useEffect, useState } from 'react';

import { getPrograms } from '../api/program';
import { getProgrammedWorkouts } from '../api/programmedWorkout';
import { generateNextWeek } from '../api/conjugateWorkoutGenerator';
import type { User, Program, ProgrammedWorkout } from '../api/types';

interface WorkoutsProps {
  user: User;
}

/**
 * Workouts component for managing and viewing workout programs.
 *
 * Displays current workouts, allows generation of new workouts,
 * and provides workout flow functionality with sets, reps,
 * rest periods, and cues.
 *
 * @param user The user data
 * @return Workouts component
 */
export const Workouts: React.FC<WorkoutsProps> = ({ user }) => {
  const [programs, setPrograms] = useState<Program[]>([]);
  const [workouts, setWorkouts] = useState<ProgrammedWorkout[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentWorkout, setCurrentWorkout] = useState<ProgrammedWorkout | null>(null);
  const [workoutProgress, setWorkoutProgress] = useState(0);
  const [isWorkoutActive, setIsWorkoutActive] = useState(false);
  const [isGenerating, setIsGenerating] = useState(false);
  const [generateDialogOpen, setGenerateDialogOpen] = useState(false);
  const [selectedProgram, setSelectedProgram] = useState<Program | null>(null);

  useEffect(() => {
    loadWorkoutData();
  }, []);

  const loadWorkoutData = async () => {
    try {
      setIsLoading(true);
      setError(null);

      const [programsData, workoutsData] = await Promise.all([
        getPrograms(),
        getProgrammedWorkouts(),
      ]);

      setPrograms(programsData);
      setWorkouts(workoutsData);

      // Find active program and today's workout
      const activeProgram = programsData.find(program => program.is_active);
      if (activeProgram) {
        const todayWorkout = workoutsData.find(workout => 
          workout.program_id === activeProgram.id
        );
        setCurrentWorkout(todayWorkout || null);
      }
    } catch (err) {
      console.error('Error loading workout data:', err);
      setError('Failed to load workout data. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleGenerateWorkouts = async () => {
    if (!selectedProgram) return;

    try {
      setIsGenerating(true);
      setError(null);

      const updatedProgram = await generateNextWeek(selectedProgram.id);
      
      // Update the programs list with the updated program
      setPrograms(prev => prev.map(p => p.id === updatedProgram.id ? updatedProgram : p));
      
      // Reload workouts to get the newly generated ones
      const newWorkouts = await getProgrammedWorkouts();
      setWorkouts(newWorkouts);

      // Find the new workout for today
      const todayWorkout = newWorkouts.find(workout => 
        workout.program_id === updatedProgram.id
      );
      setCurrentWorkout(todayWorkout || null);

      setGenerateDialogOpen(false);
      setSelectedProgram(null);
    } catch (err) {
      console.error('Error generating workouts:', err);
      
      // Provide more specific error messages based on the error
      if (err && typeof err === 'object' && 'error' in err) {
        const errorMessage = (err as any).error;
        if (errorMessage === 'Resource not found') {
          setError('Program not found or you don\'t have the required data set up. Please ensure you have created a program.');
        } else if (errorMessage.includes('Access denied')) {
          setError('Access denied. Please ensure you own this program.');
        } else {
          setError(`Failed to generate workouts: ${errorMessage}`);
        }
      } else {
        setError('Failed to generate workouts. Please try again.');
      }
    } finally {
      setIsGenerating(false);
    }
  };

  const startWorkout = () => {
    setIsWorkoutActive(true);
    setWorkoutProgress(0);
  };

  const pauseWorkout = () => {
    setIsWorkoutActive(false);
  };

  const completeWorkout = () => {
    setIsWorkoutActive(false);
    setWorkoutProgress(100);
    // TODO: Save workout completion to backend
  };

  const getActiveProgram = () => {
    return programs.find(program => program.is_active);
  };

  const getWorkoutsForActiveProgram = () => {
    const activeProgram = getActiveProgram();
    if (!activeProgram) return [];
    return workouts.filter(workout => workout.program_id === activeProgram.id);
  };

  const openGenerateDialog = (program: Program) => {
    setSelectedProgram(program);
    setGenerateDialogOpen(true);
  };

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight={400}>
        <CircularProgress />
      </Box>
    );
  }

  const activeProgram = getActiveProgram();
  const programWorkouts = getWorkoutsForActiveProgram();

  return (
    <React.Fragment>
      <Typography variant="h5" gutterBottom>
        Workouts
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {!activeProgram ? (
        <Card>
          <CardContent sx={{ textAlign: 'center', py: 4 }}>
            <Typography variant="h6" gutterBottom>
              No Active Program
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Create or activate a program to start your workouts.
            </Typography>
          </CardContent>
        </Card>
      ) : (
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
                      Week {activeProgram.current_week_number} • {programWorkouts.length} workouts
                    </Typography>
                  </Box>
                  <Box display="flex" gap={1}>
                    <Button
                      variant="contained"
                      startIcon={<AddIcon />}
                      onClick={() => openGenerateDialog(activeProgram)}
                      disabled={isGenerating}
                    >
                      Generate Workouts
                    </Button>
                    <Button
                      variant="outlined"
                      startIcon={<RefreshIcon />}
                      onClick={loadWorkoutData}
                      disabled={isLoading}
                    >
                      Refresh
                    </Button>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>

          {/* Current Workout Section */}
          <Grid item xs={12} lg={8}>
            <Card>
              <CardContent>
                <Box display="flex" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
                  <Typography variant="h6">
                    Today's Workout
                  </Typography>
                  <Box display="flex" gap={1}>
                    {isWorkoutActive ? (
                      <Button
                        variant="outlined"
                        startIcon={<PauseIcon />}
                        onClick={pauseWorkout}
                      >
                        Pause
                      </Button>
                    ) : (
                      <Button
                        variant="contained"
                        startIcon={<PlayArrowIcon />}
                        onClick={startWorkout}
                        disabled={!currentWorkout}
                      >
                        Start Workout
                      </Button>
                    )}
                    <Button
                      variant="contained"
                      color="success"
                      startIcon={<CheckCircleIcon />}
                      onClick={completeWorkout}
                      disabled={!isWorkoutActive}
                    >
                      Complete
                    </Button>
                  </Box>
                </Box>

                {currentWorkout ? (
                  <React.Fragment>
                    <Box sx={{ mb: 3 }}>
                      <Typography variant="h5" gutterBottom>
                        {currentWorkout.name}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Day {currentWorkout.day_number} • {activeProgram.name}
                      </Typography>
                    </Box>

                    {/* Workout Progress */}
                    {isWorkoutActive && (
                      <Box sx={{ mb: 3 }}>
                        <Box display="flex" justifyContent="space-between" alignItems="center" sx={{ mb: 1 }}>
                          <Typography variant="body2">Progress</Typography>
                          <Typography variant="body2">{workoutProgress}%</Typography>
                        </Box>
                        <LinearProgress variant="determinate" value={workoutProgress} />
                      </Box>
                    )}

                    {/* Mock Workout Structure - Replace with actual workout data */}
                    <Box>
                      <Typography variant="h6" gutterBottom>
                        Workout Structure
                      </Typography>
                      <List>
                        <ListItem>
                          <ListItemText
                            primary="Warm-up Sets"
                            secondary="3 sets × 8-10 reps at 50-60% 1RM"
                          />
                          <Chip label="Completed" color="success" size="small" />
                        </ListItem>
                        <Divider />
                        <ListItem>
                          <ListItemText
                            primary="Working Sets"
                            secondary="4 sets × 5-6 reps at 80-85% 1RM"
                          />
                          <Chip label="In Progress" color="primary" size="small" />
                        </ListItem>
                        <Divider />
                        <ListItem>
                          <ListItemText
                            primary="Accessory Work"
                            secondary="3 sets × 10-12 reps"
                          />
                          <Chip label="Pending" color="default" size="small" />
                        </ListItem>
                      </List>
                    </Box>
                  </React.Fragment>
                ) : (
                  <Box sx={{ textAlign: 'center', py: 4 }}>
                    <Typography variant="h6" gutterBottom>
                      No Workout Scheduled
                    </Typography>
                    <Typography variant="body1" color="text.secondary">
                      Generate workouts for your program to get started.
                    </Typography>
                  </Box>
                )}
              </CardContent>
            </Card>
          </Grid>

          {/* This Week's Workouts */}
          <Grid item xs={12} lg={4}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  This Week's Workouts
                </Typography>
                
                {programWorkouts.length > 0 ? (
                  <List>
                    {programWorkouts.map((workout, index) => (
                      <React.Fragment key={workout.id}>
                        <ListItem>
                          <ListItemText
                            primary={workout.name}
                            secondary={`Day ${workout.day_number}`}
                          />
                          <Box display="flex" gap={1}>
                            {workout.id === currentWorkout?.id && (
                              <Chip label="Today" color="primary" size="small" />
                            )}
                            <Tooltip title="Start this workout">
                              <IconButton
                                size="small"
                                onClick={() => setCurrentWorkout(workout)}
                              >
                                <PlayArrowIcon />
                              </IconButton>
                            </Tooltip>
                          </Box>
                        </ListItem>
                        {index < programWorkouts.length - 1 && <Divider />}
                      </React.Fragment>
                    ))}
                  </List>
                ) : (
                  <Typography variant="body2" color="text.secondary">
                    No workouts scheduled for this week.
                  </Typography>
                )}
              </CardContent>
            </Card>

            {/* Quick Stats */}
            <Card sx={{ mt: 2 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Quick Stats
                </Typography>
                <Box display="flex" justifyContent="space-between" sx={{ mb: 1 }}>
                  <Typography variant="body2">Workouts This Week</Typography>
                  <Typography variant="body2">{programWorkouts.length}</Typography>
                </Box>
                <Box display="flex" justifyContent="space-between" sx={{ mb: 1 }}>
                  <Typography variant="body2">Current Week</Typography>
                  <Typography variant="body2">{activeProgram.current_week_number}</Typography>
                </Box>
                <Box display="flex" justifyContent="space-between">
                  <Typography variant="body2">Program</Typography>
                  <Typography variant="body2">{activeProgram.name}</Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {/* Generate Workouts Dialog */}
      <Dialog
        open={generateDialogOpen}
        onClose={() => setGenerateDialogOpen(false)}
        aria-labelledby="generate-dialog-title"
        aria-describedby="generate-dialog-description"
      >
        <DialogTitle id="generate-dialog-title">
          Generate Workouts
        </DialogTitle>
        <DialogContent>
          <DialogContentText id="generate-dialog-description">
            This will generate the next week of workouts for "{selectedProgram?.name}". 
            The workouts will be based on your preferences and available equipment.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setGenerateDialogOpen(false)} disabled={isGenerating}>
            Cancel
          </Button>
          <Button
            onClick={handleGenerateWorkouts}
            variant="contained"
            disabled={isGenerating}
            startIcon={isGenerating ? <CircularProgress size={16} /> : <AddIcon />}
          >
            {isGenerating ? 'Generating...' : 'Generate'}
          </Button>
        </DialogActions>
      </Dialog>
    </React.Fragment>
  );
};
