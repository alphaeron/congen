import Box from '@mui/material/Box';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Chip from '@mui/material/Chip';
import Divider from '@mui/material/Divider';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import * as React from 'react';
import { Link } from 'react-router-dom';

import { Exercise } from '../api/types';
import { capitalizeEachWord } from '../common/utils';
import { BinaryTag } from './BinaryTag';

import '../styles/UndecoratedLink.css';

/**
 * Props for the ExerciseCard component.
 */
export interface ExerciseCardProps {
  exercise: Exercise;
  equipment: string[];
  muscles: string[];
} // end interface ExerciseCardProps

/**
 * Construct a component for showing an exercise's high-level data in a card.
 *
 * @param props The props for the component.
 *
 * @return The exercise card component.
 */
export function ExerciseCard(props: ExerciseCardProps): React.ReactElement<ExerciseCardProps> {
  return (
    <Box sx={{ minWidth: 275 }}>
      <Card variant="outlined">
        <CardContent>
          <Link className="undecoratedLink" to={`/exercises/${props.exercise.name}`}>
            <Typography color="text.secondary">{props.exercise.name}</Typography>
          </Link>
          <Stack direction="row" spacing={2} sx={{ marginBottom: '12px' }}>
            <Chip label={`${capitalizeEachWord(props.exercise.movement_type)}Exercise`} />
            <BinaryTag isOn={props.exercise.is_upper} onText="Upper Body" offText="Lower Body" />
            <BinaryTag
              isOn={props.exercise.is_accessory}
              onText="Accessory"
              offText="Primary Movement"
            />
            <BinaryTag isOn={props.exercise.is_unilateral} onText="Unilateral" offText="Bilateral" />
          </Stack>
          {props.equipment.length > 0 && (
            <React.Fragment>
              <Divider textAlign="left" sx={{ marginBottom: '12px' }}>
                <Typography color="text.secondary">Equipment</Typography>
              </Divider>
              <Stack direction="row" spacing={2} sx={{ marginBottom: '12px' }}>
                {props.equipment.map(e => (
                  <Chip label={`${capitalizeEachWord(e)}`} key={e} />
                ))}
              </Stack>
            </React.Fragment>
          )}
          {props.muscles.length > 0 && (
            <React.Fragment>
              <Divider textAlign="left" sx={{ marginBottom: '12px' }}>
                <Typography color="text.secondary">Muscles</Typography>
              </Divider>
              <Stack direction="row" spacing={2} sx={{ marginBottom: '12px' }}>
                {props.muscles.map(e => (
                  <Chip label={`${capitalizeEachWord(e)}`} key={e} />
                ))}
              </Stack>
            </React.Fragment>
          )}
        </CardContent>
      </Card>
    </Box>
  );
} // end component ExerciseCard
