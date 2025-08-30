import { default as InfoIcon } from '@mui/icons-material/Info';
import { default as ShowChartIcon } from '@mui/icons-material/ShowChart';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  CircularProgress,
  Chip,
  Tooltip,
  Tabs,
  Tab,
  Autocomplete,
  TextField,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router';

import { ExerciseAnalytics } from './ExerciseAnalytics';
import { getExercises } from '../api/exercise';
import type { User, UserOneRepMax, Exercise } from '../api/types';
import { getUserOneRepMaxes } from '../api/userOneRepMax';

interface ExerciseHistoryProps {
  user: User;
}

type TabName = 'onerepmax' | 'usage';

/**
 * Exercise History page component for exercise history and trends.
 *
 * Tracks exercise history per lift and visualizes set/rep schemes,
 * and estimated 1RM with explanatory tooltips.
 *
 * @param user The user data
 * @return Exercise History page component
 */
export const ExerciseHistory: React.FC<ExerciseHistoryProps> = ({ user }) => {
  const [searchParams, setSearchParams] = useSearchParams();
  const { enqueueSnackbar } = useSnackbar();
  const [oneRepMaxes, setOneRepMaxes] = useState<UserOneRepMax[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [selectedExercise, setSelectedExercise] = useState<string>('all');
  const [allExercises, setAllExercises] = useState<Exercise[]>([]);

  // Get active tab from URL parameters, default to 'onerepmax'
  const activeTab = (searchParams.get('tab') as TabName) || 'onerepmax';

  useEffect(() => {
    loadExerciseHistoryData();
  }, [user.keycloak_id]);

  // Update URL when tab changes
  const handleTabChange = (event: React.SyntheticEvent, newValue: TabName) => {
    const newSearchParams = new URLSearchParams(searchParams);
    newSearchParams.set('tab', newValue);
    setSearchParams(newSearchParams);
  };

  const loadExerciseHistoryData = async () => {
    try {
      setIsLoading(true);

      const [oneRepMaxesData, allExercisesData] = await Promise.all([
        getUserOneRepMaxes(user.keycloak_id),
        getExercises(),
      ]);

      setOneRepMaxes(oneRepMaxesData);
      setAllExercises(allExercisesData);
    } catch {
      enqueueSnackbar('Failed to load exercise history data. Please try again.', {
        variant: 'error',
      });
    } finally {
      setIsLoading(false);
    }
  };

  // Get unique exercise names for filter (from both 1RM and exercises data)
  const uniqueExercises = Array.from(
    new Set([
      ...oneRepMaxes.map(orm => orm.exercise_name),
      ...allExercises.map(exercise => exercise.name),
    ])
  ).sort();

  // Filter data based on selected exercise
  const filteredOneRepMaxes =
    selectedExercise === 'all'
      ? oneRepMaxes
      : oneRepMaxes.filter(orm => orm.exercise_name === selectedExercise);

  // Calculate exercise statistics based on filtered data
  const exerciseStats = (selectedExercise === 'all' ? uniqueExercises : [selectedExercise]).map(
    exerciseName => {
      const exerciseOneRepMax = filteredOneRepMaxes.find(orm => orm.exercise_name === exerciseName);
      const exercise = allExercises.find(ex => ex.name === exerciseName);

      return {
        name: exerciseName,
        oneRepMax: exerciseOneRepMax,
        isAccessory: exercise?.is_accessory || false,
      };
    }
  );

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight={400}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <React.Fragment>
      <Typography variant="h5" gutterBottom>
        Exercise History
      </Typography>

      {/* Exercise Filter */}
      <Card sx={{ mb: 4 }}>
        <CardContent>
          <Autocomplete
            options={['all', ...uniqueExercises]}
            value={selectedExercise}
            onChange={(event, newValue) => setSelectedExercise(newValue || 'all')}
            renderInput={params => (
              <TextField
                {...params}
                label="Filter by Exercise"
                placeholder="Select an exercise or 'All Exercises'"
              />
            )}
            renderOption={(props, option) => (
              <Box component="li" {...props}>
                {option === 'all' ? 'All Exercises' : option}
              </Box>
            )}
            getOptionLabel={option => (option === 'all' ? 'All Exercises' : option)}
          />
        </CardContent>
      </Card>

      {/* Visualization Tabs */}
      <Card sx={{ mb: 4 }}>
        <Tabs
          value={activeTab}
          onChange={handleTabChange}
          variant="scrollable"
          scrollButtons="auto"
          sx={{ borderBottom: 1, borderColor: 'divider' }}
        >
          <Tab
            label="1RM Progress"
            value="onerepmax"
            icon={<ShowChartIcon />}
            iconPosition="start"
          />
          <Tab
            label="Exercise Information"
            value="usage"
            icon={<InfoIcon />}
            iconPosition="start"
          />
        </Tabs>

        <Box sx={{ p: 3 }}>
          {/* 1RM Progress Tab */}
          {activeTab === 'onerepmax' && (
            <Box>
              <Typography variant="h6" gutterBottom>
                1RM Progress Tracking
              </Typography>

              {filteredOneRepMaxes.length > 0 ? (
                <Grid container spacing={3}>
                  {filteredOneRepMaxes.map((oneRepMax, index) => (
                    <Grid item xs={12} md={6} lg={4} key={index}>
                      <Card variant="outlined">
                        <CardContent>
                          <Typography variant="h6" gutterBottom>
                            {oneRepMax.exercise_name}
                          </Typography>
                          <Typography variant="h4" color="primary" gutterBottom>
                            {oneRepMax.one_rep_max} {oneRepMax.unit}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            Last Updated: {new Date(oneRepMax.updated_at).toLocaleDateString()}
                          </Typography>

                          <Tooltip title="This 1RM value is used for workout generation and progression calculations">
                            <Chip
                              icon={<InfoIcon />}
                              label="Estimated from performance"
                              size="small"
                              variant="outlined"
                              sx={{ mt: 1 }}
                            />
                          </Tooltip>
                        </CardContent>
                      </Card>
                    </Grid>
                  ))}
                </Grid>
              ) : (
                <Box sx={{ textAlign: 'center', py: 4 }}>
                  <Typography variant="body1" color="text.secondary">
                    No 1RM data available for the selected exercise.
                  </Typography>
                </Box>
              )}
            </Box>
          )}

          {/* Exercise Information Tab */}
          {activeTab === 'usage' && (
            <Box>
              <Typography variant="h6" gutterBottom>
                Exercise Information
              </Typography>

              {exerciseStats.length > 0 ? (
                <Grid container spacing={3}>
                  {exerciseStats.map((stat, index) => (
                    <Grid item xs={12} md={6} lg={4} key={index}>
                      <Card variant="outlined">
                        <CardContent>
                          <Typography variant="h6" gutterBottom>
                            {stat.name}
                          </Typography>

                          <Box sx={{ mb: 2 }}>
                            <Chip
                              label={stat.isAccessory ? 'Accessory' : 'Primary'}
                              color={stat.isAccessory ? 'secondary' : 'primary'}
                              size="small"
                              sx={{ mr: 1 }}
                            />
                          </Box>

                          {stat.oneRepMax && (
                            <Box sx={{ mb: 2 }}>
                              <Typography variant="h6" color="primary">
                                {stat.oneRepMax.one_rep_max} {stat.oneRepMax.unit}
                              </Typography>
                              <Typography variant="caption" color="text.secondary">
                                Current 1RM
                              </Typography>
                            </Box>
                          )}
                        </CardContent>
                      </Card>
                    </Grid>
                  ))}
                </Grid>
              ) : (
                <Box sx={{ textAlign: 'center', py: 4 }}>
                  <Typography variant="body1" color="text.secondary">
                    No exercise information available.
                  </Typography>
                </Box>
              )}
            </Box>
          )}
        </Box>
      </Card>

      {/* Exercise Analytics Section */}
      <ExerciseAnalytics user={user} />
    </React.Fragment>
  );
};
