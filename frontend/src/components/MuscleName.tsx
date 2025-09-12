import { Tooltip, Typography } from '@mui/material';
import React from 'react';

import { useApiGet } from '../api/hooks';
import { getIndividualMuscle } from '../api/muscle';
import type { Muscle } from '../api/types';
import { capitalizeEachWord } from '../common/utils';

import type { TypographyProps } from '@mui/material';

interface MuscleNameProps {
  muscleName: string;
  variant?: TypographyProps['variant'];
  component?: React.ElementType;
  sx?: TypographyProps['sx'];
  children?: React.ReactNode;
}

/**
 * Component that renders a muscle name with a tooltip containing muscle information.
 * The tooltip shows muscle description and other relevant information.
 *
 * @param props The component props.
 * @return The muscle name component with tooltip.
 */
export function MuscleName({
  muscleName,
  variant = 'body2',
  component,
  sx,
  children,
}: MuscleNameProps): React.ReactElement {
  const {
    data: muscle,
    isLoading: isMuscleLoading,
    error: muscleError,
  } = useApiGet<Muscle>(
    [`individualMuscle${muscleName}`],
    getIndividualMuscle,
    {
      enabled: true,
      refetchOnWindowFocus: false,
      retry: 1,
    },
    [muscleName]
  );

  const isLoading = isMuscleLoading;
  const hasError = muscleError;

  const tooltipContent = React.useMemo(() => {
    if (isLoading) {
      return 'Loading muscle information...';
    }

    if (hasError || !muscle) {
      return `Muscle: ${muscleName}`;
    }

    return (
      <div>
        <div style={{ fontWeight: 'bold', marginBottom: '8px' }}>{muscle.name}</div>
        {muscle.description && <div style={{ fontStyle: 'italic' }}>{muscle.description}</div>}
      </div>
    );
  }, [muscle, muscleName, isLoading, hasError]);

  return (
    <Tooltip title={tooltipContent} arrow placement="top">
      <Typography
        variant={variant}
        component={component}
        sx={{
          cursor: 'help',
          ...sx,
        }}
      >
        {children || capitalizeEachWord(muscleName)}
      </Typography>
    </Tooltip>
  );
}
