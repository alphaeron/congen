
import { alpha } from '@mui/material';
import Box from '@mui/material/Box';
import Container from '@mui/material/Container';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import * as React from 'react';

import { useTypewriter } from './useTypewriter';

export function Hero() {
  const headline = useTypewriter('Conjugate Method Programming, Without the Hastle', 40);
  return (
    <Box
      id="hero"
      sx={theme => ({
        width: '100%',
        position: 'relative',
        overflow: 'hidden',
        backgroundImage:
          theme.palette.mode === 'light'
            ? 'linear-gradient(180deg, #CEE5FD, #FFF)'
            : `linear-gradient(#02294F, ${alpha('#090E10', 0.0)})`,
        backgroundSize: '100% 100%',
        backgroundRepeat: 'no-repeat',
      })}
    >
      {/* Animated Gym-Themed SVG Background */}
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
        {/* Dumbbell */}
        <Box
          component="span"
          sx={{
            position: 'absolute',
            top: { xs: 30, sm: 60 },
            left: { xs: 20, sm: 80 },
            animation: 'float1 6s ease-in-out infinite',
            opacity: 0.15,
          }}
        >
          <svg
            width="60"
            height="24"
            viewBox="0 0 60 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <rect x="10" y="10" width="40" height="4" rx="2" fill="#1976d2" />
            <rect x="4" y="8" width="6" height="8" rx="2" fill="#424242" />
            <rect x="50" y="8" width="6" height="8" rx="2" fill="#424242" />
          </svg>
        </Box>
        {/* Kettlebell */}
        <Box
          component="span"
          sx={{
            position: 'absolute',
            bottom: { xs: 20, sm: 40 },
            right: { xs: 30, sm: 100 },
            animation: 'float2 7s ease-in-out infinite',
            opacity: 0.13,
          }}
        >
          <svg
            width="32"
            height="40"
            viewBox="0 0 32 40"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <ellipse cx="16" cy="28" rx="14" ry="12" fill="#388e3c" />
            <rect x="8" y="6" width="16" height="10" rx="8" fill="#616161" />
          </svg>
        </Box>
        {/* Barbell with bend effect */}
        <Box
          component="span"
          sx={{
            position: 'absolute',
            top: { xs: 80, sm: 120 },
            right: { xs: 10, sm: 60 },
            animation: 'barbell-bend 2.5s ease-in-out infinite',
            opacity: 0.1,
          }}
        >
          <svg
            width="100"
            height="16"
            viewBox="0 0 100 16"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <rect x="4" y="2" width="12" height="12" rx="6" fill="#757575" />
            <rect x="84" y="2" width="12" height="12" rx="6" fill="#757575" />
            <path
              id="barbell-bar"
              d="M20 8 Q50 8 80 8"
              stroke="#fbc02d"
              strokeWidth="4"
              fill="none"
            />
          </svg>
        </Box>
        {/* Plate Weight */}
        <Box
          component="span"
          sx={{
            position: 'absolute',
            top: { xs: 120, sm: 180 },
            left: { xs: 10, sm: 40 },
            animation: 'float4 9s ease-in-out infinite',
            opacity: 0.1,
          }}
        >
          <svg
            width="40"
            height="40"
            viewBox="0 0 40 40"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <circle cx="20" cy="20" r="18" stroke="#607d8b" strokeWidth="4" fill="#b0bec5" />
            <circle cx="20" cy="20" r="7" fill="#fff" />
          </svg>
        </Box>
        {/* Jump Rope */}
        <Box
          component="span"
          sx={{
            position: 'absolute',
            bottom: { xs: 60, sm: 120 },
            left: { xs: 40, sm: 160 },
            animation: 'float5 8s ease-in-out infinite',
            opacity: 0.09,
          }}
        >
          <svg
            width="60"
            height="32"
            viewBox="0 0 60 32"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path d="M10 28 Q30 4 50 28" stroke="#8d6e63" strokeWidth="4" fill="none" />
            <circle cx="10" cy="28" r="4" fill="#ff7043" />
            <circle cx="50" cy="28" r="4" fill="#ff7043" />
          </svg>
        </Box>
        {/* Water Bottle (spinning, left side) */}
        <Box
          component="span"
          sx={{
            position: 'absolute',
            top: { xs: 60, sm: 120 },
            left: { xs: 80, sm: 180 },
            animation: 'spin-bottle 6s linear infinite',
            opacity: 0.1,
            transformOrigin: '50% 60%',
          }}
        >
          <svg
            width="24"
            height="48"
            viewBox="0 0 24 48"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <rect x="6" y="8" width="12" height="32" rx="6" fill="#4fc3f7" />
            <rect x="9" y="4" width="6" height="8" rx="3" fill="#0288d1" />
            <rect x="10" y="0" width="4" height="6" rx="2" fill="#b3e5fc" />
          </svg>
        </Box>
        {/* Medicine Ball (bouncing) */}
        <Box
          component="span"
          sx={{
            position: 'absolute',
            bottom: { xs: 80, sm: 120 },
            left: { xs: 120, sm: 300 },
            animation: 'bounce-ball 2.2s cubic-bezier(.68,-0.55,.27,1.55) infinite',
            opacity: 0.13,
          }}
        >
          <svg
            width="36"
            height="36"
            viewBox="0 0 36 36"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <circle cx="18" cy="18" r="16" fill="#ff7043" stroke="#fff" strokeWidth="2" />
            <path d="M8 18 Q18 8 28 18" stroke="#fff" strokeWidth="2" fill="none" />
            <path d="M8 18 Q18 28 28 18" stroke="#fff" strokeWidth="2" fill="none" />
          </svg>
        </Box>
        {/* Jump Rope (wobble) */}
        <Box
          component="span"
          sx={{
            position: 'absolute',
            bottom: { xs: 60, sm: 100 },
            right: { xs: 120, sm: 300 },
            animation: 'wobble-rope 2.8s ease-in-out infinite',
            opacity: 0.09,
          }}
        >
          <svg
            width="60"
            height="32"
            viewBox="0 0 60 32"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path d="M10 28 Q30 4 50 28" stroke="#8d6e63" strokeWidth="4" fill="none" />
            <circle cx="10" cy="28" r="4" fill="#ff7043" />
            <circle cx="50" cy="28" r="4" fill="#ff7043" />
          </svg>
        </Box>
        {/* Running Shoe (slide left-right) */}
        <Box
          component="span"
          sx={{
            position: 'absolute',
            top: { xs: 120, sm: 180 },
            right: { xs: 60, sm: 180 },
            animation: 'slide-shoe 3.5s ease-in-out infinite',
            opacity: 0.1,
          }}
        >
          <svg
            width="44"
            height="24"
            viewBox="0 0 44 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <rect x="4" y="14" width="36" height="6" rx="3" fill="#ffb300" />
            <rect x="8" y="8" width="28" height="8" rx="4" fill="#fff" />
            <rect x="12" y="4" width="20" height="6" rx="3" fill="#90caf9" />
          </svg>
        </Box>
        {/* Stopwatch (clock hand moves) */}
        <Box
          component="span"
          sx={{
            position: 'absolute',
            bottom: { xs: 10, sm: 30 },
            right: { xs: 10, sm: 40 },
            opacity: 0.11,
          }}
        >
          <svg
            width="32"
            height="32"
            viewBox="0 0 32 32"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <circle cx="16" cy="16" r="14" fill="#fffde7" stroke="#fbc02d" strokeWidth="3" />
            <rect x="14" y="2" width="4" height="6" rx="2" fill="#fbc02d" />
            <g>
              <line
                x1="16"
                y1="16"
                x2="16"
                y2="8"
                stroke="#fbc02d"
                strokeWidth="2"
                style={{
                  transformOrigin: '16px 16px',
                  animation: 'clock-hand-spin 4s linear infinite',
                }}
              />
            </g>
            <line x1="16" y1="16" x2="24" y2="16" stroke="#fbc02d" strokeWidth="2" />
          </svg>
        </Box>
        {/* Extra Dumbbell (smaller, different position) */}
        <Box
          component="span"
          sx={{
            position: 'absolute',
            bottom: { xs: 100, sm: 180 },
            right: { xs: 120, sm: 220 },
            animation: 'float8 11s ease-in-out infinite',
            opacity: 0.08,
          }}
        >
          <svg
            width="36"
            height="14"
            viewBox="0 0 36 14"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <rect x="6" y="6" width="24" height="2" rx="1" fill="#1976d2" />
            <rect x="1" y="4" width="5" height="6" rx="2" fill="#424242" />
            <rect x="30" y="4" width="5" height="6" rx="2" fill="#424242" />
          </svg>
        </Box>
        {/* Resistance Band (above headline, left) */}
        <Box
          component="span"
          sx={{
            position: 'absolute',
            top: { xs: 0, sm: 10 },
            left: { xs: 60, sm: 180 },
            animation: 'float9 8.5s ease-in-out infinite',
            opacity: 0.1,
          }}
        >
          <svg
            width="56"
            height="24"
            viewBox="0 0 56 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <rect x="4" y="8" width="48" height="8" rx="4" fill="#ab47bc" />
            <rect x="0" y="6" width="8" height="12" rx="4" fill="#7e57c2" />
            <rect x="48" y="6" width="8" height="12" rx="4" fill="#7e57c2" />
          </svg>
        </Box>
        {/* Gym Shoe (above headline, right) */}
        <Box
          component="span"
          sx={{
            position: 'absolute',
            top: { xs: 8, sm: 24 },
            right: { xs: 40, sm: 160 },
            animation: 'float10 9.5s ease-in-out infinite',
            opacity: 0.09,
          }}
        >
          <svg
            width="44"
            height="24"
            viewBox="0 0 44 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <rect x="4" y="14" width="36" height="6" rx="3" fill="#ffb300" />
            <rect x="8" y="8" width="28" height="8" rx="4" fill="#fff" />
            <rect x="12" y="4" width="20" height="6" rx="3" fill="#90caf9" />
          </svg>
        </Box>
        {/* Clipboard (top left) */}
        <Box
          component="span"
          sx={{
            position: 'absolute',
            top: { xs: 0, sm: 0 },
            left: { xs: 0, sm: 0 },
            animation: 'float11 10s ease-in-out infinite',
            opacity: 0.08,
          }}
        >
          <svg
            width="32"
            height="40"
            viewBox="0 0 32 40"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <rect x="4" y="8" width="24" height="28" rx="4" fill="#ffe082" />
            <rect x="10" y="2" width="12" height="8" rx="3" fill="#bcaaa4" />
            <rect x="8" y="14" width="16" height="2" rx="1" fill="#bcaaa4" />
            <rect x="8" y="20" width="16" height="2" rx="1" fill="#bcaaa4" />
            <rect x="8" y="26" width="12" height="2" rx="1" fill="#bcaaa4" />
          </svg>
        </Box>
        {/* Foam Roller (top right) */}
        <Box
          component="span"
          sx={{
            position: 'absolute',
            top: { xs: 0, sm: 0 },
            right: { xs: 0, sm: 0 },
            animation: 'float12 11s ease-in-out infinite',
            opacity: 0.08,
          }}
        >
          <svg
            width="48"
            height="16"
            viewBox="0 0 48 16"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <rect x="4" y="4" width="40" height="8" rx="4" fill="#8d6e63" />
            <rect x="0" y="2" width="8" height="12" rx="4" fill="#bcaaa4" />
            <rect x="40" y="2" width="8" height="12" rx="4" fill="#bcaaa4" />
          </svg>
        </Box>
        {/* Heart Rate Monitor (above headline, center) */}
        <Box
          component="span"
          sx={{
            position: 'absolute',
            top: { xs: 0, sm: 0 },
            left: '50%',
            transform: 'translateX(-50%)',
            animation: 'float13 12s ease-in-out infinite',
            opacity: 0.1,
          }}
        >
          <svg
            width="48"
            height="24"
            viewBox="0 0 48 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <rect x="4" y="8" width="40" height="8" rx="4" fill="#e57373" />
            <polyline
              points="8,16 16,12 20,18 28,6 32,16 40,10 44,16"
              fill="none"
              stroke="#fff"
              strokeWidth="2"
            />
          </svg>
        </Box>
        {/* Hand Grip (above headline, far left, 10%) */}
        <Box
          component="span"
          sx={{
            position: 'absolute',
            top: { xs: 40, sm: 60 },
            left: '10%',
            animation: 'float14 7.5s ease-in-out infinite',
            opacity: 0.1,
          }}
        >
          <svg
            width="32"
            height="32"
            viewBox="0 0 32 32"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <rect x="8" y="12" width="16" height="8" rx="4" fill="#ff7043" />
            <rect x="6" y="8" width="4" height="16" rx="2" fill="#616161" />
            <rect x="22" y="8" width="4" height="16" rx="2" fill="#616161" />
          </svg>
        </Box>
        {/* Protein Shaker (above headline, left-center, 30%) */}
        <Box
          component="span"
          sx={{
            position: 'absolute',
            top: { xs: 40, sm: 60 },
            left: '30%',
            animation: 'float15 8.5s ease-in-out infinite',
            opacity: 0.1,
          }}
        >
          <svg
            width="24"
            height="40"
            viewBox="0 0 24 40"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <rect x="6" y="10" width="12" height="24" rx="6" fill="#81d4fa" />
            <rect x="8" y="4" width="8" height="8" rx="4" fill="#0288d1" />
            <rect x="10" y="0" width="4" height="6" rx="2" fill="#b3e5fc" />
          </svg>
        </Box>
        {/* Yoga Mat (above headline, right-center, 70%) */}
        <Box
          component="span"
          sx={{
            position: 'absolute',
            top: { xs: 40, sm: 60 },
            left: '70%',
            animation: 'float16 8.5s ease-in-out infinite',
            opacity: 0.1,
          }}
        >
          <svg
            width="40"
            height="16"
            viewBox="0 0 40 16"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <rect x="4" y="4" width="32" height="8" rx="4" fill="#4caf50" />
            <rect x="0" y="2" width="8" height="12" rx="4" fill="#81c784" />
            <rect x="32" y="2" width="8" height="12" rx="4" fill="#81c784" />
          </svg>
        </Box>
        {/* Whistle (above headline, far right, 90%) */}
        <Box
          component="span"
          sx={{
            position: 'absolute',
            top: { xs: 40, sm: 60 },
            left: '90%',
            animation: 'float17 7.5s ease-in-out infinite',
            opacity: 0.1,
          }}
        >
          <svg
            width="32"
            height="32"
            viewBox="0 0 32 32"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <circle cx="16" cy="16" r="12" fill="#ffd54f" />
            <rect x="20" y="12" width="8" height="8" rx="4" fill="#ffb300" />
            <rect x="14" y="6" width="4" height="8" rx="2" fill="#fffde7" />
          </svg>
        </Box>
        {/* Keyframes for floating animation */}
        <style>{`
          @keyframes float1 {
            0% { transform: translateY(0px) rotate(-8deg); }
            50% { transform: translateY(-18px) rotate(8deg); }
            100% { transform: translateY(0px) rotate(-8deg); }
          }
          @keyframes float2 {
            0% { transform: translateY(0px) scale(1); }
            50% { transform: translateY(-12px) scale(1.05); }
            100% { transform: translateY(0px) scale(1); }
          }
          @keyframes float3 {
            0% { transform: translateY(0px) rotate(0deg); }
            50% { transform: translateY(-10px) rotate(4deg); }
            100% { transform: translateY(0px) rotate(0deg); }
          }
          @keyframes float4 {
            0% { transform: translateY(0px) scale(1); }
            50% { transform: translateY(-16px) scale(1.08); }
            100% { transform: translateY(0px) scale(1); }
          }
          @keyframes float5 {
            0% { transform: translateY(0px) rotate(-6deg); }
            50% { transform: translateY(-10px) rotate(6deg); }
            100% { transform: translateY(0px) rotate(-6deg); }
          }
          @keyframes float6 {
            0% { transform: translateY(0px) rotate(0deg); }
            50% { transform: translateY(-14px) rotate(-8deg); }
            100% { transform: translateY(0px) rotate(0deg); }
          }
          @keyframes float7 {
            0% { transform: translateY(0px) scale(1); }
            50% { transform: translateY(-8px) scale(1.07); }
            100% { transform: translateY(0px) scale(1); }
          }
          @keyframes float8 {
            0% { transform: translateY(0px) rotate(0deg); }
            50% { transform: translateY(-12px) rotate(-8deg); }
            100% { transform: translateY(0px) rotate(0deg); }
          }
          @keyframes float9 {
            0% { transform: translateY(0px) rotate(-4deg); }
            50% { transform: translateY(-14px) rotate(4deg); }
            100% { transform: translateY(0px) rotate(-4deg); }
          }
          @keyframes float10 {
            0% { transform: translateY(0px) rotate(0deg); }
            50% { transform: translateY(-10px) rotate(-6deg); }
            100% { transform: translateY(0px) rotate(0deg); }
          }
          @keyframes float11 {
            0% { transform: translateY(0px) scale(1); }
            50% { transform: translateY(-12px) scale(1.05); }
            100% { transform: translateY(0px) scale(1); }
          }
          @keyframes float12 {
            0% { transform: translateY(0px) rotate(0deg); }
            50% { transform: translateY(-8px) rotate(8deg); }
            100% { transform: translateY(0px) rotate(0deg); }
          }
          @keyframes float13 {
            0% { transform: translateY(0px) scale(1); }
            50% { transform: translateY(-16px) scale(1.08); }
            100% { transform: translateY(0px) scale(1); }
          }
          @keyframes float14 {
            0% { transform: translateY(0px) rotate(-6deg); }
            50% { transform: translateY(-10px) rotate(6deg); }
            100% { transform: translateY(0px) rotate(-6deg); }
          }
          @keyframes float15 {
            0% { transform: translateY(0px) scale(1); }
            50% { transform: translateY(-12px) scale(1.05); }
            100% { transform: translateY(0px) scale(1); }
          }
          @keyframes float16 {
            0% { transform: translateY(0px) scale(1); }
            50% { transform: translateY(-12px) scale(1.05); }
            100% { transform: translateY(0px) scale(1); }
          }
          @keyframes float17 {
            0% { transform: translateY(0px) rotate(6deg); }
            50% { transform: translateY(-10px) rotate(-6deg); }
            100% { transform: translateY(0px) rotate(6deg); }
          }
          @keyframes barbell-bend {
            0%   { }
            20%  { }
            40%  { }
            50%  { }
            60%  { }
            80%  { }
            100% { }
          }
          @keyframes spin-bottle {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
          @keyframes clock-hand-spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
          @keyframes bounce-ball {
            0%, 100% { transform: translateY(0); }
            20% { transform: translateY(-18px); }
            40% { transform: translateY(-8px); }
            60% { transform: translateY(-22px); }
            80% { transform: translateY(-8px); }
          }
          @keyframes wobble-rope {
            0%, 100% { transform: rotate(-4deg); }
            20% { transform: rotate(4deg); }
            40% { transform: rotate(-6deg); }
            60% { transform: rotate(6deg); }
            80% { transform: rotate(-4deg); }
          }
          @keyframes slide-shoe {
            0%, 100% { transform: translateX(0); }
            50% { transform: translateX(-24px); }
          }
        `}</style>
      </Box>
      <Container
        sx={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          pt: { xs: 14, sm: 20 },
          pb: { xs: 8, sm: 12 },
          position: 'relative',
          zIndex: 1,
        }}
      >
        <Stack spacing={2} useFlexGap sx={{ width: { xs: '100%', sm: '70%' } }}>
          <Typography
            variant="h1"
            sx={{
              display: 'flex',
              flexDirection: { xs: 'column', md: 'row' },
              alignSelf: 'center',
              textAlign: 'center',
              fontSize: 'clamp(3.5rem, 10vw, 4rem)',
              minHeight: '4.5rem',
            }}
          >
            {headline}
          </Typography>
          <Typography
            textAlign="center"
            color="text.secondary"
            sx={{ alignSelf: 'center', width: { sm: '100%', md: '80%' } }}
          >
            ConGen is the first of its kind conjugate workout generator. All the benefits of the
            conjugate method without the hastle of writing a program yourself.
          </Typography>
        </Stack>
      </Container>
    </Box>
  );
}
