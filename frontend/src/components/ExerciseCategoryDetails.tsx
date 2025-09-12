import {
  FitnessCenter as FitnessCenterIcon,
  Speed as SpeedIcon,
  Timeline as TimelineIcon,
} from '@mui/icons-material';
import {
  Box,
  Typography,
  Grid,
  Alert,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useState, useEffect, useMemo } from 'react';

import { LoadingSpinner } from './LoadingSpinner';
import { ExerciseCard } from './ExerciseCard';
import { getUserExercisePool } from '../api/conjugateWorkoutGenerator';
import type { UserExercisePoolResponse, Exercise } from '../api/types';
import { useAuth } from '../contexts/AuthContext';

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

  const [exercisePoolData, setExercisePoolData] = useState<UserExercisePoolResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Load exercise pool data
  useEffect(() => {
    if (user?.keycloak_id) {
      loadData();
    }
  }, [user?.keycloak_id]);

  const loadData = async () => {
    try {
      setIsLoading(true);
      const exercisePoolData = await getUserExercisePool();
      setExercisePoolData(exercisePoolData);
    } catch (error) {
      enqueueSnackbar('Failed to load exercise pool data. Please try again.', { variant: 'error' });
    } finally {
      setIsLoading(false);
    }
  };


  // Get category data based on the selected category
  const categoryData = useMemo(() => {
    if (!exercisePoolData) return null;

    switch (category) {
      case 'primary':
        return {
          title: 'Primary Exercises',
          exercises: exercisePoolData.primary_exercises,
          color: 'error' as const,
        };
      case 'accessory':
        return {
          title: 'Accessory Exercises',
          exercises: exercisePoolData.accessory_exercises,
          color: 'info' as const,
        };
      case 'recent':
        return {
          title: 'Recent Exercises',
          exercises: exercisePoolData.previously_used_exercises.map(exerciseName => ({
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
  }, [exercisePoolData, category]);

  if (isLoading) {
    return <LoadingSpinner message="Loading exercise category details..." />;
  }

  if (!categoryData) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">
          Invalid exercise category. Please select a valid category.
        </Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>

        {/* Category Header */}
        <Box sx={{ mb: 3 }}>
          <Alert severity="info" sx={{ mb: 2 }}>
            {categoryData.exercises.length === 0 
              ? `No ${categoryData.title.toLowerCase()} in your rotation.`
              : `${categoryData.exercises.length} ${categoryData.title.toLowerCase()}${categoryData.exercises.length === 1 ? '' : 's'} in your rotation.`
            }
          </Alert>
        </Box>

        {/* Exercise Grid */}
        <Grid container spacing={3}>
          {categoryData.exercises.map((exercise) => (
            <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={exercise.name}>
              <ExerciseCard exercise={exercise} />
            </Grid>
          ))}
        </Grid>
    </Box>
  );
};
