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
    return <ExerciseDetails exerciseName={exerciseName} />;
  } else {
    // TODO Show alert instead?
    // eslint-disable-next-line react/jsx-no-useless-fragment
    return <React.Fragment />;
  }
} // end component ExerciseDetailsPage
