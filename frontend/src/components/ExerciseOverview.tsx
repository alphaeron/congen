import Alert from "@mui/material/Alert";
import AlertTitle from "@mui/material/AlertTitle";
import Autocomplete from "@mui/material/Autocomplete";
import Container from "@mui/material/Container";
import Divider from "@mui/material/Divider";
import FormLabel from "@mui/material/FormLabel";
import FormControl from "@mui/material/FormControl";
import FormControlLabel from "@mui/material/FormControlLabel";
import Grid from "@mui/material/Grid2";
import Radio from "@mui/material/Radio";
import RadioGroup from "@mui/material/RadioGroup";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import * as React from "react";

import { getEquipment } from "../api/equipment";
import { getExercises } from "../api/exercise";
import { getExerciseEquipment } from "../api/exerciseEquipment";
import { getExerciseMuscle } from "../api/exerciseMuscle";
import { useApiGet } from "../api/hooks";
import { getMuscles } from "../api/muscle";
import { Equipment, Exercise, Muscle } from "../api/types";
import { ExerciseCard } from "./ExerciseCard";

import "../styles/Form.css";

/**
 * Shows an overview of all available exercises.
 *
 * @return The exercise overview component.
 */
export function ExerciseOverview(): React.ReactElement {
  const [movementTypes, setMovementTypes] = React.useState<string[]>([]);

  const [movementTypeFilter, setMovementTypeFilter] = React.useState<
    string | null
  >(null);
  const [exerciseEquipmentFilter, setExerciseEquipmentFilter] = React.useState<
    string | null
  >(null);
  const [exerciseMuscleFilter, setExerciseMuscleFilter] = React.useState<
    string | null
  >(null);

  const [isUnilateralFilter, setIsUnilateralFilter] =
    React.useState<string>("Both");
  const [isAccessoryFilter, setIsAccessoryFilter] =
    React.useState<string>("Both");
  const [isUpperFilter, setIsUpperFilter] = React.useState<string>("Both");

  const [exercisesToDisplay, setExercisesToDisplay] = React.useState<
    Exercise[]
  >([]);

  const {
    data: exercises,
    isLoading: isExercisesLoading,
    error: exercisesError,
    isError: isExercisesError,
  } = useApiGet<Exercise[]>(["exercises"], getExercises, {
    enabled: true,
    refetchOnWindowFocus: true,
    retry: 1,
  });

  const {
    data: equipment,
    isLoading: isEquipmentLoading,
    error: equipmentError,
    isError: isEquipmentError,
  } = useApiGet<Equipment[]>(["equipment"], getEquipment, {
    enabled: true,
    refetchOnWindowFocus: true,
    retry: 1,
  });

  const {
    data: muscles,
    isLoading: isMusclesLoading,
    error: musclesError,
    isError: isMusclesError,
  } = useApiGet<Muscle[]>(["muscles"], getMuscles, {
    enabled: true,
    refetchOnWindowFocus: true,
    retry: 1,
  });

  const getExerciseEquipmentMap = async (): Map<string, Set<string>> => {
    const res = await getExerciseEquipment();
    const mapping = new Map<string, string>();
    const exerciseNames = Array.from(new Set(res.map((e) => e.exerciseName)));
    for (const exerciseName of exerciseNames) {
      mapping.set(
        exerciseName,
        new Set(
          res
            .filter((e) => e.exerciseName == exerciseName)
            .map((e) => e.equipmentName),
        ),
      );
    }
    return mapping;
  };

  const {
    data: exerciseEquipmentMap,
    isLoading: isExerciseEquipmentLoading,
    error: exerciseEquipmentError,
    isError: isExerciseEquipmentError,
  } = useApiGet<Map<string, Set<string>>>(
    ["exerciseEquipmentMap"],
    getExerciseEquipmentMap,
    {
      enabled: true,
      refetchOnWindowFocus: true,
      retry: 1,
    },
  );

  const getExerciseMuscleMap = async (): Map<string, Set<string>> => {
    const res = await getExerciseMuscle();
    const mapping = new Map<string, string>();
    const exerciseNames = Array.from(new Set(res.map((e) => e.exerciseName)));
    for (const exerciseName of exerciseNames) {
      mapping.set(
        exerciseName,
        new Set(
          res
            .filter((e) => e.exerciseName == exerciseName)
            .map((e) => e.muscleName),
        ),
      );
    }
    return mapping;
  };

  const {
    data: exerciseMuscleMap,
    isLoading: isExerciseMuscleLoading,
    error: exerciseMuscleError,
    isError: isExerciseMuscleError,
  } = useApiGet<Map<string, Set<string>>>(
    ["exerciseMuscleMap"],
    getExerciseMuscleMap,
    {
      enabled: true,
      refetchOnWindowFocus: true,
      retry: 1,
    },
  );

  React.useEffect(() => {
    if (exercises) {
      setMovementTypes(
        Array.from(new Set(exercises.map((e) => e.movementType))),
      );
    }
  }, [exercises]);

  React.useEffect(() => {
    if (exercises) {
      let toDisplay = exercises;
      if (isUnilateralFilter !== "Both") {
        const isUnilateral = isUnilateralFilter === "Unilateral";
        toDisplay = toDisplay.filter((e) => e.isUnilateral === isUnilateral);
      }
      if (isAccessoryFilter !== "Both") {
        const isAccessory = isAccessoryFilter === "Accessory";
        toDisplay = toDisplay.filter((e) => e.isAccessory === isAccessory);
      }
      if (isUpperFilter !== "Both") {
        const isUpper = isUpperFilter === "Upper";
        toDisplay = toDisplay.filter((e) => e.isUpper === isUpper);
      }
      if (
        movementTypes.find((element) => element === movementTypeFilter) !==
        undefined
      ) {
        toDisplay = toDisplay.filter(
          (e) => e.movementType === movementTypeFilter,
        );
      }

      if (
        equipment &&
        exerciseEquipmentMap &&
        equipment.find(
          (element) => element.name === exerciseEquipmentFilter,
        ) !== undefined
      ) {
        toDisplay = toDisplay.filter((e) =>
          exerciseEquipmentMap.get(e.name).has(exerciseEquipmentFilter),
        );
      }

      if (
        muscles &&
        exerciseMuscleMap &&
        muscles.find((element) => element.name === exerciseMuscleFilter) !==
          undefined
      ) {
        toDisplay = toDisplay.filter((e) =>
          exerciseMuscleMap.get(e.name).has(exerciseMuscleFilter),
        );
      }

      setExercisesToDisplay(toDisplay);
    }
  }, [
    isUnilateralFilter,
    isUpperFilter,
    isAccessoryFilter,
    movementTypeFilter,
    exerciseEquipmentFilter,
    exerciseMuscleFilter,
    exercises,
    muscles,
    equipment,
    exerciseEquipmentMap,
    exerciseMuscleMap,
  ]);

  const handleIsUnilateralFilterChange = (
    event: React.ChangeEvent<HTMLInputElement>,
  ): void => {
    setIsUnilateralFilter((event.target as HTMLInputElement).value);
  };

  const handleIsAccessoryFilterChange = (
    event: React.ChangeEvent<HTMLInputElement>,
  ): void => {
    setIsAccessoryFilter((event.target as HTMLInputElement).value);
  };

  const handleIsUpperFilterChange = (
    event: React.ChangeEvent<HTMLInputElement>,
  ): void => {
    setIsUpperFilter((event.target as HTMLInputElement).value);
  };

  if (
    isExercisesLoading ||
    isEquipmentLoading ||
    isMusclesLoading ||
    isExerciseMuscleLoading ||
    isExerciseEquipmentLoading ||
    exercises === undefined ||
    equipment === undefined ||
    muscles === undefined ||
    exerciseEquipmentMap === undefined ||
    exerciseMuscleMap === undefined
  ) {
    return <React.Fragment />;
  } else if (
    isExercisesError ||
    isEquipmentError ||
    isMusclesError ||
    isExerciseMuscleError ||
    isExerciseEquipmentError
  ) {
    return (
      <Alert severity="error">
        <AlertTitle>Exercises Not Found</AlertTitle>
        <Typography>The exercises could not be loaded.</Typography>
        {exercisesError && <Typography>{exercisesError.toString()}</Typography>}
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
        <Container maxWidth="lg">
          <Grid container={true} spacing={2}>
            <Grid size={4}>
              <Typography variant="h2" gutterBottom={true}>
                Filter By
              </Typography>
              <Autocomplete
                disablePortal={true}
                options={movementTypes}
                sx={{ marginBottom: "16px" }}
                renderInput={(params) => (
                  <TextField {...params} label="Movement Type" />
                )}
                onChange={(event, newInputValue, reason) => {
                  if (reason === "clear") {
                    setMovementTypeFilter(null);
                  } else {
                    setMovementTypeFilter(
                      (event.target as HTMLInputElement).textContent,
                    );
                  }
                }}
              />
              <Autocomplete
                disablePortal={true}
                options={equipment.map((e) => e.name)}
                sx={{ marginBottom: "16px" }}
                renderInput={(params) => (
                  <TextField {...params} label="Equipment" />
                )}
                onChange={(event, newInputValue, reason) => {
                  if (reason === "clear") {
                    setExerciseEquipmentFilter(null);
                  } else {
                    setExerciseEquipmentFilter(
                      (event.target as HTMLInputElement).textContent,
                    );
                  }
                }}
              />
              <Autocomplete
                disablePortal={true}
                options={muscles.map((e) => e.name)}
                sx={{ marginBottom: "16px" }}
                renderInput={(params) => (
                  <TextField {...params} label="Muscle" />
                )}
                onChange={(event, newInputValue, reason) => {
                  if (reason === "clear") {
                    setExerciseMuscleFilter(null);
                  } else {
                    setExerciseMuscleFilter(
                      (event.target as HTMLInputElement).textContent,
                    );
                  }
                }}
              />
              <Stack spacing={2}>
                <FormControl
                  component="fieldset"
                  variant="standard"
                  className="formGutter"
                >
                  <FormLabel
                    component="legend"
                    className="undecoratedFormLabel"
                  >
                    Unilateral/Bilateral Exercises
                  </FormLabel>
                  <RadioGroup
                    aria-labelledby="exercise-property-filters"
                    name="exercisePropertyFilters"
                    value={isUnilateralFilter}
                    onChange={handleIsUnilateralFilterChange}
                  >
                    <FormControlLabel
                      control={<Radio />}
                      label="Unilateral"
                      value="Unilateral"
                    />
                    <FormControlLabel
                      control={<Radio />}
                      label="Bilateral"
                      value="Bilateral"
                    />
                    <FormControlLabel
                      control={<Radio />}
                      label="Both"
                      value="Both"
                    />
                  </RadioGroup>
                </FormControl>
                <FormControl
                  component="fieldset"
                  variant="standard"
                  className="formGutter"
                >
                  <FormLabel
                    component="legend"
                    className="undecoratedFormLabel"
                  >
                    Accessory/Primary Exercise
                  </FormLabel>
                  <RadioGroup
                    aria-labelledby="exercise-property-filters"
                    name="exercisePropertyFilters"
                    value={isAccessoryFilter}
                    onChange={handleIsAccessoryFilterChange}
                  >
                    <FormControlLabel
                      control={<Radio />}
                      label="Accessory"
                      value="Accessory"
                    />
                    <FormControlLabel
                      control={<Radio />}
                      label="Primary"
                      value="Primary"
                    />
                    <FormControlLabel
                      control={<Radio />}
                      label="Both"
                      value="Both"
                    />
                  </RadioGroup>
                </FormControl>
                <FormControl
                  component="fieldset"
                  variant="standard"
                  className="formGutter"
                >
                  <FormLabel
                    component="legend"
                    className="undecoratedFormLabel"
                  >
                    Upper/Lower Body Exercise
                  </FormLabel>
                  <RadioGroup
                    aria-labelledby="exercise-property-filters"
                    name="exercisePropertyFilters"
                    value={isUpperFilter}
                    onChange={handleIsUpperFilterChange}
                  >
                    <FormControlLabel
                      control={<Radio />}
                      label="Upper"
                      value="Upper"
                    />
                    <FormControlLabel
                      control={<Radio />}
                      label="Lower"
                      value="Lower"
                    />
                    <FormControlLabel
                      control={<Radio />}
                      label="Both"
                      value="Both"
                    />
                  </RadioGroup>
                </FormControl>
              </Stack>
            </Grid>
            <Grid justifyContent="center" alignItems="center">
              <Divider
                orientation="vertical"
                style={{ height: "100%", width: "1px" }}
              />
            </Grid>
            <Grid size="grow">
              <Typography
                variant="h1"
                gutterBottom={true}
                data-testid="exerciseHeader"
              >
                Exercises
              </Typography>
              <Grid container={true} spacing={2}>
                {exercisesToDisplay.map((e: Exercise): React.ReactElement => {
                  return (
                    <Grid size={12} key={e.name}>
                      <ExerciseCard
                        exercise={e}
                        equipment={
                          exerciseEquipmentMap.has(e.name)
                            ? Array.from(exerciseEquipmentMap.get(e.name))
                            : []
                        }
                        muscles={
                          exerciseMuscleMap.has(e.name)
                            ? Array.from(exerciseMuscleMap.get(e.name))
                            : []
                        }
                      />
                    </Grid>
                  );
                })}
              </Grid>
            </Grid>
          </Grid>
        </Container>
      </React.Fragment>
    );
  }
} // end component ExerciseOverview
