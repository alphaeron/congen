import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useSnackbar } from 'notistack';
import * as React from 'react';

import { AppSnackbarProvider } from './AppSnackbarProvider';

function SnackbarTrigger(): React.ReactElement {
  const { enqueueSnackbar } = useSnackbar();

  return (
    <button type="button" onClick={() => enqueueSnackbar('Test message', { variant: 'success' })}>
      Show snackbar
    </button>
  );
}

describe('AppSnackbarProvider', () => {
  it('renders a close button on snackbars', async () => {
    const user = userEvent.setup();

    render(
      <AppSnackbarProvider>
        <SnackbarTrigger />
      </AppSnackbarProvider>
    );

    await user.click(screen.getByRole('button', { name: 'Show snackbar' }));

    await waitFor(() => {
      expect(screen.getByText('Test message')).toBeInTheDocument();
    });

    expect(screen.getByRole('button', { name: 'Close' })).toBeInTheDocument();
  });

  it('dismisses snackbar when close button is clicked', async () => {
    const user = userEvent.setup();

    render(
      <AppSnackbarProvider>
        <SnackbarTrigger />
      </AppSnackbarProvider>
    );

    await user.click(screen.getByRole('button', { name: 'Show snackbar' }));

    await waitFor(() => {
      expect(screen.getByText('Test message')).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: 'Close' }));

    await waitFor(() => {
      expect(screen.queryByText('Test message')).not.toBeInTheDocument();
    });
  });
});
