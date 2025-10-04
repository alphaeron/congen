import Box from '@mui/material/Box';
import Container from '@mui/material/Container';
import Stack from '@mui/material/Stack';
import { alpha } from '@mui/material/styles';
import * as React from 'react';
import { motion, useScroll, useTransform } from 'framer-motion';

import { useTypewriter } from '../hooks/useTypewriter';
import { GameText, GameContainer, GAME_CLASSES } from './GameTheme';

// Advanced animation variants inspired by Framer best practices
const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.15,
      delayChildren: 0.2,
    },
  },
};

const itemVariants = {
  hidden: { y: 30, opacity: 0, scale: 0.95 },
  visible: {
    y: 0,
    opacity: 1,
    scale: 1,
    transition: {
      duration: 0.8,
      ease: [0.25, 0.46, 0.45, 0.94] as const,
    },
  },
};

// Advanced text reveal animation
const textRevealVariants = {
  hidden: { 
    y: 50, 
    opacity: 0,
    rotateX: -15,
    transformPerspective: 1000,
  },
  visible: {
    y: 0,
    opacity: 1,
    rotateX: 0,
    transition: {
      duration: 1.2,
      ease: [0.25, 0.46, 0.45, 0.94] as const,
    },
  },
};

// Magnetic hover effect for interactive elements
const magneticVariants = {
  rest: { scale: 1, rotate: 0 },
  hover: { 
    scale: 1.05, 
    rotate: 2,
    transition: {
      type: "spring" as const,
      stiffness: 300,
      damping: 20,
    },
  },
  tap: { scale: 0.95 },
};

const floatingVariants = {
  animate: {
    y: [-10, 10, -10],
    transition: {
      duration: 6,
      repeat: Infinity,
      ease: "easeInOut" as const,
    },
  },
};

const pulseVariants = {
  animate: {
    scale: [1, 1.05, 1],
    opacity: [0.7, 1, 0.7],
    transition: {
      duration: 4,
      repeat: Infinity,
      ease: "easeInOut" as const,
    },
  },
};

export function Hero() {
  const headline = useTypewriter('Conjugate Method Programming, Without the Hassle', 40);
  const { scrollY } = useScroll();
  const y = useTransform(scrollY, [0, 300], [0, 0]);
  const opacity = useTransform(scrollY, [0, 300], [1, 0.8]);
  

  return (
    <motion.div
      style={{ y, opacity }}
    >
      <Box
        id="hero"
        sx={{
          width: '100%',
          position: 'relative',
          overflow: 'hidden',
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: theme => 
            `linear-gradient(135deg, ${alpha(theme.palette.primary.main, 0.08)} 0%, ${alpha(theme.palette.secondary.main, 0.04)} 50%, ${alpha(theme.palette.primary.main, 0.06)} 100%)`,
          '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: theme =>
              `radial-gradient(circle at 20% 80%, ${alpha(theme.palette.primary.main, 0.12)} 0%, transparent 50%),
               radial-gradient(circle at 80% 20%, ${alpha(theme.palette.secondary.main, 0.08)} 0%, transparent 50%),
               radial-gradient(circle at 50% 50%, ${alpha(theme.palette.primary.main, 0.05)} 0%, transparent 70%)`,
            zIndex: 0,
          },
          '&::after': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: theme =>
              `linear-gradient(45deg, transparent 30%, ${alpha(theme.palette.primary.main, 0.02)} 50%, transparent 70%)`,
            zIndex: 1,
          },
        }}
      >
      {/* Animated background elements */}
      <Box
        sx={{
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          overflow: 'hidden',
          zIndex: 0,
        }}
      >
        
        {/* Animated Circuit Board Pattern */}
        <Box
          sx={{
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundImage: `
              linear-gradient(90deg, rgba(14, 165, 233, 0.1) 1px, transparent 1px),
              linear-gradient(rgba(14, 165, 233, 0.1) 1px, transparent 1px),
              radial-gradient(circle at 25% 25%, rgba(14, 165, 233, 0.15) 2px, transparent 2px),
              radial-gradient(circle at 75% 75%, rgba(249, 115, 22, 0.1) 2px, transparent 2px),
              radial-gradient(circle at 50% 50%, rgba(16, 185, 129, 0.08) 1px, transparent 1px)
            `,
            backgroundSize: '100px 100px, 100px 100px, 50px 50px, 50px 50px, 25px 25px',
            opacity: 0.4,
            animation: 'circuitPulse 15s ease-in-out infinite',
            '@keyframes circuitPulse': {
              '0%': {
                opacity: 0.2,
                transform: 'translateX(0) translateY(0)',
              },
              '25%': {
                opacity: 0.4,
                transform: 'translateX(-10px) translateY(-5px)',
              },
              '50%': {
                opacity: 0.3,
                transform: 'translateX(0) translateY(-10px)',
              },
              '75%': {
                opacity: 0.5,
                transform: 'translateX(10px) translateY(-5px)',
              },
              '100%': {
                opacity: 0.2,
                transform: 'translateX(0) translateY(0)',
              },
            },
          }}
        />
        
        {/* Additional circuit connections */}
        <Box
          sx={{
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundImage: `
              linear-gradient(45deg, transparent 48%, rgba(14, 165, 233, 0.08) 49%, rgba(14, 165, 233, 0.08) 51%, transparent 52%),
              linear-gradient(-45deg, transparent 48%, rgba(249, 115, 22, 0.06) 49%, rgba(249, 115, 22, 0.06) 51%, transparent 52%)
            `,
            backgroundSize: '200px 200px, 300px 300px',
            opacity: 0.3,
            animation: 'circuitFlow 20s linear infinite',
            '@keyframes circuitFlow': {
              '0%': {
                transform: 'translateX(0) translateY(0)',
              },
              '100%': {
                transform: 'translateX(200px) translateY(200px)',
              },
            },
          }}
        />
      </Box>

      {/* Main content */}
      <motion.div
        style={{ y, opacity }}
        variants={containerVariants}
        initial="hidden"
        animate="visible"
      >
        <Container
          maxWidth="lg"
          sx={{
            position: 'relative',
            zIndex: 2,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            minHeight: '100vh',
            pt: { xs: 8, sm: 12 },
            mx: 'auto',
            width: '100%',
            maxWidth: '1200px',
            px: { xs: 2, sm: 3, md: 4 },
          }}
        >
          <Stack
            spacing={6}
            useFlexGap
            sx={{
              width: { xs: '100%', sm: '95%', md: '90%', lg: '85%' },
              textAlign: 'center',
              alignItems: 'center',
              justifyContent: 'center',
              mx: 'auto',
              maxWidth: '1000px',
            }}
          >
            {/* Advanced headline with text reveal effect */}
            <motion.div 
              variants={textRevealVariants}
            >
              <GameText
                variant="h1"
                textVariant="glow"
                sx={{
                  fontWeight: 800,
                  letterSpacing: '-0.03em',
                  mb: 2,
                  background: 'linear-gradient(135deg, #0ea5e9, #f97316, #8b5cf6)',
                  backgroundClip: 'text',
                  WebkitBackgroundClip: 'text',
                  WebkitTextFillColor: 'transparent',
                  textShadow: '0 0 40px rgba(14, 165, 233, 0.4), 0 0 80px rgba(249, 115, 22, 0.2)',
                  filter: 'drop-shadow(0 4px 8px rgba(0,0,0,0.1))',
                }}
              >
                {headline}
              </GameText>
            </motion.div>

            {/* Subtitle with staggered animation */}
            <motion.div variants={itemVariants}>
              <GameText
                variant="h5"
                textVariant="secondary"
                sx={{
                  maxWidth: '700px',
                  fontWeight: 400,
                  lineHeight: 1.6,
                  opacity: 0.9,
                  fontSize: { xs: '1.1rem', sm: '1.25rem', md: '1.5rem' },
                }}
              >
                The world's first algorithmic conjugate method workout generator. Experience personalized 
                powerlifting programming with RPG-style gamification, automatic exercise rotation, and 
                comprehensive performance tracking.
              </GameText>
            </motion.div>

            {/* Feature highlights */}
            <motion.div variants={itemVariants}>
              <Stack
                direction={{ xs: 'column', sm: 'row' }}
                spacing={3}
                sx={{
                  flexWrap: 'wrap',
                  justifyContent: 'center',
                  gap: 2,
                }}
              >
                {[
                  'Level Progression',
                  'Performance Tracking', 
                  'Skill System'
                ].map((feature, index) => (
                  <Box
                    key={index}
                    sx={{
                      px: 3,
                      py: 1.5,
                      borderRadius: 3,
                      background: theme => 
                        `linear-gradient(135deg, ${alpha(theme.palette.primary.main, 0.1)}, ${alpha(theme.palette.secondary.main, 0.05)})`,
                      border: theme => `1px solid ${alpha(theme.palette.primary.main, 0.2)}`,
                      backdropFilter: 'blur(10px)',
                      transition: 'all 0.3s ease',
                      '&:hover': {
                        transform: 'translateY(-2px)',
                        boxShadow: theme => `0 8px 25px ${alpha(theme.palette.primary.main, 0.2)}`,
                      },
                    }}
                  >
                    <GameText
                      variant="body1"
                      sx={{
                        fontWeight: 600,
                        color: 'primary.main',
                      }}
                    >
                      {feature}
                    </GameText>
                  </Box>
                ))}
              </Stack>
            </motion.div>

            {/* Advanced CTA buttons with magnetic effects */}
            <motion.div variants={itemVariants}>
              <Stack 
                direction={{ xs: 'column', sm: 'row' }} 
                spacing={3} 
                sx={{ mt: 2 }}
              >
                <motion.div
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                >
                  <Box
                    component="a"
                    href="#features"
                    sx={{
                      px: 6,
                      py: 3,
                      borderRadius: 4,
                      background: 'linear-gradient(135deg, #0ea5e9, #0284c7, #0369a1)',
                      color: 'white',
                      border: 'none',
                      fontSize: '1.1rem',
                      fontWeight: 600,
                      cursor: 'pointer',
                      transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                      boxShadow: '0 8px 25px rgba(14, 165, 233, 0.4), 0 0 0 1px rgba(255,255,255,0.1)',
                      textDecoration: 'none',
                      display: 'inline-block',
                      backdropFilter: 'blur(20px)',
                      position: 'relative',
                      overflow: 'hidden',
                      '&::before': {
                        content: '""',
                        position: 'absolute',
                        top: 0,
                        left: '-100%',
                        width: '100%',
                        height: '100%',
                        background: 'linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent)',
                        transition: 'left 0.5s',
                      },
                      '&:hover': {
                        boxShadow: '0 12px 35px rgba(14, 165, 233, 0.6), 0 0 0 1px rgba(255,255,255,0.2)',
                        textDecoration: 'none',
                        color: 'white',
                        '&::before': {
                          left: '100%',
                        },
                      },
                    }}
                  >
                    Explore Features
                  </Box>
                </motion.div>
                
                <motion.div
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                >
                  <Box
                    component="a"
                    href="#how-it-works"
                    sx={{
                      px: 6,
                      py: 3,
                      borderRadius: 4,
                      background: 'rgba(255, 255, 255, 0.05)',
                      color: theme => theme.palette.primary.main,
                      border: theme => `2px solid ${alpha(theme.palette.primary.main, 0.3)}`,
                      fontSize: '1.1rem',
                      fontWeight: 600,
                      cursor: 'pointer',
                      transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                      textDecoration: 'none',
                      display: 'inline-block',
                      backdropFilter: 'blur(20px)',
                      position: 'relative',
                      overflow: 'hidden',
                      '&::before': {
                        content: '""',
                        position: 'absolute',
                        top: 0,
                        left: 0,
                        right: 0,
                        bottom: 0,
                        background: 'linear-gradient(135deg, rgba(14, 165, 233, 0.1), rgba(249, 115, 22, 0.05))',
                        opacity: 0,
                        transition: 'opacity 0.3s',
                      },
                      '&:hover': {
                        background: 'linear-gradient(135deg, #0ea5e9, #0284c7)',
                        color: 'white',
                        border: '2px solid transparent',
                        textDecoration: 'none',
                        boxShadow: '0 8px 25px rgba(14, 165, 233, 0.4)',
                        '&::before': {
                          opacity: 1,
                        },
                      },
                    }}
                  >
                    How It Works
                  </Box>
                </motion.div>
              </Stack>
            </motion.div>

          </Stack>
        </Container>
      </motion.div>
    </Box>
    </motion.div>
  );
}