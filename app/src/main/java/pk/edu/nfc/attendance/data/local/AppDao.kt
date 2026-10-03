package pk.edu.nfc.attendance.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Users & Roles
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE username = :query OR identifierCode = :query LIMIT 1")
    suspend fun findUserByCredentials(query: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersByRole(role: UserRole): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    // Academic Structures
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartment(dept: DepartmentEntity)

    @Query("SELECT * FROM departments")
    fun getAllDepartments(): Flow<List<DepartmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Query("SELECT * FROM subjects")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: RoomEntity)

    @Query("SELECT * FROM rooms")
    fun getAllRooms(): Flow<List<RoomEntity>>

    // Timetable & Conflict Engine
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableEntry(entry: TimetableEntity)

    @Query("SELECT * FROM timetable_entries WHERE teacherId = :teacherId")
    fun getTimetableForTeacher(teacherId: String): Flow<List<TimetableEntity>>

    @Query("SELECT * FROM timetable_entries WHERE programId = :programId AND semesterNumber = :semester")
    fun getTimetableForClass(programId: String, semester: Int): Flow<List<TimetableEntity>>

    @Query("""
        SELECT * FROM timetable_entries 
        WHERE dayOfWeek = :day 
        AND ((startTime <= :start AND endTime > :start) OR (startTime < :end AND endTime >= :end))
    """)
    suspend fun getOverlappingEntries(day: String, start: String, end: String): List<TimetableEntity>

    // Attendance Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: AttendanceDraftEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<AttendanceRecordEntity>)

    @Query("UPDATE attendance_drafts SET syncState = :state, isLocked = 1 WHERE id = :draftId")
    suspend fun markAttendanceSubmitted(draftId: String, state: SyncState)

    @Query("SELECT * FROM attendance_drafts WHERE syncState = 'PENDING_SYNC'")
    suspend fun getPendingSyncDrafts(): List<AttendanceDraftEntity>

    @Query("SELECT * FROM attendance_records WHERE draftId = :draftId")
    suspend fun getRecordsForDraft(draftId: String): List<AttendanceRecordEntity>

    @Query("""
        SELECT COUNT(*) FROM attendance_records r 
        INNER JOIN attendance_drafts d ON r.draftId = d.id 
        WHERE r.studentId = :studentId AND r.status = 'PRESENT'
    """)
    fun getStudentPresentCount(studentId: String): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM attendance_records r 
        INNER JOIN attendance_drafts d ON r.draftId = d.id 
        WHERE r.studentId = :studentId
    """)
    fun getStudentTotalCount(studentId: String): Flow<Int>

    // Notices
    @Query("SELECT * FROM notices ORDER BY publishedDate DESC")
    fun getAllNotices(): Flow<List<NoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeEntity)
}
