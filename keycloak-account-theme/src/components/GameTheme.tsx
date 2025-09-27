import React from 'react';
import { Card, Box, Chip, Typography, TextField, Select, MenuItem, FormControl, InputLabel } from '@mui/material';
import { styled, alpha } from '@mui/material/styles';

import { brand, secondary, gray, green, warning } from '../theme';
import '../styles/gameTheme.css';

/**
 * Centralized game-themed styling for RPG-style components.
 * This promotes consistency across all gamified UI elements and aligns with Congen's design system.
 * 
 * All styling is now handled via CSS classes in gameTheme.css for better maintainability.
 */

// CSS class names for easy reference
export const GAME_CLASSES = {
  // Text classes
  text: 'game-text',
  textSecondary: 'game-text-secondary',
  textMuted: 'game-text-muted',
  textAccent: 'game-text-accent',
  textGlow: 'game-text-glow',
  textAnimatedGlow: 'game-text-animated-glow',
  textBold: 'game-text-bold',
  textMedium: 'game-text-medium',
  textCenter: 'game-text-center',
  textItalic: 'game-text-italic',
  
  // Card classes
  card: 'game-card',
  cardInteractive: 'game-card-interactive',
  subCard: 'game-sub-card',
  
  // Chip classes
  statusChip: 'game-status-chip',
  statusChipPending: 'game-status-chip-pending',
  statusChipCompleted: 'game-status-chip-completed',
  statusChipInfo: 'game-status-chip-info',
  skillChip: 'game-skill-chip',
  
  // Button classes
  button: 'game-button',
  
  // Tab classes
  tabs: 'game-tabs',
  
  // List item classes
  listItem: 'game-list-item',
  
  // Progress classes
  progressBar: 'game-progress-bar',
  circularProgress: 'game-circular-progress',
  
  // Spacing and utility classes
  marginBottom1: 'game-margin-bottom-1',
  marginBottom2: 'game-margin-bottom-2',
  marginBottom3: 'game-margin-bottom-3',
  marginTop1: 'game-margin-top-1',
  marginTop2: 'game-margin-top-2',
  padding2: 'game-padding-2',
  padding3: 'game-padding-3',
  opacity70: 'game-opacity-70',
  opacity80: 'game-opacity-80',
  
  // Dialog classes
  dialog: 'game-dialog',
  
  // Form classes
  formInput: 'game-form-input',
  textField: 'game-text-field',
  select: 'game-select',
  menuItem: 'game-menu-item',
  
  // Alert classes
  alert: 'game-alert',
  
  // Icon classes
  icon: 'game-icon',
  
  // Container classes
  container: 'game-container',
  sidebar: 'game-sidebar',
  header: 'game-header',
  
  // Layout utility classes
  overflowVisible: 'game-overflow-visible',
  overflowHidden: 'game-overflow-hidden',
  overflowAuto: 'game-overflow-auto',
  width100: 'game-width-100',
  height100: 'game-height-100',
  height100vh: 'game-height-100vh',
  flex: 'game-flex',
  flexColumn: 'game-flex-column',
  flexGrow1: 'game-flex-grow-1',
  flexShrink0: 'game-flex-shrink-0',
  flex1: 'game-flex-1',
  positionRelative: 'game-position-relative',
  positionAbsolute: 'game-position-absolute',
  top0: 'game-top-0',
  borderBottom1: 'game-border-bottom-1',
  borderColorDivider: 'game-border-color-divider',
  minWidth40: 'game-min-width-40',
  paddingBottom0: 'game-padding-bottom-0',
  marginTop3: 'game-margin-top-3',
  
  // Additional utility classes for specific styling
  paddingTop0: 'game-padding-top-0',
  rowGap0: 'game-row-gap-0',
  marginTopNegative16: 'game-margin-top-negative-16',
  marginTopNegative10: 'game-margin-top-negative-10',
  fontSize80: 'game-font-size-80',
  fontSize40: 'game-font-size-40',
  fontSize32: 'game-font-size-32',
  fontSize24: 'game-font-size-24',
  fontSize16: 'game-font-size-16',
  fontSize3rem: 'game-font-size-3rem',
  colorWhite: 'game-color-white',
  colorCyan: 'game-color-cyan',
  colorLightGray: 'game-color-light-gray',
  textShadowGlow: 'game-text-shadow-glow',
  textShadowGlow5: 'game-text-shadow-glow-5',
  textTransformUppercase: 'game-text-transform-uppercase',
  lineHeight1: 'game-line-height-1',
  marginTop16: 'game-margin-top-16',
  tooltipBackground: 'game-tooltip-background',
  fontSizeSmall: 'game-font-size-small',
  fontStyleItalic: 'game-font-style-italic',
  height80: 'game-height-80',
  cursorPointer: 'game-cursor-pointer',
  hoverOpacity80: 'game-hover-opacity-80',
  justifyCenter: 'game-justify-center',
  backgroundColorCyan: 'game-background-color-cyan',
  hoverBackgroundColorCyan: 'game-hover-background-color-cyan',
  alignItemsCenter: 'game-align-items-center',
  paddingTop1: 'game-padding-top-1',
  
  // Utility classes (removed duplicates)
};

/**
 * Simple wrapper components that use CSS classes for styling
 */

// Game Card Component
export const GameCard: React.FC<React.ComponentProps<typeof Card> & { interactive?: boolean }> = ({ 
  className = '', 
  interactive = false, 
  ...props 
}) => (
  <Card 
    className={`${GAME_CLASSES.card} ${interactive ? GAME_CLASSES.cardInteractive : ''} ${className}`}
    {...props} 
  />
);

// Game Sub Card Component
export const GameSubCard: React.FC<React.ComponentProps<typeof Card>> = ({ 
  className = '', 
  ...props 
}) => (
  <Card 
    className={`${GAME_CLASSES.subCard} ${className}`}
    {...props} 
  />
);

// Game Status Chip Component
export const GameStatusChip: React.FC<React.ComponentProps<typeof Chip> & { 
  status?: 'PENDING' | 'COMPLETED' | 'SKIPPED' | 'ERROR' | 'WARNING' | 'INFO' | 'SUCCESS' 
}> = ({ 
  className = '', 
  status = 'PENDING', 
  ...props 
}) => {
  const getStatusClass = () => {
    switch (status) {
      case 'COMPLETED':
      case 'SUCCESS':
        return GAME_CLASSES.statusChipCompleted;
      case 'SKIPPED':
      case 'WARNING':
        return GAME_CLASSES.statusChipPending;
      case 'ERROR':
        return GAME_CLASSES.statusChipPending;
      case 'INFO':
        return GAME_CLASSES.statusChipInfo;
      default:
        return GAME_CLASSES.statusChipPending;
    }
  };

  return (
    <Chip 
      className={`${GAME_CLASSES.statusChip} ${getStatusClass()} ${className}`}
      {...props} 
    />
  );
};

// Game Skill Chip Component
export const GameSkillChip: React.FC<React.ComponentProps<typeof Chip>> = ({ 
  className = '', 
  ...props 
}) => (
  <Chip 
    className={`${GAME_CLASSES.skillChip} ${className}`}
    {...props} 
  />
);

// Game Text Components
export const GameText: React.FC<React.ComponentProps<typeof Typography> & { 
  textVariant?: 'primary' | 'secondary' | 'muted' | 'accent' | 'glow' | 'animatedGlow' 
}> = ({ 
  className = '', 
  textVariant = 'primary', 
  ...props 
}) => {
  const getVariantClass = () => {
    switch (textVariant) {
      case 'secondary':
        return GAME_CLASSES.textSecondary;
      case 'muted':
        return GAME_CLASSES.textMuted;
      case 'accent':
        return GAME_CLASSES.textAccent;
      case 'glow':
        return GAME_CLASSES.textGlow;
      case 'animatedGlow':
        return GAME_CLASSES.textAnimatedGlow;
      default:
        return GAME_CLASSES.text;
    }
  };

  return (
    <Typography 
      className={`${getVariantClass()} ${className}`}
      {...props} 
    />
  );
};

// Alias for secondary text
export const GameTextSecondary: React.FC<Omit<React.ComponentProps<typeof Typography>, 'textVariant'> & { 
  textVariant?: never 
}> = ({ 
  className = '', 
  ...props 
}) => (
  <GameText 
    textVariant="secondary" 
    className={className} 
    {...props} 
  />
);

// Game Button Component
export const GameButton: React.FC<React.ComponentProps<'button'> & { 
  variant?: 'primary' | 'secondary' 
}> = ({ 
  className = '', 
  variant = 'primary', 
  ...props 
}) => (
  <button 
    className={`${GAME_CLASSES.button} ${className}`}
    {...props} 
  />
);

// Game Container Component
export const GameContainer: React.FC<React.ComponentProps<typeof Box>> = ({ 
  className = '', 
  ...props 
}) => (
  <Box 
    className={`${GAME_CLASSES.container} ${className}`}
    {...props} 
  />
);

// Game Sidebar Component
export const GameSidebar: React.FC<React.ComponentProps<typeof Box>> = ({ 
  className = '', 
  ...props 
}) => (
  <Box 
    className={`${GAME_CLASSES.sidebar} ${className}`}
    {...props} 
  />
);

// Game Header Component
export const GameHeader: React.FC<React.ComponentProps<typeof Box>> = ({ 
  className = '', 
  ...props 
}) => (
  <Box 
    className={`${GAME_CLASSES.header} ${className}`}
    {...props} 
  />
);

// Game Progress Bar Component
export const GameProgressBar: React.FC<React.ComponentProps<typeof Box>> = ({ 
  className = '', 
  ...props 
}) => (
  <Box 
    className={`${GAME_CLASSES.progressBar} ${className}`}
    {...props} 
  />
);

// Game Alert Component
export const GameAlert: React.FC<React.ComponentProps<typeof Box>> = ({ 
  className = '', 
  ...props 
}) => (
  <Box 
    className={`${GAME_CLASSES.alert} ${className}`}
    {...props} 
  />
);

// Game Text Field Component - Unified form input styling
export const GameTextField: React.FC<React.ComponentProps<typeof TextField>> = ({ 
  className = '', 
  ...props 
}) => (
  <TextField 
    className={`${GAME_CLASSES.formInput} ${className}`}
    {...props} 
  />
);

// Game Select Component - Unified form input styling
export const GameSelect: React.FC<React.ComponentProps<typeof Select>> = ({ 
  className = '', 
  ...props 
}) => (
  <Select 
    className={`${GAME_CLASSES.formInput} ${className}`}
    {...props} 
  />
);

// Game Form Control Component - For Select with label
export const GameFormControl: React.FC<React.ComponentProps<typeof FormControl>> = ({ 
  className = '', 
  ...props 
}) => (
  <FormControl 
    className={`${GAME_CLASSES.formInput} ${className}`}
    {...props} 
  />
);

// Game Input Label Component
export const GameInputLabel: React.FC<React.ComponentProps<typeof InputLabel>> = ({ 
  className = '', 
  ...props 
}) => (
  <InputLabel 
    className={`${GAME_CLASSES.formInput} ${className}`}
    {...props} 
  />
);

// Game Menu Item Component
export const GameMenuItem: React.FC<React.ComponentProps<typeof MenuItem>> = ({ 
  className = '', 
  ...props 
}) => (
  <MenuItem 
    className={`${GAME_CLASSES.menuItem} ${className}`}
    {...props} 
  />
);

// Game Dialog Component
export const GameDialog: React.FC<React.ComponentProps<typeof Box>> = ({ 
  className = '', 
  ...props 
}) => (
  <Box 
    className={`${GAME_CLASSES.dialog} ${className}`}
    {...props} 
  />
);

// Game Icon Component
export const GameIcon: React.FC<React.ComponentProps<typeof Box>> = ({ 
  className = '', 
  ...props 
}) => (
  <Box 
    className={`${GAME_CLASSES.icon} ${className}`}
    {...props} 
  />
);
