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
} from '@mui/material';
import { useForm } from '@tanstack/react-form';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState } from 'react';

import { OneRepMaxInputStep } from './OneRepMaxInputStep';
import { GameText } from './GameTheme';
import { WizardStep } from '../api/types';
import type {
  Program,
  UserExercisePoolResponse,
  WorkoutGenerationWizardData,
  Exercise,
  UserOneRepMax,
} from '../api/types';
import { generateNextWeek, getUserExercisePool, updateWorkoutWithOneRepMax } from '../api/conjugateWorkoutGenerator';
import { useData } from '../contexts/DataContext';

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
  const { userData } = useData(); // Only get userData for 1RM check, no other DataContext functions
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
  const form = useForm({
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
      let isMounted = true;

      const loadExercisePool = async () => {
        try {
          const pool = await getUserExercisePool();
          if (isMounted) {
            setExercisePool(pool);
          }
        } catch {
          if (isMounted) {
            enqueueSnackbar('Failed to load exercise pool', { variant: 'error' });
          }
        }
      };

      // Use setTimeout to ensure the async operation happens in the next tick
      const timeoutId = setTimeout(() => {
        loadExercisePool();
      }, 0);

      return () => {
        isMounted = false;
        clearTimeout(timeoutId);
      };
    }
  }, [open, enqueueSnackbar]);

  // Handle workout update when reaching UPDATING_WORKOUT_WITH_1RM stage
  useEffect(() => {
    if (currentStep === WizardStep.UPDATING_WORKOUT_WITH_1RM && !isUpdatingWorkout) {
      handleWorkoutUpdate();
    }
  }, [currentStep, isUpdatingWorkout]);

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
        // Don't refresh data here - let the UPDATING_WORKOUT_WITH_1RM stage handle it
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

      // Show success message and close - no DataContext refresh needed
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

    // Since generateWorkout returns a Program type without workout data,
    // we need to get the exercise data from the userData which should be refreshed
    // after workout generation. We'll use the exercise pool to determine which
    // exercises might need 1RM input based on the program's current week.
    
    // Get user's 1RM data from userData to check which exercises already have 1RM records
    const userOneRepMaxes = userData?.user_one_rep_max as unknown as UserOneRepMax[] || [];
    const exerciseNamesWithOneRepMax = new Set(userOneRepMaxes.map(orm => orm.exercise_name));
    
    // Return exercises from the pool that don't have 1RM data
    const exercisesWithoutOneRepMax = pool.primary_exercises
      .concat(pool.accessory_exercises)
      .filter(exercise => !exerciseNamesWithOneRepMax.has(exercise.name));

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
            <GameText variant="h6" gutterBottom>
              Generating Workouts
            </GameText>
            <GameText variant="body2" textVariant="secondary" gutterBottom>
              This may take a few moments...
            </GameText>
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
            <GameText variant="h6" gutterBottom>
              Updating Workout
            </GameText>
            <GameText variant="body2" textVariant="secondary" gutterBottom>
              Updating your workout with 1RM data...
            </GameText>
            <LinearProgress sx={{ mt: 2 }} />
          </Box>
        );

      case WizardStep.UPDATING_WORKOUT_WITH_1RM:
        return (
          <Box textAlign="center">
            <GameText variant="h6" gutterBottom>
              Updating Workout
            </GameText>
            <GameText variant="body2" textVariant="secondary" gutterBottom>
              Updating your workout with 1RM data...
            </GameText>
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
          <GameText variant="h6">
            Generate Workouts
          </GameText>
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
