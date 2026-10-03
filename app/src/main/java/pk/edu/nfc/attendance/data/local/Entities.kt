package pk.edu.nfc.attendance.data.local

import androidx.room.*

enum class UserRole { ADMIN, HOD, TEACHER, STUDENT }
enum class AttendanceStatus { PRESENT, ABSENT, LEAVE }
enum class SyncState { SYNCED, PENDING_SYNC, SYNC_FAILED }
enum class ClassType { THEORY, LAB }

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val fullName: String,
    val role: UserRole,
    val departmentId: String,
    val identifierCode: String, // Roll Number or Employee ID
    val email: String,
    val isActive: Boolean = true
)

@Entity(tableName = "departments")
data class DepartmentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val code: String,
    val hodUserId: String? = null
)

@Entity(tableName = "programs")
data class ProgramEntity(
    @PrimaryKey val id: String,
    val departmentId: String,
    val name: String,
    val code: String
)

@Entity(tableName = "academic_sessions")
data class AcademicSessionEntity(
    @PrimaryKey val id: String,
    val programId: String,
    val name: String, // e.g. "2023-2027"
    val isCurrent: Boolean = true
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val code: String,
    val name: String,
    val creditHoursTheory: Int,
    val creditHoursLab: Int,
    val semesterNumber: Int
)

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey val id: String,
    val roomNumber: String,
    val building: String,
    val capacity: Int,
    val isLab: Boolean
)

@Entity(tableName = "timetable_entries")
data class TimetableEntity(
    @PrimaryKey val id: String,
    val dayOfWeek: String, // "MONDAY", "TUESDAY", etc.
    val startTime: String, // "08:30"
    val endTime: String,   // "09:30"
    val teacherId: String,
    val subjectId: String,
    val roomId: String,
    val programId: String,
    val section: String,
    val semesterNumber: Int,
    val classType: ClassType
)

@Entity(tableName = "attendance_drafts")
data class AttendanceDraftEntity(
    @PrimaryKey val id: String, // UUID
    val timetableEntryId: String,
    val date: String,          // "YYYY-MM-DD"
    val takenByTeacherId: String,
    val syncState: SyncState = SyncState.PENDING_SYNC,
    val isLocked: Boolean = false
)

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = AttendanceDraftEntity::class,
            parentColumns = ["id"],
            childColumns = ["draftId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("draftId"), Index("studentId")]
)
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val draftId: String,
    val studentId: String,
    val status: AttendanceStatus
)

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val publishedDate: String,
    val targetRole: String
)
