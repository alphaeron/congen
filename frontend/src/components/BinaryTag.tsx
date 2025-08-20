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
} // end interface BinaryTagProps

/**
 * A chip that displays one of two texts with the specified label.
 */
export function BinaryTag(props: BinaryTagProps): React.ReactElement<BinaryTagProps> {
  const { isOn, onText, offText } = props;
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
        borderColor: isOn ? 'success.main' : 'text.secondary',
        color: isOn ? 'success.main' : 'text.secondary',
        backgroundColor: isOn 
          ? theme => alpha(theme.palette.success.main, 0.1)
          : theme => alpha(theme.palette.text.secondary, 0.1),
        '&:hover': {
          backgroundColor: isOn 
            ? theme => alpha(theme.palette.success.main, 0.2)
            : theme => alpha(theme.palette.text.secondary, 0.2),
        },
      }}
    />
  );
} // end component BinaryTag
