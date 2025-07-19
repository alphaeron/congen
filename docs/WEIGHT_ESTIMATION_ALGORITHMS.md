# Weight Estimation Algorithms

This document describes the weight estimation algorithms used in the Congen Conjugate Workout Generator, including the 1RM calculator process, exercise matching algorithm, and reference exercise detection system.

## Overview

The weight estimation system is designed to provide accurate weight recommendations for exercises when users don't have a 1RM (One Rep Maximum) for a specific exercise. It uses intelligent algorithms to match exercises to reference lifts and estimate appropriate weights based on movement patterns, equipment, and user data.

## 1RM Calculator Process

### OneRepMaxCalculatorService

The `OneRepMaxCalculatorService` provides multiple formulas for calculating 1RM from submaximal performance data.

#### Available Formulas

1. **Epley Formula** (Default)
   - Formula: `weight × (1 + reps/30)`
   - Best for: 1-10 reps, compound movements
   - Accuracy: High for moderate rep ranges

2. **Brzycki Formula**
   - Formula: `weight × (36 / (37 - reps))`
   - Best for: 1-10 reps, general use
   - Accuracy: Good for lower rep ranges

3. **Lombardi Formula**
   - Formula: `weight × (reps^0.1)`
   - Best for: 1-10 reps, powerlifting
   - Accuracy: Good for strength-focused training

4. **O'Conner Formula**
   - Formula: `weight × (1 + reps/40)`
   - Best for: 1-10 reps, conservative estimates
   - Accuracy: Conservative, good for beginners

5. **Wathan Formula**
   - Formula: `weight × (100 / (102.78 - 2.78 × reps))`
   - Best for: 1-10 reps, scientific approach
   - Accuracy: Based on extensive research

#### Usage

```kotlin
val calculator = OneRepMaxCalculatorService()

// Calculate 1RM using Epley formula (default)
val oneRepMax = calculator.calculateOneRepMax(
    weight = BigDecimal("185"),
    reps = 5
)

// Calculate 1RM using specific formula
val oneRepMax = calculator.calculateOneRepMax(
    weight = BigDecimal("185"),
    reps = 5,
    formula = OneRepMaxFormula.BRZYCKI
)
```

#### Formula Selection Logic

The service automatically selects the best formula based on:
- **Rep range**: Different formulas work better for different rep ranges
- **Exercise type**: Compound vs isolation movements
- **User experience level**: Conservative vs aggressive estimates

## Exercise Matching Algorithm

### ExerciseMatchingService

The `ExerciseMatchingService` finds the best reference exercise for weight estimation when a user doesn't have a 1RM for a specific exercise.

#### Similarity Metrics

The algorithm uses multiple similarity factors to determine the best reference exercise:

1. **Name Similarity** (40% weight)
   - Uses Levenshtein distance to compare exercise names
   - Accounts for variations like "Bench Press" vs "Incline Bench Press"

2. **Movement Pattern Similarity** (30% weight)
   - Compares MovementType classifications
   - Ensures exercises use similar movement patterns

3. **Equipment Similarity** (20% weight)
   - Uses Jaccard index to compare required equipment
   - Prioritizes exercises with similar equipment requirements

4. **Muscle Group Similarity** (10% weight)
   - Uses Jaccard index to compare target muscle groups
   - Ensures exercises target similar muscle groups

#### Algorithm Process

```kotlin
fun findBestReferenceExercise(
    targetExercise: Exercise,
    allExercises: List<Exercise>,
    exerciseEquipmentMap: Map<String, List<ExerciseEquipment>>,
    exerciseMuscleMap: Map<String, List<ExerciseMuscle>>,
    userOneRepMaxes: List<UserOneRepMax> = emptyList()
): ExerciseMatch
```

1. **Movement Pattern Classification**
   - Classifies the target exercise's movement pattern
   - Uses MovementType enum and name-based fallback classification

2. **Reference Exercise Detection**
   - Uses ReferenceExerciseDetector to find potential reference exercises
   - Filters by movement pattern compatibility

3. **Similarity Calculation**
   - Calculates overall similarity score using weighted factors
   - Returns the exercise with the highest similarity score

4. **Fallback Handling**
   - If no suitable reference exercise is found, uses movement pattern-based fallback
   - Provides conservative estimates for unknown exercises

#### Weight Estimation from Reference

```kotlin
fun estimateWeightFromReference(
    targetExercise: Exercise,
    referenceExercise: Exercise,
    referenceOneRepMax: BigDecimal,
    similarityScore: Double
): BigDecimal
```

1. **Base Percentage Calculation**
   - Determines base percentage based on exercise name patterns
   - Accounts for exercise variations (incline, decline, dumbbell, etc.)

2. **Similarity Adjustment**
   - Adjusts percentage based on similarity score
   - Higher similarity = closer to base percentage
   - Lower similarity = more conservative estimate

3. **Final Weight Calculation**
   - Applies adjusted percentage to reference 1RM
   - Rounds to appropriate weight increments

## Reference Exercise Detection

### ReferenceExerciseDetector

The `ReferenceExerciseDetector` intelligently identifies the best reference exercises from the available exercise database.

#### Detection Criteria

The detector scores exercises based on multiple factors:

1. **Equipment Preference** (30% weight)
   - Barbell exercises: 1.0 (highest priority)
   - Dumbbell exercises: 0.7
   - Machine/Cable exercises: 0.4
   - Bodyweight exercises: 0.6

2. **Movement Pattern Purity** (25% weight)
   - Standard compound movements: 1.0
   - Variations (incline, decline): 0.6-0.9
   - Isolation exercises: 0.5

3. **User 1RM Availability** (25% weight)
   - User has 1RM for exercise: 1.0
   - User doesn't have 1RM: 0.0
   - Prioritizes exercises the user can actually perform

4. **Exercise Usage Frequency** (10% weight)
   - High usage (>100 times): 1.0
   - Medium usage (20-100 times): 0.6-0.8
   - Low usage (<20 times): 0.2-0.4

5. **Name Clarity** (10% weight)
   - Clear, standard names: 1.0
   - Descriptive names: 0.8-0.9
   - Ambiguous names: 0.4-0.6

#### Detection Process

```kotlin
fun findBestReferenceExercises(
    allExercises: List<Exercise>,
    userOneRepMaxes: List<UserOneRepMax> = emptyList(),
    exerciseUsageCounts: Map<String, Int> = emptyMap()
): List<Exercise>
```

1. **Candidate Generation**
   - Evaluates all exercises for reference potential
   - Calculates comprehensive score for each exercise

2. **Score Calculation**
   - Combines all factors using weighted average
   - Only includes exercises with score > 0.5

3. **Ranking and Selection**
   - Sorts exercises by score (descending)
   - Returns top candidates for each movement pattern

#### Pattern Recognition

The detector recognizes reference exercise patterns:

- **Squat Patterns**: "back squat", "squat", "safety bar squat"
- **Bench Patterns**: "bench press", "flat bench"
- **Deadlift Patterns**: "deadlift", "conventional deadlift"
- **Overhead Patterns**: "overhead press", "strict press", "military press"
- **Compound Movements**: "row", "dip", "pull up"

## Weight Estimation Examples

### Example 1: User has 1RM for Bench Press

```
Target Exercise: Incline Bench Press
Reference Exercise: Bench Press (user has 1RM: 225 lbs)
Similarity Score: 0.85

Calculation:
- Base percentage: 80% (incline variation)
- Adjusted percentage: 80% × (0.8 + 0.85 × 0.4) = 80% × 1.14 = 91.2%
- Estimated weight: 225 × 0.912 = 205.2 lbs
- Rounded weight: 205 lbs
```

### Example 2: User has no 1RM, uses exercise matching

```
Target Exercise: Front Squat
Available Reference: Safety Bar Squat (user has 1RM: 315 lbs)
Similarity Score: 0.75

Calculation:
- Base percentage: 85% (front squat variation)
- Adjusted percentage: 85% × (0.8 + 0.75 × 0.4) = 85% × 1.1 = 93.5%
- Estimated weight: 315 × 0.935 = 294.5 lbs
- Rounded weight: 295 lbs
```

### Example 3: Isolation exercise with bodyweight estimation

```
Target Exercise: Barbell Curl
Reference Type: Bodyweight/Isolation
User Bodyweight: 70 kg

Calculation:
- Bodyweight percentage: 20% (curl pattern)
- Estimated weight: 70 × 0.2 = 14 kg
- Rounded weight: 14 kg
```

## Integration with Workout Generation

### WorkoutStageGenerator Integration

The weight estimation system integrates with the workout generation process:

1. **Primary Exercise Selection**
   - Uses user's 1RM data when available
   - Falls back to exercise matching for missing 1RMs

2. **Dynamic Effort Calculations**
   - Applies percentage-based calculations for DE workouts
   - Uses band weight calculations for accommodating resistance

3. **Accessory Exercise Estimation**
   - Uses bodyweight-based calculations for isolation exercises
   - Applies conservative estimates for unknown exercises

### Error Handling

The system includes robust error handling:

- **No 1RM Available**: Uses exercise matching with fallback
- **No Reference Exercise**: Uses conservative bodyweight estimates
- **Invalid Data**: Returns safe default values
- **Missing Equipment**: Adjusts calculations based on available equipment

## Configuration and Customization

### Formula Selection

Users can configure which 1RM formula to use:

```kotlin
// In application.properties
congen.one-rep-max.formula=EPLEY
```

### Similarity Weights

The similarity calculation weights can be adjusted:

```kotlin
// In ExerciseMatchingService
private val SIMILARITY_WEIGHTS = mapOf(
    "name" to 0.4,
    "movement" to 0.3,
    "equipment" to 0.2,
    "muscle" to 0.1
)
```

### Reference Exercise Thresholds

The reference exercise detection thresholds can be customized:

```kotlin
// In ReferenceExerciseDetector
private const val MIN_REFERENCE_SCORE = 0.5
private const val HIGH_USAGE_THRESHOLD = 100
private const val MEDIUM_USAGE_THRESHOLD = 20
```

## Performance Considerations

### Caching

The system implements caching for:
- Exercise similarity calculations
- Reference exercise detection results
- 1RM calculations

### Database Optimization

- Indexes on exercise names and movement types
- Efficient queries for equipment and muscle relationships
- Batch processing for large exercise databases

### Memory Management

- Lazy loading of exercise relationships
- Efficient data structures for similarity calculations
- Garbage collection optimization for large datasets

## Future Enhancements

### Machine Learning Integration

- Train models on user performance data
- Improve similarity calculations based on actual usage
- Personalized weight estimation based on user patterns

### Advanced Pattern Recognition

- Natural language processing for exercise names
- Video analysis for movement pattern classification
- Equipment detection from exercise descriptions

### Real-time Adaptation

- Learn from user feedback on weight estimates
- Adjust algorithms based on success rates
- Continuous improvement of estimation accuracy

---

*This documentation covers the weight estimation algorithms as implemented in version 1.0.0 of the Congen Conjugate Workout Generator.* 