import AutoFixHighRoundedIcon from '@mui/icons-material/AutoFixHighRounded';
import ConstructionRoundedIcon from '@mui/icons-material/ConstructionRounded';
import QueryStatsRoundedIcon from '@mui/icons-material/QueryStatsRounded';
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
    title: 'Programming without the hassle',
    description:
      'ConGen builds your workout, selecting exercises targeting the specific muscles you give it.',
    color: 'primary',
  },
  {
    icon: <AutoFixHighRoundedIcon />,
    title: 'Automatically cycle your exercises',
    description:
      'ConGen alters your exercises automatically, ensuring you progress and preventing staleness.',
    color: 'secondary',
  },
  {
    icon: <QueryStatsRoundedIcon />,
    title: 'View progression over time',
    description:
      'View your previous workouts and track progress to make sure you are achieving your goals.',
    color: 'success',
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
          component="h2"
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
                    background: theme =>
                      `linear-gradient(135deg, ${alpha(theme.palette[item.color as keyof typeof theme.palette]?.main || theme.palette.primary.main, 0.1)}, ${alpha(theme.palette[item.color as keyof typeof theme.palette]?.main || theme.palette.primary.main, 0.05)})`,
                    border: theme =>
                      `1px solid ${alpha(theme.palette[item.color as keyof typeof theme.palette]?.main || theme.palette.primary.main, 0.2)}`,
                    color: theme =>
                      theme.palette[item.color as keyof typeof theme.palette]?.main ||
                      theme.palette.primary.main,
                    fontSize: '2rem',
                    transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                    '&:hover': {
                      transform: 'scale(1.1)',
                      background: theme =>
                        `linear-gradient(135deg, ${alpha(theme.palette[item.color as keyof typeof theme.palette]?.main || theme.palette.primary.main, 0.2)}, ${alpha(theme.palette[item.color as keyof typeof theme.palette]?.main || theme.palette.primary.main, 0.1)})`,
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
