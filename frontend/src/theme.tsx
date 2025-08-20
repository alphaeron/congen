import type {} from '@mui/material/themeCssVarsAugmentation';
import { red } from '@mui/material/colors';
import { alpha } from '@mui/material/styles';

import type { PaletteMode } from '@mui/material';
import type { Theme, ThemeOptions } from '@mui/material/styles';

declare module '@mui/material-pigment-css' {
  interface ThemeArgs {
    theme: Theme;
  }
}

declare module '@mui/material/styles/createPalette' {
  interface ColorRange {
    50: string;
    100: string;
    200: string;
    300: string;
    400: string;
    500: string;
    600: string;
    700: string;
    800: string;
    900: string;
  }
}

// Modern Congen brand colors - inspired by fitness/strength
export const brand = {
  50: '#f0f9ff',
  100: '#e0f2fe',
  200: '#bae6fd',
  300: '#7dd3fc',
  400: '#38bdf8',
  500: '#0ea5e9', // Primary blue - modern and energetic
  600: '#0284c7',
  700: '#0369a1',
  800: '#075985',
  900: '#0c4a6e',
};

// Secondary accent - vibrant orange for energy and motivation
export const secondary = {
  50: '#fff7ed',
  100: '#ffedd5',
  200: '#fed7aa',
  300: '#fdba74',
  400: '#fb923c',
  500: '#f97316', // Vibrant orange
  600: '#ea580c',
  700: '#c2410c',
  800: '#9a3412',
  900: '#7c2d12',
};

// Modern gray scale - more contrast and sophistication
export const gray = {
  50: '#fafafa',
  100: '#f5f5f5',
  200: '#e5e5e5',
  300: '#d4d4d4',
  400: '#a3a3a3',
  500: '#737373',
  600: '#525252',
  700: '#404040',
  800: '#262626',
  900: '#171717',
};

// Success green - for achievements and progress
export const green = {
  50: '#f0fdf4',
  100: '#dcfce7',
  200: '#bbf7d0',
  300: '#86efac',
  400: '#4ade80',
  500: '#22c55e',
  600: '#16a34a',
  700: '#15803d',
  800: '#166534',
  900: '#14532d',
};

// Warning/energy colors
export const warning = {
  50: '#fffbeb',
  100: '#fef3c7',
  200: '#fde68a',
  300: '#fcd34d',
  400: '#fbbf24',
  500: '#f59e0b',
  600: '#d97706',
  700: '#b45309',
  800: '#92400e',
  900: '#78350f',
};

const getDesignTokens = (mode: PaletteMode) => ({
  palette: {
    mode,
    primary: {
      light: brand[300],
      main: brand[500],
      dark: brand[700],
      contrastText: '#ffffff',
      ...(mode === 'dark' && {
        light: brand[400],
        main: brand[500],
        dark: brand[800],
        contrastText: '#ffffff',
      }),
    },
    secondary: {
      light: secondary[300],
      main: secondary[500],
      dark: secondary[700],
      contrastText: '#ffffff',
      ...(mode === 'dark' && {
        light: secondary[400],
        main: secondary[500],
        dark: secondary[800],
        contrastText: '#ffffff',
      }),
    },
    warning: {
      main: warning[500],
      dark: warning[700],
      ...(mode === 'dark' && {
        main: warning[400],
        dark: warning[600],
      }),
    },
    error: {
      light: red[50],
      main: red[500],
      dark: red[700],
      ...(mode === 'dark' && {
        light: red[400],
        main: red[500],
        dark: red[600],
      }),
    },
    success: {
      light: green[300],
      main: green[500],
      dark: green[700],
      ...(mode === 'dark' && {
        light: green[400],
        main: green[500],
        dark: green[600],
      }),
    },
    grey: {
      50: gray[50],
      100: gray[100],
      200: gray[200],
      300: gray[300],
      400: gray[400],
      500: gray[500],
      600: gray[600],
      700: gray[700],
      800: gray[800],
      900: gray[900],
    },
    divider: mode === 'dark' ? alpha(gray[700], 0.4) : alpha(gray[200], 0.6),
    background: {
      default: mode === 'dark' ? '#0a0a0a' : '#ffffff',
      paper: mode === 'dark' ? '#111111' : '#fafafa',
    },
    text: {
      primary: mode === 'dark' ? '#ffffff' : gray[900],
      secondary: mode === 'dark' ? gray[300] : gray[600],
    },
    action: {
      selected: alpha(brand[500], 0.12),
      hover: alpha(brand[500], 0.04),
      ...(mode === 'dark' && {
        selected: alpha(brand[400], 0.16),
        hover: alpha(brand[400], 0.08),
      }),
    },
  },
  typography: {
    fontFamily: ['"Inter", "system-ui", "sans-serif"'].join(','),
    h1: {
      fontSize: 'clamp(2.5rem, 8vw, 4rem)',
      fontWeight: 700,
      lineHeight: 1.1,
      letterSpacing: '-0.02em',
    },
    h2: {
      fontSize: 'clamp(2rem, 6vw, 3rem)',
      fontWeight: 600,
      lineHeight: 1.2,
      letterSpacing: '-0.01em',
    },
    h3: {
      fontSize: 'clamp(1.5rem, 4vw, 2.25rem)',
      fontWeight: 600,
      lineHeight: 1.3,
    },
    h4: {
      fontSize: 'clamp(1.25rem, 3vw, 1.75rem)',
      fontWeight: 600,
      lineHeight: 1.4,
    },
    h5: {
      fontSize: '1.25rem',
      fontWeight: 600,
      lineHeight: 1.4,
    },
    h6: {
      fontSize: '1.125rem',
      fontWeight: 600,
      lineHeight: 1.4,
    },
    subtitle1: {
      fontSize: '1.125rem',
      fontWeight: 500,
      lineHeight: 1.5,
    },
    subtitle2: {
      fontSize: '1rem',
      fontWeight: 500,
      lineHeight: 1.5,
    },
    body1: {
      fontSize: '1rem',
      fontWeight: 400,
      lineHeight: 1.6,
    },
    body2: {
      fontSize: '0.875rem',
      fontWeight: 400,
      lineHeight: 1.6,
    },
    caption: {
      fontSize: '0.75rem',
      fontWeight: 500,
      lineHeight: 1.4,
    },
    button: {
      fontSize: '0.875rem',
      fontWeight: 600,
      textTransform: 'none',
      letterSpacing: '0.025em',
    },
  },
  shape: {
    borderRadius: 12,
  },
  shadows: [
    'none',
    '0px 1px 2px rgba(0, 0, 0, 0.05)',
    '0px 1px 3px rgba(0, 0, 0, 0.1), 0px 1px 2px rgba(0, 0, 0, 0.06)',
    '0px 4px 6px rgba(0, 0, 0, 0.1), 0px 2px 4px rgba(0, 0, 0, 0.06)',
    '0px 10px 15px rgba(0, 0, 0, 0.1), 0px 4px 6px rgba(0, 0, 0, 0.05)',
    '0px 20px 25px rgba(0, 0, 0, 0.1), 0px 10px 10px rgba(0, 0, 0, 0.04)',
    '0px 25px 50px rgba(0, 0, 0, 0.15)',
    '0px 25px 50px rgba(0, 0, 0, 0.15)',
    '0px 25px 50px rgba(0, 0, 0, 0.15)',
    '0px 25px 50px rgba(0, 0, 0, 0.15)',
    '0px 25px 50px rgba(0, 0, 0, 0.15)',
    '0px 25px 50px rgba(0, 0, 0, 0.15)',
    '0px 25px 50px rgba(0, 0, 0, 0.15)',
    '0px 25px 50px rgba(0, 0, 0, 0.15)',
    '0px 25px 50px rgba(0, 0, 0, 0.15)',
    '0px 25px 50px rgba(0, 0, 0, 0.15)',
    '0px 25px 50px rgba(0, 0, 0, 0.15)',
    '0px 25px 50px rgba(0, 0, 0, 0.15)',
    '0px 25px 50px rgba(0, 0, 0, 0.15)',
    '0px 25px 50px rgba(0, 0, 0, 0.15)',
    '0px 25px 50px rgba(0, 0, 0, 0.15)',
    '0px 25px 50px rgba(0, 0, 0, 0.15)',
    '0px 25px 50px rgba(0, 0, 0, 0.15)',
    '0px 25px 50px rgba(0, 0, 0, 0.15)',
    '0px 25px 50px rgba(0, 0, 0, 0.15)',
    '0px 25px 50px rgba(0, 0, 0, 0.15)',
  ],
});

export function getTheme(mode: PaletteMode): ThemeOptions {
  return {
    ...getDesignTokens(mode),
    components: {
      MuiAccordion: {
        defaultProps: {
          elevation: 0,
          disableGutters: true,
        },
        styleOverrides: {
          root: ({ theme }) => ({
            backgroundColor: 'transparent',
            border: `1px solid ${theme.palette.divider}`,
            borderRadius: 12,
            overflow: 'hidden',
            '&:before': {
              display: 'none',
            },
            '&:not(:last-child)': {
              marginBottom: 16,
            },
          }),
        },
      },
      MuiAccordionSummary: {
        styleOverrides: {
          root: ({ theme }) => ({
            padding: '16px 20px',
            '&:hover': {
              backgroundColor: alpha(theme.palette.primary.main, 0.04),
            },
            '&.Mui-expanded': {
              minHeight: 56,
            },
          }),
        },
      },
      MuiAccordionDetails: {
        styleOverrides: {
          root: {
            padding: '0 20px 20px',
          },
        },
      },
      MuiToggleButtonGroup: {
        styleOverrides: {
          root: ({ theme }) => ({
            borderRadius: 12,
            boxShadow: theme.shadows[2],
            '& .Mui-selected': {
              backgroundColor: theme.palette.primary.main,
              color: theme.palette.primary.contrastText,
              '&:hover': {
                backgroundColor: theme.palette.primary.dark,
              },
            },
          }),
        },
      },
      MuiToggleButton: {
        styleOverrides: {
          root: ({ theme }) => ({
            padding: '12px 20px',
            textTransform: 'none',
            fontWeight: 500,
            border: 'none',
            '&:not(:first-of-type)': {
              borderLeft: `1px solid ${theme.palette.divider}`,
            },
            '&:hover': {
              backgroundColor: alpha(theme.palette.primary.main, 0.08),
            },
          }),
        },
      },
      MuiButtonBase: {
        defaultProps: {
          disableTouchRipple: true,
          disableRipple: true,
        },
        styleOverrides: {
          root: {
            transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
            '&:focus-visible': {
              outline: `2px solid ${alpha(brand[500], 0.5)}`,
              outlineOffset: '2px',
            },
          },
        },
      },
      MuiButton: {
        styleOverrides: {
          root: ({ theme }) => ({
            borderRadius: 12,
            textTransform: 'none',
            fontWeight: 600,
            padding: '12px 24px',
            fontSize: '0.875rem',
            letterSpacing: '0.025em',
            transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
            '&:active': {
              transform: 'scale(0.98)',
            },
            variants: [
              {
                props: { size: 'small' },
                style: {
                  padding: '8px 16px',
                  fontSize: '0.75rem',
                },
              },
              {
                props: { size: 'large' },
                style: {
                  padding: '16px 32px',
                  fontSize: '1rem',
                },
              },
              {
                props: { color: 'primary', variant: 'contained' },
                style: {
                  background: `linear-gradient(135deg, ${brand[500]}, ${brand[600]})`,
                  boxShadow: `0 4px 14px ${alpha(brand[500], 0.4)}`,
                  '&:hover': {
                    background: `linear-gradient(135deg, ${brand[600]}, ${brand[700]})`,
                    boxShadow: `0 6px 20px ${alpha(brand[500], 0.6)}`,
                    transform: 'translateY(-1px)',
                  },
                },
              },
              {
                props: { variant: 'outlined' },
                style: {
                  border: `2px solid ${theme.palette.primary.main}`,
                  backgroundColor: 'transparent',
                  color: theme.palette.primary.main,
                  '&:hover': {
                    backgroundColor: alpha(theme.palette.primary.main, 0.08),
                    borderColor: theme.palette.primary.dark,
                  },
                },
              },
              {
                props: { variant: 'text' },
                style: {
                  color: theme.palette.primary.main,
                  '&:hover': {
                    backgroundColor: alpha(theme.palette.primary.main, 0.08),
                  },
                },
              },
            ],
          }),
        },
      },
      MuiCard: {
        styleOverrides: {
          root: ({ theme }) => ({
            backgroundColor: theme.palette.background.paper,
            borderRadius: 16,
            border: `1px solid ${theme.palette.divider}`,
            boxShadow: theme.shadows[2],
            transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
            overflow: 'hidden',
            '&:hover': {
              boxShadow: theme.shadows[4],
              transform: 'translateY(-2px)',
            },
          }),
        },
      },
      MuiChip: {
        styleOverrides: {
          root: ({ theme }) => ({
            borderRadius: 20,
            fontWeight: 600,
            fontSize: '0.75rem',
            height: 28,
            '& .MuiChip-label': {
              padding: '0 12px',
            },
            variants: [
              {
                props: { color: 'primary' },
                style: {
                  backgroundColor: alpha(theme.palette.primary.main, 0.12),
                  color: theme.palette.primary.main,
                  '&:hover': {
                    backgroundColor: alpha(theme.palette.primary.main, 0.2),
                  },
                },
              },
              {
                props: { color: 'secondary' },
                style: {
                  backgroundColor: alpha(theme.palette.secondary.main, 0.12),
                  color: theme.palette.secondary.main,
                  '&:hover': {
                    backgroundColor: alpha(theme.palette.secondary.main, 0.2),
                  },
                },
              },
            ],
          }),
        },
      },
      MuiDivider: {
        styleOverrides: {
          root: ({ theme }) => ({
            borderColor: theme.palette.divider,
          }),
        },
      },
      MuiLink: {
        defaultProps: {
          underline: 'none',
        },
        styleOverrides: {
          root: ({ theme }) => ({
            color: theme.palette.primary.main,
            fontWeight: 500,
            textDecoration: 'none',
            position: 'relative',
            '&::after': {
              content: '""',
              position: 'absolute',
              width: 0,
              height: '2px',
              bottom: -2,
              left: 0,
              backgroundColor: theme.palette.primary.main,
              transition: 'width 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
            },
            '&:hover::after': {
              width: '100%',
            },
          }),
        },
      },
      MuiMenuItem: {
        styleOverrides: {
          root: ({ theme }) => ({
            borderRadius: 8,
            margin: '4px 8px',
            padding: '12px 16px',
            fontWeight: 500,
            '&:hover': {
              backgroundColor: alpha(theme.palette.primary.main, 0.08),
            },
          }),
        },
      },
      MuiPaper: {
        styleOverrides: {
          root: ({ theme }) => ({
            backgroundImage: 'none',
            backgroundColor: theme.palette.background.paper,
            borderRadius: 12,
          }),
        },
      },
      MuiSwitch: {
        styleOverrides: {
          root: ({ theme }) => ({
            width: 44,
            height: 24,
            padding: 0,
            '& .MuiSwitch-switchBase': {
              padding: 2,
              '&.Mui-checked': {
                transform: 'translateX(20px)',
                '& + .MuiSwitch-track': {
                  backgroundColor: theme.palette.primary.main,
                  opacity: 1,
                },
              },
            },
            '& .MuiSwitch-track': {
              borderRadius: 12,
              backgroundColor: theme.palette.grey[400],
              opacity: 1,
            },
            '& .MuiSwitch-thumb': {
              width: 20,
              height: 20,
              boxShadow: '0 2px 4px rgba(0, 0, 0, 0.2)',
            },
          }),
        },
      },
      MuiTextField: {
        styleOverrides: {
          root: ({ theme }) => ({
            '& .MuiInputBase-root': {
              borderRadius: 12,
              backgroundColor: theme.palette.background.paper,
              border: `1px solid ${theme.palette.divider}`,
              transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
              '&:hover': {
                borderColor: theme.palette.primary.main,
              },
              '&.Mui-focused': {
                borderColor: theme.palette.primary.main,
                boxShadow: `0 0 0 3px ${alpha(theme.palette.primary.main, 0.1)}`,
              },
              '& fieldset': {
                border: 'none',
              },
            },
            '& .MuiInputLabel-root': {
              '&.Mui-focused': {
                color: theme.palette.primary.main,
              },
            },
          }),
        },
      },
      MuiAppBar: {
        styleOverrides: {
          root: ({ theme }) => ({
            backgroundColor: alpha(theme.palette.background.paper, 0.8),
            backdropFilter: 'blur(20px)',
            borderBottom: `1px solid ${theme.palette.divider}`,
            boxShadow: 'none',
          }),
        },
      },
      MuiToolbar: {
        styleOverrides: {
          root: {
            minHeight: 80,
            padding: '0 24px',
          },
        },
      },
      MuiContainer: {
        styleOverrides: {
          root: {
            paddingLeft: 24,
            paddingRight: 24,
          },
        },
      },
    },
  };
}
