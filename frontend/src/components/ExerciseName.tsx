import { Tooltip, Typography } from '@mui/material';
import React from 'react';
import { Link } from 'react-router';

import { getIndividualExercise, getExerciseMuscles } from '../api/exercise';
import { useApiGet } from '../api/hooks';
import { getIndividualMuscle } from '../api/muscle';
import type { Exercise, ExerciseMuscle, Muscle } from '../api/types';
import { capitalizeEachWord } from '../common/utils';

import type { TypographyProps } from '@mui/material';

interface ExerciseNameProps {
  exerciseName: string;
  variant?: TypographyProps['variant'];
  component?: React.ElementType;
  sx?: TypographyProps['sx'];
  children?: React.ReactNode;
}

/**
 * Component that renders an exercise name with a tooltip containing exercise information.
 * The tooltip shows exercise details, muscles worked, and other relevant information.
 *
 * @param props The component props.
 * @return The exercise name component with tooltip.
 */
export function ExerciseName({
  exerciseName,
  variant = 'body2',
  component,
  sx,
  children,
}: ExerciseNameProps): React.ReactElement {
  const {
    data: exercise,
    isLoading: isExerciseLoading,
    error: exerciseError,
  } = useApiGet<Exercise>(
    [`individualExercise${exerciseName}`],
    getIndividualExercise,
    {
      enabled: true,
      refetchOnWindowFocus: false,
      retry: 1,
    },
    [exerciseName]
  );

  const { data: exerciseMuscles, isLoading: isExerciseMuscleLoading } = useApiGet<ExerciseMuscle[]>(
    [`exerciseMuscle${exerciseName}`],
    getExerciseMuscles,
    {
      enabled: true,
      refetchOnWindowFocus: false,
      retry: 1,
    },
    [exerciseName]
  );

  const { data: muscles, isLoading: isMusclesLoading } = useApiGet<Muscle[]>(
    [`muscles${exerciseName}`, exerciseMuscles],
    async (): Promise<Muscle[]> =>
      Promise.all(exerciseMuscles.map(element => getIndividualMuscle(element.muscle_name))),
    {
      refetchOnWindowFocus: false,
      retry: 1,
      enabled: exerciseMuscles && exerciseMuscles.length > 0,
    }
  );

  const isLoading = isExerciseLoading || isExerciseMuscleLoading || isMusclesLoading;
  const hasError = exerciseError;

  const tooltipContent = React.useMemo(() => {
    if (isLoading) {
      return 'Loading exercise information...';
    }

    if (hasError || !exercise) {
      return `Exercise: ${exerciseName}`;
    }

    const muscleNames = muscles?.map(m => capitalizeEachWord(m.name)).join(', ') || 'Unknown';

    return (
      <div>
        <div style={{ fontWeight: 'bold', marginBottom: '8px' }}>{exercise.name}</div>
        <div style={{ marginBottom: '4px' }}>
          <strong>Type:</strong> {capitalizeEachWord(exercise.movement_type)}
        </div>
        <div style={{ marginBottom: '4px' }}>
          <strong>Body:</strong> {exercise.is_upper ? 'Upper Body' : 'Lower Body'}
        </div>
        <div style={{ marginBottom: '4px' }}>
          <strong>Category:</strong> {exercise.is_accessory ? 'Accessory' : 'Primary'}
        </div>
        <div style={{ marginBottom: '4px' }}>
          <strong>Movement:</strong> {exercise.is_unilateral ? 'Unilateral' : 'Bilateral'}
        </div>
        <div style={{ marginBottom: '4px' }}>
          <strong>Muscles:</strong> {muscleNames}
        </div>
        {exercise.description && (
          <div style={{ marginTop: '8px', fontStyle: 'italic' }}>{exercise.description}</div>
        )}
      </div>
    );
  }, [exercise, muscles, exerciseName, isLoading, hasError]);

  return (
    <Tooltip title={tooltipContent} arrow placement="top">
      <Link
        to={`/exercises/${encodeURIComponent(exerciseName)}`}
        style={{ textDecoration: 'none', color: 'inherit' }}
      >
        <Typography
          variant={variant}
          component={component}
          sx={{
            cursor: 'pointer',
            '&:hover': {
              textDecoration: 'underline',
            },
            ...sx,
          }}
        >
          {children || exerciseName}
        </Typography>
      </Link>
    </Tooltip>
  );
}
