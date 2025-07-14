# Conjugate Workout Generator

## Overview

The Conjugate Workout Generator is a service that implements the Westside Barbell conjugate method for powerlifting, incorporating undulating periodization, exercise rotation, and personalized programming based on user preferences and available equipment.

## System Architecture

The service is built with a modular architecture that provides clear separation of concerns:

### Package Structure

```
src/main/kotlin/com/congen/service/conjugate/
├── ConjugateModels.kt           # Data classes and constants
├── ConjugateTemplates.kt        # Workout templates and selection logic
├── PrilepinGuidelinesService.kt # Prilepin chart and periodization logic
├── ExerciseSelectionService.kt  # Exercise selection and filtering logic
├── WorkoutStageGenerator.kt     # Individual stage generation helpers
└── SessionTimeCalculator.kt     # Session time allocation utilities
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
  - Exercise rotation history management
  - User preference filtering
  - Equipment availability checking
  - Weak muscle determination
  - Exercise filtering utilities
- **Benefits**: Separates exercise selection logic from workout generation

#### 5. WorkoutStageGenerator.kt
- **Purpose**: Generates individual workout stages and their components
- **Responsibilities**:
  - Workout stage creation
  - Programmed exercise creation
  - Set scheme generation (Prilepin-based, secondary, AMRAP/EMOM)
  - Target weight calculation
- **Benefits**: Focuses on stage-level generation details

#### 6. SessionTimeCalculator.kt
- **Purpose**: Handles session time calculations and accessory exercise count
- **Responsibilities**:
  - Time allocation calculations
  - Accessory exercise count determination
  - Utility methods for time management
- **Benefits**: Isolates time-related logic for easy modification

### Main Service (ConjugateWorkoutGeneratorService.kt)

The main service is a lightweight orchestrator that:
- Coordinates between the modular components
- Handles the high-level workflow
- Manages data flow between components

## Conjugate Method Principles

The conjugate method, developed by Louie Simmons at Westside Barbell, combines:

- **Max Effort (ME)**: Heavy singles, doubles, or triples at 85-92% 1RM
- **Dynamic Effort (DE)**: Speed work at 60-70% 1RM with explosive intent
- **Accessory Work**: Targeted muscle development and weak point training
- **Exercise Rotation**: Prevent accommodation by rotating exercises every 1-3 weeks

## Program Structure

### 2-Day Programs
Condensed conjugate approach (Phil Daru method):
- Day 1: ME Upper (horizontal push + vertical push)
- Day 2: DE Lower (squat + hinge)

### 3-Day Programs
Traditional conjugate with ME/DE/accessory split:
- Day 1: ME Upper (horizontal push + vertical push)
- Day 2: DE Lower (squat + hinge)
- Day 3: ME Lower (squat + hinge)

### 4-Day Programs
Extended conjugate with additional volume:
- Day 1: ME Upper (horizontal push + vertical push)
- Day 2: DE Lower (squat + hinge)
- Day 3: ME Lower (squat + hinge)
- Day 4: DE Upper (horizontal push + vertical pull)

## Exercise Categories

- **Primary**: Main compound movements (squat, bench, deadlift variations) - `isAccessory = false`
- **Secondary**: Additional primary movements (different from the main ME/DE exercise) - `isAccessory = false`
- **Accessory**: Isolation and weak point training - `isAccessory = true`
- **Conditioning**: Cardio and recovery work - `isAccessory = true`

## Exercise Selection Algorithm

The service uses a sophisticated algorithm to select exercises:

### Primary Exercise Selection
1. **Filter by Exercise Type**: Select ME/DE exercises (`is_accessory = false`)
2. **Filter by User Preferences**: Exclude exercises the user wants to avoid
3. **Filter by Equipment**: Only include exercises the user can perform with available equipment
4. **Exercise Rotation**: Prioritize unused exercises, then least recently used exercises
5. **Sort by Priority**: Sort by equipment options, targeted muscles, and exercise name

### Secondary Exercise Selection
Secondary exercises are selected to be similar to the primary exercise in terms of movement type and muscles worked:

1. **Similarity-Based Selection**: Uses a scoring algorithm to find exercises most similar to the primary movement
2. **Movement Type Matching**: Prioritizes exercises with the same movement type (e.g., "horizontal push")
3. **Muscle Overlap Analysis**: Calculates muscle overlap between primary and potential secondary exercises
4. **Movement Category Similarity**: Provides partial credit for related movement types:
   - Same category (push/pull): 50 points
   - Same plane (horizontal/vertical): 25 points  
   - Same body part focus (upper/lower): 15 points
5. **Rotation History Bonus**: Gives preference to less recently used exercises
6. **User Preferences & Equipment**: Applies the same filtering as primary exercises

#### Similarity Scoring Algorithm
The system calculates a similarity score for each potential secondary exercise:

- **Movement Type Match**: 100 points for exact match
- **Muscle Overlap**: Up to 50 points based on percentage of primary muscles targeted
- **Rotation Bonus**: 0-20 points based on usage frequency (less used = higher bonus)

The exercise with the highest total score is selected as the secondary movement.

### Accessory Exercise Selection
1. **Filter by Exercise Type**: Select accessory exercises (`is_accessory = true`)
2. **Filter by User Preferences**: Exclude exercises the user wants to avoid
3. **Filter by Equipment**: Only include exercises the user can perform with available equipment
4. **Exercise Rotation**: Prioritize unused exercises, then least recently used exercises
5. **Sort by Priority**: Sort by equipment options, targeted muscles, and exercise name

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

### Exercise Rotation Logic

The service ensures exercise variety by:
- Tracking exercise usage history by accessory type (primary/secondary vs accessory)
- Prioritizing exercises that haven't been used in the accessory type
- When all exercises have been used, selecting the least frequently used exercise
- This prevents accommodation and ensures balanced exercise selection

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

### Accessory Movements
- **Week 1**: 55-65% intensity
- **Week 2-3**: 70-80% intensity
- **Week 4**: 55-65% intensity (deload week)

This periodization approach ensures progressive overload while incorporating planned deload weeks to prevent overtraining and promote recovery.

## Secondary Exercise Guidelines

Secondary exercises (additional primary movements) are intelligently selected to complement the primary movement and follow specific guidelines:

### Selection Criteria
- **Movement Similarity**: Prioritizes exercises with the same movement type as the primary exercise
- **Muscle Overlap**: Selects exercises that target similar muscle groups to the primary movement
- **Exercise Variety**: Ensures rotation and prevents accommodation through usage tracking
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
- **Target Weight**: Based on user's 1RM and exercise intensity
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
- Falls back to default weights for new users
- Automatically updates 1RM when users exceed current max

### Weak Muscle Groups
- Focuses accessory work on user's weak points
- Defaults to common weak areas for new users

### Exercise History
- Tracks exercise rotation to ensure variety
- Prevents overuse of the same exercises

## Error Handling

The service handles various edge cases:

- **Missing User Data**: Gracefully handles users with no exercise history
- **No Available Exercises**: Falls back to any available exercise when equipment constraints are too restrictive
- **Database Errors**: Proper error propagation and logging
- **Invalid Parameters**: Validation of input parameters

## Technical Implementation

### Secondary Exercise Selection Implementation
The secondary exercise selection is implemented in the `ExerciseSelectionService` with the following key components:

#### `selectSimilarSecondaryExercise()` Method
- **Input**: Primary exercise, user equipment, preferences, available exercises, rotation history
- **Process**: 
  1. Filters exercises by user preferences and equipment
  2. Fetches muscle data for primary exercise from database
  3. Scores each potential secondary exercise using similarity algorithm
  4. Returns the highest-scoring exercise
- **Output**: `Mono<Exercise?>` - Reactive stream containing the selected exercise or null

#### Similarity Scoring Functions
- **`calculateExerciseSimilarityScore()`**: Main scoring function that combines movement type, muscle overlap, and rotation history
- **`calculateMovementTypeSimilarity()`**: Provides partial credit for related movement types
- **`calculateMuscleOverlapScore()`**: Calculates percentage overlap between primary and secondary exercise muscles
- **`calculateRotationBonus()`**: Awards points based on exercise usage frequency

#### Database Integration
- Uses `ExerciseMuscleDAL` to fetch muscle relationships for exercises
- Reactive database queries ensure non-blocking operation
- Error handling with fallback to null when muscle data is unavailable

### Testing

### Unit Tests
Comprehensive unit tests cover:
- Exercise selection logic
- Secondary exercise similarity scoring
- Set scheme generation
- Error handling
- Edge cases with missing data
- Individual component functionality (templates, periodization, exercise selection, stage generation, time calculations)

### Integration Tests
End-to-end tests verify:
- Complete workout generation workflow
- Database persistence
- API endpoint functionality
- User personalization features
- Component integration

## Usage Examples

### Basic 3-Day Program Generation
```bash
curl -X GET "http://localhost:8080/conjugate-workout-generator/1/generate?numDaysPerWeek=3"
```

### 2-Day Program for Week 2
```bash
curl -X GET "http://localhost:8080/conjugate-workout-generator/1/generate?currentWeekNumber=2&numDaysPerWeek=2"
```

### 4-Day Program with Custom Week
```bash
curl -X GET "http://localhost:8080/conjugate-workout-generator/1/generate?currentWeekNumber=5&numDaysPerWeek=4"
```

## Future Enhancements

Potential improvements include:
- Advanced weak point analysis based on performance data
- Machine learning for exercise selection optimization
- Integration with wearable devices for real-time feedback
- Seasonal periodization planning
- Competition preparation programs
- Configuration management for different training styles
- Plugin architecture for new exercise selection algorithms
- Performance optimization of individual components
- Feature extensions for new workout types or periodization schemes 