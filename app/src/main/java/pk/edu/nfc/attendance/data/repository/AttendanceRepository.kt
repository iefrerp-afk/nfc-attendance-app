package pk.edu.nfc.attendance.data.repository

import kotlinx.coroutines.flow.Flow
import pk.edu.nfc.attendance.data.local.*
import java.util.UUID

class AttendanceRepository(private val dao: AppDao) {
    fun getTeacherClasses(teacherId: String): Flow<List<TimetableEntity>> = dao.getTimetableForTeacher(teacherId)
    fun getAllStudents(): Flow<List<UserEntity>> = dao.getUsersByRole(UserRole.STUDENT)
    fun getStudentPresentCount(id: String): Flow<Int> = dao.getStudentPresentCount(id)
    fun getStudentTotalCount(id: String): Flow<Int> = dao.getStudentTotalCount(id)
    fun getNotices(): Flow<List<NoticeEntity>> = dao.getAllNotices()

    suspend fun recordAttendance(
        timetableEntryId: String,
        teacherId: String,
        records: Map<String, AttendanceStatus>
    ): Result<String> {
        return try {
            val draftId = UUID.randomUUID().toString()
            val draft = AttendanceDraftEntity(
                id = draftId,
                timetableEntryId = timetableEntryId,
                date = "2026-10-03",
                takenByTeacherId = teacherId,
                syncState = SyncState.PENDING_SYNC,
                isLocked = true
            )
            val recordEntities = records.map {
                AttendanceRecordEntity(draftId = draftId, studentId = it.key, status = it.value)
            }
            dao.insertDraft(draft)
            dao.insertRecords(recordEntities)
            
            // Mark as synced locally
            dao.markAttendanceSubmitted(draftId, SyncState.SYNCED)
            Result.success(draftId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
