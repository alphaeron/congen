import {
  Box,
  Card,
  CardContent,
  FormControl,
  FormControlLabel,
  FormLabel,
  Grid,
  LinearProgress,
  Radio,
  RadioGroup,
} from '@mui/material';
import React from 'react';

import { GameText, GAME_CLASSES } from './GameTheme';
import { NumericStepInput } from './NumericStepInput';
import type { Exercise } from '../api/types';
import { formatWeightWithUnit, KG_TO_LBS } from '../common/utils';

interface OneRepMaxData {
  exerciseName: string;
  reps?: number;
  weight?: number;
  unit?: string;
  declined?: boolean;
}

interface OneRepMaxInputStepProps {
  exercises: Exercise[];
  onDeclineAll: () => void;
  onComplete: () => void;
  onInputsChange?: (inputs: OneRepMaxData[]) => void;
  onNavigationChange?: (navigation: {
    onSkipExercise: () => void;
    onSkipRemaining: () => void;
    onNext: () => void;
    isLastExercise: boolean;
  }) => void;
}

/**
 * Component for collecting 1RM data from users.
 *
 * Shows one exercise at a time with reps/weight input form.
 * Allows users to:
 * - Provide reps/weight data (converted to 1RM using Epley formula)
 * - Skip individual exercises
 * - Skip remaining exercises
 */
export const OneRepMaxInputStep: React.FC<OneRepMaxInputStepProps> = ({
  exercises,
  onComplete,
  onInputsChange,
  onNavigationChange,
}) => {
  const [inputs, setInputs] = React.useState<OneRepMaxData[]>(
    exercises.map(exercise => ({
      exerciseName: exercise.name,
      reps: exercise.set_schemes?.[0]?.reps || 8,
      weight: exercise.set_schemes?.[0]?.weight || 0,
      unit: exercise.set_schemes?.[0]?.weight_unit || 'KG',
      declined: false,
    }))
  );

  const [currentExerciseIndex, setCurrentExerciseIndex] = React.useState(0);
  const [isComplete, setIsComplete] = React.useState(false);

  const updateInput = (exerciseName: string, updates: Partial<OneRepMaxData>) => {
    const newInputs = inputs.map(input =>
      input.exerciseName === exerciseName ? { ...input, ...updates } : input
    );
    setInputs(newInputs);
  };

  // Call onInputsChange when inputs change
  React.useEffect(() => {
    if (onInputsChange) {
      onInputsChange(inputs);
    }
  }, [inputs, onInputsChange]);

  const handleRepsWeightChange = (
    exerciseName: string,
    reps: number,
    weight: number,
    unit: string
  ) => {
    updateInput(exerciseName, { reps, weight, unit });
  };

  const calculateOneRepMax = (weight: number, reps: number): number => {
    // Epley formula: 1RM = weight * (1 + reps / 30)
    return weight * (1 + reps / 30);
  };

  const currentExercise = exercises[currentExerciseIndex];
  const currentInput = inputs.find(i => i.exerciseName === currentExercise?.name);
  const progress = currentExerciseIndex + 1;
  const total = exercises.length;

  const handleNext = () => {
    if (currentExerciseIndex < exercises.length - 1) {
      setCurrentExerciseIndex(currentExerciseIndex + 1);
    } else {
      setIsComplete(true);
      onComplete();
    }
  };

  const handleSkipExercise = () => {
    updateInput(currentExercise.name, { declined: true });
    handleNext();
  };

  const handleSkipRemaining = () => {
    const newInputs = inputs.map((input, index) =>
      index >= currentExerciseIndex ? { ...input, declined: true } : input
    );
    setInputs(newInputs);
    setIsComplete(true);
    onComplete();
  };

  // Expose navigation functions to parent
  React.useEffect(() => {
    if (onNavigationChange) {
      onNavigationChange({
        onSkipExercise: handleSkipExercise,
        onSkipRemaining: handleSkipRemaining,
        onNext: handleNext,
        isLastExercise: currentExerciseIndex === exercises.length - 1,
      });
    }
  }, [currentExerciseIndex, exercises.length, onNavigationChange]);

  if (isComplete) {
    return (
      <Box>
        <GameText variant="h6" gutterBottom>
          All exercises processed!
        </GameText>
        <GameText variant="body2" textVariant="secondary">
          You have completed entering 1RM data for all exercises. Click &quot;Complete&quot; to
          finish.
        </GameText>
      </Box>
    );
  }

  if (!currentExercise || !currentInput) {
    return (
      <Box>
        <GameText variant="h6" gutterBottom>
          No exercises to process
        </GameText>
      </Box>
    );
  }

  return (
    <Box>
      {/* Progress indicator */}
      <Box sx={{ mb: 3 }}>
        <GameText variant="h6" gutterBottom>
          Record Your 1RM Data ({progress}/{total})
        </GameText>
        <LinearProgress variant="determinate" value={(progress / total) * 100} sx={{ mb: 1 }} />
      </Box>

      {/* Current exercise input */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <GameText
            variant="h5"
            gutterBottom
            className={`${GAME_CLASSES.textBold} ${GAME_CLASSES.marginBottom2}`}
          >
            {currentExercise.name}
          </GameText>
          <Grid container spacing={2}>
            <Grid size={4}>
              <NumericStepInput
                label="Reps"
                value={currentInput.reps ?? 8}
                onChange={reps =>
                  handleRepsWeightChange(
                    currentExercise.name,
                    reps ?? 8,
                    currentInput.weight ?? 0,
                    currentInput.unit || 'KG'
                  )
                }
                min={1}
                integer
              />
            </Grid>
            <Grid size={4}>
              <NumericStepInput
                label="Weight"
                suffix={(currentInput.unit || 'KG').toLowerCase()}
                value={currentInput.weight ?? 0}
                onChange={weight =>
                  handleRepsWeightChange(
                    currentExercise.name,
                    currentInput.reps ?? 8,
                    weight ?? 0,
                    currentInput.unit || 'KG'
                  )
                }
                min={0}
                step={0.5}
              />
            </Grid>
            <Grid size={4}>
              <FormControl fullWidth>
                <FormLabel>Unit</FormLabel>
                <RadioGroup
                  row
                  value={currentInput.unit || 'KG'}
                  onChange={e => {
                    handleRepsWeightChange(
                      currentExercise.name,
                      currentInput.reps || 8,
                      currentInput.weight || 0,
                      e.target.value
                    );
                  }}
                >
                  <FormControlLabel value="KG" control={<Radio />} label="KG" />
                  <FormControlLabel value="LBS" control={<Radio />} label="LBS" />
                </RadioGroup>
              </FormControl>
            </Grid>
          </Grid>

          {currentInput.reps &&
            currentInput.weight !== undefined &&
            currentInput.weight !== null && (
              <Box
                sx={{
                  mt: 2,
                  p: 2,
                  bgcolor: 'background.paper',
                  border: 1,
                  borderColor: 'divider',
                  borderRadius: 1,
                }}
              >
                <GameText variant="body2" textVariant="secondary">
                  <strong>Calculated 1RM:</strong>{' '}
                  {(() => {
                    const oneRepMax = calculateOneRepMax(
                      currentInput.weight || 0,
                      currentInput.reps || 0
                    );
                    const weightInKg =
                      currentInput.unit === 'LBS' ? oneRepMax / KG_TO_LBS : oneRepMax;
                    return formatWeightWithUnit(
                      weightInKg,
                      currentInput.unit as 'KG' | 'LBS' | undefined
                    );
                  })()}
                </GameText>
                <GameText variant="caption" textVariant="secondary">
                  (Using Epley formula: 1RM = weight × (1 + reps ÷ 30))
                </GameText>
              </Box>
            )}
        </CardContent>
      </Card>
    </Box>
  );
};
