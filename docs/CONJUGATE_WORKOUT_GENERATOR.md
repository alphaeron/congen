# Conjugate Workout Generator

## Overview

The Conjugate Workout Generator is a service that implements the Westside Barbell conjugate method for powerlifting, incorporating undulating periodization, and personalized programming based on user preferences and available equipment.

## System Architecture

The service is built with a modular architecture that provides clear separation of concerns:

### Package Structure

```
src/main/kotlin/com/congen/generator/
├── ConjugateModels.kt                           # Data classes and constants
├── ConjugateTemplates.kt                        # Workout templates and selection logic
├── PrilepinGuidelinesService.kt                 # Prilepin chart and periodization logic
├── ExerciseSelectionService.kt                  # Exercise selection and filtering logic
├── WorkoutStageGenerator.kt                     # Individual stage generation helpers
├── SessionTimeCalculator.kt                     # Session time allocation utilities
├── WeightSelectionService.kt                    # Weight calculation and estimation
├── ExerciseMatchingService.kt                   # Exercise similarity and matching
├── ReferenceExerciseDetector.kt                 # Dynamic reference exercise detection
├── SupportedEquipmentWeightRoundingService.kt  # Equipment-based weight rounding
├── MovementBalanceService.kt                    # Movement balance constraints and scoring
└── BandWeightService.kt                         # Band weight calculations for DE
```

### Component Responsibilities

#### 1. ConjugateModels.kt
- **Purpose**: Centralized data classes and constants
- **Contains**:
  - `DayTemplate` data class
  - `PrilepinGuidelines` data class
  - `ConjugateConstants` object with default values and time allocations
- **Benefits**: Eliminates code duplication and provides single source of truth

#### 2. ConjugateTemplates.kt
- **Purpose**: Manages workout templates and template selection logic
- **Responsibilities**:
  - Template selection based on days per week (2, 3, or 4)
  - Helper methods for determining secondary movement and conditioning inclusion
- **Benefits**: Isolates template logic and makes it easily testable

#### 3. PrilepinGuidelinesService.kt
- **Purpose**: Handles all Prilepin chart guidelines and undulating periodization
- **Responsibilities**:
  - Prilepin chart constants and guidelines
  - Undulating periodization logic (4-week cycles)
  - Max Effort, Dynamic Effort, and Accessory guidelines
- **Benefits**: Centralizes complex periodization logic in one place

#### 4. ExerciseSelectionService.kt
- **Purpose**: Manages exercise selection based on various criteria
- **Responsibilities**:
  - User preference filtering
  - Equipment availability checking
  - Weak muscle determination
  - Exercise filtering utilities
  - Secondary exercise similarity selection
  - Movement balance state integration
- **Benefits**: Separates exercise selection logic from workout generation

#### 5. WorkoutStageGenerator.kt
- **Purpose**: Generates individual workout stages and their components
- **Responsibilities**:
  - Workout stage creation
  - Programmed exercise creation
  - Set scheme generation (Prilepin-based, secondary, AMRAP/EMOM)
  - Target weight calculation integration
- **Benefits**: Focuses on stage-level generation details

#### 6. SessionTimeCalculator.kt
- **Purpose**: Handles session time calculations and accessory exercise count
- **Responsibilities**:
  - Time allocation calculations
  - Accessory exercise count determination
  - Utility methods for time management
- **Benefits**: Isolates time-related logic for easy modification

#### 7. WeightSelectionService.kt
- **Purpose**: Handles weight calculation and estimation for exercises
- **Responsibilities**:
  - Weight calculation based on user's 1RM and intensity
  - Weight estimation for exercises without 1RM using similar exercises
  - Dynamic effort weight calculations with bands
  - Bodyweight exercise estimations
  - Unit conversion and weight rounding integration
- **Benefits**: Centralizes all weight-related logic and provides intelligent fallbacks

#### 8. ExerciseMatchingService.kt
- **Purpose**: Automatically matches exercises to reference lifts using multiple similarity metrics
- **Responsibilities**:
  - Name similarity analysis
  - Movement pattern classification
  - Equipment similarity matching
  - Muscle group overlap analysis
  - Weight estimation from reference exercises
- **Benefits**: Provides intelligent weight estimation for exercises without 1RM data

#### 9. ReferenceExerciseDetector.kt
- **Purpose**: Dynamically identifies the best reference exercises from the database
- **Responsibilities**:
  - Equipment preference scoring (barbell > dumbbell > machine)
  - Movement pattern purity analysis
  - User 1RM data integration
  - Exercise popularity/usage tracking
  - Name clarity assessment
- **Benefits**: Replaces hardcoded reference exercise names with intelligent detection

#### 10. SupportedEquipmentWeightRoundingService.kt
- **Purpose**: Rounds weights to match available plate and equipment sizes
- **Responsibilities**:
  - Barbell weight rounding using standard plates
  - Kettlebell weight matching to standard sizes
  - Dumbbell weight rounding to standard increments
  - Equipment-specific weight calculations
- **Benefits**: Ensures calculated weights can be achieved with standard gym equipment

#### 11. MovementBalanceService.kt
- **Purpose**: Manages movement balance constraints and scoring for workout generation
- **Responsibilities**:
  - Movement balance state tracking across workout stages
  - Movement type scoring and constraint evaluation
  - Balance constraint enforcement (vertical_push ↔ horizontal_pull, horizontal_push ↔ vertical_pull)
  - Pull-to-push volume ratio maintenance (2:1 ratio)
  - Soft constraint implementation that doesn't block workout generation
- **Benefits**: Ensures balanced movement patterns and prevents overemphasis on specific movement types

#### 12. BandWeightService.kt
- **Purpose**: Handles band weight calculations for dynamic effort exercises
- **Responsibilities**:
  - Band weight distribution by week
  - Band selection based on target weight
  - Bar weight calculation
  - Unit conversion for band calculations
- **Benefits**: Provides accurate accommodated resistance for DE movements

### Main Service (ConjugateWorkoutGeneratorService.kt)

The main service is a lightweight orchestrator that:
- Coordinates between the modular components
- Handles the high-level workflow
- Manages data flow between components
- Provides program-based generation (works with existing programs)
- Integrates movement balance state tracking across workout generation

## Conjugate Method Principles

The conjugate method, developed by Louie Simmons at Westside Barbell, combines:

- **Max Effort (ME)**: Heavy singles, doubles, or triples at 85-92% 1RM
- **Dynamic Effort (DE)**: Speed work at 60-70% 1RM with explosive intent
- **Accessory Work**: Targeted muscle development and weak point training


## Program Structure

### 2-Day Programs
Condensed conjugate approach with combined ME+DE days:
- Day 1: ME Upper + DE Lower (combined workout)
- Day 2: ME Lower + DE Upper (combined workout)

Each day combines both Max Effort and Dynamic Effort movements in a single session, providing a time-efficient approach to conjugate training.

### 3-Day Programs
Condensed conjugate with combined days plus additional volume on third day:
- Day 1: ME Upper + DE Lower (combined workout)
- Day 2: ME Lower + DE Upper (combined workout)
- Day 3: Full Body Dynamic Effort (DE Upper + DE Lower)

The third day provides additional dynamic effort work with both upper and lower body movements, increasing training frequency while maintaining the conjugate structure.

### 4-Day Programs
Extended conjugate with traditional split:
- Day 1: ME Upper (horizontal push + vertical push)
- Day 2: DE Lower (squat + hinge)
- Day 3: ME Lower (squat + hinge)
- Day 4: DE Upper (horizontal push + vertical pull)

The traditional 4-day split provides maximum volume and specialization for each movement pattern.

## Exercise Categories

- **Primary**: Main compound movements (squat, bench, deadlift variations) - `isAccessory = false`
- **Secondary**: Additional primary movements (different from the main ME/DE exercise) - `isAccessory = false`
- **Accessory**: Isolation and weak point training - `isAccessory = true`
- **Conditioning**: Cardio and recovery work - `isAccessory = true`

## Exercise Selection Algorithm

The service uses a sophisticated algorithm to select exercises with movement balance constraints:

### Movement Balance Constraints

The system implements soft constraints to ensure balanced movement patterns across workouts:

#### Balance Pairs
- **Vertical Push ↔ Horizontal Pull**: These movements should be balanced to prevent overemphasis on either
- **Horizontal Push ↔ Vertical Pull**: These movements should be balanced to maintain overall push/pull equilibrium

#### Volume Ratio
- **Pull-to-Push Ratio**: Pull exercise volume should be approximately twice that of push exercises (2:1 ratio)
- This ratio helps prevent muscle imbalances and promotes balanced development

#### Soft Constraint Implementation
- Movement balance constraints are **soft constraints** that influence exercise selection but don't block workout generation
- If balance constraints cannot be fully met, the system will still generate a complete workout
- The system tracks movement balance state across workout stages to make informed decisions

### Primary Exercise Selection
1. **Filter by Exercise Type**: Select ME/DE exercises (`is_accessory = false`)
2. **Filter by Workout Type**: Match exercises to specific workout types (ME/DE)
3. **Filter by User Preferences**: Exclude exercises the user wants to avoid
4. **Filter by Equipment**: Only include exercises the user can perform with available equipment
5. **Movement Balance Scoring**: Score exercises based on current movement balance state
6. **Sort by Priority**: Sort by movement balance score, equipment options, targeted muscles, and exercise name

### Secondary Exercise Selection
Secondary exercises are selected to be similar to the primary exercise in terms of movement type and muscles worked, while considering movement balance:

1. **Similarity-Based Selection**: Uses a scoring algorithm to find exercises most similar to the primary movement
2. **Movement Type Matching**: Prioritizes exercises with the same movement type (e.g., "horizontal push")
3. **Muscle Overlap Analysis**: Calculates muscle overlap between primary and potential secondary exercises
4. **Movement Category Similarity**: Provides partial credit for related movement types:
   - Same category (push/pull): 50 points
   - Same plane (horizontal/vertical): 25 points
   - Same body part focus (upper/lower): 15 points
5. **Movement Balance Scoring**: Scores exercises based on current movement balance state and balance constraints
6. **User Preferences & Equipment**: Applies the same filtering as primary exercises

#### Similarity Scoring Algorithm
The system calculates a similarity score for each potential secondary exercise:

- **Movement Type Match**: 100 points for exact match
- **Muscle Overlap**: Up to 50 points based on percentage of primary muscles targeted
- **Movement Balance Score**: Up to 30 points based on how well the exercise balances current movement patterns

The exercise with the highest total score is selected as the secondary movement.

### Accessory Exercise Selection
1. **Filter by Exercise Type**: Select accessory exercises (`is_accessory = true`)
2. **Filter by User Preferences**: Exclude exercises the user wants to avoid
3. **Filter by Equipment**: Only include exercises the user can perform with available equipment
4. **Movement Balance Scoring**: Score exercises based on current movement balance state
5. **Sort by Priority**: Sort by movement balance score, equipment options, targeted muscles, and exercise name

## Weight Calculation and Estimation

The system provides intelligent weight calculation and estimation through multiple services:

### Weight Selection Service
- **Direct 1RM Usage**: Uses actual 1RM values when available
- **Exercise Matching**: Estimates weights for exercises without 1RM using similar exercises
- **Bodyweight Estimation**: Provides conservative estimates for bodyweight/isolation exercises
- **Dynamic Effort Integration**: Handles band weight calculations for DE movements
- **Unit Conversion**: Supports both pounds and kilograms with user preferences

### Exercise Matching Service
- **Multi-Metric Similarity**: Combines name similarity, movement pattern, equipment, and muscle groups
- **Reference Exercise Detection**: Dynamically identifies best reference exercises
- **Weight Estimation**: Calculates estimated weights based on reference exercise and similarity score
- **Fallback Mechanisms**: Provides conservative estimates when no good matches are found

### Reference Exercise Detector
- **Equipment Preference**: Prioritizes barbell > dumbbell > machine exercises
- **Pattern Purity**: Scores exercises based on movement pattern clarity
- **User Data Integration**: Prioritizes exercises the user has 1RM data for

- **Name Clarity**: Prefers clear, standard exercise names

### Supported Equipment Weight Rounding
- **Barbell Exercises**: Rounds to achievable weights using standard plates (2.5, 5, 10, 25, 35, 45 lbs)
- **Kettlebell Exercises**: Matches to standard kettlebell weights
- **Dumbbell Exercises**: Rounds to nearest 5lb/2.5kg increment
- **Equipment Detection**: Automatically detects required equipment from exercise data

## Session Time Management

The service uses dynamic time calculation to determine the number of accessory exercises based on actual set schemes:

### Time Calculation Formula
- **Exercise time**: `num_sets * (rest_seconds + reps_per_set * 6)`
  - Where 6 seconds is the estimated time per repetition
- **Primary movement**: Calculated from actual set schemes
- **Secondary movement**: Calculated from actual set schemes
- **Each accessory exercise**: 5 minutes
- **Conditioning**: 10 minutes (for DE days)

### Dynamic Allocation Logic
1. Calculate actual time taken by primary and secondary movements
2. Determine remaining time for accessories: `sessionTimeMinutes - (primaryTime + secondaryTime)/60`
3. If conditioning is included, subtract 10 minutes from remaining time
4. If no time left for accessories, skip conditioning and use that time for accessories instead
5. Calculate number of accessories: `remainingTime / 5 minutes`

### Examples
- **ME Upper with 3 sets of 5 reps, 180s rest**: 3 * (180 + 5*6) = 630s (10.5 min) for primary
- **Secondary with 4 sets of 6 reps, 240s rest**: 4 * (240 + 6*6) = 1104s (18.4 min) for secondary
- **60-minute session**: 60 - 10.5 - 18.4 - 10 = 21.1 minutes → 4 accessories

This approach ensures more accurate time allocation based on the actual workout structure rather than fixed estimates.



## Prilepin's Chart Integration

The service uses updated Prilepin's Chart guidelines for different intensity ranges:

### Intensity Ranges
- **55-65% intensity**: 3-6 reps per set, 24 total reps, 60-90 second rest
- **70-80% intensity**: 3-6 reps per set, 18 total reps, 90-120 second rest
- **80-90% intensity**: 2-4 reps per set, 15 total reps, 180-300 second rest
- **90-100% intensity**: 1-2 reps per set, 4 total reps, 180-300 second rest

## Undulating Periodization

The system implements undulating periodization over a 4-week cycle to optimize strength development and prevent plateaus:

### Max Effort Movements
- **Week 1-2**: 80-90% intensity (build phase)
- **Week 3**: 90-100% intensity (peak phase)
  - Upper body: maximum 95% intensity
  - Lower body: up to 100% intensity
- **Week 4**: 55-65% intensity (deload week)

### Dynamic Effort Movements

#### Lower Body
- **Week 1**: 12 sets of 2 reps or 5 sets of 5 reps, 75% intensity
- **Week 2**: 10 sets of 2 reps or 5 sets of 5 reps, 80% intensity
- **Week 3**: 8 sets of 2 reps or 5 sets of 5 reps, 85% intensity
- **Week 4**: 12 sets of 2 reps or 5 sets of 5 reps, 50% intensity (deload)

#### Upper Body
- **Week 1**: 9 sets of 3 reps, 50% intensity + bands
- **Week 2**: 9 sets of 3 reps, 55% intensity + bands
- **Week 3**: 9 sets of 3 reps, 60% intensity + bands
- **Week 4**: 9 sets of 3 reps, 50% intensity, no bands (deload)

### Dynamic Effort Weight Computation

Dynamic Effort exercises use accommodated resistance with bands to provide progressive resistance during the concentric phase. The weight distribution follows specific undulating periodization guidelines:

#### Band Weight Distribution by Week
- **Week 1**: 25% band weight, 50% bar weight
- **Week 2**: 25% band weight, 55% bar weight
- **Week 3**: 25% band weight, 60% bar weight
- **Week 4**: 0% band weight, 50% bar weight (deload)

#### Available Band Weights
- **Black**: 100 lbs
- **Green**: 65 lbs
- **Blue**: 50 lbs
- **Red**: 30 lbs
- **Orange**: 15 lbs

#### Weight Computation Process
1. **Calculate Total Target Weight**: Based on user's 1RM and exercise intensity
2. **Determine Band Weight Percentage**: Based on week in the 4-week cycle
3. **Select Appropriate Bands**: Choose the band pair whose total weight (2 × individual band weight) is closest to the target band weight, minimizing the absolute difference
4. **Calculate Bar Weight**: Subtract total band weight from target weight
5. **Round Bar Weight**: Round to achievable plate weights using standard gym math
6. **Store Band Information**: Record band weight and color in the database

#### Unit Handling
- **Pounds**: Direct calculation and rounding
- **Kilograms**: Convert to pounds for band calculations, then convert bar weight back to kg for storage

#### Example Calculation
For a 200 lb bench press in Week 1:
- Total target weight: 200 lbs
- Band weight target: 25% × 200 = 50 lbs
- Selected bands: 2 × Red bands (30 lbs each) = 60 lbs total (closest to 50 lbs target)
- Bar weight: 200 - 60 = 140 lbs
- Final setup: 140 lbs on bar + 60 lbs from bands = 200 lbs total resistance

> **Note**: For detailed information about 1RM calculation, exercise matching algorithms, and reference exercise detection, see [Weight Estimation Algorithms](docs/WEIGHT_ESTIMATION_ALGORITHMS.md).

### Accessory Movements
- **Week 1**: 55-65% intensity, 3 sets
- **Week 2**: 70-80% intensity, 3 sets
- **Week 3**: 70-80% intensity, 4 sets
- **Week 4**: 55-65% intensity (deload week), 3 sets

All accessory exercises in a workout use the same number of sets based on the week in the 4-week cycle, following undulating periodization (3, 3, 4, 3 sets for weeks 1-4). Reps per set vary based on intensity within the 6-15 range.

This periodization approach ensures progressive overload while incorporating planned deload weeks to prevent overtraining and promote recovery.

## Secondary Exercise Guidelines

Secondary exercises (additional primary movements) are intelligently selected to complement the primary movement and follow specific guidelines:

### Selection Criteria
- **Movement Similarity**: Prioritizes exercises with the same movement type as the primary exercise
- **Muscle Overlap**: Selects exercises that target similar muscle groups to the primary movement

- **User Preferences**: Respects user's exercise preferences and equipment availability

### Training Parameters
- **Intensity**: 80-90% of 1RM
- **Sets**: 3-4 sets
- **Reps**: 5-8 reps per set
- **Rest**: 180-300 seconds between sets
- **Tempo**: Variable (may include eccentric, isometric, and concentric timing)

### Examples of Secondary Exercise Selection
- **Primary: Bench Press (horizontal push)** → **Secondary: Incline Bench Press (horizontal push)**
- **Primary: Overhead Press (vertical push)** → **Secondary: Push Press (vertical push)**
- **Primary: Squat (squat)** → **Secondary: Front Squat (squat)**

These guidelines provide additional volume for primary movement patterns while maintaining proper intensity for strength development and ensuring movement pattern consistency.

## Set Scheme Generation

Each exercise generates multiple set schemes with:

- **AMRAP**: As Many Reps As Possible (for conditioning)
- **EMOM**: Every Minute On the Minute (for conditioning)
- **Tempo**: Eccentric, isometric, and concentric timing
- **Target Weight**: Based on user's 1RM and exercise intensity (with intelligent estimation)
- **Rest Periods**: Based on Prilepin's guidelines

## User Personalization

The service personalizes workouts based on:

### User Exercise Preferences
- Exercises to avoid
- Preferred exercises

### Available Equipment
- Filters exercises based on user's equipment
- Falls back to available exercises if preferred equipment is unavailable

### One Rep Max Data
- Uses actual 1RM values when available
- Falls back to intelligent weight estimation for exercises without 1RM data
- Automatically updates 1RM when users exceed current max
- Estimates weights using advanced exercise matching algorithms
- Supports multiple 1RM calculation formulas (Epley, Brzycki, Lombardi, O'Conner, Wathan)

### Weight Unit Preferences
- Supports both pounds and kilograms
- User-specific preferences per exercise
- Automatic unit conversion for calculations
- Equipment-based weight rounding in user's preferred unit

### Weak Muscle Groups
- Focuses accessory work on user's weak points
- Defaults to common weak areas for new users



## Error Handling

The service handles various edge cases:

- **Missing User Data**: Gracefully handles users with no exercise history
- **No Available Exercises**: Falls back to any available exercise when equipment constraints are too restrictive
- **Database Errors**: Proper error propagation and logging
- **Invalid Parameters**: Validation of input parameters
- **Weight Estimation Failures**: Conservative fallback estimates for bodyweight exercises
- **Equipment Lookup Failures**: Returns original weight if equipment data is unavailable

## Technical Implementation

### Movement Balance Implementation
The movement balance system is implemented through several key components:

#### MovementBalanceService
- **State Tracking**: Maintains `MovementBalanceState` across workout generation stages
- **Scoring Algorithm**: Calculates movement balance scores for exercise selection
- **Constraint Evaluation**: Evaluates balance pairs and volume ratios
- **Soft Constraint Logic**: Ensures constraints influence selection without blocking generation

#### Integration Points
- **ExerciseSelectionService**: Accepts `MovementBalanceState` parameter for informed exercise selection
- **WorkoutStageGenerationService**: Propagates movement balance state through stage generation
- **Program Generation**: Tracks movement balance across entire program generation process

### Program-Based Generation
The system now works with existing programs instead of creating new ones:
- **Input**: Program ID of existing program
- **Process**: Generates next week based on program's current week number
- **Output**: Updated program with new workouts
- **Benefits**: Maintains program continuity and user progress tracking

### Secondary Exercise Selection Implementation
The secondary exercise selection is implemented in the `ExerciseSelectionService` with the following key components:

#### `selectSimilarSecondaryExercise()` Method
- **Input**: Primary exercise, user equipment, preferences, available exercises
- **Process**:
  1. Filters exercises by user preferences and equipment
  2. Fetches muscle data for primary exercise from database
  3. Scores each potential secondary exercise using similarity algorithm
  4. Returns the highest-scoring exercise
- **Output**: `Mono<Exercise?>` - Reactive stream containing the selected exercise or null

#### Similarity Scoring Functions
- **`calculateExerciseSimilarityScore()`**: Main scoring function that combines movement type and muscle overlap
- **`calculateMovementTypeSimilarity()`**: Provides partial credit for related movement types
- **`calculateMuscleOverlapScore()`**: Calculates percentage overlap between primary and secondary exercise muscles

#### Database Integration
- Uses `ExerciseMuscleDAL` to fetch muscle relationships for exercises
- Reactive database queries ensure non-blocking operation
- Error handling with fallback to null when muscle data is unavailable

### Weight Calculation Pipeline
The weight calculation follows a sophisticated pipeline:

1. **Direct 1RM Check**: First attempts to use user's actual 1RM for the exercise
2. **Exercise Matching**: If no 1RM, finds similar exercises using multiple similarity metrics
3. **Reference Exercise Detection**: Dynamically identifies best reference exercises
4. **Weight Estimation**: Calculates estimated weight based on reference and similarity
5. **Equipment Rounding**: Rounds to achievable weights based on required equipment
6. **Unit Conversion**: Handles user's preferred weight units
7. **Band Integration**: For DE exercises, calculates bar and band weight distribution

### Testing

### Unit Tests
Comprehensive unit tests cover:
- Exercise selection logic
- Secondary exercise similarity scoring
- Movement balance constraints and scoring
- Set scheme generation
- Error handling
- Edge cases with missing data
- Individual component functionality (templates, periodization, exercise selection, stage generation, time calculations)
- Weight calculation and estimation
- Exercise matching algorithms
- Equipment weight rounding
- Band weight calculations

### Integration Tests
End-to-end tests verify:
- Complete workout generation workflow
- Database persistence
- API endpoint functionality
- User personalization features
- Component integration
- Weight estimation accuracy
- Equipment-based weight rounding
- Movement balance constraint enforcement
- Movement balance state tracking across workout stages

## API Endpoints

### Generate Next Week
- **Endpoint**: `POST /conjugate_workout_generator/{program_id}`
- **Description**: Generates the next week of workouts for an existing program
- **Parameters**:
  - `program_id` (path): ID of the existing program
- **Response**: Updated program with new workouts
- **Features**:
  - Automatic week progression based on program's current week
  - Program-based generation (works with existing programs)
  - Comprehensive error handling and validation

## Usage Examples

### Basic Program Generation
```bash
curl -X POST "http://localhost:8888/conjugate_workout_generator/1"
```

### Program with Week Progression
The system automatically determines the next week number from the program's current week and generates appropriate workouts with the correct periodization phase.

## Future Enhancements

Potential improvements include:
- Advanced weak point analysis based on performance data
- Machine learning for exercise selection optimization
- Integration with wearable devices for real-time feedback
- Configuration management for different training styles
- Plugin architecture for new exercise selection algorithms
- Feature extensions for new workout types or periodization schemes
- Enhanced reference exercise detection with machine learning
- Dynamic exercise similarity scoring based on user performance patterns
- Advanced weight estimation algorithms for specialized exercises
- Real-time exercise difficulty adjustment based on user feedback
- Integration with nutrition and recovery tracking
- Injury prevention and rehabilitation integration
- Advanced movement balance algorithms with machine learning
- Dynamic movement balance adjustment based on user performance feedback
- Movement pattern analysis and optimization
- Personalized movement balance ratios based on individual needs
