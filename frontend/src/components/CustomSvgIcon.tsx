import { SvgIcon } from '@mui/material';
import React from 'react';

import type { SvgIconProps } from '@mui/material';

interface CustomSvgIconProps extends Omit<SvgIconProps, 'children'> {
  src: string;
  alt?: string;
}

/**
 * Custom SvgIcon component that loads SVG files as icons.
 * This provides better integration with MUI's theming and accessibility.
 * The SVG icons use 'currentColor' so they inherit the theme's color.
 */
export const CustomSvgIcon: React.FC<CustomSvgIconProps> = ({ src, sx, ...props }) => {
  return (
    <SvgIcon
      {...props}
      sx={{
        // Default styling for better integration
        '& svg': {
          width: '100%',
          height: '100%',
        },
        // Inherit color from theme
        color: 'inherit',
        ...sx,
      }}
    >
      <image href={src} width="100%" height="100%" />
    </SvgIcon>
  );
};
