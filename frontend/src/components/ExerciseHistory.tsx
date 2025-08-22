import { default as ShowChartIcon } from '@mui/icons-material/ShowChart';
import { default as TrendingUpIcon } from '@mui/icons-material/TrendingUp';
import { default as InfoIcon } from '@mui/icons-material/Info';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Alert,
  CircularProgress,
  Chip,
  Tooltip,
  Tabs,
  Tab,
  Autocomplete,
  TextField,
} from '@mui/material';
import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router';

import { getUserOneRepMaxes } from '../api/userOneRepMax';
import { getExerciseRotationHistory } from '../api/exerciseRotationHistory';
import { getExercises } from '../api/exercise';
import type { User, UserOneRepMax, ExerciseRotationHistory, Exercise } from '../api/types';

interface ExerciseHistoryProps {
  user: User;
}

type TabName = 'onerepmax' | 'exercise_rotation' | 'usage';

/**
 * Exercise History page component for exercise history and trends.
 *
 * Tracks exercise history per lift and visualizes set/rep schemes,
 * rotation frequency, and estimated 1RM with explanatory tooltips.
 *
 * @param user The user data
 * @return Exercise History page component
 */
export const ExerciseHistory: React.FC<ExerciseHistoryProps> = ({ user }) => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [oneRepMaxes, setOneRepMaxes] = useState<UserOneRepMax[]>([]);
  const [exerciseHistory, setExerciseHistory] = useState<ExerciseRotationHistory[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
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
      setError(null);

      const [oneRepMaxesData, exerciseHistoryData, allExercisesData] = await Promise.all([
        getUserOneRepMaxes(user.keycloak_id),
        getExerciseRotationHistory(),
        getExercises(),
      ]);

      setOneRepMaxes(oneRepMaxesData);
      setExerciseHistory(exerciseHistoryData);
      setAllExercises(allExercisesData);
    } catch (err) {
      console.error('Error loading exercise history data:', err);
      setError('Failed to load exercise history data. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  // Get unique exercise names for filter (from both 1RM and history data)
  const uniqueExercises = Array.from(
    new Set([
      ...oneRepMaxes.map(orm => orm.exercise_name),
      ...exerciseHistory.map(history => history.exercise_name),
      ...allExercises.map(exercise => exercise.name)
    ])
  ).sort();

  // Filter data based on selected exercise
  const filteredOneRepMaxes = selectedExercise === 'all' 
    ? oneRepMaxes 
    : oneRepMaxes.filter(orm => orm.exercise_name === selectedExercise);

  const filteredExerciseHistory = selectedExercise === 'all'
    ? exerciseHistory
    : exerciseHistory.filter(history => history.exercise_name === selectedExercise);

  // Calculate exercise statistics based on filtered data
  const exerciseStats = (selectedExercise === 'all' ? uniqueExercises : [selectedExercise]).map(exerciseName => {
    const exerciseOneRepMax = filteredOneRepMaxes.find(orm => orm.exercise_name === exerciseName);
    const exerciseHistoryCount = filteredExerciseHistory.filter(history => history.exercise_name === exerciseName).length;
    const primaryCount = filteredExerciseHistory.filter(history => 
      history.exercise_name === exerciseName && !history.is_accessory
    ).length;
    const accessoryCount = filteredExerciseHistory.filter(history => 
      history.exercise_name === exerciseName && history.is_accessory
    ).length;

    return {
      name: exerciseName,
      oneRepMax: exerciseOneRepMax,
      totalUses: exerciseHistoryCount,
      primaryUses: primaryCount,
      accessoryUses: accessoryCount,
      lastUsed: filteredExerciseHistory
        .filter(history => history.exercise_name === exerciseName)
        .sort((a, b) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime())[0]?.created_at
    };
  });

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

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Exercise Filter */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Autocomplete
            options={['all', ...uniqueExercises]}
            value={selectedExercise}
            onChange={(event, newValue) => setSelectedExercise(newValue || 'all')}
            renderInput={(params) => (
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
            getOptionLabel={(option) => option === 'all' ? 'All Exercises' : option}
          />
        </CardContent>
      </Card>

      {/* Visualization Tabs */}
      <Card>
        <Tabs
          value={activeTab}
          onChange={handleTabChange}
          variant="scrollable"
          scrollButtons="auto"
          sx={{ borderBottom: 1, borderColor: 'divider' }}
        >
          <Tab label="1RM Progress" value="onerepmax" icon={<ShowChartIcon />} iconPosition="start" />
          <Tab label="Exercise Rotation" value="exercise_rotation" icon={<TrendingUpIcon />} iconPosition="start" />
          <Tab label="Usage Statistics" value="usage" icon={<InfoIcon />} iconPosition="start" />
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

          {/* Exercise Rotation Tab */}
          {activeTab === 'exercise_rotation' && (
            <Box>
              <Typography variant="h6" gutterBottom>
                Exercise Rotation History
              </Typography>
              
              {filteredExerciseHistory.length > 0 ? (
                <Grid container spacing={3}>
                  {filteredExerciseHistory.slice(-10).map((history, index) => (
                    <Grid item xs={12} md={6} lg={4} key={index}>
                      <Card variant="outlined">
                        <CardContent>
                          <Typography variant="h6" gutterBottom>
                            {history.exercise_name}
                          </Typography>
                          <Box display="flex" gap={1} sx={{ mb: 2 }}>
                            <Chip
                              label={history.is_accessory ? 'Accessory' : 'Primary'}
                              color={history.is_accessory ? 'secondary' : 'primary'}
                              size="small"
                            />
                            <Chip
                              label={new Date(history.created_at).toLocaleDateString()}
                              variant="outlined"
                              size="small"
                            />
                          </Box>
                          <Typography variant="body2" color="text.secondary">
                            Used as {history.is_accessory ? 'accessory movement' : 'primary/secondary movement'}
                          </Typography>
                        </CardContent>
                      </Card>
                    </Grid>
                  ))}
                </Grid>
              ) : (
                <Box sx={{ textAlign: 'center', py: 4 }}>
                  <Typography variant="body1" color="text.secondary">
                    No exercise rotation history available for the selected exercise.
                  </Typography>
                </Box>
              )}
            </Box>
          )}

          {/* Usage Statistics Tab */}
          {activeTab === 'usage' && (
            <Box>
              <Typography variant="h6" gutterBottom>
                Exercise Usage Statistics
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
                            <Typography variant="body2" color="text.secondary">
                              Total Uses: {stat.totalUses}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                              Primary: {stat.primaryUses} | Accessory: {stat.accessoryUses}
                            </Typography>
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

                          {stat.lastUsed && (
                            <Typography variant="caption" color="text.secondary">
                              Last used: {new Date(stat.lastUsed).toLocaleDateString()}
                            </Typography>
                          )}
                        </CardContent>
                      </Card>
                    </Grid>
                  ))}
                </Grid>
              ) : (
                <Box sx={{ textAlign: 'center', py: 4 }}>
                  <Typography variant="body1" color="text.secondary">
                    No exercise statistics available.
                  </Typography>
                </Box>
              )}
            </Box>
          )}
        </Box>
      </Card>
    </React.Fragment>
  );
};
