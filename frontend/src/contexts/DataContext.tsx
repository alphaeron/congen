import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useAuth } from './AuthContext';
import { getUserDataExport } from '../api/gdpr';
import { getExerciseMuscle } from '../api/exerciseMuscle';
import { getUserWeightUnitPreferences } from '../api/userWeightUnitPreference';
import type { UserDataExport, ExerciseMuscle, UserWeightUnitPreference } from '../api/types';

interface DataContextType {
  userData: UserDataExport | null;
  exerciseMuscleData: Map<string, string[]>;
  weightUnitPreferences: UserWeightUnitPreference[];
  isLoading: boolean;
  error: string | null;
  refreshData: () => Promise<void>;
  isDataStale: boolean;
}

const DataContext = createContext<DataContextType | undefined>(undefined);

interface DataProviderProps {
  children: React.ReactNode;
}

export const DataProvider: React.FC<DataProviderProps> = ({ children }) => {
  const { user } = useAuth();
  const [userData, setUserData] = useState<UserDataExport | null>(null);
  const [exerciseMuscleData, setExerciseMuscleData] = useState<Map<string, string[]>>(new Map());
  const [weightUnitPreferences, setWeightUnitPreferences] = useState<UserWeightUnitPreference[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lastFetchTime, setLastFetchTime] = useState<number>(0);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [isLoadingData, setIsLoadingData] = useState(false);

  // Data is considered stale after 5 minutes
  const DATA_STALE_THRESHOLD = 5 * 60 * 1000;
  const isDataStale = Date.now() - lastFetchTime > DATA_STALE_THRESHOLD;

  const loadData = useCallback(async (forceRefresh = false) => {
    if (!user?.keycloak_id) return;

    // Don't reload if data is fresh and not forcing refresh
    if (!forceRefresh && userData && !isDataStale && !isRefreshing) {
      return;
    }

    // Prevent multiple simultaneous loads
    if (isLoadingData) {
      return;
    }

    setIsLoadingData(true);
    try {
      if (forceRefresh) {
        setIsRefreshing(true);
      } else {
        setIsLoading(true);
      }
      setError(null);

      // Load all data in parallel to minimize API calls
      const [dataExport, exerciseMuscleData, weightUnitPreferencesData] = await Promise.all([
        getUserDataExport({ forceRefresh }),
        getExerciseMuscle({ forceRefresh }),
        getUserWeightUnitPreferences(user.keycloak_id, { forceRefresh }),
      ]);

      // Convert exercise muscle data to Map for efficient lookup
      const muscleMap = new Map<string, string[]>();
      exerciseMuscleData.forEach((item: ExerciseMuscle) => {
        const existing = muscleMap.get(item.exercise_name) || [];
        existing.push(item.muscle_name);
        muscleMap.set(item.exercise_name, existing);
      });

      setUserData(dataExport);
      setExerciseMuscleData(muscleMap);
      setWeightUnitPreferences(weightUnitPreferencesData || []);
      setLastFetchTime(Date.now());
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to load data';
      setError(errorMessage);
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
      setIsLoadingData(false);
    }
  }, [user?.keycloak_id, userData, isDataStale, isRefreshing, isLoadingData]);

  const refreshData = useCallback(async () => {
    await loadData(true);
  }, [loadData]);

  // Load data on mount and when user changes
  useEffect(() => {
    loadData();
  }, [loadData]);

  // Auto-refresh stale data when components request it
  useEffect(() => {
    if (isDataStale && userData) {
      loadData(true);
    }
  }, [isDataStale, userData, loadData]);

  const value: DataContextType = {
    userData,
    exerciseMuscleData,
    weightUnitPreferences,
    isLoading,
    error,
    refreshData,
    isDataStale,
  };

  return <DataContext.Provider value={value}>{children}</DataContext.Provider>;
};

export const useData = (): DataContextType => {
  const context = useContext(DataContext);
  if (context === undefined) {
    throw new Error('useData must be used within a DataProvider');
  }
  return context;
};
