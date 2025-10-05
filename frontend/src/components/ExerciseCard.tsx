import Box from '@mui/material/Box';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Chip from '@mui/material/Chip';
import Divider from '@mui/material/Divider';
import Stack from '@mui/material/Stack';
import { alpha } from '@mui/material/styles';
import * as React from 'react';
import { Link } from 'react-router';

import { encodeExerciseName } from '../api/endpoint';

import { BinaryTag } from './BinaryTag';
import { ExercisePreferenceControls } from './ExercisePreferenceControls';
import { GameCard, GameText } from './GameTheme';
import type { Exercise } from '../api/types';
import { capitalizeEachWord } from '../common/utils';

import '../styles/UndecoratedLink.css';

/**
 * Props for the ExerciseCard component.
 */
export interface ExerciseCardProps {
  exercise: Exercise;
  equipment?: string[];
  muscles?: string[];
} // end interface ExerciseCardProps

/**
 * Construct a component for showing an exercise's high-level data in a card.
 *
 * @param props The props for the component.
 *
 * @return The exercise card component.
 */
export function ExerciseCard(props: ExerciseCardProps): React.ReactElement<ExerciseCardProps> {
  const { exercise, equipment = [], muscles = [] } = props;

  return (
    <GameCard
      interactive={true}
      sx={{ height: '100%' }}
    >
      <CardContent sx={{ p: 3, height: '100%', display: 'flex', flexDirection: 'column' }}>
        <Link
          className="undecoratedLink"
          to={`/exercises/${encodeExerciseName(exercise.name)}`}
          style={{ textDecoration: 'none' }}
        >
          <GameText
            variant="h6"
            sx={{
              fontWeight: 600,
              mb: 2,
              color: 'text.primary',
              lineHeight: 1.3,
              '&:hover': {
                color: 'primary.main',
              },
            }}
          >
            {exercise.name}
          </GameText>
        </Link>

        <Stack spacing={2} sx={{ flex: 1 }}>
          {/* Exercise Type and Preference Controls */}
          <Box display="flex" justifyContent="space-between" alignItems="flex-start" gap={1}>
            <Chip
              label={`${capitalizeEachWord(exercise.movement_type)} Exercise`}
              color="primary"
              size="small"
              sx={{
                fontWeight: 600,
                borderRadius: 2,
              }}
            />
            <ExercisePreferenceControls exerciseName={exercise.name} variant="icon" size="small" />
          </Box>

          {/* Exercise Properties */}
          <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
            <BinaryTag
              isOn={exercise.is_upper}
              onText="Upper Body"
              offText="Lower Body"
              color="success"
            />
            <BinaryTag
              isOn={exercise.is_accessory}
              onText="Accessory"
              offText="Primary"
              color="secondary"
            />
            <BinaryTag
              isOn={exercise.is_unilateral}
              onText="Unilateral"
              offText="Bilateral"
              color="primary"
            />
          </Stack>

          {/* Equipment Section */}
          {equipment.length > 0 && (
            <Box>
              <Divider
                textAlign="left"
                sx={{
                  mb: 1.5,
                  '&::before': {
                    borderTop: theme => `1px solid ${alpha(theme.palette.divider, 0.5)}`,
                  },
                  '&::after': {
                    borderTop: theme => `1px solid ${alpha(theme.palette.divider, 0.5)}`,
                  },
                }}
              >
                <GameText
                  variant="caption"
                  textVariant="secondary"
                  sx={{
                    fontWeight: 600,
                    textTransform: 'uppercase',
                    letterSpacing: '0.5px',
                  }}
                >
                  Equipment
                </GameText>
              </Divider>
              <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                {equipment.map(e => (
                  <Chip
                    label={capitalizeEachWord(e)}
                    key={e}
                    size="small"
                    variant="outlined"
                    sx={{
                      borderRadius: 2,
                      fontSize: '0.75rem',
                    }}
                  />
                ))}
              </Stack>
            </Box>
          )}

          {/* Muscles Section */}
          {muscles.length > 0 && (
            <Box>
              <Divider
                textAlign="left"
                sx={{
                  mb: 1.5,
                  '&::before': {
                    borderTop: theme => `1px solid ${alpha(theme.palette.divider, 0.5)}`,
                  },
                  '&::after': {
                    borderTop: theme => `1px solid ${alpha(theme.palette.divider, 0.5)}`,
                  },
                }}
              >
                <GameText
                  variant="caption"
                  textVariant="secondary"
                  sx={{
                    fontWeight: 600,
                    textTransform: 'uppercase',
                    letterSpacing: '0.5px',
                  }}
                >
                  Target Muscles
                </GameText>
              </Divider>
              <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                {muscles.map(m => (
                  <Chip
                    label={capitalizeEachWord(m)}
                    key={m}
                    size="small"
                    color="secondary"
                    sx={{
                      borderRadius: 2,
                      fontSize: '0.75rem',
                    }}
                  />
                ))}
              </Stack>
            </Box>
          )}
        </Stack>
      </CardContent>
    </GameCard>
  );
} // end component ExerciseCard
