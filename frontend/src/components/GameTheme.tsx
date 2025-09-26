import { styled, alpha } from '@mui/material/styles';
import { Card, Box, Chip, Typography, useTheme } from '@mui/material';
import { brand, secondary, gray, green, warning } from '../theme';

/**
 * Centralized game-themed styling for RPG-style components.
 * This promotes consistency across all gamified UI elements and aligns with Congen's design system.
 */

// Base gradient background using Congen's brand colors
export const GAME_GRADIENT = `linear-gradient(135deg, ${brand[500]} 0%, ${brand[700]} 100%)`;

// Alternative gradient using secondary colors for variety
export const GAME_GRADIENT_SECONDARY = `linear-gradient(135deg, ${secondary[500]} 0%, ${secondary[700]} 100%)`;

// Common spacing and styling values - consistent with Congen theme
export const GAME_SPACING = {
  cardPadding: 2,
  borderRadius: 16, // Matches Congen's borderRadius
  shadow: '0 8px 32px rgba(0, 0, 0, 0.12)', // Softer shadow matching Congen
  backdropFilter: 'blur(20px)', // Matches Congen's backdrop filter
  border: `1px solid ${alpha(brand[500], 0.2)}`, // Using brand color with alpha
};

// Status colors using Congen's color palette
export const STATUS_COLORS = {
  completed: green[500],
  skipped: warning[500],
  pending: alpha(gray[400], 0.3),
  error: '#F44336', // Keep red for errors
  warning: warning[500],
  info: brand[500],
  success: green[500],
};

// Text colors for game theme - using Congen's text colors
export const TEXT_COLORS = {
  primary: '#ffffff',
  secondary: alpha('#ffffff', 0.8),
  muted: alpha('#ffffff', 0.6),
  accent: warning[400], // Using warning color for accent instead of gold
};

/**
 * Base game card with consistent styling - matches Congen's modern aesthetic
 */
export const GameCard = styled(Card)(({ theme }) => ({
  background: alpha(brand[500], 0.15), // Use hover background as default
  color: TEXT_COLORS.primary,
  borderRadius: GAME_SPACING.borderRadius,
  boxShadow: GAME_SPACING.shadow,
  backdropFilter: GAME_SPACING.backdropFilter,
  border: `1px solid ${alpha(brand[500], 0.3)}`, // More visible border
  overflow: 'hidden',
  // Explicitly disable hover animation
  '&:hover': {
    background: alpha(brand[500], 0.15), // Keep same background
    boxShadow: GAME_SPACING.shadow, // Keep same shadow
    transform: 'none', // No transform
  },
}));

/**
 * Secondary card for nested content - using Congen's alpha values
 */
export const GameSubCard = styled(Card)(({ theme }) => ({
  backgroundColor: alpha('#ffffff', 0.1),
  color: TEXT_COLORS.primary,
  borderRadius: 12, // Matches Congen's borderRadius
  border: `1px solid ${alpha('#ffffff', 0.2)}`,
  transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
  '&:hover': {
    backgroundColor: alpha('#ffffff', 0.15),
    transform: 'translateY(-1px)',
  },
}));

/**
 * Status chip with consistent styling - matches Congen's chip design
 */
export const GameStatusChip = styled(Chip)<{ 
  status?: 'PENDING' | 'COMPLETED' | 'SKIPPED' | 'ERROR' | 'WARNING' | 'INFO' | 'SUCCESS' 
}>(({ theme, status = 'PENDING' }) => {
  const getStatusColor = () => {
    switch (status) {
      case 'COMPLETED':
      case 'SUCCESS':
        return { bg: STATUS_COLORS.completed, color: TEXT_COLORS.primary };
      case 'SKIPPED':
      case 'WARNING':
        return { bg: STATUS_COLORS.skipped, color: TEXT_COLORS.primary };
      case 'ERROR':
        return { bg: STATUS_COLORS.error, color: TEXT_COLORS.primary };
      case 'INFO':
        return { bg: STATUS_COLORS.info, color: TEXT_COLORS.primary };
      default:
        return { bg: STATUS_COLORS.pending, color: TEXT_COLORS.primary };
    }
  };
  
  const colors = getStatusColor();
  return {
    backgroundColor: colors.bg,
    color: colors.color,
    fontWeight: 600, // Matches Congen's font weight
    borderRadius: 20, // Matches Congen's chip borderRadius
    fontSize: '0.75rem', // Matches Congen's chip fontSize
    height: 28, // Matches Congen's chip height
    '& .MuiChip-label': {
      padding: '0 12px', // Matches Congen's chip label padding
    },
  };
});

/**
 * Skill chip with accent styling using Congen's warning color
 */
export const GameSkillChip = styled(Chip)(({ theme }) => ({
  backgroundColor: alpha(warning[400], 0.2),
  color: warning[400],
  border: `1px solid ${alpha(warning[400], 0.3)}`,
  margin: theme.spacing(0.5),
  borderRadius: 20, // Matches Congen's chip borderRadius
  fontWeight: 600,
  fontSize: '0.75rem',
  height: 28,
  '& .MuiChip-label': {
    padding: '0 12px',
  },
}));

/**
 * Container for status bars with consistent spacing - matches Congen's design
 */
export const StatusBarContainer = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  gap: theme.spacing(1),
  padding: theme.spacing(1),
  transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
}));

/**
 * Base text styling for game theme - matches Congen's typography
 */
export const GameText = styled(Typography)(({ theme }) => ({
  color: TEXT_COLORS.primary,
  fontFamily: '"Inter", "system-ui", "sans-serif"', // Matches Congen's font family
  fontWeight: 400,
}));

/**
 * Secondary text styling
 */
export const GameTextSecondary = styled(Typography)(({ theme }) => ({
  color: TEXT_COLORS.secondary,
  fontFamily: '"Inter", "system-ui", "sans-serif"',
  fontWeight: 400,
}));

/**
 * Muted text styling
 */
export const GameTextMuted = styled(Typography)(({ theme }) => ({
  color: TEXT_COLORS.muted,
  fontFamily: '"Inter", "system-ui", "sans-serif"',
  fontWeight: 400,
}));

/**
 * Accent text styling using Congen's warning color
 */
export const GameTextAccent = styled(Typography)(({ theme }) => ({
  color: TEXT_COLORS.accent,
  fontFamily: '"Inter", "system-ui", "sans-serif"',
  fontWeight: 600, // Slightly bolder for accent text
}));
