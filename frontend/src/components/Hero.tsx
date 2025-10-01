import Box from '@mui/material/Box';
import Container from '@mui/material/Container';
import Stack from '@mui/material/Stack';
import * as React from 'react';

import { useTypewriter } from '../hooks/useTypewriter';
import { GameText, GameContainer, GAME_CLASSES } from './GameTheme';

export function Hero() {
  const headline = useTypewriter('Conjugate Method Programming, Without the Hassle', 40);
  return (
    <Box
      id="hero"
      sx={theme => ({
        width: '100%',
        position: 'relative',
        overflow: 'hidden',
        background:
          theme.palette.mode === 'light'
            ? 'linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 25%, #bae6fd 50%, #7dd3fc 75%, #38bdf8 100%)'
            : `linear-gradient(135deg, #0c4a6e 0%, #075985 25%, #0369a1 50%, #0284c7 75%, #0ea5e9 100%)`,
        backgroundSize: '400% 400%',
        animation: 'gradientShift 15s ease infinite',
        '&::before': {
          content: '""',
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background:
            theme.palette.mode === 'light'
              ? 'radial-gradient(circle at 20% 80%, rgba(120, 119, 198, 0.3) 0%, transparent 50%), radial-gradient(circle at 80% 20%, rgba(255, 119, 198, 0.3) 0%, transparent 50%)'
              : 'radial-gradient(circle at 20% 80%, rgba(14, 165, 233, 0.2) 0%, transparent 50%), radial-gradient(circle at 80% 20%, rgba(249, 115, 22, 0.2) 0%, transparent 50%)',
          zIndex: 0,
        },
      })}
    >
      {/* Modern animated background elements */}
      <Box
        sx={{
          position: 'absolute',
          top: 0,
          left: 0,
          width: '100%',
          height: '100%',
          zIndex: 0,
          pointerEvents: 'none',
        }}
      >
        {/* Floating geometric shapes */}
        <Box
          sx={{
            position: 'absolute',
            top: '10%',
            left: '10%',
            width: 60,
            height: 60,
            borderRadius: '50%',
            background: 'linear-gradient(135deg, rgba(14, 165, 233, 0.1), rgba(249, 115, 22, 0.1))',
            animation: 'float1 8s ease-in-out infinite',
          }}
        />
        <Box
          sx={{
            position: 'absolute',
            top: '20%',
            right: '15%',
            width: 40,
            height: 40,
            borderRadius: '12px',
            background: 'linear-gradient(135deg, rgba(249, 115, 22, 0.1), rgba(14, 165, 233, 0.1))',
            animation: 'float2 10s ease-in-out infinite',
          }}
        />
        <Box
          sx={{
            position: 'absolute',
            bottom: '20%',
            left: '20%',
            width: 50,
            height: 50,
            borderRadius: '50%',
            background: 'linear-gradient(135deg, rgba(34, 197, 94, 0.1), rgba(14, 165, 233, 0.1))',
            animation: 'float3 12s ease-in-out infinite',
          }}
        />
        <Box
          sx={{
            position: 'absolute',
            bottom: '15%',
            right: '10%',
            width: 35,
            height: 35,
            borderRadius: '8px',
            background: 'linear-gradient(135deg, rgba(14, 165, 233, 0.1), rgba(34, 197, 94, 0.1))',
            animation: 'float4 9s ease-in-out infinite',
          }}
        />

        {/* Grid pattern overlay */}
        <Box
          sx={{
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
            backgroundImage:
              'radial-gradient(circle at 1px 1px, rgba(255, 255, 255, 0.1) 1px, transparent 0)',
            backgroundSize: '40px 40px',
            opacity: 0.3,
          }}
        />
      </Box>

      <Container
        sx={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '80vh',
          pt: { xs: 8, sm: 12 },
          pb: { xs: 8, sm: 12 },
          position: 'relative',
          zIndex: 1,
        }}
      >
        <Stack
          spacing={4}
          useFlexGap
          sx={{
            width: { xs: '100%', sm: '80%', md: '70%' },
            textAlign: 'center',
            alignItems: 'center',
          }}
        >
          <GameText
            variant="h1"
            textVariant="glow"
            sx={{
              fontWeight: 800,
              letterSpacing: '-0.03em',
              mb: 2,
            }}
          >
            {headline}
          </GameText>

          <GameText
            variant="h5"
            textVariant="secondary"
            sx={{
              maxWidth: '600px',
              fontWeight: 400,
              lineHeight: 1.6,
              opacity: 0.9,
            }}
          >
            The world's first algorithmic conjugate method workout generator. Experience personalized 
            powerlifting programming with RPG-style gamification, automatic exercise rotation, and 
            comprehensive performance tracking - all without the complexity of manual programming.
          </GameText>

          {/* Call to action buttons */}
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mt: 3 }}>
            <Box
              component="a"
              href="#features"
              sx={{
                px: 4,
                py: 2,
                borderRadius: 3,
                background: 'linear-gradient(135deg, #0ea5e9, #0284c7)',
                color: 'white',
                border: 'none',
                fontSize: '1rem',
                fontWeight: 600,
                cursor: 'pointer',
                transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                boxShadow: '0 8px 25px rgba(14, 165, 233, 0.3)',
                textDecoration: 'none',
                display: 'inline-block',
                '&:hover': {
                  transform: 'translateY(-2px)',
                  boxShadow: '0 12px 35px rgba(14, 165, 233, 0.4)',
                  textDecoration: 'none',
                  color: 'white',
                },
                '&:active': {
                  transform: 'translateY(0)',
                },
              }}
            >
              Explore Features
            </Box>
          </Stack>
        </Stack>
      </Container>

      {/* CSS Animations */}
      <style>{`
        @keyframes gradientShift {
          0% { background-position: 0% 50%; }
          50% { background-position: 100% 50%; }
          100% { background-position: 0% 50%; }
        }

        @keyframes float1 {
          0%, 100% { transform: translateY(0px) rotate(0deg); }
          50% { transform: translateY(-20px) rotate(180deg); }
        }

        @keyframes float2 {
          0%, 100% { transform: translateY(0px) scale(1); }
          50% { transform: translateY(-15px) scale(1.1); }
        }

        @keyframes float3 {
          0%, 100% { transform: translateY(0px) rotate(0deg); }
          50% { transform: translateY(-25px) rotate(-180deg); }
        }

        @keyframes float4 {
          0%, 100% { transform: translateY(0px) scale(1); }
          50% { transform: translateY(-18px) scale(0.9); }
        }
      `}</style>
    </Box>
  );
}
