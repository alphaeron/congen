import {
  Alert,
  AlertTitle,
  Box,
  Button,
  Card,
  CardContent,
  Dialog,
  DialogContent,
  DialogTitle,
  LinearProgress,
  Stepper,
  Step,
  StepLabel,
  Typography,
} from '@mui/material';
import { useForm } from '@tanstack/react-form';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState } from 'react';

import { OneRepMaxInputStep } from './OneRepMaxInputStep';
import {
  generateNextWeek,
  getUserExercisePool,
  updateWorkoutWithOneRepMax,
} from '../api/conjugateWorkoutGenerator';
import { WizardStep } from '../api/types';
import type {
  Program,
  UserExercisePoolResponse,
  WorkoutGenerationWizardData,
  Exercise,
} from '../api/types';

interface WorkoutGenerationWizardProps {
  open: boolean;
  onClose: () => void;
  onComplete: (program: Program) => void;
  program: Program;
}

/**
 * Wizard component for workout generation with 1RM input collection.
 *
 * This wizard guides users through:
 * 1. Workout generation confirmation
 * 2. Loading during generation
 * 3. 1RM input collection for exercises not in user's history
 * 4. Loading during 1RM recording
 * 5. Completion
 */
export const WorkoutGenerationWizard: React.FC<WorkoutGenerationWizardProps> = ({
  open,
  onClose,
  onComplete,
  program,
}) => {
  const { enqueueSnackbar } = useSnackbar();
  const [currentStep, setCurrentStep] = useState<WizardStep>(WizardStep.WORKOUT_GENERATION);
  const [generatedWorkout, setGeneratedWorkout] = useState<Program | null>(null);
  const [exercisePool, setExercisePool] = useState<UserExercisePoolResponse | null>(null);
  const [isGenerating, setIsGenerating] = useState(false);
  const [isUpdatingWorkout, setIsUpdatingWorkout] = useState(false);
  const [oneRepMaxNavigation, setOneRepMaxNavigation] = useState<{
    onSkipExercise: () => void;
    onSkipRemaining: () => void;
    onNext: () => void;
    isLastExercise: boolean;
  } | null>(null);

  // Helper function to get active step for stepper
  const getActiveStep = (): number => {
    switch (currentStep) {
      case WizardStep.WORKOUT_GENERATION:
        return 0;
      case WizardStep.GENERATION_LOADING:
        return 0; // Stay on step 0 during generation
      case WizardStep.ONE_REP_MAX_INPUT:
        return 1;
      case WizardStep.UPDATING_WORKOUT:
        return 1; // Stay on step 1 during updating
      case WizardStep.UPDATING_WORKOUT_WITH_1RM:
        return 2;
      default:
        return 0;
    }
  };

  // TanStack Form for wizard data
  const form = useForm<WorkoutGenerationWizardData>({
    defaultValues: {
      programId: program.id,
      currentStep: WizardStep.WORKOUT_GENERATION,
      declinedExercises: [],
      declineAll: false,
    },
    onSubmit: async ({ value }) => {
      // Handle form submission based on current step
      if (value.currentStep === WizardStep.WORKOUT_GENERATION) {
        await handleWorkoutGeneration();
      } else if (value.currentStep === WizardStep.ONE_REP_MAX_INPUT) {
        await handleOneRepMaxSubmission();
      }
    },
  });

  // Load exercise pool when wizard opens
  useEffect(() => {
    if (open) {
      loadExercisePool();
    }
  }, [open]);

  // Handle workout update when reaching UPDATING_WORKOUT_WITH_1RM stage
  useEffect(() => {
    if (currentStep === WizardStep.UPDATING_WORKOUT_WITH_1RM && !isUpdatingWorkout) {
      handleWorkoutUpdate();
    }
  }, [currentStep, isUpdatingWorkout]);

  const loadExercisePool = async () => {
    try {
      const pool = await getUserExercisePool();
      setExercisePool(pool);
    } catch {
      enqueueSnackbar('Failed to load exercise pool', { variant: 'error' });
    }
  };

  const handleWorkoutGeneration = async () => {
    setIsGenerating(true);
    setCurrentStep(WizardStep.GENERATION_LOADING);

    try {
      const workout = await generateNextWeek(program.id);
      setGeneratedWorkout(workout);

      // Check if there are exercises that need 1RM input
      const exercisesNeedingInput = getExercisesNeedingOneRepMax(workout, exercisePool);

      if (exercisesNeedingInput.length > 0) {
        setCurrentStep(WizardStep.ONE_REP_MAX_INPUT);
      } else {
        setCurrentStep(WizardStep.UPDATING_WORKOUT_WITH_1RM);
        onComplete(workout);
      }
    } catch {
      enqueueSnackbar('Failed to generate workouts', { variant: 'error' });
      onClose(); // Close the dialog when generation fails
    } finally {
      setIsGenerating(false);
    }
  };

  const handleOneRepMaxSubmission = async () => {
    // Move to UPDATING_WORKOUT_WITH_1RM stage where the API call will happen
    setCurrentStep(WizardStep.UPDATING_WORKOUT_WITH_1RM);
  };

  const handleWorkoutUpdate = async () => {
    setIsUpdatingWorkout(true);

    try {
      // Make API call to update workout with 1RM data (backend will fetch from database)
      const updatedWorkout = await updateWorkoutWithOneRepMax(program.id);

      // Show success message and close
      enqueueSnackbar('Workout updated successfully!', { variant: 'success' });
      onComplete(updatedWorkout);
    } catch {
      enqueueSnackbar('Failed to update workout with 1RM data', { variant: 'error' });
      setCurrentStep(WizardStep.ONE_REP_MAX_INPUT);
    } finally {
      setIsUpdatingWorkout(false);
    }
  };

  const getExercisesNeedingOneRepMax = (
    workout: Program,
    pool: UserExercisePoolResponse | null
  ): Exercise[] => {
    if (!pool || !workout) return [];

    // Get all exercises from the generated workout
    const workoutExercises = new Set<string>();

    // Extract exercise names from workout data structure
    if (workout.workouts) {
      workout.workouts.forEach(workoutItem => {
        if (workoutItem.exercises) {
          workoutItem.exercises.forEach(exercise => {
            workoutExercises.add(exercise.exercise_name);
          });
        }
      });
    }

    // For now, let's return all exercises from the pool that don't have 1RM data
    // This ensures we always show the 1RM input step for testing
    const exercisesWithoutOneRepMax = pool.primary_exercises
      .concat(pool.accessory_exercises)
      .filter(exercise => !exercise.one_rep_max);

    return exercisesWithoutOneRepMax;
  };

  const handleClose = () => {
    if (
      currentStep === WizardStep.GENERATION_LOADING ||
      currentStep === WizardStep.UPDATING_WORKOUT ||
      isUpdatingWorkout
    ) {
      return; // Prevent closing during loading or updating
    }
    onClose();
  };

  const renderStepContent = () => {
    switch (currentStep) {
      case WizardStep.WORKOUT_GENERATION:
        return (
          <Alert severity="info">
            <AlertTitle>Generate Workouts</AlertTitle>
            The next week&apos;s workouts will be generated for {program.name}. This will create a
            new week of workouts based on your program preferences and current progress.
          </Alert>
        );

      case WizardStep.GENERATION_LOADING:
        return (
          <Box textAlign="center">
            <Typography variant="h6" gutterBottom>
              Generating Workouts
            </Typography>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              This may take a few moments...
            </Typography>
            <LinearProgress sx={{ mt: 2 }} />
          </Box>
        );

      case WizardStep.ONE_REP_MAX_INPUT: {
        const exercisesNeedingInput = getExercisesNeedingOneRepMax(generatedWorkout!, exercisePool);
        return (
          <OneRepMaxInputStep
            exercises={exercisesNeedingInput}
            onDeclineAll={() => {
              form.setFieldValue('declineAll', true);
              handleOneRepMaxSubmission();
            }}
            onComplete={() => handleOneRepMaxSubmission()}
            onNavigationChange={setOneRepMaxNavigation}
          />
        );
      }

      case WizardStep.UPDATING_WORKOUT:
        return (
          <Box textAlign="center">
            <Typography variant="h6" gutterBottom>
              Updating Workout
            </Typography>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              Updating your workout with 1RM data...
            </Typography>
            <LinearProgress sx={{ mt: 2 }} />
          </Box>
        );

      case WizardStep.UPDATING_WORKOUT_WITH_1RM:
        return (
          <Box textAlign="center">
            <Typography variant="h6" gutterBottom>
              Updating Workout
            </Typography>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              Updating your workout with 1RM data...
            </Typography>
            <LinearProgress sx={{ mt: 2 }} />
          </Box>
        );

      default:
        return null;
    }
  };

  const renderActions = () => {
    switch (currentStep) {
      case WizardStep.WORKOUT_GENERATION:
        return (
          <Box display="flex" gap={2} justifyContent="flex-end">
            <Button onClick={handleClose} disabled={isGenerating}>
              Cancel
            </Button>
            <Button variant="contained" onClick={() => form.handleSubmit()} disabled={isGenerating}>
              {isGenerating ? 'Generating...' : 'Generate Workouts'}
            </Button>
          </Box>
        );

      case WizardStep.ONE_REP_MAX_INPUT:
        if (!oneRepMaxNavigation) return null;
        return (
          <Box display="flex" gap={2} justifyContent="space-between">
            <Box>
              <Button
                variant="outlined"
                onClick={oneRepMaxNavigation.onSkipExercise}
                sx={{ mr: 1 }}
              >
                Skip Exercise
              </Button>
              <Button variant="outlined" onClick={oneRepMaxNavigation.onSkipRemaining}>
                Skip Remaining
              </Button>
            </Box>
            <Button variant="contained" onClick={oneRepMaxNavigation.onNext}>
              {oneRepMaxNavigation.isLastExercise ? 'Updating Workout' : 'Next Exercise'}
            </Button>
          </Box>
        );

      case WizardStep.UPDATING_WORKOUT_WITH_1RM:
        return null; // No action buttons during updating

      default:
        return null;
    }
  };

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      maxWidth="md"
      fullWidth
      disableEscapeKeyDown={
        currentStep === WizardStep.GENERATION_LOADING ||
        currentStep === WizardStep.UPDATING_WORKOUT ||
        isUpdatingWorkout
      }
    >
      <DialogTitle>
        <Box>
          <Typography variant="h6" component="div">
            Generate Workouts
          </Typography>
          <Box mt={2}>
            <Stepper activeStep={getActiveStep()} alternativeLabel>
              <Step>
                <StepLabel>Generate</StepLabel>
              </Step>
              <Step>
                <StepLabel>Enter 1RM Values</StepLabel>
              </Step>
              <Step>
                <StepLabel>Updating Workout</StepLabel>
              </Step>
            </Stepper>
          </Box>
        </Box>
      </DialogTitle>

      <DialogContent>
        <Card
          sx={{ '& .MuiCard-root': { animation: 'none !important' }, animation: 'none !important' }}
        >
          <CardContent>{renderStepContent()}</CardContent>
        </Card>

        <Box mt={3}>{renderActions()}</Box>
      </DialogContent>
    </Dialog>
  );
};
