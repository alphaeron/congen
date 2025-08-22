import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Grid,
  Alert,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Chip,
  List,
  ListItem,
  ListItemText,
  Divider,
  IconButton,
  Tooltip,
  Breadcrumbs,
  Link,
  Slide,
} from '@mui/material';
import {
  FitnessCenter as FitnessCenterIcon,
  Add as AddIcon,
  Refresh as RefreshIcon,
  ArrowBack as ArrowBackIcon,
  CalendarToday as CalendarIcon,
  TrendingUp as TrendingUpIcon,
} from '@mui/icons-material';
import { useNavigate, useSearchParams } from 'react-router';

import { getPrograms } from '../api/program';
import { getProgrammedWorkouts } from '../api/programmedWorkout';
import { generateNextWeek } from '../api/conjugateWorkoutGenerator';
import { WorkoutDetail } from './WorkoutDetail';
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
  
  const [programs, setPrograms] = useState<Program[]>([]);
  const [workouts, setWorkouts] = useState<ProgrammedWorkout[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isGenerating, setIsGenerating] = useState(false);
  const [generateDialogOpen, setGenerateDialogOpen] = useState(false);
  const [selectedProgram, setSelectedProgram] = useState<Program | null>(null);

  // URL query parameters
  const selectedWorkoutId = searchParams.get('workout') || selectedWorkout;
  const selectedSection = searchParams.get('section') || 'overview';

  // Load workout data
  useEffect(() => {
    const loadWorkoutData = async () => {
      setIsLoading(true);
      setError(null);
      try {
        const [programsData, workoutsData] = await Promise.all([
          getPrograms(),
          getProgrammedWorkouts(),
        ]);

        setPrograms(programsData);
        setWorkouts(workoutsData);
      } catch (err) {
        console.error('Error loading workout data:', err);
        setError('Failed to load workout data. Please try again.');
      } finally {
        setIsLoading(false);
      }
    };

    loadWorkoutData();
  }, []); // Only load once on mount

  const activeProgram = programs.find(program => program.is_active);
  const programWorkouts = workouts.filter(workout => 
    activeProgram && workout.program_id === activeProgram.id
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

  const handleBreadcrumbClick = (path: string) => {
    const newSearchParams = new URLSearchParams(searchParams);
    if (path === 'workouts') {
      newSearchParams.delete('workout');
    }
    navigate(`?${newSearchParams.toString()}`);
  };

  const handleGenerateWorkouts = async () => {
    if (!selectedProgram) return;

    setIsGenerating(true);
    setError(null);
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
    } catch (err) {
      console.error('Error generating workouts:', err);
      setError('Failed to generate workouts. Please try again.');
    } finally {
      setIsGenerating(false);
    }
  };

  // Render breadcrumbs
  const renderBreadcrumbs = () => (
    <Breadcrumbs sx={{ mb: 2 }}>
      <Link
        component="button"
        variant="body1"
        onClick={() => handleBreadcrumbClick('workouts')}
        sx={{ color: 'text.secondary' }}
      >
        Workouts
      </Link>
      {selectedWorkoutId && (
        <Typography variant="body1" color="text.primary">
          Workout Details
        </Typography>
      )}
    </Breadcrumbs>
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
              You need to create a program first before you can generate and view workouts.
            </Typography>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => navigate('/programs')}
            >
              Create Program
            </Button>
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
                            Week {activeProgram.current_week_number} • {programWorkouts.length} workouts
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
                          No workouts generated yet. Click "Generate Workouts" to create your first workout.
                        </Typography>
                      ) : (
                        <List>
                          {programWorkouts.map((workout, index) => (
                            <ListItem 
                              key={workout.id} 
                              disablePadding
                              sx={{ 
                                cursor: 'pointer',
                                '&:hover': { backgroundColor: 'action.hover' }
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
            Generate next week's workouts for {selectedProgram?.name}?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setGenerateDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleGenerateWorkouts} variant="contained">
            Generate
          </Button>
        </DialogActions>
      </Dialog>

      {/* Error Display */}
      {error && (
        <Alert severity="error" sx={{ mt: 2 }}>
          {error}
        </Alert>
      )}
    </React.Fragment>
  );
};
