import CloseIcon from '@mui/icons-material/Close';
import IconButton from '@mui/material/IconButton';
import {
  SnackbarProvider,
  closeSnackbar,
  type SnackbarKey,
  type SnackbarProviderProps,
} from 'notistack';
import * as React from 'react';

function getSnackbarCloseAction(snackbarId: SnackbarKey): React.ReactElement {
  return (
    <IconButton
      aria-label="Close"
      color="inherit"
      size="small"
      onClick={() => closeSnackbar(snackbarId)}
    >
      <CloseIcon fontSize="small" />
    </IconButton>
  );
}

/**
 * Configured notistack provider that adds a dismiss action to every snackbar.
 *
 * @param props SnackbarProvider props; a custom `action` overrides the default close button.
 * @return A provider that wraps the application and manages snackbar notifications.
 */
export function AppSnackbarProvider({
  children,
  action,
  ...props
}: SnackbarProviderProps): React.ReactElement {
  return (
    <SnackbarProvider action={action ?? getSnackbarCloseAction} {...props}>
      {children}
    </SnackbarProvider>
  );
}
