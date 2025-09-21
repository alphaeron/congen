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
import type { Exercise, ExerciseEquipment, ExerciseMuscle, Equipment, Muscle } from '../api/types';
import { capitalizeEachWord } from '../common/utils';
import { useData } from '../contexts/DataContext';

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
  const { getExercise, getExerciseEquipmentData, getMuscle, getEquipment, getExerciseMuscles } = useData();
  const [exercise, setExercise] = React.useState<Exercise | null>(null);
  const [exerciseLoading, setExerciseLoading] = React.useState(true);
  const [exerciseError, setExerciseError] = React.useState<Error | null>(null);

  const [exerciseMuscles, setExerciseMuscles] = React.useState<ExerciseMuscle[]>([]);
  const [muscles, setMuscles] = React.useState<Muscle[]>([]);
  const [exerciseEquipment, setExerciseEquipment] = React.useState<ExerciseEquipment[]>([]);
  const [equipment, setEquipment] = React.useState<Equipment[]>([]);
  const [isLoading, setIsLoading] = React.useState(true);
  const [error, setError] = React.useState<Error | null>(null);

  // Load exercise data using DataContext
  React.useEffect(() => {
    const loadExerciseData = async () => {
      if (!props.exerciseName) return;
      
      setIsLoading(true);
      setError(null);
      
      try {
        // Load exercise data from DataContext
        const exerciseData = await getExercise(props.exerciseName);
        if (!exerciseData) {
          throw new Error('Exercise not found');
        }
        setExercise(exerciseData);

        // Load exercise muscles using DataContext
        const musclesData = await getExerciseMuscles(props.exerciseName);
        setExerciseMuscles(musclesData);

        // Load individual muscle details using DataContext
        const muscleDetails = await Promise.all(
          musclesData.map(muscle => getMuscle(muscle.muscle_name))
        );
        setMuscles(muscleDetails.filter(Boolean) as Muscle[]);

        // Load exercise equipment using DataContext
        const equipmentData = await getExerciseEquipmentData(props.exerciseName);
        if (equipmentData) {
          setExerciseEquipment(equipmentData);
          
          // Load individual equipment details using DataContext
          const equipmentDetails = await Promise.all(
            equipmentData.map(eq => getEquipment(eq.equipment_name))
          );
          setEquipment(equipmentDetails.filter(Boolean) as Equipment[]);
        }
      } catch (err) {
        setError(err instanceof Error ? err : new Error('Failed to load exercise data'));
      } finally {
        setIsLoading(false);
      }
    };

    loadExerciseData();
  }, [props.exerciseName, getExercise, getExerciseEquipmentData, getMuscle, getEquipment, getExerciseMuscles]);

  const editor = React.useMemo(() => withReact(createEditor()), []);

  if (isLoading || !props.exerciseName || !exercise) {
    return <LoadingSpinner message="Loading exercise details..." fullHeight={true} />;
  } else if (error) {
    // Check if it's a network error or authentication error
    const isNetworkError =
      error?.message?.includes('Network Error') ||
      error?.message?.includes('timeout') ||
      error?.message?.includes('NS_BINDING_ABORTED');

    if (isNetworkError) {
      return (
        <Alert severity="warning">
          <AlertTitle>Connection Error</AlertTitle>
          <Typography>
            Unable to connect to the server. Please check your internet connection and try again.
          </Typography>
          <Typography variant="body2" sx={{ mt: 1 }}>
            Error: {error?.message || 'Network error'}
          </Typography>
        </Alert>
      );
    } else {
      return (
        <Alert severity="error">
          <AlertTitle>Exercise Not Found</AlertTitle>
          <Typography>The specified exercise could not be found.</Typography>
          {error && (
            <Typography variant="body2" sx={{ mt: 1 }}>
              {error.toString()}
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
