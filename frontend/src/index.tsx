import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import * as React from 'react';
import ReactDOM from 'react-dom/client';

import { App } from './App';
import { AppSnackbarProvider } from './AppSnackbarProvider';

import '@mui/material-pigment-css/styles.css';
import '@fontsource/roboto/300.css';
import '@fontsource/roboto/400.css';
import '@fontsource/roboto/500.css';
import '@fontsource/roboto/700.css';
import './index.css';
import './styles/global.css';

const queryClient = new QueryClient();

const root = ReactDOM.createRoot(document.getElementById('root') as HTMLElement);

root.render(
  <React.StrictMode>
    <AppSnackbarProvider>
      <QueryClientProvider client={queryClient}>
        <App />
      </QueryClientProvider>
    </AppSnackbarProvider>
  </React.StrictMode>
);
