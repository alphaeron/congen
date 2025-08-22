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
import { useAutoRefresh } from '../hooks/useAutoRefresh';
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

  // Auto-refresh hook for workout data
  const { executeAndRefresh: refreshWorkoutData } = useAutoRefresh(async () => {
    const [programsData, workoutsData] = await Promise.all([
      getPrograms(),
      getProgrammedWorkouts(),
    ]);

    setPrograms(programsData);
    setWorkouts(workoutsData);

    return { programs: programsData, workouts: workoutsData };
  });

  useEffect(() => {
    // Initial load using the refresh function
    const initialLoad = async () => {
      setIsLoading(true);
      setError(null);
      try {
        await refreshWorkoutData(() => Promise.resolve()); // Trigger initial load
      } catch (err) {
        console.error('Error loading workout data:', err);
        setError('Failed to load workout data. Please try again.');
      } finally {
        setIsLoading(false);
      }
    };
    initialLoad();
  }, []); // Empty dependency array to run once on mount

  const activeProgram = programs.find(program => program.is_active);
  const programWorkouts = workouts.filter(workout => 
    activeProgram && workout.program_id === activeProgram.id
  );

  const handleGenerateWorkouts = async () => {
    if (!selectedProgram) return;

    try {
      setIsGenerating(true);
      setError(null);

      // Use the auto-refresh hook to execute the generation and refresh data
      await refreshWorkoutData(async () => {
        const updatedProgram = await generateNextWeek(selectedProgram.id);
        return updatedProgram;
      });

      setGenerateDialogOpen(false);
      setSelectedProgram(null);
    } catch (err) {
      console.error('Error generating workouts:', err);
      // Enhanced error handling
      if (err && typeof err === 'object' && 'error' in err) {
        const errorMessage = (err as any).error;
        if (errorMessage === 'Resource not found') {
          setError('Program not found or you don\'t have the required data set up. Please ensure you have created a program.');
        } else if (errorMessage.includes('Access denied')) {
          setError('Access denied. Please ensure you own this program.');
        } else if (errorMessage.includes('Unauthorized') || errorMessage.includes('401')) {
          setError('Authentication error. Please refresh the page and try again.');
        } else if (errorMessage.includes('timeout')) {
          setError('Workout generation is taking longer than expected. Please wait and try again.');
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

  const openGenerateDialog = (program: Program) => {
    setSelectedProgram(program);
    setGenerateDialogOpen(true);
  };

  const handleWorkoutClick = (workoutId: number) => {
    const newSearchParams = new URLSearchParams(searchParams);
    newSearchParams.set('workout', workoutId.toString());
    newSearchParams.set('section', 'detail');
    navigate(`?${newSearchParams.toString()}`);
  };

  const handleBackToWorkouts = () => {
    const newSearchParams = new URLSearchParams(searchParams);
    newSearchParams.delete('workout');
    newSearchParams.set('section', 'overview');
    navigate(`?${newSearchParams.toString()}`);
  };

  // If showing workout detail, render the detail component
  if (selectedSection === 'detail' && selectedWorkoutId) {
    return (
      <WorkoutDetail 
        workoutId={parseInt(selectedWorkoutId)} 
        onBack={handleBackToWorkouts}
      />
    );
  }

  return (
    <React.Fragment>
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
                        sx={{ cursor: 'pointer' }}
                        onClick={() => handleWorkoutClick(workout.id)}
                      >
                        <ListItemText
                          primary={workout.name}
                          secondary={`Day ${workout.day_number} • Created ${new Date(workout.created_at).toLocaleDateString()}`}
                        />
                        <Chip 
                          label={`Day ${workout.day_number}`}
                          size="small"
                          color="primary"
                          variant="outlined"
                        />
                      </ListItem>
                    ))}
                  </List>
                )}
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {/* Workout Detail View */}
      {selectedWorkoutId && selectedSection === 'detail' && (
        <WorkoutDetail 
          workoutId={parseInt(selectedWorkoutId)} 
          onBack={handleBackToWorkouts}
        />
      )}

      {/* Generate Workouts Dialog */}
      <Dialog open={generateDialogOpen} onClose={() => setGenerateDialogOpen(false)}>
        <DialogTitle>Generate Workouts</DialogTitle>
        <DialogContent>
          <Typography>
            Generate the next week of workouts for "{selectedProgram?.name}"?
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setGenerateDialogOpen(false)}>Cancel</Button>
          <Button 
            onClick={handleGenerateWorkouts} 
            variant="contained"
            disabled={isGenerating}
          >
            {isGenerating ? 'Generating...' : 'Generate'}
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
