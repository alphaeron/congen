import { Tooltip } from '@mui/material';
import React from 'react';
import { Link } from 'react-router';

import { GameText } from './GameTheme';
import { encodeExerciseName } from '../api/endpoint';
import type { Exercise, Muscle } from '../api/types';
import { capitalizeEachWord } from '../common/utils';
import { useData } from '../contexts/DataContext';

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
  const { getExercise, getMuscle, getExerciseMuscles } = useData();
  const [exercise, setExercise] = React.useState<Exercise | null>(null);
  const [muscles, setMuscles] = React.useState<Muscle[]>([]);
  const [isLoading, setIsLoading] = React.useState(true);
  const [hasError, setHasError] = React.useState(false);

  // Load exercise data using DataContext
  React.useEffect(() => {
    const loadExerciseData = async () => {
      if (!exerciseName) return;

      setIsLoading(true);
      setHasError(false);

      try {
        // Load exercise data from DataContext
        const exerciseData = await getExercise(exerciseName);
        if (!exerciseData) {
          throw new Error('Exercise not found');
        }
        setExercise(exerciseData);

        // Load exercise muscles using DataContext
        const exerciseMuscles = await getExerciseMuscles(exerciseName);

        // Load individual muscle details using DataContext
        const muscleDetails = await Promise.all(
          exerciseMuscles.map(muscle => getMuscle(muscle.muscle_name))
        );
        setMuscles(muscleDetails.filter(Boolean) as Muscle[]);
      } catch {
        setHasError(true);
      } finally {
        setIsLoading(false);
      }
    };

    loadExerciseData();
  }, [exerciseName, getExercise, getMuscle, getExerciseMuscles]);

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
        to={`/exercises/${encodeExerciseName(exerciseName)}`}
        style={{ textDecoration: 'none', color: 'inherit' }}
      >
        <GameText
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
        </GameText>
      </Link>
    </Tooltip>
  );
}
