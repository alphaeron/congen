import {
  PrivacyTip as PrivacyIcon,
  Download as DownloadIcon,
  DeleteForever as DeleteForeverIcon,
  CheckCircle as CheckCircleIcon,
  Cancel as CancelIcon,
  Policy as PolicyIcon,
} from '@mui/icons-material';
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  DialogContentText,
  Divider,
  FormControl,
  FormControlLabel,
  FormLabel,
  Radio,
  RadioGroup,
  TextField,
  Typography,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React from 'react';
import { Link } from 'react-router';

import { ConfirmationDialog } from './ConfirmationDialog';
import { FormDialog } from './FormDialog';
import { LoadingSpinner } from './LoadingSpinner';
import {
  recordConsent,
  getConsentStatus,
  exportUserData,
  deleteAllPersonalData,
} from '../api/gdpr';
import type { UserConsent } from '../api/types';
import { formatDate } from '../common/utils';

import type { AxiosError } from 'axios';

/**
 * GDPR Compliance Section component for user profile.
 *
 * This component provides users with all required GDPR actions including:
 * - Consent management (give/withdraw consent)
 * - Data export (Right to Data Portability)
 * - Data deletion (Right to be Forgotten)
 * - Privacy policy access
 *
 * ## GDPR Rights Implemented
 * - Article 7: Consent management
 * - Article 15: Right of Access (data export)
 * - Article 17: Right to Erasure (data deletion)
 * - Article 20: Right to Data Portability
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
export function GdprComplianceSection(): React.ReactElement {
  const { enqueueSnackbar } = useSnackbar();
  const [consentStatus, setConsentStatus] = React.useState<UserConsent | null>(null);
  const [loading, setLoading] = React.useState(true);
  const [operationLoading, setOperationLoading] = React.useState<string | null>(null);

  // Dialog states
  const [consentDialogOpen, setConsentDialogOpen] = React.useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = React.useState(false);
  const [deleteConfirmation, setDeleteConfirmation] = React.useState('');

  // Form data interface for consent
  interface ConsentFormData {
    consentValue: boolean;
  }

  // Load initial consent status
  React.useEffect(() => {
    loadConsentStatus();
  }, []);

  const loadConsentStatus = async () => {
    try {
      setLoading(true);
      const response = await getConsentStatus();
      setConsentStatus(response);
    } catch (err: unknown) {
      const axiosError = err as AxiosError<{ message?: string }>;
      enqueueSnackbar(axiosError.response?.data?.message || 'Failed to load consent status', {
        variant: 'error',
      });
    } finally {
      setLoading(false);
    }
  };

  const handleConsentChange = async (data: ConsentFormData) => {
    try {
      setOperationLoading('consent');
      await recordConsent(data.consentValue);
      await loadConsentStatus(); // Refresh status
      setConsentDialogOpen(false);
      enqueueSnackbar(
        data.consentValue ? 'Consent given successfully' : 'Consent withdrawn successfully',
        { variant: 'success' }
      );
    } catch (err: unknown) {
      const axiosError = err as AxiosError<{ message?: string }>;
      enqueueSnackbar(axiosError.response?.data?.message || 'Failed to update consent', {
        variant: 'error',
      });
    } finally {
      setOperationLoading(null);
    }
  };

  const handleDataExport = async () => {
    try {
      setOperationLoading('export');
      const response = await exportUserData();

      // Create and download JSON file
      const dataStr = JSON.stringify(response, null, 2);
      const dataBlob = new Blob([dataStr], { type: 'application/json' });
      const url = URL.createObjectURL(dataBlob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `congen-personal-data-${new Date().toISOString().split('T')[0]}.json`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);

      enqueueSnackbar('Personal data exported successfully', { variant: 'success' });
    } catch (err: unknown) {
      const axiosError = err as AxiosError<{ message?: string }>;
      enqueueSnackbar(axiosError.response?.data?.message || 'Failed to export data', {
        variant: 'error',
      });
    } finally {
      setOperationLoading(null);
    }
  };

  const handleDataDeletion = async () => {
    if (deleteConfirmation !== 'DELETE_ALL_MY_DATA') {
      enqueueSnackbar('Please type "DELETE_ALL_MY_DATA" to confirm deletion', { variant: 'error' });
      return;
    }

    try {
      setOperationLoading('delete');
      await deleteAllPersonalData(deleteConfirmation);
      setDeleteDialogOpen(false);
      enqueueSnackbar('All personal data has been deleted. You will be logged out shortly.', {
        variant: 'success',
        autoHideDuration: 5000,
      });
      // In a real app, you'd redirect to logout here
      setTimeout(() => {
        window.location.href = '/login';
      }, 3000);
    } catch (err: unknown) {
      const axiosError = err as AxiosError<{ message?: string }>;
      enqueueSnackbar(axiosError.response?.data?.message || 'Failed to delete data', {
        variant: 'error',
      });
    } finally {
      setOperationLoading(null);
    }
  };

  if (loading) {
    return (
      <Card>
        <CardContent>
          <Box display="flex" alignItems="center" gap={2}>
            <LoadingSpinner size={24} message="" />
            <Typography>Loading GDPR compliance status...</Typography>
          </Box>
        </CardContent>
      </Card>
    );
  }

  return (
    <React.Fragment>
      <Card>
        <CardContent>
          <Box display="flex" alignItems="center" gap={2} mb={2}>
            <PrivacyIcon color="primary" />
            <Typography variant="h6">Privacy & Data Protection</Typography>
          </Box>

          <Typography variant="body2" color="text.secondary" paragraph>
            Manage your data and privacy settings. You have the right to control how your personal
            data is processed.
          </Typography>

          <Divider sx={{ my: 2 }} />

          {/* Consent Status */}
          <Box sx={{ mb: 3 }}>
            <Typography variant="subtitle1" gutterBottom>
              Data Processing Consent
            </Typography>
            <Box display="flex" alignItems="center" gap={2} mb={2}>
              {consentStatus?.data_processing_consent ? (
                <Chip
                  icon={<CheckCircleIcon />}
                  label="Consent Given"
                  color="success"
                  size="small"
                />
              ) : (
                <Chip icon={<CancelIcon />} label="Consent Withdrawn" color="error" size="small" />
              )}
              {consentStatus?.consent_timestamp && (
                <Typography variant="caption" color="text.secondary">
                  Last updated:{' '}
                  {formatDate(consentStatus.consent_timestamp, {
                    year: 'numeric',
                    month: 'short',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit',
                  })}
                </Typography>
              )}
            </Box>
            <Button variant="outlined" size="small" onClick={() => setConsentDialogOpen(true)}>
              {consentStatus?.data_processing_consent ? 'Withdraw Consent' : 'Give Consent'}
            </Button>
          </Box>

          <Divider sx={{ my: 2 }} />

          {/* GDPR Actions */}
          <Typography variant="subtitle1" gutterBottom>
            Your Data Rights
          </Typography>

          <Box display="flex" flexDirection="column" gap={2}>
            {/* Data Export */}
            <Box display="flex" justifyContent="space-between" alignItems="center">
              <Box>
                <Typography variant="body2" fontWeight="medium">
                  Export Your Data
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Download all your personal data in JSON format
                </Typography>
              </Box>
              <Button
                variant="outlined"
                size="small"
                startIcon={<DownloadIcon />}
                onClick={handleDataExport}
                disabled={operationLoading === 'export'}
              >
                {operationLoading === 'export' ? 'Exporting...' : 'Export Data'}
              </Button>
            </Box>

            {/* Privacy Policy */}
            <Box display="flex" justifyContent="space-between" alignItems="center">
              <Box>
                <Typography variant="body2" fontWeight="medium">
                  Privacy Policy
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  View our privacy policy and data processing information
                </Typography>
              </Box>
              <Button
                variant="outlined"
                size="small"
                startIcon={<PolicyIcon />}
                component={Link}
                to="/privacy_policy"
              >
                View Policy
              </Button>
            </Box>

            {/* Data Deletion */}
            <Box display="flex" justifyContent="space-between" alignItems="center">
              <Box>
                <Typography variant="body2" fontWeight="medium" color="error">
                  Delete All Data
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Permanently delete all your personal data
                </Typography>
              </Box>
              <Button
                variant="outlined"
                size="small"
                color="error"
                startIcon={<DeleteForeverIcon />}
                onClick={() => setDeleteDialogOpen(true)}
                disabled={operationLoading === 'delete'}
              >
                Delete All Data
              </Button>
            </Box>
          </Box>
        </CardContent>
      </Card>

      {/* Consent Dialog */}
      <FormDialog<ConsentFormData>
        open={consentDialogOpen}
        onClose={() => setConsentDialogOpen(false)}
        onSubmit={handleConsentChange}
        title={consentStatus?.data_processing_consent ? 'Withdraw Consent' : 'Give Consent'}
        description={
          consentStatus?.data_processing_consent
            ? 'By withdrawing consent, you revoke permission for us to process your personal data. Some features may become unavailable.'
            : 'By giving consent, you allow us to process your personal data for the purposes outlined in our privacy policy.'
        }
        submitText="Confirm"
        submitColor="primary"
        loading={operationLoading === 'consent'}
        useTanStackForm={true}
        defaultValues={{
          consentValue: !consentStatus?.data_processing_consent, // Opposite of current state
        }}
        validate={values => {
          const errors: Record<string, string> = {};
          if (typeof values.consentValue !== 'boolean') {
            errors.consentValue = 'Please make a selection';
          }
          return Object.keys(errors).length > 0 ? errors : undefined;
        }}
      >
        {form => (
          <FormControl component="fieldset" fullWidth>
            <FormLabel component="legend">Your choice:</FormLabel>
            <RadioGroup
              value={form.state.values.consentValue}
              onChange={e => form.setFieldValue('consentValue', e.target.value === 'true')}
            >
              <FormControlLabel
                value={true}
                control={<Radio />}
                label="I give consent for data processing"
              />
              <FormControlLabel
                value={false}
                control={<Radio />}
                label="I withdraw consent for data processing"
              />
            </RadioGroup>
          </FormControl>
        )}
      </FormDialog>

      {/* Delete Confirmation Dialog */}
      <ConfirmationDialog
        open={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
        onConfirm={handleDataDeletion}
        title="Delete All Personal Data"
        confirmText="Delete All Data"
        confirmColor="error"
        loading={operationLoading === 'delete'}
        disabled={deleteConfirmation !== 'DELETE_ALL_MY_DATA'}
      >
        <DialogContentText paragraph>
          <strong>This action cannot be undone!</strong>
        </DialogContentText>
        <DialogContentText paragraph>
          This will permanently delete all your personal data including:
        </DialogContentText>
        <Typography component="ul" variant="body2" color="text.secondary">
          <li>Profile information</li>
          <li>Exercise preferences and history</li>
          <li>Workout programs and progress</li>
          <li>Account settings and preferences</li>
        </Typography>
        <DialogContentText paragraph sx={{ mt: 2 }}>
          To confirm deletion, please type <strong>DELETE_ALL_MY_DATA</strong> below:
        </DialogContentText>
        <TextField
          fullWidth
          variant="outlined"
          placeholder="DELETE_ALL_MY_DATA"
          value={deleteConfirmation}
          onChange={e => setDeleteConfirmation(e.target.value)}
          error={deleteConfirmation !== '' && deleteConfirmation !== 'DELETE_ALL_MY_DATA'}
          helperText={
            deleteConfirmation !== '' && deleteConfirmation !== 'DELETE_ALL_MY_DATA'
              ? 'Please type exactly "DELETE_ALL_MY_DATA"'
              : ''
          }
        />
      </ConfirmationDialog>
    </React.Fragment>
  );
}
