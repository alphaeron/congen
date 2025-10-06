import AutoFixHighRoundedIcon from '@mui/icons-material/AutoFixHighRounded';
import ConstructionRoundedIcon from '@mui/icons-material/ConstructionRounded';
import SportsEsportsRoundedIcon from '@mui/icons-material/SportsEsportsRounded';
import Box from '@mui/material/Box';
import Container from '@mui/material/Container';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import { alpha } from '@mui/material/styles';
import { motion, useInView } from 'framer-motion';
import * as React from 'react';

import { GameText, GameCard } from './GameTheme';

export const FEATURE_ITEMS = [
  {
    icon: <ConstructionRoundedIcon />,
    title: 'Algorithmic Workout Generation',
    description:
      'Advanced algorithms automatically generate personalized conjugate method workouts based on your equipment, goals, and performance data.',
    color: 'primary',
  },
  {
    icon: <SportsEsportsRoundedIcon />,
    title: 'RPG-Style Gamification',
    description:
      'Level up your character, unlock skills, and progress through ranks with our comprehensive fitness RPG system featuring HP/MP/Fatigue mechanics.',
    color: 'success',
  },
  {
    icon: <AutoFixHighRoundedIcon />,
    title: 'Smart Personalization',
    description:
      'Every aspect of your program is algorithmically tailored to your equipment, goals, experience level, and physical attributes.',
    color: 'secondary',
  },
];

// Advanced animation variants with 3D effects
const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.15,
      delayChildren: 0.3,
    },
  },
};

const itemVariants = {
  hidden: {
    y: 50,
    opacity: 0,
    scale: 0.9,
    rotateX: -15,
    transformPerspective: 1000,
  },
  visible: {
    y: 0,
    opacity: 1,
    scale: 1,
    rotateX: 0,
    transition: {
      duration: 0.8,
      ease: [0.25, 0.46, 0.45, 0.94] as const,
    },
  },
};

const iconVariants = {
  hidden: {
    scale: 0,
    rotate: -180,
    y: 20,
    opacity: 0,
  },
  visible: {
    scale: 1,
    rotate: 0,
    y: 0,
    opacity: 1,
    transition: {
      duration: 1.0,
      ease: [0.25, 0.46, 0.45, 0.94] as const,
    },
  },
};

export function Features() {
  const ref = React.useRef(null);
  const isInView = useInView(ref, { once: true, margin: '-100px' });
  return (
    <motion.div>
      <Box
        sx={{
          position: 'relative',
          width: '100vw',
          height: '100vh',
          marginLeft: 'calc(-50vw + 50%)',
          marginTop: 0,
          display: 'flex',
          alignItems: 'center',
          '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: `linear-gradient(90deg, transparent 48%, ${alpha('#0ea5e9', 0.03)} 49%, ${alpha('#0ea5e9', 0.03)} 51%, transparent 52%),
               linear-gradient(0deg, transparent 48%, ${alpha('#f97316', 0.02)} 49%, ${alpha('#f97316', 0.02)} 51%, transparent 52%),
               radial-gradient(circle at 20% 20%, ${alpha('#22c55e', 0.04)} 1px, transparent 1px),
               radial-gradient(circle at 80% 80%, ${alpha('#8b5cf6', 0.03)} 1px, transparent 1px)`,
            backgroundSize: '80px 80px, 80px 80px, 40px 40px, 40px 40px',
            zIndex: 0,
            animation: 'codeFlow 18s linear infinite',
            '@keyframes codeFlow': {
              '0%': { transform: 'translateX(0) translateY(0)' },
              '100%': { transform: 'translateX(80px) translateY(80px)' },
            },
          },
          '&::after': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: `linear-gradient(45deg, transparent 30%, ${alpha('#0ea5e9', 0.05)} 50%, transparent 70%),
               linear-gradient(-45deg, transparent 30%, ${alpha('#f97316', 0.03)} 50%, transparent 70%)`,
            backgroundSize: '200px 200px, 300px 300px',
            opacity: 0.3,
            zIndex: 0,
            animation: 'commitFlow 25s linear infinite',
            '@keyframes commitFlow': {
              '0%': { transform: 'translateX(0) translateY(0)' },
              '100%': { transform: 'translateX(-200px) translateY(-200px)' },
            },
          },
        }}
      >
        <Container
          id="features"
          sx={{
            position: 'relative',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: { xs: 4, sm: 8 },
            pb: { xs: 8, sm: 12 },
            zIndex: 1,
          }}
        >
          <motion.div
            ref={ref}
            variants={containerVariants}
            initial="hidden"
            animate={isInView ? 'visible' : 'hidden'}
            style={{ width: '100%' }}
          >
            <motion.div variants={itemVariants}>
              <Box
                sx={{
                  width: { sm: '100%', md: '70%' },
                  textAlign: 'center',
                  mb: 6,
                  mx: 'auto',
                }}
              >
                <GameText
                  variant="h3"
                  textVariant="glow"
                  sx={{
                    fontWeight: 700,
                    mb: 2,
                    background: 'linear-gradient(135deg, #0ea5e9, #f97316)',
                    backgroundClip: 'text',
                    WebkitBackgroundClip: 'text',
                    WebkitTextFillColor: 'transparent',
                    mt: 5,
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
                    fontSize: { xs: '1.1rem', sm: '1.25rem' },
                  }}
                >
                  Everything you need to build the perfect conjugate workout program
                </GameText>
              </Box>
            </motion.div>

            <Grid container spacing={4}>
              {FEATURE_ITEMS.map((item, index) => (
                <Grid size={{ xs: 12, md: 4 }} key={index}>
                  <motion.div
                    variants={itemVariants}
                    initial="rest"
                    whileHover="hover"
                    style={{
                      height: '100%',
                      transformStyle: 'preserve-3d',
                      perspective: 1000,
                    }}
                  >
                    <GameCard
                      interactive={true}
                      sx={{
                        p: 4,
                        height: '100%',
                        position: 'relative',
                        overflow: 'hidden',
                        backdropFilter: 'blur(20px)',
                        background: 'rgba(255, 255, 255, 0.05)',
                        border: theme => `1px solid ${alpha(theme.palette.primary.main, 0.1)}`,
                        boxShadow: theme => `0 8px 32px ${alpha(theme.palette.primary.main, 0.1)}`,
                        '&::before': {
                          content: '""',
                          position: 'absolute',
                          top: 0,
                          left: 0,
                          right: 0,
                          bottom: 0,
                          background: theme => {
                            const colorValue =
                              item.color === 'primary'
                                ? theme.palette.primary.main
                                : item.color === 'secondary'
                                  ? theme.palette.secondary.main
                                  : item.color === 'success'
                                    ? '#22c55e'
                                    : item.color === 'warning'
                                      ? '#f59e0b'
                                      : item.color === 'info'
                                        ? '#3b82f6'
                                        : item.color === 'error'
                                          ? '#ef4444'
                                          : theme.palette.primary.main;
                            return `linear-gradient(135deg, ${alpha(colorValue, 0.08)}, transparent)`;
                          },
                          opacity: 0,
                          transition: 'opacity 0.3s ease',
                        },
                        '&::after': {
                          content: '""',
                          position: 'absolute',
                          top: 0,
                          left: 0,
                          right: 0,
                          bottom: 0,
                          background:
                            'linear-gradient(45deg, transparent 30%, rgba(255,255,255,0.1) 50%, transparent 70%)',
                          opacity: 0,
                          transition: 'opacity 0.3s ease',
                        },
                        '&:hover': {
                          boxShadow: theme =>
                            `0 20px 40px ${alpha(theme.palette.primary.main, 0.2)}`,
                          border: theme => `1px solid ${alpha(theme.palette.primary.main, 0.3)}`,
                          '&::before': {
                            opacity: 1,
                          },
                          '&::after': {
                            opacity: 1,
                          },
                        },
                      }}
                    >
                      <Stack spacing={3} sx={{ height: '100%', position: 'relative', zIndex: 1 }}>
                        <motion.div
                          variants={iconVariants}
                          whileHover={{
                            scale: 1.1,
                            rotate: 5,
                            transition: { duration: 0.2 },
                          }}
                        >
                          <Box
                            sx={{
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'center',
                              width: 80,
                              height: 80,
                              borderRadius: 3,
                              background: theme => {
                                const colorValue =
                                  item.color === 'primary'
                                    ? theme.palette.primary.main
                                    : item.color === 'secondary'
                                      ? theme.palette.secondary.main
                                      : item.color === 'success'
                                        ? '#22c55e'
                                        : item.color === 'warning'
                                          ? '#f59e0b'
                                          : item.color === 'info'
                                            ? '#3b82f6'
                                            : item.color === 'error'
                                              ? '#ef4444'
                                              : theme.palette.primary.main;
                                return `linear-gradient(135deg, ${alpha(colorValue, 0.1)}, ${alpha(colorValue, 0.05)})`;
                              },
                              border: theme => {
                                const colorValue =
                                  item.color === 'primary'
                                    ? theme.palette.primary.main
                                    : item.color === 'secondary'
                                      ? theme.palette.secondary.main
                                      : item.color === 'success'
                                        ? '#22c55e'
                                        : item.color === 'warning'
                                          ? '#f59e0b'
                                          : item.color === 'info'
                                            ? '#3b82f6'
                                            : item.color === 'error'
                                              ? '#ef4444'
                                              : theme.palette.primary.main;
                                return `2px solid ${alpha(colorValue, 0.2)}`;
                              },
                              color: theme => {
                                const colorValue =
                                  item.color === 'primary'
                                    ? theme.palette.primary.main
                                    : item.color === 'secondary'
                                      ? theme.palette.secondary.main
                                      : item.color === 'success'
                                        ? '#22c55e'
                                        : item.color === 'warning'
                                          ? '#f59e0b'
                                          : item.color === 'info'
                                            ? '#3b82f6'
                                            : item.color === 'error'
                                              ? '#ef4444'
                                              : theme.palette.primary.main;
                                return colorValue;
                              },
                              fontSize: '2.5rem',
                              transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                              boxShadow: theme => {
                                const colorValue =
                                  item.color === 'primary'
                                    ? theme.palette.primary.main
                                    : item.color === 'secondary'
                                      ? theme.palette.secondary.main
                                      : item.color === 'success'
                                        ? '#22c55e'
                                        : item.color === 'warning'
                                          ? '#f59e0b'
                                          : item.color === 'info'
                                            ? '#3b82f6'
                                            : item.color === 'error'
                                              ? '#ef4444'
                                              : theme.palette.primary.main;
                                return `0 8px 25px ${alpha(colorValue, 0.15)}`;
                              },
                            }}
                          >
                            {React.cloneElement(item.icon, {
                              sx: { fontSize: '2.5rem' },
                            })}
                          </Box>
                        </motion.div>

                        <Box sx={{ flex: 1 }}>
                          <GameText
                            variant="h5"
                            sx={{
                              fontWeight: 600,
                              mb: 2,
                              lineHeight: 1.3,
                              fontSize: { xs: '1.25rem', sm: '1.5rem' },
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
                              fontSize: { xs: '0.95rem', sm: '1rem' },
                            }}
                          >
                            {item.description}
                          </GameText>
                        </Box>
                      </Stack>
                    </GameCard>
                  </motion.div>
                </Grid>
              ))}
            </Grid>
          </motion.div>
        </Container>
      </Box>
    </motion.div>
  );
}
