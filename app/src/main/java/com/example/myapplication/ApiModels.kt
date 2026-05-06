package com.example.myapplication

data class SignUpRequest(
    val name: String,
    val username: String,
    val email: String,
    val password: String,
    val role: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UserResponse(
    val userId: Int,
    val name: String,
    val username: String? = null,
    val email: String,
    val role: String,
    val dateOfBirth: String? = null,
    val fitnessLevel: String? = null
)

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val user: UserResponse? = null
)

data class LogWorkoutRequest(
    val userId: Int,
    val sport: String,
    val workoutDate: String,
    val duration: Int,
    val distanceKm: Double?,
    val avgPace: Double?,
    val avgHeartRate: Int?,
    val notes: String?,
    val exercises: List<ExerciseEntry>? = null
)

data class ExerciseEntry(
    val exerciseName: String,
    val setsCount: Int,
    val repsCount: Int,
    val weightKg: Double
)

data class BasicApiResponse(
    val success: Boolean,
    val message: String
)

data class WorkoutItem(
    val workoutId: Int,
    val userId: Int? = null,
    val sportName: String,
    val workoutDate: String,
    val duration: Int,
    val distanceKm: Double?,
    val avgPace: Double?,
    val avgHeartRate: Int?,
    val notes: String?,
    val exerciseSummaries: List<String>? = null,
    val exercises: List<ExerciseEntry>? = null
)

data class WorkoutsResponse(
    val success: Boolean,
    val workouts: List<WorkoutItem>
)

data class WorkoutDetailResponse(
    val success: Boolean,
    val workout: WorkoutItem?,
    val message: String? = null
)

data class UpdateWorkoutRequest(
    val sport: String,
    val workoutDate: String,
    val duration: Int,
    val distanceKm: Double?,
    val avgPace: Double?,
    val avgHeartRate: Int?,
    val notes: String?,
    val exercises: List<ExerciseEntry>? = null
)

data class AddClientRequest(
    val trainerId: Int,
    val clientEmail: String
)

data class ClientItem(
    val userId: Int,
    val name: String,
    val email: String,
    val role: String?,
    val fitnessLevel: String?
)

data class ClientListResponse(
    val success: Boolean,
    val clients: List<ClientItem>
)

data class CreateTrainingPlanRequest(
    val userId: Int,
    val createdByUserId: Int,
    val planName: String,
    val description: String,
    val startDate: String,
    val endDate: String
)

data class TrainingPlanItem(
    val planId: Int,
    val userId: Int,
    val planName: String,
    val description: String?,
    val startDate: String,
    val endDate: String
)

data class TrainingPlansResponse(
    val success: Boolean,
    val plans: List<TrainingPlanItem>
)

data class TrainingPlanDetailResponse(
    val success: Boolean,
    val plan: TrainingPlanItem? = null,
    val message: String? = null
)

data class UpdateTrainingPlanRequest(
    val planName: String,
    val description: String,
    val startDate: String,
    val endDate: String
)

data class DailyDistanceItem(
    val label: String,
    val distance: Double
)

data class DashboardStats(
    val thisWeekDistance: Double,
    val thisWeekDuration: Int,
    val thisWeekAvgPace: Double,
    val thisWeekAvgHeartRate: Double,
    val dailyDistance: List<DailyDistanceItem>
)

data class DashboardStatsResponse(
    val success: Boolean,
    val stats: DashboardStats
)

data class CompetitionItem(
    val competitionId: Int,
    val name: String,
    val location: String?,
    val competitionDate: String,
    val sportId: Int,
    val eventType: String,
    val sportName: String,
    val description: String?
)

data class CompetitionResultItem(
    val resultId: Int,
    val userId: Int,
    val competitionId: Int,
    val finishTime: Double,
    val position: Int?,
    val notes: String?,
    val isPersonalBest: Boolean,
    val name: String,
    val location: String?,
    val competitionDate: String,
    val eventType: String,
    val sportName: String
)

data class CreateCompetitionRequest(
    val userId: Int,
    val name: String,
    val location: String?,
    val competitionDate: String,
    val sportId: Int,
    val eventType: String,
    val description: String?
)

data class CreateCompetitionResultRequest(
    val userId: Int,
    val competitionId: Int,
    val finishTime: Double,
    val position: Int?,
    val notes: String?
)

data class CompetitionsResponse(
    val success: Boolean,
    val competitions: List<CompetitionItem>
)

data class CompetitionResultsResponse(
    val success: Boolean,
    val results: List<CompetitionResultItem>
)

data class UpdateCompetitionRequest(
    val name: String,
    val location: String?,
    val competitionDate: String,
    val sportId: Int,
    val eventType: String,
    val description: String?
)

data class UpdateCompetitionResultRequest(
    val finishTime: Double,
    val position: Int?,
    val notes: String?
)

data class ConversationItem(
    val conversationId: Int,
    val conversationType: String,
    val title: String?,
    val username: String?,
    val createdAt: String?,
    val lastMessage: String?,
    val lastMessageTime: String?
)

data class MessageItem(
    val messageId: Int,
    val conversationId: Int,
    val senderUserId: Int,
    val senderName: String,
    val messageText: String,
    val sentAt: String
)

data class CreateDirectConversationRequest(
    val user1Id: Int,
    val user2Id: Int
)

data class SendMessageRequest(
    val conversationId: Int,
    val senderUserId: Int,
    val messageText: String
)

data class ConversationResponse(
    val success: Boolean,
    val conversationId: Int?
)

data class ConversationsResponse(
    val success: Boolean,
    val conversations: List<ConversationItem>
)

data class MessagesResponse(
    val success: Boolean,
    val messages: List<MessageItem>
)

data class CreateGroupByUsernamesRequest(
    val createdByUserId: Int,
    val title: String,
    val usernames: List<String>
)

data class AddMemberByUsernameRequest(
    val username: String
)

data class LeaveGroupRequest(
    val userId: Int
)

data class GroupMemberItem(
    val userId: Int,
    val name: String,
    val username: String?,
    val participantRole: String,
    val joinedAt: String?
)

data class GroupMembersResponse(
    val success: Boolean,
    val groupTitle: String?,
    val members: List<GroupMemberItem>,
    val message: String? = null
)
