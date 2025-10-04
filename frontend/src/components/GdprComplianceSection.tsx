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
  Chip,
  DialogContentText,
  Divider,
  FormControl,
  FormControlLabel,
  FormLabel,
  Radio,
  RadioGroup,
  TextField,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React from 'react';
import { Link } from 'react-router';
import { motion } from 'framer-motion';

import { ConfirmationDialog } from './ConfirmationDialog';
import { GameCard, GameText, GAME_CLASSES } from './GameTheme';
import { FormDialog } from './FormDialog';
import { LoadingSpinner } from './LoadingSpinner';
import { formatDate } from '../common/utils';
import { useData } from '../contexts/DataContext';

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
  const {
    userConsent,
    loadUserConsent,
    updateUserConsent,
    exportUserData,
    deleteAllPersonalData,
    isLoading,
    isReady,
  } = useData();
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
    loadUserConsent();
  }, [loadUserConsent]);

  const handleConsentChange = async (data: ConsentFormData) => {
    try {
      setOperationLoading('consent');
      await updateUserConsent(data.consentValue);
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
      enqueueSnackbar(
        'Your account and all data has been deleted. You will be logged out shortly.',
        {
          variant: 'success',
          autoHideDuration: 5000,
        }
      );
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

  if (!isReady || isLoading) {
    return (
      <GameCard>
        <Box className={GAME_CLASSES.padding2}>
          <Box display="flex" alignItems="center" gap={2}>
            <LoadingSpinner size={24} message="" />
            <GameText>Loading GDPR compliance status...</GameText>
          </Box>
        </Box>
      </GameCard>
    );
  }

  return (
    <React.Fragment>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.8, ease: 'easeOut' }}
        whileHover={{ y: -2 }}
        style={{ transition: 'box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1)' }}
      >
        <GameCard
          sx={{
            transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
            '&:hover': {
              boxShadow: '0 8px 25px rgba(0, 188, 212, 0.15)',
            }
          }}
        >
        <Box className={GAME_CLASSES.padding2}>
          <motion.div
            initial={{ opacity: 0, x: -30 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.6, ease: 'easeOut', delay: 0.2 }}
            style={{ 
              display: 'flex', 
              alignItems: 'center', 
              gap: '16px',
              marginBottom: '16px',
            }}
          >
            <motion.div
              animate={{ 
                scale: [1, 1.05, 1],
                opacity: [1, 0.8, 1]
              }}
              transition={{ 
                duration: 2, 
                repeat: Infinity, 
                ease: 'easeInOut' 
              }}
            >
              <PrivacyIcon 
                sx={{ 
                  color: '#00bcd4',
                }} 
              />
            </motion.div>
            <motion.div
              animate={{ 
                scale: [1, 1.02, 1],
                opacity: [1, 0.8, 1]
              }}
              transition={{ 
                duration: 2, 
                repeat: Infinity, 
                ease: 'easeInOut' 
              }}
            >
              <GameText 
                variant="h6" 
                textVariant="glow"
              >
                Privacy & Data Protection
              </GameText>
            </motion.div>
          </motion.div>

          <GameText 
            variant="body2" 
            textVariant="secondary" 
            paragraph
            sx={{
            }}
          >
            Manage your data and privacy settings. You have the right to control how your personal
            data is processed.
          </GameText>

          <Divider sx={{ my: 2 }} />

          {/* Consent Status */}
          <Box 
            className={GAME_CLASSES.marginBottom3}
            sx={{
            }}
          >
            <GameText 
              variant="subtitle1" 
              gutterBottom
              sx={{
              }}
            >
              Data Processing Consent
            </GameText>
            <Box 
              display="flex" 
              alignItems="center" 
              gap={2} 
              className={GAME_CLASSES.marginBottom2}
              sx={{
              }}
            >
              {userConsent?.data_processing_consent ? (
                <Chip
                  icon={<CheckCircleIcon />}
                  label="Consent Given"
                  color="success"
                  size="small"
                  sx={{
                    transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                    '&:hover': {
                      transform: 'scale(1.05)',
                      boxShadow: '0 4px 15px rgba(76, 175, 80, 0.3)',
                    }
                  }}
                />
              ) : (
                <Chip 
                  icon={<CancelIcon />} 
                  label="Consent Withdrawn" 
                  color="error" 
                  size="small"
                  sx={{
                    transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                    '&:hover': {
                      transform: 'scale(1.05)',
                      boxShadow: '0 4px 15px rgba(244, 67, 54, 0.3)',
                    }
                  }}
                />
              )}
              {userConsent?.consent_timestamp && (
                <GameText 
                  variant="caption" 
                  textVariant="secondary"
                  sx={{
                  }}
                >
                  Last updated:{' '}
                  {formatDate(userConsent.consent_timestamp, {
                    year: 'numeric',
                    month: 'short',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit',
                  })}
                </GameText>
              )}
            </Box>
            <Button 
              variant="outlined" 
              size="small" 
              onClick={() => setConsentDialogOpen(true)}
              sx={{ 
                borderColor: '#00bcd4', 
                color: '#00bcd4',
                transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                '&:hover': {
                  borderColor: '#00acc1',
                  backgroundColor: 'rgba(0, 188, 212, 0.1)',
                  transform: 'translateY(-2px)',
                  boxShadow: '0 4px 15px rgba(0, 188, 212, 0.3)',
                }
              }}
            >
              {userConsent?.data_processing_consent ? 'Withdraw Consent' : 'Give Consent'}
            </Button>
          </Box>

          <Divider sx={{ my: 2 }} />

          {/* GDPR Actions */}
          <GameText 
            variant="subtitle1" 
            gutterBottom
            sx={{
            }}
          >
            Your Data Rights
          </GameText>

          <Box 
            display="flex" 
            flexDirection="column" 
            gap={2}
            sx={{
            }}
          >
            {/* Data Export */}
            <Box 
              display="flex" 
              justifyContent="space-between" 
              alignItems="center"
              sx={{
                transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                '&:hover': {
                  transform: 'translateX(4px)',
                  backgroundColor: 'rgba(0, 188, 212, 0.05)',
                  borderRadius: 1,
                  padding: 1,
                }
              }}
            >
              <Box>
                <GameText variant="body2" className={GAME_CLASSES.textMedium}>
                  Export Your Data
                </GameText>
                <GameText variant="caption" textVariant="secondary">
                  Download all your personal data in JSON format
                </GameText>
              </Box>
              <Button
                variant="outlined"
                size="small"
                startIcon={<DownloadIcon />}
                onClick={handleDataExport}
                disabled={operationLoading === 'export'}
                sx={{ 
                  borderColor: '#00bcd4', 
                  color: '#00bcd4',
                  transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                  '&:hover:not(:disabled)': {
                    borderColor: '#00acc1',
                    backgroundColor: 'rgba(0, 188, 212, 0.1)',
                    transform: 'translateY(-2px)',
                    boxShadow: '0 4px 15px rgba(0, 188, 212, 0.3)',
                  }
                }}
              >
                {operationLoading === 'export' ? 'Exporting...' : 'Export Data'}
              </Button>
            </Box>

            {/* Privacy Policy */}
            <Box 
              display="flex" 
              justifyContent="space-between" 
              alignItems="center"
              sx={{
                transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                '&:hover': {
                  transform: 'translateX(4px)',
                  backgroundColor: 'rgba(0, 188, 212, 0.05)',
                  borderRadius: 1,
                  padding: 1,
                }
              }}
            >
              <Box>
                <GameText variant="body2" className={GAME_CLASSES.textMedium}>
                  Privacy Policy
                </GameText>
                <GameText variant="caption" textVariant="secondary">
                  View our privacy policy and data processing information
                </GameText>
              </Box>
              <Button
                variant="outlined"
                size="small"
                startIcon={<PolicyIcon />}
                component={Link}
                to="/privacy_policy"
                sx={{ 
                  borderColor: '#00bcd4', 
                  color: '#00bcd4',
                  transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                  '&:hover': {
                    borderColor: '#00acc1',
                    backgroundColor: 'rgba(0, 188, 212, 0.1)',
                    transform: 'translateY(-2px)',
                    boxShadow: '0 4px 15px rgba(0, 188, 212, 0.3)',
                  }
                }}
              >
                View Policy
              </Button>
            </Box>
          </Box>
        </Box>
      </GameCard>
      </motion.div>

      {/* Danger Zone */}
      <motion.div
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6, ease: 'easeOut', delay: 2.0 }}
        whileHover={{ y: -2 }}
        style={{ 
          marginTop: '24px',
          transition: 'box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1)' 
        }}
      >
        <GameCard 
          sx={{ 
            border: '2px solid', 
            borderColor: '#f44336',
            transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
            '&:hover': {
              boxShadow: '0 8px 25px rgba(244, 67, 54, 0.15)',
            }
          }}
        >
        <Box className={GAME_CLASSES.padding2}>
          <Box 
            display="flex" 
            alignItems="center" 
            gap={2} 
            className={GAME_CLASSES.marginBottom2}
            sx={{
            }}
          >
            <DeleteForeverIcon 
              sx={{ 
                color: '#f44336',
              }} 
            />
            <GameText 
              variant="h6" 
              textVariant="accent"
              sx={{
              }}
            >
              Danger Zone
            </GameText>
          </Box>

          <GameText 
            variant="body2" 
            textVariant="secondary" 
            paragraph
            sx={{
            }}
          >
            These actions are irreversible. Please proceed with caution.
          </GameText>

          <Box 
            display="flex" 
            justifyContent="space-between" 
            alignItems="center"
            sx={{
              transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
              '&:hover': {
                transform: 'translateX(4px)',
                backgroundColor: 'rgba(244, 67, 54, 0.05)',
                borderRadius: 1,
                padding: 1,
              }
            }}
          >
            <Box>
              <GameText variant="body2" className={GAME_CLASSES.textMedium} textVariant="accent">
                Delete All Data
              </GameText>
              <GameText variant="caption" textVariant="secondary">
                Permanently delete all your personal data and account
              </GameText>
            </Box>
            <Button
              variant="contained"
              size="small"
              startIcon={<DeleteForeverIcon />}
              onClick={() => setDeleteDialogOpen(true)}
              disabled={operationLoading === 'delete'}
              sx={{
                backgroundColor: '#f44336',
                color: '#ffffff',
                transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                '&:hover:not(:disabled)': {
                  backgroundColor: '#d32f2f',
                  transform: 'translateY(-2px)',
                  boxShadow: '0 4px 15px rgba(244, 67, 54, 0.4)',
                },
                '&:active:not(:disabled)': {
                  transform: 'translateY(0)',
                },
                '&:disabled': {
                  backgroundColor: 'rgba(244, 67, 54, 0.3)',
                  color: 'rgba(255, 255, 255, 0.5)',
                }
              }}
            >
              Delete All Data
            </Button>
          </Box>
        </Box>
      </GameCard>
      </motion.div>

      {/* Consent Dialog */}
      <FormDialog<ConsentFormData>
        open={consentDialogOpen}
        onClose={() => setConsentDialogOpen(false)}
        onSubmit={handleConsentChange}
        title={userConsent?.data_processing_consent ? 'Withdraw Consent' : 'Give Consent'}
        description={
          userConsent?.data_processing_consent
            ? 'By withdrawing consent, you revoke permission for us to process your personal data. Some features may become unavailable.'
            : 'By giving consent, you allow us to process your personal data for the purposes outlined in our privacy policy.'
        }
        submitText="Confirm"
        submitColor="primary"
        loading={operationLoading === 'consent'}
        useTanStackForm={true}
        defaultValues={{
          consentValue: !userConsent?.data_processing_consent, // Opposite of current state
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
        title="Delete Account and All Data"
        confirmText="Delete Account"
        confirmColor="error"
        loading={operationLoading === 'delete'}
        disabled={deleteConfirmation !== 'DELETE_ALL_MY_DATA'}
      >
        <DialogContentText paragraph>
          <strong>This action cannot be undone!</strong>
        </DialogContentText>
        <DialogContentText paragraph>
          This will permanently delete your account and all associated data including:
        </DialogContentText>
        <GameText component="ul" variant="body2" textVariant="secondary">
          <li>Your entire account</li>
          <li>Profile information</li>
          <li>Exercise preferences and history</li>
          <li>Workout programs and progress</li>
          <li>Account settings and preferences</li>
        </GameText>
        <DialogContentText paragraph sx={{ mt: 2 }}>
          To confirm account deletion, please type <strong>DELETE_ALL_MY_DATA</strong> below:
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
