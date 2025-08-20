import Chip from '@mui/material/Chip';
import { alpha } from '@mui/material/styles';
import * as React from 'react';

/**
 * Props for the BinaryTag component.
 */
interface BinaryTagProps {
  isOn: boolean;
  onText: string;
  offText: string;
  color?: 'primary' | 'secondary' | 'success' | 'error' | 'warning' | 'info';
} // end interface BinaryTagProps

/**
 * A chip that displays one of two texts with the specified label.
 */
export function BinaryTag(props: BinaryTagProps): React.ReactElement<BinaryTagProps> {
  const { isOn, onText, offText, color = 'success' } = props;
  const label = isOn ? onText : offText;

  return (
    <Chip
      label={label}
      size="small"
      variant="outlined"
      sx={{
        borderRadius: 2,
        fontSize: '0.75rem',
        fontWeight: 500,
        borderColor: `${color}.main`,
        color: `${color}.main`,
        backgroundColor: theme => alpha(theme.palette[color].main, 0.1),
        '&:hover': {
          backgroundColor: theme => alpha(theme.palette[color].main, 0.2),
        },
      }}
    />
  );
} // end component BinaryTag
