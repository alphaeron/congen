import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import InfoIcon from '@mui/icons-material/Info';
import {
  Box,
  Card,
  CardContent,
  Chip,
  Grid,
  Typography,
  Paper,
  Alert,
  Slide,
  Tooltip,
  IconButton,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState, useMemo } from 'react';
import { useNavigate, useSearchParams } from 'react-router';

import { ExerciseCategoryDetails } from './ExerciseCategoryDetails';
import { ExerciseName } from './ExerciseName';
import { ExercisePoolPieChart } from './ExercisePoolPieChart';
import { ExercisePoolSunburstChart } from './ExercisePoolSunburstChart';
import { LoadingSpinner } from './LoadingSpinner';
import { RadialBarChart } from './RadialBarChart';
import type { UserExercisePoolResponse } from '../api/types';
import { useAuth } from '../contexts/AuthContext';
import { useData } from '../contexts/DataContext';

/**
 * Exercise Rotation Visualization component.
 *
 * This component displays the user's exercise rotation patterns, showing how exercises
 * are rotated over time in their programs. It visualizes the undulating periodization
 * and exercise rotation that is central to the conjugate method. It also shows the
 * current exercise pool based on user preferences, equipment, and weak muscles.
 *
 * @return Exercise Rotation Visualization component
 */
export const ExerciseRotationVisualization: React.FC = () => {
  const { user } = useAuth();
  const { enqueueSnackbar } = useSnackbar();
  const { userExercisePool, loadUserExercisePool, isLoading: isDataLoading } = useData();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [isLoading, setIsLoading] = useState(true);
  const [exercisePoolData, setExercisePoolData] = useState<UserExercisePoolResponse | null>(null);
  const [showCategoryDetails, setShowCategoryDetails] = useState(false);

  // Check if we're viewing a specific category
  const selectedCategory = searchParams.get('category');

  useEffect(() => {
    if (user?.keycloak_id) {
      loadData();
    }
  }, [user?.keycloak_id]);

  // Update showCategoryDetails when selectedCategory changes
  useEffect(() => {
    setShowCategoryDetails(!!selectedCategory);
  }, [selectedCategory]);

  const loadData = async () => {
    try {
      setIsLoading(true);

      // Load user exercise pool if not already loaded
      if (!userExercisePool) {
        await loadUserExercisePool();
      }
      setExercisePoolData(userExercisePool);
    } catch {
      enqueueSnackbar('Failed to load exercise pool data. Please try again.', { variant: 'error' });
    } finally {
      setIsLoading(false);
    }
  };

  const handleCategoryClick = (category: string) => {
    const newSearchParams = new URLSearchParams();
    newSearchParams.set('section', 'workouts');
    newSearchParams.set('subsection', 'rotation');
    newSearchParams.set('category', category);
    navigate(`?${newSearchParams.toString()}`);
  };

  const handlePreferencesClick = () => {
    const newSearchParams = new URLSearchParams();
    newSearchParams.set('section', 'workouts');
    newSearchParams.set('subsection', 'preferences');
    navigate(`?${newSearchParams.toString()}`);
  };

  const handleBackClick = () => {
    const newSearchParams = new URLSearchParams();
    newSearchParams.set('section', 'workouts');
    newSearchParams.set('subsection', 'rotation');
    // Remove category to go back to main view
    navigate(`?${newSearchParams.toString()}`);
  };

  const handleExerciseClick = (exerciseName: string) => {
    const newSearchParams = new URLSearchParams();
    newSearchParams.set('section', 'workouts');
    newSearchParams.set('subsection', 'rotation');
    newSearchParams.set('category', selectedCategory || '');
    newSearchParams.set('exercise', exerciseName);
    navigate(`?${newSearchParams.toString()}`);
  };

  // Render minimal floating back button
  const renderBackButton = () => {
    // Only show back button when in a category view
    if (!selectedCategory) return null;

    return (
      <IconButton
        onClick={handleBackClick}
        sx={{
          position: 'absolute',
          top: 24,
          left: -32,
          zIndex: 1001,
          backgroundColor: 'background.paper',
          boxShadow: 2,
          '&:hover': {
            backgroundColor: 'action.hover',
            boxShadow: 4,
          },
        }}
      >
        <ArrowBackIcon />
      </IconButton>
    );
  };

  // Exercise pool analysis
  const exercisePoolAnalysis = useMemo(() => {
    if (!exercisePoolData) return null;

    return {
      totalExercises: exercisePoolData.total_exercises,
      availableExercises: exercisePoolData.available_exercises,
      categorizedExercises: {
        primary: exercisePoolData.primary_exercises,
        accessory: exercisePoolData.accessory_exercises,
      },
      userEquipment: exercisePoolData.user_equipment,
      userPreferences: exercisePoolData.user_preferences,
      previouslyUsedExercises: exercisePoolData.previously_used_exercises,
    };
  }, [exercisePoolData]);

  if (isLoading) {
    return <LoadingSpinner message="Loading exercise pool data..." />;
  }

  if (!exercisePoolAnalysis) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="info">
          No exercise pool data available. Please check your preferences and equipment settings.
        </Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ position: 'relative' }}>
      {renderBackButton()}

      {/* Main Exercise Rotation - Slides right when category is selected */}
      <Slide direction="right" in={!showCategoryDetails} mountOnEnter unmountOnExit>
        <Box sx={{ p: 3 }}>
          <Grid container spacing={3}>
            {/* Exercise Insights Charts */}
            {exercisePoolAnalysis && (
              <Grid size={{ xs: 12 }}>
                <Grid container spacing={3}>
                  {/* Pool Availability - Pie Chart */}
                  <Grid size={{ xs: 12, md: 4 }}>
                    <ExercisePoolPieChart
                      exercisePoolData={exercisePoolData}
                      title="Exercise Availability"
                      description={`${exercisePoolAnalysis.availableExercises} of ${exercisePoolAnalysis.totalExercises} exercises available`}
                      height={250}
                    />
                  </Grid>

                  {/* Exercise Variety - Radial Bar Chart */}
                  <Grid size={{ xs: 12, md: 4 }}>
                    <RadialBarChart
                      exercisePoolData={exercisePoolData}
                      title="Exercise Variety"
                      description="Primary vs accessory distribution"
                      height={250}
                    />
                  </Grid>

                  {/* Exercise Selection - Sunburst Chart */}
                  <Grid size={{ xs: 12, md: 4 }}>
                    <ExercisePoolSunburstChart
                      exercisePoolData={exercisePoolData}
                      title="Exercise Selection"
                      description="Hierarchical pool structure"
                      height={250}
                    />
                  </Grid>
                </Grid>
              </Grid>
            )}

            {/* Exercise Rotation and Recent Exercises - Side by side */}
            <Grid container spacing={3}>
              {/* Available Exercises - Left side */}
              {exercisePoolAnalysis && (
                <Grid
                  size={
                    exercisePoolAnalysis.previouslyUsedExercises.length > 0
                      ? { xs: 12, md: 6 }
                      : { xs: 12 }
                  }
                >
                  <Card>
                    <CardContent>
                      <Typography
                        variant="h6"
                        gutterBottom
                        sx={{ display: 'flex', alignItems: 'center', gap: 1 }}
                      >
                        Available Exercises
                      </Typography>
                      <Grid container spacing={2}>
                        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                          <Paper
                            sx={{
                              p: 2,
                              textAlign: 'center',
                              cursor: 'pointer',
                              transition: 'all 0.2s ease-in-out',
                              '&:hover': {
                                transform: 'translateY(-2px)',
                                boxShadow: 3,
                              },
                            }}
                            onClick={() => handleCategoryClick('primary')}
                          >
                            <Typography variant="h4" color="error.main">
                              {exercisePoolAnalysis.categorizedExercises.primary.length}
                            </Typography>
                            <Typography variant="body2">Primary Exercises</Typography>
                            <Box
                              sx={{
                                mt: 1,
                                display: 'flex',
                                flexWrap: 'wrap',
                                gap: 0.5,
                                justifyContent: 'center',
                              }}
                            >
                              {exercisePoolAnalysis.categorizedExercises.primary
                                .slice(0, 3)
                                .map(exercise => (
                                  <Chip
                                    key={exercise.name}
                                    label={
                                      <ExerciseName
                                        exerciseName={exercise.name}
                                        variant="caption"
                                      />
                                    }
                                    size="small"
                                    variant="outlined"
                                    color="error"
                                    clickable
                                    onClick={e => {
                                      e.stopPropagation();
                                      handleExerciseClick(exercise.name);
                                    }}
                                  />
                                ))}
                              {exercisePoolAnalysis.categorizedExercises.primary.length > 3 && (
                                <Chip
                                  label={`+${exercisePoolAnalysis.categorizedExercises.primary.length - 3}`}
                                  size="small"
                                  variant="outlined"
                                />
                              )}
                            </Box>
                          </Paper>
                        </Grid>
                        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                          <Paper
                            sx={{
                              p: 2,
                              textAlign: 'center',
                              cursor: 'pointer',
                              transition: 'all 0.2s ease-in-out',
                              '&:hover': {
                                transform: 'translateY(-2px)',
                                boxShadow: 3,
                              },
                            }}
                            onClick={() => handleCategoryClick('accessory')}
                          >
                            <Typography variant="h4" color="info.main">
                              {exercisePoolAnalysis.categorizedExercises.accessory.length}
                            </Typography>
                            <Typography variant="body2">Accessory Exercises</Typography>
                            <Box
                              sx={{
                                mt: 1,
                                display: 'flex',
                                flexWrap: 'wrap',
                                gap: 0.5,
                                justifyContent: 'center',
                              }}
                            >
                              {exercisePoolAnalysis.categorizedExercises.accessory
                                .slice(0, 3)
                                .map(exercise => (
                                  <Chip
                                    key={exercise.name}
                                    label={
                                      <ExerciseName
                                        exerciseName={exercise.name}
                                        variant="caption"
                                      />
                                    }
                                    size="small"
                                    variant="outlined"
                                    color="info"
                                    clickable
                                    onClick={e => {
                                      e.stopPropagation();
                                      handleExerciseClick(exercise.name);
                                    }}
                                  />
                                ))}
                              {exercisePoolAnalysis.categorizedExercises.accessory.length > 3 && (
                                <Chip
                                  label={`+${exercisePoolAnalysis.categorizedExercises.accessory.length - 3}`}
                                  size="small"
                                  variant="outlined"
                                />
                              )}
                            </Box>
                          </Paper>
                        </Grid>
                        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                          <Paper
                            sx={{
                              p: 2,
                              textAlign: 'center',
                              cursor: 'pointer',
                              transition: 'all 0.2s ease-in-out',
                              '&:hover': {
                                transform: 'translateY(-2px)',
                                boxShadow: 3,
                              },
                            }}
                            onClick={handlePreferencesClick}
                          >
                            <Typography variant="h4" color="success.main">
                              {exercisePoolAnalysis.userEquipment.length}
                            </Typography>
                            <Typography variant="body2">Available Equipment</Typography>
                            <Box
                              sx={{
                                mt: 1,
                                display: 'flex',
                                flexWrap: 'wrap',
                                gap: 0.5,
                                justifyContent: 'center',
                              }}
                            >
                              {exercisePoolAnalysis.userEquipment.slice(0, 3).map(equipment => (
                                <Chip
                                  key={equipment.equipment_name}
                                  label={equipment.equipment_name}
                                  size="small"
                                  variant="outlined"
                                  color="success"
                                />
                              ))}
                              {exercisePoolAnalysis.userEquipment.length > 3 && (
                                <Chip
                                  label={`+${exercisePoolAnalysis.userEquipment.length - 3}`}
                                  size="small"
                                  variant="outlined"
                                />
                              )}
                            </Box>
                          </Paper>
                        </Grid>
                        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                          <Paper
                            sx={{
                              p: 2,
                              textAlign: 'center',
                              cursor: 'pointer',
                              transition: 'all 0.2s ease-in-out',
                              '&:hover': {
                                transform: 'translateY(-2px)',
                                boxShadow: 3,
                              },
                            }}
                            onClick={handlePreferencesClick}
                          >
                            <Typography variant="h4" color="warning.main">
                              {
                                exercisePoolAnalysis.userPreferences.filter(p => p.should_avoid)
                                  .length
                              }
                            </Typography>
                            <Typography variant="body2">Avoided Exercises</Typography>
                            <Box
                              sx={{
                                mt: 1,
                                display: 'flex',
                                flexWrap: 'wrap',
                                gap: 0.5,
                                justifyContent: 'center',
                              }}
                            >
                              {exercisePoolAnalysis.userPreferences
                                .filter(p => p.should_avoid)
                                .slice(0, 3)
                                .map(pref => (
                                  <Chip
                                    key={pref.exercise_name}
                                    label={pref.exercise_name}
                                    size="small"
                                    variant="outlined"
                                    color="warning"
                                  />
                                ))}
                              {exercisePoolAnalysis.userPreferences.filter(p => p.should_avoid)
                                .length > 3 && (
                                <Chip
                                  label={`+${exercisePoolAnalysis.userPreferences.filter(p => p.should_avoid).length - 3}`}
                                  size="small"
                                  variant="outlined"
                                />
                              )}
                            </Box>
                          </Paper>
                        </Grid>
                      </Grid>
                    </CardContent>
                  </Card>
                </Grid>
              )}

              {/* Recent Exercises - Right side */}
              {exercisePoolAnalysis && exercisePoolAnalysis.previouslyUsedExercises.length > 0 && (
                <Grid size={{ xs: 12, md: 6 }}>
                  <Card
                    sx={{
                      cursor: 'pointer',
                      transition: 'all 0.2s ease-in-out',
                      '&:hover': {
                        transform: 'translateY(-2px)',
                        boxShadow: 3,
                      },
                    }}
                    onClick={() => handleCategoryClick('recent')}
                  >
                    <CardContent>
                      <Typography
                        variant="h6"
                        gutterBottom
                        sx={{ display: 'flex', alignItems: 'center', gap: 1 }}
                      >
                        Recent Exercises
                        <Tooltip
                          title="These exercises have been used in recent weeks and are temporarily excluded from selection to promote variety."
                          arrow
                        >
                          <IconButton size="small" sx={{ p: 0.5 }}>
                            <InfoIcon fontSize="small" color="action" />
                          </IconButton>
                        </Tooltip>
                      </Typography>
                      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                        {exercisePoolAnalysis.previouslyUsedExercises
                          .slice(0, 10)
                          .map(exerciseName => (
                            <Chip
                              key={exerciseName}
                              label={<ExerciseName exerciseName={exerciseName} variant="caption" />}
                              variant="outlined"
                              color="warning"
                              size="small"
                              clickable
                              onClick={e => {
                                e.stopPropagation();
                                handleExerciseClick(exerciseName);
                              }}
                            />
                          ))}
                        {exercisePoolAnalysis.previouslyUsedExercises.length > 10 && (
                          <Chip
                            label={`+${exercisePoolAnalysis.previouslyUsedExercises.length - 10}`}
                            size="small"
                            variant="outlined"
                          />
                        )}
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>
              )}
            </Grid>
          </Grid>
        </Box>
      </Slide>

      {/* Category Details - Slides in from left when category is selected */}
      {selectedCategory && (
        <Slide direction="left" in={showCategoryDetails} mountOnEnter unmountOnExit>
          <Box sx={{ p: 3 }}>
            {/* Horizontal layout for alert - positioned to align with back button */}
            <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2, mb: 3, ml: 5 }}>
              <Alert severity="info" sx={{ flex: 1 }}>
                {selectedCategory === 'primary' && '10 primary exercises in your rotation.'}
                {selectedCategory === 'accessory' && '15 accessory exercises in your rotation.'}
                {selectedCategory === 'recent' && '8 recent exercises in your rotation.'}
              </Alert>
            </Box>
            <ExerciseCategoryDetails category={selectedCategory} />
          </Box>
        </Slide>
      )}
    </Box>
  );
};
