import { Tooltip, Typography } from '@mui/material';
import React, { useEffect, useState } from 'react';

import type { Muscle } from '../api/types';
import { capitalizeEachWord } from '../common/utils';
import { useData } from '../contexts/DataContext';

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
  const { getMuscle } = useData();
  const [muscle, setMuscle] = useState<Muscle | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [hasError, setHasError] = useState(false);

  useEffect(() => {
    const loadMuscle = async () => {
      if (!muscleName) return;

      setIsLoading(true);
      setHasError(false);

      try {
        const muscleData = await getMuscle(muscleName);
        setMuscle(muscleData);
      } catch {
        setHasError(true);
        // Error is handled by setting hasError state
      } finally {
        setIsLoading(false);
      }
    };

    loadMuscle();
  }, [muscleName, getMuscle]);

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
