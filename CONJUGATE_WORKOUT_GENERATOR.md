# Conjugate Workout Generator

## Overview

The Conjugate Workout Generator is a service that implements the Westside Barbell conjugate method for powerlifting, incorporating undulating periodization, exercise rotation, and personalized programming based on user preferences and available equipment.

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

## API Endpoints

### Generate Workout Program

```
GET /conjugate-workout-generator/{userId}/generate
```

**Parameters:**
- `userId` (path): The ID of the user
- `currentWeekNumber` (query, optional): Current week number in the program (default: 1)
- `numDaysPerWeek` (query, optional): Number of training days per week (2, 3, or 4, default: 3)

**Response:**
```json
{
  "id": 1,
  "user_id": 1,
  "name": "Conjugate Powerlifting - Week 1",
  "description": "Conjugate powerlifting program with 3 days per week"
}
```

**Error Responses:**
- `400 Bad Request`: Invalid parameters (numDaysPerWeek must be 2, 3, or 4)
- `404 Not Found`: User doesn't exist
- `422 Unprocessable Entity`: Workout generation failed
- `500 Internal Server Error`: Database operations failed

## Exercise Selection Algorithm

The service uses a sophisticated algorithm to select exercises:

1. **Filter by Exercise Type**: 
   - Primary stage: Select ME/DE exercises (`is_accessory = false`)
   - Secondary stage: Select different primary exercises (`is_accessory = false`) that are not the same as the primary exercise
   - Accessory stage: Select accessory exercises (`is_accessory = true`)
2. **Filter by User Preferences**: Exclude exercises the user wants to avoid
3. **Filter by Equipment**: Only include exercises the user can perform with available equipment
4. **Exercise Rotation**: Prioritize unused exercises, then least recently used exercises
5. **Sort by Priority**: Sort by equipment options, targeted muscles, and exercise name

## Session Time Management

The service dynamically calculates the number of accessory exercises based on the user's desired session time:

### Time Allocation
- **Primary Movement (ME/DE)**: 10 minutes (warm-up, work sets, rest periods)
- **Secondary Movement**: 8 minutes (additional primary exercise, if applicable for upper body days)
- **Conditioning**: 10 minutes (for DE days)
- **Each Accessory Exercise**: 5 minutes

### Calculation Logic
The service determines accessory count using the formula:
```
Remaining Time = Session Time - Primary Time - Secondary Time - Conditioning Time
Number of Accessories = Remaining Time ÷ 5 minutes
```

### Examples
- **45-minute session, ME Upper**: 45 - 10 - 8 = 27 minutes → 5 accessories
- **40-minute session, DE Lower**: 40 - 10 - 10 = 20 minutes → 4 accessories
- **60-minute session, ME Lower**: 60 - 10 = 50 minutes → 10 accessories

This ensures workouts fit within the user's time constraints while maintaining proper exercise balance.

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

Secondary exercises (additional primary movements) follow specific guidelines:

- **Intensity**: 80-90% of 1RM
- **Sets**: 3-4 sets
- **Reps**: 5-8 reps per set
- **Rest**: 180-300 seconds between sets
- **Tempo**: Variable (may include eccentric, isometric, and concentric timing)

These guidelines provide additional volume for primary movement patterns while maintaining proper intensity for strength development.

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

## Database Schema

The service integrates with existing models:

- **Program**: Contains the overall workout program
- **ProgrammedWorkout**: Individual workouts within the program
- **WorkoutStage**: Stages within each workout (primary, secondary, accessory, conditioning)
- **ProgrammedExercise**: Exercises within each stage
- **SetScheme**: Individual sets with specific parameters

## Error Handling

The service handles various edge cases:

- **Missing User Data**: Gracefully handles users with no exercise history
- **No Available Exercises**: Falls back to any available exercise when equipment constraints are too restrictive
- **Database Errors**: Proper error propagation and logging
- **Invalid Parameters**: Validation of input parameters

## Testing

### Unit Tests
Comprehensive unit tests cover:
- Exercise selection logic
- Set scheme generation
- Error handling
- Edge cases with missing data

### Integration Tests
End-to-end tests verify:
- Complete workout generation workflow
- Database persistence
- API endpoint functionality
- User personalization features

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