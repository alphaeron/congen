import AutoFixHighRoundedIcon from '@mui/icons-material/AutoFixHighRounded';
import ConstructionRoundedIcon from '@mui/icons-material/ConstructionRounded';
import QueryStatsRoundedIcon from '@mui/icons-material/QueryStatsRounded';
import PsychologyRoundedIcon from '@mui/icons-material/PsychologyRounded';
import SportsEsportsRoundedIcon from '@mui/icons-material/SportsEsportsRounded';
import TrendingUpRoundedIcon from '@mui/icons-material/TrendingUpRounded';
import Box from '@mui/material/Box';
import Card from '@mui/material/Card';
import Container from '@mui/material/Container';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import { alpha } from '@mui/material/styles';
import * as React from 'react';

import { GameText, GameCard, GAME_CLASSES } from './GameTheme';

export const FEATURE_ITEMS = [
  {
    icon: <ConstructionRoundedIcon />,
    title: 'Algorithmic Workout Generation',
    description:
      'Advanced algorithms automatically generate personalized conjugate method workouts based on your equipment, goals, and performance data.',
    color: 'primary',
  },
  {
    icon: <AutoFixHighRoundedIcon />,
    title: 'Intelligent Exercise Rotation',
    description:
      'Prevents accommodation and plateaus by automatically rotating exercises every 1-3 weeks using scientific periodization principles.',
    color: 'secondary',
  },
  {
    icon: <SportsEsportsRoundedIcon />,
    title: 'RPG-Style Gamification',
    description:
      'Level up your character, unlock skills, and progress through ranks with our comprehensive fitness RPG system featuring HP/MP/Fatigue mechanics.',
    color: 'success',
  },
  {
    icon: <PsychologyRoundedIcon />,
    title: 'Performance Analytics',
    description:
      'Track 6 core performance domains (Strength, Power, Endurance, Recovery, Stamina, Speed) with detailed analytics and insights.',
    color: 'warning',
  },
  {
    icon: <TrendingUpRoundedIcon />,
    title: 'Adaptive Programming',
    description:
      'Your program evolves with you - automatic weight progression, exercise substitutions, and program adjustments based on your performance.',
    color: 'info',
  },
  {
    icon: <QueryStatsRoundedIcon />,
    title: 'Comprehensive Tracking',
    description:
      'Monitor everything from one-rep maxes to recovery metrics, with detailed workout history and progress visualization.',
    color: 'error',
  },
];

export function Features() {
  return (
    <Container
      id="features"
      sx={{
        position: 'relative',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: { xs: 4, sm: 8 },
        py: { xs: 8, sm: 12 },
      }}
    >
      <Box
        sx={{
          width: { sm: '100%', md: '70%' },
          textAlign: 'center',
          mb: 3,
        }}
      >
        <GameText
          variant="h3"
          textVariant="glow"
          sx={{
            fontWeight: 700,
            mb: 2,
          }}
        >
          Features
        </GameText>
        <GameText
          variant="h6"
          textVariant="secondary"
          sx={{
            fontWeight: 400,
            opacity: 0.8,
          }}
        >
          Everything you need to build the perfect conjugate workout program
        </GameText>
      </Box>

      <Grid container spacing={4}>
        {FEATURE_ITEMS.map((item, index) => (
          <Grid size={{ xs: 12, sm: 6, md: 4 }} key={index}>
            <GameCard
              interactive={true}
              sx={{
                p: 4,
                height: '100%',
              }}
            >
              <Stack spacing={3} sx={{ height: '100%' }}>
                <Box
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    width: 64,
                    height: 64,
                    borderRadius: 2,
                    background: theme => {
                      const colorValue = item.color === 'primary' ? theme.palette.primary.main :
                                       item.color === 'secondary' ? theme.palette.secondary.main :
                                       item.color === 'success' ? '#22c55e' :
                                       item.color === 'warning' ? '#f59e0b' :
                                       item.color === 'info' ? '#3b82f6' :
                                       item.color === 'error' ? '#ef4444' :
                                       theme.palette.primary.main;
                      return `linear-gradient(135deg, ${alpha(colorValue, 0.1)}, ${alpha(colorValue, 0.05)})`;
                    },
                    border: theme => {
                      const colorValue = item.color === 'primary' ? theme.palette.primary.main :
                                       item.color === 'secondary' ? theme.palette.secondary.main :
                                       item.color === 'success' ? '#22c55e' :
                                       item.color === 'warning' ? '#f59e0b' :
                                       item.color === 'info' ? '#3b82f6' :
                                       item.color === 'error' ? '#ef4444' :
                                       theme.palette.primary.main;
                      return `1px solid ${alpha(colorValue, 0.2)}`;
                    },
                    color: theme => {
                      const colorValue = item.color === 'primary' ? theme.palette.primary.main :
                                       item.color === 'secondary' ? theme.palette.secondary.main :
                                       item.color === 'success' ? '#22c55e' :
                                       item.color === 'warning' ? '#f59e0b' :
                                       item.color === 'info' ? '#3b82f6' :
                                       item.color === 'error' ? '#ef4444' :
                                       theme.palette.primary.main;
                      return colorValue;
                    },
                    fontSize: '2rem',
                    transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                    '&:hover': {
                      transform: 'scale(1.1)',
                      background: theme => {
                        const colorValue = item.color === 'primary' ? theme.palette.primary.main :
                                         item.color === 'secondary' ? theme.palette.secondary.main :
                                         item.color === 'success' ? '#22c55e' :
                                         item.color === 'warning' ? '#f59e0b' :
                                         item.color === 'info' ? '#3b82f6' :
                                         item.color === 'error' ? '#ef4444' :
                                         theme.palette.primary.main;
                        return `linear-gradient(135deg, ${alpha(colorValue, 0.2)}, ${alpha(colorValue, 0.1)})`;
                      },
                    },
                  }}
                >
                  {React.cloneElement(item.icon, {
                    sx: { fontSize: '2rem' },
                  })}
                </Box>

                <Box sx={{ flex: 1 }}>
                  <GameText
                    variant="h5"
                    sx={{
                      fontWeight: 600,
                      mb: 2,
                      lineHeight: 1.3,
                    }}
                    gutterBottom
                  >
                    {item.title}
                  </GameText>
                  <GameText
                    variant="body1"
                    textVariant="secondary"
                    sx={{
                      lineHeight: 1.6,
                      opacity: 0.8,
                    }}
                  >
                    {item.description}
                  </GameText>
                </Box>
              </Stack>
            </GameCard>
          </Grid>
        ))}
      </Grid>
    </Container>
  );
}
