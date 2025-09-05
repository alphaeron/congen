import AutoFixHighRoundedIcon from '@mui/icons-material/AutoFixHighRounded';
import ConstructionRoundedIcon from '@mui/icons-material/ConstructionRounded';
import QueryStatsRoundedIcon from '@mui/icons-material/QueryStatsRounded';
import Box from '@mui/material/Box';
import Card from '@mui/material/Card';
import Container from '@mui/material/Container';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import { alpha } from '@mui/material/styles';
import Typography from '@mui/material/Typography';
import * as React from 'react';

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
        <Typography
          component="h2"
          variant="h3"
          sx={{
            fontWeight: 700,
            mb: 2,
            background: 'linear-gradient(135deg, #0ea5e9, #f97316)',
            backgroundClip: 'text',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
          }}
        >
          Features
        </Typography>
        <Typography
          variant="h6"
          color="text.secondary"
          sx={{
            fontWeight: 400,
            opacity: 0.8,
          }}
        >
          Everything you need to build the perfect conjugate workout program
        </Typography>
      </Box>

      <Grid container spacing={4}>
        {FEATURE_ITEMS.map((item, index) => (
          <Grid size={{ xs: 12, sm: 6, md: 4 }} key={index}>
            <Card
              sx={{
                p: 4,
                height: '100%',
                borderRadius: 3,
                background: theme =>
                  `linear-gradient(135deg, ${alpha(theme.palette.background.paper, 0.8)}, ${alpha(theme.palette.background.paper, 0.6)})`,
                border: theme => `1px solid ${alpha(theme.palette.divider, 0.3)}`,
                backdropFilter: 'blur(20px)',
                transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                cursor: 'pointer',
                '&:hover': {
                  transform: 'translateY(-8px)',
                  boxShadow: theme => `0 20px 40px ${alpha(theme.palette.primary.main, 0.15)}`,
                  border: theme => `1px solid ${alpha(theme.palette.primary.main, 0.3)}`,
                },
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
                  <Typography
                    variant="h5"
                    fontWeight={600}
                    gutterBottom
                    sx={{
                      mb: 2,
                      lineHeight: 1.3,
                    }}
                  >
                    {item.title}
                  </Typography>
                  <Typography
                    variant="body1"
                    color="text.secondary"
                    sx={{
                      lineHeight: 1.6,
                      opacity: 0.8,
                    }}
                  >
                    {item.description}
                  </Typography>
                </Box>
              </Stack>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Container>
  );
}
