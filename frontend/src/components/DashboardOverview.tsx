import { default as TrendingUpIcon } from '@mui/icons-material/TrendingUp';
import { default as FitnessCenterIcon } from '@mui/icons-material/FitnessCenter';
import { default as CalendarTodayIcon } from '@mui/icons-material/CalendarToday';
import { default as ShowChartIcon } from '@mui/icons-material/ShowChart';
import {
  Box,
  Card,
  CardContent,
  Grid,
  Typography,
  Alert,
  CircularProgress,
  Tooltip,
  Chip,
  Button,
} from '@mui/material';
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';

import { getPrograms } from '../api/program';
import { getUserOneRepMaxes } from '../api/userOneRepMax';
import { getExerciseRotationHistory } from '../api/exerciseRotationHistory';
import type { User, Program, UserOneRepMax, ExerciseRotationHistory } from '../api/types';

interface DashboardOverviewProps {
  user: User;
}

/**
 * Dashboard overview component displaying user progress and statistics.
 *
 * Shows user progress over time, 1RM graphs, exercise trends,
 * and key statistics like volume, frequency, and PRs.
 *
 * @param user The user data to display
 * @return Dashboard overview component
 */
export const DashboardOverview: React.FC<DashboardOverviewProps> = ({ user }) => {
  const navigate = useNavigate();
  const [programs, setPrograms] = useState<Program[]>([]);
  const [oneRepMaxes, setOneRepMaxes] = useState<UserOneRepMax[]>([]);
  const [exerciseHistory, setExerciseHistory] = useState<ExerciseRotationHistory[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const handleActiveProgramClick = () => {
    if (activeProgram) {
      navigate('/dashboard?section=workouts');
    }
  };

  useEffect(() => {
    const loadDashboardData = async () => {
      try {
        setIsLoading(true);
        setError(null);

        // Load all dashboard data in parallel
        const [programsData, oneRepMaxesData, exerciseHistoryData] = await Promise.all([
          getPrograms(),
          getUserOneRepMaxes(user.keycloak_id),
          getExerciseRotationHistory(),
        ]);

        setPrograms(programsData);
        setOneRepMaxes(oneRepMaxesData);
        setExerciseHistory(exerciseHistoryData);
      } catch (err) {
        console.error('Error loading dashboard data:', err);
        setError('Failed to load dashboard data. Please try again.');
      } finally {
        setIsLoading(false);
      }
    };

    loadDashboardData();
  }, [user.keycloak_id]);

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight={400}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return <Alert severity="error">{error}</Alert>;
  }

  const activeProgram = programs.find(program => program.is_active);
  const totalWorkouts = programs.reduce((total, program) => total + program.current_week_number, 0);
  const recentOneRepMaxes = oneRepMaxes.slice(-5); // Last 5 1RMs
  const recentExerciseHistory = exerciseHistory.slice(-10); // Last 10 exercises

  // Calculate statistics
  const uniqueExercises = new Set(exerciseHistory.map(history => history.exercise_name)).size;
  const accessoryExercises = exerciseHistory.filter(history => history.is_accessory).length;
  const primaryExercises = exerciseHistory.filter(history => !history.is_accessory).length;

  return (
    <React.Fragment>
      <Typography variant="h5" gutterBottom>
        Dashboard Overview
      </Typography>

      {/* Key Statistics Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ height: '100%' }}>
            <CardContent sx={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Box display="flex" flexDirection="column" alignItems="center" gap={2}>
                <FitnessCenterIcon color="primary" />
                <Box display="flex" flexDirection="column" alignItems="center">
                  <Typography variant="h4" component="div" textAlign="center">
                    {totalWorkouts}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" textAlign="center">
                    Total Workouts
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ height: '100%' }}>
            <CardContent sx={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Box display="flex" flexDirection="column" alignItems="center" gap={2}>
                <ShowChartIcon color="primary" />
                <Box display="flex" flexDirection="column" alignItems="center">
                  <Typography variant="h4" component="div" textAlign="center">
                    {oneRepMaxes.length}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" textAlign="center">
                    1RM Records
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ height: '100%' }}>
            <CardContent sx={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Box display="flex" flexDirection="column" alignItems="center" gap={2}>
                <TrendingUpIcon color="primary" />
                <Box display="flex" flexDirection="column" alignItems="center">
                  <Typography variant="h4" component="div" textAlign="center">
                    {uniqueExercises}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" textAlign="center">
                    Unique Exercises
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ height: '100%' }}>
            <CardContent sx={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Box display="flex" flexDirection="column" alignItems="center" gap={2}>
                <CalendarTodayIcon color="primary" />
                <Box display="flex" flexDirection="column" alignItems="center">
                  <Typography variant="h4" component="div" textAlign="center">
                    {activeProgram ? activeProgram.current_week_number : 0}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" textAlign="center">
                    Current Week
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Active Program Section */}
      {activeProgram && (
        <Card sx={{ mb: 4, cursor: 'pointer' }} onClick={handleActiveProgramClick}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Active Program
            </Typography>
            <Box display="flex" alignItems="center" gap={2} flexWrap="wrap">
              <Typography variant="body1" fontWeight="medium">
                {activeProgram.name}
              </Typography>
              <Chip 
                label={`Week ${activeProgram.current_week_number}`} 
                color="primary" 
                size="small" 
              />
              <Chip 
                label="Active" 
                color="success" 
                size="small" 
              />
            </Box>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              Click to view workouts
            </Typography>
          </CardContent>
        </Card>
      )}

      {/* Recent 1RM Section */}
      {recentOneRepMaxes.length > 0 && (
        <Card sx={{ mb: 4 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Recent 1RM Records
            </Typography>
            <Grid container spacing={2}>
              {recentOneRepMaxes.map((oneRepMax, index) => (
                <Grid item xs={12} sm={6} md={4} key={index}>
                  <Box
                    sx={{
                      p: 2,
                      border: 1,
                      borderColor: 'divider',
                      borderRadius: 1,
                    }}
                  >
                    <Typography variant="body1" fontWeight="medium">
                      {oneRepMax.exercise_name}
                    </Typography>
                    <Typography variant="h6" color="primary">
                      {oneRepMax.one_rep_max} {oneRepMax.unit}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      Updated: {new Date(oneRepMax.updated_at).toLocaleDateString()}
                    </Typography>
                  </Box>
                </Grid>
              ))}
            </Grid>
          </CardContent>
        </Card>
      )}

      {/* Exercise History Section */}
      {recentExerciseHistory.length > 0 && (
        <Card sx={{ mb: 4 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Recent Exercise History
            </Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} md={6}>
                <Typography variant="subtitle1" gutterBottom>
                  Primary Exercises ({primaryExercises})
                </Typography>
                <Box display="flex" flexWrap="wrap" gap={1}>
                  {exerciseHistory
                    .filter(history => !history.is_accessory)
                    .slice(-5)
                    .map((history, index) => (
                      <Tooltip
                        key={index}
                        title={`Used on ${new Date(history.created_at).toLocaleDateString()}`}
                      >
                        <Chip
                          label={history.exercise_name}
                          size="small"
                          variant="outlined"
                        />
                      </Tooltip>
                    ))}
                </Box>
              </Grid>
              <Grid item xs={12} md={6}>
                <Typography variant="subtitle1" gutterBottom>
                  Accessory Exercises ({accessoryExercises})
                </Typography>
                <Box display="flex" flexWrap="wrap" gap={1}>
                  {exerciseHistory
                    .filter(history => history.is_accessory)
                    .slice(-5)
                    .map((history, index) => (
                      <Tooltip
                        key={index}
                        title={`Used on ${new Date(history.created_at).toLocaleDateString()}`}
                      >
                        <Chip
                          label={history.exercise_name}
                          size="small"
                          variant="outlined"
                          color="secondary"
                        />
                      </Tooltip>
                    ))}
                </Box>
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      )}

      {/* No Data State */}
      {!activeProgram && recentOneRepMaxes.length === 0 && recentExerciseHistory.length === 0 && (
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Welcome to Your Dashboard!
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Start by creating your first program and tracking your 1RM values to see your progress here.
            </Typography>
          </CardContent>
        </Card>
      )}
    </React.Fragment>
  );
};
