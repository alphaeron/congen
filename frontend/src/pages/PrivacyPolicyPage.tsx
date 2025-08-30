import {
  Container,
  Typography,
  Box,
  Paper,
  List,
  ListItem,
  ListItemText,
  Divider,
  Alert,
  CircularProgress,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import * as React from 'react';

import { getPrivacyPolicy } from '../api/gdpr';
import type { PrivacyPolicy } from '../api/types';

import type { AxiosError } from 'axios';

/**
 * Privacy Policy page component.
 *
 * This page displays the complete privacy policy and data processing information
 * as required by GDPR Articles 13 and 14. The page is accessible without authentication
 * to ensure transparency and compliance with GDPR requirements.
 *
 * ## GDPR Compliance
 * - Provides information about data controller
 * - Details data processing purposes and legal basis
 * - Explains user rights under GDPR
 * - Shows data retention periods
 * - Accessible without authentication
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
export function PrivacyPolicyPage(): React.ReactElement {
  const { enqueueSnackbar } = useSnackbar();
  const [privacyPolicy, setPrivacyPolicy] = React.useState<PrivacyPolicy | null>(null);
  const [loading, setLoading] = React.useState(true);

  React.useEffect(() => {
    const fetchPrivacyPolicy = async () => {
      try {
        const response = await getPrivacyPolicy();
        setPrivacyPolicy(response.data);
      } catch (err: unknown) {
        const axiosError = err as AxiosError<{ message?: string }>;
        enqueueSnackbar(axiosError.response?.data?.message || 'Failed to load privacy policy', {
          variant: 'error',
        });
      } finally {
        setLoading(false);
      }
    };

    fetchPrivacyPolicy();
  }, []);

  if (loading) {
    return (
      <Container maxWidth="md" sx={{ py: 4, textAlign: 'center' }}>
        <CircularProgress />
        <Typography variant="h6" sx={{ mt: 2 }}>
          Loading Privacy Policy...
        </Typography>
      </Container>
    );
  }

  if (!privacyPolicy) {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Alert severity="error">Privacy policy not available</Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Typography variant="h3" component="h1" gutterBottom>
        Privacy Policy
      </Typography>

      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        Last updated: {new Date(privacyPolicy.last_updated).toLocaleDateString()} (Version{' '}
        {privacyPolicy.version})
      </Typography>

      {/* Contact Information */}
      <Alert severity="info" sx={{ mt: 3, mb: 3 }}>
        <Typography variant="body2">
          <strong>Questions about this privacy policy?</strong>
          <br />
          Contact us at: {privacyPolicy.data_controller.contact}
        </Typography>
      </Alert>

      {/* Data Controller Section */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h5" component="h2" gutterBottom>
          Data Controller
        </Typography>
        <Typography variant="body1" paragraph>
          <strong>Name:</strong> {privacyPolicy.data_controller.name}
        </Typography>
        <Typography variant="body1" paragraph>
          <strong>Contact:</strong> {privacyPolicy.data_controller.contact}
        </Typography>
        {privacyPolicy.data_controller.dpo && (
          <Typography variant="body1" paragraph>
            <strong>Data Protection Officer:</strong> {privacyPolicy.data_controller.dpo}
          </Typography>
        )}
      </Paper>

      {/* Data Processing Section */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h5" component="h2" gutterBottom>
          Data Processing
        </Typography>

        <Box sx={{ mb: 3 }}>
          <Typography variant="h6" component="h3" gutterBottom>
            Purposes of Processing
          </Typography>
          <List dense>
            {privacyPolicy.data_processing.purposes.map((purpose, index) => (
              <ListItem key={index} sx={{ py: 0.5 }}>
                <ListItemText primary={purpose} />
              </ListItem>
            ))}
          </List>
        </Box>

        <Box sx={{ mb: 3 }}>
          <Typography variant="h6" component="h3" gutterBottom>
            Legal Basis
          </Typography>
          <List dense>
            {privacyPolicy.data_processing.legal_basis.map((basis, index) => (
              <ListItem key={index} sx={{ py: 0.5 }}>
                <ListItemText primary={basis} />
              </ListItem>
            ))}
          </List>
        </Box>

        <Box sx={{ mb: 3 }}>
          <Typography variant="h6" component="h3" gutterBottom>
            Types of Data Collected
          </Typography>
          <List dense>
            {privacyPolicy.data_processing.data_types.map((dataType, index) => (
              <ListItem key={index} sx={{ py: 0.5 }}>
                <ListItemText primary={dataType} />
              </ListItem>
            ))}
          </List>
        </Box>

        <Box>
          <Typography variant="h6" component="h3" gutterBottom>
            Data Retention Periods
          </Typography>
          {Object.entries(privacyPolicy.data_processing.retention_periods).map(
            ([dataType, period]) => (
              <Typography key={dataType} variant="body2" sx={{ mb: 1 }}>
                <strong>
                  {dataType.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())}:
                </strong>{' '}
                {period}
              </Typography>
            )
          )}
        </Box>
      </Paper>

      {/* User Rights Section */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h5" component="h2" gutterBottom>
          Your Rights
        </Typography>

        <Box sx={{ mb: 2 }}>
          <Typography variant="h6" component="h3" gutterBottom>
            Right of Access
          </Typography>
          <Typography variant="body2" paragraph>
            {privacyPolicy.user_rights.access}
          </Typography>
        </Box>

        <Divider sx={{ my: 2 }} />

        <Box sx={{ mb: 2 }}>
          <Typography variant="h6" component="h3" gutterBottom>
            Right to Rectification
          </Typography>
          <Typography variant="body2" paragraph>
            {privacyPolicy.user_rights.rectification}
          </Typography>
        </Box>

        <Divider sx={{ my: 2 }} />

        <Box sx={{ mb: 2 }}>
          <Typography variant="h6" component="h3" gutterBottom>
            Right to Erasure (Right to be Forgotten)
          </Typography>
          <Typography variant="body2">{privacyPolicy.user_rights.erasure}</Typography>
        </Box>

        <Divider sx={{ my: 2 }} />

        <Box sx={{ mb: 2 }}>
          <Typography variant="h6" component="h3" gutterBottom>
            Right to Data Portability
          </Typography>
          <Typography variant="body2">{privacyPolicy.user_rights.portability}</Typography>
        </Box>

        <Divider sx={{ my: 2 }} />

        <Box sx={{ mb: 2 }}>
          <Typography variant="h6" component="h3" gutterBottom>
            Right to Object
          </Typography>
          <Typography variant="body2">{privacyPolicy.user_rights.objection}</Typography>
        </Box>

        <Divider sx={{ my: 2 }} />

        <Box>
          <Typography variant="h6" component="h3" gutterBottom>
            Right to File a Complaint
          </Typography>
          <Typography variant="body2">{privacyPolicy.user_rights.complaint}</Typography>
        </Box>
      </Paper>
    </Container>
  );
}
