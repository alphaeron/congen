import React from 'react';
import { Container, Box, Paper } from '@mui/material';
import { ProfileCreationForm } from '../components/ProfileCreationForm';

export const ProfileCreationPage: React.FC = () => {
  return (
    <Container component="main" maxWidth="sm">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Paper elevation={3} sx={{ width: '100%', p: 3 }}>
          <ProfileCreationForm />
        </Paper>
      </Box>
    </Container>
  );
};
