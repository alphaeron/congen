import Alert from '@mui/material/Alert';
import AlertTitle from '@mui/material/AlertTitle';
import Box from '@mui/material/Box';
import Chip from '@mui/material/Chip';
import Divider from '@mui/material/Divider';
import Grid from '@mui/material/Grid';
import Skeleton from '@mui/material/Skeleton';
import Stack from '@mui/material/Stack';
import Tooltip from '@mui/material/Tooltip';
import Typography from '@mui/material/Typography';
import * as React from 'react';
import { createEditor } from 'slate';
import { Slate, Editable, withReact } from 'slate-react';

import { BinaryTag } from './BinaryTag';
import { ExercisePreferenceControls } from './ExercisePreferenceControls';
import { LoadingSpinner } from './LoadingSpinner';
import { getIndividualEquipment } from '../api/equipment';
import { getIndividualExercise, getExerciseMuscles, getExerciseEquipment } from '../api/exercise';
import { useApiGet } from '../api/hooks';
import { getIndividualMuscle } from '../api/muscle';
import type { Exercise, ExerciseEquipment, ExerciseMuscle, Equipment, Muscle } from '../api/types';
import { capitalizeEachWord } from '../common/utils';

/**
 * Props for the ExerciseDetails component.
 */
interface ExerciseDetailsProps {
  exerciseName: string;
} // end interface ExerciseDetailsProps

/**
 * Shows an individual exercise.
 *
 * @return The exercise details component.
 */
export function ExerciseDetails(
  props: ExerciseDetailsProps
): React.ReactElement<ExerciseDetailsProps> {
  const {
    data: exercise,
    isLoading: isExerciseLoading,
    error: exerciseError,
    isError: isExerciseError,
  } = useApiGet<Exercise>(
    [`individualExercise${props.exerciseName}`],
    (exerciseName: unknown) => getIndividualExercise(exerciseName as string),
    {
      retry: 1,
    },
    [props.exerciseName]
  );

  const {
    data: exerciseMuscles,
    isLoading: isExerciseMuscleLoading,
    error: exerciseMuscleError,
    isError: isExerciseMuscleError,
  } = useApiGet<ExerciseMuscle[]>(
    [`exerciseMuscle${props.exerciseName}`],
    (exerciseName: unknown) => getExerciseMuscles(exerciseName as string),
    {
      retry: 1,
    },
    [props.exerciseName]
  );

  const {
    data: muscles,
    isLoading: isMusclesLoading,
    isError: isMusclesError,
  } = useApiGet<Muscle[]>(
    [`muscles${props.exerciseName}`, exerciseMuscles?.map(m => m.muscle_name).join(',') || ''],
    async (): Promise<Muscle[]> => {
      if (!exerciseMuscles || exerciseMuscles.length === 0) return [];
      return Promise.all(exerciseMuscles.map(element => getIndividualMuscle(element.muscle_name)));
    },
    {
      retry: 1,
    }
  );

  const {
    data: exerciseEquipment,
    isLoading: isExerciseEquipmentLoading,
    error: exerciseEquipmentError,
    isError: isExerciseEquipmentError,
  } = useApiGet<ExerciseEquipment[]>(
    [`exerciseEquipment${props.exerciseName}`],
    (exerciseName: unknown) => getExerciseEquipment(exerciseName as string),
    {
      retry: 1,
    },
    [props.exerciseName]
  );

  const {
    data: equipment,
    isLoading: isEquipmentLoading,
    isError: isEquipmentError,
  } = useApiGet<Equipment[]>(
    [
      `equipment${props.exerciseName}`,
      exerciseEquipment?.map(e => e.equipment_name).join(',') || '',
    ],
    async (): Promise<Equipment[]> => {
      if (!exerciseEquipment || exerciseEquipment.length === 0) return [];
      return Promise.all(
        exerciseEquipment.map(element => getIndividualEquipment(element.equipment_name))
      );
    },
    {
      retry: 1,
    }
  );

  const editor = React.useMemo(() => withReact(createEditor()), []);

  if (
    isExerciseLoading ||
    isEquipmentLoading ||
    isMusclesLoading ||
    isExerciseMuscleLoading ||
    isExerciseEquipmentLoading ||
    !props.exerciseName ||
    exercise === undefined ||
    equipment === undefined ||
    muscles === undefined
  ) {
    return <LoadingSpinner message="Loading exercise details..." fullHeight={true} />;
  } else if (
    isExerciseError ||
    isEquipmentError ||
    isMusclesError ||
    isExerciseMuscleError ||
    isExerciseEquipmentError
  ) {
    // Check if it's a network error or authentication error
    const isNetworkError =
      exerciseError?.message?.includes('Network Error') ||
      exerciseError?.message?.includes('timeout') ||
      exerciseError?.message?.includes('NS_BINDING_ABORTED');

    if (isNetworkError) {
      return (
        <Alert severity="warning">
          <AlertTitle>Connection Error</AlertTitle>
          <Typography>
            Unable to connect to the server. Please check your internet connection and try again.
          </Typography>
          <Typography variant="body2" sx={{ mt: 1 }}>
            Error: {exerciseError?.message || 'Network error'}
          </Typography>
        </Alert>
      );
    } else {
      return (
        <Alert severity="error">
          <AlertTitle>Exercise Not Found</AlertTitle>
          <Typography>The specified exercise could not be found.</Typography>
          {exerciseError && (
            <Typography variant="body2" sx={{ mt: 1 }}>
              {exerciseError.toString()}
            </Typography>
          )}
          {exerciseMuscleError && (
            <Typography variant="body2" sx={{ mt: 1 }}>
              {exerciseMuscleError.toString()}
            </Typography>
          )}
          {exerciseEquipmentError && (
            <Typography variant="body2" sx={{ mt: 1 }}>
              {exerciseEquipmentError.toString()}
            </Typography>
          )}
        </Alert>
      );
    }
  } else {
    return (
      <React.Fragment>
        <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
          <Typography variant="h1" gutterBottom={true}>
            {exercise.name}
          </Typography>
          <ExercisePreferenceControls
            exerciseName={exercise.name}
            variant="segmented"
            size="medium"
          />
        </Box>
        <Grid container={true} spacing={2}>
          <Grid size={{ xs: 12 }}>
            <Stack direction="row" spacing={2}>
              <Chip
                label={`${capitalizeEachWord(exercise.movement_type)} Exercise`}
                color="primary"
              />
              <BinaryTag
                isOn={exercise.is_upper}
                onText="Upper Body"
                offText="Lower Body"
                color="success"
              />
              <BinaryTag
                isOn={exercise.is_accessory}
                onText="Accessory"
                offText="Primary Movement"
                color="secondary"
              />
              <BinaryTag
                isOn={exercise.is_unilateral}
                onText="Unilateral"
                offText="Bilateral"
                color="primary"
              />
            </Stack>
          </Grid>
          <Grid size={{ xs: 9 }}>
            <Stack spacing={2}>
              <Skeleton variant="rectangular" height={360} />
              <Slate
                editor={editor}
                initialValue={[
                  {
                    children: [
                      {
                        text: exercise.description,
                      },
                    ],
                  },
                ]}
              >
                <Editable readOnly={true} placeholder="No description provided." />
              </Slate>
            </Stack>
          </Grid>
          <Grid size={{ xs: 3 }}>
            <Grid container={true} spacing={2}>
              <Grid size={{ xs: 12 }}>
                <Divider textAlign="left" sx={{ marginBottom: '16px' }}>
                  <Typography variant="h3">Muscles Worked</Typography>
                </Divider>
                <Grid container={true} spacing={2}>
                  {exerciseMuscles?.map(em => {
                    const muscle = muscles.find(elem => elem.name === em.muscle_name);
                    return (
                      <Grid size={{ xs: 12 }} key={em.muscle_name}>
                        <Tooltip arrow={true} title={muscle?.description}>
                          <Chip label={`${capitalizeEachWord(em.muscle_name)}`} />
                        </Tooltip>
                      </Grid>
                    );
                  })}
                </Grid>
              </Grid>
              <Grid size={{ xs: 12 }}>
                <Divider textAlign="left" sx={{ marginBottom: '16px' }}>
                  <Typography variant="h3">Equipment Needed</Typography>
                </Divider>
                <Grid container={true} spacing={2}>
                  {exerciseEquipment?.map(ee => {
                    const equip = equipment.find(elem => elem.name === ee.equipment_name);
                    return (
                      <Grid size={{ xs: 12 }} key={ee.equipment_name}>
                        <Tooltip arrow={true} title={equip?.description}>
                          <Chip label={`${capitalizeEachWord(ee.equipment_name)}`} />
                        </Tooltip>
                      </Grid>
                    );
                  })}
                </Grid>
              </Grid>
            </Grid>
          </Grid>
        </Grid>
      </React.Fragment>
    );
  }
} // end component ExerciseOverview
