import Chip from '@mui/material/Chip';
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
  const label = `${props.isOn ? props.onText : props.offText}`;
  return <Chip label={label} />;
} // end component BinaryTag
