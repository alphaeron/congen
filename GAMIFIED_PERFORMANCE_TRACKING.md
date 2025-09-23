# 🎮 Gamified Performance Tracking System

A comprehensive fitness tracking system that gamifies workout performance using RPG-style mechanics, inspired by Solo Leveling and modern fitness apps. The system transforms raw performance metrics into engaging visualizations and progress tracking.

## 🌟 Features Overview

### 📊 Performance Metrics & Athleticism Score
- **6 Core Performance Domains**: Strength, Power, Endurance, Recovery, Stamina, Speed
- **Non-linear Scoring**: Logarithmic and tanh scaling for realistic difficulty curves
- **Athleticism Level**: 1-20+ levels with diminishing returns at higher levels
- **Real-time Calculations**: Automatic score updates based on test results

### ❤️ HP / 🔷 MP / ⚡ Fatigue System

The system tracks three distinct metrics inspired by RPG mechanics, each with unique characteristics:

#### ❤️ HP (Health Points) - Long-term Physical Durability
- **Represents**: Structural/tissue level wear and tear
- **Scope**: Cumulative mechanical stress, injury, overtraining
- **Primary Sources**: Cumulative strain (Whoop), long-term performance drop, injury flags
- **Recovery Timeframe**: Days to weeks
- **Calculation**: `previous_hp - (strain > 12 ? (strain - 12) * 0.5 : 0) + recovery_score * 0.1`
- **Analogy**: Body's armor / structural integrity

#### 🔷 MP (Magic Points) - Short-term Neurological/Cognitive Energy
- **Represents**: CNS & mental readiness
- **Scope**: Neurological and cognitive energy
- **Primary Sources**: Current HRV, sleep stages (REM/deep), recovery score
- **Recovery Timeframe**: Overnight to 2-3 days
- **Calculation**: `max(0, min(100, (hrv - 30) * 3 + rem_sleep * 0.5))`
- **Analogy**: Mind's energy bar / casting power

#### ⚡ Fatigue - Immediate Performance Inhibition
- **Represents**: Session-to-session exhaustion
- **Scope**: Acute workload, energy depletion
- **Primary Sources**: Acute strain, session RPE, hours of sleep, subjective tiredness
- **Recovery Timeframe**: Hours to 1-2 days
- **Calculation**: `min(100, strain * 5 + subjective_tiredness * 10)`
- **Analogy**: Body's status effect / temporary debuff

#### 🔄 How They Interact
- **After a heavy leg day**: HP ↓ Slightly, MP ↓ Moderate, Fatigue 🔻 High
- **After 3 weeks of no deload**: HP ↓ Significantly, MP ↓ Low, Fatigue 🔺 Baseline
- **After a night of poor sleep**: HP ↔️ No change, MP ↓ Big drop, Fatigue 🔻 Moderate
- **Sick or injured**: HP 🔻 Major drop, MP ↓ Likely, Fatigue 🔻 High
- **After a full rest day with great sleep**: HP ↑ Slightly, MP ↑ Full, Fatigue 🔺 Recovered

### 📅 Weekly Test Protocol Tracker
- **Structured Testing Schedule**:
  - Monday: Vertical Jump (MyJump2 app)
  - Wednesday: Push-ups & Pull-ups (max in 1 minute)
  - Friday: 12-minute Run + VO₂ Max calculation
  - Any Day: HRV & HR Recovery (Oura/Whoop or manual)
  - Any Day: Reflex Speed (Human Benchmark)
- **Progress Tracking**: Visual completion status with editable results
- **Auto-integration**: Completed tests automatically update performance metrics

### 🧭 RPG-Style Adventurer Status Card
- **Character Profile**: Name, class, level, rank, guild
- **Status Bars**: HP/MP/Fatigue with tooltips and color coding
- **Core Stats**: VO₂ Max, Wilks score, jump height, HRV, sleep, recovery
- **Collapsible Sections**: Skills, equipment, weekly quests

### 🎯 Smart Skill Generation
- **Threshold-based Skills**: Auto-generated based on performance levels
- **Skill Tiers**: Bronze, Silver, Gold, Platinum with different requirements
- **Progress Tracking**: Visual progress bars for skill development
- **Motivational System**: Unlock new skills as performance improves

### 📈 Performance Visualization
- **Radar Chart**: 6-domain performance visualization with fitness terminology
- **Status Bars**: RPG-style HP/MP/Fatigue indicators
- **Progress Tracking**: Weekly test completion and skill advancement

## 🏗️ Architecture

### Backend Components

#### Models
- **UserPerformanceMetrics**: Raw performance data and wearable integration
- **UserPerformanceScores**: Calculated scores, HP/MP/Fatigue, and skills
- **UserWeeklyTest**: Weekly test protocol tracking and results

#### Services
- **PerformanceScoringService**: Core calculation engine with normalization functions
- **PerformanceTrackingService**: Orchestrates metrics, scores, and weekly tests

#### Data Access Layer
- **UserPerformanceMetricsDAL**: CRUD operations for performance metrics
- **UserPerformanceScoresDAL**: CRUD operations for calculated scores
- **UserWeeklyTestDAL**: CRUD operations for weekly test protocol

#### API Endpoints
- `POST /performance/metrics` - Submit performance metrics
- `GET /performance/scores` - Get current performance scores
- `GET /performance/metrics` - Get current performance metrics
- `POST /performance/weekly-test` - Submit weekly test results
- `GET /performance/weekly-test/current` - Get current week's test protocol
- `GET /performance/weekly-test/range` - Get weekly tests in date range

### Frontend Components

#### Core Components
- **PerformanceDashboard**: Main dashboard with tabbed interface
- **AdventurerStatusCard**: RPG-style character status display
- **StatusBars**: HP/MP/Fatigue visualization
- **PerformanceRadarChart**: 6-domain performance radar chart
- **WeeklyTestTracker**: Weekly test protocol management
- **SkillGenerator**: Auto-generated skills based on performance

#### API Integration
- **performanceTracking.ts**: API functions for all performance endpoints
- **React Query Integration**: Caching, loading states, and error handling

## 🧮 Scoring Algorithms

### Individual Metric Scoring

#### Relative Strength (Wilks Score)
```kotlin
score = min(100, 20 * log10(wilks / 250))
```
- Typical range: 250 (average) to 500+ (elite)
- Logarithmic scaling for diminishing returns

#### Explosiveness (Vertical Jump)
```kotlin
score = min(100, 25 * log10(jumpCm / 30))
```
- Typical range: 30cm (low) to 70cm (elite)
- Logarithmic scaling for power development

#### Aerobic Capacity (VO₂ Max)
```kotlin
score = min(100, 25 * log10(vo2Max / 35))
```
- Typical range: 35 (average) to 70+ (elite)
- Logarithmic scaling for endurance

#### Recovery (HR Recovery)
```kotlin
score = min(100, 2 * (hrRecovery - 20))
```
- Typical range: 20 (poor) to 60+ (excellent)
- Linear scaling for recovery ability

#### Muscular Endurance (Push-ups)
```kotlin
score = min(100, 3 * log10(pushups + 1))
```
- Typical range: 20 (low) to 60+ (high)
- Logarithmic scaling for endurance

#### Reaction Time
```kotlin
score = max(0, min(100, 100 - ((reactionTimeMs - 300) * 0.33)))
```
- Typical range: 300ms (fast) to 600ms (slow)
- Inverse linear scaling (lower is better)

### HP/MP/Fatigue Calculations

#### HP (Health Points)
```kotlin
hp = (vo2Score + enduranceScore + recoveryScore) / 3
```
- Physical resilience based on aerobic capacity, muscular endurance, and recovery

#### MP (Magic Points)
```kotlin
mp = (recoveryScore + reactionScore + explosivenessScore) / 3
```
- Neural readiness based on recovery, reaction time, and explosiveness

#### Fatigue
```kotlin
fatigue = min(100, strain * 5 + tiredness * 10 + sleepAdjustment)
```
- Session-level depletion based on strain, sleep, and subjective factors

### Athleticism Level
```kotlin
athleticismScore = (tanh((averageScore - 50) / 15) + 1) * 50
level = 1 + (log(athleticismScore / 5 + 1) / log(2)) * 3
```
- Tanh scaling for final score with diminishing returns
- Logarithmic level progression (1-20+)

## 🎮 Gamification Elements

### Character Progression
- **Levels**: 1-20+ with increasing difficulty
- **Ranks**: D, C, C+, B, B+, A, A+, S, S+ based on level
- **Classes**: Auto-assigned based on performance profile (Warrior, Ranger, Paladin, Rogue, Monk, Barbarian)
- **Guilds**: Unlocked based on level progression

### Skill System
- **Bronze Skills**: Basic performance levels (40-59 score)
- **Silver Skills**: Intermediate performance (60-79 score)
- **Gold Skills**: Advanced performance (80-89 score)
- **Platinum Skills**: Elite performance (90+ score)

### Weekly Quests
- **Test Completion**: Complete weekly test protocol
- **Performance Goals**: Beat personal records
- **Recovery Targets**: Achieve high-recovery days
- **Level Progression**: Reach specific athleticism levels

## 🔌 Wearable Integration

### Supported Devices
- **Whoop**: Strain, recovery, HRV, sleep score
- **Oura**: HRV, VO₂ max estimate, sleep stages
- **Manual Input**: For users without wearables

### Data Flow
1. Wearable data syncs to performance metrics
2. Metrics automatically calculate updated scores
3. HP/MP/Fatigue values update in real-time
4. Skills unlock based on new performance levels

## 📱 User Interface

### Dashboard Layout
- **Overview Tab**: Adventurer card, status bars, radar chart
- **Metrics Tab**: Full-width performance visualization
- **Weekly Tests Tab**: Test protocol tracker
- **Skills Tab**: Skill progression and achievements

### Visual Design
- **RPG Theme**: Gradient backgrounds, status bars, character cards
- **Color Coding**: Health indicators, skill tiers, progress states
- **Responsive Design**: Mobile-friendly with collapsible sections
- **Accessibility**: Tooltips, ARIA labels, keyboard navigation

## 🚀 Getting Started

### Backend Setup
1. Run database migrations to create performance tracking tables
2. Deploy updated backend with new controllers and services
3. Configure wearable API integrations (optional)

### Frontend Integration
1. Import PerformanceDashboard component
2. Add to main Dashboard navigation
3. Configure API endpoints and authentication

### User Onboarding
1. Complete initial performance assessment
2. Set up wearable device integration (optional)
3. Begin weekly test protocol
4. Track progress and unlock skills

## 🔧 Configuration

### Performance Thresholds
- Customize skill unlock requirements
- Adjust level progression curves
- Modify HP/MP/Fatigue calculations

### Wearable Integration
- Configure API keys for Whoop/Oura
- Set up data sync intervals
- Customize metric mappings

### UI Customization
- Modify color schemes and themes
- Adjust component layouts
- Customize skill descriptions

## 📊 Analytics & Insights

### Performance Trends
- Track athleticism score over time
- Monitor HP/MP/Fatigue patterns
- Analyze skill progression

### Weekly Test Analytics
- Completion rates and trends
- Performance improvements
- Test result correlations

### Gamification Metrics
- Level progression rates
- Skill unlock frequency
- User engagement patterns

## 🔮 Future Enhancements

### Advanced Features
- **PvP Challenges**: Compare performance with other users
- **Guild System**: Team-based challenges and competitions
- **Seasonal Events**: Limited-time challenges and rewards
- **AI Coaching**: Personalized recommendations based on performance

### Integration Expansions
- **Additional Wearables**: Apple Watch, Garmin, Fitbit
- **Smart Home**: Integration with recovery devices
- **Social Features**: Share achievements and progress

### Advanced Analytics
- **Predictive Modeling**: Forecast performance improvements
- **Injury Prevention**: Identify overtraining risks
- **Optimization**: Suggest training adjustments

## 🎯 Success Metrics

### User Engagement
- Daily active users on performance dashboard
- Weekly test completion rates
- Skill unlock frequency

### Performance Improvement
- Average athleticism score increases
- HP/MP/Fatigue optimization
- Test result improvements over time

### Gamification Effectiveness
- Level progression rates
- Quest completion rates
- User retention in performance tracking

---

*This gamified performance tracking system transforms traditional fitness metrics into an engaging, RPG-style experience that motivates users to improve their athletic performance through structured testing, skill progression, and visual feedback.*
