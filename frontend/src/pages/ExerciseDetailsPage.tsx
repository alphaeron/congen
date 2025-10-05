import { Alert, AlertTitle, Typography } from '@mui/material';
import * as React from 'react';
import { useParams } from 'react-router';

import { ExerciseDetails } from '../components/ExerciseDetails';

/**
 * The exercise details page.
 *
 * @return The exercise details page.
 */
export function ExerciseDetailsPage(): React.ReactElement {
  const { exerciseName } = useParams();
  if (exerciseName) {
    // Decode the exercise name from the URL parameter
    const decodedExerciseName = decodeURIComponent(exerciseName);
    return <ExerciseDetails exerciseName={decodedExerciseName} />;
  } else {
    return (
      <Alert severity="error">
        <AlertTitle>Exercise Not Found</AlertTitle>
        <Typography>No exercise name provided in the URL.</Typography>
      </Alert>
    );
  }
} // end component ExerciseDetailsPage
