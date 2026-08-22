import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import InfoIcon from '@mui/icons-material/Info';
import { Box, CardContent, Chip, Grid, Alert, Tooltip, IconButton } from '@mui/material';
import { motion, AnimatePresence } from 'framer-motion';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState, useMemo } from 'react';
import { useNavigate, useSearchParams } from 'react-router';

import { ExerciseCategoryDetails } from './ExerciseCategoryDetails';
import { ExerciseName } from './ExerciseName';
import { ExercisePoolPieChart } from './ExercisePoolPieChart';
import { ExercisePoolSunburstChart } from './ExercisePoolSunburstChart';
import { GameText, GameCard, GameSubCard } from './GameTheme';
import { LoadingSpinner } from './LoadingSpinner';
import { RadialBarChart } from './RadialBarChart';
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
  const { userExercisePool, loadUserExercisePool } = useData();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [isLoading, setIsLoading] = useState(true);
  const [showCategoryDetails, setShowCategoryDetails] = useState(false);

  const selectedCategory = searchParams.get('category');

  useEffect(() => {
    const loadPool = async () => {
      if (!user?.keycloak_id) {
        setIsLoading(false);
        return;
      }

      if (userExercisePool) {
        setIsLoading(false);
        return;
      }

      try {
        setIsLoading(true);
        await loadUserExercisePool();
      } catch {
        enqueueSnackbar('Failed to load exercise pool data. Please try again.', {
          variant: 'error',
        });
      } finally {
        setIsLoading(false);
      }
    };

    loadPool();
  }, [user?.keycloak_id, userExercisePool, loadUserExercisePool, enqueueSnackbar]);

  useEffect(() => {
    setShowCategoryDetails(!!selectedCategory);
  }, [selectedCategory]);

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
    if (!userExercisePool) return null;

    return {
      totalExercises: userExercisePool.total_exercises,
      availableExercises: userExercisePool.available_exercises,
      categorizedExercises: {
        primary: userExercisePool.primary_exercises,
        accessory: userExercisePool.accessory_exercises,
      },
      userEquipment: userExercisePool.user_equipment,
      userPreferences: userExercisePool.user_preferences,
      previouslyUsedExercises: userExercisePool.previously_used_exercises,
    };
  }, [userExercisePool]);

  if (isLoading) {
    return <LoadingSpinner message="Loading exercise pool data..." />;
  }

  if (!exercisePoolAnalysis) {
    return (
      <Box sx={{ px: 3, pt: 3 }}>
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
      <AnimatePresence mode="wait">
        {!showCategoryDetails && (
          <motion.div
            key="main-exercise-rotation"
            initial={{ opacity: 0, x: 50 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: 50 }}
            transition={{ duration: 0.3, ease: 'easeInOut' }}
          >
            <Box sx={{ px: 3, pt: 3 }}>
              <Grid container spacing={3}>
                {/* Exercise Insights Charts */}
                {exercisePoolAnalysis && (
                  <Grid size={{ xs: 12 }}>
                    <Grid container spacing={3}>
                      {/* Pool Availability - Pie Chart */}
                      <Grid size={{ xs: 12, md: 4 }}>
                        <ExercisePoolPieChart
                          exercisePoolData={userExercisePool}
                          title="Exercise Availability"
                          description={`${exercisePoolAnalysis.availableExercises} of ${exercisePoolAnalysis.totalExercises} exercises available`}
                          height={250}
                        />
                      </Grid>

                      {/* Exercise Variety - Radial Bar Chart */}
                      <Grid size={{ xs: 12, md: 4 }}>
                        <RadialBarChart
                          exercisePoolData={userExercisePool}
                          title="Exercise Variety"
                          description="Primary vs accessory distribution"
                          height={250}
                        />
                      </Grid>

                      {/* Exercise Selection - Sunburst Chart */}
                      <Grid size={{ xs: 12, md: 4 }}>
                        <ExercisePoolSunburstChart
                          exercisePoolData={userExercisePool}
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
                      <GameCard>
                        <CardContent>
                          <GameText
                            variant="h6"
                            gutterBottom
                            sx={{ display: 'flex', alignItems: 'center', gap: 1 }}
                          >
                            Available Exercises
                          </GameText>
                          <Grid container spacing={2}>
                            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                              <GameSubCard
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
                                <GameText variant="h4" textVariant="accent">
                                  {exercisePoolAnalysis.categorizedExercises.primary.length}
                                </GameText>
                                <GameText variant="body2">Primary Exercises</GameText>
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
                                        color="info"
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
                              </GameSubCard>
                            </Grid>
                            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                              <GameSubCard
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
                                <GameText variant="h4" textVariant="accent">
                                  {exercisePoolAnalysis.categorizedExercises.accessory.length}
                                </GameText>
                                <GameText variant="body2">Accessory Exercises</GameText>
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
                                  {exercisePoolAnalysis.categorizedExercises.accessory.length >
                                    3 && (
                                    <Chip
                                      label={`+${exercisePoolAnalysis.categorizedExercises.accessory.length - 3}`}
                                      size="small"
                                      variant="outlined"
                                    />
                                  )}
                                </Box>
                              </GameSubCard>
                            </Grid>
                            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                              <GameSubCard
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
                                <GameText variant="h4" textVariant="accent">
                                  {exercisePoolAnalysis.userEquipment.length}
                                </GameText>
                                <GameText variant="body2">Available Equipment</GameText>
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
                              </GameSubCard>
                            </Grid>
                            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                              <GameSubCard
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
                                <GameText variant="h4" textVariant="accent">
                                  {
                                    exercisePoolAnalysis.userPreferences.filter(p => p.should_avoid)
                                      .length
                                  }
                                </GameText>
                                <GameText variant="body2">Avoided Exercises</GameText>
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
                              </GameSubCard>
                            </Grid>
                          </Grid>
                        </CardContent>
                      </GameCard>
                    </Grid>
                  )}

                  {/* Recent Exercises - Right side */}
                  {exercisePoolAnalysis &&
                    exercisePoolAnalysis.previouslyUsedExercises.length > 0 && (
                      <Grid size={{ xs: 12, md: 6 }}>
                        <GameCard
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
                            <GameText
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
                            </GameText>
                            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                              {exercisePoolAnalysis.previouslyUsedExercises
                                .slice(0, 10)
                                .map(exerciseName => (
                                  <Chip
                                    key={exerciseName}
                                    label={
                                      <ExerciseName exerciseName={exerciseName} variant="caption" />
                                    }
                                    variant="outlined"
                                    color="info"
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
                        </GameCard>
                      </Grid>
                    )}
                </Grid>
              </Grid>
            </Box>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Category Details - Slides in from left when category is selected */}
      <AnimatePresence mode="wait">
        {selectedCategory && showCategoryDetails && (
          <motion.div
            key="category-details"
            initial={{ opacity: 0, x: -50 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -50 }}
            transition={{ duration: 0.3, ease: 'easeInOut' }}
          >
            <Box sx={{ px: 3, pt: 3 }}>
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
          </motion.div>
        )}
      </AnimatePresence>
    </Box>
  );
};
