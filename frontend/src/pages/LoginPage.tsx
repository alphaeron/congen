import React, { useState } from 'react';
import { Container, Paper, Box, Typography, Tabs, Tab, Alert } from '@mui/material';
import { LoginForm } from '../components/LoginForm';
import { RegisterForm } from '../components/RegisterForm';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`auth-tabpanel-${index}`}
      aria-labelledby={`auth-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

export const LoginPage: React.FC = () => {
  const [tabValue, setTabValue] = useState(0);
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const registrationMessage = location.state?.message;

  // Redirect if already authenticated
  React.useEffect(() => {
    if (isAuthenticated) {
      navigate('/profile');
    }
  }, [isAuthenticated, navigate]);

  // Switch to login tab if there's a registration message
  React.useEffect(() => {
    if (registrationMessage) {
      setTabValue(0);
    }
  }, [registrationMessage]);

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleAuthSuccess = () => {
    navigate('/profile');
  };

  const handleSwitchToLogin = () => {
    setTabValue(0);
  };

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
        {registrationMessage && (
          <Alert severity="success" sx={{ width: '100%', mb: 2 }}>
            {registrationMessage}
          </Alert>
        )}
        <Paper elevation={3} sx={{ width: '100%' }}>
          <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
            <Tabs value={tabValue} onChange={handleTabChange} aria-label="auth tabs">
              <Tab label="Sign In" />
              <Tab label="Sign Up" />
            </Tabs>
          </Box>

          <TabPanel value={tabValue} index={0}>
            <Typography component="h1" variant="h5" align="center" gutterBottom>
              Sign In
            </Typography>
            <LoginForm onSuccess={handleAuthSuccess} />
          </TabPanel>

          <TabPanel value={tabValue} index={1}>
            <Typography component="h1" variant="h5" align="center" gutterBottom>
              Create Account
            </Typography>
            <RegisterForm onSuccess={handleAuthSuccess} onSwitchToLogin={handleSwitchToLogin} />
          </TabPanel>
        </Paper>
      </Box>
    </Container>
  );
};
