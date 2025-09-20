import {
  CheckCircle as CheckCircleIcon,
  Warning as WarningIcon,
  Error as ErrorIcon,
} from '@mui/icons-material';
import { Box, Card, CardContent, Typography, Chip } from '@mui/material';
import React from 'react';

export type StatusLevel = 'excellent' | 'good' | 'fair' | 'needs_attention';

export interface StatusMetric {
  value: number;
  unit: string;
  status: StatusLevel;
  trend: 'up' | 'down' | 'stable';
  lastMeasurement: Date;
}

export interface StatusCardProps {
  title: string;
  status: StatusLevel;
  value?: string | number;
  unit?: string;
  trend?: 'up' | 'down' | 'stable';
  lastUpdated?: Date;
  description?: string;
  onClick?: () => void;
  children?: React.ReactNode;
}

const statusConfig = {
  excellent: {
    color: '#4CAF50',
    icon: CheckCircleIcon,
    label: 'Excellent',
    bgColor: 'rgba(76, 175, 80, 0.1)',
  },
  good: {
    color: '#4CAF50',
    icon: CheckCircleIcon,
    label: 'Good',
    bgColor: 'rgba(76, 175, 80, 0.05)',
  },
  fair: {
    color: '#FF9800',
    icon: WarningIcon,
    label: 'Fair',
    bgColor: 'rgba(255, 152, 0, 0.1)',
  },
  needs_attention: {
    color: '#F44336',
    icon: ErrorIcon,
    label: 'Needs Attention',
    bgColor: 'rgba(244, 67, 54, 0.1)',
  },
};

const trendConfig = {
  up: { color: '#4CAF50', icon: '↗', label: 'Improving' },
  down: { color: '#F44336', icon: '↘', label: 'Declining' },
  stable: { color: '#2196F3', icon: '→', label: 'Stable' },
};

/**
 * StatusCard component for displaying status information with color coding and icons.
 *
 * @param title The title of the status card
 * @param status The status level (excellent, good, fair, needs_attention)
 * @param value Optional value to display
 * @param unit Optional unit for the value
 * @param trend Optional trend indicator
 * @param lastUpdated Optional last updated timestamp
 * @param description Optional description text
 * @param onClick Optional click handler
 * @param children Optional child content
 * @return StatusCard component
 */
export const StatusCard: React.FC<StatusCardProps> = ({
  title,
  status,
  value,
  unit,
  trend,
  lastUpdated,
  description,
  onClick,
  children,
}) => {
  const config = statusConfig[status];
  const IconComponent = config.icon;
  const trendInfo = trend ? trendConfig[trend] : null;

  return (
    <Card
      sx={{
        cursor: onClick ? 'pointer' : 'default',
        transition: 'all 0.2s ease-in-out',
        border: `2px solid ${config.color}`,
        backgroundColor: config.bgColor,
        '&:hover': onClick
          ? {
              transform: 'translateY(-2px)',
              boxShadow: 4,
              backgroundColor: 'background.paper',
            }
          : {},
      }}
      onClick={onClick}
    >
      <CardContent sx={{ p: 2 }}>
        <Box display="flex" alignItems="center" justifyContent="space-between" sx={{ mb: 1 }}>
          <Box display="flex" alignItems="center" gap={1}>
            <IconComponent sx={{ color: config.color, fontSize: 20 }} />
            <Typography variant="h6" fontWeight="medium" color={config.color}>
              {title}
            </Typography>
          </Box>
          <Chip
            label={config.label}
            size="small"
            sx={{
              backgroundColor: config.color,
              color: 'white',
              fontWeight: 'bold',
            }}
          />
        </Box>

        {value !== undefined && (
          <Box sx={{ mb: 1 }}>
            <Typography variant="h4" fontWeight="bold" color="text.primary">
              {value}
              {unit && (
                <Typography component="span" variant="h6" color="text.secondary" sx={{ ml: 0.5 }}>
                  {unit}
                </Typography>
              )}
            </Typography>
          </Box>
        )}

        {trendInfo && (
          <Box display="flex" alignItems="center" gap={0.5} sx={{ mb: 1 }}>
            <Typography
              variant="body2"
              sx={{
                color: trendInfo.color,
                fontWeight: 'medium',
              }}
            >
              {trendInfo.icon} {trendInfo.label}
            </Typography>
          </Box>
        )}

        {description && (
          <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
            {description}
          </Typography>
        )}

        {lastUpdated && (
          <Typography variant="caption" color="text.secondary">
            Updated: {lastUpdated.toLocaleDateString()}
          </Typography>
        )}

        {children}
      </CardContent>
    </Card>
  );
};

/**
 * StatusIndicator component for small status displays.
 *
 * @param status The status level
 * @param size The size of the indicator
 * @param showLabel Whether to show the status label
 * @return StatusIndicator component
 */
export const StatusIndicator: React.FC<{
  status: StatusLevel;
  size?: 'small' | 'medium' | 'large';
  showLabel?: boolean;
}> = ({ status, size = 'medium', showLabel = false }) => {
  const config = statusConfig[status];
  const IconComponent = config.icon;

  const sizeConfig = {
    small: { icon: 16, chip: 'small' as const },
    medium: { icon: 20, chip: 'medium' as const },
    large: { icon: 24, chip: 'medium' as const },
  };

  const currentSize = sizeConfig[size];

  return (
    <Box display="flex" alignItems="center" gap={0.5}>
      <IconComponent sx={{ color: config.color, fontSize: currentSize.icon }} />
      {showLabel && (
        <Chip
          label={config.label}
          size={currentSize.chip}
          sx={{
            backgroundColor: config.color,
            color: 'white',
            fontWeight: 'bold',
            height: 20,
          }}
        />
      )}
    </Box>
  );
};

/**
 * StatusProgress component for displaying progress with status-based coloring.
 *
 * @param value The progress value (0-100)
 * @param status The status level
 * @param label Optional label
 * @param showValue Whether to show the value
 * @return StatusProgress component
 */
export const StatusProgress: React.FC<{
  value: number;
  status: StatusLevel;
  label?: string;
  showValue?: boolean;
}> = ({ value, status, label, showValue = true }) => {
  const config = statusConfig[status];

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" sx={{ mb: 0.5 }}>
        {label && (
          <Typography variant="body2" color="text.secondary">
            {label}
          </Typography>
        )}
        {showValue && (
          <Typography variant="body2" fontWeight="medium" color={config.color}>
            {Math.round(Math.min(100, Math.max(0, value)))}%
          </Typography>
        )}
      </Box>
      <Box
        sx={{
          width: '100%',
          height: 8,
          backgroundColor: 'rgba(0, 0, 0, 0.1)',
          borderRadius: 4,
          overflow: 'hidden',
        }}
      >
        <Box
          sx={{
            width: `${Math.min(100, Math.max(0, value))}%`,
            height: '100%',
            backgroundColor: config.color,
            transition: 'width 0.3s ease-in-out',
          }}
        />
      </Box>
    </Box>
  );
};
