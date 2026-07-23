import { Box, Grid, Alert } from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useState, useEffect, useMemo } from 'react';

import { ExerciseCard } from './ExerciseCard';
import { LoadingSpinner } from './LoadingSpinner';
import { useAuth } from '../contexts/AuthContext';
import { useData } from '../contexts/DataContext';

interface ExerciseCategoryDetailsProps {
  category: string;
}

/**
 * ExerciseCategoryDetails component for viewing detailed information about a specific exercise category.
 *
 * Features:
 * - Display exercises for a specific category (primary, accessory, equipment, avoided)
 * - Breadcrumb navigation with category name
 * - Slide animation for smooth transitions
 * - URL query parameters for category selection
 * - Detailed exercise information with chips
 *
 * @param category The exercise category to display
 * @returns ExerciseCategoryDetails component
 */
export const ExerciseCategoryDetails: React.FC<ExerciseCategoryDetailsProps> = ({ category }) => {
  const { enqueueSnackbar } = useSnackbar();
  const { user } = useAuth();
  const { userExercisePool, loadUserExercisePool } = useData();

  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const loadData = async () => {
      if (!user?.keycloak_id) return;

      try {
        setIsLoading(true);
        if (!userExercisePool) {
          await loadUserExercisePool();
        }
      } catch {
        enqueueSnackbar('Failed to load exercise pool data. Please try again.', {
          variant: 'error',
        });
      } finally {
        setIsLoading(false);
      }
    };

    loadData();
  }, [user?.keycloak_id, userExercisePool, loadUserExercisePool, enqueueSnackbar]);

  const categoryData = useMemo(() => {
    if (!userExercisePool) return null;

    switch (category) {
      case 'primary':
        return {
          title: 'Primary Exercise',
          exercises: userExercisePool.primary_exercises,
          color: 'error' as const,
        };
      case 'accessory':
        return {
          title: 'Accessory Exercise',
          exercises: userExercisePool.accessory_exercises,
          color: 'info' as const,
        };
      case 'recent':
        return {
          title: 'Recent Exercise',
          exercises: userExercisePool.previously_used_exercises.map(exerciseName => ({
            name: exerciseName,
            description: `Recently used: ${exerciseName}`,
            movement_type: 'recent',
            is_unilateral: false,
            is_upper: false,
            is_accessory: false,
          })),
          color: 'warning' as const,
        };
      default:
        return null;
    }
  }, [userExercisePool, category]);

  if (isLoading) {
    return <LoadingSpinner message="Loading exercise category details..." />;
  }

  if (!categoryData) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">Invalid exercise category. Please select a valid category.</Alert>
      </Box>
    );
  }

  return (
    <Box>
      <Grid container spacing={3}>
        {categoryData.exercises.map(exercise => (
          <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={exercise.name}>
            <ExerciseCard exercise={exercise} />
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};
