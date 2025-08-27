import { default as CalendarTodayIcon } from '@mui/icons-material/CalendarToday';
import { default as FitnessCenterIcon } from '@mui/icons-material/FitnessCenter';
import { default as ShowChartIcon } from '@mui/icons-material/ShowChart';
import { default as TrendingUpIcon } from '@mui/icons-material/TrendingUp';
import { Box, Card, CardContent, Grid, Typography, CircularProgress, Chip } from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';

import { ExerciseHistory } from './ExerciseHistory';
import { getPrograms } from '../api/program';
import type { User, Program, UserOneRepMax } from '../api/types';
import { getUserOneRepMaxes } from '../api/userOneRepMax';

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
  const { enqueueSnackbar } = useSnackbar();
  const [programs, setPrograms] = useState<Program[]>([]);
  const [oneRepMaxes, setOneRepMaxes] = useState<UserOneRepMax[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const handleActiveProgramClick = () => {
    if (activeProgram) {
      navigate('/dashboard?section=workouts');
    }
  };

  useEffect(() => {
    const loadDashboardData = async () => {
      try {
        setIsLoading(true);

        // Load all dashboard data in parallel
        const [programsData, oneRepMaxesData] = await Promise.all([
          getPrograms(),
          getUserOneRepMaxes(user.keycloak_id),
        ]);

        setPrograms(programsData);
        setOneRepMaxes(oneRepMaxesData);
      } catch {
        enqueueSnackbar('Failed to load dashboard data. Please try again.', { variant: 'error' });
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

  const activeProgram = programs.find(program => program.is_active);
  const totalWorkouts = programs.reduce((total, program) => total + program.current_week_number, 0);
  const recentOneRepMaxes = oneRepMaxes.slice(-5); // Last 5 1RMs

  return (
    <React.Fragment>
      <Typography variant="h5" gutterBottom>
        Dashboard Overview
      </Typography>

      {/* Key Statistics Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ height: '100%' }}>
            <CardContent
              sx={{
                height: '100%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
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
            <CardContent
              sx={{
                height: '100%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
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
            <CardContent
              sx={{
                height: '100%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <Box display="flex" flexDirection="column" alignItems="center" gap={2}>
                <TrendingUpIcon color="primary" />
                <Box display="flex" flexDirection="column" alignItems="center">
                  <Typography variant="h4" component="div" textAlign="center">
                    {new Set(oneRepMaxes.map(orm => orm.exercise_name)).size}
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
            <CardContent
              sx={{
                height: '100%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
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
              <Chip label="Active" color="success" size="small" />
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
      <ExerciseHistory user={user} />

      {/* No Data State */}
      {!activeProgram && recentOneRepMaxes.length === 0 && (
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Welcome to Your Dashboard!
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Start by creating your first program and tracking your 1RM values to see your progress
              here.
            </Typography>
          </CardContent>
        </Card>
      )}
    </React.Fragment>
  );
};
