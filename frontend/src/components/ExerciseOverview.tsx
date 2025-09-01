import Alert from '@mui/material/Alert';
import AlertTitle from '@mui/material/AlertTitle';
import Autocomplete from '@mui/material/Autocomplete';
import Box from '@mui/material/Box';
import Container from '@mui/material/Container';
import Grid from '@mui/material/Grid';
import Paper from '@mui/material/Paper';
import Stack from '@mui/material/Stack';
import { alpha } from '@mui/material/styles';
import TextField from '@mui/material/TextField';
import ToggleButton from '@mui/material/ToggleButton';
import ToggleButtonGroup from '@mui/material/ToggleButtonGroup';
import Typography from '@mui/material/Typography';
import * as React from 'react';

import { ExerciseCard } from './ExerciseCard';
import { LoadingSpinner } from './LoadingSpinner';
import { getEquipment } from '../api/equipment';
import { getExercises } from '../api/exercise';
import { getExerciseEquipment } from '../api/exerciseEquipment';
import { getExerciseMuscle } from '../api/exerciseMuscle';
import { useApiGet } from '../api/hooks';
import { getMuscles } from '../api/muscle';
import type { Equipment, Exercise, Muscle } from '../api/types';
import { capitalizeEachWord } from '../common/utils';

import '../styles/Form.css';

/**
 * Shows an overview of all available exercises.
 *
 * @return The exercise overview component.
 */
export function ExerciseOverview(): React.ReactElement {
  const [movementTypes, setMovementTypes] = React.useState<string[]>([]);

  const [movementTypeFilter, setMovementTypeFilter] = React.useState<string | null>(null);
  const [exerciseEquipmentFilter, setExerciseEquipmentFilter] = React.useState<string | null>(null);
  const [exerciseMuscleFilter, setExerciseMuscleFilter] = React.useState<string | null>(null);

  const [isUnilateralFilter, setIsUnilateralFilter] = React.useState<string>('Both');
  const [isAccessoryFilter, setIsAccessoryFilter] = React.useState<string>('Both');
  const [isUpperFilter, setIsUpperFilter] = React.useState<string>('Both');

  const [exercisesToDisplay, setExercisesToDisplay] = React.useState<Exercise[]>([]);

  const {
    data: exercises,
    isLoading: isExercisesLoading,
    isError: isExercisesError,
  } = useApiGet<Exercise[]>(['exercises'], getExercises, {
    enabled: true,
    refetchOnWindowFocus: true,
    retry: 1,
  });

  const {
    data: equipment,
    isLoading: isEquipmentLoading,
    isError: isEquipmentError,
  } = useApiGet<Equipment[]>(['equipment'], getEquipment, {
    enabled: true,
    refetchOnWindowFocus: true,
    retry: 1,
  });

  const {
    data: muscles,
    isLoading: isMusclesLoading,
    isError: isMusclesError,
  } = useApiGet<Muscle[]>(['muscles'], getMuscles, {
    enabled: true,
    refetchOnWindowFocus: true,
    retry: 1,
  });

  const getExerciseEquipmentMap = async (): Promise<Map<string, Set<string>>> => {
    const res = await getExerciseEquipment();
    const mapping = new Map<string, Set<string>>();
    const exerciseNames = Array.from(new Set(res.map(e => e.exercise_name)));
    for (const exerciseName of exerciseNames) {
      mapping.set(
        exerciseName,
        new Set(res.filter(e => e.exercise_name === exerciseName).map(e => e.equipment_name))
      );
    }
    return mapping;
  };

  const {
    data: exerciseEquipmentMap,
    isLoading: isExerciseEquipmentLoading,
    isError: isExerciseEquipmentError,
  } = useApiGet<Map<string, Set<string>>>(['exerciseEquipmentMap'], getExerciseEquipmentMap, {
    enabled: true,
    refetchOnWindowFocus: true,
    retry: 1,
  });

  const getExerciseMuscleMap = async (): Promise<Map<string, Set<string>>> => {
    const res = await getExerciseMuscle();
    const mapping = new Map<string, Set<string>>();
    const exerciseNames = Array.from(new Set(res.map(e => e.exercise_name)));
    for (const exerciseName of exerciseNames) {
      mapping.set(
        exerciseName,
        new Set(res.filter(e => e.exercise_name === exerciseName).map(e => e.muscle_name))
      );
    }
    return mapping;
  };

  const {
    data: exerciseMuscleMap,
    isLoading: isExerciseMuscleLoading,
    isError: isExerciseMuscleError,
  } = useApiGet<Map<string, Set<string>>>(['exerciseMuscleMap'], getExerciseMuscleMap, {
    enabled: true,
    refetchOnWindowFocus: true,
    retry: 1,
  });

  React.useEffect(() => {
    if (exercises) {
      const types = Array.from(new Set(exercises.map(e => e.movement_type)));
      setMovementTypes(types);
    }
  }, [exercises]);

  React.useEffect(() => {
    if (!exercises) return;

    let filtered = exercises;

    if (movementTypeFilter) {
      filtered = filtered.filter(e => e.movement_type === movementTypeFilter);
    }

    if (exerciseEquipmentFilter) {
      filtered = filtered.filter(e => {
        const equipmentSet = exerciseEquipmentMap?.get(e.name);
        return equipmentSet?.has(exerciseEquipmentFilter);
      });
    }

    if (exerciseMuscleFilter) {
      filtered = filtered.filter(e => {
        const muscleSet = exerciseMuscleMap?.get(e.name);
        return muscleSet?.has(exerciseMuscleFilter);
      });
    }

    if (isUnilateralFilter !== 'Both') {
      const isUnilateral = isUnilateralFilter === 'Unilateral';
      filtered = filtered.filter(e => e.is_unilateral === isUnilateral);
    }

    if (isAccessoryFilter !== 'Both') {
      const isAccessory = isAccessoryFilter === 'Accessory';
      filtered = filtered.filter(e => e.is_accessory === isAccessory);
    }

    if (isUpperFilter !== 'Both') {
      const isUpper = isUpperFilter === 'Upper';
      filtered = filtered.filter(e => e.is_upper === isUpper);
    }

    setExercisesToDisplay(filtered);
  }, [
    exercises,
    movementTypeFilter,
    exerciseEquipmentFilter,
    exerciseMuscleFilter,
    isUnilateralFilter,
    isAccessoryFilter,
    isUpperFilter,
    exerciseEquipmentMap,
    exerciseMuscleMap,
  ]);

  const isLoading =
    isExercisesLoading ||
    isEquipmentLoading ||
    isMusclesLoading ||
    isExerciseEquipmentLoading ||
    isExerciseMuscleLoading ||
    exercises === undefined ||
    equipment === undefined ||
    muscles === undefined ||
    exerciseEquipmentMap === undefined ||
    exerciseMuscleMap === undefined;

  const hasError =
    isExercisesError ||
    isEquipmentError ||
    isMusclesError ||
    isExerciseEquipmentError ||
    isExerciseMuscleError;

  if (isLoading) {
    return <LoadingSpinner message="Loading exercises..." />;
  }

  if (hasError) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Alert severity="error" sx={{ borderRadius: 2 }}>
          <AlertTitle>Error</AlertTitle>
          Failed to load exercises. Please try again later.
        </Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <Stack spacing={4}>
        {/* Header */}
        <Box sx={{ textAlign: 'center', mb: 2 }}>
          <Typography
            variant="h3"
            data-testid="exerciseHeader"
            sx={{
              fontWeight: 700,
              mb: 2,
              background: 'linear-gradient(135deg, #0ea5e9, #f97316)',
              backgroundClip: 'text',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
            }}
          >
            Exercise Library
          </Typography>
          <Typography
            variant="h6"
            color="text.secondary"
            sx={{
              fontWeight: 400,
              opacity: 0.8,
            }}
          >
            Discover and filter exercises to build your perfect workout
          </Typography>
        </Box>

        {/* Filters Section */}
        <Paper
          sx={{
            p: 4,
            borderRadius: 3,
            background: theme =>
              `linear-gradient(135deg, ${alpha(theme.palette.background.paper, 0.8)}, ${alpha(theme.palette.background.paper, 0.6)})`,
            border: theme => `1px solid ${alpha(theme.palette.divider, 0.3)}`,
            backdropFilter: 'blur(20px)',
            boxShadow: theme => `0 8px 32px ${alpha(theme.palette.primary.main, 0.1)}`,
          }}
        >
          <Typography variant="h5" sx={{ fontWeight: 600, mb: 3 }}>
            Filters
          </Typography>

          <Grid container spacing={4}>
            {/* Autocomplete Filters - Stacked Vertically */}
            <Grid size={{ xs: 12, md: 6 }}>
              <Stack spacing={3}>
                <Autocomplete
                  options={movementTypes}
                  value={movementTypeFilter}
                  onChange={(_, newValue) => setMovementTypeFilter(newValue)}
                  getOptionLabel={option => capitalizeEachWord(option)}
                  renderInput={params => (
                    <TextField {...params} label="Movement Type" variant="outlined" fullWidth />
                  )}
                  clearOnBlur
                  selectOnFocus
                  handleHomeEndKeys
                />

                <Autocomplete
                  options={equipment?.map(e => e.name) || []}
                  value={exerciseEquipmentFilter}
                  onChange={(_, newValue) => setExerciseEquipmentFilter(newValue)}
                  renderInput={params => (
                    <TextField {...params} label="Equipment" variant="outlined" fullWidth />
                  )}
                  clearOnBlur
                  selectOnFocus
                  handleHomeEndKeys
                />

                <Autocomplete
                  options={muscles?.map(m => m.name) || []}
                  value={exerciseMuscleFilter}
                  onChange={(_, newValue) => setExerciseMuscleFilter(newValue)}
                  renderInput={params => (
                    <TextField {...params} label="Target Muscle" variant="outlined" fullWidth />
                  )}
                  clearOnBlur
                  selectOnFocus
                  handleHomeEndKeys
                />
              </Stack>
            </Grid>

            {/* Toggle Button Filters - Modern Design */}
            <Grid size={{ xs: 12, md: 6 }}>
              <Stack spacing={4}>
                {/* Unilateral Filter */}
                <Box>
                  <Typography
                    variant="subtitle1"
                    sx={{ fontWeight: 600, mb: 2, color: 'text.primary' }}
                  >
                    Movement Pattern
                  </Typography>
                  <ToggleButtonGroup
                    value={isUnilateralFilter}
                    exclusive
                    onChange={(_, newValue) => newValue && setIsUnilateralFilter(newValue)}
                    aria-label="movement pattern"
                    sx={{
                      '& .MuiToggleButton-root': {
                        textTransform: 'none',
                        fontWeight: 500,
                        borderRadius: 2,
                        border: theme => `1px solid ${alpha(theme.palette.primary.main, 0.2)}`,
                        '&.Mui-selected': {
                          background: theme =>
                            `linear-gradient(135deg, ${theme.palette.primary.main}, ${alpha(theme.palette.primary.main, 0.8)})`,
                          color: 'white',
                          '&:hover': {
                            background: theme =>
                              `linear-gradient(135deg, ${theme.palette.primary.main}, ${alpha(theme.palette.primary.main, 0.9)})`,
                          },
                        },
                        '&:hover': {
                          background: theme => alpha(theme.palette.primary.main, 0.1),
                        },
                      },
                    }}
                  >
                    <ToggleButton value="Both" aria-label="both">
                      Both
                    </ToggleButton>
                    <ToggleButton value="Unilateral" aria-label="unilateral">
                      Unilateral
                    </ToggleButton>
                    <ToggleButton value="Bilateral" aria-label="bilateral">
                      Bilateral
                    </ToggleButton>
                  </ToggleButtonGroup>
                </Box>

                {/* Exercise Type Filter */}
                <Box>
                  <Typography
                    variant="subtitle1"
                    sx={{ fontWeight: 600, mb: 2, color: 'text.primary' }}
                  >
                    Exercise Type
                  </Typography>
                  <ToggleButtonGroup
                    value={isAccessoryFilter}
                    exclusive
                    onChange={(_, newValue) => newValue && setIsAccessoryFilter(newValue)}
                    aria-label="exercise type"
                    sx={{
                      '& .MuiToggleButton-root': {
                        textTransform: 'none',
                        fontWeight: 500,
                        borderRadius: 2,
                        border: theme => `1px solid ${alpha(theme.palette.secondary.main, 0.2)}`,
                        '&.Mui-selected': {
                          background: theme =>
                            `linear-gradient(135deg, ${theme.palette.secondary.main}, ${alpha(theme.palette.secondary.main, 0.8)})`,
                          color: 'white',
                          '&:hover': {
                            background: theme =>
                              `linear-gradient(135deg, ${theme.palette.secondary.main}, ${alpha(theme.palette.secondary.main, 0.9)})`,
                          },
                        },
                        '&:hover': {
                          background: theme => alpha(theme.palette.secondary.main, 0.1),
                        },
                      },
                    }}
                  >
                    <ToggleButton value="Both" aria-label="both">
                      Both
                    </ToggleButton>
                    <ToggleButton value="Primary" aria-label="primary">
                      Primary
                    </ToggleButton>
                    <ToggleButton value="Accessory" aria-label="accessory">
                      Accessory
                    </ToggleButton>
                  </ToggleButtonGroup>
                </Box>

                {/* Body Part Filter */}
                <Box>
                  <Typography
                    variant="subtitle1"
                    sx={{ fontWeight: 600, mb: 2, color: 'text.primary' }}
                  >
                    Body Part
                  </Typography>
                  <ToggleButtonGroup
                    value={isUpperFilter}
                    exclusive
                    onChange={(_, newValue) => newValue && setIsUpperFilter(newValue)}
                    aria-label="body part"
                    sx={{
                      '& .MuiToggleButton-root': {
                        textTransform: 'none',
                        fontWeight: 500,
                        borderRadius: 2,
                        border: theme => `1px solid ${alpha(theme.palette.success.main, 0.2)}`,
                        '&.Mui-selected': {
                          background: theme =>
                            `linear-gradient(135deg, ${theme.palette.success.main}, ${alpha(theme.palette.success.main, 0.8)})`,
                          color: 'white',
                          '&:hover': {
                            background: theme =>
                              `linear-gradient(135deg, ${theme.palette.success.main}, ${alpha(theme.palette.success.main, 0.9)})`,
                          },
                        },
                        '&:hover': {
                          background: theme => alpha(theme.palette.success.main, 0.1),
                        },
                      },
                    }}
                  >
                    <ToggleButton value="Both" aria-label="both">
                      Both
                    </ToggleButton>
                    <ToggleButton value="Upper" aria-label="upper">
                      Upper Body
                    </ToggleButton>
                    <ToggleButton value="Lower" aria-label="lower">
                      Lower Body
                    </ToggleButton>
                  </ToggleButtonGroup>
                </Box>
              </Stack>
            </Grid>
          </Grid>
        </Paper>

        {/* Results Section */}
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 600, mb: 3 }}>
            Results ({exercisesToDisplay.length} exercises)
          </Typography>

          {exercisesToDisplay.length === 0 ? (
            <Paper
              sx={{
                p: 6,
                textAlign: 'center',
                borderRadius: 3,
                background: theme =>
                  `linear-gradient(135deg, ${alpha(theme.palette.background.paper, 0.8)}, ${alpha(theme.palette.background.paper, 0.6)})`,
                border: theme => `1px solid ${alpha(theme.palette.divider, 0.3)}`,
              }}
            >
              <Typography variant="h6" color="text.secondary" sx={{ opacity: 0.7 }}>
                No exercises found matching your filters.
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1, opacity: 0.6 }}>
                Try adjusting your filter criteria to see more results.
              </Typography>
            </Paper>
          ) : (
            <Grid container spacing={3}>
              {exercisesToDisplay.map(exercise => (
                <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={exercise.name}>
                  <ExerciseCard exercise={exercise} />
                </Grid>
              ))}
            </Grid>
          )}
        </Box>
      </Stack>
    </Container>
  );
}
