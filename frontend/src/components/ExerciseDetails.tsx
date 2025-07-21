import Alert from "@mui/material/Alert";
import AlertTitle from "@mui/material/AlertTitle";
import Chip from "@mui/material/Chip";
import Divider from "@mui/material/Divider";
import Skeleton from "@mui/material/Skeleton";
import Stack from "@mui/material/Stack";
import Tooltip from "@mui/material/Tooltip";
import Typography from "@mui/material/Typography";
import Grid from "@mui/material/Grid2";
import * as React from "react";
import { createEditor } from "slate";
import { Slate, Editable, withReact } from "slate-react";

import {
  getIndividualExercise,
  getExerciseMuscles,
  getExerciseEquipment,
} from "../api/exercise";
import { getIndividualMuscle } from "../api/muscle";
import { getIndividualEquipment } from "../api/equipment";
import { useApiGet } from "../api/hooks";
import {
  Exercise,
  ExerciseEquipment,
  ExerciseMuscle,
  Equipment,
  Muscle,
} from "../api/types";
import { capitalizeEachWord } from "../common/utils";
import { BinaryTag } from "./BinaryTag";

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
  props: ExerciseDetailsProps,
): React.ReactElement<ExerciseDetailsProps> {
  const {
    data: exercise,
    isLoading: isExerciseLoading,
    error: exerciseError,
    isError: isExerciseError,
  } = useApiGet<Exercise>(
    [`individualExercise${props.exerciseName}`],
    getIndividualExercise,
    {
      enabled: true,
      refetchOnWindowFocus: true,
      retry: 1,
    },
    [props.exerciseName],
  );

  const {
    data: exerciseMuscles,
    isLoading: isExerciseMuscleLoading,
    error: exerciseMuscleError,
    isError: isExerciseMuscleError,
  } = useApiGet<ExerciseMuscle[]>(
    [`exerciseMuscle${props.exerciseName}`],
    getExerciseMuscles,
    {
      enabled: true,
      refetchOnWindowFocus: true,
      retry: 1,
    },
    [props.exerciseName],
  );

  const {
    data: muscles,
    isLoading: isMusclesLoading,
    error: musclesError,
    isError: isMusclesError,
  } = useApiGet<Muscle[]>(
    [`muscles${props.exerciseName}`, exerciseMuscles],
    async (): Promise<Muscle[]> =>
      Promise.all(
        exerciseMuscles.map((element) =>
          getIndividualMuscle(element.muscleName),
        ),
      ),
    {
      enabled: true,
      refetchOnWindowFocus: true,
      retry: 1,
      enabled: exerciseMuscles && exerciseMuscles.length > 0,
    },
  );

  const {
    data: exerciseEquipment,
    isLoading: isExerciseEquipmentLoading,
    error: exerciseEquipmentError,
    isError: isExerciseEquipmentError,
  } = useApiGet<ExerciseEquipment[]>(
    [`exerciseEquipment${props.exerciseName}`],
    getExerciseEquipment,
    {
      enabled: true,
      refetchOnWindowFocus: true,
      retry: 1,
    },
    [props.exerciseName],
  );

  const {
    data: equipment,
    isLoading: isEquipmentLoading,
    error: equipmentError,
    isError: isEquipmentError,
  } = useApiGet<Equipment[]>(
    [`equipment${props.exerciseName}`, exerciseEquipment],
    async (): Promise<Equipment[]> =>
      Promise.all(
        exerciseEquipment.map((element) =>
          getIndividualEquipment(element.equipmentName),
        ),
      ),
    {
      enabled: true,
      refetchOnWindowFocus: true,
      retry: 1,
      enabled: exerciseEquipment && exerciseEquipment.length > 0,
    },
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
    return <React.Fragment />;
  } else if (
    isExerciseError ||
    isEquipmentError ||
    isMusclesError ||
    isExerciseMuscleError ||
    isExerciseEquipmentError
  ) {
    return (
      <Alert severity="error">
        <AlertTitle>Exercise Not Found</AlertTitle>
        The specified exercise could not be found.
        {exerciseError && <Typography>{exerciseError.toString()}</Typography>}
        {equipmentError && <Typography>{equipmentError.toString()}</Typography>}
        {musclesError && <Typography>{musclesError.toString()}</Typography>}
        {exerciseMuscleError && (
          <Typography>{exerciseMuscleError.toString()}</Typography>
        )}
        {exerciseEquipmentError && (
          <Typography>{exerciseEquipmentError.toString()}</Typography>
        )}
      </Alert>
    );
  } else {
    return (
      <React.Fragment>
        <Typography variant="h1" gutterBottom={true}>
          {exercise.name}
        </Typography>
        <Grid container={true} spacing={2}>
          <Grid size={12}>
            <Stack direction="row" spacing={2}>
              <Chip
                label={`${capitalizeEachWord(exercise.movementType)} Exercise`}
              />
              <BinaryTag
                isOn={exercise.isUpper}
                onText="Upper Body"
                offText="Lower Body"
              />
              <BinaryTag
                isOn={exercise.isAccessory}
                onText="Accessory"
                offText="Primary Movement"
              />
              <BinaryTag
                isOn={exercise.isUnilateral}
                onText="Unilateral"
                offText="Bilateral"
              />
            </Stack>
          </Grid>
          <Grid size={9}>
            <Stack spacing={2}>
              <Skeleton variant="rectangular" height={360} />
              <Slate
                editor={editor}
                initialValue={[
                  {
                    type: "paragraph",
                    children: [
                      {
                        text: exercise.description,
                      },
                    ],
                  },
                ]}
              >
                <Editable
                  readOnly={true}
                  placeholder="No description provided."
                />
              </Slate>
            </Stack>
          </Grid>
          <Grid size={3}>
            <Grid container={true} spacing={2}>
              <Grid size={12}>
                <Divider textAlign="left" sx={{ marginBottom: "16px" }}>
                  <Typography variant="h3">Muscles Worked</Typography>
                </Divider>
                <Grid container={true} spacing={2}>
                  {exerciseMuscles.map((em) => {
                    const muscle = muscles.find(
                      (elem) => elem.name === em.muscleName,
                    );
                    return (
                      <Grid size={12} key={em.muscleName}>
                        <Tooltip arrow={true} title={muscle?.description}>
                          <Chip
                            label={`${capitalizeEachWord(em.muscleName)}`}
                          />
                        </Tooltip>
                      </Grid>
                    );
                  })}
                </Grid>
              </Grid>
              <Grid size={12}>
                <Divider textAlign="left" sx={{ marginBottom: "16px" }}>
                  <Typography variant="h3">Equipment Needed</Typography>
                </Divider>
                <Grid container={true} spacing={2}>
                  {exerciseEquipment.map((ee) => {
                    const equip = equipment.find(
                      (elem) => elem.name === ee.equipmentName,
                    );
                    return (
                      <Grid size={12} key={ee.equipmentName}>
                        <Tooltip arrow={true} title={equip?.description}>
                          <Chip
                            label={`${capitalizeEachWord(ee.equipmentName)}`}
                          />
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
