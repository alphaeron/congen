import React, { createContext, useContext, useState, useEffect, useCallback, useMemo } from 'react';

import { useAuth } from './AuthContext';
import {
  getUserExercisePool,
  generateNextWeek,
  updateWorkoutWithOneRepMax as updateWorkoutWithOneRepMaxAPI,
} from '../api/conjugateWorkoutGenerator';
import { getIndividualEquipment, getEquipment as getEquipmentAPI } from '../api/equipment';
import {
  getIndividualExercise,
  getExerciseMuscles,
  getExerciseEquipment,
  getExercises,
} from '../api/exercise';
import { getExerciseEquipment as getExerciseEquipmentBulk } from '../api/exerciseEquipment';
import { getExerciseMuscle } from '../api/exerciseMuscle';
import { getUserDataExport } from '../api/gdpr';
import {
  getConsentStatus,
  recordConsent,
  exportUserData as exportUserDataAPI,
  deleteAllPersonalData as deleteAllPersonalDataAPI,
} from '../api/gdpr';
import { getIndividualMuscle, getMuscles as getMusclesAPI } from '../api/muscle';
import {
  getCurrentPerformanceScores,
  getCurrentPerformanceMetrics,
  getWeeklyTestsInRange,
  submitPerformanceMetrics as submitPerformanceMetricsAPI,
  submitWeeklyTest as submitWeeklyTestAPI,
  getTestProtocols,
  getPerformanceMetricsInRange,
  getPerformanceScoresHistory,
} from '../api/performanceTracking';
import { getProgram as getProgramAPI, getProgramsWithPreferences } from '../api/program';
import {
  createProgrammedExercise as createProgrammedExerciseAPI,
  updateProgrammedExercise as updateProgrammedExerciseAPI,
  deleteProgrammedExercise as deleteProgrammedExerciseAPI,
} from '../api/programmedExercise';
import { getProgrammedWorkouts } from '../api/programmedWorkout';
import { getProgramPreferences } from '../api/programPreferences';
import type {
  UserDataExport,
  ExerciseMuscle,
  UserWeightUnitPreference,
  Exercise,
  ExerciseEquipment,
  Equipment,
  Muscle,
  Program,
  UserEquipment,
  UserWeakMuscle,
  UserExercisePreference,
  ProgramWithPreferences,
  ProgrammedWorkout,
  UserOneRepMax,
  UserConsent,
  UserExercisePoolResponse,
  DashboardStats,
  ProgramPreferences,
  ProgrammedExercise,
  UserPerformanceScores,
  UserPerformanceMetrics,
  UserTestResult,
  TestProtocol,
} from '../api/types';
import { getUserEquipment } from '../api/userEquipment';
import { getUserExercisePreferences } from '../api/userExercisePreference';
import {
  getUserOneRepMaxes,
  upsertUserOneRepMax as upsertUserOneRepMaxAPI,
} from '../api/userOneRepMax';
import { getUserWeakMuscles } from '../api/userWeakMuscle';
import { getUserWeightUnitPreferences } from '../api/userWeightUnitPreference';

interface DataContextType {
  userData: UserDataExport | null;
  exerciseMuscleData: Map<string, string[]>;
  weightUnitPreferences: UserWeightUnitPreference[];
  exerciseData: Map<string, Exercise>;
  exerciseEquipmentData: Map<string, ExerciseEquipment[]>;
  muscleData: Map<string, Muscle>;
  equipmentData: Map<string, Equipment>;
  programData: Map<number, Program>;
  // Bulk data caches
  allExercises: Exercise[];
  allMuscles: Muscle[];
  allEquipment: Equipment[];
  // User-specific data caches
  userEquipment: UserEquipment[];
  userWeakMuscles: UserWeakMuscle[];
  userExercisePreferences: UserExercisePreference[];
  programPreferences: ProgramWithPreferences[];
  programmedWorkouts: ProgrammedWorkout[];
  userOneRepMaxes: UserOneRepMax[];
  // New data types
  userConsent: UserConsent | null;
  userExercisePool: UserExercisePoolResponse | null;
  dashboardStats: DashboardStats | null;
  // Performance tracking data
  performanceScores: UserPerformanceScores | null;
  performanceScoresHistory: UserPerformanceScores[];
  performanceMetrics: UserPerformanceMetrics | null;
  weeklyTests: UserTestResult[];
  testProtocols: TestProtocol[];
  isLoading: boolean;
  error: string | null;
  refreshData: () => Promise<void>;
  isDataStale: boolean;
  isReady: boolean;
  getExercise: (exerciseName: string) => Promise<Exercise | null>;
  getExerciseMuscles: (exerciseName: string) => Promise<ExerciseMuscle[] | null>;
  getExerciseEquipmentData: (exerciseName: string) => Promise<ExerciseEquipment[] | null>;
  getMuscle: (muscleName: string) => Promise<Muscle | null>;
  getEquipment: (equipmentName: string) => Promise<Equipment | null>;
  getProgram: (programId: number) => Promise<Program | null>;
  // Bulk data loading functions
  loadAllExercises: () => Promise<Exercise[]>;
  loadAllMuscles: () => Promise<Muscle[]>;
  loadAllEquipment: () => Promise<Equipment[]>;
  loadAllExerciseMuscleData: () => Promise<Map<string, string[]>>;
  loadAllExerciseEquipmentData: () => Promise<Map<string, ExerciseEquipment[]>>;
  // User-specific data loading functions
  loadUserEquipment: () => Promise<UserEquipment[]>;
  loadUserWeakMuscles: () => Promise<UserWeakMuscle[]>;
  loadUserExercisePreferences: () => Promise<UserExercisePreference[]>;
  loadProgramPreferences: () => Promise<ProgramWithPreferences[]>;
  loadProgrammedWorkouts: () => Promise<ProgrammedWorkout[]>;
  loadUserOneRepMaxes: () => Promise<UserOneRepMax[]>;
  upsertUserOneRepMax: (
    exerciseName: string,
    oneRepMax: number,
    unit: string
  ) => Promise<UserOneRepMax>;
  // New data loading functions
  loadUserConsent: () => Promise<UserConsent | null>;
  loadUserExercisePool: () => Promise<UserExercisePoolResponse | null>;
  loadDashboardStats: () => Promise<DashboardStats | null>;
  updateUserConsent: (consent: boolean) => Promise<UserConsent>;
  // Performance tracking data loading functions
  loadPerformanceScores: () => Promise<UserPerformanceScores | null>;
  loadPerformanceMetrics: () => Promise<UserPerformanceMetrics | null>;
  loadPerformanceMetricsInRange: (
    startDate: string,
    endDate: string
  ) => Promise<UserPerformanceMetrics[]>;
  loadWeeklyTests: (startDate?: string, endDate?: string) => Promise<UserTestResult[]>;
  loadTestProtocols: () => Promise<TestProtocol[]>;
  refreshPerformanceData: () => Promise<void>;
  // Performance tracking utility functions
  getCurrentWeekTest: () => UserTestResult[] | null;
  getPerformanceDataForDateRange: (
    startDate: string,
    endDate: string
  ) => Promise<{
    scores: UserPerformanceScores | null;
    metrics: UserPerformanceMetrics | null;
    tests: UserTestResult[];
  }>;
  // Performance tracking mutation functions
  submitPerformanceMetrics: (
    metrics: Omit<UserPerformanceMetrics, 'keycloak_id' | 'created_at' | 'updated_at'>
  ) => Promise<UserPerformanceScores>;
  submitWeeklyTest: (
    testResult: Omit<UserTestResult, 'id' | 'keycloak_id' | 'created_at' | 'updated_at'>
  ) => Promise<UserTestResult[]>;
  // GDPR functions
  exportUserData: () => Promise<UserDataExport>;
  deleteAllPersonalData: (confirmationText: string) => Promise<void>;
  // Programmed exercise functions
  createProgrammedExercise: (
    workoutStageId: number,
    exerciseName: string,
    position: number,
    notes?: string,
    totalSets?: number,
    targetWeight?: number,
    targetReps?: number,
    restSeconds?: number,
    performedWeight?: number,
    performedReps?: number,
    tempo?: string,
    isAmrap?: boolean,
    isEmom?: boolean
  ) => Promise<ProgrammedExercise>;
  updateProgrammedExercise: (
    id: number,
    workoutStageId: number,
    exerciseName: string,
    position: number,
    notes?: string
  ) => Promise<ProgrammedExercise>;
  deleteProgrammedExercise: (id: number) => Promise<void>;
  getProgramPreferencesById: (programId: number) => Promise<ProgramPreferences | null>;
  // Workout generation functions
  generateWorkout: (programId: number) => Promise<Program>;
  updateWorkoutWithOneRepMax: (programId: number) => Promise<Program>;
  // Bulk exercise data loading for components that need all exercises
  loadAllExercisesForComponents: () => Promise<Map<string, Exercise>>;
  // Cache invalidation and refresh strategies
  invalidateCache: (cacheType?: string) => void;
  refreshSpecificData: (dataType: string) => Promise<void>;
  // Loading states and error handling
  isLoadingSpecific: (dataType: string) => boolean;
  getErrorForDataType: (dataType: string) => string | null;
  clearError: () => void;
}

export const DataContext = createContext<DataContextType | undefined>(undefined);

interface DataProviderProps {
  children: React.ReactNode;
}

export const DataProvider: React.FC<DataProviderProps> = ({ children }) => {
  const { user, isLoading: authIsLoading, isAuthenticated } = useAuth();
  const [userData, setUserData] = useState<UserDataExport | null>(null);
  const [exerciseMuscleData, setExerciseMuscleData] = useState<Map<string, string[]>>(new Map());
  const [weightUnitPreferences, setWeightUnitPreferences] = useState<UserWeightUnitPreference[]>(
    []
  );
  const [exerciseData, setExerciseData] = useState<Map<string, Exercise>>(new Map());
  const [exerciseEquipmentData, setExerciseEquipmentData] = useState<
    Map<string, ExerciseEquipment[]>
  >(new Map());
  const [muscleData, setMuscleData] = useState<Map<string, Muscle>>(new Map());
  const [equipmentData, setEquipmentData] = useState<Map<string, Equipment>>(new Map());
  const [programData, setProgramData] = useState<Map<number, Program>>(new Map());
  // Bulk data caches
  const [allExercises, setAllExercises] = useState<Exercise[]>([]);
  const [allMuscles, setAllMuscles] = useState<Muscle[]>([]);
  const [allEquipment, setAllEquipment] = useState<Equipment[]>([]);
  // User-specific data caches
  const [userEquipment, setUserEquipment] = useState<UserEquipment[]>([]);
  const [userWeakMuscles, setUserWeakMuscles] = useState<UserWeakMuscle[]>([]);
  const [userExercisePreferences, setUserExercisePreferences] = useState<UserExercisePreference[]>(
    []
  );
  const [programPreferences, setProgramPreferences] = useState<ProgramWithPreferences[]>([]);
  const [programmedWorkouts, setProgrammedWorkouts] = useState<ProgrammedWorkout[]>([]);
  const [userOneRepMaxes, setUserOneRepMaxes] = useState<UserOneRepMax[]>([]);
  // New data caches
  const [userConsent, setUserConsent] = useState<UserConsent | null>(null);
  const [userExercisePool, setUserExercisePool] = useState<UserExercisePoolResponse | null>(null);
  const [dashboardStats, setDashboardStats] = useState<DashboardStats | null>(null);
  // Performance tracking data
  const [performanceScores, setPerformanceScores] = useState<UserPerformanceScores | null>(null);
  const [performanceScoresHistory, setPerformanceScoresHistory] = useState<UserPerformanceScores[]>(
    []
  );
  const [performanceMetrics, setPerformanceMetrics] = useState<UserPerformanceMetrics | null>(null);
  const [weeklyTests, setWeeklyTests] = useState<UserTestResult[]>([]);
  const [testProtocols, setTestProtocols] = useState<TestProtocol[]>([]);
  // Additional data caches
  // Centralized exercise data cache for components
  const [allExercisesMap, setAllExercisesMap] = useState<Map<string, Exercise>>(new Map());
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lastFetchTime, setLastFetchTime] = useState<number>(0);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [isLoadingData, setIsLoadingData] = useState(false);
  // Specific loading states for different data types
  const [loadingStates, setLoadingStates] = useState<Map<string, boolean>>(new Map());
  // Specific error states for different data types
  const [errorStates, setErrorStates] = useState<Map<string, string>>(new Map());
  // Removed offline support - not implementing
  // Basic caching features
  const [cacheTimestamps, setCacheTimestamps] = useState<Map<string, number>>(new Map());

  // Data is considered stale after 5 minutes
  const DATA_STALE_THRESHOLD = 5 * 60 * 1000;
  const isDataStale = Date.now() - lastFetchTime > DATA_STALE_THRESHOLD;

  // Basic caching configuration
  const CACHE_TTL = 10 * 60 * 1000; // 10 minutes TTL

  const loadData = useCallback(
    async (forceRefresh = false) => {
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
        const results = await Promise.allSettled([
          getUserDataExport({ forceRefresh }),
          getExerciseMuscle({ forceRefresh }),
          getUserWeightUnitPreferences(user.keycloak_id, { forceRefresh }),
          getCurrentPerformanceScores({ forceRefresh }),
          getPerformanceScoresHistory(
            undefined, // No start date - get all history
            undefined, // No end date - get all history
            { forceRefresh }
          ),
          getCurrentPerformanceMetrics({ forceRefresh }),
          getWeeklyTestsInRange(
            undefined, // No start date - get all history
            undefined, // No end date - get all history
            { forceRefresh }
          ),
        ]);

        const dataExport = results[0].status === 'fulfilled' ? results[0].value : null;
        const exerciseMuscleData = results[1].status === 'fulfilled' ? results[1].value : [];
        const weightUnitPreferencesData = results[2].status === 'fulfilled' ? results[2].value : [];
        const performanceScoresData = results[3].status === 'fulfilled' ? results[3].value : null;
        const performanceScoresHistoryData =
          results[4].status === 'fulfilled' ? results[4].value : [];
        const performanceMetricsData = results[5].status === 'fulfilled' ? results[5].value : null;
        const weeklyTestsData = results[6].status === 'fulfilled' ? results[6].value : [];

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
        setPerformanceScores(performanceScoresData);
        setPerformanceScoresHistory(performanceScoresHistoryData);
        setPerformanceMetrics(performanceMetricsData);
        setWeeklyTests(weeklyTestsData);

        setLastFetchTime(Date.now());
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to load data';
        setError(errorMessage);
      } finally {
        setIsLoading(false);
        setIsRefreshing(false);
        setIsLoadingData(false);
      }
    },
    [user?.keycloak_id, userData, isDataStale, isRefreshing, isLoadingData]
  );

  const refreshData = useCallback(async () => {
    await loadData(true);
  }, [loadData]);

  // Exercise data caching functions
  const getExercise = useCallback(
    async (exerciseName: string): Promise<Exercise | null> => {
      // Check if we already have this exercise cached
      if (exerciseData.has(exerciseName)) {
        return exerciseData.get(exerciseName) || null;
      }

      try {
        const exercise = await getIndividualExercise(exerciseName);
        setExerciseData(prev => new Map(prev).set(exerciseName, exercise));
        return exercise;
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to load exercise data';
        setError(errorMessage);
        return null;
      }
    },
    [exerciseData]
  );

  const getExerciseEquipmentData = useCallback(
    async (exerciseName: string): Promise<ExerciseEquipment[] | null> => {
      // Check if we already have this exercise equipment cached
      if (exerciseEquipmentData.has(exerciseName)) {
        return exerciseEquipmentData.get(exerciseName) || null;
      }

      try {
        const equipment = await getExerciseEquipment(exerciseName);
        setExerciseEquipmentData(prev => new Map(prev).set(exerciseName, equipment));
        return equipment;
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : 'Failed to load exercise equipment data';
        setError(errorMessage);
        return null;
      }
    },
    [exerciseEquipmentData]
  );

  const getMuscle = useCallback(
    async (muscleName: string): Promise<Muscle | null> => {
      // Check if we already have this muscle cached
      if (muscleData.has(muscleName)) {
        return muscleData.get(muscleName) || null;
      }

      try {
        const muscle = await getIndividualMuscle(muscleName);
        setMuscleData(prev => new Map(prev).set(muscleName, muscle));
        return muscle;
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to load muscle data';
        setError(errorMessage);
        return null;
      }
    },
    [muscleData]
  );

  const getEquipment = useCallback(
    async (equipmentName: string): Promise<Equipment | null> => {
      // Check if we already have this equipment cached
      if (equipmentData.has(equipmentName)) {
        return equipmentData.get(equipmentName) || null;
      }

      try {
        const equipment = await getIndividualEquipment(equipmentName);
        setEquipmentData(prev => new Map(prev).set(equipmentName, equipment));
        return equipment;
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to load equipment data';
        setError(errorMessage);
        return null;
      }
    },
    [equipmentData]
  );

  const getProgram = useCallback(
    async (programId: number): Promise<Program | null> => {
      // Check if we already have this program cached
      if (programData.has(programId)) {
        return programData.get(programId) || null;
      }

      try {
        const program = await getProgramAPI(programId);
        setProgramData(prev => new Map(prev).set(programId, program));
        return program;
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to load program data';
        setError(errorMessage);
        return null;
      }
    },
    [programData]
  );

  // Exercise muscles caching function
  const getExerciseMusclesData = useCallback(
    async (exerciseName: string): Promise<ExerciseMuscle[] | null> => {
      try {
        const exerciseMuscles = await getExerciseMuscles(exerciseName);
        return exerciseMuscles;
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : 'Failed to load exercise muscles data';
        setError(errorMessage);
        return null;
      }
    },
    []
  );

  // Helper function to set loading state for specific data type
  const setLoadingState = useCallback((dataType: string, isLoading: boolean) => {
    setLoadingStates(prev => new Map(prev).set(dataType, isLoading));
  }, []);

  // Advanced caching utilities
  const isCacheValid = useCallback(
    (cacheKey: string): boolean => {
      const timestamp = cacheTimestamps.get(cacheKey);
      if (!timestamp) return false;
      return Date.now() - timestamp < CACHE_TTL;
    },
    [cacheTimestamps]
  );

  // Optimized bulk data loading with batching
  const loadAllExercises = useCallback(async (): Promise<Exercise[]> => {
    if (allExercises.length > 0 && isCacheValid('exercises')) {
      return allExercises;
    }

    try {
      setLoadingState('exercises', true);
      const exercises = await getExercises();
      setAllExercises(exercises);
      setCacheTimestamps(prev => new Map(prev).set('exercises', Date.now()));
      return exercises;
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to load exercises data';
      setError(errorMessage);
      return [];
    } finally {
      setLoadingState('exercises', false);
    }
  }, [allExercises, isCacheValid, setLoadingState]);

  const loadAllMuscles = useCallback(async (): Promise<Muscle[]> => {
    if (allMuscles.length > 0 && isCacheValid('muscles')) {
      return allMuscles;
    }

    try {
      setLoadingState('muscles', true);
      const muscles = await getMusclesAPI();
      setAllMuscles(muscles);
      setCacheTimestamps(prev => new Map(prev).set('muscles', Date.now()));
      return muscles;
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to load muscles data';
      setError(errorMessage);
      return [];
    } finally {
      setLoadingState('muscles', false);
    }
  }, [allMuscles, isCacheValid, setLoadingState]);

  const loadAllEquipment = useCallback(async (): Promise<Equipment[]> => {
    if (allEquipment.length > 0 && isCacheValid('equipment')) {
      return allEquipment;
    }

    try {
      setLoadingState('equipment', true);
      const equipment = await getEquipmentAPI();
      setAllEquipment(equipment);
      setCacheTimestamps(prev => new Map(prev).set('equipment', Date.now()));
      return equipment;
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to load equipment data';
      setError(errorMessage);
      return [];
    } finally {
      setLoadingState('equipment', false);
    }
  }, [allEquipment, isCacheValid, setLoadingState]);

  const loadAllExerciseMuscleData = useCallback(async (): Promise<Map<string, string[]>> => {
    if (exerciseMuscleData.size > 0 && isCacheValid('exerciseMuscleData')) {
      return exerciseMuscleData;
    }

    try {
      setLoadingState('exerciseMuscleData', true);
      const exerciseMuscleDataRaw = await getExerciseMuscle();

      // Convert to Map format
      const muscleMap = new Map<string, string[]>();
      exerciseMuscleDataRaw.forEach((item: ExerciseMuscle) => {
        const existing = muscleMap.get(item.exercise_name) || [];
        existing.push(item.muscle_name);
        muscleMap.set(item.exercise_name, existing);
      });

      setExerciseMuscleData(muscleMap);
      setCacheTimestamps(prev => new Map(prev).set('exerciseMuscleData', Date.now()));
      return muscleMap;
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : 'Failed to load exercise muscle data';
      setError(errorMessage);
      return new Map();
    } finally {
      setLoadingState('exerciseMuscleData', false);
    }
  }, [exerciseMuscleData, isCacheValid, setLoadingState]);

  const loadAllExerciseEquipmentData = useCallback(async (): Promise<
    Map<string, ExerciseEquipment[]>
  > => {
    if (exerciseEquipmentData.size > 0 && isCacheValid('exerciseEquipmentData')) {
      return exerciseEquipmentData;
    }

    try {
      setLoadingState('exerciseEquipmentData', true);
      const exerciseEquipmentDataRaw = await getExerciseEquipmentBulk();

      // Convert to Map format
      const equipmentMap = new Map<string, ExerciseEquipment[]>();
      exerciseEquipmentDataRaw.forEach((item: ExerciseEquipment) => {
        const existing = equipmentMap.get(item.exercise_name) || [];
        existing.push(item);
        equipmentMap.set(item.exercise_name, existing);
      });

      setExerciseEquipmentData(equipmentMap);
      setCacheTimestamps(prev => new Map(prev).set('exerciseEquipmentData', Date.now()));
      return equipmentMap;
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : 'Failed to load exercise equipment data';
      setError(errorMessage);
      return new Map();
    } finally {
      setLoadingState('exerciseEquipmentData', false);
    }
  }, [exerciseEquipmentData, isCacheValid, setLoadingState]);

  // User-specific data loading functions
  const loadUserEquipment = useCallback(async (): Promise<UserEquipment[]> => {
    if (!user?.keycloak_id) return [];

    if (userEquipment.length > 0) {
      return userEquipment;
    }

    try {
      const equipment = await getUserEquipment(user.keycloak_id);
      setUserEquipment(equipment);
      return equipment;
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : 'Failed to load user equipment data';
      setError(errorMessage);
      return [];
    }
  }, [user?.keycloak_id, userEquipment]);

  const loadUserWeakMuscles = useCallback(async (): Promise<UserWeakMuscle[]> => {
    if (!user?.keycloak_id) return [];

    if (userWeakMuscles.length > 0) {
      return userWeakMuscles;
    }

    try {
      const weakMuscles = await getUserWeakMuscles(user.keycloak_id);
      setUserWeakMuscles(weakMuscles);
      return weakMuscles;
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : 'Failed to load user weak muscles data';
      setError(errorMessage);
      return [];
    }
  }, [user?.keycloak_id, userWeakMuscles]);

  const loadUserExercisePreferences = useCallback(async (): Promise<UserExercisePreference[]> => {
    if (!user?.keycloak_id) return [];

    if (userExercisePreferences.length > 0) {
      return userExercisePreferences;
    }

    try {
      const preferences = await getUserExercisePreferences(user.keycloak_id);
      setUserExercisePreferences(preferences);
      return preferences;
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : 'Failed to load user exercise preferences data';
      setError(errorMessage);
      return [];
    }
  }, [user?.keycloak_id, userExercisePreferences]);

  const loadProgramPreferences = useCallback(async (): Promise<ProgramWithPreferences[]> => {
    if (programPreferences.length > 0) {
      return programPreferences;
    }

    try {
      const preferences = await getProgramsWithPreferences();
      setProgramPreferences(preferences);
      return preferences;
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : 'Failed to load program preferences data';
      setError(errorMessage);
      return [];
    }
  }, [programPreferences]);

  const loadUserOneRepMaxes = useCallback(async (): Promise<UserOneRepMax[]> => {
    if (!user?.keycloak_id) return [];

    if (userOneRepMaxes.length > 0) {
      return userOneRepMaxes;
    }

    try {
      const oneRepMaxes = await getUserOneRepMaxes(user.keycloak_id);
      setUserOneRepMaxes(oneRepMaxes);
      return oneRepMaxes;
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : 'Failed to load user one rep max data';
      setError(errorMessage);
      return [];
    }
  }, [user?.keycloak_id, userOneRepMaxes]);

  const upsertUserOneRepMax = useCallback(
    async (exerciseName: string, oneRepMax: number, unit: string): Promise<UserOneRepMax> => {
      if (!user?.keycloak_id) {
        throw new Error('User not authenticated');
      }

      try {
        const result = await upsertUserOneRepMaxAPI(
          user.keycloak_id,
          exerciseName,
          oneRepMax,
          unit
        );

        await loadData(true);
        setUserOneRepMaxes(prev => {
          const without = prev.filter(orm => orm.exercise_name !== exerciseName);
          return [...without, result].sort((a, b) =>
            a.exercise_name.localeCompare(b.exercise_name)
          );
        });
        return result;
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to save one rep max';
        setError(errorMessage);
        throw err;
      }
    },
    [user?.keycloak_id, loadData]
  );

  // New data loading functions
  const loadUserConsent = useCallback(async (): Promise<UserConsent | null> => {
    if (!user?.keycloak_id) return null;

    if (userConsent) {
      return userConsent;
    }

    try {
      const consent = await getConsentStatus();
      setUserConsent(consent);
      setCacheTimestamps(prev => new Map(prev).set('userConsent', Date.now()));
      return consent;
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to load user consent data';
      setError(errorMessage);
      return null;
    }
  }, [user?.keycloak_id, userConsent]);

  const loadUserExercisePool = useCallback(async (): Promise<UserExercisePoolResponse | null> => {
    if (!user?.keycloak_id) return null;

    if (userExercisePool) {
      return userExercisePool;
    }

    try {
      const pool = await getUserExercisePool();
      setUserExercisePool(pool);
      setCacheTimestamps(prev => new Map(prev).set('userExercisePool', Date.now()));
      return pool;
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : 'Failed to load user exercise pool data';
      setError(errorMessage);
      return null;
    }
  }, [user?.keycloak_id, userExercisePool]);

  const loadDashboardStats = useCallback(async (): Promise<DashboardStats | null> => {
    if (!user?.keycloak_id) return null;

    if (dashboardStats) {
      return dashboardStats;
    }

    try {
      // Calculate dashboard stats from existing data
      const stats: DashboardStats = {
        total_workouts: programmedWorkouts.length,
        current_week: Math.max(0, ...programmedWorkouts.map(w => Math.ceil(w.day_number / 7) || 0)),
        recent_one_rep_maxes: userOneRepMaxes.slice(-5), // Last 5 1RM records
      };
      setDashboardStats(stats);
      setCacheTimestamps(prev => new Map(prev).set('dashboardStats', Date.now()));
      return stats;
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to load dashboard stats';
      setError(errorMessage);
      return null;
    }
  }, [user?.keycloak_id, dashboardStats, programmedWorkouts, userOneRepMaxes]);

  // Performance tracking data loading functions
  const loadPerformanceScores = useCallback(async (): Promise<UserPerformanceScores | null> => {
    if (!user?.keycloak_id) return null;

    if (performanceScores) {
      return performanceScores;
    }

    try {
      const scores = await getCurrentPerformanceScores();
      setPerformanceScores(scores);
      setCacheTimestamps(prev => new Map(prev).set('performanceScores', Date.now()));
      return scores;
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to load performance scores';
      setError(errorMessage);
      return null;
    }
  }, [user?.keycloak_id, performanceScores]);

  const loadPerformanceMetrics = useCallback(async (): Promise<UserPerformanceMetrics | null> => {
    if (!user?.keycloak_id) return null;

    if (performanceMetrics) {
      return performanceMetrics;
    }

    try {
      const metrics = await getCurrentPerformanceMetrics();
      setPerformanceMetrics(metrics);
      setCacheTimestamps(prev => new Map(prev).set('performanceMetrics', Date.now()));
      return metrics;
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : 'Failed to load performance metrics';
      setError(errorMessage);
      return null;
    }
  }, [user?.keycloak_id, performanceMetrics]);

  const loadPerformanceMetricsInRange = useCallback(
    async (startDate: string, endDate: string): Promise<UserPerformanceMetrics[]> => {
      if (!user?.keycloak_id) return [];

      try {
        const metrics = await getPerformanceMetricsInRange(startDate, endDate);
        return metrics;
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : 'Failed to load performance metrics in range';
        setError(errorMessage);
        return [];
      }
    },
    [user?.keycloak_id]
  );

  const loadWeeklyTests = useCallback(
    async (startDate?: string, endDate?: string): Promise<UserTestResult[]> => {
      if (!user?.keycloak_id) return [];

      // Use default date range if not provided
      const start = startDate || new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString(); // 30 days ago
      const end = endDate || new Date().toISOString(); // today

      try {
        const tests = await getWeeklyTestsInRange(start, end);
        setWeeklyTests(tests);
        setCacheTimestamps(prev => new Map(prev).set('weeklyTests', Date.now()));
        return tests;
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to load weekly tests';
        setError(errorMessage);
        return [];
      }
    },
    [user?.keycloak_id]
  );

  const loadTestProtocols = useCallback(async (): Promise<TestProtocol[]> => {
    if (testProtocols.length > 0) {
      return testProtocols;
    }

    try {
      const protocols = await getTestProtocols();
      setTestProtocols(protocols);
      setCacheTimestamps(prev => new Map(prev).set('testProtocols', Date.now()));
      return protocols;
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to load test protocols';
      setError(errorMessage);
      return [];
    }
  }, [testProtocols]);

  const refreshPerformanceData = useCallback(async (): Promise<void> => {
    if (!user?.keycloak_id) return;

    try {
      // Load all performance data in parallel
      const [scores, metrics, tests] = await Promise.all([
        getCurrentPerformanceScores().catch(() => null),
        getCurrentPerformanceMetrics().catch(() => null),
        getWeeklyTestsInRange(
          new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString(), // 30 days ago
          new Date().toISOString() // today
        ).catch(() => []),
      ]);

      setPerformanceScores(scores);
      setPerformanceMetrics(metrics);
      setWeeklyTests(tests);
      setCacheTimestamps(prev => {
        const newMap = new Map(prev);
        newMap.set('performanceScores', Date.now());
        newMap.set('performanceMetrics', Date.now());
        newMap.set('weeklyTests', Date.now());
        return newMap;
      });
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : 'Failed to refresh performance data';
      setError(errorMessage);
    }
  }, [user?.keycloak_id]);

  // Performance tracking utility functions
  const getCurrentWeekTest = useCallback((): UserTestResult[] | null => {
    if (!weeklyTests || weeklyTests.length === 0) return null;

    // Calculate current week start (Monday)
    const now = new Date();
    const dayOfWeek = now.getDay();
    const daysToMonday = dayOfWeek === 0 ? -6 : 1 - dayOfWeek; // Sunday = 0, Monday = 1
    const currentWeekStart = new Date(now);
    currentWeekStart.setDate(now.getDate() + daysToMonday);
    currentWeekStart.setHours(0, 0, 0, 0);

    // Filter tests for the current week
    return weeklyTests.filter(test => {
      const testWeekStart = new Date(test.week_start_timestamp);
      testWeekStart.setHours(0, 0, 0, 0);
      return testWeekStart.getTime() === currentWeekStart.getTime();
    });
  }, [weeklyTests]);

  const getPerformanceDataForDateRange = useCallback(
    async (startDate: string, endDate: string) => {
      if (!user?.keycloak_id) {
        return {
          scores: null,
          metrics: null,
          tests: [],
        };
      }

      try {
        // Load data for the specific date range
        const [scores, metrics, tests] = await Promise.all([
          getCurrentPerformanceScores().catch(() => null),
          getCurrentPerformanceMetrics().catch(() => null),
          getWeeklyTestsInRange(startDate, endDate).catch(() => []),
        ]);

        return {
          scores,
          metrics,
          tests,
        };
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : 'Failed to load performance data for date range';
        setError(errorMessage);
        return {
          scores: null,
          metrics: null,
          tests: [],
        };
      }
    },
    [user?.keycloak_id]
  );

  // Performance tracking mutation functions
  const submitPerformanceMetrics = useCallback(
    async (
      metrics: Omit<UserPerformanceMetrics, 'keycloak_id' | 'created_at' | 'updated_at'>
    ): Promise<UserPerformanceScores> => {
      if (!user?.keycloak_id) {
        throw new Error('User not authenticated');
      }

      try {
        const result = await submitPerformanceMetricsAPI(metrics);

        // Refresh performance data after successful submission
        await refreshPerformanceData();

        return result;
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : 'Failed to submit performance metrics';
        setError(errorMessage);
        throw err;
      }
    },
    [user?.keycloak_id, refreshPerformanceData]
  );

  const submitWeeklyTest = useCallback(
    async (
      testResult: Omit<UserTestResult, 'id' | 'keycloak_id' | 'created_at' | 'updated_at'>
    ): Promise<UserTestResult[]> => {
      if (!user?.keycloak_id) {
        throw new Error('User not authenticated');
      }

      try {
        const result = await submitWeeklyTestAPI(testResult);

        // Refresh performance data after successful submission
        await refreshPerformanceData();

        return result;
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to submit weekly test';
        setError(errorMessage);
        throw err;
      }
    },
    [user?.keycloak_id, refreshPerformanceData]
  );

  const updateUserConsent = useCallback(
    async (consent: boolean): Promise<UserConsent> => {
      if (!user?.keycloak_id) {
        throw new Error('User not authenticated');
      }

      try {
        const updatedConsent = await recordConsent(consent);
        setUserConsent(updatedConsent);
        setCacheTimestamps(prev => new Map(prev).set('userConsent', Date.now()));
        return updatedConsent;
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to update user consent';
        setError(errorMessage);
        throw err;
      }
    },
    [user?.keycloak_id]
  );

  // GDPR functions
  const exportUserData = useCallback(async (): Promise<UserDataExport> => {
    try {
      const data = await exportUserDataAPI();
      return data;
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to export user data';
      setError(errorMessage);
      throw err;
    }
  }, []);

  const deleteAllPersonalData = useCallback(
    async (confirmationText: string): Promise<void> => {
      try {
        await deleteAllPersonalDataAPI(confirmationText);

        // Refresh data to ensure all components have the latest data
        await loadData(true);
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to delete personal data';
        setError(errorMessage);
        throw err;
      }
    },
    [loadData]
  );

  // Programmed exercise functions
  const createProgrammedExercise = useCallback(
    async (
      workoutStageId: number,
      exerciseName: string,
      position: number,
      notes?: string,
      totalSets?: number,
      targetWeight?: number,
      targetReps?: number,
      restSeconds?: number,
      performedWeight?: number,
      performedReps?: number,
      tempo?: string,
      isAmrap?: boolean,
      isEmom?: boolean
    ): Promise<ProgrammedExercise> => {
      try {
        const exercise = await createProgrammedExerciseAPI(
          workoutStageId,
          exerciseName,
          position,
          notes,
          totalSets,
          targetWeight,
          targetReps,
          restSeconds,
          performedWeight,
          performedReps,
          tempo,
          isAmrap,
          isEmom
        );

        // Refresh data to ensure all components have the latest data
        await loadData(true);
        return exercise;
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : 'Failed to create programmed exercise';
        setError(errorMessage);
        throw err;
      }
    },
    [loadData]
  );

  const updateProgrammedExercise = useCallback(
    async (
      id: number,
      workoutStageId: number,
      exerciseName: string,
      position: number,
      notes?: string
    ): Promise<ProgrammedExercise> => {
      try {
        const exercise = await updateProgrammedExerciseAPI(
          id,
          workoutStageId,
          exerciseName,
          position,
          notes
        );

        // Refresh data to ensure all components have the latest data
        await loadData(true);
        return exercise;
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : 'Failed to update programmed exercise';
        setError(errorMessage);
        throw err;
      }
    },
    [loadData]
  );

  const deleteProgrammedExercise = useCallback(
    async (id: number): Promise<void> => {
      try {
        await deleteProgrammedExerciseAPI(id);

        // Refresh data to ensure all components have the latest data
        await loadData(true);
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : 'Failed to delete programmed exercise';
        setError(errorMessage);
        throw err;
      }
    },
    [loadData]
  );

  const getProgramPreferencesById = useCallback(
    async (programId: number): Promise<ProgramPreferences | null> => {
      try {
        const preferences = await getProgramPreferences(programId);
        return preferences;
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : 'Failed to load program preferences';
        setError(errorMessage);
        return null;
      }
    },
    []
  );

  // Workout generation functions
  const generateWorkout = useCallback(
    async (programId: number): Promise<Program> => {
      try {
        const program = await generateNextWeek(programId);
        // Refresh data after generating workout to ensure all components have latest data
        await loadData(true);
        return program;
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to generate workout';
        setError(errorMessage);
        throw err;
      }
    },
    [loadData]
  );

  const updateWorkoutWithOneRepMax = useCallback(
    async (programId: number): Promise<Program> => {
      try {
        const program = await updateWorkoutWithOneRepMaxAPI(programId);
        // Refresh data after updating workout to ensure all components have latest data
        await loadData(true);
        return program;
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : 'Failed to update workout with 1RM data';
        setError(errorMessage);
        throw err;
      }
    },
    [loadData]
  );

  const loadProgrammedWorkouts = useCallback(async (): Promise<ProgrammedWorkout[]> => {
    if (programmedWorkouts.length > 0) {
      return programmedWorkouts;
    }

    try {
      const workouts = await getProgrammedWorkouts();
      setProgrammedWorkouts(workouts);
      return workouts;
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : 'Failed to load programmed workouts data';
      setError(errorMessage);
      return [];
    }
  }, [programmedWorkouts]);

  // Bulk exercise data loading for components that need all exercises
  const loadAllExercisesForComponents = useCallback(async (): Promise<Map<string, Exercise>> => {
    if (allExercisesMap.size > 0) {
      return allExercisesMap;
    }

    try {
      // Load all exercises and create a map for efficient lookup
      const exercises = await loadAllExercises();
      const exerciseMap = new Map<string, Exercise>();
      exercises.forEach(exercise => {
        exerciseMap.set(exercise.name, exercise);
      });
      setAllExercisesMap(exerciseMap);
      return exerciseMap;
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : 'Failed to load exercises for components';
      setError(errorMessage);
      return new Map();
    }
  }, [allExercisesMap, loadAllExercises]);

  // Cache invalidation and refresh strategies
  const invalidateCache = useCallback((cacheType?: string) => {
    if (!cacheType || cacheType === 'all') {
      // Clear all caches
      setExerciseData(new Map());
      setExerciseEquipmentData(new Map());
      setMuscleData(new Map());
      setEquipmentData(new Map());
      setProgramData(new Map());
      setAllExercises([]);
      setAllMuscles([]);
      setAllEquipment([]);
      setUserEquipment([]);
      setUserWeakMuscles([]);
      setUserExercisePreferences([]);
      setProgramPreferences([]);
      setProgrammedWorkouts([]);
      setUserOneRepMaxes([]);
      setAllExercisesMap(new Map());
    } else {
      // Clear specific cache type
      switch (cacheType) {
        case 'exercises':
          setExerciseData(new Map());
          setAllExercises([]);
          setAllExercisesMap(new Map());
          break;
        case 'muscles':
          setMuscleData(new Map());
          setAllMuscles([]);
          break;
        case 'equipment':
          setEquipmentData(new Map());
          setAllEquipment([]);
          break;
        case 'programs':
          setProgramData(new Map());
          setProgramPreferences([]);
          setProgrammedWorkouts([]);
          break;
        case 'userData':
          setUserEquipment([]);
          setUserWeakMuscles([]);
          setUserExercisePreferences([]);
          setUserOneRepMaxes([]);
          break;
      }
    }
  }, []);

  const refreshSpecificData = useCallback(
    async (dataType: string) => {
      try {
        switch (dataType) {
          case 'exercises':
            setAllExercises([]);
            setAllExercisesMap(new Map());
            await loadAllExercises();
            break;
          case 'muscles':
            setAllMuscles([]);
            await loadAllMuscles();
            break;
          case 'equipment':
            setAllEquipment([]);
            await loadAllEquipment();
            break;
          case 'programs':
            setProgramPreferences([]);
            setProgrammedWorkouts([]);
            await loadProgramPreferences();
            await loadProgrammedWorkouts();
            break;
          case 'userData':
            setUserEquipment([]);
            setUserWeakMuscles([]);
            setUserExercisePreferences([]);
            setUserOneRepMaxes([]);
            await loadUserEquipment();
            await loadUserWeakMuscles();
            await loadUserExercisePreferences();
            await loadUserOneRepMaxes();
            break;
          case 'all':
            await refreshData();
            break;
        }
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : `Failed to refresh ${dataType} data`;
        setError(errorMessage);
      }
    },
    [
      loadAllExercises,
      loadAllMuscles,
      loadAllEquipment,
      loadAllExerciseMuscleData,
      loadAllExerciseEquipmentData,
      loadProgramPreferences,
      loadProgrammedWorkouts,
      loadUserEquipment,
      loadUserWeakMuscles,
      loadUserExercisePreferences,
      loadUserOneRepMaxes,
      refreshData,
    ]
  );

  // Loading states and error handling
  const isLoadingSpecific = useCallback(
    (dataType: string): boolean => {
      return loadingStates.get(dataType) || false;
    },
    [loadingStates]
  );

  const getErrorForDataType = useCallback(
    (dataType: string): string | null => {
      return errorStates.get(dataType) || null;
    },
    [errorStates]
  );

  const clearError = useCallback(() => {
    setError(null);
    setErrorStates(new Map());
  }, []);

  // Load data on mount and when user changes
  useEffect(() => {
    if (user?.keycloak_id && !authIsLoading) {
      loadData();
    } else if (!user?.keycloak_id && !authIsLoading) {
      // Clear data when user is not authenticated
      setUserData(null);
      setExerciseMuscleData(new Map());
      setWeightUnitPreferences([]);
      setExerciseData(new Map());
      setExerciseEquipmentData(new Map());
      setMuscleData(new Map());
      setEquipmentData(new Map());
      setProgramData(new Map());
      setAllExercises([]);
      setAllMuscles([]);
      setAllEquipment([]);
      setUserEquipment([]);
      setUserWeakMuscles([]);
      setUserExercisePreferences([]);
      setProgramPreferences([]);
      setProgrammedWorkouts([]);
      setUserOneRepMaxes([]);
      setUserConsent(null);
      setUserExercisePool(null);
      setDashboardStats(null);
      setPerformanceScores(null);
      setPerformanceScoresHistory([]);
      setPerformanceMetrics(null);
      setWeeklyTests([]);
      setTestProtocols([]);
      setAllExercisesMap(new Map());
      setError(null);
      setIsLoading(false);
    }
  }, [user?.keycloak_id, authIsLoading, loadData]);

  // Auto-refresh stale data when components request it
  useEffect(() => {
    if (isDataStale && userData && !authIsLoading) {
      loadData(true);
    }
  }, [isDataStale, userData, authIsLoading, loadData]);

  // Memoize the context value to prevent unnecessary re-renders
  const value: DataContextType = useMemo(
    () => ({
      userData,
      exerciseMuscleData,
      weightUnitPreferences,
      exerciseData,
      exerciseEquipmentData,
      muscleData,
      equipmentData,
      programData,
      allExercises,
      allMuscles,
      allEquipment,
      userEquipment,
      userWeakMuscles,
      userExercisePreferences,
      programPreferences,
      programmedWorkouts,
      userOneRepMaxes,
      userConsent,
      userExercisePool,
      dashboardStats,
      performanceScores,
      performanceScoresHistory,
      performanceMetrics,
      weeklyTests,
      testProtocols,
      isLoading,
      error,
      refreshData,
      isDataStale,
      isReady: isAuthenticated && !!user?.keycloak_id && !authIsLoading,
      getExercise,
      getExerciseMuscles: getExerciseMusclesData,
      getExerciseEquipmentData,
      getMuscle,
      getEquipment,
      getProgram,
      loadAllExercises,
      loadAllMuscles,
      loadAllEquipment,
      loadAllExerciseMuscleData,
      loadAllExerciseEquipmentData,
      loadUserEquipment,
      loadUserWeakMuscles,
      loadUserExercisePreferences,
      loadProgramPreferences,
      loadProgrammedWorkouts,
      loadUserOneRepMaxes,
      upsertUserOneRepMax,
      loadUserConsent,
      loadUserExercisePool,
      loadDashboardStats,
      loadPerformanceScores,
      loadPerformanceMetrics,
      loadPerformanceMetricsInRange,
      loadWeeklyTests,
      loadTestProtocols,
      refreshPerformanceData,
      getCurrentWeekTest,
      getPerformanceDataForDateRange,
      submitPerformanceMetrics,
      submitWeeklyTest,
      updateUserConsent,
      exportUserData,
      deleteAllPersonalData,
      createProgrammedExercise,
      updateProgrammedExercise,
      deleteProgrammedExercise,
      getProgramPreferencesById,
      generateWorkout,
      updateWorkoutWithOneRepMax,
      loadAllExercisesForComponents,
      invalidateCache,
      refreshSpecificData,
      isLoadingSpecific,
      getErrorForDataType,
      clearError,
    }),
    [
      userData,
      exerciseMuscleData,
      weightUnitPreferences,
      exerciseData,
      exerciseEquipmentData,
      muscleData,
      equipmentData,
      programData,
      allExercises,
      allMuscles,
      allEquipment,
      userEquipment,
      userWeakMuscles,
      userExercisePreferences,
      programPreferences,
      programmedWorkouts,
      userOneRepMaxes,
      userConsent,
      userExercisePool,
      dashboardStats,
      isLoading,
      error,
      refreshData,
      isDataStale,
      isAuthenticated,
      user?.keycloak_id,
      authIsLoading,
      getExercise,
      getExerciseMusclesData,
      getExerciseEquipmentData,
      getMuscle,
      getEquipment,
      getProgram,
      loadAllExercises,
      loadAllMuscles,
      loadAllEquipment,
      loadAllExerciseMuscleData,
      loadAllExerciseEquipmentData,
      loadUserEquipment,
      loadUserWeakMuscles,
      loadUserExercisePreferences,
      loadProgramPreferences,
      loadProgrammedWorkouts,
      loadUserOneRepMaxes,
      upsertUserOneRepMax,
      loadUserConsent,
      loadUserExercisePool,
      loadDashboardStats,
      loadPerformanceScores,
      loadPerformanceMetrics,
      loadPerformanceMetricsInRange,
      loadWeeklyTests,
      loadTestProtocols,
      refreshPerformanceData,
      getCurrentWeekTest,
      getPerformanceDataForDateRange,
      submitPerformanceMetrics,
      submitWeeklyTest,
      updateUserConsent,
      exportUserData,
      deleteAllPersonalData,
      createProgrammedExercise,
      updateProgrammedExercise,
      deleteProgrammedExercise,
      getProgramPreferencesById,
      generateWorkout,
      updateWorkoutWithOneRepMax,
      loadAllExercisesForComponents,
      invalidateCache,
      refreshSpecificData,
      isLoadingSpecific,
      getErrorForDataType,
      clearError,
    ]
  );

  return <DataContext.Provider value={value}>{children}</DataContext.Provider>;
};

export const useData = (): DataContextType => {
  const context = useContext(DataContext);
  if (context === undefined) {
    throw new Error('useData must be used within a DataProvider');
  }
  return context;
};
