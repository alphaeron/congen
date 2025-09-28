import Chip from '@mui/material/Chip';
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
      color={color}
      sx={{
        borderRadius: 2,
        fontSize: '0.75rem',
        fontWeight: 500,
      }}
    />
  );
} // end component BinaryTag
